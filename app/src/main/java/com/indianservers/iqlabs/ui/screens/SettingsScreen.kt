package com.indianservers.iqlabs.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.R
import com.indianservers.iqlabs.game.BackupSystem
import com.indianservers.iqlabs.game.DifficultyMode
import com.indianservers.iqlabs.game.ProgressBackup
import com.indianservers.iqlabs.ui.components.IqPage
import com.indianservers.iqlabs.ui.components.SoftCard
import com.indianservers.iqlabs.ui.rankForXp
import com.indianservers.iqlabs.ui.theme.AppColorTheme
import com.indianservers.iqlabs.ui.theme.IqPeach
import com.indianservers.iqlabs.ui.theme.LocalIqExtras
import java.time.Instant

@Composable
fun SettingsScreen(
    xp: Int = 480,
    sound: Boolean,
    haptics: Boolean,
    reducedMotion: Boolean,
    music: Boolean = true,
    difficultyMode: DifficultyMode,
    appColorTheme: AppColorTheme,
    onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
    onMusic: (Boolean) -> Unit = {},
    onDifficultyMode: (DifficultyMode) -> Unit,
    onAppColorTheme: (AppColorTheme) -> Unit,
    onReplayOnboarding: () -> Unit,
    onReset: () -> Unit,
) {
    val extras = LocalIqExtras.current
    var textScale by rememberSaveable { mutableStateOf(.5f) }
    var colorBlind by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val rank = rankForXp(xp)

    IqPage {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Text("Make it yours", color = extras.muted)
                    }
                    Image(
                        painter = painterResource(R.drawable.settings_mascot),
                        contentDescription = null,
                        modifier = Modifier.size(88.dp).clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.avatar_boy),
                            contentDescription = "Profile",
                            modifier = Modifier.size(54.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(rank.title, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text("Keep exploring", color = extras.muted, fontSize = 13.sp)
                        }
                    }
                }
            }
            item { Text("Appearance", fontWeight = FontWeight.Black, fontSize = 18.sp) }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AppColorTheme.entries.forEach { theme ->
                            ThemePreview(
                                theme = theme,
                                selected = appColorTheme == theme,
                                modifier = Modifier.weight(1f),
                                onClick = { onAppColorTheme(theme) },
                            )
                        }
                    }
                }
            }
            item { Text("Audio & Feedback", fontWeight = FontWeight.Black, fontSize = 18.sp) }
            item { ToggleRow("Sound effects", sound, onSound) }
            item { ToggleRow("Music", music, onMusic) }
            item { ToggleRow("Haptics", haptics, onHaptics) }
            item { ToggleRow("Reduced motion", reducedMotion, onReducedMotion) }
            item { Text("Accessibility", fontWeight = FontWeight.Black, fontSize = 18.sp) }
            item { ToggleRow("Colour-blind-friendly palette", colorBlind) { colorBlind = it } }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Text size", fontWeight = FontWeight.Bold)
                    Slider(value = textScale, onValueChange = { textScale = it })
                    Row(Modifier.fillMaxWidth()) {
                        Text("A", fontSize = 12.sp)
                        Spacer(Modifier.weight(1f))
                        Text("A", fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            item { Text("Difficulty mode", fontWeight = FontWeight.Black, fontSize = 18.sp) }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        DifficultyMode.entries.forEach { mode ->
                            val selected = difficultyMode == mode
                            Text(
                                mode.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary else extras.chipIdle)
                                    .clickable { onDifficultyMode(mode) }
                                    .padding(vertical = 10.dp),
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else extras.muted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Adaptive uses recent accuracy, speed and consistency. Fixed keeps your chosen tier. Relaxed reduces time pressure.",
                        color = extras.muted,
                        fontSize = 12.sp,
                    )
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onReplayOnboarding)) {
                    Text("Replay onboarding", fontWeight = FontWeight.Bold)
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth().clickable { confirmReset = true }) {
                    Text("Reset progress", fontWeight = FontWeight.Bold, color = IqPeach)
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE8F8F0)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF1C7C74))
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text("Privacy & Offline Use", fontWeight = FontWeight.Black, color = Color(0xFF1C7C74))
                            Text(
                                "Works offline. No account, ads, contacts, camera, microphone or location permission.",
                                color = Color(0xFF1C7C74),
                                fontSize = 13.sp,
                            )
                            val preview = remember(difficultyMode) {
                                BackupSystem.export(
                                    ProgressBackup(
                                        appVersion = "1.0",
                                        exportedAt = Instant.parse("2026-08-19T00:00:00Z"),
                                        xp = 0,
                                        streak = 0,
                                        sessions = 0,
                                        favorites = emptySet(),
                                        bestScores = emptyMap(),
                                        difficultyMode = difficultyMode,
                                    )
                                ).take(42)
                            }
                            Text("Backup format preview: $preview...", fontSize = 11.sp, color = Color(0xFF1C7C74).copy(alpha = .7f), modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }
            }
            item {
                Text(
                    "IQ Lab v1.0 · Cognitive entertainment and practice. It is not a medical, psychological or diagnostic product.",
                    color = extras.muted,
                    fontSize = 12.sp,
                )
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progress?") },
            text = { Text("This clears local XP, favorites, best scores and settings.") },
            confirmButton = { TextButton(onClick = { confirmReset = false; onReset() }) { Text("Reset") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Switch(checked, onChange)
        }
    }
}

@Composable
private fun ThemePreview(theme: AppColorTheme, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val brush = when (theme) {
        AppColorTheme.SimpleWhite -> Brush.verticalGradient(listOf(Color(0xFFFFF6D8), Color(0xFFFFE08A)))
        AppColorTheme.LabDark -> Brush.verticalGradient(listOf(Color(0xFF1B2740), Color(0xFF0B1220)))
        AppColorTheme.Ocean -> Brush.verticalGradient(listOf(Color(0xFF7AD7F0), Color(0xFF1C7C74)))
        AppColorTheme.Forest -> Brush.verticalGradient(listOf(Color(0xFF9BE08A), Color(0xFF2F6F3E)))
        AppColorTheme.Sunset -> Brush.verticalGradient(listOf(Color(0xFFFFB088), Color(0xFFC24B2C)))
    }
    val icon: ImageVector = when (theme) {
        AppColorTheme.SimpleWhite -> Icons.Rounded.LightMode
        AppColorTheme.LabDark -> Icons.Rounded.DarkMode
        AppColorTheme.Ocean -> Icons.Rounded.WaterDrop
        AppColorTheme.Forest -> Icons.Rounded.Park
        AppColorTheme.Sunset -> Icons.Rounded.WbTwilight
    }
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .height(64.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(brush)
                .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = theme.label, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(theme.label, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Black else FontWeight.Medium, maxLines = 1)
    }
}
