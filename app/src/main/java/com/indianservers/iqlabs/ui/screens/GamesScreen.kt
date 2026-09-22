package com.indianservers.iqlabs.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import com.indianservers.iqlabs.ui.components.EmptyState
import com.indianservers.iqlabs.ui.components.GameArt
import com.indianservers.iqlabs.ui.components.IqPage
import com.indianservers.iqlabs.ui.components.PlayBadge
import com.indianservers.iqlabs.ui.components.SectionHeader
import com.indianservers.iqlabs.ui.components.SoftCard
import com.indianservers.iqlabs.ui.featuredGameIds
import com.indianservers.iqlabs.ui.theme.LocalIqExtras

@Composable
fun CatalogueScreen(
    favorites: Map<String, Boolean>,
    onGame: (String) -> Unit,
    onFavorite: (String) -> Unit,
) {
    val extras = LocalIqExtras.current
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>("All") }
    var levelName by rememberSaveable { mutableStateOf("All") }
    val games = IqCatalog.games.filter {
        (query.isBlank() || it.name.contains(query, true) || it.tags.any { tag -> tag.contains(query, true) } || it.category.label.contains(query, true)) &&
            (category == null || category == "All" || it.category.label == category) &&
            (levelName == "All" || it.level.title == levelName)
    }
    val featured = featuredGameIds.mapNotNull { id -> games.find { it.id == id } ?: IqCatalog.games.find { it.id == id } }.take(2)

    IqPage {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text("IQ Games", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text("Play. Learn. Get Smarter.", color = extras.muted)
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(extras.card)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = extras.muted)
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (query.isEmpty()) Text("Search games, skills or categories...", color = extras.muted, fontSize = 14.sp)
                            inner()
                        },
                    )
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (listOf("All") + LevelId.entries.map { it.title }).forEach { chip ->
                        val selected = levelName == chip
                        Text(
                            chip,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else extras.card)
                                .clickable { levelName = chip }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else extras.muted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Memory", "Logic", "Speed", "Attention", "Reading", "Reasoning").forEach { chip ->
                        val selected = category == chip || (chip == "All" && category == null)
                        Text(
                            chip,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else extras.card)
                                .clickable { category = if (chip == "All") "All" else chip }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else extras.muted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
            if (featured.size == 2 && query.isBlank() && category == "All" && levelName == "All") {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader("Featured Games", "See all") {}
                }
                featured.forEach { game ->
                    item {
                        FeaturedCard(game, favorites[game.id] == true, { onFavorite(game.id) }, { onGame(game.id) })
                    }
                }
            }
            if (games.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { EmptyState("No games match those filters.") }
            }
            IqCatalog.levels.forEach { level ->
                val sectionGames = games.filter { it.level == level }
                if (sectionGames.isNotEmpty()) {
                    item(key = "level-${level.name}", span = { GridItemSpan(maxLineSpan) }) {
                        Text("${level.title} (${sectionGames.size})", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    items(sectionGames, key = { it.id }) { game ->
                        GameGridCard(game, favorites[game.id] == true, { onFavorite(game.id) }, { onGame(game.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedCard(game: GameDefinition, favorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), padding = 10.dp, radius = 22.dp) {
        Box {
            GameArt(game, Modifier.fillMaxWidth().height(120.dp))
            FavoriteDot(favorite, onFavorite, Modifier.align(Alignment.TopEnd).padding(8.dp))
            PlayBadge(onClick, Modifier.align(Alignment.BottomEnd).padding(8.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(game.name, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${game.category.label} · ${game.level.title}", color = LocalIqExtras.current.muted, fontSize = 12.sp)
    }
}

@Composable
private fun GameGridCard(game: GameDefinition, favorite: Boolean, onFavorite: () -> Unit, onClick: () -> Unit) {
    SoftCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), padding = 10.dp, radius = 22.dp) {
        Box {
            GameArt(game, Modifier.fillMaxWidth().height(96.dp))
            FavoriteDot(favorite, onFavorite, Modifier.align(Alignment.TopEnd).padding(6.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(game.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${game.durationMinutes} min", color = LocalIqExtras.current.muted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            PlayBadge(onClick, Modifier.size(28.dp))
        }
    }
}

@Composable
private fun FavoriteDot(favorite: Boolean, onFavorite: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(LocalIqExtras.current.card.copy(alpha = .92f))
            .clickable(onClick = onFavorite),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (favorite) "Unfavorite" else "Favorite",
            tint = if (favorite) MaterialTheme.colorScheme.tertiary else LocalIqExtras.current.muted,
            modifier = Modifier.size(16.dp),
        )
    }
}
