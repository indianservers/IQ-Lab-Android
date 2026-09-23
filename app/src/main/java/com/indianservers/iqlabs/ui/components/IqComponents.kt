package com.indianservers.iqlabs.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.ui.Tab
import com.indianservers.iqlabs.ui.gameArtRes
import com.indianservers.iqlabs.ui.theme.LocalIqExtras

@Composable
fun IqPage(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val extras = LocalIqExtras.current
    Box(modifier.fillMaxSize().background(extras.pageBrush)) { content() }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    color: Color = LocalIqExtras.current.card,
    radius: Dp = 24.dp,
    shadow: Dp = 10.dp,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .shadow(shadow, RoundedCornerShape(radius), ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(radius))
            .background(color)
            .padding(padding),
        content = content,
    )
}

@Composable
fun AccentCard(
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SoftCard(
        modifier = modifier.fillMaxWidth(),
        color = LocalIqExtras.current.card,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(accent.copy(alpha = .14f), Color.Transparent)))
        ) { content() }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Text(
                action,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable(onClick = onAction).padding(8.dp),
            )
        }
    }
}

@Composable
fun GameArt(
    game: GameDefinition,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(modifier.clip(RoundedCornerShape(18.dp))) {
        Image(
            painter = painterResource(gameArtRes(game)),
            contentDescription = "${game.name} illustration",
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
        )
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(game.accent.copy(alpha = .92f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(game.icon, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PlayBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) .9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "playBadgeScale",
    )
    Box(
        modifier
            .padding(2.dp)
            .size(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(6.dp, CircleShape, ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = .3f))
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary.copy(alpha = .82f), MaterialTheme.colorScheme.primary),
                ),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun IqProgressBar(progress: Float, color: Color, modifier: Modifier = Modifier, track: Color = color.copy(alpha = .16f)) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier.height(8.dp).clip(CircleShape),
        color = color,
        trackColor = track,
    )
}

@Composable
fun IqBottomBar(tab: Tab, onTab: (Tab) -> Unit) {
    val extras = LocalIqExtras.current
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(start = 18.dp, end = 18.dp, bottom = 10.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(18.dp, RoundedCornerShape(34.dp), ambientColor = Color(0x22000000), spotColor = Color(0x33000000))
                .clip(RoundedCornerShape(34.dp))
                .background(extras.navBar)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Tab.entries.forEach { item ->
                val selected = tab == item
                val interactionSource = remember(item) { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) .94f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                    label = "${item.label}TabScale",
                )
                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp, vertical = 5.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selected) extras.navSelected.copy(alpha = .11f) else Color.Transparent)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = LocalIndication.current,
                        ) { onTab(item) }
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) extras.navSelected else extras.navContent,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        item.label,
                        color = if (selected) extras.navSelected else extras.navContent,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
fun MiniStat(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    val extras = LocalIqExtras.current
    Row(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(extras.card)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            Text(value, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(label, fontSize = 10.sp, color = extras.muted)
        }
    }
}

@Composable
fun EmptyState(text: String) {
    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Text(text, fontWeight = FontWeight.Bold, color = LocalIqExtras.current.muted)
    }
}

@Composable
fun GameRowCard(game: GameDefinition, favorite: Boolean = false, onFavorite: (() -> Unit)? = null, onClick: () -> Unit) {
    val extras = LocalIqExtras.current
    SoftCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), padding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GameArt(game, Modifier.size(64.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(game.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${game.category.label} · ${game.durationMinutes} min",
                    color = extras.muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
            PlayBadge(onClick)
        }
    }
}

@Composable
fun HeroRow(onClick: (() -> Unit)? = null, content: @Composable RowScope.() -> Unit) {
    SoftCard(modifier = if (onClick == null) Modifier.fillMaxWidth() else Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, content = content)
    }
}
