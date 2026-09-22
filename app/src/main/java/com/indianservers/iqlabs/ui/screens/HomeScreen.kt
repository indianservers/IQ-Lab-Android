package com.indianservers.iqlabs.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.R
import com.indianservers.iqlabs.game.ChallengeSystem
import com.indianservers.iqlabs.game.DailyPlanGenerator
import com.indianservers.iqlabs.game.UnlockSystem
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import com.indianservers.iqlabs.ui.categoryPastel
import com.indianservers.iqlabs.ui.components.GameArt
import com.indianservers.iqlabs.ui.components.IqPage
import com.indianservers.iqlabs.ui.components.IqProgressBar
import com.indianservers.iqlabs.ui.components.PlayBadge
import com.indianservers.iqlabs.ui.components.SectionHeader
import com.indianservers.iqlabs.ui.components.SoftCard
import com.indianservers.iqlabs.ui.displayLevelNumber
import com.indianservers.iqlabs.ui.featuredGameIds
import com.indianservers.iqlabs.ui.levelAccent
import com.indianservers.iqlabs.ui.levelIcon
import com.indianservers.iqlabs.ui.levelPastel
import com.indianservers.iqlabs.ui.progressToNextRank
import com.indianservers.iqlabs.ui.rankForXp
import com.indianservers.iqlabs.ui.theme.IqGold
import com.indianservers.iqlabs.ui.theme.IqPeach
import com.indianservers.iqlabs.ui.theme.IqPurple
import com.indianservers.iqlabs.ui.theme.IqPurpleDeep
import com.indianservers.iqlabs.ui.theme.IqSky
import com.indianservers.iqlabs.ui.theme.LocalIqExtras
import java.time.LocalDate

@Composable
fun HomeScreen(
    xp: Int,
    streak: Int,
    sessions: Int,
    onLevel: (LevelId) -> Unit,
    onGame: (String) -> Unit,
    onSeeAll: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val extras = LocalIqExtras.current
    val dailyPlan = remember(xp, sessions) { DailyPlanGenerator.generate(LocalDate.now(), xp) }
    val rank = rankForXp(xp)
    val levelNo = displayLevelNumber(xp)
    val nextProgress = progressToNextRank(xp)
    val recommended = remember {
        featuredGameIds.mapNotNull { id -> IqCatalog.games.find { it.id == id } }.ifEmpty {
            IqCatalog.games.take(4)
        }
    }

    IqPage {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Box(Modifier.fillMaxWidth().height(168.dp)) {
                    Image(
                        painter = painterResource(R.drawable.hero_boy),
                        contentDescription = "IQ Lab explorer",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 18.dp, y = (-8).dp)
                            .width(168.dp)
                            .height(176.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(extras.card)
                            .clickable(onClick = onOpenSettings),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = extras.muted)
                    }
                    Column(Modifier.align(Alignment.TopStart).fillMaxWidth(.58f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.logo_brain),
                                contentDescription = "IQ Lab",
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("IQ Lab", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        }
                        Text("Train Smarter.\nThink Brighter.", fontWeight = FontWeight.Bold, color = extras.muted, fontSize = 14.sp, lineHeight = 18.sp)
                        Text(
                            "Small Steps\nBig Mind!",
                            fontFamily = FontFamily.Cursive,
                            color = IqSky,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth(), radius = 28.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Brush.linearGradient(listOf(IqPurple, IqSky))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(levelIcon(rank), contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Level $levelNo", color = extras.muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(rank.title, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Spacer(Modifier.height(6.dp))
                            IqProgressBar(nextProgress, MaterialTheme.colorScheme.primary, Modifier.fillMaxWidth())
                            Text("${(nextProgress * 100).toInt()}% to next level", color = extras.muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HomeChip(Icons.Rounded.MilitaryTech, "$xp", "XP", IqGold)
                            HomeChip(Icons.Rounded.LocalFireDepartment, "$streak d", "Streak", IqPeach)
                        }
                    }
                }
            }
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(118.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onGame("kids-light-patterns") },
                ) {
                    Image(
                        painter = painterResource(R.drawable.continue_banner),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(listOf(IqPurpleDeep.copy(alpha = .92f), IqPurple.copy(alpha = .55f), Color.Transparent)),
                        ),
                    )
                    Row(
                        Modifier.fillMaxSize().padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Continue Training", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text("Light Patterns", color = Color.White.copy(alpha = .86f), fontSize = 13.sp)
                        }
                        PlayBadge({ onGame("kids-light-patterns") })
                    }
                }
            }
            item {
                SoftCard(
                    modifier = Modifier.fillMaxWidth().clickable { onGame("kids-color-difference") },
                    color = Color(0xFFFFF1E8),
                    radius = 24.dp,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = IqGold, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Daily Challenge", fontWeight = FontWeight.Black)
                            Text("Beat 6 generated rounds", color = extras.muted, fontSize = 12.sp)
                        }
                        Text("+120 XP", color = IqPeach, fontWeight = FontWeight.Black)
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Today's Training Plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Text("${dailyPlan.games.size} games · ~${dailyPlan.estimatedMinutes} min", color = extras.muted, fontSize = 12.sp)
                }
            }
            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    dailyPlan.games.take(4).forEach { game ->
                        PlanTile(game, onGame)
                    }
                    SoftCard(
                        modifier = Modifier.width(86.dp).height(108.dp).clickable(onClick = onSeeAll),
                        color = extras.chipIdle,
                        padding = 10.dp,
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                Text("More", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            item { SectionHeader("Choose Your Level", "See all") { onLevel(rank) } }
            item {
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    IqCatalog.levels.forEach { level ->
                        val selected = level == rank
                        val unlocked = UnlockSystem.isLevelUnlocked(level, xp, explorerCompleted = 5)
                        Column(
                            Modifier
                                .width(92.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(levelPastel(level))
                                .then(
                                    if (selected) Modifier.border(2.dp, levelAccent(level), RoundedCornerShape(22.dp))
                                    else Modifier
                                )
                                .clickable { onLevel(level) }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = .8f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(levelIcon(level), contentDescription = null, tint = levelAccent(level), modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(level.title, fontWeight = FontWeight.Black, fontSize = 11.sp, maxLines = 1)
                            Text(level.audience, color = extras.muted, fontSize = 10.sp, maxLines = 1)
                            Text(if (unlocked) "35 games" else "Locked", color = extras.muted, fontSize = 10.sp)
                        }
                    }
                }
            }
            item { SectionHeader("Recommended for You", "See all", onSeeAll) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recommended.take(4), key = { it.id }) { game ->
                        RecommendedCard(game, onGame)
                    }
                }
            }
            item {
                Text(
                    "“A sharper mind today, a brighter tomorrow.”",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    color = extras.muted,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                val weekly = remember { ChallengeSystem.weeklyCup(LocalDate.now()) }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF5B4CFF), Color(0xFF7C6CFF))))
                        .clickable { weekly.gameIds.firstOrNull()?.let(onGame) }
                        .padding(18.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = IqGold, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Weekly Cup", color = Color.White, fontWeight = FontWeight.Black)
                            Text("${weekly.gameIds.size} games · local leaderboard", color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
                        }
                        Text("Play Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .7f)).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(value, fontWeight = FontWeight.Black, fontSize = 12.sp)
            Text(label, fontSize = 9.sp, color = LocalIqExtras.current.muted)
        }
    }
}

@Composable
private fun PlanTile(game: GameDefinition, onGame: (String) -> Unit) {
    Column(
        Modifier
            .width(92.dp)
            .height(108.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(categoryPastel(game.category))
            .clickable { onGame(game.id) }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        GameArt(game, Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)))
        Text(game.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("${game.category.label} · ${game.durationMinutes} min", fontSize = 9.sp, color = LocalIqExtras.current.muted, maxLines = 1)
    }
}

@Composable
private fun RecommendedCard(game: GameDefinition, onGame: (String) -> Unit) {
    SoftCard(modifier = Modifier.width(168.dp).clickable { onGame(game.id) }, padding = 10.dp, radius = 22.dp) {
        Box {
            GameArt(game, Modifier.fillMaxWidth().height(110.dp))
            PlayBadge({ onGame(game.id) }, Modifier.align(Alignment.BottomEnd).padding(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(game.name, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${game.category.label} · ${game.durationMinutes} min", color = LocalIqExtras.current.muted, fontSize = 12.sp)
    }
}
