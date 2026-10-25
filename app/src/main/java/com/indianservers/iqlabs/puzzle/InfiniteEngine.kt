package com.indianservers.iqlabs.puzzle

import java.security.MessageDigest
import java.util.Random
import kotlin.math.abs

data class Cell(val x: Int, val y: Int) {
    operator fun plus(other: Cell) = Cell(x + other.x, y + other.y)
    operator fun minus(other: Cell) = Cell(x - other.x, y - other.y)
    fun adjacent(other: Cell) = abs(x - other.x) + abs(y - other.y) == 1
}
enum class PuzzleKind(val title: String) { Arrows("Arrow Escape"), Colours("Connect Colours") }
enum class PuzzleMode(val lives: Int) { Normal(5), Challenge(3), Relaxed(Int.MAX_VALUE) }
data class PuzzlePath(val id: Int, val cells: List<Cell>) {
    val head get() = cells.last()
    val direction get() = head - cells[cells.lastIndex - 1]
}
data class InfinitePuzzle(
    val kind: PuzzleKind, val level: Long, val variation: Long, val version: Int,
    val seed: Long, val size: Int, val paths: List<PuzzlePath>, val solution: List<Int>,
) {
    val signature: String by lazy {
        val canonical = paths.map { path ->
            if (kind == PuzzleKind.Colours) listOf(path.cells.first(),path.head).map { "${it.x},${it.y}" }.sorted().joinToString("|")
            else path.cells.joinToString("|") { "${it.x},${it.y}" }
        }.sorted().joinToString(";")
        InfiniteEngine.digest("$kind:$size:$canonical")
    }
    val arrowGeometry: ArrowCollisionEngine by lazy { ArrowCollisionEngine(paths,size) }
    val tier: String get() = InfiniteEngine.tiers[if(kind==PuzzleKind.Arrows && version>=2) ArrowLevelGenerator.tier(level) else InfiniteEngine.tier(level)]
}

/** Integer lattice geometry. Strokes have radius .14 cells and endpoints radius .28.
 * Disjoint orthogonal lattice segments have at least one cell of clearance, so no
 * graphical touching is accepted by the logical model. Animation follows the same route. */
object InfiniteEngine {
    const val VERSION = 1
    val tiers = listOf("Beginner", "Easy", "Medium", "Hard", "Expert", "Master", "Genius")
    fun tier(level: Long) = ((level.coerceAtLeast(1) - 1) / 8).coerceAtMost(6).toInt()
    fun next(level: Long) = if (level == Long.MAX_VALUE) level else level + 1
    fun digest(value: String): String = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(java.util.Locale.ROOT, it) }
    fun seed(kind: PuzzleKind, level: Long, variation: Long): Long {
        val bytes = MessageDigest.getInstance("SHA-256").digest("IQ:$kind:$level:${if(kind==PuzzleKind.Arrows) ArrowLevelGenerator.VERSION else VERSION}:$variation".toByteArray(Charsets.UTF_8))
        return bytes.take(8).fold(0L) { n, b -> (n shl 8) or (b.toLong() and 255) }
    }
    fun inside(p: Cell, size: Int) = p.x in 0 until size && p.y in 0 until size
    val directions = listOf(Cell(1, 0), Cell(-1, 0), Cell(0, 1), Cell(0, -1))
    fun escape(path: PuzzlePath, size: Int): List<Cell> {
        val result = mutableListOf<Cell>()
        var p = path.head + path.direction
        while (inside(p, size)) { result += p; p += path.direction }
        return result
    }
    fun removable(puzzle: InfinitePuzzle, remaining: Set<Int>): List<Int> = if(puzzle.kind==PuzzleKind.Arrows && puzzle.version>=2) puzzle.arrowGeometry.removable(remaining) else puzzle.paths.filter { a ->
        a.id in remaining && (puzzle.kind!=PuzzleKind.Arrows || !ArrowCollisionEngine.selfBlocked(a)) && puzzle.paths.none { b -> b.id != a.id && b.id in remaining && escape(a, puzzle.size).any { it in b.cells } }
    }.map { it.id }
    fun solveArrows(puzzle: InfinitePuzzle): List<Int>? {
        if(puzzle.version>=2) return puzzle.arrowGeometry.solve()
        val remaining = puzzle.paths.map { it.id }.toMutableSet()
        val solution = mutableListOf<Int>()
        while (remaining.isNotEmpty()) {
            val moves = removable(puzzle, remaining)
            if (moves.isEmpty()) return null
            solution += moves; remaining.removeAll(moves.toSet())
        }
        return solution
    }
    fun validRoute(puzzle: InfinitePuzzle, id: Int, route: List<Cell>, existing: Map<Int, List<Cell>>, complete: Boolean): Boolean {
        val pair = puzzle.paths.firstOrNull { it.id == id } ?: return false
        if (route.isEmpty() || route.first() !in listOf(pair.cells.first(), pair.head)) return false
        if (route.any { !inside(it, puzzle.size) } || route.distinct().size != route.size) return false
        if (route.zipWithNext().any { (a, b) -> !a.adjacent(b) }) return false
        val forbidden = existing.filterKeys { it != id }.values.flatten().toSet() + puzzle.paths.filter { it.id != id }.flatMap { listOf(it.cells.first(), it.head) }
        if (route.any { it in forbidden }) return false
        return !complete || (route.size > 1 && setOf(route.first(), route.last()) == setOf(pair.cells.first(), pair.head))
    }
    /** Bounded independent path search. Null means no solution found, including budget
     * exhaustion; callers must never interpret that result as proof of impossibility. */
    fun solveColours(puzzle: InfinitePuzzle, existing: Map<Int, List<Cell>>, budget: Int = 60000): Map<Int, List<Cell>>? {
        if (existing.any { (id, route) -> !validRoute(puzzle, id, route, existing, true) }) return null
        if (puzzle.paths.all { p -> existing[p.id] == null || existing[p.id] == p.cells || existing[p.id] == p.cells.reversed() }) {
            val candidate = puzzle.paths.associate { it.id to (existing[it.id] ?: it.cells) }
            if (candidate.all { (id, route) -> validRoute(puzzle, id, route, candidate, true) }) return candidate
        }
        var steps = 0
        val routes = existing.toMutableMap()
        fun search(): Boolean {
            val pair = puzzle.paths.filter { it.id !in routes }.minByOrNull { abs(it.head.x - it.cells.first().x) + abs(it.head.y - it.cells.first().y) } ?: return true
            val start = pair.cells.first(); val end = pair.head
            val occupied = routes.values.flatten().toSet() + puzzle.paths.filter { it.id != pair.id }.flatMap { listOf(it.cells.first(), it.head) }
            val path = mutableListOf(start); val visited = mutableSetOf(start)
            fun walk(p: Cell): Boolean {
                if (++steps > budget) return false
                if (p == end) { routes[pair.id] = path.toList(); if (search()) return true; routes.remove(pair.id); return false }
                for (q in directions.map { p + it }.sortedBy { abs(it.x - end.x) + abs(it.y - end.y) }) {
                    if (inside(q, puzzle.size) && q !in occupied && visited.add(q)) {
                        path += q
                        if (walk(q)) return true
                        path.removeAt(path.lastIndex); visited.remove(q)
                    }
                }
                return false
            }
            return walk(start)
        }
        return if (search()) routes.toMap() else null
    }
    data class ColourAdvice(val solution: Map<Int,List<Cell>>, val revise: Set<Int>)
    fun colourAdvice(puzzle: InfinitePuzzle, existing: Map<Int,List<Cell>>): ColourAdvice {
        solveColours(puzzle, existing)?.let { return ColourAdvice(it, emptySet()) }
        for (id in existing.keys) {
            solveColours(puzzle, existing - id, 12000)?.let { return ColourAdvice(it, setOf(id)) }
        }
        // If a bounded continuation search cannot find a route, the construction
        // witness always provides a verified recovery plan. Only conflicting
        // player routes are marked for revision; matching routes are preserved.
        val revise = existing.filter { (id, route) ->
            val original = puzzle.paths.first { it.id == id }.cells
            route != original && route != original.reversed()
        }.keys
        val kept = existing.filterKeys { it !in revise }
        val solution = puzzle.paths.associate { it.id to (kept[it.id] ?: it.cells) }
        check(solution.all { (id, route) -> validRoute(puzzle,id,route,solution,true) })
        return ColourAdvice(solution, revise)
    }
    fun complexity(puzzle: InfinitePuzzle): Int {
        val bends = puzzle.paths.sumOf { p -> p.cells.zipWithNext().map { (a,b) -> b-a }.zipWithNext().count { (a,b) -> a != b } }
        val dependencies = if (puzzle.kind == PuzzleKind.Arrows) {
            if(puzzle.version>=2) puzzle.arrowGeometry.blockers.values.sumOf { it.size }
            else puzzle.paths.sumOf { a -> puzzle.paths.count { b -> a.id != b.id && escape(a,puzzle.size).any { it in b.cells } } }
        } else 0
        return puzzle.paths.sumOf { it.cells.size } + bends * 2 + dependencies * 4
    }
    fun validate(puzzle: InfinitePuzzle): Boolean {
        if (puzzle.paths.isEmpty() || puzzle.paths.map { it.id }.distinct().size != puzzle.paths.size) return false
        val all = puzzle.paths.flatMap { it.cells }
        if (all.distinct().size != all.size || all.any { !inside(it, puzzle.size) }) return false
        if (puzzle.paths.any { it.cells.size < 2 || it.cells.zipWithNext().any { (a,b) -> !a.adjacent(b) } }) return false
        return if (puzzle.kind == PuzzleKind.Arrows) puzzle.paths.none(ArrowCollisionEngine::selfBlocked) && solveArrows(puzzle) != null else {
            val routes = puzzle.paths.associate { it.id to it.cells }
            routes.all { (id, route) -> validRoute(puzzle, id, route, routes, true) }
        }
    }
    fun generateFresh(kind: PuzzleKind, level: Long, variation: Long = 0, seen: (String) -> Boolean): InfinitePuzzle {
        var v = variation
        repeat(64) {
            val puzzle = generate(kind, level, v)
            if (!seen(puzzle.signature)) return puzzle
            check(v < Long.MAX_VALUE) { "Variation space exhausted" }
            v++
        }
        error("Could not find a fresh puzzle within generation budget")
    }
    fun generate(kind: PuzzleKind, level: Long, variation: Long = 0): InfinitePuzzle {
        require(level > 0)
        if(kind==PuzzleKind.Arrows) return ArrowLevelGenerator.generate(level,variation)
        val seed = seed(kind, level, variation); val rng = Random(seed)
        val tier = tier(level)
        // Bounded logical density; dense boards use the UI's zoom/pan viewport.
        // After Genius, continue varying geometry rather than growing forever.
        val size = listOf(6,7,8,9,10,11,12)[tier]
        val target = listOf(2,3,5,7,9,10,12)[tier]
        val paths = mutableListOf<PuzzlePath>(); val occupied = mutableSetOf<Cell>()
        repeat(1800) {
            if (paths.size >= target) return@repeat
            val free = Cell(rng.nextInt(size), rng.nextInt(size))
            if (free in occupied) return@repeat
            val route = mutableListOf(free)
            repeat(1 + rng.nextInt(3 + tier)) {
                val choices = directions.map { route.last() + it }.filter { inside(it, size) && it !in occupied && it !in route }
                if (choices.isNotEmpty()) route += choices[rng.nextInt(choices.size)]
            }
            if (route.size < 2) return@repeat
            val candidate = PuzzlePath(paths.size, route.toList())
            paths += candidate; occupied += route
        }
        if (paths.isEmpty()) paths += PuzzlePath(0, listOf(Cell(0,0), Cell(1,0)))
        val raw = InfinitePuzzle(kind, level, variation, VERSION, seed, size, paths, emptyList())
        check(validate(raw)) { "Generator produced invalid geometry" }
        return raw.copy(solution = if (kind == PuzzleKind.Arrows) solveArrows(raw)!! else paths.map { it.id })
    }
}
