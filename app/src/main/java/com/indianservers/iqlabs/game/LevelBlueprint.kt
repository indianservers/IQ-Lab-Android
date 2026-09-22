package com.indianservers.iqlabs.game

import com.indianservers.iqlabs.model.IqCatalog

data class BlueprintGame(
    val workbookId: String,
    val gameName: String,
    val primaryCategory: String,
    val catalogId: String,
    val difficultyAxes: List<String>,
    val tags: List<String>,
)

data class BlueprintStage(
    val workbookId: String,
    val level: Int,
    val stage: String,
    val designIntent: String,
    val exactDesign: String,
    val sessionShape: String,
    val hints: String,
    val unlockRule: String,
    val masteryTarget: String,
    val scoreMultiplier: Double,
)

/** Native progression generated only for games present in IQ Lab-web. */
object LevelBlueprint {
    const val STANDARD_MAX_LEVEL = 12
    const val MAX_LEVEL = 20

    private val stageNames = listOf(
        "Orientation", "Foundation", "Control", "Momentum", "Expansion",
        "Interference", "Dual Rule", "Pressure", "Advanced", "Mastery",
        "Elite", "Limit Test", "Synthesis", "Transfer", "Ambiguity",
        "Optimization", "Endurance", "Expert", "Grandmaster", "Apex",
    )

    val games: List<BlueprintGame> = IqCatalog.games.map { game ->
        BlueprintGame(
            workbookId = game.id.uppercase().replace('-', '_'),
            gameName = game.name,
            primaryCategory = game.category.label,
            catalogId = game.id,
            difficultyAxes = listOf("round length", "response time", "distractor load", "memory load", "rule complexity"),
            tags = game.tags,
        )
    }

    val stages: List<BlueprintStage> = games.flatMap { game ->
        stageNames.mapIndexed { index, name ->
            val level = index + 1
            BlueprintStage(
                workbookId = game.workbookId,
                level = level,
                stage = name,
                designIntent = "Increase the native ${game.gameName} challenge without changing its core rule.",
                exactDesign = "Level $level: tune ${game.difficultyAxes[index % game.difficultyAxes.size]} for ${game.gameName}.",
                sessionShape = "Six native rounds with immediate feedback.",
                hints = if (level < 4) "One replay or contextual hint." else "No automatic hints.",
                unlockRule = if (level == 1) "Available with the game." else "Complete level ${level - 1}.",
                masteryTarget = if (level < 10) "80% accuracy" else "90% accuracy",
                scoreMultiplier = 1.0 + index * 0.08,
            )
        }
    }

    fun stagesForCatalogGame(catalogId: String): List<BlueprintStage> =
        games.firstOrNull { it.catalogId == catalogId }?.let { game -> stages.filter { it.workbookId == game.workbookId } }.orEmpty()

    fun gameForCatalogId(catalogId: String): BlueprintGame? = games.firstOrNull { it.catalogId == catalogId }
    fun maxLevelFor(catalogId: String): Int = if (gameForCatalogId(catalogId) == null) STANDARD_MAX_LEVEL else MAX_LEVEL
    fun stageFor(catalogId: String, level: Int): BlueprintStage? =
        stagesForCatalogGame(catalogId).firstOrNull { it.level == level.coerceIn(1, maxLevelFor(catalogId)) }

    fun mappedCatalogGamesArePlayable(): Boolean = games.size == IqCatalog.games.size
}
