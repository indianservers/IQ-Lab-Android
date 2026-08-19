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

object LevelBlueprint {
    private val stageNames = listOf(
        "Orientation",
        "Foundation",
        "Control",
        "Momentum",
        "Expansion",
        "Interference",
        "Dual Rule",
        "Pressure",
        "Advanced",
        "Mastery",
        "Elite",
        "Limit Test",
    )

    val games = listOf(
        BlueprintGame("MEM_GRID", "Memory Grid", "Visual Memory", "memory-grid", listOf("grid size", "highlighted cells", "display time", "order recall", "interference", "transformation"), listOf("memory", "visual", "grid", "sequence", "focus")),
        BlueprintGame("FLASH_SUM", "Flash Sum", "Mental Mathematics", "flash-sum", listOf("term count", "value range", "signs", "display speed", "operations", "recall delay"), listOf("math", "speed", "working-memory", "numbers", "timed")),
        BlueprintGame("SEQ_DETECT", "Sequence Detective", "Numerical Reasoning", "sequence-detective", listOf("rule family", "missing position", "sequence length", "alternating rules", "noise", "answer mode"), listOf("logic", "math", "sequence", "pattern", "numbers")),
        BlueprintGame("WORD_SPRINT", "Word Sprint", "Speed Reading", "word-sprint", listOf("words per minute", "passage length", "question inference", "recall delay", "distractors"), listOf("reading", "speed", "words", "focus", "comprehension")),
        BlueprintGame("PATTERN_PULSE", "Pattern Pulse", "Sequence Memory", "pattern-pulse", listOf("sequence length", "symbol set", "tempo", "reverse recall", "dual rule"), listOf("memory", "visual", "sequence", "focus", "timed")),
        BlueprintGame("QUICK_MATCH", "Quick Match", "Processing Speed", "quick-match", listOf("stimulus type", "rule changes", "response window", "distractors", "inhibition"), listOf("speed", "reaction", "attention", "focus", "adaptive")),
        BlueprintGame("MATH_RUSH", "Mental Math Rush", "Arithmetic Speed", "mental-math-rush", listOf("operations", "number range", "term count", "time pressure", "mixed signs"), listOf("math", "speed", "numbers", "calculation", "timed")),
        BlueprintGame("LOGIC_GRID", "Logic Grid", "Deductive Logic", "logic-grid", listOf("clue count", "attribute count", "indirectness", "grid size", "constraint depth"), listOf("logic", "deduction", "planning", "attention", "untimed")),
        BlueprintGame("ROTATION_RUSH", "Rotation Rush", "Spatial Reasoning", "rotation-rush", listOf("shape complexity", "rotation angle", "mirror traps", "time pressure", "3D cueing"), listOf("spatial", "visual", "rotation", "speed", "timed")),
        BlueprintGame("NBACK", "N-Back Lab", "Working Memory", "n-back-starter", listOf("n depth", "stream length", "stimulus types", "dual stream", "lure frequency"), listOf("memory", "working-memory", "attention", "sequence", "timed")),
        BlueprintGame("STROOP_SHIFT", "Stroop Shift", "Cognitive Flexibility", "stroop-shift", listOf("rule count", "switch frequency", "incongruence", "response window", "distractor load"), listOf("focus", "attention", "cognitive-flexibility", "reaction", "adaptive")),
        BlueprintGame("TARGET_TRACK", "Target Tracker", "Visual Attention", "target-tracker", listOf("target count", "distractor count", "movement speed", "occlusion", "rule changes"), listOf("focus", "attention", "visual", "reaction", "timed")),
        BlueprintGame("CODE_BREAKER", "Code Breaker", "Logical Deduction", "code-breaker", listOf("code length", "symbol set", "clue count", "positional feedback", "deduction depth"), listOf("logic", "deduction", "memory", "planning", "adaptive")),
        BlueprintGame("VIS_MATRIX", "Visual Matrix", "Abstract Reasoning", "visual-matrix", listOf("matrix size", "rule layers", "shape features", "rotation", "position changes"), listOf("spatial", "visual", "patterns", "logic", "untimed")),
        BlueprintGame("EQUATION_BUILD", "Equation Builder", "Mathematical Reasoning", "equation-builder", listOf("operator set", "number pool", "target structure", "order constraints", "time pressure"), listOf("math", "logic", "numbers", "planning", "untimed")),
        BlueprintGame("CUBE_NET", "Cube Net", "3D Spatial Reasoning", "cube-net", listOf("net complexity", "opposite faces", "rotation", "invalid traps", "feature tracking"), listOf("spatial", "visual", "logic", "planning", "untimed")),
        BlueprintGame("ROUTE_PLAN", "Route Planner", "Planning", "route-planner", listOf("board size", "obstacles", "cost rules", "objective count", "lookahead depth"), listOf("spatial", "planning", "logic", "sequence", "untimed")),
        BlueprintGame("RECALL_CHAIN", "Recall Chain", "Working Memory", "recall-chain", listOf("item count", "item types", "order transforms", "interference", "recall mode"), listOf("memory", "sequence", "words", "numbers", "timed")),
        BlueprintGame("ARG_ANALYSER", "Argument Analyser", "Critical Reasoning", "argument-analyser", listOf("passage density", "premise count", "assumption depth", "distractor subtlety", "evidence matching"), listOf("reading", "verbal", "logic", "deduction", "untimed")),
        BlueprintGame("GRAND_GAUNTLET", "Grand Gauntlet", "Mixed Intelligence", "grand-gauntlet", listOf("category mix", "round count", "tier spread", "rule switching", "fatigue control"), listOf("logic", "math", "memory", "spatial", "expert")),
    )

    val stages: List<BlueprintStage> = games.flatMap { game ->
        stageNames.mapIndexed { index, stageName ->
            val level = index + 1
            BlueprintStage(
                workbookId = game.workbookId,
                level = level,
                stage = stageName,
                designIntent = intentFor(level),
                exactDesign = designFor(game, level),
                sessionShape = sessionShapeFor(level),
                hints = hintsFor(level),
                unlockRule = unlockRuleFor(level),
                masteryTarget = masteryTargetFor(level),
                scoreMultiplier = 1.0 + index * 0.12,
            )
        }
    }

    fun stagesForCatalogGame(catalogId: String): List<BlueprintStage> =
        games.firstOrNull { it.catalogId == catalogId }?.let { game -> stages.filter { it.workbookId == game.workbookId } }.orEmpty()

    fun gameForCatalogId(catalogId: String): BlueprintGame? = games.firstOrNull { it.catalogId == catalogId }

    fun stageFor(catalogId: String, level: Int): BlueprintStage? =
        stagesForCatalogGame(catalogId).firstOrNull { it.level == level.coerceIn(1, 12) }

    fun mappedCatalogGamesArePlayable(): Boolean =
        games.all { blueprint -> IqCatalog.game(blueprint.catalogId).status.name == "Playable" }

    private fun intentFor(level: Int): String = when (level) {
        1 -> "Teach the core interaction and establish a low-pressure baseline."
        in 2..4 -> "Increase one primary cognitive load dimension while keeping the rule stable."
        in 5..6 -> "Expand information load and introduce controlled interference."
        in 7..8 -> "Combine two rules and add moderate time or attention pressure."
        in 9..10 -> "Require multi-step reasoning or transformation with reduced assistance."
        else -> "Use reproducible mastery seeds with minimal help and strict validation."
    }

    private fun designFor(game: BlueprintGame, level: Int): String {
        val axis = game.difficultyAxes[(level - 1).coerceAtLeast(0) % game.difficultyAxes.size]
        val secondary = game.difficultyAxes[level % game.difficultyAxes.size]
        return "Level $level ${game.gameName}: raise $axis, tune $secondary, and keep answers deterministic from the session seed."
    }

    private fun sessionShapeFor(level: Int): String = when (level) {
        in 1..2 -> "8-10 rounds; target session 2-3 min; early exit only by user choice."
        in 3..6 -> "10-12 rounds; target session 2-5 min; feedback after each round."
        in 7..9 -> "12-15 rounds; target session 3-6 min; difficulty changes only between rounds."
        else -> "15-20 rounds; target session 4-7 min; reproducible mastery seed available."
    }

    private fun hintsFor(level: Int): String = when (level) {
        1 -> "Guided tutorial and unlimited practice hints; hints disable competitive score."
        in 2..3 -> "One contextual hint per session with score penalty."
        in 4..8 -> "One optional hint or replay where the mechanic permits, with stronger score penalty."
        else -> "No automatic hint; mastery attempts use minimal assistance."
    }

    private fun unlockRuleFor(level: Int): String =
        if (level == 1) "Available when the game is unlocked." else "Earn at least 2 stars on Level ${level - 1}."

    private fun masteryTargetFor(level: Int): String = when (level) {
        in 1..3 -> "Complete 2 sessions and achieve at least ${72 + level * 2}% accuracy."
        in 4..8 -> "Complete 3 sessions and achieve at least ${74 + level * 2}% accuracy with no more than one hint."
        else -> "Complete 3 valid sessions with median accuracy at least ${80 + level}% and meet the efficiency target."
    }
}
