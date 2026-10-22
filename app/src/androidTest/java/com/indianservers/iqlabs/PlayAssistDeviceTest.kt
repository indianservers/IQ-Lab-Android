package com.indianservers.iqlabs

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.indianservers.iqlabs.game.*
import com.indianservers.iqlabs.model.*
import com.indianservers.iqlabs.ui.theme.IQLabsTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PlayAssistDeviceTest {
    @get:Rule val compose = createComposeRule()
    @Test fun quickPairShowsEncouragementBeforeTheRoundFinishes() {
        val game = IqCatalog.games.first()
        val question = Question("Find the pairs", emptyList(), "done", "Match both pairs.", PlayStyle.Match, listOf("A","A","B","B"), "praise-pairs")
        var completed = false
        compose.setContent {
            IQLabsTheme {
                val praise = com.indianservers.iqlabs.ui.components.rememberQuickPraise()
                androidx.compose.foundation.layout.Column {
                    com.indianservers.iqlabs.ui.components.QuickPraiseBanner(praise)
                    com.indianservers.iqlabs.ui.play.PlayRound(game, question, null, null, { completed = true }, sound = false, onProgress = { praise.success() })
                }
            }
        }
        compose.onAllNodesWithText("?")[0].performClick()
        compose.onAllNodesWithText("?")[0].performClick()
        compose.onNodeWithText(QuickSolvePraise.messages.first()).assertExists()
        assertFalse(completed)
        compose.onAllNodesWithText("?").assertCountEquals(2)
    }
    @Test fun topRightHintLivesExhaustionAndRetry() {
        val mode = DifficultyMode.Fixed
        fun questions(g: GameDefinition): List<Question> {
            val profile = AdaptiveDifficultyEngine.initialProfile(g, mode)
            return QuestionFactory.questionsFor(g, 87, 6, AdaptiveDifficultyEngine.parametersFor(g, profile).tier)
        }
        val game = IqCatalog.games.first { g -> questions(g).all { PlayStyleResolver.forRound(g,it) == PlayStyle.Reading } }
        val rounds = questions(game)
        var completed = false
        compose.setContent { IQLabsTheme { PlayScreen(game,87,mode,null,false,{}, {}, { completed = true }) } }
        compose.onNodeWithContentDescription("5 lives remaining").assertExists()
        compose.onNodeWithContentDescription("Next move hint").performClick()
        compose.onNodeWithText("Next move").assertExists()
        compose.onNodeWithText("Choose ${rounds.first().answer}. ${rounds.first().detail}").assertExists()
        compose.onNodeWithText("Got it").performClick()
        repeat(5) { index ->
            val wrong = rounds[index].choices.first { it != rounds[index].answer }
            compose.waitUntil(15000) { compose.onAllNodesWithText(wrong).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(wrong).performClick()
            compose.onNodeWithContentDescription("${4-index} lives remaining").assertExists()
            compose.waitUntil(15000) {
                if(index==4) compose.onAllNodesWithText("Out of lives").fetchSemanticsNodes().isNotEmpty()
                else compose.onAllNodesWithText("Round ${index+2}/6 · Combo 0").fetchSemanticsNodes().isNotEmpty()
            }
        }
        assertFalse(completed)
        compose.onNodeWithText("Retry session").performClick()
        compose.onNodeWithContentDescription("5 lives remaining").assertExists()
        compose.onNodeWithText("Round 1/6 · Combo 0").assertExists()
    }
}
