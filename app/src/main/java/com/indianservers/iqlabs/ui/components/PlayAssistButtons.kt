package com.indianservers.iqlabs.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Compact visuals with full 48dp touch targets. */
@Composable
fun PlayAssistButtons(lives: Int, onLives: () -> Unit, onHint: () -> Unit,
    hintEnabled: Boolean = true, unlimited: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        TextButton(onClick = onLives, contentPadding = PaddingValues(horizontal = 6.dp),
            modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = if (unlimited) "Unlimited lives" else "$lives lives remaining" }) {
            Icon(Icons.Rounded.Favorite, null, tint = androidx.compose.ui.graphics.Color(0xFFEC526B), modifier = Modifier.size(18.dp))
            Text(if (unlimited) " ∞" else " $lives")
        }
        TextButton(onClick = onHint, enabled = hintEnabled, contentPadding = PaddingValues(horizontal = 6.dp),
            modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Next move hint" }) {
            Icon(Icons.Rounded.Lightbulb, null, modifier = Modifier.size(18.dp))
        }
    }
}
