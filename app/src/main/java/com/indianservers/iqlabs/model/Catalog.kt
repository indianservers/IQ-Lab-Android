package com.indianservers.iqlabs.model

import androidx.compose.ui.graphics.Color

/** The five audience levels used by the IQ Lab website. */
enum class LevelId(
    val title: String,
    val audience: String,
    val tagline: String,
    val accent: Color,
    val darkAccent: Color,
    val minXp: Int,
) {
    Explorer("Kids", "Kids", "Playful brain games with shapes, pictures, memory, and quick focus.", Color(0xFF8DE640), Color(0xFF2F6F1D), 0),
    Challenger("Juniors", "Juniors", "School-age challenges for reasoning, memory, attention, speed, and reading.", Color(0xFFFFB02E), Color(0xFF925B0E), 600),
    Thinker("Advanced", "Advanced", "Stronger working-memory, reasoning, attention, and processing-speed tasks.", Color(0xFF14C8FF), Color(0xFF075E8D), 1600),
    Mastermind("Expert", "Expert", "Hard multi-step logic, attention, memory, and speed tasks for advanced players.", Color(0xFFB35CFF), Color(0xFF60309C), 3200),
    Genius("Master", "Master", "Complex, high-load challenges built to be difficult and unforgiving.", Color(0xFFFF5D64), Color(0xFF9F2530), 5600),
}

/** Website categories. Math is retained only for saved-progress compatibility. */
enum class Category(val label: String) {
    Math("Speed"),
    Memory("Memory"),
    Logic("Logic"),
    Reading("Reading"),
    Focus("Attention"),
    Spatial("Reasoning"),
    Speed("Speed"),
}

enum class BestScoreType { HigherIsBetter, LowerIsBetter }
enum class ImplementationStatus { Playable, LaterPhase }

data class GameDefinition(
    val id: String,
    val name: String,
    val description: String,
    val level: LevelId,
    val category: Category,
    val tags: List<String>,
    val difficulty: Int,
    val durationMinutes: Int,
    val minAge: Int,
    val icon: String,
    val accent: Color,
    val instructions: String,
    val scoringModel: String,
    val xpReward: Int,
    val unlockRequirement: String,
    val bestScoreType: BestScoreType,
    val status: ImplementationStatus,
    val accessibilityNotes: String,
)

/**
 * Strict native mirror of IQ Lab-web. Android-only, generated, filler, and
 * placeholder games must never be added here.
 */
object IqCatalog {
    val games: List<GameDefinition> = WebLabCatalog.games
    val playableIds: Set<String> = games.mapTo(linkedSetOf()) { it.id }
    val levels: List<LevelId> = LevelId.entries

    init {
        check(games.size == 152) { "IQ Lab-web catalog must contain exactly 152 unique games." }
        check(playableIds.size == games.size) { "IQ Lab-web game ids must be unique." }
    }

    fun levelGames(level: LevelId): List<GameDefinition> = games.filter { it.level == level }
    fun game(id: String): GameDefinition = games.first { it.id == id }
}
