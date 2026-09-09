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
        if ("analysis-pack" in game.tags) return analyticalQuestions(game, random, count, effectiveTier)
        return when (game.id) {
            "flash-sum" -> flashSum(random, count, effectiveTier)
            "memory-grid" -> memoryGrid(random, count, effectiveTier)
            "word-sprint" -> wordSprint(random, count)
            "sequence-detective" -> sequenceDetective(random, count, effectiveTier)
            "pattern-pulse" -> patternPulse(random, count, effectiveTier)
            "quick-match" -> quickMatch(random, count)
            "maze-scout" -> mazeScout(random, count, effectiveTier)
            else -> genericQuestions(game.copy(difficulty = effectiveTier), random, count)
        }
    }

    private fun analyticalQuestions(game: GameDefinition, random: Random, count: Int, tier: Int): List<Question> =
        List(count) { round ->
            when {
                "causality" in game.tags || "systems" in game.tags -> causalQuestion(game, random)
                "bias" in game.tags -> biasQuestion(random)
                "probability" in game.tags -> probabilityQuestion(game, random, tier)
                "decision" in game.tags || "optimization" in game.tags || "planning" in game.tags -> decisionQuestion(game, random, tier)
                "data" in game.tags || "forecasting" in game.tags || "comparison" in game.tags -> dataQuestion(game, random, tier)
                "assumptions" in game.tags || "models" in game.tags -> assumptionQuestion(game, random)
                "argument" in game.tags || "counterexample" in game.tags || "evidence" in game.tags && game.category == Category.Reading -> evidenceQuestion(game, random)
                "classification" in game.tags -> classificationQuestion(game, random)
                "constraints" in game.tags || "deduction" in game.tags || "rules" in game.tags -> deductionQuestion(game, random)
                else -> patternAnalysisQuestion(game, random, tier, round)
            }
        }

    private fun classificationQuestion(game: GameDefinition, random: Random): Question {
        val banks = listOf(
            Triple("Rule: multiples of 3. Which item belongs?", listOf("14", "18", "22", "25"), "18"),
            Triple("Rule: shapes with four equal sides. Which item belongs?", listOf("Rectangle", "Square", "Triangle", "Circle"), "Square"),
            Triple("Rule: living things. Which item belongs?", listOf("Robot", "Tree", "Chair", "Stone"), "Tree"),
            Triple("Rule: words that name a colour. Which item belongs?", listOf("Amber", "River", "Music", "Window"), "Amber"),
        )
        val (prompt, choices, answer) = banks[random.nextInt(banks.size)]
        return Question("${game.name}: $prompt", choices.shuffled(random), answer, "$answer is the only choice that satisfies the stated rule.")
    }

    private fun patternAnalysisQuestion(game: GameDefinition, random: Random, tier: Int, round: Int): Question {
        val start = random.nextInt(1, 8 + tier)
        val step = random.nextInt(2, 4 + tier / 2)
        val sequence = List(4) { start + it * step }
        val answer = sequence.last() + step
        return choiceQuestion(
            "${game.name}: ${sequence.joinToString(", ")}, ?\nWhich value confirms the rule?",
            answer,
            random,
            "Every step adds $step, so round ${round + 1} continues with $answer.",
        )
    }

    private fun dataQuestion(game: GameDefinition, random: Random, tier: Int): Question {
        val start = random.nextInt(8, 18 + tier)
        val change = random.nextInt(2, 5 + tier / 3)
        val values = List(4) { start + it * change }
        return when {
            "forecasting" in game.tags -> choiceQuestion(
                "${game.name}: Weekly values are ${values.joinToString(", ")}. What is the most defensible next forecast?",
                values.last() + change,
                random,
                "The stable trend increases by $change each week.",
            )
            "trends" in game.tags -> Question(
                "${game.name}: Values are ${values.joinToString(" → ")}. Which statement is supported?",
                listOf("Rises steadily", "Falls steadily", "Stays constant", "Changes randomly").shuffled(random),
                "Rises steadily",
                "Each value is $change higher than the previous value.",
            )
            else -> {
                val first = values.first()
                val last = values.last()
                choiceQuestion(
                    "${game.name}: A = $first, B = ${values[1]}, C = ${values[2]}, D = $last. How much greater is D than A?",
                    last - first,
                    random,
                    "Subtract A from D: $last - $first = ${last - first}.",
                )
            }
        }
    }

    private fun probabilityQuestion(game: GameDefinition, random: Random, tier: Int): Question {
        if (game.id == "bayesian-update") {
            val affected = 10 + random.nextInt(6) * 2
            val trueAlerts = affected * 4 / 5
            val falseAlerts = 4 + random.nextInt(4) * 2
            val answer = trueAlerts * 100 / (trueAlerts + falseAlerts)
            return choiceQuestion(
                "${game.name}: In 100 cases, $affected have the issue. The test alerts on $trueAlerts of them and on $falseAlerts healthy cases. Among all alerts, about what percent truly have the issue?",
                answer,
                random,
                "There are ${trueAlerts + falseAlerts} alerts; $trueAlerts are true, so the updated probability is about $answer%.",
            )
        }
        if (game.id == "decision-under-risk") {
            val safe = 4 + tier
            val win = safe * 3
            val chance = 40
            val riskyExpected = win * chance / 100
            val answer = if (safe >= riskyExpected) "Safe option" else "Risky option"
            return Question(
                "${game.name}: Safe option earns $safe points. Risky option has a $chance% chance to earn $win and otherwise earns 0. Which has the higher expected value?",
                listOf("Safe option", "Risky option", "They are equal").shuffled(random),
                answer,
                "The risky expected value is $riskyExpected; compare it with the safe value of $safe.",
            )
        }
        val blue = random.nextInt(2, 8 + tier)
        val gold = random.nextInt(2, 8 + tier)
        val answer = when {
            blue > gold -> "Blue"
            gold > blue -> "Gold"
            else -> "Equally likely"
        }
        return Question(
            "${game.name}: A bag contains $blue blue and $gold gold tokens. Which result is more likely on one draw?",
            listOf("Blue", "Gold", "Equally likely").shuffled(random),
            answer,
            "The outcome with more tokens has greater probability.",
        )
    }

    private fun evidenceQuestion(game: GameDefinition, random: Random): Question {
        if (game.id == "counterexample-forge") {
            val banks = listOf(
                Triple("Claim: Every even number is divisible by 4. Which example disproves it?", listOf("8", "10", "12", "16"), "10"),
                Triple("Claim: All birds can fly. Which example disproves it?", listOf("Sparrow", "Eagle", "Penguin", "Robin"), "Penguin"),
                Triple("Claim: Adding two odd numbers gives an odd number. Which example disproves it?", listOf("3 + 5 = 8", "2 + 4 = 6", "1 + 2 = 3", "4 + 5 = 9"), "3 + 5 = 8"),
            )
            val (prompt, choices, answer) = banks[random.nextInt(banks.size)]
            return Question("${game.name}: $prompt", choices.shuffled(random), answer, "$answer is a valid case that makes the universal claim false.")
        }
        val banks = listOf(
            Triple("Claim: The new route is faster. Which fact supports it most directly?", listOf("Average trip time fell from 30 to 22 minutes", "The buses are blue", "More posters were printed", "The route has a new name"), "Average trip time fell from 30 to 22 minutes"),
            Triple("Claim: Regular review improved recall. Which fact is strongest?", listOf("The review group remembered 18% more after one week", "The room was quiet", "The notes used green ink", "Everyone liked the teacher"), "The review group remembered 18% more after one week"),
            Triple("Claim: The conclusion needs stronger support. Which addition helps most?", listOf("A controlled comparison with measured results", "A louder opinion", "A colourful title", "One unrelated example"), "A controlled comparison with measured results"),
        )
        val (prompt, choices, answer) = banks[random.nextInt(banks.size)]
        return Question("${game.name}: $prompt", choices.shuffled(random), answer, "The best evidence measures the claim directly and rules out weaker explanations.")
    }

    private fun assumptionQuestion(game: GameDefinition, random: Random): Question {
        val banks = if (game.id == "model-critic") {
            listOf(
                Triple("A sales model uses advertising spend alone. What missing variable most threatens it?", listOf("Product price", "Logo colour", "Report font", "File name"), "Product price"),
                Triple("A travel-time model ignores weather. Which assumption is weakest?", listOf("Weather never affects traffic", "Minutes measure time", "Roads connect places", "Trips have destinations"), "Weather never affects traffic"),
                Triple("A crop model uses rainfall but ignores soil. What should be added?", listOf("Soil quality", "Chart border", "Farm name length", "Page number"), "Soil quality"),
            )
        } else {
            listOf(
                Triple("Argument: The library should stay open later because evening visits increased. What must be assumed?", listOf("Later hours would serve those evening visitors", "Every visitor reads fiction", "The building is new", "Books are arranged by colour"), "Later hours would serve those evening visitors"),
                Triple("Argument: This route will save time because it is shorter. What must be assumed?", listOf("Traffic and speed are reasonably similar", "The car is red", "The map is printed", "The driver likes shortcuts"), "Traffic and speed are reasonably similar"),
                Triple("Argument: Practice quizzes raised scores, so we should use them again. What must be assumed?", listOf("The quizzes contributed to the improvement", "All questions were easy", "Scores never vary", "Students used pencils"), "The quizzes contributed to the improvement"),
            )
        }
        val (prompt, choices, answer) = banks[random.nextInt(banks.size)]
        return Question("${game.name}: $prompt", choices.shuffled(random), answer, "The argument depends on: $answer.")
    }

    private fun biasQuestion(random: Random): Question {
        val banks = listOf(
            Triple("A team reads only reviews that agree with its plan. Which bias is this?", listOf("Confirmation bias", "Random sampling", "Base-rate use", "Controlled testing"), "Confirmation bias"),
            Triple("After one dramatic failure, a manager assumes failure is common. Which bias is strongest?", listOf("Availability bias", "Blind testing", "Regression analysis", "Deduction"), "Availability bias"),
            Triple("A buyer keeps funding a failing project because much was already spent. Which bias is this?", listOf("Sunk-cost bias", "Survivorship bias", "Recency weighting", "Peer review"), "Sunk-cost bias"),
        )
        val (prompt, choices, answer) = banks[random.nextInt(banks.size)]
        return Question("Bias Detector: $prompt", choices.shuffled(random), answer, "$answer best explains the reasoning error.")
    }

    private fun causalQuestion(game: GameDefinition, random: Random): Question {
        if (game.id == "systems-thinker") {
            val banks = listOf(
                Triple("A city adds buses, so fewer people drive. What likely second-order effect follows?", listOf("Less road congestion", "Longer book titles", "More rainfall", "Shorter buildings"), "Less road congestion"),
                Triple("A lake loses predators, so small fish increase. What may happen next?", listOf("Their food supply declines", "The lake becomes square", "Days get longer", "Rocks disappear"), "Their food supply declines"),
                Triple("A store cuts checkout time, attracting more shoppers. What feedback effect may follow?", listOf("Queues grow again", "Prices become colours", "Shelves get shorter", "Clocks stop"), "Queues grow again"),
            )
            val (prompt, choices, answer) = banks[random.nextInt(banks.size)]
            return Question("${game.name}: $prompt", choices.shuffled(random), answer, "$answer follows through the next link in the system.")
        }
        val banks = listOf(
            Triple("Ice-cream sales and sunburn both rise in summer. What is the best conclusion?", listOf("Warm weather may influence both", "Ice cream causes sunburn", "Sunburn causes ice cream sales", "The data prove no relationship"), "Warm weather may influence both"),
            Triple("Plants given fertilizer grew more, but they also received more sunlight. What can we conclude?", listOf("The cause is unclear", "Fertilizer definitely caused growth", "Sunlight had no effect", "Growth caused fertilizer"), "The cause is unclear"),
            Triple("A randomized group using a new method improves while the control group does not. What is best supported?", listOf("The method may have caused improvement", "Improvement caused the method", "Only coincidence is possible", "No comparison can be made"), "The method may have caused improvement"),
        )
        val (prompt, choices, answer) = banks[random.nextInt(banks.size)]
        return Question("${game.name}: $prompt", choices.shuffled(random), answer, "This choice distinguishes evidence for causation from simple correlation.")
    }

    private fun deductionQuestion(game: GameDefinition, random: Random): Question {
        val banks = listOf(
            Triple("A is before B. C is after B. Which order must be correct?", listOf("A-B-C", "B-A-C", "C-B-A", "A-C-B"), "A-B-C"),
            Triple("The red box is not first. The blue box is before the green box. Which order is valid?", listOf("Blue-Red-Green", "Red-Blue-Green", "Green-Blue-Red", "Red-Green-Blue"), "Blue-Red-Green"),
            Triple("Mira chose neither square nor blue. Options are red circle, blue circle, red square, blue square. What did she choose?", listOf("Red circle", "Blue circle", "Red square", "Blue square"), "Red circle"),
            Triple("Rule: accept numbers greater than 5 and even. Which input is accepted?", listOf("4", "7", "8", "9"), "8"),
        )
        val (prompt, choices, answer) = banks[random.nextInt(banks.size)]
        return Question("${game.name}: $prompt", choices.shuffled(random), answer, "$answer is the only choice consistent with every constraint.")
    }

    private fun decisionQuestion(game: GameDefinition, random: Random, tier: Int): Question {
        if (game.id == "trade-off-matrix") {
            val a = 6 + random.nextInt(3)
            val b = 5 + random.nextInt(3)
            val c = 4 + random.nextInt(3)
            val scoreA = a * 2 + 5
            val scoreB = b * 2 + 8
            val scoreC = c * 2 + 7
            val answer = listOf("A" to scoreA, "B" to scoreB, "C" to scoreC).maxBy { it.second }.first
            return Question(
                "${game.name}: Reliability counts double; speed counts once. A = $a reliability, 5 speed. B = $b reliability, 8 speed. C = $c reliability, 7 speed. Which scores highest?",
                listOf("A", "B", "C", "All tie").shuffled(random),
                answer,
                "Weighted totals are A=$scoreA, B=$scoreB and C=$scoreC.",
            )
        }
        val budget = 8 + tier
        val options = listOf(
            Triple("Plan A", budget - 1, 12 + tier),
            Triple("Plan B", budget + 3, 18 + tier),
            Triple("Plan C", budget - 3, 9 + tier),
        )
        val feasible = options.filter { it.second <= budget }
        val answer = feasible.maxBy { it.third }.first
        val summary = options.joinToString("; ") { "${it.first}: cost ${it.second}, value ${it.third}" }
        return Question(
            "${game.name}: Budget is $budget. $summary. Which feasible plan gives the greatest value?",
            (options.map { it.first } + "None").shuffled(random),
            answer,
            "$answer stays within budget and has the highest feasible value.",
        )
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

    private fun mazeScout(random: Random, count: Int, difficulty: Int): List<Question> {
        val directions = listOf("Up", "Right", "Down", "Left")
        return List(count) { round ->
            val steps = 2 + (difficulty / 3).coerceIn(0, 3)
            val path = List(steps) { directions[random.nextInt(directions.size)] }
            val answer = path.last()
            Question(
                prompt = "Safe path ${round + 1}: ${path.dropLast(1).joinToString(" → ")} → ?\nSwipe the next safe step.",
                choices = directions,
                answer = answer,
                detail = "The next safe step is $answer.",
            )
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
