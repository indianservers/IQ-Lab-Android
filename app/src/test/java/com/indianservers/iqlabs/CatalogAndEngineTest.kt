package com.indianservers.iqlabs

import com.indianservers.iqlabs.game.LevelBlueprint
import com.indianservers.iqlabs.game.PlayStyle
import com.indianservers.iqlabs.game.QuestionFactory
import com.indianservers.iqlabs.game.WebNativePack
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogAndEngineTest {
    @Test
    fun catalogContainsOnlyTheUniqueWebGames() {
        assertEquals(152, IqCatalog.games.size)
        assertEquals(152, IqCatalog.games.map { it.id }.toSet().size)
        assertEquals(setOf(36, 44, 34, 19), LevelId.entries.map { IqCatalog.levelGames(it).size }.toSet())
    }

    @Test
    fun websiteLevelCountsArePreservedAfterDuplicateIdsAreCollapsed() {
        assertEquals(listOf(36, 44, 34, 19, 19), LevelId.entries.map { IqCatalog.levelGames(it).size })
    }

    @Test
    fun everyCatalogGameHasANativeRoundGenerator() {
        IqCatalog.games.forEach { game ->
            val rounds = QuestionFactory.questionsFor(game, seed = game.id.hashCode(), count = 2, tier = game.difficulty)
            assertEquals("${game.id} round count", 2, rounds.size)
            rounds.forEach { round ->
                assertTrue("${game.id} prompt", round.prompt.startsWith(game.name))
                assertTrue("${game.id} answer", round.answer in round.choices)
            }
        }
    }

    @Test
    fun progressionContainsNoLegacyGameIds() {
        assertEquals(IqCatalog.playableIds, LevelBlueprint.games.mapTo(linkedSetOf()) { it.catalogId })
        assertTrue(LevelBlueprint.mappedCatalogGamesArePlayable())
    }

    @Test
    fun everyGameExposesThreeNativeUxEnhancements() {
        IqCatalog.games.forEach { game ->
            val enhancements = WebNativePack.enhancementsFor(game)
            assertEquals("${game.id} enhancement count", 3, enhancements.size)
            assertEquals("${game.id} unique enhancements", 3, enhancements.distinct().size)
            assertTrue("${game.id} native play style", QuestionFactory.questionsFor(game, 17, 1).single().play != null)
        }
    }

    @Test
    fun generatedRoundsHaveUniqueStateKeysEvenWhenPromptsRepeat() {
        IqCatalog.games.forEach { game ->
            val rounds = QuestionFactory.questionsFor(game, seed = 73, count = 6)
            assertEquals("${game.id} unique round state", rounds.size, rounds.map { it.roundKey }.toSet().size)
            assertTrue("${game.id} non-empty round state", rounds.all { it.roundKey.isNotBlank() })
        }
    }

    @Test
    fun numberMemoryHidesItsAnswerUntilRecall() {
        val game = IqCatalog.game("number-memory")
        val rounds = QuestionFactory.questionsFor(game, seed = 91, count = 6)

        rounds.forEach { round ->
            assertEquals(PlayStyle.Reveal, round.play)
            assertFalse("answer leaked in Number Memory prompt", round.prompt.contains(round.answer))
            assertEquals(round.answer.length, round.field.size)
        }
    }

    @Test
    fun everyNativeMechanicProducesSolvableWellFormedRounds() {
        IqCatalog.games.forEach { game ->
            val rounds = listOf(1, 5, 10, 15, 20).flatMap { tier ->
                QuestionFactory.questionsFor(game, seed = game.id.hashCode() xor 404 xor tier, count = 4, tier = tier)
            }
            rounds.forEach { round ->
                assertTrue("${game.id} answer is selectable", round.answer in round.choices)
                assertEquals("${game.id} choices are unique", round.choices.size, round.choices.distinct().size)
                assertTrue("${game.id} has at least two outcomes", round.choices.size >= 2)
                when (round.play) {
                    PlayStyle.Grid -> {
                        val cells = round.answer.split("-").map { it.toInt() }
                        val boardSize = if (game.id.startsWith("kids-")) 9 else 16
                        assertEquals("${game.id} grid cells unique", cells.size, cells.distinct().size)
                        assertTrue("${game.id} grid cells in bounds", cells.all { it in 1..boardSize })
                    }
                    PlayStyle.Reveal -> {
                        assertTrue("${game.id} reveal data", round.field.isNotEmpty())
                        assertTrue("${game.id} keypad capacity", round.answer.length <= 12)
                    }
                    PlayStyle.Match -> {
                        assertTrue("${game.id} pair board", round.field.isNotEmpty() && round.field.size % 2 == 0)
                        assertTrue("${game.id} exactly two of each card", round.field.groupingBy { it }.eachCount().values.all { it == 2 })
                    }
                    PlayStyle.Wait -> assertTrue("${game.id} positive reaction delay", round.field.single().toLong() > 0)
                    PlayStyle.Search -> assertTrue("${game.id} search target present", round.answer in round.field)
                    PlayStyle.Peripheral -> {
                        val rendered = round.field.drop(1).take(4)
                        assertEquals("${game.id} four edge targets", 4, rendered.size)
                        assertTrue("${game.id} peripheral target rendered", round.answer in rendered)
                    }
                    PlayStyle.Sudoku -> {
                        assertEquals("${game.id} sudoku size", 16, round.field.size)
                        assertTrue("${game.id} sudoku values", round.field.all { it.toInt() in 0..4 })
                    }
                    PlayStyle.Tower -> assertTrue("${game.id} tower size", round.field.single().toInt() in 3..4)
                    PlayStyle.Trail -> assertEquals("${game.id} unique trail tokens", round.field.size, round.field.distinct().size)
                    PlayStyle.Shade -> assertEquals("${game.id} one odd shade", 1, round.field.count { it == "odd" })
                    PlayStyle.CodeBreaker -> assertTrue("${game.id} code format", round.answer.matches(Regex("[1-6]{3,4}")))
                    PlayStyle.MultiSelect -> {
                        val selected = round.answer.split("-").filter { it.isNotBlank() }
                        assertTrue("${game.id} selected options exist", selected.isNotEmpty() && selected.all { it in round.choices })
                    }
                    PlayStyle.FocusTrack -> assertTrue("${game.id} moving focus path", round.field.isNotEmpty() && round.answer == "tracked")
                    PlayStyle.StudyChoice -> {
                        val divider = round.field.indexOf("|")
                        assertTrue("${game.id} study/probe divider", divider > 0)
                        assertTrue("${game.id} answer studied", round.answer in round.field.take(divider))
                        assertTrue("${game.id} answer probed", round.answer in round.field.drop(divider + 1))
                    }
                    PlayStyle.Matrix -> {
                        assertEquals("${game.id} matrix size", 9, round.field.size)
                        assertEquals("${game.id} one missing matrix cell", 1, round.field.count { it == "?" })
                        assertFalse("${game.id} placeholder leak", "pick" in round.field)
                    }
                    PlayStyle.Deduction -> assertTrue("${game.id} answer must be deduced", round.field.none { round.answer in it })
                    PlayStyle.GridPlacement -> {
                        val valid = round.field.first { it.startsWith("valid:") }.removePrefix("valid:").split("|")
                        assertTrue("${game.id} legal placement", round.answer in valid)
                    }
                    PlayStyle.NBack -> {
                        val n = Regex("match (\\d+) steps").find(round.prompt)?.groupValues?.get(1)?.toInt()
                            ?: error("Missing N value: ${round.prompt}")
                        assertTrue("${game.id} n-back stream", round.field.size > n)
                        val actual = round.field.last() == round.field[round.field.lastIndex - n]
                        assertEquals(if (actual) "Yes" else "No", round.answer)
                    }
                    else -> Unit
                }
            }
        }
    }

    @Test
    fun hiddenStudyGamesDoNotPrintTheirAnswersInThePrompt() {
        val hiddenStudyIds = listOf(
            "number-memory", "flash-calculation", "story-recall", "multi-task",
            "simon-game", "direction-memory", "face-memory", "expert-memory-weave",
            "fast-reading", "constraint-maze", "stroop-test", "focus-timer",
        )
        hiddenStudyIds.forEach { id ->
            val game = IqCatalog.game(id)
            QuestionFactory.questionsFor(game, seed = 818, count = 12, tier = 20).forEach { round ->
                assertFalse("$id leaked ${round.answer} in its prompt", round.prompt.contains(round.answer))
            }
        }
    }

    @Test
    fun answerChoicesAreNotPositionBiased() {
        val positions = IqCatalog.games.flatMap { game ->
            QuestionFactory.questionsFor(game, seed = game.id.hashCode() xor 919, count = 10)
                .map { it.choices.indexOf(it.answer) }
        }
        val firstRatio = positions.count { it == 0 }.toDouble() / positions.size
        assertTrue("answers should occupy multiple positions", positions.distinct().size >= 4)
        assertTrue("first-choice ratio was $firstRatio", firstRatio < 0.55)
    }

    @Test
    fun targetForgeHasExactlyOneExpressionThatHitsTheTarget() {
        listOf("target-number-forge", "expert-target-forge", "master-target-forge").forEach { id ->
            val game = IqCatalog.game(id)
            QuestionFactory.questionsFor(game, seed = 1221, count = 20, tier = 20).forEach { round ->
                val target = Regex("hits (\\d+)").find(round.prompt)?.groupValues?.get(1)?.toInt()
                    ?: error("Missing target in ${round.prompt}")
                val hits = round.choices.count { expressionValue(it) == target }
                assertEquals("$id must have one mathematically correct expression", 1, hits)
                assertEquals(target, expressionValue(round.answer))
            }
        }
    }

    @Test
    fun colorWordConflictUsesTheSourcePaletteAndInkAnswer() {
        val expected = setOf("Red", "Blue", "Green", "Yellow", "Purple")
        listOf("stroop-test", "kids-color-focus", "seniors-inhibition-control").forEach { id ->
            QuestionFactory.questionsFor(IqCatalog.game(id), seed = 551, count = 20).forEach { round ->
                assertEquals(expected, round.choices.toSet())
                assertEquals("stroop", round.field[0])
                assertTrue(round.field[1] in expected)
                assertTrue(round.field[2] in expected)
                assertEquals(round.field[2], round.answer)
                assertFalse(round.prompt.contains(round.answer))
            }
        }
    }

    @Test
    fun everyCodeBreakerHasOneCodeConsistentWithAllClues() {
        listOf("code-breaker", "expert-code-breaker", "master-code-breaker").forEach { id ->
            QuestionFactory.questionsFor(IqCatalog.game(id), seed = 707, count = 20).forEach { round ->
                assertEquals(4, round.field.size)
                assertEquals(3, round.answer.toSet().size)
                val allCodes = ('1'..'6').flatMap { a ->
                    ('1'..'6').filter { it != a }.flatMap { b ->
                        ('1'..'6').filter { it != a && it != b }.map { c -> "$a$b$c" }
                    }
                }
                val survivors = allCodes.filter { code -> round.field.all { encoded ->
                    val parts = encoded.split("|")
                    codeClue(code, parts[0]) == (parts[1].toInt() to parts[2].toInt())
                } }
                assertEquals("$id unique clue solution", listOf(round.answer), survivors)
                assertTrue("$id does not reveal secret as a clue", round.field.none { it.startsWith("${round.answer}|") })
            }
        }
    }

    @Test
    fun operationSwitchAnswersMatchTheirDisplayedRule() {
        listOf("operation-switch", "expert-operation-switch").forEach { id ->
            QuestionFactory.questionsFor(IqCatalog.game(id), seed = 881, count = 30, tier = 20).forEach { round ->
                val match = Regex("Rule: (SUM|DIFFERENCE|PRODUCT|LARGER|SMALLER)\\. Apply it to (\\d+) and (\\d+)").find(round.prompt)
                    ?: error("Malformed operation round: ${round.prompt}")
                val rule = match.groupValues[1]
                val a = match.groupValues[2].toInt()
                val b = match.groupValues[3].toInt()
                val expected = when (rule) {
                    "SUM" -> a + b
                    "DIFFERENCE" -> kotlin.math.abs(a - b)
                    "PRODUCT" -> a * b
                    "LARGER" -> maxOf(a, b)
                    else -> minOf(a, b)
                }
                assertEquals(expected.toString(), round.answer)
            }
        }
    }

    @Test
    fun mentalMathAndSpeedComparisonAnswersMatchVisibleStimuli() {
        listOf("mental-math", "kids-counting-sprint", "seniors-math-fluency", "expert-math-sprint").forEach { id ->
            QuestionFactory.questionsFor(IqCatalog.game(id), seed = 993, count = 30, tier = 20).forEach { round ->
                val expression = round.field.single()
                val parts = expression.split(" ")
                val expected = when (parts[1]) {
                    "+" -> parts[0].toInt() + parts[2].toInt()
                    "−" -> parts[0].toInt() - parts[2].toInt() + 3
                    else -> parts[0].toInt() * parts[2].toInt()
                }
                assertEquals("$id $expression", expected.toString(), round.answer)
            }
        }
        listOf("speed-comparison", "seniors-number-comparison").forEach { id ->
            QuestionFactory.questionsFor(IqCatalog.game(id), seed = 994, count = 30, tier = 20).forEach { round ->
                assertEquals(round.field.maxOf { it.toInt() }.toString(), round.answer)
                assertTrue("$id should scale beyond two digits", round.field.all { it.length >= 5 })
            }
        }
    }

    private fun expressionValue(expression: String): Int {
        val parts = expression.split(" ")
        val product = parts[0].toInt() * parts[2].toInt()
        return if (parts[3] == "+") product + parts[4].toInt() else product - parts[4].toInt()
    }

    private fun codeClue(code: String, guess: String): Pair<Int, Int> {
        val exact = guess.zip(code).count { it.first == it.second }
        val common = guess.count { it in code }
        return exact to (common - exact)
    }
}
