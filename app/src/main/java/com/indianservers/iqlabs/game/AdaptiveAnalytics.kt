package com.indianservers.iqlabs.game

import com.indianservers.iqlabs.model.Category
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.ImplementationStatus
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import java.time.LocalDate
import kotlin.math.roundToInt

enum class DifficultyMode { Adaptive, Fixed, Relaxed }
enum class SkillArea(val label: String) {
    MathematicalReasoning("Mathematical reasoning"),
    Memory("Memory"),
    WorkingMemory("Working memory"),
    Logic("Logic"),
    VerbalReasoning("Verbal reasoning"),
    Reading("Reading"),
    ProcessingSpeed("Processing speed"),
    Attention("Attention"),
    SpatialReasoning("Spatial reasoning"),
    CognitiveFlexibility("Cognitive flexibility"),
    Planning("Planning"),
    ReactionSpeed("Reaction speed"),
}

data class DifficultyProfile(
    val gameId: String,
    val mode: DifficultyMode = DifficultyMode.Adaptive,
    val tier: Int,
    val previousTier: Int = tier,
    val cooldownRounds: Int = 0,
    val lastReason: String = "Starting from the catalogue-recommended tier.",
)

data class PerformanceSample(
    val accuracy: Int,
    val medianResponseMs: Int,
    val completed: Boolean = true,
    val bestCombo: Int = 0,
)

data class DifficultyParameters(
    val tier: Int,
    val sequenceLength: Int,
    val distractors: Int,
    val responseWindowSeconds: Int,
    val readingWords: Int,
    val numberRange: Int,
)

data class SkillObservation(
    val skill: SkillArea,
    val gameId: String,
    val date: LocalDate,
    val score: Int,
    val accuracy: Int,
    val tier: Int,
)

data class SkillAggregate(
    val skill: SkillArea,
    val observations: Int,
    val level: Int,
    val confidence: String,
    val trend: String,
)

data class SessionMetric(
    val gameId: String,
    val date: LocalDate,
    val level: LevelId,
    val category: Category,
    val difficultyTier: Int,
    val score: Int,
    val accuracy: Int,
    val responseMs: Int,
    val xp: Int,
    val stars: Int,
)

data class Recommendation(val game: GameDefinition, val reason: String)

object AdaptiveDifficultyEngine {
    fun initialProfile(game: GameDefinition, mode: DifficultyMode = DifficultyMode.Adaptive): DifficultyProfile =
        DifficultyProfile(game.id, mode, tier = game.difficulty.coerceIn(1, LevelBlueprint.maxLevelFor(game.id)))

    fun parametersFor(game: GameDefinition, profile: DifficultyProfile): DifficultyParameters {
        val tier = when (profile.mode) {
            DifficultyMode.Relaxed -> (profile.tier - 1).coerceAtLeast(1)
            else -> profile.tier
        }
        return DifficultyParameters(
            tier = tier,
            sequenceLength = 2 + tier + if (game.category == Category.Memory) 1 else 0,
            distractors = 2 + tier,
            responseWindowSeconds = (18 - tier * 2).coerceAtLeast(if (profile.mode == DifficultyMode.Relaxed) 10 else 6),
            readingWords = 24 + tier * 16,
            numberRange = 10 + tier * 12,
        )
    }

    fun adapt(profile: DifficultyProfile, samples: List<PerformanceSample>): DifficultyProfile {
        if (profile.mode != DifficultyMode.Adaptive || samples.size < 5) {
            return profile.copy(lastReason = if (profile.mode == DifficultyMode.Adaptive) "Not enough recent rounds yet." else "Manual difficulty mode is active.")
        }
        if (profile.cooldownRounds > 0) return profile.copy(cooldownRounds = profile.cooldownRounds - 1, lastReason = "Holding steady to avoid rapid difficulty changes.")
        val recent = samples.takeLast(5)
        val averageAccuracy = recent.map { it.accuracy }.average()
        val averageSpeed = recent.map { it.medianResponseMs }.average()
        val completed = recent.count { it.completed }
        val newTier = when {
            averageAccuracy >= 86.0 && averageSpeed <= 4200 && completed >= 5 -> (profile.tier + 1).coerceAtMost(LevelBlueprint.maxLevelFor(profile.gameId))
            averageAccuracy <= 58.0 || completed <= 3 -> (profile.tier - 1).coerceAtLeast(1)
            else -> profile.tier
        }
        val reason = when {
            newTier > profile.tier -> "Raised one tier after strong recent accuracy and speed."
            newTier < profile.tier -> "Lowered one tier after sustained difficulty."
            else -> "Kept steady because recent evidence was mixed."
        }
        return profile.copy(previousTier = profile.tier, tier = newTier, cooldownRounds = if (newTier == profile.tier) 0 else 2, lastReason = reason)
    }

    fun undo(profile: DifficultyProfile): DifficultyProfile =
        profile.copy(tier = profile.previousTier, lastReason = "Restored the previous difficulty tier.")
}

object SkillModel {
    fun skillsFor(game: GameDefinition): List<SkillArea> = when (game.category) {
        Category.Math -> listOf(SkillArea.MathematicalReasoning, SkillArea.Planning, SkillArea.ProcessingSpeed)
        Category.Memory -> listOf(SkillArea.Memory, SkillArea.WorkingMemory, SkillArea.Attention)
        Category.Logic -> listOf(SkillArea.Logic, SkillArea.Planning, SkillArea.CognitiveFlexibility)
        Category.Reading -> listOf(SkillArea.Reading, SkillArea.VerbalReasoning, SkillArea.Attention)
        Category.Focus -> listOf(SkillArea.Attention, SkillArea.CognitiveFlexibility, SkillArea.ReactionSpeed)
        Category.Spatial -> listOf(SkillArea.SpatialReasoning, SkillArea.Logic, SkillArea.Planning)
        Category.Speed -> listOf(SkillArea.ReactionSpeed, SkillArea.ProcessingSpeed, SkillArea.Attention)
    }

    fun observation(game: GameDefinition, metric: SessionMetric): List<SkillObservation> =
        skillsFor(game).map { skill -> SkillObservation(skill, game.id, metric.date, metric.score, metric.accuracy, metric.difficultyTier) }

    fun aggregate(observations: List<SkillObservation>): List<SkillAggregate> =
        SkillArea.entries.map { skill ->
            val skillObservations = observations.filter { it.skill == skill }
            val confidence = when (skillObservations.size) {
                0 -> "Not enough data"
                in 1..2 -> "Early estimate"
                in 3..6 -> "Developing confidence"
                else -> "Established trend"
            }
            val level = if (skillObservations.isEmpty()) 0 else skillObservations.map { it.accuracy + it.tier * 6 }.average().roundToInt().coerceIn(1, 100)
            val trend = if (skillObservations.size < 4) "Needs more sessions" else {
                val ordered = skillObservations.sortedBy { it.date }
                val first = ordered.take(ordered.size / 2).map { it.accuracy }.average()
                val last = ordered.drop(ordered.size / 2).map { it.accuracy }.average()
                when {
                    last - first >= 4 -> "Improving"
                    first - last >= 4 -> "Needs practice"
                    else -> "Steady"
                }
            }
            SkillAggregate(skill, skillObservations.size, level, confidence, trend)
        }
}

object RecommendationEngine {
    fun recommend(xp: Int, recent: List<SessionMetric>, favorites: Set<String> = emptySet(), limit: Int = 5): List<Recommendation> {
        val played = recent.takeLast(8).map { it.gameId }.toSet()
        val weakCategory = recent.groupBy { it.category }.minByOrNull { entry -> entry.value.map { it.accuracy }.average() }?.key
        val playable = IqCatalog.games.filter { it.status == ImplementationStatus.Playable && it.id !in played }
        return playable.sortedWith(
            compareByDescending<GameDefinition> { it.id in favorites }
                .thenByDescending { if (it.category == weakCategory) 1 else 0 }
                .thenBy { if (it.level == LevelId.Thinker && xp >= LevelId.Thinker.minXp) 0 else it.level.ordinal }
        ).take(limit).map { game ->
            Recommendation(
                game,
                when {
                    game.id in favorites -> "Similar to a game you marked as favourite."
                    game.category == weakCategory -> "Practise a category that has been harder recently."
                    game.durationMinutes <= 3 -> "Short three-minute session."
                    game.level == LevelId.Thinker -> "Recommended next intermediate challenge."
                    else -> "Adds variety to today's training."
                }
            )
        }
    }
}
