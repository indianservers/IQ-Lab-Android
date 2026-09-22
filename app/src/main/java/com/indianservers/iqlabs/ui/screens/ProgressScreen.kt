package com.indianservers.iqlabs.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.R
import com.indianservers.iqlabs.game.AchievementSystem
import com.indianservers.iqlabs.game.SessionMetric
import com.indianservers.iqlabs.game.SkillArea
import com.indianservers.iqlabs.game.SkillModel
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import com.indianservers.iqlabs.ui.components.EmptyState
import com.indianservers.iqlabs.ui.components.IqPage
import com.indianservers.iqlabs.ui.components.IqProgressBar
import com.indianservers.iqlabs.ui.components.SectionHeader
import com.indianservers.iqlabs.ui.components.SoftCard
import com.indianservers.iqlabs.ui.displayLevelNumber
import com.indianservers.iqlabs.ui.levelAccent
import com.indianservers.iqlabs.ui.progressToNextRank
import com.indianservers.iqlabs.ui.rankForXp
import com.indianservers.iqlabs.ui.theme.IqGold
import com.indianservers.iqlabs.ui.theme.IqMint
import com.indianservers.iqlabs.ui.theme.IqPeach
import com.indianservers.iqlabs.ui.theme.IqPurple
import com.indianservers.iqlabs.ui.theme.IqSky
import com.indianservers.iqlabs.ui.theme.LocalIqExtras
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProgressScreen(xp: Int, streak: Int, sessions: Int, metrics: List<SessionMetric>) {
    val extras = LocalIqExtras.current
    val observations = remember(metrics) {
        metrics.flatMap { metric -> runCatching { SkillModel.observation(IqCatalog.game(metric.gameId), metric) }.getOrDefault(emptyList()) }
    }
    val skillAggregates = remember(observations) { SkillModel.aggregate(observations) }
    val radarSkills = listOf(
        SkillArea.MathematicalReasoning,
        SkillArea.Memory,
        SkillArea.WorkingMemory,
        SkillArea.Logic,
        SkillArea.VerbalReasoning,
        SkillArea.Reading,
    )
    val radarValues = radarSkills.map { skill ->
        skillAggregates.find { it.skill == skill }?.level
            ?: (18 + (xp / 40) + skill.ordinal * 4).coerceAtMost(42)
    }
    val achievements = AchievementSystem.achievements(xp, sessions, streak)
    val unlocked = achievements.count { it.unlocked }
    val rank = rankForXp(xp)

    IqPage {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth().height(168.dp).clip(RoundedCornerShape(28.dp))) {
                    Image(
                        painter = painterResource(R.drawable.progress_hero),
                        contentDescription = "Progress journey",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                        Text("Your Progress", color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                        Text("Small steps. Big brain.", color = Color.White.copy(alpha = .88f))
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ProgressStat(Icons.Rounded.MilitaryTech, "$xp", "Total XP", IqGold, Modifier.weight(1f))
                    ProgressStat(Icons.Rounded.LocalFireDepartment, "${streak}d", "Streak", IqPeach, Modifier.weight(1f))
                    ProgressStat(Icons.Rounded.SportsEsports, "$sessions", "Games", IqSky, Modifier.weight(1f))
                    ProgressStat(Icons.Rounded.EmojiEvents, "$unlocked", "Badges", IqMint, Modifier.weight(1f))
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(52.dp).clip(CircleShape).background(levelAccent(rank).copy(alpha = .18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(rank.title.take(1), color = levelAccent(rank), fontWeight = FontWeight.Black, fontSize = 22.sp)
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Level ${displayLevelNumber(xp)}  ${rank.title}", fontWeight = FontWeight.Black)
                            Text("${(progressToNextRank(xp) * 100).toInt()}% to next rank", color = extras.muted, fontSize = 12.sp)
                            Spacer(Modifier.height(6.dp))
                            IqProgressBar(progressToNextRank(xp), levelAccent(rank), Modifier.fillMaxWidth())
                        }
                    }
                }
            }
            item { Text("Level Progress", fontWeight = FontWeight.Black, fontSize = 18.sp) }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LevelId.entries.forEach { level ->
                            val value = progressForLevel(level, xp)
                            Column {
                                Row {
                                    Text(level.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("${(value * 100).toInt()}%", color = extras.muted, fontSize = 12.sp)
                                }
                                Spacer(Modifier.height(6.dp))
                                IqProgressBar(value, levelAccent(level), Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
            item { SectionHeader("Skill Profile", "Details") {} }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadarChart(radarValues, Modifier.size(168.dp).padding(8.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            radarSkills.zip(radarValues).forEach { (skill, value) ->
                                Text("${skill.label}: $value%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            val strongest = radarSkills.zip(radarValues).maxBy { it.second }
                            val weakest = radarSkills.zip(radarValues).minBy { it.second }
                            Text("Strongest: ${strongest.first.label}", color = IqMint, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Area to improve: ${weakest.first.label}", color = IqPeach, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
            item { Text("Achievements", fontWeight = FontWeight.Black, fontSize = 18.sp) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(achievements.take(8)) { achievement ->
                        SoftCard(
                            modifier = Modifier.width(120.dp),
                            color = if (achievement.unlocked) IqMint.copy(alpha = .18f) else extras.card,
                            padding = 12.dp,
                        ) {
                            Text(if (achievement.unlocked) "★" else "○", color = if (achievement.unlocked) IqGold else extras.muted, fontSize = 20.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(achievement.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 2)
                            Text("${achievement.progress}/${achievement.target}", color = extras.muted, fontSize = 11.sp)
                        }
                    }
                }
            }
            item { Text("Game History", fontWeight = FontWeight.Black, fontSize = 18.sp) }
            if (metrics.isEmpty()) {
                item { EmptyState("Complete a session to build real trends and history.") }
            }
            items(metrics.takeLast(8).reversed()) { metric ->
                SoftCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(IqCatalog.game(metric.gameId).name, fontWeight = FontWeight.Bold)
                            Text("${metric.date} · ${metric.category.label}", color = extras.muted, fontSize = 12.sp)
                        }
                        Text("${metric.accuracy}% · ${metric.stars}★", fontWeight = FontWeight.Black, color = levelAccent(metric.level))
                    }
                }
            }
        }
    }
}

private fun progressForLevel(level: LevelId, xp: Int): Float {
    val next = LevelId.entries.getOrNull(level.ordinal + 1)
    return when {
        xp >= (next?.minXp ?: (level.minXp + 2000)) -> 1f
        xp <= level.minXp -> (xp / (level.minXp + 400f)).coerceIn(0.08f, .35f)
        else -> ((xp - level.minXp).toFloat() / ((next?.minXp ?: (level.minXp + 1200)) - level.minXp)).coerceIn(0.08f, 1f)
    }
}

@Composable
private fun ProgressStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    SoftCard(modifier = modifier, padding = 10.dp, radius = 18.dp, shadow = 6.dp) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp)
        Text(label, fontSize = 10.sp, color = LocalIqExtras.current.muted)
    }
}

@Composable
private fun RadarChart(values: List<Int>, modifier: Modifier = Modifier) {
    val color = IqPurple
    Canvas(modifier) {
        val n = values.size.coerceAtLeast(3)
        val radius = size.minDimension / 2.1f
        val center = Offset(size.width / 2f, size.height / 2f)
        fun point(index: Int, t: Float): Offset {
            val angle = (2.0 * PI * index / n - PI / 2).toFloat()
            return Offset(center.x + cos(angle) * radius * t, center.y + sin(angle) * radius * t)
        }
        for (ring in 1..4) {
            val path = Path()
            repeat(n) { i ->
                val p = point(i, ring / 4f)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            drawPath(path, color.copy(alpha = .12f), style = Stroke(width = 2f))
        }
        val fill = Path()
        values.forEachIndexed { i, value ->
            val p = point(i, (value / 100f).coerceIn(0.08f, 1f))
            if (i == 0) fill.moveTo(p.x, p.y) else fill.lineTo(p.x, p.y)
        }
        fill.close()
        drawPath(fill, color.copy(alpha = .28f))
        drawPath(fill, color, style = Stroke(width = 4f))
    }
}
