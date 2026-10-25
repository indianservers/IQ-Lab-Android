package com.indianservers.iqlabs.puzzle

import com.indianservers.iqlabs.ui.components.rememberQuickPraise
import com.indianservers.iqlabs.ui.components.SpokenQuickPraise
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

private val ink = Color(0xFF13294B)
private val palette = listOf(0xFFEF5675, 0xFF248CE5, 0xFF17AF89, 0xFFF5AD29, 0xFF9664DA, 0xFF00A6B4, 0xFFDA7350, 0xFF7371CF, 0xFF7EAB22, 0xFFD237B5, 0xFF304E91, 0xFFA67A15).map { Color(it) }

@Composable
fun InfinitePuzzleScreen(kind: PuzzleKind, sound: Boolean, haptics: Boolean, reducedMotion: Boolean, onSound: (Boolean) -> Unit, onExit: () -> Unit, storage: PuzzleStore? = null, onSolved: (PuzzleSession) -> Unit = {}) {
    val context = LocalContext.current
    val view = LocalView.current
    DisposableEffect(view) {
        var owner: android.content.Context = view.context
        while (owner is android.content.ContextWrapper && owner !is android.app.Activity) owner = owner.baseContext
        val activity = owner as? android.app.Activity
        val controller = activity?.let { androidx.core.view.WindowCompat.getInsetsController(it.window, view) }
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = true
        onDispose { if (previous != null) controller?.isAppearanceLightStatusBars = previous }
    }
    val store = remember(storage) { storage ?: PuzzleStore(context) }
    val scope = rememberCoroutineScope()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var foreground by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> foreground = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    var session by remember { mutableStateOf<PuzzleSession?>(null) }
    var selection by rememberSaveable { mutableStateOf(true) }
    var loading by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(PuzzleMode.Normal) }
    var message by remember { mutableStateOf("Take your time. Find a clear way through.") }
    var settings by remember { mutableStateOf(false) }
    val preferences = remember { context.getSharedPreferences("iq_puzzle_settings", 0) }
    var symbols by remember { mutableStateOf(preferences.getBoolean("symbols", true)) }
    var grid by remember { mutableStateOf(preferences.getBoolean("grid", false)) }
    var spoken by remember { mutableStateOf(preferences.getBoolean("spoken", false)) }
    val tts = remember { android.speech.tts.TextToSpeech(context, null) }
    DisposableEffect(tts) { onDispose { tts.shutdown() } }
    var history by remember { mutableStateOf<List<PuzzleSession>>(emptyList()) }
    var highlight by remember { mutableStateOf<Int?>(null) }
    var preview by remember { mutableStateOf<List<Cell>>(emptyList()) }
    var animating by remember { mutableStateOf<Int?>(null) }
    var escapeJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val travel = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    val heartScale = remember { Animatable(1f) }
    LaunchedEffect(session?.mistakes) {
        if ((session?.mistakes ?: 0) > 0 && !reducedMotion) {
            heartScale.animateTo(1.16f, tween(80)); heartScale.animateTo(1f, tween(150))
            repeat(2) { shake.animateTo(8f, tween(45)); shake.animateTo(-8f, tween(45)) }
            shake.animateTo(0f, tween(45))
        }
    }
    val praise = rememberQuickPraise()
    LaunchedEffect(selection, session?.puzzle, foreground) { praise.resetWindow() }
    var page by remember { mutableLongStateOf(1L) }
    fun update(s: PuzzleSession) { session = s; store.save(s) }
    fun feedback(bad: Boolean = false) {
        if (haptics) view.performHapticFeedback(if (bad) android.view.HapticFeedbackConstants.LONG_PRESS else android.view.HapticFeedbackConstants.CLOCK_TICK)
        if (sound) view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
    }
    fun explain(text: String) { message = text; if (spoken) tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "hint") }
    fun load(level: Long, different: Boolean = false) {
        escapeJob?.cancel(); animating = null
        loading = true
        scope.launch {
            val old = withContext(Dispatchers.IO) { store.saved(kind, level) }
            val next = withContext(Dispatchers.Default) { runCatching {
                if (old != null && !different && !(kind==PuzzleKind.Arrows && old.puzzle.version<ArrowLevelGenerator.VERSION)) PuzzleSession(old.puzzle, mode) else store.fresh(kind, level, mode, if (different) InfiniteEngine.next(old?.puzzle?.variation ?: 0) else 0)
            }.getOrNull() }
            if (next == null) { loading = false; explain("A fresh puzzle could not be prepared. Please try again."); return@launch }
            update(next); history = emptyList(); highlight = null; preview = emptyList(); selection = false; loading = false
            message = if (kind == PuzzleKind.Arrows) "Tap a clear shape. Drag to pan; pinch or double-tap to zoom." else "Drag matching symbols. Two fingers zoom and pan."
        }
    }
    LaunchedEffect(kind) {
        session = withContext(Dispatchers.IO) { store.active(kind)?.copy(practice = false) }
        mode = session?.mode ?: PuzzleMode.Normal
        if(kind==PuzzleKind.Arrows && session?.puzzle?.version?.let { it<2 }==true && session?.complete==false) {
            val old=session!!
            session=withContext(Dispatchers.Default) { store.fresh(kind,old.puzzle.level,old.mode) }
            store.save(session!!)
        }
        // Preserve partly played boards. Untouched legacy level 3+ boards can
        // adopt the new curriculum immediately; old canonical geometry remains archived.
        session?.takeIf { kind==PuzzleKind.Arrows && it.puzzle.version<ArrowLevelGenerator.VERSION && it.puzzle.level>=3 && it.moves==0 && it.mistakes==0 }?.let { old->
            session=withContext(Dispatchers.Default) { store.fresh(kind,old.puzzle.level,old.mode) }
            store.save(session!!)
        }
        page = ((store.highest(kind) - 1) / 12) * 12 + 1
        loading = false
    }
    LaunchedEffect(selection, session?.complete, session?.lives, foreground) {
        while (foreground && !selection && session != null && session?.complete == false && session!!.lives > 0) {
            delay(1000); session?.let { update(it.copy(seconds = it.seconds + 1)) }
        }
    }
    LaunchedEffect(session?.complete, session?.puzzle) {
        session?.takeIf { it.complete }?.let { completed ->
            if (withContext(Dispatchers.IO) { store.claimReward(completed) }) onSolved(completed)
        }
    }
    BackHandler { if (!selection) selection = true else onExit() }
    fun fail(reason: String) {
        val s = session ?: return
        if (s.complete || s.lives <= 0) return
        update(s.copy(lives = if (s.mode == PuzzleMode.Relaxed) s.lives else (s.lives - 1).coerceAtLeast(0), mistakes = s.mistakes + 1))
        feedback(true); explain(reason)
    }
    fun remove(id: Int, byHint: Boolean = false) {
        val s = session ?: return
        if (animating != null || s.complete || s.lives <= 0) return
        val remaining = s.puzzle.paths.map { it.id }.toSet() - s.removed
        if (id !in InfiniteEngine.removable(s.puzzle, remaining)) {
            highlight=if(s.puzzle.version>=2) s.puzzle.arrowGeometry.blocking(id,remaining).firstOrNull() else null
            fail("The highlighted arrow blocks this shape's escape. Clear it first."); return
        }
        animating = id
        escapeJob = scope.launch {
            travel.snapTo(0f); travel.animateTo(1f, tween(if (reducedMotion) 1 else 650))
            // Read latest timer/hint state at commit; one animation owns the board.
            val current = session ?: s
            history = history + current
            update(current.copy(removed = current.removed + id, moves = current.moves + 1))
            animating = null; highlight = null; preview = emptyList(); feedback(); praise.success(byHint = byHint)
        }
    }
    fun hint(type: Int) {
        val s = session ?: return
        if (loading || animating != null || s.complete || s.lives <= 0) return
        loading = true; praise.usedHint()
        scope.launch {
            val result = withContext(Dispatchers.Default) {
                if (kind == PuzzleKind.Colours) InfiniteEngine.colourAdvice(s.puzzle, s.routes) else null
            }
            val current = session ?: s
            update(current.copy(hintsUsed = current.hintsUsed + 1, practice = false))
            loading = false
            if (kind == PuzzleKind.Arrows) {
                val id = InfiniteEngine.removable(s.puzzle, s.puzzle.paths.map { it.id }.toSet() - s.removed).first()
                highlight = id
                explain("The highlighted arrow has a clear exit.")
                if (type == 2) preview = InfiniteEngine.escape(s.puzzle.paths.first { it.id == id }, s.puzzle.size)
                if (type == 3) remove(id, byHint = true)
            } else if (result != null && result.revise.isNotEmpty()) {
                val id = result.revise.first()
                highlight = id
                explain("Revise connection ${id + 1}. A verified continuation requires ${result.revise.size} connection(s) to be redrawn. Tap the line to erase it, or use Undo.")
            } else if (result != null) {
                val route = result.solution.entries.firstOrNull { it.key !in s.routes }
                if (route != null) {
                    highlight = route.key; explain("Connect the highlighted matching pair.")
                    if (type == 2) preview = route.value.take(maxOf(2, route.value.size / 2))
                    if (type == 3) { history = history + session!!; update(session!!.copy(routes = session!!.routes + (route.key to route.value), moves = session!!.moves + 1)); preview = emptyList(); feedback() }
                }
            }
        }
    }
    Surface(color = if(kind==PuzzleKind.Arrows) Color(0xFFF8F1E3) else Color(0xFFFFFBF3), modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (selection) onExit() else selection = true }) { Text("‹ Back") }
                Column(Modifier.weight(1f)) {
                    Text("IQ LABS  /  INFINITE", fontSize = 10.sp, letterSpacing = 2.sp, color = Color(0xFF72839B))
                    Text(kind.title, fontSize = 25.sp, fontWeight = FontWeight.Black, color = ink)
                }
                com.indianservers.iqlabs.ui.components.PlayUtilityButtons(sound, onSound, onSettings = { settings = true })
            }
            if (selection) {
                Text(if (kind == PuzzleKind.Arrows) "A little foresight. A clear escape." else "Find your flow. Connect every colour.", color = ink)
                PuzzleHero(kind, Modifier.fillMaxWidth().height(130.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PuzzleMode.entries.forEach { m -> FilterChip(selected = mode == m, onClick = { mode = m }, label = { Text(m.name, fontSize = 12.sp) }) }
                }
                Text("${store.highest(kind)} levels unlocked · no final level", color = Color(0xFF68788C))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(4) { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) { col ->
                                val delta = row * 3L + col
                                val n = if (page > Long.MAX_VALUE - delta) Long.MAX_VALUE else page + delta
                                OutlinedButton(onClick = { load(n) }, enabled = n <= store.highest(kind) && !loading, modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(18.dp)) { Text("$n" + if (store.bestStars(kind,n) > 0) " · " + "★".repeat(store.bestStars(kind,n)) else "") }
                            }
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { page = (page - 12).coerceAtLeast(1) }, enabled = page > 1) { Text("Previous") }
                    TextButton(onClick = { page = if (page <= Long.MAX_VALUE - 12) page + 12 else page }, enabled = page <= store.highest(kind) - 12) { Text("More levels") }
                }
                Button(onClick = { if (session != null && session!!.mode == mode) selection = false else load(store.highest(kind)) }, enabled = !loading, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text(if (session != null) "Continue · Level ${session!!.puzzle.level}" else "Begin your journey") }
            } else {
                val s = session
                if (s == null) { CircularProgressIndicator(); return@Column }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Level ${s.puzzle.level} · ${s.puzzle.tier}", modifier = Modifier.weight(1f), maxLines = 2, fontWeight = FontWeight.Bold, color = ink)
                    com.indianservers.iqlabs.ui.components.PlayAssistButtons(s.lives,
                        onLives = { message = "Normal: 5 lives. Challenge: 3. Relaxed: unlimited. Blocked moves cost one life." },
                        onHint = { hint(2) }, unlimited = s.mode == PuzzleMode.Relaxed,
                        hintEnabled = !loading && animating == null && !s.complete && s.lives > 0)
                }
                val done = if (kind == PuzzleKind.Arrows) s.removed.size else s.routes.size
                LinearProgressIndicator(progress = { done.toFloat() / s.puzzle.paths.size }, modifier = Modifier.fillMaxWidth(), color = palette[2])
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$done / ${s.puzzle.paths.size} ${if (kind == PuzzleKind.Arrows) "cleared" else "connected"}", color = ink)
                    Text("${s.seconds / 60}:${(s.seconds % 60).toString().padStart(2, '0')}  ·  ${s.mode.name}", color = ink)
                }
                SpokenQuickPraise(praise, sound, active = !selection && !settings)
                Box(Modifier.weight(1f).fillMaxWidth().graphicsLayer(translationX = shake.value).background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF3EFE5))), RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center) {
                    if(kind==PuzzleKind.Arrows && s.puzzle.version>=2) {
                        ArrowBoard(s,Modifier.fillMaxSize(),highlight,animating,travel.value,!loading && !s.complete && s.lives>0 && animating==null,
                            preview=preview.isNotEmpty(),onArrow={ remove(it) },onCamera={ camera -> session?.let { update(it.copy(cameraX=camera.x,cameraY=camera.y,cameraZoom=camera.zoom)) } })
                    } else {
                    var zoom by remember(s.puzzle) { mutableFloatStateOf(1f) }
                    var pan by remember(s.puzzle) { mutableStateOf(Offset.Zero) }
                    Box(Modifier.fillMaxWidth().aspectRatio(1f).clipToBounds().pointerInput(s.puzzle) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.count { it.pressed } >= 2) {
                                    zoom = (zoom * event.calculateZoom()).coerceIn(1f, 4f)
                                    val limit = size.width * (zoom - 1) / 2f
                                    val proposed = pan + event.calculatePan()
                                    pan = Offset(proposed.x.coerceIn(-limit,limit), proposed.y.coerceIn(-limit,limit))
                                    event.changes.forEach { it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }) {
                    PuzzleBoard(s, Modifier.fillMaxWidth().aspectRatio(1f).graphicsLayer(scaleX = zoom, scaleY = zoom, translationX = pan.x, translationY = pan.y).padding(12.dp), highlight, preview, animating, travel.value, grid, symbols, !loading && !s.complete && s.lives > 0 && animating == null,
                        onArrow = { remove(it) }, onInvalid = ::fail,
                        onRoute = { id, route -> history = history + session!!; update(session!!.copy(routes = session!!.routes + (id to route), moves = session!!.moves + 1)); highlight = null; preview = emptyList(); feedback(); praise.success() },
                        onErase = { id -> history = history + session!!; update(session!!.copy(routes = session!!.routes - id)) })
                    }
                    }
                    if (loading) CircularProgressIndicator()
                }
                Text(message, fontSize = 13.sp, color = Color(0xFF62718A), minLines = 2)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { if (history.isNotEmpty() && animating == null) { val previous = history.last(); val now = session!!; update(previous.copy(lives = now.lives, mistakes = now.mistakes, hints = now.hints, hintsUsed = now.hintsUsed, practice = now.practice, seconds = now.seconds, cameraX=now.cameraX,cameraY=now.cameraY,cameraZoom=now.cameraZoom)); history = history.dropLast(1); highlight = null; preview = emptyList() } }, enabled = history.isNotEmpty() && !loading && !s.complete && s.lives > 0) { Text("↶ Undo") }
                    TextButton(onClick = { update(PuzzleSession(s.puzzle, s.mode, cameraX=s.cameraX,cameraY=s.cameraY,cameraZoom=s.cameraZoom)); history = emptyList(); highlight = null; preview = emptyList() }, enabled = animating == null && !loading) { Text("↻ Retry") }
                    TextButton(onClick = { hint(1) }, enabled = !loading && !s.complete && s.lives > 0) { Text("✦ Hint") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    TextButton(onClick = { hint(2) }, enabled = !loading && !s.complete && s.lives > 0) { Text("Preview next move", fontSize = 11.sp) }
                    TextButton(onClick = { hint(3) }, enabled = !loading && !s.complete && s.lives > 0) { Text("Show one move", fontSize = 11.sp) }
                }
                if (s.complete || s.lives == 0) {
                    AlertDialog(onDismissRequest = {}, title = { Text(if (s.complete) "Beautifully solved!" else "A fresh perspective?") }, text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (s.complete) Text("★".repeat(s.stars) + "☆".repeat(3-s.stars), color = palette[3], fontSize = 38.sp)
                            Text("${s.moves} moves · ${s.mistakes} mistakes\n${s.hintsUsed} hints · ${s.seconds}s${if (s.practice) " · Practice" else ""}")
                            if (!s.complete) TextButton(onClick = { load(s.puzzle.level, true) }) { Text("Different puzzle, same tier") }
                            TextButton(onClick = { selection = true }) { Text("Games / level selection") }
                        }
                    }, confirmButton = { Button(onClick = { if (s.complete) load(InfiniteEngine.next(s.puzzle.level)) else { update(PuzzleSession(s.puzzle, s.mode, cameraX=s.cameraX,cameraY=s.cameraY,cameraZoom=s.cameraZoom)); history = emptyList(); highlight = null; preview = emptyList() } }) { Text(if (s.complete) "Next Infinite Level" else "Retry") } }, dismissButton = { if (s.complete) TextButton(onClick = { update(PuzzleSession(s.puzzle, s.mode, cameraX=s.cameraX,cameraY=s.cameraY,cameraZoom=s.cameraZoom)); history = emptyList(); highlight = null; preview = emptyList() }) { Text("Replay") } })
                }
            }
        }
    }
    if (settings) AlertDialog(onDismissRequest = { settings = false }, title = { Text("Your play space") }, text = {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Sound", Modifier.weight(1f)); Switch(sound, onSound) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Routing dots", Modifier.weight(1f)); Switch(grid, { grid = it; preferences.edit().putBoolean("grid", it).apply() }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Colour symbols", Modifier.weight(1f)); Switch(symbols, { symbols = it; preferences.edit().putBoolean("symbols", it).apply() }) }
            Row(verticalAlignment = Alignment.CenterVertically) { Text("Spoken hints", Modifier.weight(1f)); Switch(spoken, { spoken = it; preferences.edit().putBoolean("spoken", it).apply() }) }
            Text("Normal: 5 lives. Challenge: 3. Relaxed: unlimited. Hints are always available and never cost lives.", fontSize = 12.sp)
        }
    }, confirmButton = { TextButton(onClick = { settings = false }) { Text("Done") } })
}

@Composable
fun PuzzleHero(kind: PuzzleKind, modifier: Modifier) {
    Canvas(modifier) {
        val colours = palette.take(3)
        colours.forEachIndexed { i, c ->
            val x = size.width * (.25f + i * .24f); val y = size.height * .3f
            val path = Path().apply { moveTo(x, y); cubicTo(x - 55, y + 50, x + 60, y + 65, x, y + 85) }
            drawPath(path, c.copy(alpha = .12f), style = Stroke(27f, cap = StrokeCap.Round))
            drawPath(path, c, style = Stroke(13f, cap = StrokeCap.Round))
            if (kind == PuzzleKind.Colours) { drawCircle(c, 16f, Offset(x,y)); drawCircle(c,16f,Offset(x,y+85)) }
            else { drawLine(c,Offset(x,y+85),Offset(x-14,y+69),13f,StrokeCap.Round); drawLine(c,Offset(x,y+85),Offset(x+17,y+73),13f,StrokeCap.Round) }
        }
    }
}

@Composable
private fun PuzzleBoard(s: PuzzleSession, modifier: Modifier, highlight: Int?, preview: List<Cell>, animating: Int?, travel: Float, grid: Boolean, symbols: Boolean, enabled: Boolean, onArrow: (Int) -> Unit, onInvalid: (String) -> Unit, onRoute: (Int,List<Cell>) -> Unit, onErase: (Int) -> Unit) {
    val p = s.puzzle
    val numberPaint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE; textAlign = android.graphics.Paint.Align.CENTER; isFakeBoldText = true } }
    var touchStart by remember(p) { mutableStateOf(Offset.Zero) }
    var drawing by remember(p) { mutableStateOf<List<Cell>>(emptyList()) }
    var pair by remember(p) { mutableStateOf<Int?>(null) }
    var invalid by remember(p) { mutableStateOf(false) }
    val latest by rememberUpdatedState(s)
    val routeCallback by rememberUpdatedState(onRoute)
    val invalidCallback by rememberUpdatedState(onInvalid)
    val eraseCallback by rememberUpdatedState(onErase)
    val currentEnabled by rememberUpdatedState(enabled)
    val tapCallback by rememberUpdatedState(onArrow)
    Canvas(modifier.semantics { contentDescription = "Puzzle board" }.pointerInput(p) {
        awaitEachGesture {
            touchStart = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial).position
            do { val event = awaitPointerEvent(PointerEventPass.Initial) } while (event.changes.any { it.pressed })
        }
    }.pointerInput(p) {
        if (p.kind == PuzzleKind.Colours) detectTapGestures { o ->
            if (currentEnabled) {
                val step = size.width.toFloat() / (p.size + 1)
                val q = Cell((o.x / step - 1).roundToInt(), (o.y / step - 1).roundToInt())
                latest.routes.entries.firstOrNull { q in it.value }?.let { eraseCallback(it.key) }
            }
        }
    }.pointerInput(p) {
        fun cell(o: Offset): Cell { val step = size.width.toFloat() / (p.size + 1); return Cell((o.x / step - 1).roundToInt(), (o.y / step - 1).roundToInt()) }
        if (p.kind == PuzzleKind.Arrows) detectTapGestures { o ->
            if (currentEnabled) {
                val step = size.width.toFloat() / (p.size+1)
                val q = Offset(o.x / step-1, o.y / step-1)
                fun distance(a: Cell, b: Cell): Float {
                    val v = Offset((b.x-a.x).toFloat(), (b.y-a.y).toFloat()); val w = q - Offset(a.x.toFloat(),a.y.toFloat())
                    val t = ((w.x*v.x+w.y*v.y)/(v.x*v.x+v.y*v.y)).coerceIn(0f,1f)
                    return (w-v*t).getDistance()
                }
                p.paths.filter { it.id !in latest.removed }.map { a -> a.id to a.cells.zipWithNext().minOf { (u,v) -> distance(u,v) } }.minByOrNull { it.second }?.takeIf { it.second < .45f }?.let { tapCallback(it.first) }
            }
        } else detectDragGestures(onDragStart = { o ->
            drawing = emptyList(); pair = null; invalid = false
            if (currentEnabled) {
                val c = cell(touchStart)
                val found = p.paths.firstOrNull { c == it.cells.first() || c == it.head }
                if (found != null) { pair = found.id; drawing = listOf(c);  }
            }
        }, onDrag = { change, _ ->
            change.consume()
            val id = pair
            if (id != null && drawing.isNotEmpty() && currentEnabled) {
                val target = cell(change.position)
                if (target != drawing.last()) {
                    if (drawing.size > 1 && target == drawing[drawing.lastIndex-1]) drawing = drawing.dropLast(1)
                    else {
                        // Interpolate all lattice steps, preventing fast swipes from tunnelling.
                        var route = drawing
                        var guard = 0
                        while (route.last() != target && guard++ < p.size*3) {
                            val a = route.last(); val dx = target.x-a.x; val dy = target.y-a.y
                            val next = if (abs(dx) >= abs(dy) && dx != 0) Cell(a.x+dx.sign,a.y) else Cell(a.x,a.y+dy.sign)
                            val candidate = route + next
                            val own = p.paths.first { it.id == id }
                            val reached = route.size > 1 && route.last() in listOf(own.head,own.cells.first())
                            if (reached) break
                            if (InfiniteEngine.validRoute(p,id,candidate,latest.routes,false)) route = candidate
                            else { if (!invalid) { invalid = true; invalidCallback("Keep lines apart and stay inside the board.") }; break }
                        }
                        drawing = route
                    }
                }
            }
        }, onDragEnd = {
            val id = pair
            if (id != null && currentEnabled && InfiniteEngine.validRoute(p,id,drawing,latest.routes,true)) routeCallback(id,drawing)
            pair = null; drawing = emptyList()
        }, onDragCancel = { pair = null; drawing = emptyList() })
    }) {
        val step = size.width / (p.size + 1)
        fun pos(c: Cell) = Offset((c.x+1)*step,(c.y+1)*step)
        fun stroke(cells: List<Cell>, color: Color, width: Float = step*.24f) {
            if (cells.size < 2) return
            val path = Path().apply { moveTo(pos(cells.first()).x,pos(cells.first()).y); cells.drop(1).forEach { lineTo(pos(it).x,pos(it).y) } }
            drawPath(path,color.copy(alpha=.10f),style=Stroke(width+step*.15f,cap=StrokeCap.Round,join=StrokeJoin.Round))
            drawPath(path,color,style=Stroke(width,cap=StrokeCap.Round,join=StrokeJoin.Round))
        }
        if (grid || p.kind == PuzzleKind.Arrows) repeat(p.size) { x -> repeat(p.size) { y -> drawCircle(Color(0xFFE2DDCF),step*.045f,pos(Cell(x,y))) } }
        if (p.kind == PuzzleKind.Arrows) p.paths.filter { it.id !in s.removed }.forEach { a ->
            val c = palette[a.id % palette.size]
            val points = if (a.id == animating) {
                val length = a.cells.size-1f
                val advance = travel*(length+p.size*2)
                fun at(t: Float): Offset {
                    if (t >= length) return pos(a.head) + Offset(a.direction.x*step,a.direction.y*step)*(t-length)
                    val i = t.toInt().coerceIn(0,a.cells.lastIndex-1)
                    return pos(a.cells[i]) + (pos(a.cells[i+1])-pos(a.cells[i]))*(t-i)
                }
                (0..((a.cells.size-1)*8)).map { at(advance+it/8f) }
            } else a.cells.map { pos(it) }
            val path = Path().apply { moveTo(points.first().x,points.first().y); points.drop(1).forEach { lineTo(it.x,it.y) } }
            if (highlight == a.id) drawPath(path,c.copy(alpha=.22f),style=Stroke(step*.55f,cap=StrokeCap.Round,join=StrokeJoin.Round))
            drawPath(path,c,style=Stroke(step*.23f,cap=StrokeCap.Round,join=StrokeJoin.Round))
            val head = points.last(); val dir = Offset(a.direction.x.toFloat(),a.direction.y.toFloat()); val perpendicular = Offset(-dir.y,dir.x)
            drawLine(c,head,head-dir*step*.30f+perpendicular*step*.22f,step*.21f,StrokeCap.Round)
            drawLine(c,head,head-dir*step*.30f-perpendicular*step*.22f,step*.21f,StrokeCap.Round)
        } else {
            s.routes.filterKeys { it != pair }.forEach { (id,route) -> stroke(route,palette[id%palette.size]) }
            if (drawing.isNotEmpty()) stroke(drawing,if (invalid) Color(0xFFCC4455) else palette[(pair?:0)%palette.size])
            p.paths.forEach { a ->
                val c = palette[a.id%palette.size]
                listOf(a.cells.first(),a.head).forEach { point ->
                    val center = pos(point)
                    if (highlight == a.id) drawCircle(c.copy(alpha=.18f),step*.45f,center)
                    drawCircle(c.copy(alpha=.16f),step*.33f,center+Offset(0f,step*.04f))
                    drawCircle(c,step*.28f,center)
                    drawCircle(Color.White.copy(alpha=.7f),step*.24f,center,style=Stroke(step*.035f))
                    if (symbols) {
                        // Numbers distinguish every pair, including colours reused above 8.
                        drawContext.canvas.nativeCanvas.drawText("${a.id+1}",center.x,center.y+step*.09f,numberPaint.apply { textSize=step*.25f })
                    }
                }
            }
        }
        if (preview.isNotEmpty()) stroke(preview,ink.copy(alpha=.4f),step*.10f)
    }
}
