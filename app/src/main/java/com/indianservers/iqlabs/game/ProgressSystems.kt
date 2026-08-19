package com.indianservers.iqlabs.game

import com.indianservers.iqlabs.model.Category
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.ImplementationStatus
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import java.time.LocalDate
import kotlin.random.Random

data class MasteryRule(val oneStarScore: Int, val twoStarAccuracy: Int, val threeStarScore: Int, val threeStarAccuracy: Int)
enum class MasteryRank { None, Bronze, Silver, Gold, Diamond }
data class DailyTrainingPlan(val date: LocalDate, val games: List<GameDefinition>, val estimatedMinutes: Int, val bonusXp: Int)
data class AchievementDefinition(val id: String, val title: String, val unlocked: Boolean, val progress: Int, val target: Int)

object MasterySystem {
    fun ruleFor(game: GameDefinition): MasteryRule {
        val base = 260 + game.difficulty * 90
        return MasteryRule(
            oneStarScore = base,
            twoStarAccuracy = if (game.level == LevelId.Explorer) 65 else 72,
            threeStarScore = base + 420 + game.difficulty * 60,
            threeStarAccuracy = if (game.level == LevelId.Explorer) 85 else 90,
        )
    }

    fun starsFor(game: GameDefinition, score: Int, accuracy: Int): Int {
        val rule = ruleFor(game)
        return when {
            score >= rule.threeStarScore && accuracy >= rule.threeStarAccuracy -> 3
            score >= rule.oneStarScore && accuracy >= rule.twoStarAccuracy -> 2
            score > 0 -> 1
            else -> 0
        }
    }

    fun rankFor(game: GameDefinition, score: Int, accuracy: Int, validSessions: Int): MasteryRank {
        val rule = ruleFor(game)
        return when {
            validSessions >= 8 && score >= rule.threeStarScore + 900 && accuracy >= 95 -> MasteryRank.Diamond
            validSessions >= 5 && score >= rule.threeStarScore + 500 && accuracy >= 92 -> MasteryRank.Gold
            validSessions >= 3 && score >= rule.threeStarScore && accuracy >= rule.threeStarAccuracy -> MasteryRank.Silver
            validSessions >= 1 && score >= rule.oneStarScore -> MasteryRank.Bronze
            else -> MasteryRank.None
        }
    }
}

object UnlockSystem {
    fun isLevelUnlocked(level: LevelId, xp: Int, explorerCompleted: Int): Boolean =
        when (level) {
            LevelId.Explorer -> true
            LevelId.Challenger -> xp >= 300 || explorerCompleted >= 5
            else -> xp >= level.minXp
        }

    fun isGameUnlocked(game: GameDefinition, xp: Int, completedInLevel: Int): Boolean {
        if (!isLevelUnlocked(game.level, xp, if (game.level == LevelId.Challenger) 5 else completedInLevel)) return false
        val slot = IqCatalog.levelGames(game.level).indexOfFirst { it.id == game.id }
        return slot < 6 || completedInLevel >= slot / 3
    }
}

object DailyPlanGenerator {
    fun generate(date: LocalDate, xp: Int, recentlyPlayed: Set<String> = emptySet()): DailyTrainingPlan {
        val random = Random(date.toEpochDay().toInt() xor xp)
        val unlockedLevels = if (UnlockSystem.isLevelUnlocked(LevelId.Challenger, xp, explorerCompleted = 5)) {
            setOf(LevelId.Explorer, LevelId.Challenger)
        } else {
            setOf(LevelId.Explorer)
        }
        val candidates = IqCatalog.games
            .filter { it.status == ImplementationStatus.Playable && it.level in unlockedLevels && it.id !in recentlyPlayed }
            .shuffled(random)
        val picked = mutableListOf<GameDefinition>()
        Category.entries.forEach { category ->
            candidates.firstOrNull { it.category == category && picked.none { pickedGame -> pickedGame.category == category } }?.let(picked::add)
            if (picked.size == 5) return@forEach
        }
        candidates.forEach { if (picked.size < 4 && it !in picked) picked.add(it) }
        return DailyTrainingPlan(
            date = date,
            games = picked.take(5),
            estimatedMinutes = picked.take(5).sumOf { it.durationMinutes }.coerceIn(8, 12),
            bonusXp = 90 + picked.take(5).size * 12,
        )
    }
}

object AchievementSystem {
    fun achievements(xp: Int, sessions: Int, streak: Int, explorerStars: Int = 0, challengerStars: Int = 0, thinkerStars: Int = 0, mastermindStars: Int = 0, geniusStars: Int = 0, challengeCompletions: Int = 0): List<AchievementDefinition> {
        val specs = listOf(
            "first-session" to Triple("First Session", sessions, 1),
            "five-sessions" to Triple("Five Sessions", sessions, 5),
            "ten-games" to Triple("Ten Games Played", sessions, 10),
            "twenty-games" to Triple("Twenty Sessions", sessions, 20),
            "five-day-streak" to Triple("Five-Day Streak", streak, 5),
            "week-streak" to Triple("Seven-Day Spark", streak, 7),
            "xp-500" to Triple("XP Starter", xp, 500),
            "xp-1500" to Triple("Lab Regular", xp, 1500),
            "xp-3000" to Triple("Training Driver", xp, 3000),
            "explorer-3" to Triple("Explorer Path", explorerStars, 3),
            "explorer-15" to Triple("Explorer Collector", explorerStars, 15),
            "explorer-45" to Triple("Explorer Mastery", explorerStars, 45),
            "challenger-3" to Triple("Challenger Entry", challengerStars, 3),
            "challenger-15" to Triple("Challenger Builder", challengerStars, 15),
            "challenger-45" to Triple("Challenger Mastery", challengerStars, 45),
            "math-steps" to Triple("Math Steps", sessions + xp / 200, 8),
            "memory-steps" to Triple("Memory Starter", sessions, 2),
            "logic-steps" to Triple("Logic Ladder", sessions + explorerStars, 12),
            "reading-steps" to Triple("Reading Practice", sessions, 4),
            "focus-steps" to Triple("Focus Builder", sessions + streak, 10),
            "reaction-steps" to Triple("Quick Hands", sessions, 6),
            "accuracy-70" to Triple("Steady Accuracy", xp / 20, 70),
            "accuracy-90" to Triple("Sharp Accuracy", xp / 18, 90),
            "combo-3" to Triple("Combo Spark", sessions, 3),
            "combo-6" to Triple("Combo Master", sessions, 6),
            "daily-one" to Triple("Daily Plan Started", sessions, 1),
            "daily-five" to Triple("Daily Rhythm", streak + sessions, 8),
            "multi-talent" to Triple("Multi-Talent", sessions + explorerStars + challengerStars, 15),
            "personal-best" to Triple("New Best Energy", xp / 100, 5),
            "three-star" to Triple("Three-Star Chase", explorerStars + challengerStars, 3),
        ) + listOf(
            "thinker-3" to Triple("Thinker Entry", thinkerStars, 3),
            "thinker-30" to Triple("Thinker Analyst", thinkerStars, 30),
            "thinker-75" to Triple("Thinker Mastery", thinkerStars, 75),
            "mastermind-3" to Triple("Mastermind Entry", mastermindStars, 3),
            "mastermind-30" to Triple("Mastermind Strategist", mastermindStars, 30),
            "mastermind-75" to Triple("Mastermind Mastery", mastermindStars, 75),
            "genius-3" to Triple("Genius Entry", geniusStars, 3),
            "genius-30" to Triple("Genius Builder", geniusStars, 30),
            "genius-75" to Triple("Genius Mastery", geniusStars, 75),
            "daily-arena" to Triple("Daily Arena", challengeCompletions, 1),
            "weekly-cup" to Triple("Weekly Cup", challengeCompletions, 2),
            "category-sprint" to Triple("Category Sprint", challengeCompletions, 3),
            "pass-and-play" to Triple("Pass-and-Play Duel", challengeCompletions, 4),
            "grand-gauntlet" to Triple("Grand Gauntlet", challengeCompletions, 5),
            "challenge-10" to Triple("Ten Challenges", challengeCompletions, 10),
            "catalogue-50" to Triple("Fifty-Game Tour", sessions, 50),
            "catalogue-100" to Triple("Hundred-Game Tour", sessions, 100),
            "catalogue-150" to Triple("Full Lab Tour", sessions, 150),
            "mastery-bronze" to Triple("Bronze Mastery", explorerStars + challengerStars + thinkerStars + mastermindStars + geniusStars, 10),
            "mastery-silver" to Triple("Silver Mastery", explorerStars + challengerStars + thinkerStars + mastermindStars + geniusStars, 30),
            "mastery-gold" to Triple("Gold Mastery", explorerStars + challengerStars + thinkerStars + mastermindStars + geniusStars, 60),
            "mastery-diamond" to Triple("Diamond Mastery", explorerStars + challengerStars + thinkerStars + mastermindStars + geniusStars, 100),
            "all-levels-open" to Triple("All Levels Open", xp, 5600),
            "advanced-accuracy" to Triple("Advanced Accuracy", xp / 16, 90),
            "expert-accuracy" to Triple("Expert Accuracy", xp / 14, 95),
            "violet-path" to Triple("Violet Path", mastermindStars + sessions, 12),
            "ruby-path" to Triple("Ruby Path", geniusStars + sessions, 12),
            "weekly-rhythm" to Triple("Weekly Rhythm", streak + challengeCompletions, 9),
            "score-5000" to Triple("Five Thousand Energy", xp, 5000),
            "score-10000" to Triple("Ten Thousand Energy", xp, 10000),
            "balanced-training" to Triple("Balanced Training", sessions + challengeCompletions, 20),
            "speed-focus" to Triple("Speed Focus", sessions, 15),
            "spatial-lab" to Triple("Spatial Lab", sessions + thinkerStars, 18),
            "logic-core" to Triple("Logic Core", sessions + mastermindStars, 20),
            "reading-arc" to Triple("Reading Arc", sessions + geniusStars, 20),
            "memory-arc" to Triple("Memory Arc", sessions + explorerStars, 20),
            "quant-arc" to Triple("Quant Arc", sessions + challengerStars, 20),
            "adaptive-step" to Triple("Adaptive Step", sessions, 7),
            "adaptive-strong" to Triple("Adaptive Strong", sessions, 21),
            "share-code" to Triple("Share-Code Ready", challengeCompletions, 1),
            "local-league" to Triple("Local League", challengeCompletions, 2),
            "diamond-league" to Triple("Diamond League", xp, 16000),
            "steady-habit" to Triple("Steady Habit", streak, 14),
            "calm-practice" to Triple("Calm Practice", sessions, 8),
            "precision-run" to Triple("Precision Run", xp / 12, 90),
            "variety-seven" to Triple("Seven-Skill Variety", sessions, 7),
        )
        return specs.map { (id, spec) ->
            AchievementDefinition(id, spec.first, spec.second >= spec.third, spec.second.coerceAtMost(spec.third), spec.third)
        }
    }
}
