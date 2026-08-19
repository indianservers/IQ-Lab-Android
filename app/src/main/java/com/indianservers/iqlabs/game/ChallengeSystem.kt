package com.indianservers.iqlabs.game

import com.indianservers.iqlabs.model.Category
import com.indianservers.iqlabs.model.GameDefinition
import com.indianservers.iqlabs.model.ImplementationStatus
import com.indianservers.iqlabs.model.IqCatalog
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.random.Random

enum class ChallengeType {
    DailyArena,
    WeeklyCup,
    PersonalBest,
    CategorySprint,
    MasteryChallenge,
    GrandGauntlet,
    PassAndPlayDuel,
    ShareCode,
}

enum class LocalLeague { Bronze, Silver, Gold, Platinum, Diamond }

data class ChallengeDefinition(
    val version: Int = 1,
    val type: ChallengeType,
    val gameIds: List<String>,
    val seed: Int,
    val tier: Int,
    val rounds: Int,
    val timeLimitSeconds: Int,
    val scoringVersion: String = "iq-score-v1",
    val identity: String,
) {
    val checksum: Int = checksum(rawWithoutChecksum())

    fun rawWithoutChecksum(): String = listOf(
        version,
        type.name,
        gameIds.joinToString("."),
        seed,
        tier,
        rounds,
        timeLimitSeconds,
        scoringVersion,
        identity,
    ).joinToString("|")
}

data class ChallengeResult(
    val definition: ChallengeDefinition,
    val playerName: String,
    val score: Int,
    val accuracy: Int,
    val completed: Boolean,
)

object ChallengeSystem {
    private const val MAX_CODE_LENGTH = 360

    fun dailyArena(date: LocalDate = LocalDate.now()): ChallengeDefinition {
        val seed = ("daily-${date}".hashCode()).absoluteValue
        val games = pickGames(seed, 3, null)
        return ChallengeDefinition(1, ChallengeType.DailyArena, games.map { it.id }, seed, 4, 8, 360, identity = date.toString())
    }

    fun weeklyCup(date: LocalDate = LocalDate.now()): ChallengeDefinition {
        val week = date.get(WeekFields.ISO.weekOfWeekBasedYear())
        val year = date.get(WeekFields.ISO.weekBasedYear())
        val identity = "$year-W$week"
        val seed = ("weekly-$identity".hashCode()).absoluteValue
        val games = pickGames(seed, 5, null)
        return ChallengeDefinition(1, ChallengeType.WeeklyCup, games.map { it.id }, seed, 5, 12, 900, identity = identity)
    }

    fun categorySprint(category: Category, date: LocalDate = LocalDate.now()): ChallengeDefinition {
        val seed = ("sprint-${category.name}-$date".hashCode()).absoluteValue
        val games = pickGames(seed, 3, category)
        return ChallengeDefinition(1, ChallengeType.CategorySprint, games.map { it.id }, seed, 4, 6, 240, identity = "${category.name}-$date")
    }

    fun grandGauntlet(seed: Int = 150): ChallengeDefinition {
        val games = Category.entries.flatMap { category -> pickGames(seed + category.ordinal, 1, category) }.take(7)
        return ChallengeDefinition(1, ChallengeType.GrandGauntlet, games.map { it.id }, seed, 5, 15, 1200, identity = "grand-$seed")
    }

    fun shareCode(definition: ChallengeDefinition): String =
        "IQL4:" + (definition.rawWithoutChecksum() + "|" + definition.checksum.toString(16)).encodeToByteArray().joinToString("") { "%02x".format(it) }

    fun importCode(code: String): Result<ChallengeDefinition> = runCatching {
        require(code.length <= MAX_CODE_LENGTH) { "Challenge code is too long." }
        require(code.startsWith("IQL4:")) { "Unsupported challenge code." }
        val hex = code.removePrefix("IQL4:")
        require(hex.length % 2 == 0 && hex.all { it in '0'..'9' || it in 'a'..'f' }) { "Challenge code is malformed." }
        val raw = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray().decodeToString()
        val parts = raw.split("|")
        require(parts.size == 10) { "Challenge payload is incomplete." }
        val withoutChecksum = parts.take(9).joinToString("|")
        require(parts[9].toInt(16) == checksum(withoutChecksum)) { "Challenge checksum does not match." }
        require(parts[0].toInt() == 1) { "Unsupported challenge version." }
        val ids = parts[2].split(".").filter { it.isNotBlank() }
        require(ids.isNotEmpty() && ids.all { id -> IqCatalog.games.any { it.id == id } }) { "Challenge references unknown games." }
        ChallengeDefinition(
            version = parts[0].toInt(),
            type = ChallengeType.valueOf(parts[1]),
            gameIds = ids,
            seed = parts[3].toInt(),
            tier = parts[4].toInt().coerceIn(1, 6),
            rounds = parts[5].toInt().coerceIn(1, 30),
            timeLimitSeconds = parts[6].toInt().coerceIn(30, 1800),
            scoringVersion = parts[7],
            identity = parts[8],
        )
    }

    fun standings(results: List<ChallengeResult>): List<ChallengeResult> =
        results.sortedWith(compareByDescending<ChallengeResult> { it.score }.thenByDescending { it.accuracy }.thenBy { it.playerName.lowercase(Locale.US) })

    fun leagueFor(totalTournamentScore: Int): LocalLeague = when {
        totalTournamentScore >= 16000 -> LocalLeague.Diamond
        totalTournamentScore >= 10000 -> LocalLeague.Platinum
        totalTournamentScore >= 6000 -> LocalLeague.Gold
        totalTournamentScore >= 2500 -> LocalLeague.Silver
        else -> LocalLeague.Bronze
    }

    fun challengeScore(correct: Int, incorrect: Int, bestCombo: Int, tier: Int, validSeconds: Int): Int {
        require(validSeconds >= 0) { "Duration cannot be negative." }
        return ScoreEngine.score(correct, incorrect, bestCombo, tier, validSeconds.coerceAtMost(120))
    }

    private fun pickGames(seed: Int, count: Int, category: Category?): List<GameDefinition> {
        val candidates = IqCatalog.games.filter { it.status == ImplementationStatus.Playable && (category == null || it.category == category) }
        return candidates.shuffled(Random(seed)).take(count)
    }
}

private fun checksum(raw: String): Int =
    raw.fold(0x45D9F3B) { acc, char -> ((acc * 31) xor char.code) and 0x7fffffff }
