package com.indianservers.iqlabs

import com.indianservers.iqlabs.game.AchievementSystem
import com.indianservers.iqlabs.game.AdaptiveDifficultyEngine
import com.indianservers.iqlabs.game.BackupSystem
import com.indianservers.iqlabs.game.ChallengeResult
import com.indianservers.iqlabs.game.ChallengeSystem
import com.indianservers.iqlabs.game.DailyPlanGenerator
import com.indianservers.iqlabs.game.DifficultyMode
import com.indianservers.iqlabs.game.LevelBlueprint
import com.indianservers.iqlabs.game.PerformanceSample
import com.indianservers.iqlabs.game.ProgressBackup
import com.indianservers.iqlabs.game.QuestionFactory
import com.indianservers.iqlabs.game.RecommendationEngine
import com.indianservers.iqlabs.game.ScoreEngine
import com.indianservers.iqlabs.game.SessionMetric
import com.indianservers.iqlabs.game.SkillModel
import com.indianservers.iqlabs.game.MasterySystem
import com.indianservers.iqlabs.game.UnlockSystem
import com.indianservers.iqlabs.model.Category
import com.indianservers.iqlabs.model.ImplementationStatus
import com.indianservers.iqlabs.model.IqCatalog
import com.indianservers.iqlabs.model.LevelId
import java.time.LocalDate
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogAndEngineTest {
    @Test
    fun catalogueContainsAtLeast150UniqueGames() {
        assertTrue(IqCatalog.games.size >= 150)
        assertEquals(IqCatalog.games.size, IqCatalog.games.map { it.id }.toSet().size)
    }

    @Test
    fun eachLevelContainsAtLeast30Games() {
        LevelId.entries.forEach { level ->
            assertTrue("${level.title} should have at least 30 games", IqCatalog.levelGames(level).size >= 30)
        }
    }

    @Test
    fun everyGameHasRequiredMetadataAndValidTags() {
        IqCatalog.games.forEach { game ->
            assertTrue(game.name.isNotBlank())
            assertTrue(game.description.length > 24)
            assertTrue(game.instructions.isNotBlank())
            assertTrue(game.tags.size in 3..6)
            assertEquals(game.tags.size, game.tags.distinct().size)
            assertTrue(game.difficulty >= 1)
            assertTrue(game.durationMinutes >= 3)
            assertTrue(game.xpReward > 0)
            assertNotNull(game.bestScoreType)
        }
    }

    @Test
    fun sixPlayableGamesGenerateQuestions() {
        val playable = IqCatalog.games.filter { it.status == ImplementationStatus.Playable }
        assertTrue(playable.size >= 60)
        playable.forEach { game ->
            val questions = QuestionFactory.questionsFor(game, seed = 42, count = 6)
            assertEquals(6, questions.size)
            questions.forEach { question ->
                assertTrue(question.prompt.isNotBlank())
                assertTrue(question.choices.size >= 2)
                assertTrue(question.choices.contains(question.answer))
                assertEquals(question.choices.size, question.choices.distinct().size)
            }
        }
    }

    @Test
    fun phase2ExplorerAndChallengerRequiredGamesArePlayable() {
        val required = explorerIds + challengerIds
        val byId = IqCatalog.games.associateBy { it.id }
        required.forEach { id ->
            assertTrue("Missing required game $id", byId.containsKey(id))
            assertEquals("Required game $id must be playable", ImplementationStatus.Playable, byId[id]?.status)
        }
        assertTrue(IqCatalog.levelGames(LevelId.Explorer).count { it.status == ImplementationStatus.Playable } >= 30)
        assertTrue(IqCatalog.levelGames(LevelId.Challenger).count { it.status == ImplementationStatus.Playable } >= 30)
    }

    @Test
    fun phase3ThinkerRequiredGamesArePlayable() {
        val byId = IqCatalog.games.associateBy { it.id }
        thinkerIds.forEach { id ->
            assertTrue("Missing Thinker game $id", byId.containsKey(id))
            assertEquals("Thinker game $id must be playable", ImplementationStatus.Playable, byId[id]?.status)
            assertEquals(LevelId.Thinker, byId[id]?.level)
        }
        assertTrue(IqCatalog.levelGames(LevelId.Thinker).count { it.status == ImplementationStatus.Playable } >= 30)
        assertTrue(IqCatalog.games.count { it.status == ImplementationStatus.Playable } >= 90)
    }

    @Test
    fun phase4MastermindAndGeniusRequiredGamesArePlayable() {
        val byId = IqCatalog.games.associateBy { it.id }
        (mastermindIds + geniusIds).forEach { id ->
            assertTrue("Missing Phase 4 game $id", byId.containsKey(id))
            assertEquals("Phase 4 game $id must be playable", ImplementationStatus.Playable, byId[id]?.status)
        }
        LevelId.entries.forEach { level ->
            assertTrue("$level should have 30 playable games", IqCatalog.levelGames(level).count { it.status == ImplementationStatus.Playable } >= 30)
        }
        assertTrue(IqCatalog.games.count { it.status == ImplementationStatus.Playable } >= 150)
    }

    @Test
    fun allThinkerGeneratorsProduceValidRounds() {
        IqCatalog.levelGames(LevelId.Thinker).filter { it.status == ImplementationStatus.Playable }.forEach { game ->
            val questions = QuestionFactory.questionsFor(game, seed = 99, count = 8)
            assertEquals(8, questions.size)
            questions.forEach {
                assertTrue(it.prompt.isNotBlank())
                assertTrue(it.choices.contains(it.answer))
                assertEquals(it.choices.size, it.choices.distinct().size)
            }
        }
    }

    @Test
    fun allPlayableGamesGenerateValidRounds() {
        IqCatalog.games.filter { it.status == ImplementationStatus.Playable }.forEach { game ->
            val questions = QuestionFactory.questionsFor(game, seed = game.id.hashCode(), count = 5, tier = game.difficulty.coerceIn(1, 6))
            assertEquals(game.id, 5, questions.size)
            questions.forEach { question ->
                assertTrue("${game.id} prompt", question.prompt.isNotBlank())
                assertTrue("${game.id} answer in choices", question.choices.contains(question.answer))
                assertEquals("${game.id} unique choices", question.choices.size, question.choices.distinct().size)
            }
        }
    }

    @Test
    fun workbookBlueprintHasTwentyGamesAndTwelveLevelsEach() {
        assertEquals(20, LevelBlueprint.games.size)
        assertEquals(240, LevelBlueprint.stages.size)
        assertTrue(LevelBlueprint.mappedCatalogGamesArePlayable())
        LevelBlueprint.games.forEach { blueprintGame ->
            val stages = LevelBlueprint.stagesForCatalogGame(blueprintGame.catalogId)
            assertEquals("${blueprintGame.gameName} stage count", 12, stages.size)
            assertEquals((1..12).toList(), stages.map { it.level })
            assertTrue(stages.zipWithNext().all { (a, b) -> b.scoreMultiplier > a.scoreMultiplier })
            stages.forEach { stage ->
                assertTrue(stage.exactDesign.contains(blueprintGame.gameName))
                assertTrue(stage.unlockRule.isNotBlank())
                assertTrue(stage.masteryTarget.isNotBlank())
            }
        }
    }

    @Test
    fun workbookBlueprintMappedGamesGenerateAcrossAllTwelveLevels() {
        LevelBlueprint.games.forEach { blueprintGame ->
            val game = IqCatalog.game(blueprintGame.catalogId)
            (1..12).forEach { level ->
                val questions = QuestionFactory.questionsFor(game, seed = level * 101, count = 4, tier = level)
                assertEquals("${blueprintGame.gameName} L$level", 4, questions.size)
                questions.forEach { question ->
                    assertTrue(question.choices.contains(question.answer))
                    assertEquals(question.choices.size, question.choices.distinct().size)
                }
            }
        }
    }

    @Test
    fun mazeScoutGeneratesDirectionalSwipeRounds() {
        val questions = QuestionFactory.questionsFor(IqCatalog.game("maze-scout"), seed = 17, count = 8, tier = 2)
        val directions = setOf("Up", "Right", "Down", "Left")
        assertEquals(8, questions.size)
        questions.forEach { question ->
            assertTrue(question.prompt.contains("Swipe"))
            assertEquals(directions, question.choices.toSet())
            assertTrue(question.answer in directions)
        }
    }

    @Test
    fun scoringXpAndPersonalBestAreStable() {
        val strong = ScoreEngine.score(correct = 6, incorrect = 0, bestCombo = 6, difficulty = 4, secondsRemaining = 10)
        val weak = ScoreEngine.score(correct = 3, incorrect = 3, bestCombo = 1, difficulty = 4, secondsRemaining = 0)
        assertTrue(strong > weak)
        assertTrue(ScoreEngine.xp(correct = 6, incorrect = 0, difficulty = 4, reward = 120) > 0)
        assertTrue(ScoreEngine.isPersonalBest(500, null))
        assertTrue(ScoreEngine.isPersonalBest(501, 500))
        assertFalse(ScoreEngine.isPersonalBest(499, 500))
    }

    @Test
    fun starsUnlocksDailyPlanAndAchievementsAreFunctional() {
        val game = IqCatalog.game("number-pop")
        assertEquals(3, MasterySystem.starsFor(game, score = 1200, accuracy = 95))
        assertTrue(UnlockSystem.isLevelUnlocked(LevelId.Explorer, xp = 0, explorerCompleted = 0))
        assertTrue(UnlockSystem.isLevelUnlocked(LevelId.Challenger, xp = 300, explorerCompleted = 0))
        val plan = DailyPlanGenerator.generate(LocalDate.of(2026, 8, 19), xp = 400)
        assertTrue(plan.games.size in 4..5)
        assertTrue(plan.estimatedMinutes in 8..12)
        assertTrue(plan.games.all { it.status == ImplementationStatus.Playable })
        val achievements = AchievementSystem.achievements(xp = 1600, sessions = 10, streak = 5)
        assertTrue(achievements.size >= 75)
        assertTrue(achievements.any { it.unlocked })
    }

    @Test
    fun challengeDefinitionsAreDeterministicAndShareCodesRoundTrip() {
        val date = LocalDate.of(2026, 8, 19)
        val daily = ChallengeSystem.dailyArena(date)
        assertEquals(daily, ChallengeSystem.dailyArena(date))
        val weekly = ChallengeSystem.weeklyCup(date)
        assertEquals(weekly, ChallengeSystem.weeklyCup(date))
        val code = ChallengeSystem.shareCode(daily)
        val imported = ChallengeSystem.importCode(code).getOrThrow()
        assertEquals(daily.rawWithoutChecksum(), imported.rawWithoutChecksum())
        assertTrue(ChallengeSystem.importCode(code.dropLast(1) + "0").isFailure)
        assertTrue(ChallengeSystem.importCode("IQL4:" + "a".repeat(400)).isFailure)
    }

    @Test
    fun challengeScoringStandingsAndLeagueAreStable() {
        val definition = ChallengeSystem.grandGauntlet(88)
        val score = ChallengeSystem.challengeScore(correct = 8, incorrect = 1, bestCombo = 5, tier = 5, validSeconds = 30)
        assertTrue(score > 0)
        assertTrue(runCatching { ChallengeSystem.challengeScore(1, 0, 1, 1, -1) }.isFailure)
        val standings = ChallengeSystem.standings(
            listOf(
                ChallengeResult(definition, "B", 500, 90, true),
                ChallengeResult(definition, "A", 700, 80, true),
            )
        )
        assertEquals("A", standings.first().playerName)
        assertEquals(ChallengeSystem.leagueFor(12000).name, "Platinum")
    }

    @Test
    fun backupExportPreviewRejectsMalformedInput() {
        val backup = ProgressBackup(
            appVersion = "1.0",
            exportedAt = Instant.parse("2026-08-19T00:00:00Z"),
            xp = 1200,
            streak = 4,
            sessions = 12,
            favorites = setOf("flash-sum", "grand-gauntlet"),
            bestScores = mapOf("flash-sum" to 900),
            difficultyMode = DifficultyMode.Adaptive,
        )
        val payload = BackupSystem.export(backup)
        val imported = BackupSystem.previewImport(payload).getOrThrow()
        assertEquals(backup.xp, imported.xp)
        assertEquals(backup.favorites, imported.favorites)
        assertTrue(BackupSystem.previewImport(payload.replace("xp=1200", "xp=-1")).isFailure)
        assertTrue(BackupSystem.previewImport("not a backup").isFailure)
        assertTrue(BackupSystem.previewImport("IQLAB-BACKUP-v1\n" + "x".repeat(70_000)).isFailure)
    }

    @Test
    fun adaptiveDifficultyUsesEvidenceBoundsAndModes() {
        val game = IqCatalog.game("rapid-equations")
        val profile = AdaptiveDifficultyEngine.initialProfile(game)
        val unchanged = AdaptiveDifficultyEngine.adapt(profile, List(4) { PerformanceSample(accuracy = 95, medianResponseMs = 2500) })
        assertEquals(profile.tier, unchanged.tier)

        val raised = AdaptiveDifficultyEngine.adapt(profile, List(5) { PerformanceSample(accuracy = 92, medianResponseMs = 2600, bestCombo = 4) })
        assertEquals((profile.tier + 1).coerceAtMost(12), raised.tier)
        assertTrue(raised.lastReason.contains("Raised"))

        val lowered = AdaptiveDifficultyEngine.adapt(profile, List(5) { PerformanceSample(accuracy = 45, medianResponseMs = 7000, completed = false) })
        assertEquals((profile.tier - 1).coerceAtLeast(1), lowered.tier)

        val fixed = AdaptiveDifficultyEngine.adapt(profile.copy(mode = DifficultyMode.Fixed), List(5) { PerformanceSample(accuracy = 98, medianResponseMs = 1000) })
        assertEquals(profile.tier, fixed.tier)

        val relaxedParams = AdaptiveDifficultyEngine.parametersFor(game, profile.copy(mode = DifficultyMode.Relaxed))
        assertTrue(relaxedParams.responseWindowSeconds >= 10)
    }

    @Test
    fun skillAnalyticsAndRecommendationsUseRealMetrics() {
        val metrics = listOf(
            SessionMetric("rapid-equations", LocalDate.of(2026, 8, 18), LevelId.Thinker, Category.Math, 4, 900, 82, 3500, 120, 2),
            SessionMetric("working-memory-mix", LocalDate.of(2026, 8, 19), LevelId.Thinker, Category.Memory, 4, 760, 61, 5200, 95, 1),
            SessionMetric("reading-burst", LocalDate.of(2026, 8, 19), LevelId.Thinker, Category.Reading, 4, 840, 74, 4100, 100, 2),
        )
        val observations = metrics.flatMap { SkillModel.observation(IqCatalog.game(it.gameId), it) }
        val aggregates = SkillModel.aggregate(observations)
        assertEquals(12, aggregates.size)
        assertTrue(aggregates.any { it.confidence == "Early estimate" || it.confidence == "Not enough data" })

        val recommendations = RecommendationEngine.recommend(xp = 2000, recent = metrics, limit = 4)
        assertEquals(4, recommendations.size)
        assertTrue(recommendations.all { it.game.status == ImplementationStatus.Playable })
        assertTrue(recommendations.all { it.reason.isNotBlank() })
    }

    private val explorerIds = listOf(
        "number-pop", "shape-match", "colour-catch", "memory-grid-junior", "picture-pairs", "count-the-stars",
        "bigger-or-smaller", "missing-number-explorer", "pattern-train", "odd-one-out-junior", "shadow-match",
        "direction-dash", "quick-tap", "simon-colours", "animal-order", "maze-scout", "word-and-picture",
        "letter-hunt", "rhyme-time", "first-sound", "tiny-word-sprint", "story-snapshot", "flash-count",
        "simple-sum", "take-away", "number-balance", "shape-builder", "size-sort", "safe-path", "focus-friend"
    )

    private val challengerIds = listOf(
        "flash-sum", "memory-grid", "sequence-detective", "word-sprint", "pattern-pulse", "quick-match",
        "mental-math-rush", "equation-builder", "number-matrix", "target-total", "estimate-it", "fraction-match",
        "logic-gates", "deduction-cards", "rule-switch", "symbol-sequence", "rotation-match", "mirror-mind",
        "cube-counter", "route-planner", "n-back-starter", "dual-focus", "distraction-filter", "reaction-choice",
        "word-link", "analogy-starter", "category-sort", "scrambled-words", "reading-scan", "recall-chain"
    )

    private val thinkerIds = listOf(
        "rapid-equations", "number-pyramid", "target-operations", "percentage-pulse", "fraction-forge",
        "algebra-balance", "prime-patrol", "sequence-fusion", "number-matrix-thinker", "mental-chain",
        "logic-grid", "syllogism-lab", "code-breaker", "conditional-logic", "truth-detectives",
        "rule-discovery", "rotation-rush", "cube-net", "spatial-fold", "hidden-shape",
        "mirror-maze-thinker", "visual-matrix", "working-memory-mix", "reverse-recall",
        "memory-interference", "dual-n-back", "target-tracker", "stroop-shift", "reading-burst",
        "verbal-connections"
    )

    private val mastermindIds = listOf(
        "equation-cascade", "number-web", "modular-mind", "ratio-relay", "sequence-architect",
        "magic-matrix", "countdown-solver", "quantitative-comparison", "arithmetic-logic-grid", "equation-switch",
        "deduction-vault", "constraint-circuit", "knights-and-liars", "boolean-builder", "set-logic",
        "logic-grid-pro", "rule-network", "schedule-solver", "three-d-rotation", "cube-assembly",
        "paper-fold-pro", "spatial-sequence", "perspective-match", "tangram-logic", "dual-n-back-pro",
        "interference-shift", "multi-target-tracker", "critical-reading", "argument-analyser", "word-matrix"
    )

    private val geniusIds = listOf(
        "operator-cipher", "alphametic-vault", "number-theory-lab", "recursive-sequence", "equation-maze",
        "combinatorial-count", "probability-instinct", "balance-scale", "optimisation-route", "matrix-elite",
        "logic-grid-elite", "cipher-master", "paradox-lab", "conditional-chain", "truth-network",
        "set-intersection-elite", "resource-planner", "grand-deduction", "cube-slice", "block-projection",
        "folding-elite", "rotation-matrix", "visual-analogy-elite", "topology-paths", "triple-n-back",
        "working-memory-update", "memory-under-interference", "divided-attention-elite", "rapid-inference", "grand-gauntlet"
    )
}
