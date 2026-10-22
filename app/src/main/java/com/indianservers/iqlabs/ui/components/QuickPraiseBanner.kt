package com.indianservers.iqlabs.ui.components

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.game.QuickSolvePraise
import kotlinx.coroutines.delay

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

@Composable
fun QuickPraiseBanner(state: QuickPraiseState) {
    val message = state.message
    LaunchedEffect(message) { if (message != null) { delay(3200); state.dismiss() } }
    // Reserve a slim line so encouragement never resizes the touch canvas.
    Box(Modifier.fillMaxWidth().height(34.dp), contentAlignment = Alignment.Center) {
        if (message != null) Text(message, color = Color(0xFF655018), fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold, maxLines = 2,
            modifier = Modifier.background(Color(0xFFFFEDB2), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp).semantics { liveRegion = LiveRegionMode.Polite })
    }
}
