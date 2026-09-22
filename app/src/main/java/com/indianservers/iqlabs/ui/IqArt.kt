package com.indianservers.iqlabs.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.indianservers.iqlabs.R
import com.indianservers.iqlabs.model.Category
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.LevelId
import com.indianservers.iqlabs.ui.theme.IqGold
import com.indianservers.iqlabs.ui.theme.IqMint
import com.indianservers.iqlabs.ui.theme.IqPeach
import com.indianservers.iqlabs.ui.theme.IqPink
import com.indianservers.iqlabs.ui.theme.IqPurple
import com.indianservers.iqlabs.ui.theme.IqSky
import com.indianservers.iqlabs.ui.theme.IqSoftLavender
import com.indianservers.iqlabs.ui.theme.IqSoftMint
import com.indianservers.iqlabs.ui.theme.IqSoftPeach
import com.indianservers.iqlabs.ui.theme.IqSoftPink
import com.indianservers.iqlabs.ui.theme.IqSoftYellow

fun gameArtRes(game: GameDefinition): Int = when (game.id) {
    "animal-order" -> R.drawable.art_animal_order
    "colour-catch" -> R.drawable.art_colour_catch
    "bigger-or-smaller", "number-balance" -> R.drawable.art_bigger_smaller
    "letter-hunt" -> R.drawable.art_letter_hunt
    "count-the-stars" -> R.drawable.art_count_stars
    "flash-count", "flash-sum" -> R.drawable.art_flash_count
    "first-sound" -> R.drawable.art_first_sound
    "compare-and-choose" -> R.drawable.art_compare_choose
    "direction-dash" -> R.drawable.art_direction_dash
    "equation-builder", "simple-sum" -> R.drawable.art_equation
    "memory-grid", "memory-grid-junior" -> R.drawable.art_memory_grid
    "step-by-step" -> R.drawable.art_step
    else -> when (game.category) {
        Category.Math -> R.drawable.art_math
        Category.Memory -> R.drawable.art_memory_grid
        Category.Logic -> R.drawable.art_logic
        Category.Reading -> R.drawable.art_reading
        Category.Focus -> R.drawable.art_focus
        Category.Spatial -> R.drawable.art_spatial
        Category.Speed -> R.drawable.art_speed
    }
}

fun levelPastel(level: LevelId): Color = when (level) {
    LevelId.Explorer -> IqSoftMint
    LevelId.Challenger -> IqSoftYellow
    LevelId.Thinker -> Color(0xFFDCEFFF)
    LevelId.Mastermind -> IqSoftLavender
    LevelId.Genius -> IqSoftPink
}

fun levelAccent(level: LevelId): Color = when (level) {
    LevelId.Explorer -> IqMint
    LevelId.Challenger -> IqGold
    LevelId.Thinker -> IqSky
    LevelId.Mastermind -> IqPurple
    LevelId.Genius -> IqPink
}

fun levelIcon(level: LevelId): ImageVector = when (level) {
    LevelId.Explorer -> Icons.Rounded.Pets
    LevelId.Challenger -> Icons.Rounded.Bolt
    LevelId.Thinker -> Icons.Rounded.Psychology
    LevelId.Mastermind -> Icons.Rounded.AutoAwesome
    LevelId.Genius -> Icons.Rounded.Star
}

fun categoryPastel(category: Category): Color = when (category) {
    Category.Math -> IqSoftMint
    Category.Memory -> IqSoftPeach
    Category.Logic -> IqSoftYellow
    Category.Reading -> IqSoftLavender
    Category.Focus -> Color(0xFFDCEFFF)
    Category.Spatial -> IqSoftPink
    Category.Speed -> IqSoftYellow
}

fun rankForXp(xp: Int): LevelId = LevelId.entries.lastOrNull { xp >= it.minXp } ?: LevelId.Explorer

fun displayLevelNumber(xp: Int): Int = (xp / 60).coerceAtLeast(1)

fun progressToNextRank(xp: Int): Float {
    val current = rankForXp(xp)
    val next = LevelId.entries.getOrNull(current.ordinal + 1) ?: return 1f
    val span = (next.minXp - current.minXp).coerceAtLeast(1)
    return ((xp - current.minXp).toFloat() / span).coerceIn(0f, 1f)
}

val featuredGameIds = listOf(
    "kids-picture-pairs",
    "kids-light-patterns",
    "kids-shape-detective",
    "kids-color-difference",
    "number-memory",
    "matrix-reasoning",
    "seniors-advanced-matrix",
    "expert-rule-matrix",
    "master-memory-orbit",
    "master-logic-tangle",
)
