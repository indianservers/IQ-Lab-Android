package com.indianservers.iqlabs

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.game.AchievementSystem
import com.indianservers.iqlabs.game.AdaptiveDifficultyEngine
import com.indianservers.iqlabs.game.BackupSystem
import com.indianservers.iqlabs.game.ChallengeSystem
import com.indianservers.iqlabs.game.DailyPlanGenerator
import com.indianservers.iqlabs.game.DifficultyMode
import com.indianservers.iqlabs.game.GameResult
import com.indianservers.iqlabs.game.LevelBlueprint
import com.indianservers.iqlabs.game.MasterySystem
import com.indianservers.iqlabs.game.ProgressBackup
import com.indianservers.iqlabs.game.Question
import com.indianservers.iqlabs.game.QuestionFactory
import com.indianservers.iqlabs.game.SessionMetric
import com.indianservers.iqlabs.game.ScoreEngine
import com.indianservers.iqlabs.game.SkillModel
import com.indianservers.iqlabs.game.UnlockSystem
import com.indianservers.iqlabs.model.Category
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.ImplementationStatus
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import com.indianservers.iqlabs.ui.theme.AppColorTheme
import com.indianservers.iqlabs.ui.theme.IQLabsTheme
import java.time.Instant
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { IQLabsTheme { IqLabApp() } }
    }
}

private enum class Tab(val label: String, val icon: String) {
    Home("Home", "⌂"), Games("Games", "◆"), Progress("Progress", "▥"), Settings("Settings", "⚙")
}

private sealed interface Screen {
    data object Onboarding : Screen
    data class Main(val tab: Tab = Tab.Home) : Screen
    data class LevelDetails(val level: LevelId) : Screen
    data class GameDetails(val id: String) : Screen
    data class Play(val id: String, val seed: Int = 1) : Screen
    data class Results(val id: String, val result: GameResult) : Screen
}

private class ProgressStore(context: Context) {
    private val prefs = context.getSharedPreferences("iq_lab_progress", Context.MODE_PRIVATE)
    var xp: Int
        get() = prefs.getInt("xp", 480)
        set(value) = prefs.edit().putInt("xp", value.coerceAtLeast(0)).apply()
    var streak: Int
        get() = prefs.getInt("streak", 2)
        set(value) = prefs.edit().putInt("streak", value.coerceAtLeast(0)).apply()
    var onboardingDone: Boolean
        get() = prefs.getBoolean("onboarding", false)
        set(value) = prefs.edit().putBoolean("onboarding", value).apply()
    var sound: Boolean
        get() = prefs.getBoolean("sound", true)
        set(value) = prefs.edit().putBoolean("sound", value).apply()
    var haptics: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(value) = prefs.edit().putBoolean("haptics", value).apply()
    var reducedMotion: Boolean
        get() = prefs.getBoolean("reducedMotion", false)
        set(value) = prefs.edit().putBoolean("reducedMotion", value).apply()
    var difficultyMode: DifficultyMode
        get() = DifficultyMode.valueOf(prefs.getString("difficultyMode", DifficultyMode.Adaptive.name) ?: DifficultyMode.Adaptive.name)
        set(value) = prefs.edit().putString("difficultyMode", value.name).apply()
    var appColorTheme: AppColorTheme
        get() = runCatching { AppColorTheme.valueOf(prefs.getString("appColorTheme", AppColorTheme.SimpleWhite.name) ?: AppColorTheme.SimpleWhite.name) }.getOrDefault(AppColorTheme.SimpleWhite)
        set(value) = prefs.edit().putString("appColorTheme", value.name).apply()
    fun best(id: String): Int? = prefs.getInt("best_$id", -1).takeIf { it >= 0 }
    fun setBest(id: String, score: Int) = prefs.edit().putInt("best_$id", score).apply()
    fun sessions(): Int = prefs.getInt("sessions", 0)
    fun addSession() = prefs.edit().putInt("sessions", sessions() + 1).apply()
    fun metrics(): List<SessionMetric> = prefs.getString("metrics", "").orEmpty().lineSequence().mapNotNull { line ->
        val parts = line.split(",")
        runCatching {
            SessionMetric(
                gameId = parts[0],
                date = LocalDate.parse(parts[1]),
                level = LevelId.valueOf(parts[2]),
                category = Category.valueOf(parts[3]),
                difficultyTier = parts[4].toInt(),
                score = parts[5].toInt(),
                accuracy = parts[6].toInt(),
                responseMs = parts[7].toInt(),
                xp = parts[8].toInt(),
                stars = parts[9].toInt(),
            )
        }.getOrNull()
    }.toList()
    fun addMetric(metric: SessionMetric) {
        val row = listOf(metric.gameId, metric.date, metric.level.name, metric.category.name, metric.difficultyTier, metric.score, metric.accuracy, metric.responseMs, metric.xp, metric.stars).joinToString(",")
        val previous = prefs.getString("metrics", "").orEmpty().lines().filter { it.isNotBlank() }.takeLast(119)
        prefs.edit().putString("metrics", (previous + row).joinToString("\n")).apply()
    }
    fun favorites(): Set<String> = prefs.getStringSet("favorites", emptySet()).orEmpty()
    fun toggleFavorite(id: String) {
        val next = favorites().toMutableSet().also { if (!it.add(id)) it.remove(id) }
        prefs.edit().putStringSet("favorites", next).apply()
    }
    fun reset() = prefs.edit().clear().apply()
}

@Composable
private fun IqLabApp() {
    val context = LocalContext.current
    val store = remember(context) { ProgressStore(context) }
    var screen by remember { mutableStateOf<Screen>(if (store.onboardingDone) Screen.Main() else Screen.Onboarding) }
    val favorites = remember { mutableStateMapOf<String, Boolean>().also { map -> store.favorites().forEach { map[it] = true } } }
    var xp by rememberSaveable { mutableIntStateOf(store.xp) }
    var streak by rememberSaveable { mutableIntStateOf(store.streak) }
    var sessions by rememberSaveable { mutableIntStateOf(store.sessions()) }
    var sound by rememberSaveable { mutableStateOf(store.sound) }
    var haptics by rememberSaveable { mutableStateOf(store.haptics) }
    var reducedMotion by rememberSaveable { mutableStateOf(store.reducedMotion) }
    var difficultyMode by rememberSaveable { mutableStateOf(store.difficultyMode) }
    var appColorTheme by rememberSaveable { mutableStateOf(store.appColorTheme) }
    var metrics by remember { mutableStateOf(store.metrics()) }

    fun go(next: Screen) { screen = next }
    fun finishOnboarding() { store.onboardingDone = true; screen = Screen.Main() }

    IQLabsTheme(appColorTheme = appColorTheme) {
        when (val current = screen) {
        Screen.Onboarding -> OnboardingScreen(::finishOnboarding)
        is Screen.Main -> MainShell(
            tab = current.tab,
            xp = xp,
            streak = streak,
            sessions = sessions,
            favorites = favorites,
            sound = sound,
            haptics = haptics,
            reducedMotion = reducedMotion,
            difficultyMode = difficultyMode,
            appColorTheme = appColorTheme,
            metrics = metrics,
            onTab = { go(Screen.Main(it)) },
            onLevel = { go(Screen.LevelDetails(it)) },
            onGame = { go(Screen.GameDetails(it)) },
            onFavorite = {
                store.toggleFavorite(it)
                favorites[it] = !(favorites[it] ?: false)
            },
            onReplayOnboarding = { go(Screen.Onboarding) },
            onReset = { store.reset(); xp = store.xp; streak = store.streak; sessions = store.sessions(); favorites.clear() },
            onSound = { sound = it; store.sound = it },
            onHaptics = { haptics = it; store.haptics = it },
            onReducedMotion = { reducedMotion = it; store.reducedMotion = it },
            onDifficultyMode = { difficultyMode = it; store.difficultyMode = it },
            onAppColorTheme = { appColorTheme = it; store.appColorTheme = it },
        )
        is Screen.LevelDetails -> LevelDetailsScreen(current.level, xp, { go(Screen.Main(Tab.Games)) }, { go(Screen.GameDetails(it)) })
        is Screen.GameDetails -> GameDetailsScreen(IqCatalog.game(current.id), store.best(current.id), difficultyMode, { difficultyMode = it; store.difficultyMode = it }, { go(Screen.Main(Tab.Games)) }, { go(Screen.Play(current.id, sessions + 7)) })
        is Screen.Play -> PlayScreen(
            game = IqCatalog.game(current.id),
            seed = current.seed,
            difficultyMode = difficultyMode,
            onExit = { go(Screen.GameDetails(current.id)) },
            onComplete = { result ->
                val personalBest = ScoreEngine.isPersonalBest(result.score, store.best(current.id))
                val stars = MasterySystem.starsFor(IqCatalog.game(current.id), result.score, result.accuracy)
                val saved = result.copy(personalBest = personalBest, stars = stars)
                if (personalBest) store.setBest(current.id, result.score)
                store.xp += saved.xp
                store.streak = maxOf(1, store.streak)
                store.addSession()
                store.addMetric(SessionMetric(current.id, LocalDate.now(), IqCatalog.game(current.id).level, IqCatalog.game(current.id).category, IqCatalog.game(current.id).difficulty, saved.score, saved.accuracy, 3600, saved.xp, saved.stars))
                xp = store.xp
                streak = store.streak
                sessions = store.sessions()
                metrics = store.metrics()
                go(Screen.Results(current.id, saved))
            }
        )
        is Screen.Results -> ResultsScreen(IqCatalog.game(current.id), current.result, { go(Screen.Play(current.id, sessions + 11)) }, { go(Screen.Main(Tab.Games)) })
        }
    }
}

@Composable
private fun MainShell(
    tab: Tab,
    xp: Int,
    streak: Int,
    sessions: Int,
    favorites: Map<String, Boolean>,
    sound: Boolean,
    haptics: Boolean,
    reducedMotion: Boolean,
    difficultyMode: DifficultyMode,
    appColorTheme: AppColorTheme,
    metrics: List<SessionMetric>,
    onTab: (Tab) -> Unit,
    onLevel: (LevelId) -> Unit,
    onGame: (String) -> Unit,
    onFavorite: (String) -> Unit,
    onReplayOnboarding: () -> Unit,
    onReset: () -> Unit,
    onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
    onDifficultyMode: (DifficultyMode) -> Unit,
    onAppColorTheme: (AppColorTheme) -> Unit,
) {
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .96f)) {
                Tab.entries.forEach {
                    NavigationBarItem(selected = tab == it, onClick = { onTab(it) }, icon = { Text(it.icon) }, label = { Text(it.label) })
                }
            }
        }
    ) { padding ->
        LabBackground(Modifier.padding(padding)) {
            when (tab) {
                Tab.Home -> HomeScreen(xp, streak, sessions, onLevel, onGame)
                Tab.Games -> CatalogueScreen(favorites, onGame, onFavorite)
                Tab.Progress -> ProgressScreen(xp, streak, sessions, metrics)
                Tab.Settings -> SettingsScreen(sound, haptics, reducedMotion, difficultyMode, appColorTheme, onSound, onHaptics, onReducedMotion, onDifficultyMode, onAppColorTheme, onReplayOnboarding, onReset)
            }
        }
    }
}

@Composable
private fun LabBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant))
        )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val grid = 72.dp.toPx()
            for (x in 0..(size.width / grid).toInt() + 1) {
                drawLine(Color.White.copy(alpha = .035f), Offset(x * grid, 0f), Offset(x * grid, size.height))
            }
            for (y in 0..(size.height / grid).toInt() + 1) {
                drawLine(Color.White.copy(alpha = .035f), Offset(0f, y * grid), Offset(size.width, y * grid))
            }
            drawCircle(Color.Cyan.copy(alpha = .10f), radius = size.minDimension * .33f, center = Offset(size.width * .88f, size.height * .04f))
        }
        content()
    }
}

@Composable
private fun OnboardingScreen(onDone: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val pages = listOf(
        "Train Your Brain" to "Fast cognitive games for memory, logic, reading, math and focus.",
        "Pick Your Level" to "Explorer to Genius changes timers, patterns, scoring and challenge shape.",
        "Track Momentum" to "XP, streaks, personal bests and achievements stay local on your device.",
        "Healthy Habit" to "Short sessions, no accounts, no pressure mechanics and no medical claims.",
    )
    LabBackground {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onDone, modifier = Modifier.align(Alignment.End)) { Text("Skip") }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                BrainOrb(Modifier.size(230.dp))
                Spacer(Modifier.height(30.dp))
                Text(pages[page].first, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black)
                Text(pages[page].second, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground.copy(.76f), modifier = Modifier.padding(top = 12.dp))
            }
            Column {
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    pages.indices.forEach { Dot(active = it == page) }
                }
                Button(onClick = { if (page == pages.lastIndex) onDone() else page++ }, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(if (page == pages.lastIndex) "Start Training" else "Continue", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(xp: Int, streak: Int, sessions: Int, onLevel: (LevelId) -> Unit, onGame: (String) -> Unit) {
    val dailyPlan = remember(xp, sessions) { DailyPlanGenerator.generate(LocalDate.now(), xp) }
    val dailyArena = remember { ChallengeSystem.dailyArena(LocalDate.now()) }
    val weeklyCup = remember { ChallengeSystem.weeklyCup(LocalDate.now()) }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("IQ Lab", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text("Ready for a sharper short session?", style = MaterialTheme.typography.titleMedium)
                }
                StatPill("XP", xp.toString())
                Spacer(Modifier.width(8.dp))
                StatPill("Streak", "$streak d")
            }
        }
        item {
            HeroCard(onClick = { onGame("memory-grid") }) {
                Column(Modifier.weight(1f)) {
                    Text("Continue Training", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Memory Grid · Level 8 · 85%", color = MaterialTheme.colorScheme.onSurface.copy(.75f))
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { .85f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape))
                }
                Button(onClick = { onGame("memory-grid") }) { Text("Continue") }
            }
        }
        item {
            AccentCard(LevelId.Mastermind.accent, Modifier.clickable { onGame("quick-match") }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("◎", fontSize = 42.sp, color = LevelId.Mastermind.accent)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Daily Challenge", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Quick Match: beat six generated rounds for bonus XP.")
                    }
                    Text("+120 XP", color = LevelId.Mastermind.accent, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Text("Today's Training Plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            AccentCard(MaterialTheme.colorScheme.tertiary) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("${dailyPlan.games.size} games · about ${dailyPlan.estimatedMinutes} min · +${dailyPlan.bonusXp} bonus XP", fontWeight = FontWeight.Bold)
                    dailyPlan.games.take(4).forEach { game ->
                        Row(Modifier.fillMaxWidth().clickable { onGame(game.id) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(game.icon, color = game.accent, modifier = Modifier.width(34.dp))
                            Text(game.name, Modifier.weight(1f))
                            Text(game.category.label, color = MaterialTheme.colorScheme.onSurface.copy(.65f))
                        }
                    }
                }
            }
        }
        item {
            Text("Challenge Centre", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            AccentCard(LevelId.Genius.accent) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChallengeLine("Daily Arena", "${dailyArena.gameIds.size} games · tier ${dailyArena.tier} · ${dailyArena.rounds} rounds", dailyArena.gameIds.firstOrNull(), onGame)
                    ChallengeLine("Weekly Cup", "${weeklyCup.gameIds.size} games · ${weeklyCup.identity} · scoring ${weeklyCup.scoringVersion}", weeklyCup.gameIds.firstOrNull(), onGame)
                    val code = ChallengeSystem.shareCode(dailyArena)
                    Text("Share code preview: ${code.take(28)}...", color = MaterialTheme.colorScheme.onSurface.copy(.7f), fontSize = 12.sp)
                    Text("Local only: no fake rankings, no online players.", color = LevelId.Genius.accent, fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Text("Choose Your Level", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(IqCatalog.levels) { level -> LevelWideCard(level, xp, onClick = { onLevel(level) }) }
        item { Text("Recommended Games", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IqCatalog.games.filter { it.status == ImplementationStatus.Playable }.take(3).forEach { MiniGameCard(it, Modifier.weight(1f)) { onGame(it.id) } }
            }
        }
        item {
            Text("Sessions played: $sessions · Catalogue: ${IqCatalog.games.size} games", color = MaterialTheme.colorScheme.onBackground.copy(.7f))
        }
    }
}

@Composable
private fun ChallengeLine(title: String, detail: String, gameId: String?, onGame: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(detail, color = MaterialTheme.colorScheme.onSurface.copy(.68f), fontSize = 12.sp)
        }
        if (gameId != null) TextButton(onClick = { onGame(gameId) }) { Text("Start") }
    }
}

@Composable
private fun CatalogueScreen(favorites: Map<String, Boolean>, onGame: (String) -> Unit, onFavorite: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var level by rememberSaveable { mutableStateOf<LevelId?>(null) }
    var category by rememberSaveable { mutableStateOf<Category?>(null) }
    val games = IqCatalog.games.filter {
        (query.isBlank() || it.name.contains(query, true) || it.tags.any { tag -> tag.contains(query, true) }) &&
            (level == null || it.level == level) && (category == null || it.category == category)
    }
    LazyVerticalGrid(columns = GridCells.Adaptive(156.dp), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text("IQ Games", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), singleLine = true, label = { Text("Search games, tags or skills") })
        }
        item(span = { GridItemSpan(maxLineSpan) }) { ChipRow(LevelId.entries, level, { level = if (level == it) null else it }, { it.title }) }
        item(span = { GridItemSpan(maxLineSpan) }) { ChipRow(Category.entries, category, { category = if (category == it) null else it }, { it.label }) }
        item(span = { GridItemSpan(maxLineSpan) }) { Text("${games.size} games", fontWeight = FontWeight.Bold) }
        if (games.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("No games match those filters.") }
        items(games, key = { it.id }) { game ->
            GameGridCard(game, favorites[game.id] == true, onFavorite = { onFavorite(game.id) }, onClick = { onGame(game.id) })
        }
    }
}

@Composable
private fun LevelDetailsScreen(level: LevelId, xp: Int, onBack: () -> Unit, onGame: (String) -> Unit) {
    BackHandler(onBack = onBack)
    LabBackground {
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                TextButton(onClick = onBack) { Text("Back") }
                AccentCard(level.accent) {
                    Column {
                        Text(level.title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = level.accent)
                        Text("${level.audience} · ${level.tagline}")
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(progress = { (xp.toFloat() / (level.minXp + 1000)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape))
                        Text("${IqCatalog.levelGames(level).size} games · unlock: ${if (xp >= level.minXp) "ready" else "${level.minXp - xp} XP to go"}")
                    }
                }
            }
            items(IqCatalog.levelGames(level)) { GameRow(it, false, {}, { onGame(it.id) }) }
        }
    }
}

@Composable
private fun GameDetailsScreen(game: GameDefinition, best: Int?, difficultyMode: DifficultyMode, onDifficultyMode: (DifficultyMode) -> Unit, onBack: () -> Unit, onPlay: () -> Unit) {
    val profile = remember(game.id, difficultyMode) { AdaptiveDifficultyEngine.initialProfile(game, difficultyMode) }
    val parameters = AdaptiveDifficultyEngine.parametersFor(game, profile)
    val blueprintStages = remember(game.id) { LevelBlueprint.stagesForCatalogGame(game.id) }
    BackHandler(onBack = onBack)
    LabBackground {
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                TextButton(onClick = onBack) { Text("Back") }
                AccentCard(game.accent) {
                    Column {
                        Text(game.icon, fontSize = 54.sp, color = game.accent)
                        Text(game.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                        Text(game.description, color = MaterialTheme.colorScheme.onSurface.copy(.78f))
                    }
                }
            }
            item { DetailGrid(game, best) }
            item {
                AccentCard(game.accent) {
                    Column {
                        Text("How To Play", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(game.instructions)
                        Text("Scoring: ${game.scoringModel}", modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            item {
                AccentCard(LevelId.Thinker.accent) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Adaptive Difficulty", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Adaptive Difficulty adjusts future rounds based on your recent accuracy, speed and consistency.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DifficultyMode.entries.forEach { mode ->
                                FilterChip(selected = difficultyMode == mode, onClick = { onDifficultyMode(mode) }, label = { Text(mode.name) })
                            }
                        }
                        Text("Tier ${parameters.tier}/12 · ${profile.lastReason}", color = game.accent)
                        Text("Parameters: sequence ${parameters.sequenceLength}, distractors ${parameters.distractors}, response window ${parameters.responseWindowSeconds}s.")
                    }
                }
            }
            if (blueprintStages.isNotEmpty()) {
                item {
                    AccentCard(game.accent) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("12-Level Blueprint", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("This priority game follows the workbook progression from Orientation to Limit Test.")
                            blueprintStages.take(4).forEach { stage ->
                                Text("L${stage.level} ${stage.stage}: x${"%.2f".format(stage.scoreMultiplier)}", color = MaterialTheme.colorScheme.onSurface.copy(.76f))
                            }
                            Text("Current: ${LevelBlueprint.stageFor(game.id, parameters.tier)?.exactDesign ?: "Catalogue tier progression."}", color = game.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                if (game.status == ImplementationStatus.Playable) {
                    Button(onClick = onPlay, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = game.accent, contentColor = Color(0xFF07101F))) { Text("Play Session", fontWeight = FontWeight.Bold) }
                } else {
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Coming in a later phase") }
                }
            }
        }
    }
}

@Composable
private fun PlayScreen(game: GameDefinition, seed: Int, difficultyMode: DifficultyMode, onExit: () -> Unit, onComplete: (GameResult) -> Unit) {
    val profile = remember(game.id, difficultyMode) { AdaptiveDifficultyEngine.initialProfile(game, difficultyMode) }
    val parameters = remember(game.id, difficultyMode) { AdaptiveDifficultyEngine.parametersFor(game, profile) }
    val blueprintStage = remember(game.id, parameters.tier) { LevelBlueprint.stageFor(game.id, parameters.tier) }
    val questions = remember(game.id, seed, parameters.tier) { QuestionFactory.questionsFor(game, seed, 6, parameters.tier) }
    var started by rememberSaveable { mutableStateOf(false) }
    var paused by rememberSaveable { mutableStateOf(false) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var correct by rememberSaveable { mutableIntStateOf(0) }
    var incorrect by rememberSaveable { mutableIntStateOf(0) }
    var consecutiveIncorrect by rememberSaveable { mutableIntStateOf(0) }
    var combo by rememberSaveable { mutableIntStateOf(0) }
    var bestCombo by rememberSaveable { mutableIntStateOf(0) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmExit by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = { if (started && index < questions.size) confirmExit = true else onExit() })
    LabBackground {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (started) confirmExit = true else onExit() }) { Text("Exit") }
                Text(game.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text("Tier ${parameters.tier} · ${ScoreEngine.score(correct, incorrect, bestCombo, parameters.tier, 12)}")
            }
            LinearProgressIndicator(progress = { index / questions.size.toFloat() }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape))
            RoundProgressStars(
                completed = correct + incorrect,
                total = questions.size,
                accent = game.accent,
            )
            if (!started) {
                AccentCard(game.accent) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Session Brief", fontSize = 26.sp, fontWeight = FontWeight.Black)
                        Text(game.instructions)
                        Text("${difficultyMode.name} mode · tier ${parameters.tier}/12 · ${profile.lastReason}")
                        if (blueprintStage != null) {
                            Text("Blueprint L${blueprintStage.level} ${blueprintStage.stage}: ${blueprintStage.exactDesign}")
                            Text("Mastery: ${blueprintStage.masteryTarget}", color = game.accent)
                        }
                        Text("Six generated rounds · pause anytime · combos reward accuracy.")
                        Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth()) { Text("Start Countdown") }
                    }
                }
            } else if (paused) {
                EmptyState("Paused")
                Button(onClick = { paused = false }, modifier = Modifier.fillMaxWidth()) { Text("Resume") }
            } else {
                val question = questions[index]
                val adaptiveHint = adaptiveHintFor(game, question, consecutiveIncorrect)
                val answerHandler: (String) -> Unit = { answer ->
                    val ok = answer == question.answer
                    val nextCorrect = correct + if (ok) 1 else 0
                    val nextIncorrect = incorrect + if (ok) 0 else 1
                    val nextCombo = if (ok) combo + 1 else 0
                    val nextBestCombo = maxOf(bestCombo, nextCombo)
                    correct = nextCorrect
                    incorrect = nextIncorrect
                    consecutiveIncorrect = if (ok) 0 else consecutiveIncorrect + 1
                    combo = nextCombo
                    bestCombo = nextBestCombo
                    feedback = roundFeedback(game, ok, question.detail)
                    if (index == questions.lastIndex) {
                        val score = ScoreEngine.score(nextCorrect, nextIncorrect, nextBestCombo, parameters.tier, 12)
                        val xp = ScoreEngine.xp(nextCorrect, nextIncorrect, parameters.tier, game.xpReward)
                        onComplete(GameResult(score, nextCorrect, nextIncorrect, xp, nextBestCombo, false))
                    } else index++
                }
                if (game.id == "maze-scout") {
                    MazeSwipeQuestionCard(game, question, feedback, adaptiveHint, answerHandler)
                } else {
                    QuestionCard(game, question, feedback, adaptiveHint, answerHandler)
                }
                OutlinedButton(onClick = { paused = true }, modifier = Modifier.fillMaxWidth()) { Text("Pause") }
            }
        }
    }
    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("Exit session?") },
            text = { Text("Current round progress will not be saved.") },
            confirmButton = { TextButton(onClick = { confirmExit = false; onExit() }) { Text("Exit") } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Resume") } },
        )
    }
}

@Composable
private fun ResultsScreen(game: GameDefinition, result: GameResult, onRetry: () -> Unit, onCatalogue: () -> Unit) {
    LabBackground {
        Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center) {
            AccentCard(game.accent) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(if (result.personalBest) "Personal Best" else "Session Complete", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text(result.score.toString(), fontSize = 62.sp, fontWeight = FontWeight.Black, color = game.accent)
                    Text("★".repeat(result.stars).padEnd(3, '☆'), color = game.accent, fontSize = 30.sp)
                    Text("${result.accuracy}% accuracy · ${result.correct} correct · ${result.incorrect} missed")
                    Text("+${result.xp} XP · best combo ${result.bestCombo}", modifier = Modifier.padding(top = 6.dp))
                    Text(performanceMessage(result), modifier = Modifier.padding(top = 14.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Retry") }
            TextButton(onClick = onCatalogue, modifier = Modifier.fillMaxWidth()) { Text("Return to Catalogue") }
        }
    }
}

@Composable
private fun ProgressScreen(xp: Int, streak: Int, sessions: Int, metrics: List<SessionMetric>) {
    val observations = remember(metrics) { metrics.flatMap { metric -> runCatching { SkillModel.observation(IqCatalog.game(metric.gameId), metric) }.getOrDefault(emptyList()) } }
    val skillAggregates = remember(observations) { SkillModel.aggregate(observations) }
    val averageAccuracy = metrics.takeIf { it.isNotEmpty() }?.map { it.accuracy }?.average()?.toInt()
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Advanced Progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black) }
        item {
            HeroCard {
                Column(Modifier.weight(1f)) {
                    Text("Rank ${1 + xp / 700}", fontSize = 28.sp, fontWeight = FontWeight.Black)
                    Text("$xp XP · $streak day streak · $sessions sessions · ${averageAccuracy?.let { "$it% avg accuracy" } ?: "not enough data"}")
                }
                BrainOrb(Modifier.size(92.dp))
            }
        }
        item { Text("Level Progress", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        items(LevelId.entries) { level -> LevelProgress(level, xp) }
        item { Text("Skill Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        items(skillAggregates.take(6)) { skill ->
            AccentCard(MaterialTheme.colorScheme.primary) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(skill.skill.label, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text(skill.confidence, color = MaterialTheme.colorScheme.onSurface.copy(.7f), fontSize = 12.sp)
                    }
                    LinearProgressIndicator(progress = { skill.level / 100f }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape))
                    Text("${skill.trend} · ${skill.observations} observations", color = MaterialTheme.colorScheme.onSurface.copy(.72f), fontSize = 12.sp)
                }
            }
        }
        item { Text("Game History", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        if (metrics.isEmpty()) item { EmptyState("Complete a session to build real trends and history.") }
        items(metrics.takeLast(8).reversed()) { metric ->
            AccentCard(metric.level.accent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(IqCatalog.game(metric.gameId).name, fontWeight = FontWeight.Bold)
                        Text("${metric.date} · tier ${metric.difficultyTier} · ${metric.category.label}", color = MaterialTheme.colorScheme.onSurface.copy(.7f))
                    }
                    Text("${metric.accuracy}% · ${metric.stars}★")
                }
            }
        }
        item { Text("Achievements", fontWeight = FontWeight.Bold, fontSize = 20.sp) }
        items(AchievementSystem.achievements(xp, sessions, streak)) { achievement -> AchievementRow(achievement.title, achievement.unlocked, achievement.progress, achievement.target) }
    }
}

@Composable
private fun SettingsScreen(sound: Boolean, haptics: Boolean, reducedMotion: Boolean, difficultyMode: DifficultyMode, appColorTheme: AppColorTheme, onSound: (Boolean) -> Unit, onHaptics: (Boolean) -> Unit, onReducedMotion: (Boolean) -> Unit, onDifficultyMode: (DifficultyMode) -> Unit, onAppColorTheme: (AppColorTheme) -> Unit, onReplayOnboarding: () -> Unit, onReset: () -> Unit) {
    var textScale by rememberSaveable { mutableStateOf(.5f) }
    var colorBlind by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black) }
        item {
            AccentCard(MaterialTheme.colorScheme.primary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Color theme", fontWeight = FontWeight.Bold)
                    Text("Simple White is the default clean UI. Other themes keep the same layout with different accents.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f))
                    AppColorTheme.entries.chunked(3).forEach { rowThemes ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            rowThemes.forEach { theme ->
                                FilterChip(
                                    selected = appColorTheme == theme,
                                    onClick = { onAppColorTheme(theme) },
                                    label = { Text(theme.label, maxLines = 1) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            repeat(3 - rowThemes.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        item { ToggleRow("Sound effects", sound, onSound) }
        item { ToggleRow("Haptics", haptics, onHaptics) }
        item { ToggleRow("Reduced motion", reducedMotion, onReducedMotion) }
        item { ToggleRow("Colour-blind-friendly palette", colorBlind) { colorBlind = it } }
        item {
            AccentCard(LevelId.Thinker.accent) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Difficulty mode", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DifficultyMode.entries.forEach { mode -> FilterChip(selected = difficultyMode == mode, onClick = { onDifficultyMode(mode) }, label = { Text(mode.name) }) }
                    }
                    Text("Adaptive uses recent accuracy, speed and consistency. Fixed keeps your chosen tier. Relaxed reduces time pressure.")
                }
            }
        }
        item {
            AccentCard(MaterialTheme.colorScheme.primary) {
                Column {
                    Text("Text size", fontWeight = FontWeight.Bold)
                    Slider(value = textScale, onValueChange = { textScale = it })
                }
            }
        }
        item { OutlinedButton(onClick = onReplayOnboarding, modifier = Modifier.fillMaxWidth()) { Text("Replay onboarding") } }
        item {
            AccentCard(MaterialTheme.colorScheme.tertiary) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Release & Privacy", fontWeight = FontWeight.Bold)
                    Text("Offline-first. No account, ads, contacts, camera, microphone or location permission.")
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
                    Text("Backup format preview: $preview...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(.7f))
                }
            }
        }
        item { OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) { Text("Reset progress") } }
        item { Text("About IQ Lab: cognitive entertainment and practice. It is not a medical, psychological or diagnostic product.", color = MaterialTheme.colorScheme.onBackground.copy(.72f)) }
    }
    if (confirmReset) {
        AlertDialog(onDismissRequest = { confirmReset = false }, title = { Text("Reset progress?") }, text = { Text("This clears local XP, favorites, best scores and settings.") }, confirmButton = { TextButton(onClick = { confirmReset = false; onReset() }) { Text("Reset") } }, dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } })
    }
}

@Composable
private fun QuestionCard(game: GameDefinition, question: Question, feedback: String?, adaptiveHint: String?, onAnswer: (String) -> Unit) {
    AccentCard(game.accent) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(question.prompt, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            AnimatedVisibility(adaptiveHint != null) {
                Text(
                    adaptiveHint.orEmpty(),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(game.accent.copy(alpha = .14f)).padding(12.dp),
                    color = game.accent,
                    fontWeight = FontWeight.Bold,
                )
            }
            question.choices.forEach { choice ->
                Button(onClick = { onAnswer(choice) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) { Text(choice, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
            AnimatedVisibility(feedback != null) { Text(feedback.orEmpty(), color = game.accent) }
        }
    }
}

@Composable
private fun MazeSwipeQuestionCard(game: GameDefinition, question: Question, feedback: String?, adaptiveHint: String?, onAnswer: (String) -> Unit) {
    var dragTotal by remember { mutableStateOf(Offset.Zero) }
    AccentCard(game.accent) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(question.prompt, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            AnimatedVisibility(adaptiveHint != null) {
                Text(
                    adaptiveHint.orEmpty(),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(game.accent.copy(alpha = .14f)).padding(12.dp),
                    color = game.accent,
                    fontWeight = FontWeight.Bold,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(game.accent.copy(alpha = .10f))
                    .border(1.dp, game.accent.copy(alpha = .45f), RoundedCornerShape(18.dp))
                    .semantics { contentDescription = "Swipe up, right, down, or left to choose the next safe path step" }
                    .pointerInput(question.answer) {
                        detectDragGestures(
                            onDragStart = { dragTotal = Offset.Zero },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragTotal += dragAmount
                            },
                            onDragEnd = {
                                val answer = if (kotlin.math.abs(dragTotal.x) > kotlin.math.abs(dragTotal.y)) {
                                    if (dragTotal.x > 0) "Right" else "Left"
                                } else {
                                    if (dragTotal.y > 0) "Down" else "Up"
                                }
                                onAnswer(answer)
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Swipe the safe step", color = game.accent, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Text("↑   →   ↓   ←", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f), fontSize = 28.sp)
                }
            }
            Text("Tap fallback", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f), fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                question.choices.forEach { choice ->
                    OutlinedButton(onClick = { onAnswer(choice) }, modifier = Modifier.weight(1f).height(48.dp), contentPadding = PaddingValues(0.dp)) {
                        Text(directionGlyph(choice), fontSize = 20.sp)
                    }
                }
            }
            AnimatedVisibility(feedback != null) { Text(feedback.orEmpty(), color = game.accent) }
        }
    }
}

@Composable
private fun RoundProgressStars(completed: Int, total: Int, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "$completed of $total rounds complete" },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val active = index < completed
            Text(
                text = if (active) "★" else "☆",
                color = if (active) accent else MaterialTheme.colorScheme.onBackground.copy(alpha = .34f),
                fontSize = if (active) 24.sp else 21.sp,
                modifier = Modifier.padding(horizontal = 3.dp),
            )
        }
    }
}

@Composable
private fun LevelWideCard(level: LevelId, xp: Int, onClick: () -> Unit) {
    AccentCard(level.accent, Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Emblem(level)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(level.title, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text("${level.audience} · ${IqCatalog.levelGames(level).size} games")
                LinearProgressIndicator(progress = { (xp / (level.minXp + 900f)).coerceIn(0f, 1f) }, modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(7.dp).clip(CircleShape), color = level.accent)
            }
                Text(if (UnlockSystem.isLevelUnlocked(level, xp, explorerCompleted = 5)) "✓" else "⌁", color = level.accent, fontSize = 28.sp)
        }
    }
}

@Composable
private fun GameRow(game: GameDefinition, favorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    AccentCard(game.accent, Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(game.icon, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(game.accent.copy(.16f)).semantics { contentDescription = "${game.name} icon" }.padding(10.dp), fontSize = 24.sp, color = game.accent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(game.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${game.level.title} · ${game.category.label} · ${game.durationMinutes} min", color = MaterialTheme.colorScheme.onSurface.copy(.66f), maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 6.dp)) {
                    game.tags.take(3).forEach { AssistChip(onClick = {}, label = { Text(it, fontSize = 11.sp) }) }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(if (game.status == ImplementationStatus.Playable) "Play" else "Later", color = game.accent, fontWeight = FontWeight.Bold)
                TextButton(onClick = onFavorite) { Text(if (favorite) "★" else "☆", fontSize = 22.sp) }
            }
        }
    }
}

@Composable
private fun GameGridCard(game: GameDefinition, favorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    AccentCard(game.accent, Modifier.clickable(onClick = onClick)) {
        Column(Modifier.height(206.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    game.icon,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(game.accent.copy(.18f)).padding(11.dp),
                    fontSize = 25.sp,
                    color = game.accent,
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onFavorite) { Text(if (favorite) "★" else "♡", fontSize = 22.sp) }
            }
            Column {
                Text(game.name, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${game.level.title} · ${game.durationMinutes} min", color = MaterialTheme.colorScheme.onSurface.copy(.7f), maxLines = 1)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                game.tags.take(2).forEach { tag ->
                    Text(tag, fontSize = 10.sp, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(game.accent.copy(.14f)).padding(horizontal = 7.dp, vertical = 4.dp), color = game.accent)
                }
            }
            Text(if (game.status == ImplementationStatus.Playable) "Best: play now" else "Later phase", color = game.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun <T> ChipRow(values: List<T>, selected: T?, onClick: (T) -> Unit, label: (T) -> String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        values.take(5).forEach { value -> FilterChip(selected = selected == value, onClick = { onClick(value) }, label = { Text(label(value), maxLines = 1) }) }
    }
}

@Composable
private fun DetailGrid(game: GameDefinition, best: Int?) {
    LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(214.dp), userScrollEnabled = false, horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { StatTile("Best", best?.toString() ?: "None") }
        item { StatTile("XP", game.xpReward.toString()) }
        item { StatTile("Difficulty", "${game.difficulty}/6") }
        item { StatTile("Age", "${game.minAge}+") }
    }
}

@Composable
private fun HeroCard(onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    AccentCard(MaterialTheme.colorScheme.primary, if (onClick == null) Modifier else Modifier.clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically, content = content)
    }
}

@Composable
private fun AccentCard(accent: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().border(1.dp, accent.copy(.35f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(.88f)),
        shape = RoundedCornerShape(18.dp),
    ) { Box(Modifier.background(Brush.linearGradient(listOf(accent.copy(.13f), Color.Transparent))).padding(16.dp)) { content() } }
}

@Composable
private fun MiniGameCard(game: GameDefinition, modifier: Modifier, onClick: () -> Unit) {
    AccentCard(game.accent, modifier.clickable(onClick = onClick)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.aspectRatio(.76f)) {
            Text(game.icon, fontSize = 32.sp, color = game.accent)
            Text(game.name, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(game.category.label, color = MaterialTheme.colorScheme.onSurface.copy(.65f))
        }
    }
}

@Composable
private fun BrainOrb(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawCircle(Brush.radialGradient(listOf(Color.Cyan.copy(.8f), Color(0xFF633CFF).copy(.35f), Color.Transparent)), radius = size.minDimension / 2)
        drawCircle(Color.White.copy(.55f), radius = size.minDimension * .43f, style = Stroke(2.dp.toPx()))
        repeat(9) { i ->
            val y = size.height * (.25f + i * .055f)
            drawLine(Color.White.copy(.38f), Offset(size.width * .28f, y), Offset(size.width * .72f, y + ((i % 3) - 1) * 9f), strokeWidth = 3.dp.toPx())
        }
    }
}

@Composable private fun Dot(active: Boolean) = Box(Modifier.padding(5.dp).size(if (active) 12.dp else 8.dp).clip(CircleShape).background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(.35f)))

@Composable
private fun Emblem(level: LevelId) = Box(Modifier.size(62.dp).clip(RoundedCornerShape(18.dp)).background(level.accent.copy(.16f)).border(1.dp, level.accent, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
    Text(level.title.take(1), color = level.accent, fontSize = 31.sp, fontWeight = FontWeight.Black)
}

@Composable private fun StatPill(label: String, value: String) = Column(Modifier.clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface.copy(.9f)).padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.Black); Text(label, fontSize = 11.sp) }
@Composable private fun StatTile(label: String, value: String) = AccentCard(MaterialTheme.colorScheme.primary) { Column { Text(label, color = MaterialTheme.colorScheme.onSurface.copy(.65f)); Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black) } }
@Composable private fun EmptyState(text: String) = AccentCard(MaterialTheme.colorScheme.primary) { Text(text, modifier = Modifier.padding(20.dp), fontWeight = FontWeight.Bold) }
@Composable private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) = AccentCard(MaterialTheme.colorScheme.primary) { Row(verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f), fontWeight = FontWeight.Bold); Switch(checked, onChange) } }
@Composable private fun LevelProgress(level: LevelId, xp: Int) = AccentCard(level.accent) { Column { Text(level.title, fontWeight = FontWeight.Bold); LinearProgressIndicator(progress = { (xp / (level.minXp + 1000f)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = level.accent) } }
@Composable private fun AchievementRow(title: String, done: Boolean, progress: Int, target: Int) = AccentCard(if (done) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline) { Column { Row { Text(if (done) "✓" else "○", Modifier.width(32.dp)); Text(title, fontWeight = FontWeight.Bold) }; LinearProgressIndicator(progress = { if (target == 0) 0f else progress / target.toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)) } }

private fun performanceMessage(result: GameResult) = when {
    result.accuracy >= 90 -> "Excellent accuracy. Your next run can chase speed."
    result.accuracy >= 70 -> "Solid control. A cleaner combo will lift the score."
    else -> "Good practice round. Slow down first, then speed up."
}

private fun roundFeedback(game: GameDefinition, correct: Boolean, detail: String): String = when {
    correct && game.level == LevelId.Explorer -> "Great job. $detail"
    correct -> "Correct. $detail"
    game.level == LevelId.Explorer -> "Try again next round. $detail"
    else -> "Not quite. $detail"
}

private fun adaptiveHintFor(game: GameDefinition, question: Question, consecutiveIncorrect: Int): String? {
    if (game.level != LevelId.Explorer || consecutiveIncorrect < 2) return null
    return when {
        game.id == "maze-scout" -> "Hint: follow the path from left to right, then swipe the missing final direction."
        question.choices.size == 2 -> "Hint: compare only the current rule with the two answer buttons."
        game.category == Category.Math -> "Hint: count slowly once, then check the answer choices."
        game.category == Category.Memory -> "Hint: say the pattern quietly in order before choosing."
        game.category == Category.Reading -> "Hint: look back at the sentence and find the matching word."
        game.category == Category.Spatial -> "Hint: match the shape or direction first, then ignore extra details."
        else -> "Hint: focus on the target word in the question before you choose."
    }
}

private fun directionGlyph(direction: String): String = when (direction) {
    "Up" -> "↑"
    "Right" -> "→"
    "Down" -> "↓"
    "Left" -> "←"
    else -> direction
}

@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    IQLabsTheme { HomeScreen(740, 3, 5, {}, {}) }
}
