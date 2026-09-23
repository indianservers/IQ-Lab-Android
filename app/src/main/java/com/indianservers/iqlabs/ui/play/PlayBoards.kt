package com.indianservers.iqlabs.ui.play

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.game.PlayStyle
import com.indianservers.iqlabs.game.PlayStyleResolver
import com.indianservers.iqlabs.game.Question
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.ui.components.SoftCard
import com.indianservers.iqlabs.ui.theme.LocalIqExtras
import kotlinx.coroutines.delay

private val playPalette = listOf(
    Color(0xFF6C5CE7), Color(0xFF13D8FF), Color(0xFF8DE640), Color(0xFFFFB02E),
    Color(0xFFFF5D64), Color(0xFF8B5CFF), Color(0xFF5ED0B0), Color(0xFFFF9A6B),
)

private fun Modifier.gamePressable(
    enabled: Boolean = true,
    pressedScale: Float = .96f,
    onTap: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "gameControlScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.clickable(
        enabled = enabled,
        interactionSource = interactionSource,
        indication = LocalIndication.current,
        onClick = onTap,
    )
}

@Composable
fun PlayRound(
    game: GameDefinition,
    question: Question,
    feedback: String?,
    adaptiveHint: String?,
    onAnswer: (String) -> Unit,
    modifier: Modifier = Modifier,
    sound: Boolean = true,
) {
    val style = PlayStyleResolver.forRound(game, question)
    val cue = PlayStyleResolver.cue(game, question)
    SoftCard(modifier = modifier, padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(playVerb(style), color = game.accent, fontWeight = FontWeight.Black, fontSize = 11.sp)
            Spacer(Modifier.weight(1f))
            Text(game.icon, fontSize = 20.sp)
        }
        Text(
            if (style == PlayStyle.Reading) question.prompt else cue,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        AnimatedVisibility(adaptiveHint != null) {
            Text(
                adaptiveHint.orEmpty(),
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(game.accent.copy(alpha = .14f)).padding(6.dp),
                color = game.accent,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(4.dp))
        when (style) {
            PlayStyle.Reading -> ReadingChoices(game, question, onAnswer)
            PlayStyle.Keypad -> KeypadBoard(game, question, onAnswer)
            PlayStyle.Swipe -> SwipeBoard(game, question, onAnswer)
            PlayStyle.GoNoGo -> GoNoGoBoard(game, question, onAnswer)
            PlayStyle.Grid -> GridBoard(game, question, onAnswer)
            PlayStyle.Sequence -> SequenceBoard(game, question, onAnswer, sound)
            PlayStyle.Tiles, PlayStyle.Targets, PlayStyle.Hold -> TargetBoard(game, question, onAnswer)
            PlayStyle.Reveal -> RevealBoard(game, question, onAnswer, sound)
            PlayStyle.Match -> MatchBoard(game, question, onAnswer)
            PlayStyle.Wait -> WaitBoard(game, question, onAnswer, sound)
            PlayStyle.Search -> SearchBoard(game, question, onAnswer)
            PlayStyle.Peripheral -> PeripheralBoard(game, question, onAnswer)
            PlayStyle.SymbolKey -> SymbolKeyBoard(game, question, onAnswer)
            PlayStyle.Sudoku -> SudokuBoard(game, question, onAnswer)
            PlayStyle.Tower -> TowerBoard(game, question, onAnswer)
            PlayStyle.Trail -> TrailBoard(game, question, onAnswer)
            PlayStyle.Shade -> ShadeBoard(game, question, onAnswer)
            PlayStyle.CodeBreaker -> CodeBreakerBoard(game, question, onAnswer)
            PlayStyle.Circuit -> CircuitBoard(game, question, onAnswer)
            PlayStyle.MultiSelect -> MultiSelectBoard(game, question, onAnswer)
            PlayStyle.FocusTrack -> FocusTrackBoard(game, question, onAnswer)
            PlayStyle.StudyChoice -> StudyChoiceBoard(game, question, onAnswer)
            PlayStyle.Matrix -> MatrixBoard(game, question, onAnswer)
            PlayStyle.Deduction -> DeductionBoard(game, question, onAnswer)
            PlayStyle.GridPlacement -> GridPlacementBoard(game, question, onAnswer)
            PlayStyle.NBack -> NBackBoard(game, question, onAnswer)
        }
        AnimatedVisibility(feedback != null) {
            Text(feedback.orEmpty(), color = game.accent, fontWeight = FontWeight.Bold)
        }
    }
}

private fun playVerb(style: PlayStyle) = when (style) {
    PlayStyle.Reading -> "READ"
    PlayStyle.Keypad -> "COUNT IT"
    PlayStyle.Swipe -> "SWIPE"
    PlayStyle.GoNoGo -> "REACT"
    PlayStyle.Targets, PlayStyle.Hold -> "CATCH"
    PlayStyle.Grid -> "REBUILD"
    PlayStyle.Sequence -> "REPLAY"
    PlayStyle.Tiles -> "TAP"
    PlayStyle.Reveal -> "WATCH"
    PlayStyle.Match -> "MATCH"
    PlayStyle.Wait -> "WAIT"
    PlayStyle.Search -> "FIND"
    PlayStyle.Peripheral -> "EDGE"
    PlayStyle.SymbolKey -> "DECODE"
    PlayStyle.Sudoku -> "FILL"
    PlayStyle.Tower -> "STACK"
    PlayStyle.Trail -> "CONNECT"
    PlayStyle.Shade -> "SPOT"
    PlayStyle.CodeBreaker -> "CRACK"
    PlayStyle.Circuit -> "WIRE"
    PlayStyle.MultiSelect -> "SELECT ALL"
    PlayStyle.FocusTrack -> "TRACK"
    PlayStyle.StudyChoice -> "REMEMBER"
    PlayStyle.Matrix -> "COMPLETE"
    PlayStyle.Deduction -> "DEDUCE"
    PlayStyle.GridPlacement -> "PLACE"
    PlayStyle.NBack -> "N-BACK"
}

@Composable
private fun NBackBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    var active by remember(question.roundKey) { mutableIntStateOf(-1) }
    var ready by remember(question.roundKey) { mutableStateOf(false) }
    LaunchedEffect(question.roundKey) {
        ready = false
        question.field.forEach { position ->
            active = position.toIntOrNull() ?: -1
            delay((720L - game.difficulty * 35L).coerceAtLeast(330L))
            active = -1
            delay(120)
        }
        ready = true
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.size(174.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(9) { index ->
                Box(
                    Modifier.size(54.dp).clip(RoundedCornerShape(12.dp)).background(if (index == active) Color(0xFFFFB02E) else game.accent.copy(alpha = .12f)),
                )
            }
        }
        Text(if (ready) "Final position: match or no match?" else "Keep the positions in memory", color = LocalIqExtras.current.muted, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Yes", "No").forEach { choice ->
                PlayOrb(label = choice, color = if (choice == "Yes") Color(0xFF5ED0B0) else Color(0xFFFF776D), modifier = Modifier.weight(1f).height(48.dp)) {
                    if (ready) onAnswer(choice)
                }
            }
        }
    }
}

@Composable
private fun ReadingChoices(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    var shown by remember(question.roundKey) { mutableStateOf(question.field.firstOrNull().orEmpty()) }
    var ready by remember(question.roundKey) { mutableStateOf(question.field.isEmpty()) }
    var wordIndex by remember(question.roundKey) { mutableIntStateOf(0) }
    val wordDelay = (390L - game.difficulty * 32L).coerceAtLeast(140L)
    LaunchedEffect(question.roundKey) {
        if (question.field.isEmpty()) {
            ready = true
            return@LaunchedEffect
        }
        ready = false
        question.field.forEachIndexed { index, word ->
            shown = word
            wordIndex = index + 1
            delay(wordDelay)
        }
        ready = true
    }
    if (!ready) {
        Box(
            modifier = Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF1B2438)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(shown, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
                Text("$wordIndex/${question.field.size} · ${60000 / wordDelay} wpm", color = Color.White.copy(alpha = .65f), fontSize = 11.sp)
            }
        }
        return
    }
    question.choices.forEach { choice ->
        Button(
            onClick = { onAnswer(choice) },
            modifier = Modifier.fillMaxWidth().height(42.dp).padding(vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(choice, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun KeypadBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit, showField: Boolean = true) {
    var typed by remember(question.roundKey) { mutableStateOf("") }
    val maxInputLength = (question.answer.length + if (question.answer.startsWith("-")) 0 else 1).coerceIn(6, 12)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (showField && question.field.isNotEmpty()) {
            Text(question.field.joinToString("  "), fontSize = 20.sp, textAlign = TextAlign.Center, maxLines = 2)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(game.accent.copy(alpha = .12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(typed.ifBlank { "0" }, fontSize = 26.sp, fontWeight = FontWeight.Black, color = game.accent)
        }
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "±", "0", "⌫")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().height(154.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(keys) { key ->
                PlayOrb(
                    label = key,
                    color = if (key == "⌫") Color(0xFFFF5D64) else game.accent,
                    modifier = Modifier.height(34.dp),
                ) {
                    typed = when (key) {
                        "⌫" -> typed.dropLast(1)
                        "±" -> if (typed.startsWith("-")) typed.drop(1) else "-$typed"
                        else -> (typed + key).take(maxInputLength)
                    }
                }
            }
        }
        Button(
            onClick = { onAnswer(typed.ifBlank { "0" }.trimStart('+')) },
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text("Lock in", fontWeight = FontWeight.Black) }
    }
}

@Composable
private fun SwipeBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    var dragTotal by remember(question.roundKey) { mutableStateOf(Offset.Zero) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(116.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(game.accent.copy(alpha = .12f))
                .border(2.dp, game.accent.copy(alpha = .45f), RoundedCornerShape(22.dp))
                .semantics { contentDescription = "Swipe up, right, down, or left" }
                .pointerInput(question.roundKey) {
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
                Text("Flick the pad", color = game.accent, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text("↑   →   ↓   ←", color = LocalIqExtras.current.muted, fontSize = 32.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Up", "Left", "Down", "Right").forEach { dir ->
                PlayOrb(
                    label = directionGlyph(dir),
                    color = game.accent,
                    modifier = Modifier.weight(1f).height(44.dp),
                    circle = true,
                ) { onAnswer(dir) }
            }
        }
    }
}

@Composable
private fun GoNoGoBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val left = question.choices.getOrNull(0) ?: "Go"
    val right = question.choices.getOrNull(1) ?: "Skip"
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        PlayOrb(
            label = left,
            color = Color(0xFF5ED0B0),
            modifier = Modifier.weight(1f).height(108.dp),
            circle = true,
            textSize = 22.sp,
        ) { onAnswer(left) }
        PlayOrb(
            label = right,
            color = Color(0xFFFF9A6B),
            modifier = Modifier.weight(1f).height(108.dp),
            circle = true,
            textSize = 22.sp,
        ) { onAnswer(right) }
    }
}

@Composable
private fun TargetBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val stroopTrial = question.field.firstOrNull() == "stroop"
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        if (stroopTrial) {
            val inkColor = when (question.field.getOrNull(2)) {
                "Red" -> Color(0xFFEF4444)
                "Blue" -> Color(0xFF3B82F6)
                "Green" -> Color(0xFF10B981)
                "Yellow" -> Color(0xFFF59E0B)
                "Purple" -> Color(0xFF8B5CF6)
                else -> game.accent
            }
            Text(
                question.field.getOrNull(1).orEmpty(),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF4F5F7)).padding(10.dp),
                color = inkColor,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
            )
        } else if (question.field.isNotEmpty()) {
            Text(
                question.field.joinToString("  "),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(game.accent.copy(alpha = .10f)).padding(8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (question.choices.size <= 4) 2 else 3),
            modifier = Modifier.fillMaxWidth().height(if (question.choices.size <= 4) 112.dp else 168.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(question.choices) { choice ->
                val index = question.choices.indexOf(choice)
                val choiceColor = if (stroopTrial) when (choice) {
                    "Red" -> Color(0xFFEF4444)
                    "Blue" -> Color(0xFF3B82F6)
                    "Green" -> Color(0xFF10B981)
                    "Yellow" -> Color(0xFFF59E0B)
                    "Purple" -> Color(0xFF8B5CF6)
                    else -> game.accent
                } else playPalette[index % playPalette.size]
            PlayOrb(
                label = choice,
                color = choiceColor,
                    modifier = Modifier.height(50.dp),
                textSize = 14.sp,
            ) { onAnswer(choice) }
            }
        }
    }
}

@Composable
private fun GridBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val size = if (game.id.startsWith("kids-") || question.prompt.contains("3x3")) 9 else 16
    val columns = if (size == 9) 3 else 4
    val lit = question.answer.split("-").mapNotNull { it.toIntOrNull() }.toSet()
    var revealed by remember(question.roundKey) { mutableStateOf(true) }
    var revealedCount by remember(question.roundKey) { mutableIntStateOf(0) }
    var selected by remember(question.roundKey) { mutableStateOf(setOf<Int>()) }
    LaunchedEffect(question.roundKey) {
        revealed = true
        revealedCount = 0
        selected = emptySet()
        lit.forEachIndexed { index, _ ->
            revealedCount = index + 1
            delay((420L - game.difficulty * 28L).coerceAtLeast(170L))
        }
        delay(350)
        revealed = false
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text(if (revealed) "Watch ${revealedCount}/${lit.size}" else "Selected ${selected.size}/${lit.size}", color = LocalIqExtras.current.muted, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxWidth().height(if (size == 9) 156.dp else 196.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items((1..size).toList()) { cell ->
                val on = if (revealed) cell in lit.take(revealedCount) else cell in selected
                Box(
                    modifier = Modifier
                        .height(if (size == 9) 44.dp else 40.dp)
                        .gamePressable(enabled = !revealed) {
                            selected = if (cell in selected) selected - cell else selected + cell
                        }
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (on) game.accent else game.accent.copy(alpha = .12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(cell.toString(), color = if (on) Color.White else LocalIqExtras.current.muted, fontWeight = FontWeight.Black)
                }
            }
        }
        Button(
            onClick = { onAnswer(selected.sorted().joinToString("-")) },
            enabled = !revealed,
            modifier = Modifier.fillMaxWidth().height(42.dp),
            shape = RoundedCornerShape(16.dp),
        ) { Text("Lock pattern", fontWeight = FontWeight.Black) }
    }
}

@Composable
private fun SequenceBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit, sound: Boolean) {
    val sequence = question.answer.split(" → ").map { it.trim() }.filter { it.isNotBlank() }
    val orderingGame = game.id == "kids-animal-line-up"
    val pads = (if (orderingGame) question.field else sequence).distinct()
    var revealed by remember(question.roundKey) { mutableStateOf(!orderingGame) }
    var revealIndex by remember(question.roundKey) { mutableIntStateOf(0) }
    var built by remember(question.roundKey) { mutableStateOf(listOf<String>()) }
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 35) }
    DisposableEffect(Unit) { onDispose { tone.release() } }
    LaunchedEffect(revealIndex, revealed, sound) {
        if (revealed && sound) tone.startTone(ToneGenerator.TONE_DTMF_1 + (revealIndex % 4), 100)
    }
    LaunchedEffect(question.roundKey) {
        revealed = !orderingGame
        revealIndex = 0
        built = emptyList()
        if (!orderingGame) {
            sequence.forEachIndexed { index, _ ->
                revealIndex = index
                delay((620L - game.difficulty * 45L).coerceAtLeast(240L))
            }
            delay(280)
        }
        revealed = false
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(game.accent.copy(alpha = .10f))
                .padding(8.dp),
        ) {
            Text(
                if (revealed) sequence.getOrNull(revealIndex).orEmpty() else built.joinToString(" → ").ifBlank { if (orderingGame) "Arrange the visible items" else "Repeat the sequence" },
                fontWeight = FontWeight.Black,
                fontSize = if (revealed) 32.sp else 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().height(if (pads.size > 4) 164.dp else 112.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(pads) { token ->
                PlayOrb(
                    label = token,
                    color = game.accent,
                    modifier = Modifier.height(48.dp),
                ) {
                    if (!revealed) {
                        val next = built + token
                        built = next
                        if (next.size >= sequence.size) onAnswer(next.joinToString(" → "))
                    }
                }
            }
        }
    }
}

@Composable
private fun TileBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxWidth().height(188.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(question.choices) { choice ->
            val color = playPalette[kotlin.math.abs(choice.hashCode()) % playPalette.size]
            PlayOrb(
                label = choice,
                color = color,
                modifier = Modifier.height(82.dp),
                textSize = 16.sp,
            ) { onAnswer(choice) }
        }
    }
}

@Composable
private fun PlayOrb(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    circle: Boolean = false,
    textSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    onTap: () -> Unit,
) {
    val shape = if (circle) CircleShape else RoundedCornerShape(22.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.955f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "gameButtonScale",
    )
    Box(
        modifier
            .padding(horizontal = 3.dp, vertical = 3.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 7.dp,
                shape = shape,
                ambientColor = color.copy(alpha = .28f),
                spotColor = color.copy(alpha = .38f),
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = .82f), color),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = .34f), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onTap,
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = textSize,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun directionGlyph(direction: String): String = when (direction) {
    "Up" -> "↑"
    "Right" -> "→"
    "Down" -> "↓"
    "Left" -> "←"
    else -> direction
}

@Composable
private fun RevealBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit, sound: Boolean) {
    var revealed by remember(question.roundKey) { mutableStateOf(true) }
    var shownIndex by remember(question.roundKey) { mutableIntStateOf(0) }
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 28) }
    DisposableEffect(Unit) { onDispose { tone.release() } }
    LaunchedEffect(shownIndex, revealed, sound) {
        if (revealed && sound) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
    }
    LaunchedEffect(question.roundKey) {
        revealed = true
        shownIndex = 0
        question.field.forEachIndexed { index, _ ->
            shownIndex = index
            delay((620L - game.difficulty * 42L).coerceAtLeast(220L))
        }
        delay(260)
        revealed = false
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            if (revealed) question.field.getOrNull(shownIndex).orEmpty() else "••••",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = game.accent,
            textAlign = TextAlign.Center,
        )
        if (revealed) {
            Text("${shownIndex + 1}/${question.field.size}", color = LocalIqExtras.current.muted, fontSize = 12.sp)
        } else KeypadBoard(game, question, onAnswer, showField = false)
    }
}

@Composable
private fun MatchBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val tiles = question.field.ifEmpty { question.choices }
    var open by remember(question.roundKey) { mutableStateOf(setOf<Int>()) }
    var matched by remember(question.roundKey) { mutableStateOf(setOf<Int>()) }
    var first by remember(question.roundKey) { mutableStateOf<Int?>(null) }
    var moves by remember(question.roundKey) { mutableIntStateOf(0) }
    var mistakes by remember(question.roundKey) { mutableIntStateOf(0) }
    var pendingClose by remember(question.roundKey) { mutableStateOf(false) }
    val columns = 4
    LaunchedEffect(pendingClose) {
        if (pendingClose) {
            delay(520)
            open = emptySet()
            first = null
            pendingClose = false
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Moves $moves", fontWeight = FontWeight.Bold, color = game.accent)
            Text("Mistakes $mistakes", color = LocalIqExtras.current.muted)
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxWidth().height(196.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tiles.indices.toList()) { index ->
                val face = if (index in matched || index in open) tiles[index] else "?"
                PlayOrb(
                    label = face,
                    color = if (index in matched) Color(0xFF5ED0B0) else game.accent,
                    modifier = Modifier.height(42.dp),
                ) {
                    if (pendingClose || index in matched || index in open) return@PlayOrb
                    val shown = open + index
                    open = shown
                    val prior = first
                    if (prior == null) {
                        first = index
                    } else {
                        moves++
                        if (tiles[prior] == tiles[index] && prior != index) {
                            val next = matched + prior + index
                            matched = next
                            open = emptySet()
                            first = null
                            if (next.size == tiles.size) onAnswer(question.answer)
                        } else {
                            mistakes++
                            pendingClose = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaitBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit, sound: Boolean) {
    val waitMs = question.field.firstOrNull()?.toLongOrNull() ?: 900L
    var live by remember(question.roundKey) { mutableStateOf(false) }
    val tone = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 45) }
    DisposableEffect(Unit) { onDispose { tone.release() } }
    LaunchedEffect(question.roundKey) {
        live = false
        delay(waitMs)
        live = true
        if (sound) tone.startTone(ToneGenerator.TONE_PROP_ACK, 140)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .gamePressable(pressedScale = .975f) { onAnswer(if (live) "Go" else "Wait") }
            .clip(RoundedCornerShape(24.dp))
            .background(if (live) Color(0xFF5ED0B0) else Color(0xFF3A2230)),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (live) "GO" else "Wait…", fontWeight = FontWeight.Black, fontSize = 36.sp, color = Color.White)
    }
}

@Composable
private fun SearchBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val cells = question.field.ifEmpty { question.choices }
    val columns = when {
        cells.size <= 16 -> 4
        cells.size <= 25 -> 5
        else -> 6
    }
    val rows = (cells.size + columns - 1) / columns
    val tileHeight = when {
        rows <= 4 -> 40.dp
        rows == 5 -> 32.dp
        else -> 26.dp
    }
    val gridHeight = (tileHeight.value * rows + 4f * (rows - 1).coerceAtLeast(0)).dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxWidth().height(gridHeight),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(cells) { cell ->
            PlayOrb(label = cell, color = game.accent, modifier = Modifier.height(tileHeight), textSize = if (rows >= 6) 13.sp else 16.sp) { onAnswer(cell) }
        }
    }
}

@Composable
private fun PeripheralBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val ring = question.field.drop(1).ifEmpty { question.choices }
    val alignments = listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd)
    Box(
        Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(20.dp)).background(game.accent.copy(alpha = .08f)).padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("KEEP EYES HERE\n•", textAlign = TextAlign.Center, color = LocalIqExtras.current.muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        ring.take(4).forEachIndexed { index, glyph ->
            PlayOrb(label = glyph, color = playPalette[index], modifier = Modifier.align(alignments[index]).size(54.dp), circle = true) { onAnswer(glyph) }
        }
    }
}

@Composable
private fun SymbolKeyBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text(question.field.firstOrNull().orEmpty(), fontWeight = FontWeight.Black, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(question.field.getOrNull(1).orEmpty(), fontSize = 36.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        KeypadBoard(game, question, onAnswer)
    }
}

@Composable
private fun SudokuBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val given = question.field.map { it.toIntOrNull() ?: 0 }
    var cells by remember(question.roundKey) { mutableStateOf(given) }
    var selected by remember(question.roundKey) { mutableIntStateOf(given.indexOfFirst { it == 0 }.coerceAtLeast(0)) }
    var history by remember(question.roundKey) { mutableStateOf(emptyList<List<Int>>()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().height(174.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(16) { index ->
                val locked = given.getOrElse(index) { 0 } != 0
                val value = cells.getOrElse(index) { 0 }
                val row = index / 4
                val col = index % 4
                val conflict = value != 0 && (
                    cells.indices.any { it != index && it / 4 == row && cells[it] == value } ||
                        cells.indices.any { it != index && it % 4 == col && cells[it] == value }
                    )
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .gamePressable(enabled = !locked) { selected = index }
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (conflict) Color(0xFFFF5D64) else if (index == selected) game.accent else game.accent.copy(alpha = .16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (value == 0) "" else value.toString(), fontWeight = FontWeight.Black, color = if (index == selected || conflict) Color.White else LocalIqExtras.current.muted)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            (1..4).forEach { n ->
                PlayOrb(label = n.toString(), color = game.accent, modifier = Modifier.weight(1f).height(44.dp)) {
                    history = history + listOf(cells)
                    val next = cells.toMutableList()
                    next[selected] = n
                    cells = next
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                history.lastOrNull()?.let { previous -> cells = previous; history = history.dropLast(1) }
            }, enabled = history.isNotEmpty(), modifier = Modifier.weight(1f).height(42.dp)) { Text("Undo") }
            Button(onClick = {
                val symbols = (1..4).toSet()
                val completeAndValid = cells.size == 16 &&
                    (0 until 4).all { row -> cells.subList(row * 4, row * 4 + 4).toSet() == symbols } &&
                    (0 until 4).all { col -> (0 until 4).map { row -> cells[row * 4 + col] }.toSet() == symbols }
                onAnswer(if (completeAndValid) question.answer else cells.joinToString(""))
            }, modifier = Modifier.weight(2f).height(42.dp), shape = RoundedCornerShape(14.dp)) {
                Text("Check grid", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun TowerBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val discs = question.field.firstOrNull()?.toIntOrNull()?.coerceIn(3, 4) ?: 3
    var pegs by remember(question.roundKey) {
        mutableStateOf(listOf((discs downTo 1).toList(), emptyList(), emptyList()))
    }
    var holding by remember(question.roundKey) { mutableStateOf<Int?>(null) }
    var moves by remember(question.roundKey) { mutableIntStateOf(0) }
    var illegal by remember(question.roundKey) { mutableIntStateOf(0) }
    fun tap(peg: Int) {
        val hand = holding
        val stacks = pegs.map { it.toMutableList() }
        if (hand == null) {
            if (stacks[peg].isNotEmpty()) {
                holding = stacks[peg].removeAt(stacks[peg].lastIndex)
                pegs = stacks
            }
        } else {
            val top = stacks[peg].lastOrNull()
            if (top == null || hand < top) {
                stacks[peg].add(hand)
                holding = null
                pegs = stacks
                moves++
                if (stacks[2].size == discs) onAnswer("done")
            } else {
                illegal++
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Moves $moves · Best ${(1 shl discs) - 1}", fontWeight = FontWeight.Bold, color = game.accent)
        Text("Illegal $illegal", color = LocalIqExtras.current.muted)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        listOf("A", "B", "C").forEachIndexed { index, label ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(144.dp)
                    .gamePressable(pressedScale = .975f) { tap(index) }
                    .clip(RoundedCornerShape(16.dp))
                    .background(game.accent.copy(alpha = .12f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                pegs[index].reversed().forEach { disc ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((0.35f + disc / 8f).coerceAtMost(1f))
                            .height(18.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(playPalette[disc % playPalette.size]),
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Text(label, fontWeight = FontWeight.Black, color = game.accent)
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(if (holding == null) "Tap a peg to lift" else "Holding disc $holding", color = LocalIqExtras.current.muted, modifier = Modifier.weight(1f))
        Button(onClick = {
            pegs = listOf((discs downTo 1).toList(), emptyList(), emptyList())
            holding = null
            moves = 0
            illegal = 0
        }, modifier = Modifier.height(38.dp)) { Text("Restart") }
    }
}

@Composable
private fun TrailBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val tokens = question.field.ifEmpty { (1..6).map { it.toString() } }
    val numbers = tokens.mapNotNull { it.toIntOrNull() }.sorted().map { it.toString() }
    val letters = tokens.filter { it.length == 1 && it.first().isLetter() }.sorted()
    val needed = if (letters.isEmpty()) numbers else buildList {
        repeat(maxOf(numbers.size, letters.size)) { index ->
            numbers.getOrNull(index)?.let(::add)
            letters.getOrNull(index)?.let(::add)
        }
    }
    var tapped by remember(question.roundKey) { mutableStateOf(listOf<String>()) }
    var errors by remember(question.roundKey) { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Path ${tapped.size}/${needed.size}", fontWeight = FontWeight.Bold, color = game.accent)
        Text("Errors $errors", color = LocalIqExtras.current.muted)
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth().height(170.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tokens) { token ->
            val done = token in tapped
            PlayOrb(
                label = token,
                color = if (done) Color(0xFF5ED0B0) else game.accent,
                modifier = Modifier.height(48.dp),
            ) {
                if (done) return@PlayOrb
                val expected = needed.getOrNull(tapped.size)
                if (token == expected) {
                    val next = tapped + token
                    tapped = next
                    if (next == needed) onAnswer("clear")
                } else errors++
            }
        }
    }
    }
}

@Composable
private fun ShadeBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val tiles = question.field.ifEmpty { List(9) { "base" } }
    val columns = if (tiles.size <= 9) 3 else 4
    val rows = (tiles.size + columns - 1) / columns
    val tileHeight = if (rows <= 3) 48.dp else 38.dp
    val gridHeight = (tileHeight.value * rows + 8f * (rows - 1).coerceAtLeast(0)).dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxWidth().height(gridHeight),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tiles.indices.toList()) { index ->
            val odd = tiles[index] == "odd"
            Box(
                modifier = Modifier
                    .height(tileHeight)
                    .gamePressable { onAnswer(index.toString()) }
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (odd) game.accent.copy(alpha = .72f) else game.accent.copy(alpha = .42f))
                    .border(if (odd) 3.dp else 1.dp, if (odd) game.accent else game.accent.copy(alpha = .22f), RoundedCornerShape(12.dp)),
            )
        }
    }
}

@Composable
private fun CodeBreakerBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    if (question.field.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
            Text("● exact position · ○ right digit, wrong position", color = LocalIqExtras.current.muted, fontSize = 12.sp)
            question.field.forEach { encoded ->
                val parts = encoded.split("|")
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(game.accent.copy(alpha = .08f)).padding(7.dp)) {
                    Text(parts.getOrElse(0) { "" }.toCharArray().joinToString("  "), fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                    Text("● ${parts.getOrElse(1) { "0" }}   ○ ${parts.getOrElse(2) { "0" }}", color = game.accent, fontWeight = FontWeight.Bold)
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(104.dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(question.choices) { choice ->
                    PlayOrb(label = choice.toCharArray().joinToString(" "), color = game.accent, modifier = Modifier.height(46.dp)) { onAnswer(choice) }
                }
            }
        }
        return
    }
    val length = question.answer.length.coerceIn(3, 4)
    var digits by remember(question.roundKey) { mutableStateOf(List(length) { 1 }) }
    var attempts by remember(question.roundKey) { mutableStateOf(emptyList<Pair<String, String>>()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text("Exact position ● · Right digit ○ · ${4 - attempts.size} attempts left", color = LocalIqExtras.current.muted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            digits.forEachIndexed { index, digit ->
                PlayOrb(label = digit.toString(), color = game.accent, modifier = Modifier.weight(1f).height(54.dp)) {
                    digits = digits.toMutableList().also { it[index] = (digit % 6) + 1 }
                }
            }
        }
        attempts.takeLast(3).forEach { (guess, clue) ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(game.accent.copy(alpha = .08f)).padding(6.dp)) {
                Text(guess, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                Text(clue, color = game.accent, fontWeight = FontWeight.Bold)
            }
        }
        Button(onClick = {
            val guess = digits.joinToString("")
            val exact = guess.zip(question.answer).count { it.first == it.second }
            val common = guess.toList().groupingBy { it }.eachCount().map { (digit, count) -> minOf(count, question.answer.count { it == digit }) }.sum()
            val clue = "●".repeat(exact) + "○".repeat((common - exact).coerceAtLeast(0)) + "—".repeat((length - common).coerceAtLeast(0))
            val next = attempts + (guess to clue)
            attempts = next
            if (guess == question.answer || next.size >= 4) onAnswer(guess)
        }, modifier = Modifier.fillMaxWidth().height(44.dp)) { Text("Check code", fontWeight = FontWeight.Black) }
    }
}

@Composable
private fun CircuitBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val gate = question.field.getOrNull(0) ?: "AND"
    val target = question.field.getOrNull(1)?.toBooleanStrictOrNull() ?: true
    var a by remember(question.roundKey) { mutableStateOf(false) }
    var b by remember(question.roundKey) { mutableStateOf(false) }
    val output = when (gate) {
        "AND" -> a && b
        "XOR" -> a xor b
        else -> a || b
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text("$gate gate · Target ${if (target) "ON" else "OFF"}", fontWeight = FontWeight.Black, color = game.accent, fontSize = 18.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("A" to a, "B" to b).forEach { (label, on) ->
                PlayOrb(label = "$label\n${if (on) "ON" else "OFF"}", color = if (on) Color(0xFF5ED0B0) else Color(0xFF667085), modifier = Modifier.weight(1f).height(74.dp)) {
                    if (label == "A") a = !a else b = !b
                }
            }
        }
        Text("A ─┐\n     $gate ── ${if (output) "● ON" else "○ OFF"}\nB ─┘", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Button(onClick = { onAnswer(if (output == target) question.answer else "${a},${b}") }, modifier = Modifier.fillMaxWidth().height(44.dp)) { Text("Test circuit") }
    }
}

@Composable
private fun MultiSelectBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    var selected by remember(question.roundKey) { mutableStateOf(setOf<String>()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text("Selected ${selected.size}", color = LocalIqExtras.current.muted, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().height(150.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(question.choices) { choice ->
                PlayOrb(
                    label = choice,
                    color = if (choice in selected) Color(0xFF5ED0B0) else game.accent,
                    modifier = Modifier.height(44.dp),
                ) { selected = if (choice in selected) selected - choice else selected + choice }
            }
        }
        Button(onClick = { onAnswer(selected.sorted().joinToString("-")) }, modifier = Modifier.fillMaxWidth().height(44.dp)) { Text("Lock selection") }
    }
}

@Composable
private fun FocusTrackBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val positions = mapOf("N" to Alignment.TopCenter, "E" to Alignment.CenterEnd, "S" to Alignment.BottomCenter, "W" to Alignment.CenterStart)
    var step by remember(question.roundKey) { mutableIntStateOf(0) }
    var ready by remember(question.roundKey) { mutableStateOf(false) }
    var hitSteps by remember(question.roundKey) { mutableStateOf(setOf<Int>()) }
    LaunchedEffect(question.roundKey) {
        ready = false
        hitSteps = emptySet()
        question.field.forEachIndexed { index, _ ->
            step = index
            delay((760L - game.difficulty * 45L).coerceAtLeast(300L))
        }
        ready = true
        delay(350)
        val requiredHits = ((question.field.size * 2) + 2) / 3
        onAnswer(if (hitSteps.size >= requiredHits) question.answer else "missed")
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(154.dp).clip(RoundedCornerShape(22.dp)).background(game.accent.copy(alpha = .08f)).padding(14.dp)) {
            if (!ready) {
                Box(
                    Modifier
                        .align(positions[question.field.getOrNull(step)] ?: Alignment.Center)
                        .size(38.dp)
                        .gamePressable(pressedScale = .88f) { hitSteps = hitSteps + step }
                        .clip(CircleShape)
                        .background(game.accent),
                )
            }
            Text(if (ready) "Checking focus…" else "Tap the dot · ${hitSteps.size} hits", modifier = Modifier.align(Alignment.Center), color = LocalIqExtras.current.muted, fontWeight = FontWeight.Bold)
        }
        Text("Stay with each new position", color = game.accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StudyChoiceBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val divider = question.field.indexOf("|")
    val studied = if (divider >= 0) question.field.take(divider) else question.field.take(3)
    val probes = if (divider >= 0) question.field.drop(divider + 1) else question.choices
    var studying by remember(question.roundKey) { mutableStateOf(true) }
    LaunchedEffect(question.roundKey) {
        studying = true
        delay((2200L - game.difficulty * 90L).coerceAtLeast(1100L))
        studying = false
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(if (studying) "Study these faces" else "Which face did you study?", fontWeight = FontWeight.Bold, color = game.accent)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            (if (studying) studied else probes).forEach { face ->
                PlayOrb(label = face, color = game.accent, modifier = Modifier.size(64.dp), circle = true, textSize = 28.sp) {
                    if (!studying) onAnswer(face)
                }
            }
        }
    }
}

@Composable
private fun MatrixBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth().height(160.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(question.field.take(9)) { cell ->
                Box(
                    Modifier.height(48.dp).clip(RoundedCornerShape(10.dp)).background(if (cell == "?") game.accent else game.accent.copy(alpha = .12f)),
                    contentAlignment = Alignment.Center,
                ) { Text(cell, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (cell == "?") Color.White else game.accent) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            question.choices.forEach { choice ->
                PlayOrb(label = choice, color = game.accent, modifier = Modifier.weight(1f).height(46.dp)) { onAnswer(choice) }
            }
        }
    }
}

@Composable
private fun DeductionBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
        question.field.forEachIndexed { index, clue ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(game.accent.copy(alpha = .08f)).padding(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${index + 1}", color = Color.White, modifier = Modifier.size(24.dp).clip(CircleShape).background(game.accent).padding(3.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Black)
                Text(clue, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().height(104.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(question.choices) { choice ->
                PlayOrb(label = choice, color = game.accent, modifier = Modifier.height(46.dp)) { onAnswer(choice) }
            }
        }
    }
}

@Composable
private fun GridPlacementBoard(game: GameDefinition, question: Question, onAnswer: (String) -> Unit) {
    val grid = question.field.take(4)
    val valid = question.field.firstOrNull { it.startsWith("valid:") }?.removePrefix("valid:")?.split("|").orEmpty()
    val colourMap = mapOf("Teal" to Color(0xFF28B8A8), "Gold" to Color(0xFFFFB02E), "Violet" to Color(0xFF8B5CFF), "Coral" to Color(0xFFFF776D), "Locked" to Color(0xFF667085))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.size(174.dp),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(grid) { cell ->
                Box(
                    Modifier.size(82.dp).clip(RoundedCornerShape(16.dp)).background(colourMap[cell] ?: game.accent.copy(alpha = .10f)).border(2.dp, game.accent.copy(alpha = .35f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text(cell, color = if (cell == "?") game.accent else Color.White, fontWeight = FontWeight.Black) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            question.choices.forEach { choice ->
                PlayOrb(label = choice.take(1), color = colourMap[choice] ?: game.accent, modifier = Modifier.weight(1f).height(44.dp), circle = true) {
                    onAnswer(if (choice in valid) question.answer else choice)
                }
            }
        }
    }
}
