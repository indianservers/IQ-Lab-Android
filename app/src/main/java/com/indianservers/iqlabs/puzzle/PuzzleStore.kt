package com.indianservers.iqlabs.puzzle

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PuzzleSession(
    val puzzle: InfinitePuzzle, val mode: PuzzleMode = PuzzleMode.Normal,
    val removed: Set<Int> = emptySet(), val routes: Map<Int, List<Cell>> = emptyMap(),
    val lives: Int = mode.lives, val hints: Int = 3, val hintsUsed: Int = 0,
    val mistakes: Int = 0, val moves: Int = 0, val seconds: Long = 0,
    val practice: Boolean = false,
    val cameraX: Float = 0f, val cameraY: Float = 0f, val cameraZoom: Float = 0f,
) {
    val complete get() = if (puzzle.kind == PuzzleKind.Arrows) removed.size == puzzle.paths.size else routes.size == puzzle.paths.size
    val stars get() = if (practice) 1 else (3 - (if (mistakes > 0) 1 else 0) - (if (hintsUsed > 1) 1 else 0)).coerceAtLeast(1)
}

/** Canonical geometry is stored with each attempt and completed level. No restoration
 * depends on the current generator version. Separate namespace preserves old game data. */
class PuzzleStore(context: Context, namespace: String = "iq_infinite_puzzles") {
    private val prefs = context.getSharedPreferences(namespace, Context.MODE_PRIVATE)
    fun claimReward(s: PuzzleSession): Boolean {
        val key = "reward_${s.puzzle.kind}_${s.puzzle.level}"
        if (!s.complete || s.practice || prefs.getBoolean(key, false)) return false
        return prefs.edit().putBoolean(key, true).commit()
    }
    fun highest(kind: PuzzleKind) = prefs.getLong("highest_$kind", 1).coerceAtLeast(1)
    fun bestStars(kind: PuzzleKind, level: Long) = prefs.getInt("best_${kind}_$level", 0)
    private fun readStored(kind: PuzzleKind, suffix: String): PuzzleSession? {
        val prefixes=if(kind==PuzzleKind.Arrows)listOf("v5_","v4_","v3_","v2_","") else listOf("")
        return prefixes.firstNotNullOfOrNull { prefix->prefs.getString("$prefix$suffix",null)?.let { runCatching { decode(JSONObject(it)) }.getOrNull() } }
    }
    fun active(kind: PuzzleKind): PuzzleSession? = readStored(kind,"active_$kind")
    fun saved(kind: PuzzleKind, level: Long): PuzzleSession? = readStored(kind,"level_${kind}_$level")
    fun save(session: PuzzleSession) {
        val json = encode(session).toString()
        val prefix=if(session.puzzle.kind!=PuzzleKind.Arrows) "" else when {session.puzzle.version>=5->"v5_";session.puzzle.version>=4->"v4_";session.puzzle.version>=3->"v3_";session.puzzle.version>=2->"v2_";else->""}
        val key = "${prefix}level_${session.puzzle.kind}_${session.puzzle.level}"
        val edit = prefs.edit().putString("${prefix}active_${session.puzzle.kind}", json)
        val identityKey = "identity_$key"
        val identity = "${session.puzzle.seed}:${session.puzzle.version}:${session.puzzle.signature}"
        if (!prefs.contains(key) || prefs.getString(identityKey, null) == identity) {
            edit.putString(key, json).putString(identityKey, identity)
        }
        else edit.putString("attempt_${session.puzzle.kind}_${session.puzzle.level}_${session.puzzle.variation}", json)
        if (session.complete) {
            edit.putLong("highest_${session.puzzle.kind}", maxOf(highest(session.puzzle.kind), InfiniteEngine.next(session.puzzle.level)))
            val bestKey = "best_${session.puzzle.kind}_${session.puzzle.level}"
            edit.putInt(bestKey, maxOf(prefs.getInt(bestKey, 0), session.stars))
            val performanceKey = "performance_${session.puzzle.kind}_${session.puzzle.level}"
            val previous = prefs.getString(performanceKey, null)?.let { runCatching { decode(JSONObject(it)) }.getOrNull() }
            if (previous == null || session.stars > previous.stars || (session.stars == previous.stars && session.seconds < previous.seconds)) edit.putString(performanceKey, json)
        }
        // All accepted signatures remain canonical and independent of seed collisions.
        edit.putBoolean("signature_${session.puzzle.signature}", true).apply()
    }
    fun fresh(kind: PuzzleKind, level: Long, mode: PuzzleMode, variation: Long = 0): PuzzleSession {
        val previous = active(kind)
        val first = InfiniteEngine.generateFresh(kind, level, variation) { prefs.contains("signature_$it") }
        // Adapt spatial complexity within the same capped tier; keep the selected
        // canonical seed and geometry, so replays never depend on later performance.
        val candidates = mutableListOf(first)
        if (previous?.complete == true && level > previous.puzzle.level) {
            var v = first.variation
            repeat(3) {
                if (v < Long.MAX_VALUE) {
                    v++
                    val candidate = InfiniteEngine.generateFresh(kind, level, v) { prefs.contains("signature_$it") }
                    candidates += candidate; v = candidate.variation
                }
            }
        }
        val p = if ((previous?.mistakes ?: 0) > 1) candidates.minBy { InfiniteEngine.complexity(it) }
            else candidates.maxBy { InfiniteEngine.complexity(it) }
        return PuzzleSession(p, mode)
    }
    private fun cells(points: List<Cell>) = JSONArray().also { a -> points.forEach { a.put(JSONArray(listOf(it.x, it.y))) } }
    private fun points(a: JSONArray): List<Cell> = (0 until a.length()).map { i -> a.getJSONArray(i).let { Cell(it.getInt(0), it.getInt(1)) } }
    private fun encode(s: PuzzleSession): JSONObject = JSONObject().apply {
        val p = s.puzzle
        put("kind", p.kind.name); put("level", p.level); put("variation", p.variation); put("version", p.version); put("seed", p.seed); put("size", p.size)
        put("paths", JSONArray().also { a -> p.paths.forEach { a.put(JSONObject().put("id", it.id).put("cells", cells(it.cells))) } })
        put("solution", JSONArray(p.solution)); put("mode", s.mode.name); put("removed", JSONArray(s.removed.toList()))
        put("routes", JSONObject().also { o -> s.routes.forEach { (id, route) -> o.put(id.toString(), cells(route)) } })
        put("lives", s.lives); put("hints", s.hints); put("hintsUsed", s.hintsUsed); put("mistakes", s.mistakes); put("moves", s.moves); put("seconds", s.seconds); put("practice", s.practice); put("cameraX", s.cameraX); put("cameraY", s.cameraY); put("cameraZoom", s.cameraZoom)
    }
    private fun decode(o: JSONObject): PuzzleSession {
        val a = o.getJSONArray("paths")
        val paths = (0 until a.length()).map { a.getJSONObject(it).let { p -> PuzzlePath(p.getInt("id"), points(p.getJSONArray("cells"))) } }
        val solution = o.getJSONArray("solution").let { arr -> (0 until arr.length()).map { arr.getInt(it) } }
        val p = InfinitePuzzle(PuzzleKind.valueOf(o.getString("kind")), o.getLong("level"), o.getLong("variation"), o.getInt("version"), o.getLong("seed"), o.getInt("size"), paths, solution)
        val valid=InfiniteEngine.validate(p)
        if(!valid && p.kind==PuzzleKind.Arrows && p.version<ArrowLevelGenerator.VERSION) {
            // Archive the broken geometry in its original namespace; regenerate
            // this attempt safely, with full lives. Unlocks/stars/rewards keep
            // their existing keys, and no invalid old board reaches gameplay.
            return PuzzleSession(ArrowLevelGenerator.generate(p.level,p.variation),PuzzleMode.valueOf(o.getString("mode")))
        }
        require(valid)
        val r = o.getJSONObject("routes")
        val routes = r.keys().asSequence().associate { it.toInt() to points(r.getJSONArray(it)) }
        require(routes.all { (id, route) -> InfiniteEngine.validRoute(p, id, route, routes, true) })
        val removed = o.getJSONArray("removed").let { arr -> (0 until arr.length()).map { arr.getInt(it) }.toSet() }
        return PuzzleSession(p, PuzzleMode.valueOf(o.getString("mode")), removed, routes, o.getInt("lives"), o.getInt("hints"), o.getInt("hintsUsed"), o.getInt("mistakes"), o.getInt("moves"), o.getLong("seconds"), o.getBoolean("practice"), o.optDouble("cameraX",0.0).toFloat(),o.optDouble("cameraY",0.0).toFloat(),o.optDouble("cameraZoom",0.0).toFloat())
    }
}
