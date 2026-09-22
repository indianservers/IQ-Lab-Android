package com.indianservers.iqlabs.game

import com.indianservers.iqlabs.model.Category
import com.indianservers.iqlabs.model.GameDefinition
import kotlin.math.max

enum class PlayStyle {
    Reading, Keypad, Swipe, GoNoGo, Targets, Grid, Sequence, Tiles,
    Reveal, Match, Wait, Search, Peripheral, SymbolKey, Sudoku, Tower, Trail, Hold, Shade,
    CodeBreaker, Circuit, MultiSelect, FocusTrack, StudyChoice, Matrix, Deduction, GridPlacement,
}

data class Question(
    val prompt: String,
    val choices: List<String>,
    val answer: String,
    val detail: String,
    val play: PlayStyle? = null,
    val field: List<String> = emptyList(),
    /** Stable identity for this generated round, independent of repeated visible prompts. */
    val roundKey: String = "",
)

data class GameResult(
    val score: Int,
    val correct: Int,
    val incorrect: Int,
    val xp: Int,
    val bestCombo: Int,
    val personalBest: Boolean,
    val stars: Int = 0,
    val averageResponseMs: Int = 0,
) {
    val accuracy: Int = if (correct + incorrect == 0) 0 else correct * 100 / (correct + incorrect)
}

object ScoreEngine {
    fun score(correct: Int, incorrect: Int, bestCombo: Int, difficulty: Int, secondsRemaining: Int): Int {
        val base = correct * 100
        val combo = bestCombo * 18
        val speed = max(0, secondsRemaining) * 3
        val penalty = incorrect * 28
        return max(0, (base + combo + speed - penalty) * (100 + difficulty * 12) / 100)
    }

    fun xp(correct: Int, incorrect: Int, difficulty: Int, reward: Int): Int {
        val total = correct + incorrect
        val accuracy = if (total == 0) 0 else correct * 100 / total
        return max(5, reward * (50 + accuracy + difficulty * 6) / 160)
    }

    fun isPersonalBest(newScore: Int, previousBest: Int?): Boolean = previousBest == null || newScore > previousBest
}

object QuestionFactory {
    fun questionsFor(game: GameDefinition, seed: Int, count: Int = 6, tier: Int = game.difficulty): List<Question> {
        val effectiveTier = tier.coerceIn(1, LevelBlueprint.maxLevelFor(game.id))
        return GameQuestions.create(game, seed, count, effectiveTier)
    }
}

object PlayStyleResolver {
    private val directions = setOf("Up", "Right", "Down", "Left")

    fun forRound(game: GameDefinition, question: Question): PlayStyle {
        question.play?.let { style ->
            return style
        }
        if (game.category == Category.Reading) return PlayStyle.Reading
        if (question.choices.toSet() == directions || question.prompt.contains("Swipe", ignoreCase = true)) return PlayStyle.Swipe
        if (question.choices.size == 2) return PlayStyle.GoNoGo
        if (game.category == Category.Memory && question.answer.matches(Regex("\\d+(?:-\\d+)+"))) return PlayStyle.Grid
        if (question.answer.contains(" → ")) return PlayStyle.Sequence
        if (game.category == Category.Focus || game.category == Category.Speed) return PlayStyle.Targets
        if (question.field.isNotEmpty()) return PlayStyle.Targets
        if (question.answer.toIntOrNull() != null && game.category == Category.Math) return PlayStyle.Keypad
        return PlayStyle.Targets
    }

    fun cue(game: GameDefinition, question: Question): String =
        question.prompt.removePrefix("${game.name}:").removePrefix(game.name).trim(':', ' ', '\n')
}
