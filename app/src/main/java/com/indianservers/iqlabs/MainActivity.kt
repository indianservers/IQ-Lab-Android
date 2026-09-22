package com.indianservers.iqlabs

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.game.AdaptiveDifficultyEngine
import com.indianservers.iqlabs.game.DifficultyMode
import com.indianservers.iqlabs.game.GameResult
import com.indianservers.iqlabs.game.LevelBlueprint
import com.indianservers.iqlabs.game.MasterySystem
import com.indianservers.iqlabs.game.Question
import com.indianservers.iqlabs.game.QuestionFactory
import com.indianservers.iqlabs.game.ScoreEngine
import com.indianservers.iqlabs.game.SessionMetric
import com.indianservers.iqlabs.game.WebNativePack
import com.indianservers.iqlabs.model.Category
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.ImplementationStatus
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import com.indianservers.iqlabs.ui.Tab
import com.indianservers.iqlabs.ui.components.GameArt
import com.indianservers.iqlabs.ui.components.GameRowCard
import com.indianservers.iqlabs.ui.components.IqBottomBar
import com.indianservers.iqlabs.ui.components.IqPage
import com.indianservers.iqlabs.ui.play.PlayRound
import com.indianservers.iqlabs.ui.components.IqProgressBar
import com.indianservers.iqlabs.ui.components.SoftCard
import com.indianservers.iqlabs.ui.levelAccent
import com.indianservers.iqlabs.ui.screens.CatalogueScreen
import com.indianservers.iqlabs.ui.screens.HomeScreen
import com.indianservers.iqlabs.ui.screens.OnboardingScreen
import com.indianservers.iqlabs.ui.screens.ProgressScreen
import com.indianservers.iqlabs.ui.screens.SettingsScreen
import com.indianservers.iqlabs.ui.theme.AppColorTheme
import com.indianservers.iqlabs.ui.theme.IQLabsTheme
import com.indianservers.iqlabs.ui.theme.IqPurple
import com.indianservers.iqlabs.ui.theme.LocalIqExtras
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { IQLabsTheme { IqLabApp() } }
    }
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
    var music: Boolean
        get() = prefs.getBoolean("music", true)
        set(value) = prefs.edit().putBoolean("music", value).apply()
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
    fun last(id: String): Int? = prefs.getInt("last_$id", -1).takeIf { it >= 0 }
    fun saveScore(id: String, score: Int) {
        val editor = prefs.edit().putInt("last_$id", score)
        if (score > (best(id) ?: -1)) editor.putInt("best_$id", score)
        editor.apply()
    }
    fun resetScore(id: String) = prefs.edit().remove("best_$id").remove("last_$id").apply()
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
    var music by rememberSaveable { mutableStateOf(store.music) }
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
                music = music,
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
                onMusic = { music = it; store.music = it },
                onHaptics = { haptics = it; store.haptics = it },
                onReducedMotion = { reducedMotion = it; store.reducedMotion = it },
                onDifficultyMode = { difficultyMode = it; store.difficultyMode = it },
                onAppColorTheme = { appColorTheme = it; store.appColorTheme = it },
            )
            is Screen.LevelDetails -> LevelDetailsScreen(current.level, xp, { go(Screen.Main(Tab.Games)) }, { go(Screen.GameDetails(it)) })
            is Screen.GameDetails -> GameDetailsScreen(
                game = IqCatalog.game(current.id),
                best = store.best(current.id),
                last = store.last(current.id),
                difficultyMode = difficultyMode,
                onDifficultyMode = { difficultyMode = it; store.difficultyMode = it },
                onResetScore = { store.resetScore(current.id) },
                onBack = { go(Screen.Main(Tab.Games)) },
                onPlay = { go(Screen.Play(current.id, sessions + 7)) },
            )
            is Screen.Play -> PlayScreen(
                game = IqCatalog.game(current.id),
                seed = current.seed,
                difficultyMode = difficultyMode,
                bestScore = store.best(current.id),
                sound = sound,
                onSound = { sound = it; store.sound = it },
                onExit = { go(Screen.GameDetails(current.id)) },
                onComplete = { result ->
                    val personalBest = ScoreEngine.isPersonalBest(result.score, store.best(current.id))
                    val stars = MasterySystem.starsFor(IqCatalog.game(current.id), result.score, result.accuracy)
                    val saved = result.copy(personalBest = personalBest, stars = stars)
                    store.xp += saved.xp
                    store.streak = maxOf(1, store.streak)
                    store.addSession()
                    store.addMetric(SessionMetric(current.id, LocalDate.now(), IqCatalog.game(current.id).level, IqCatalog.game(current.id).category, IqCatalog.game(current.id).difficulty, saved.score, saved.accuracy, saved.averageResponseMs, saved.xp, saved.stars))
                    xp = store.xp
                    streak = store.streak
                    sessions = store.sessions()
                    metrics = store.metrics()
                    go(Screen.Results(current.id, saved))
                }
            )
            is Screen.Results -> ResultsScreen(
                game = IqCatalog.game(current.id),
                result = current.result,
                onSaveScore = { store.saveScore(current.id, current.result.score) },
                onResetScore = { store.resetScore(current.id) },
                onRetry = { go(Screen.Play(current.id, sessions + 11)) },
                onCatalogue = { go(Screen.Main(Tab.Games)) },
            )
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
    music: Boolean,
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
    onMusic: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onReducedMotion: (Boolean) -> Unit,
    onDifficultyMode: (DifficultyMode) -> Unit,
    onAppColorTheme: (AppColorTheme) -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { IqBottomBar(tab, onTab) },
    ) { padding ->
        IqPage {
            Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.Home -> HomeScreen(xp, streak, sessions, onLevel, onGame, onSeeAll = { onTab(Tab.Games) }, onOpenSettings = { onTab(Tab.Settings) })
                Tab.Games -> CatalogueScreen(favorites, onGame, onFavorite)
                Tab.Progress -> ProgressScreen(xp, streak, sessions, metrics)
                Tab.Settings -> SettingsScreen(
                    xp = xp,
                    sound = sound,
                    haptics = haptics,
                    reducedMotion = reducedMotion,
                    music = music,
                    difficultyMode = difficultyMode,
                    appColorTheme = appColorTheme,
                    onSound = onSound,
                    onHaptics = onHaptics,
                    onReducedMotion = onReducedMotion,
                    onMusic = onMusic,
                    onDifficultyMode = onDifficultyMode,
                    onAppColorTheme = onAppColorTheme,
                    onReplayOnboarding = onReplayOnboarding,
                    onReset = onReset,
                )
            }
            }
        }
    }
}

@Composable
private fun LevelDetailsScreen(level: LevelId, xp: Int, onBack: () -> Unit, onGame: (String) -> Unit) {
    BackHandler(onBack = onBack)
    val extras = LocalIqExtras.current
    IqPage {
        LazyColumn(
            modifier = Modifier.statusBarsPadding(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                TextButton(onClick = onBack) { Text("Back") }
                SoftCard(modifier = Modifier.fillMaxWidth(), color = extras.card) {
                    Text(level.title, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = levelAccent(level))
                    Text("${level.audience} · ${level.tagline}", color = extras.muted)
                    Spacer(Modifier.height(12.dp))
                    IqProgressBar((xp.toFloat() / (level.minXp + 1000)).coerceIn(0f, 1f), levelAccent(level), Modifier.fillMaxWidth())
                    Text("${IqCatalog.levelGames(level).size} games · unlock: ${if (xp >= level.minXp) "ready" else "${level.minXp - xp} XP to go"}")
                }
            }
            items(IqCatalog.levelGames(level)) { GameRowCard(it, onClick = { onGame(it.id) }) }
        }
    }
}

@Composable
private fun GameDetailsScreen(
    game: GameDefinition,
    best: Int?,
    last: Int?,
    difficultyMode: DifficultyMode,
    onDifficultyMode: (DifficultyMode) -> Unit,
    onResetScore: () -> Unit,
    onBack: () -> Unit,
    onPlay: () -> Unit,
) {
    val extras = LocalIqExtras.current
    val profile = remember(game.id, difficultyMode) { AdaptiveDifficultyEngine.initialProfile(game, difficultyMode) }
    val parameters = AdaptiveDifficultyEngine.parametersFor(game, profile)
    val blueprintStages = remember(game.id) { LevelBlueprint.stagesForCatalogGame(game.id) }
    val enhancements = remember(game.id) { WebNativePack.enhancementsFor(game) }
    val maxLevel = LevelBlueprint.maxLevelFor(game.id)
    var shownBest by remember(game.id, best) { mutableStateOf(best) }
    var shownLast by remember(game.id, last) { mutableStateOf(last) }
    var confirmReset by remember { mutableStateOf(false) }
    BackHandler(onBack = onBack)
    IqPage {
        LazyColumn(
            modifier = Modifier.statusBarsPadding(),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = onBack) { Text("Back") }
                    Button(
                        onClick = onPlay,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = game.accent, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text("Play", fontWeight = FontWeight.Black, fontSize = 18.sp) }
                }
                SoftCard(modifier = Modifier.fillMaxWidth(), padding = 0.dp) {
                    GameArt(game, Modifier.fillMaxWidth().height(104.dp))
                    Column(Modifier.padding(14.dp)) {
                        Text(game.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                        Text("${game.level.title} · ${game.category.label} · ${game.durationMinutes} min", color = game.accent, fontWeight = FontWeight.Bold)
                        Text(game.description, color = extras.muted, maxLines = 2)
                    }
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Best ${shownBest?.toString() ?: "—"}", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text("Last ${shownLast?.toString() ?: "—"}", color = extras.muted)
                        }
                        OutlinedButton(onClick = { confirmReset = true }, enabled = shownBest != null || shownLast != null) {
                            Text("Reset score")
                        }
                    }
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("How to play", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    enhancements.forEachIndexed { index, feature ->
                        Text("${index + 1}. $feature", color = extras.muted, modifier = Modifier.padding(top = 3.dp))
                    }
                    Text("Score: accuracy + speed + combo", modifier = Modifier.padding(top = 6.dp), color = game.accent, fontWeight = FontWeight.Bold)
                }
            }
            item {
                SoftCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Difficulty · Tier ${parameters.tier}/$maxLevel", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DifficultyMode.entries.forEach { mode ->
                            FilterChip(selected = difficultyMode == mode, onClick = { onDifficultyMode(mode) }, label = { Text(mode.name) })
                        }
                    }
                    Text("${parameters.sequenceLength} items · ${parameters.distractors} distractors · ${parameters.responseWindowSeconds}s", color = extras.muted, fontSize = 12.sp)
                }
            }
            if (blueprintStages.isNotEmpty()) {
                item {
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        val stage = LevelBlueprint.stageFor(game.id, parameters.tier)
                        Text("Level ${stage?.level ?: parameters.tier} · ${stage?.stage ?: "Progression"}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(stage?.exactDesign ?: "Difficulty rises with your performance.", color = extras.muted)
                        Text("Mastery: ${stage?.masteryTarget ?: "80% accuracy"}", color = game.accent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset score?") },
            text = { Text("This clears the saved and best score for ${game.name} only.") },
            confirmButton = {
                TextButton(onClick = {
                    onResetScore()
                    shownBest = null
                    shownLast = null
                    confirmReset = false
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun PlayScreen(
    game: GameDefinition,
    seed: Int,
    difficultyMode: DifficultyMode,
    bestScore: Int?,
    sound: Boolean,
    onSound: (Boolean) -> Unit,
    onExit: () -> Unit,
    onComplete: (GameResult) -> Unit,
) {
    val extras = LocalIqExtras.current
    val profile = remember(game.id, difficultyMode) { AdaptiveDifficultyEngine.initialProfile(game, difficultyMode) }
    val parameters = remember(game.id, difficultyMode) { AdaptiveDifficultyEngine.parametersFor(game, profile) }
    val blueprintStage = remember(game.id, parameters.tier) { LevelBlueprint.stageFor(game.id, parameters.tier) }
    val questions = remember(game.id, seed, parameters.tier) { QuestionFactory.questionsFor(game, seed, 6, parameters.tier) }
    val maxLevel = LevelBlueprint.maxLevelFor(game.id)
    var started by rememberSaveable { mutableStateOf(true) }
    var paused by rememberSaveable { mutableStateOf(false) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var correct by rememberSaveable { mutableIntStateOf(0) }
    var incorrect by rememberSaveable { mutableIntStateOf(0) }
    var consecutiveIncorrect by rememberSaveable { mutableIntStateOf(0) }
    var combo by rememberSaveable { mutableIntStateOf(0) }
    var bestCombo by rememberSaveable { mutableIntStateOf(0) }
    var totalResponseMs by rememberSaveable { mutableIntStateOf(0) }
    var roundStartedAt by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var answerLocked by rememberSaveable { mutableStateOf(false) }
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val roundScope = rememberCoroutineScope()

    BackHandler(onBack = { if (started && index < questions.size) confirmExit = true else onExit() })
    IqPage {
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = { if (started) confirmExit = true else onExit() }) { Text("Exit") }
                Column(Modifier.weight(1f)) {
                    Text(game.name, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1)
                    Text("${game.level.title} · L${parameters.tier}", color = extras.muted, fontSize = 11.sp)
                }
                Text("${ScoreEngine.score(correct, incorrect, bestCombo, parameters.tier, 12)}", color = game.accent, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            IqProgressBar((index + 1) / questions.size.toFloat(), game.accent, Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Round ${index + 1}/${questions.size} · Combo $combo", color = extras.muted, fontSize = 11.sp)
                Text("Best ${bestScore ?: "—"}", color = extras.muted, fontSize = 11.sp)
            }
            if (!started) {
                SoftCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Text("Session Brief", fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text(game.instructions, color = extras.muted)
                    Text("${difficultyMode.name} mode · tier ${parameters.tier}/$maxLevel · ${profile.lastReason}")
                    if (blueprintStage != null) {
                        Text("Blueprint L${blueprintStage.level} ${blueprintStage.stage}: ${blueprintStage.exactDesign}")
                        Text("Mastery: ${blueprintStage.masteryTarget}", color = game.accent)
                    }
                    Text("Six playful rounds · tap, swipe or count · pause anytime.", color = extras.muted)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { started = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Start Countdown") }
                }
            } else if (paused) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    SoftCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Paused", fontWeight = FontWeight.Black, fontSize = 28.sp)
                        Text("Your round is waiting.", color = extras.muted)
                        Button(onClick = { paused = false }, modifier = Modifier.fillMaxWidth()) { Text("Resume") }
                    }
                }
            } else {
                val question = questions[index]
                val adaptiveHint = adaptiveHintFor(game, question, consecutiveIncorrect)
                val answerHandler: (String) -> Unit = { answer ->
                    if (!answerLocked) {
                        answerLocked = true
                        val responseMs = (System.currentTimeMillis() - roundStartedAt).toInt().coerceAtLeast(0)
                        val nextResponseTotal = totalResponseMs + responseMs
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
                        totalResponseMs = nextResponseTotal
                        feedback = roundFeedback(game, ok, question.detail)
                        roundScope.launch {
                            delay(650)
                            if (index == questions.lastIndex) {
                                val score = ScoreEngine.score(nextCorrect, nextIncorrect, nextBestCombo, parameters.tier, 12)
                                val earnedXp = ScoreEngine.xp(nextCorrect, nextIncorrect, parameters.tier, game.xpReward)
                                onComplete(GameResult(score, nextCorrect, nextIncorrect, earnedXp, nextBestCombo, false, averageResponseMs = nextResponseTotal / questions.size))
                            } else {
                                feedback = null
                                index++
                                roundStartedAt = System.currentTimeMillis()
                                answerLocked = false
                            }
                        }
                    }
                }
                PlayRound(game, question, feedback, adaptiveHint, answerHandler, Modifier.fillMaxWidth().weight(1f), sound = sound)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { paused = true }, modifier = Modifier.weight(1f).height(42.dp)) { Text("Pause") }
                    OutlinedButton(onClick = { onSound(!sound) }, modifier = Modifier.weight(1f).height(42.dp)) { Text(if (sound) "Sound on" else "Muted") }
                }
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
private fun ResultsScreen(
    game: GameDefinition,
    result: GameResult,
    onSaveScore: () -> Unit,
    onResetScore: () -> Unit,
    onRetry: () -> Unit,
    onCatalogue: () -> Unit,
) {
    val extras = LocalIqExtras.current
    var saved by rememberSaveable(game.id, result.score) { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    IqPage {
        Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            GameArt(game, Modifier.size(140.dp))
            Spacer(Modifier.height(16.dp))
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(if (result.personalBest) "Personal Best" else "Session Complete", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text(result.score.toString(), fontSize = 62.sp, fontWeight = FontWeight.Black, color = game.accent)
                    Text("★".repeat(result.stars).padEnd(3, '☆'), color = game.accent, fontSize = 30.sp)
                    Text("${result.accuracy}% accuracy · ${result.correct} correct · ${result.incorrect} missed", color = extras.muted)
                    Text("Average response ${result.averageResponseMs} ms", color = extras.muted)
                    Text("+${result.xp} XP · best combo ${result.bestCombo}", modifier = Modifier.padding(top = 6.dp), fontWeight = FontWeight.Bold)
                    Text(performanceMessage(result), modifier = Modifier.padding(top = 14.dp), color = extras.muted)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onSaveScore(); saved = true },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = game.accent),
                ) { Text(if (saved) "Score Saved" else "Save Score", color = Color.White, fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.weight(1f).height(50.dp)) { Text("Reset Score") }
            }
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) { Text("Retry") }
            TextButton(onClick = onCatalogue, modifier = Modifier.fillMaxWidth()) { Text("Return to Catalogue") }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset saved score?") },
            text = { Text("This clears saved scores for ${game.name}.") },
            confirmButton = { TextButton(onClick = { onResetScore(); saved = false; confirmReset = false }) { Text("Reset") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
        )
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
private fun DetailGrid(game: GameDefinition, best: Int?) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(168.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { StatTile("Best", best?.toString() ?: "None") }
        item { StatTile("XP", game.xpReward.toString()) }
        item { StatTile("Difficulty", "${game.difficulty}/6") }
        item { StatTile("Age", "${game.minAge}+") }
    }
}

@Composable
private fun StatTile(label: String, value: String) {
    SoftCard(modifier = Modifier.fillMaxWidth(), padding = 14.dp, radius = 18.dp, shadow = 6.dp) {
        Text(label, color = LocalIqExtras.current.muted)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

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
        game.id == "maze-scout" -> "Hint: follow the path, then swipe the missing final direction."
        question.choices.size == 2 -> "Hint: react to the live rule with the big left or right pad."
        game.category == Category.Math -> "Hint: count slowly, then lock the number on the pad."
        game.category == Category.Memory -> "Hint: say the pattern quietly, then tap it back."
        game.category == Category.Reading -> "Hint: look back at the sentence and find the matching word."
        game.category == Category.Spatial -> "Hint: match the shape or direction, then tap or swipe."
        else -> "Hint: tap the piece that fits the live rule."
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    IQLabsTheme { HomeScreen(740, 3, 5, {}, {}) }
}
