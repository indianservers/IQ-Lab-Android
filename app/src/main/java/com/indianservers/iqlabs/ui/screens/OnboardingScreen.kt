package com.indianservers.iqlabs.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.R
import com.indianservers.iqlabs.ui.components.IqPage
import com.indianservers.iqlabs.ui.theme.IqPurple
import com.indianservers.iqlabs.ui.theme.LocalIqExtras

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val extras = LocalIqExtras.current
    val pages = listOf(
        Triple("Train Your Brain", "Fast cognitive games for memory, logic, reading, math and focus.", R.drawable.hero_boy),
        Triple("Pick Your Level", "Explorer to Genius changes timers, patterns, scoring and challenge shape.", R.drawable.progress_hero),
        Triple("Track Momentum", "XP, streaks, personal bests and achievements stay local on your device.", R.drawable.art_count_stars),
        Triple("Healthy Habit", "Short sessions, no accounts, no pressure mechanics and no medical claims.", R.drawable.settings_mascot),
    )
    IqPage {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onDone, modifier = Modifier.align(Alignment.End)) { Text("Skip") }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(pages[page].third),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(32.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(24.dp))
                Text(pages[page].first, fontWeight = FontWeight.Black, fontSize = 30.sp)
                Text(pages[page].second, color = extras.muted, fontSize = 16.sp, modifier = Modifier.padding(top = 10.dp))
            }
            Column {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    pages.indices.forEach { index ->
                        Box(
                            Modifier
                                .padding(5.dp)
                                .size(if (index == page) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (index == page) IqPurple else extras.muted.copy(alpha = .35f)),
                        )
                    }
                }
                Button(
                    onClick = { if (page == pages.lastIndex) onDone() else page++ },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IqPurple),
                ) {
                    Text(if (page == pages.lastIndex) "Start Training" else "Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
