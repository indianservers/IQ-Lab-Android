package com.indianservers.iqlabs

import com.indianservers.iqlabs.game.*
import org.junit.Assert.*
import org.junit.Test

class ReadingGymTest {
    @Test fun allFifteenGamesHaveOneHundredPlayableLevels() {
        assertEquals(15, ReadingGym.games.size)
        ReadingGym.games.forEach { game ->
            assertEquals(100, LevelBlueprint.maxLevelFor(game.id))
            assertEquals((1..100).toList(), LevelBlueprint.stagesForCatalogGame(game.id).map { it.level })
            (1..100).forEach { level ->
                val questions = QuestionFactory.questionsFor(game, level * 73, 10, level)
                assertEquals(10, questions.size)
                questions.forEach {
                    assertEquals(4, it.choices.distinct().size)
                    assertTrue(it.answer in it.choices)
                    assertEquals(level.toString(), it.field[6])
                    assertTrue(it.field[2].isNotBlank())
                }
                assertEquals(questions, QuestionFactory.questionsFor(game, level * 73, 10, level))
            }
        }
    }
    @Test fun freshSeedsChangePracticeAndDifficultyScales() {
        ReadingGym.games.forEach { game ->
            assertNotEquals(QuestionFactory.questionsFor(game, 1, 10, 1).map { it.field }, QuestionFactory.questionsFor(game, 2, 10, 1).map { it.field })
        }
        val game = ReadingGym.games[0]
        assertTrue(QuestionFactory.questionsFor(game, 1, 1, 100).first().field[2].length > QuestionFactory.questionsFor(game, 1, 1, 1).first().field[2].length)
    }
    @Test fun specializedTasksHaveCorrectPresentationAndAnswers() {
        fun round(i: Int) = QuestionFactory.questionsFor(ReadingGym.games[i], 34, 1, 100).single()
        assertTrue(round(0).field[2].contains("eht"))
        assertFalse(round(1).field[2].any { it in "aeiouAEIOU" })
        assertTrue(round(5).field[2].contains('_'))
        assertTrue(round(6).field[3].toInt() > 0)
        assertTrue(round(7).field[3].toInt() > 0)
        assertTrue(round(8).field[2].contains('/'))
        assertTrue(round(10).field[2].contains(round(10).answer))
        assertEquals(round(11).answer, round(11).field[5])
        assertNotEquals(round(11).answer, round(11).field[2])
        assertTrue(round(12).answer.endsWith(round(12).field[4]))
        assertTrue(round(13).answer.contains("open gate"))
    }
}
