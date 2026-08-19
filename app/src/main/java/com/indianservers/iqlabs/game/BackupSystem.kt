package com.indianservers.iqlabs.game

import java.time.Instant

data class ProgressBackup(
    val schemaVersion: Int = 1,
    val appVersion: String,
    val exportedAt: Instant,
    val xp: Int,
    val streak: Int,
    val sessions: Int,
    val favorites: Set<String>,
    val bestScores: Map<String, Int>,
    val difficultyMode: DifficultyMode,
)

object BackupSystem {
    private const val PREFIX = "IQLAB-BACKUP-v1"
    private const val MAX_BYTES = 64 * 1024

    fun export(backup: ProgressBackup): String {
        require(backup.xp >= 0 && backup.streak >= 0 && backup.sessions >= 0) { "Progress values must be non-negative." }
        val body = listOf(
            "app=${escape(backup.appVersion)}",
            "at=${backup.exportedAt}",
            "xp=${backup.xp}",
            "streak=${backup.streak}",
            "sessions=${backup.sessions}",
            "mode=${backup.difficultyMode.name}",
            "favorites=${backup.favorites.sorted().joinToString(";") { escape(it) }}",
            "best=${backup.bestScores.entries.sortedBy { it.key }.joinToString(";") { escape(it.key) + ":" + it.value }}",
        ).joinToString("\n")
        val checksum = checksum(body)
        val payload = "$PREFIX\n$body\nchecksum=$checksum"
        require(payload.encodeToByteArray().size <= MAX_BYTES) { "Backup is too large." }
        return payload
    }

    fun previewImport(payload: String): Result<ProgressBackup> = runCatching {
        require(payload.encodeToByteArray().size <= MAX_BYTES) { "Backup file is too large." }
        val lines = payload.lines().filter { it.isNotBlank() }
        require(lines.firstOrNull() == PREFIX) { "Unsupported backup format." }
        val values = lines.drop(1).associate { line ->
            val index = line.indexOf('=')
            require(index > 0) { "Malformed backup field." }
            line.substring(0, index) to line.substring(index + 1)
        }
        val body = lines.drop(1).filterNot { it.startsWith("checksum=") }.joinToString("\n")
        require(values["checksum"] == checksum(body)) { "Backup integrity check failed." }
        ProgressBackup(
            appVersion = unescape(values.getValue("app")),
            exportedAt = Instant.parse(values.getValue("at")),
            xp = values.getValue("xp").toInt().also { require(it >= 0) },
            streak = values.getValue("streak").toInt().also { require(it >= 0) },
            sessions = values.getValue("sessions").toInt().also { require(it >= 0) },
            difficultyMode = DifficultyMode.valueOf(values.getValue("mode")),
            favorites = values["favorites"].orEmpty().split(";").filter { it.isNotBlank() }.map(::unescape).toSet(),
            bestScores = values["best"].orEmpty().split(";").filter { it.isNotBlank() }.associate {
                val pair = it.split(":", limit = 2)
                require(pair.size == 2) { "Malformed best-score row." }
                unescape(pair[0]) to pair[1].toInt().also { score -> require(score >= 0) }
            },
        )
    }

    private fun escape(value: String): String = value.replace("%", "%25").replace(";", "%3B").replace(":", "%3A").replace("\n", "%0A")
    private fun unescape(value: String): String = value.replace("%0A", "\n").replace("%3A", ":").replace("%3B", ";").replace("%25", "%")
    private fun checksum(raw: String): String = raw.fold(0x13572468) { acc, c -> ((acc * 33) xor c.code) and 0x7fffffff }.toString(16)
}
