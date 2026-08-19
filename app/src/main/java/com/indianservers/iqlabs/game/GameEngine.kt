package com.indianservers.iqlabs.game

import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.Category
import kotlin.math.max
import kotlin.random.Random

data class Question(
    val prompt: String,
    val choices: List<String>,
    val answer: String,
    val detail: String,
)

data class GameResult(
    val score: Int,
    val correct: Int,
    val incorrect: Int,
    val xp: Int,
    val bestCombo: Int,
    val personalBest: Boolean,
    val stars: Int = 0,
) {
    val accuracy: Int = if (correct + incorrect == 0) 0 else correct * 100 / (correct + incorrect)
}

object ScoreEngine {
    fun score(correct: Int, incorrect: Int, bestCombo: Int, difficulty: Int, secondsRemaining: Int): Int {
        val base = correct * 100
        val combo = bestCombo * 18
        val speed = max(0, secondsRemaining) * 3
        val penalty = incorrect * 28
        return max(0, (base + combo + speed - penalty) * (100 + difficulty * 12) / 100)
    }

    fun xp(correct: Int, incorrect: Int, difficulty: Int, reward: Int): Int {
        val total = correct + incorrect
        val accuracy = if (total == 0) 0 else correct * 100 / total
        return max(5, reward * (50 + accuracy + difficulty * 6) / 160)
    }

    fun isPersonalBest(newScore: Int, previousBest: Int?): Boolean = previousBest == null || newScore > previousBest
}

object QuestionFactory {
    fun questionsFor(game: GameDefinition, seed: Int, count: Int = 6, tier: Int = game.difficulty): List<Question> {
        val random = Random(seed)
        val effectiveTier = tier.coerceIn(1, 12)
        return when (game.id) {
            "flash-sum" -> flashSum(random, count, effectiveTier)
            "memory-grid" -> memoryGrid(random, count, effectiveTier)
            "word-sprint" -> wordSprint(random, count)
            "sequence-detective" -> sequenceDetective(random, count, effectiveTier)
            "pattern-pulse" -> patternPulse(random, count, effectiveTier)
            "quick-match" -> quickMatch(random, count)
            else -> genericQuestions(game.copy(difficulty = effectiveTier), random, count)
        }
    }

    private fun flashSum(random: Random, count: Int, difficulty: Int) = List(count) {
        val numbers = List(3 + difficulty) { random.nextInt(-4, 12 + difficulty * 2) }.filterNot { it == 0 }
        val answer = numbers.sum()
        choiceQuestion("Add quickly: ${numbers.joinToString("  ")}", answer, random, "The total is $answer.")
    }

    private fun memoryGrid(random: Random, count: Int, difficulty: Int) = List(count) {
        val cells = (1..16).shuffled(random).take(3 + difficulty / 2).sorted()
        val answer = cells.joinToString("-")
        val choices = buildChoices(answer, random) {
            (1..16).shuffled(random).take(cells.size).sorted().joinToString("-")
        }
        Question("Remember these lit cells: $answer", choices, answer, "The lit cells were $answer.")
    }

    private fun sequenceDetective(random: Random, count: Int, difficulty: Int) = List(count) {
        val start = random.nextInt(2, 12)
        val step = random.nextInt(2, 5 + difficulty)
        val missingIndex = random.nextInt(1, 4)
        val seq = List(5) { start + it * step }
        val answer = seq[missingIndex]
        val visible = seq.mapIndexed { index, value -> if (index == missingIndex) "?" else value.toString() }
        choiceQuestion("Find the missing number: ${visible.joinToString(", ")}", answer, random, "The sequence adds $step each time.")
    }

    private fun wordSprint(random: Random, count: Int): List<Question> {
        val bank = listOf(
            Triple("A comet crossed the cold sky while Mira counted three blue flashes.", "How many flashes did Mira count?", "three"),
            Triple("The lab door opened after the green crystal pulsed twice.", "What color was the crystal?", "green"),
            Triple("Ravi solved the maze before lunch and drew a star beside it.", "When did Ravi solve the maze?", "before lunch"),
            Triple("The tiny robot sorted red cubes, silver rings and yellow cones.", "Which object was silver?", "rings"),
        )
        return List(count) {
            val item = bank[random.nextInt(bank.size)]
            val wrong = listOf("one", "two", "blue", "after lunch", "cubes", "cones", "purple").filterNot { it == item.third }.shuffled(random).take(3)
            Question("${item.first}\n\n${item.second}", (wrong + item.third).shuffled(random), item.third, "The passage states: ${item.third}.")
        }
    }

    private fun patternPulse(random: Random, count: Int, difficulty: Int) = List(count) {
        val symbols = listOf("Cyan", "Violet", "Lime", "Amber", "Rose")
        val pattern = List(3 + difficulty / 2) { symbols[random.nextInt(symbols.size)] }
        val answer = pattern.joinToString(" → ")
        val choices = buildChoices(answer, random) { pattern.shuffled(random).joinToString(" → ") }
        Question("Repeat the pulse: $answer", choices, answer, "The original pulse was $answer.")
    }

    private fun quickMatch(random: Random, count: Int): List<Question> {
        val items = listOf("AX", "AX", "47", "74", "STAR", "STAR", "BLUE", "BOLT")
        return List(count) {
            val previous = items[random.nextInt(items.size)]
            val alternatives = items.filterNot { it == previous }
            val current = if (random.nextBoolean()) previous else alternatives[random.nextInt(alternatives.size)]
            val answer = if (previous == current) "Match" else "No match"
            Question("Previous: $previous\nCurrent: $current", listOf("Match", "No match"), answer, "Compare current with previous.")
        }
    }

    private fun genericQuestions(game: GameDefinition, random: Random, count: Int): List<Question> =
        when (game.category) {
            Category.Math -> genericMath(game, random, count)
            Category.Memory -> genericMemory(game, random, count)
            Category.Logic -> genericLogic(game, random, count)
            Category.Reading -> genericReading(game, random, count)
            Category.Focus -> genericFocus(game, random, count)
            Category.Spatial -> genericSpatial(game, random, count)
            Category.Speed -> genericSpeed(game, random, count)
        }

    private fun genericMath(game: GameDefinition, random: Random, count: Int) = List(count) {
        val a = random.nextInt(1, 8 + game.difficulty * 4)
        val b = random.nextInt(1, 7 + game.difficulty * 3)
        val useMinus = game.name.contains("Away", true) || random.nextInt(4) == 0
        val answer = if (useMinus) max(a, b) - minOf(a, b) else a + b
        val prompt = if (useMinus) "${max(a, b)} - ${minOf(a, b)} = ?" else "$a + $b = ?"
        choiceQuestion(prompt, answer, random, "Careful number tracking gives $answer.")
    }

    private fun genericMemory(game: GameDefinition, random: Random, count: Int) = List(count) {
        val symbols = listOf("Blue", "Star", "Circle", "Lime", "Wave", "Box", "Sun", "Key")
        val length = 2 + game.difficulty
        val sequence = List(length) { symbols[random.nextInt(symbols.size)] }
        val answer = sequence.joinToString(" ")
        val choices = buildChoices(answer, random) { sequence.shuffled(random).joinToString(" ") }
        Question("Remember the order: $answer", choices, answer, "The original order was $answer.")
    }

    private fun genericLogic(game: GameDefinition, random: Random, count: Int) = List(count) {
        val step = 1 + random.nextInt(2 + game.difficulty)
        val start = random.nextInt(1, 8)
        val seq = List(4) { start + it * step }
        val answer = start + 4 * step
        choiceQuestion("Continue the rule: ${seq.joinToString(", ")}, ?", answer, random, "The pattern adds $step.")
    }

    private fun genericReading(game: GameDefinition, random: Random, count: Int): List<Question> {
        val content = listOf(
            Triple("Nia packed a blue pen before the quiz.", "What color was the pen?", "blue"),
            Triple("The small robot carried three cubes to the lab.", "How many cubes did it carry?", "three"),
            Triple("A calm bell rang after the puzzle was solved.", "What rang?", "bell"),
            Triple("The bright card belonged in the word group.", "Where did the card belong?", "word group"),
            Triple("Milo found the letter B beside the green tile.", "Which letter did Milo find?", "B"),
        )
        return List(count) {
            val item = content[random.nextInt(content.size)]
            val wrong = listOf("red", "two", "drum", "number group", "D", "yellow", "four").filterNot { it == item.third }.shuffled(random).take(3)
            Question("${item.first}\n${item.second}", (wrong + item.third).shuffled(random), item.third, "The sentence gives the answer: ${item.third}.")
        }
    }

    private fun genericFocus(game: GameDefinition, random: Random, count: Int): List<Question> {
        val colors = listOf("Cyan", "Amber", "Lime", "Violet")
        return List(count) {
            val target = colors[random.nextInt(colors.size)]
            val distractors = colors.filterNot { it == target }.shuffled(random).take(3)
            Question("Tap the requested target: $target", (distractors + target).shuffled(random), target, "The rule asked for $target.")
        }
    }

    private fun genericSpatial(game: GameDefinition, random: Random, count: Int): List<Question> {
        val shapes = listOf("Triangle", "Square", "Hexagon", "Diamond", "Circle")
        return List(count) {
            val answer = shapes[random.nextInt(shapes.size)]
            val prompt = if (game.name.contains("Mirror", true)) "Choose the mirrored partner for $answer" else "Choose the matching shape: $answer"
            val choices = (shapes.filterNot { it == answer }.shuffled(random).take(3) + answer).shuffled(random)
            Question(prompt, choices, answer, "The matching spatial target is $answer.")
        }
    }

    private fun genericSpeed(game: GameDefinition, random: Random, count: Int): List<Question> {
        val symbols = listOf("●", "■", "▲", "◆")
        return List(count) {
            val target = symbols[random.nextInt(symbols.size)]
            val answer = if (random.nextBoolean()) "Tap" else "Wait"
            Question("Rule: ${if (answer == "Tap") "target appears" else "distractor appears"}\nSymbol: $target", listOf("Tap", "Wait"), answer, "Speed still rewards correct control.")
        }
    }

    private fun choiceQuestion(prompt: String, answer: Int, random: Random, detail: String): Question {
        val choices = buildChoices(answer.toString(), random) { (answer + random.nextInt(-9, 10)).toString() }
        return Question(prompt, choices, answer.toString(), detail)
    }

    private fun buildChoices(answer: String, random: Random, makeWrong: () -> String): List<String> {
        val choices = mutableSetOf(answer)
        var guard = 0
        while (choices.size < 4 && guard < 40) {
            val wrong = makeWrong()
            if (wrong != answer) choices.add(wrong)
            guard++
        }
        while (choices.size < 4) choices.add("${answer}.${choices.size}")
        return choices.shuffled(random)
    }
}
