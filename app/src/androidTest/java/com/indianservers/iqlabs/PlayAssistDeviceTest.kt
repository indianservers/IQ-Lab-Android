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
    @Test fun installedSpeechEngineSynthesizesAnAppreciation() {
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        val ready = java.util.concurrent.CountDownLatch(1)
        val status = java.util.concurrent.atomic.AtomicInteger(-1)
        val tts = android.speech.tts.TextToSpeech(context) { status.set(it); ready.countDown() }
        val file = java.io.File(context.cacheDir, "appreciation-voice-test.wav")
        try {
            assertTrue("Speech engine did not initialize", ready.await(20, java.util.concurrent.TimeUnit.SECONDS))
            assertEquals(android.speech.tts.TextToSpeech.SUCCESS, status.get())
            assertTrue("English voice unavailable", tts.setLanguage(java.util.Locale.ENGLISH) >= android.speech.tts.TextToSpeech.LANG_AVAILABLE)
            val done = java.util.concurrent.CountDownLatch(1)
            val failed = java.util.concurrent.atomic.AtomicBoolean(false)
            tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) { done.countDown() }
                @Deprecated("Legacy callback") override fun onError(id: String?) { failed.set(true); done.countDown() }
            })
            assertEquals(android.speech.tts.TextToSpeech.SUCCESS, tts.synthesizeToFile(QuickSolvePraise.messages.first(), android.os.Bundle(), file, "praise-test"))
            assertTrue("Speech synthesis timed out", done.await(25, java.util.concurrent.TimeUnit.SECONDS))
            assertFalse(failed.get())
            assertTrue("Speech output was empty", file.length() > 44)
        } finally { tts.shutdown(); file.delete() }
    }
    @Test fun mutedPraiseIsConsumedWithoutSpeakingOrReplayingOnUnmute() {
        val state = com.indianservers.iqlabs.ui.components.QuickPraiseState()
        val sound = androidx.compose.runtime.mutableStateOf(false)
        val spoken = mutableListOf<String>()
        var stops = 0
        val voice = object : com.indianservers.iqlabs.ui.components.PraiseVoice {
            override fun speak(text: String) { spoken += text }
            override fun stop() { stops++ }
            override fun close() {}
        }
        compose.setContent { com.indianservers.iqlabs.ui.components.SpokenQuickPraise(state, sound.value, voice = voice) }
        compose.runOnIdle { state.success(elapsedMs = 1000) }
        compose.waitForIdle()
        assertTrue(spoken.isEmpty())
        assertTrue(stops > 0)
        compose.runOnIdle { sound.value = true }
        compose.waitForIdle()
        assertTrue(spoken.isEmpty())
    }
    @Test fun quickPairSpeaksEncouragementBeforeTheRoundFinishes() {
        val game = IqCatalog.games.first()
        val question = Question("Find the pairs", emptyList(), "done", "Match both pairs.", PlayStyle.Match, listOf("A","A","B","B"), "praise-pairs")
        var completed = false
        val spoken = mutableListOf<String>()
        val voice = object : com.indianservers.iqlabs.ui.components.PraiseVoice {
            override fun speak(text: String) { spoken += text }
            override fun stop() {}
            override fun close() {}
        }
        compose.setContent {
            IQLabsTheme {
                val praise = com.indianservers.iqlabs.ui.components.rememberQuickPraise()
                androidx.compose.foundation.layout.Column {
                    com.indianservers.iqlabs.ui.components.SpokenQuickPraise(praise, true, voice = voice)
                    com.indianservers.iqlabs.ui.play.PlayRound(game, question, null, null, { completed = true }, sound = false, onProgress = { praise.success() })
                }
            }
        }
        compose.onAllNodesWithText("?")[0].performClick()
        compose.onAllNodesWithText("?")[0].performClick()
        compose.waitForIdle()
        assertEquals(listOf(QuickSolvePraise.messages.first()), spoken)
        compose.onNodeWithText(QuickSolvePraise.messages.first()).assertDoesNotExist()
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
