package com.indianservers.iqlabs.ui.components

import android.os.SystemClock
import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.indianservers.iqlabs.game.QuickSolvePraise
import java.util.Locale

class QuickPraiseState {
    private val praise = QuickSolvePraise()
    private var started = SystemClock.elapsedRealtime()
    private var assisted = false
    var message by mutableStateOf<String?>(null)
        private set
    fun resetWindow() { started = SystemClock.elapsedRealtime(); assisted = false }
    fun usedHint() { assisted = true }
    fun success(elapsedMs: Long? = null, byHint: Boolean = false) {
        val now = SystemClock.elapsedRealtime()
        praise.success(elapsedMs ?: (now - started), now, assisted || byHint)?.let { message = it }
        started = now; assisted = false
    }
    fun dismiss() { message = null }
}

@Composable
fun rememberQuickPraise() = remember { QuickPraiseState() }

interface PraiseVoice {
    fun speak(text: String)
    fun stop()
    fun close()
}

private class AndroidPraiseVoice(context: Context) : PraiseVoice {
    private var ready = false
    private var closed = false
    private var pending: Pair<String, Long>? = null
    private var engine: TextToSpeech? = null
    init {
        engine = TextToSpeech(context.applicationContext) { status ->
            Handler(Looper.getMainLooper()).post {
                if (!closed && status == TextToSpeech.SUCCESS) {
                    val tts = engine
                    val language = tts?.setLanguage(Locale.ENGLISH)
                    ready = language != null && language >= TextToSpeech.LANG_AVAILABLE
                    tts?.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                    tts?.voices?.firstOrNull { !it.isNetworkConnectionRequired && it.locale.language == "en" }?.let { tts.voice = it }
                    pending?.takeIf { SystemClock.elapsedRealtime() - it.second < 5000 }?.let { if (ready) speak(it.first) }
                    pending = null
                }
            }
        }
    }
    override fun speak(text: String) {
        if (closed) return
        if (!ready) { pending = text to SystemClock.elapsedRealtime(); return }
        engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "quick-solver-praise")
    }
    override fun stop() { pending = null; engine?.stop() }
    override fun close() { closed = true; stop(); engine?.shutdown(); engine = null }
}

/** Audio only: does not render or reserve space on the play surface. */
@Composable
fun SpokenQuickPraise(state: QuickPraiseState, sound: Boolean, active: Boolean = true, voice: PraiseVoice? = null) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val player = remember(context, voice) { voice ?: AndroidPraiseVoice(context) }
    var foreground by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle, player) {
        val observer = LifecycleEventObserver { _, _ ->
            foreground = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (!foreground) { player.stop(); state.dismiss() }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); player.close() }
    }
    val message = state.message
    LaunchedEffect(message, sound, active, foreground, player) {
        if (!sound || !active || !foreground) player.stop()
        else if (message != null) player.speak(message)
        // Consume the event once. Unmuting/resuming must not replay stale praise.
        if (message != null) state.dismiss()
    }
}
