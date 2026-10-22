package com.indianservers.iqlabs.game

/** Rewards quick, unassisted progress without pressuring slower players. */
class QuickSolvePraise {
    companion object {
        val messages = listOf(
            "Quick thinking! Keep that spark going.",
            "Brilliant move! Your focus is shining.",
            "You spotted it fast. Trust your reasoning!",
            "Sharp eyes, smart move!",
            "Great momentum! One good idea leads to another.",
            "That clicked quickly. Keep exploring!",
            "Beautiful thinking! You're finding your rhythm.",
            "Fast and thoughtful. Nicely done!",
            "A clever step forward! Keep going.",
            "Your focus just paid off. Well done!",
            "What a bright idea! Stay curious.",
            "You made that look smooth. Great work!",
            "A strong move! Your practice is paying off.",
            "You've got a spark for this. Keep it growing!",
        )
    }
    private var next = 0
    private var lastShown: Long? = null
    fun success(elapsedMs: Long, nowMs: Long, assisted: Boolean = false): String? {
        if (assisted || elapsedMs !in 0..8000 || lastShown?.let { nowMs - it < 12000 } == true) return null
        lastShown = nowMs
        val message = messages[next]
        next = (next + 1) % messages.size
        return message
    }
}
