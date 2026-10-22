package com.indianservers.iqlabs.puzzle

import androidx.compose.ui.graphics.Color
import com.indianservers.iqlabs.model.*

object PuzzleCatalog {
    fun kind(id: String): PuzzleKind? = when(id) { "arrow-escape" -> PuzzleKind.Arrows; "connect-colours" -> PuzzleKind.Colours; else -> null }
    val games = PuzzleKind.entries.map { kind ->
        val arrows = kind == PuzzleKind.Arrows
        GameDefinition(
            id = if (arrows) "arrow-escape" else "connect-colours", name = kind.title,
            description = if (arrows) "Tap arrows in the right order. Clear the entire board!" else "Connect matching dots. Never let the lines touch!",
            level = LevelId.Explorer, category = if (arrows) Category.Logic else Category.Spatial,
            tags = listOf("Infinite", "Offline", "Puzzle"), difficulty = 1, durationMinutes = 3, minAge = 5,
            icon = if (arrows) "↗" else "●", accent = Color(if (arrows) 0xFF17AF89 else 0xFF248CE5),
            instructions = if (arrows) "Tap arrows with a clear forward exit. Clear blocking arrows first." else "Drag from one numbered dot to its matching partner. Lines cannot touch or cross. Redraw by dragging from an endpoint.",
            scoringModel = "Earn up to three stars through accurate play and thoughtful use of hints.", xpReward = 0,
            unlockRequirement = "Always available", bestScoreType = BestScoreType.HigherIsBetter,
            status = ImplementationStatus.Playable, accessibilityNotes = "Relaxed mode has unlimited lives. Numbered colour pairs, routing dots, and spoken hints are available."
        )
    }
}
