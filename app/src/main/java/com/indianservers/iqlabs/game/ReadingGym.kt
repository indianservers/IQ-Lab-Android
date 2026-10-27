package com.indianservers.iqlabs.game

import androidx.compose.ui.graphics.Color
import com.indianservers.iqlabs.model.*
import kotlin.random.Random

/** Offline, seed-reproducible reading practice. Scores describe practice performance only. */
object ReadingGym {
    val names = listOf("Reverse Word Reading", "Vowel-Free Reading", "Scrambled Middle Letters", "Mirror Reading", "Alternating Case Reading", "Missing-Letter Reconstruction", "Peripheral Vision Reading", "Flash Reading", "Number–Letter Substitution", "Reverse Sentence Order", "Hidden Word Detection", "Semantic Interference Reading", "Dual-Task Reading", "Contradiction Detection", "Multi-Layer Reading Challenge")
    private val rules = listOf(
        "Decode each reversed word while keeping word order.", "Restore the missing vowels using context.",
        "Read words whose middle letters are shuffled.", "Decode the horizontally mirrored passage.",
        "Read through alternating uppercase and lowercase letters.", "Restore the hidden letters using context.",
        "Keep your gaze on the central cross and remember the word on the requested side.",
        "Read the briefly displayed passage, then answer from memory.", "Decode the letters using A=1 through Z=26; slashes separate words.",
        "Restore the sentence from its reversed word order.", "Find the requested meaningful word inside the letter string.",
        "Identify the ink color while ignoring the color word.", "Remember the number sequence, read the passage, then answer both checks.",
        "Identify the statement that contradicts the passage.", "Decode a passage combining reversed words, missing vowels and scrambled middles."
    )
    fun index(id: String) = id.removePrefix("reading-gym-").toIntOrNull()?.minus(1)?.takeIf { id.startsWith("reading-gym-") && it in names.indices }
    fun contains(id: String) = index(id) != null
    val games = names.mapIndexed { i, name ->
        GameDefinition("reading-gym-${i+1}", name, rules[i], LevelId.Adults, Category.Reading,
            listOf("reading", "100 levels", "dynamic practice"), 1, 3, 8, "Aa", Color(0xFF7056C8),
            "${rules[i]} Choose the matching answer. Each session has 10 fresh practice questions. Select levels 1–100 in difficulty controls. Practice can improve these skills; evidence for permanent general IQ increases is limited.",
            "Skill-specific accuracy, comprehension or recall, response time and progress; not a validated IQ score.",
            20, "Always available", BestScoreType.HigherIsBetter, ImplementationStatus.Playable,
            "Large text and untimed answer selection. Flash and peripheral tasks require visual presentation; use Relaxed mode for more time.")
    }
    private val people = listOf("Maya", "Rahul", "Nora", "Arun", "Leela", "Omar", "Isha", "Ravi")
    private val places = listOf("garden", "library", "museum", "market", "station", "forest", "harbor", "school")
    private val objects = listOf("silver lantern", "yellow notebook", "wooden basket", "purple ribbon", "green compass", "orange parcel", "blue feather", "golden button")
    private val colors = listOf("RED", "BLUE", "GREEN", "PURPLE")
    fun create(game: GameDefinition, random: Random, tier: Int): Question {
        val kind = requireNotNull(index(game.id))
        val level = tier.coerceIn(1, 100)
        val person = people.random(random)
        val place = places.random(random)
        val item = objects.random(random)
        val number = random.nextInt(2, 30)
        val sentence = "$person carried the $item to the $place before lunch and counted $number birds beside the gate."
        val extra = List((level - 1) / 20) { "Later ${people.random(random)} walked past the ${places.random(random)} and noticed a ${objects.random(random)} near the path." }.joinToString(" ")
        val passage = "$sentence $extra".trim()
        fun scramble(word: String): String {
            if (word.length < 4) return word
            val middle = word.substring(1, word.lastIndex)
            var mixed = middle.toList().shuffled(random).joinToString("")
            if (mixed == middle) mixed = middle.drop(1) + middle.first()
            return word.first() + mixed + word.last()
        }
        fun transform(text: String, mode: Int): String = text.split(" ").mapIndexed { i, word ->
            when (mode) {
                0 -> word.reversed()
                1 -> word.replace(Regex("[aeiouAEIOU]"), "")
                2 -> scramble(word)
                4 -> word.mapIndexed { j, c -> if ((i+j)%2 == 0) c.uppercaseChar() else c.lowercaseChar() }.joinToString("")
                5 -> word.mapIndexed { j, c -> if (j > 0 && c.isLetter() && random.nextDouble() < .2 + level * .003) '_' else c }.joinToString("")
                8 -> word.filter { it.isLetter() }.uppercase().map { (it-'A'+1).toString() }.joinToString("-")
                14 -> when(i%3) { 0 -> word.reversed(); 1 -> word.replace(Regex("[aeiouAEIOU]"), ""); else -> scramble(word) }
                else -> word
            }
        }.joinToString(if (mode == 8) " / " else " ")
        var answer = item
        var choices = listOf(item) + objects.filter { it != item }.shuffled(random).take(3)
        var prompt = "${rules[kind]} What did $person carry?"
        var display = transform(passage, kind)
        var memory = ""
        var ink = ""
        when (kind) {
            3 -> display = passage
            6 -> {
                val words = places.shuffled(random).take(3)
                val side = random.nextBoolean()
                display = "${words[0]}     +     ${words[2]}"
                answer = words[if(side) 0 else 2]
                choices = listOf(answer) + places.filter { it != answer }.shuffled(random).take(3)
                prompt = "Keep looking at +. Which word appeared on the ${if(side) "left" else "right"}?"
            }
            9 -> display = passage.split(" ").reversed().joinToString(" ")
            10 -> {
                answer = places.random(random).uppercase()
                choices = listOf(answer) + places.map { it.uppercase() }.filter { it != answer }.shuffled(random).take(3)
                fun noise() = List(8 + level / 5) { "XQZJ".random(random) }.joinToString("")
                display = noise() + answer + noise()
                prompt = "Find the place hidden in the letter string."
            }
            11 -> {
                ink = colors.random(random)
                display = colors.filter { it != ink }.random(random)
                answer = ink
                choices = colors
                prompt = "Identify the INK color. Ignore the word."
            }
            12 -> {
                memory = List(3 + level / 25) { random.nextInt(0,10) }.joinToString("–")
                answer = "$item | $memory"
                choices = listOf(answer, "${objects.first { it != item }} | $memory", "$item | ${memory.reversed()}", "${objects.last { it != item }} | ${memory.reversed()}").distinct()
                // A palindrome must still have four distinct alternatives.
                if (choices.size < 4) choices = listOf(answer) + objects.filter { it != item }.take(3).map { "$it | $memory" }
                prompt = "What did $person carry, and what was the number sequence?"
            }
            13 -> {
                display = "$passage The gate remained closed throughout the visit."
                answer = "$person passed through the open gate during the visit."
                choices = listOf(answer, "$person carried a $item.", "$person counted $number birds.", "$person went to the $place before lunch.")
                prompt = "Which statement contradicts the passage?"
            }
        }
        val exposure = when(kind) { 6 -> (2200 - level * 15).coerceAtLeast(650); 7 -> (6500 - level * 40).coerceAtLeast(2000); else -> 0 }
        return Question("${game.name}: $prompt", choices.shuffled(random), answer, "Answer: $answer", PlayStyle.Reading,
            listOf("reading-gym", kind.toString(), display, exposure.toString(), memory, ink, level.toString()))
    }
}
