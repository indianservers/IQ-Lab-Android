package com.indianservers.iqlabs.puzzle

import java.util.PriorityQueue
import kotlin.math.*

/** Exact swept occupancy for orthogonal integer-lattice polylines.
 * Bodies have radius .115 and arrowheads fit inside a .44-cell envelope.
 * Two unrelated lattice rows/columns therefore retain positive clearance.
 * A rigid axis translation sweeps every occupied row/column from its trailing
 * extremum to the board edge. This tests the BODY, not just the head ray, and
 * cannot tunnel between animation frames. Decorative dots are never indexed. */
class ArrowCollisionEngine(val paths: List<PuzzlePath>, val size: Int) {
    data class Entry(val coordinate: Int, val id: Int)
    private val rows = mutableMapOf<Int, MutableList<Entry>>()
    private val columns = mutableMapOf<Int, MutableList<Entry>>()
    val blockers: Map<Int, Set<Int>>
    init {
        paths.forEach { a -> a.cells.forEach { c ->
            rows.getOrPut(c.y) { mutableListOf() }.add(Entry(c.x,a.id))
            columns.getOrPut(c.x) { mutableListOf() }.add(Entry(c.y,a.id))
        } }
        rows.values.forEach { it.sortBy(Entry::coordinate) }
        columns.values.forEach { it.sortBy(Entry::coordinate) }
        blockers = paths.associate { a ->
            val horizontal = a.direction.x != 0
            val positive = a.direction.x + a.direction.y > 0
            val fronts = mutableMapOf<Int,Int>()
            a.cells.forEach { c ->
                val lane = if(horizontal) c.y else c.x
                val coordinate = if(horizontal) c.x else c.y
                val old=fronts[lane]
                if(old==null || (positive && coordinate<old) || (!positive && coordinate>old)) fronts[lane]=coordinate
            }
            val blocked=mutableSetOf<Int>()
            fronts.forEach { (lane, trailing) ->
                val entries = (if(horizontal) rows else columns)[lane].orEmpty()
                entries.forEach { e ->
                    if(e.id!=a.id && (if(positive) e.coordinate>=trailing else e.coordinate<=trailing)) blocked+=e.id
                }
            }
            // A self-intercepting forward ray is an invalid escape shape, even
            // though a purely rigid translation could move the body as one piece.
            // Keep a self-edge so no solver or hint can call that arrow removable.
            if(selfBlocked(a))blocked+=a.id
            a.id to blocked.toSet()
        }
    }
    fun blocking(id: Int, remaining: Set<Int>) = blockers[id].orEmpty().filterTo(linkedSetOf()) { it in remaining }
    fun removable(remaining: Set<Int>) = paths.map { it.id }.filter { it in remaining && blockers[it].orEmpty().none(remaining::contains) }
    /** Kahn's algorithm independently verifies the actual movement dependency DAG. */
    fun solve(remaining: Set<Int> = paths.map { it.id }.toSet()): List<Int>? {
        val degree=remaining.associateWith { id -> blockers[id].orEmpty().count(remaining::contains) }.toMutableMap()
        val waiting=mutableMapOf<Int,MutableList<Int>>()
        remaining.forEach { id -> blockers[id].orEmpty().filter(remaining::contains).forEach { b -> waiting.getOrPut(b) { mutableListOf() }+=id } }
        val ready=PriorityQueue<Int>(); degree.filterValues { it==0 }.keys.forEach(ready::add)
        val result=ArrayList<Int>(remaining.size)
        while(ready.isNotEmpty()) {
            val id=ready.remove(); result+=id
            waiting[id].orEmpty().forEach { waiter -> degree[waiter]=degree.getValue(waiter)-1; if(degree[waiter]==0) ready+=waiter }
        }
        return result.takeIf { it.size==remaining.size }
    }
    val depth: Int get() {
        val order=solve() ?: return Int.MAX_VALUE
        val depths=mutableMapOf<Int,Int>()
        order.forEach { id -> depths[id]=1+(blockers[id].orEmpty().maxOfOrNull { depths.getValue(it) } ?: 0) }
        return depths.values.maxOrNull() ?: 0
    }
    companion object {
        fun selfBlocked(path: PuzzlePath):Boolean {
            val head=path.head;val d=path.direction
            return path.cells.any { c->
                val dx=c.x-head.x;val dy=c.y-head.y
                dx*d.x+dy*d.y>0 && dx*d.y-dy*d.x==0
            }
        }
        fun folds(path: PuzzlePath) = path.cells.zipWithNext().map { (a,b)-> b-a }.zipWithNext().count { (a,b)->a!=b }
        /** Brute-force oracle used by tests, separate from the indexed extrema. */
        fun sweptBlocked(a: PuzzlePath,b: PuzzlePath): Boolean = a.cells.any { c -> b.cells.any { d ->
            when(a.direction) {
                Cell(1,0) -> d.y==c.y && d.x>=c.x
                Cell(-1,0) -> d.y==c.y && d.x<=c.x
                Cell(0,1) -> d.x==c.x && d.y>=c.y
                else -> d.x==c.x && d.y<=c.y
            }
        } }
        fun exitDistance(path: PuzzlePath,size: Int): Float = when(path.direction) {
            Cell(1,0) -> size-path.cells.minOf { it.x }+1f
            Cell(-1,0) -> path.cells.maxOf { it.x }+2f
            Cell(0,1) -> size-path.cells.minOf { it.y }+1f
            else -> path.cells.maxOf { it.y }+2f
        }
        fun rigidPosition(cell: Cell,path: PuzzlePath,size: Int,progress: Float): Pair<Float,Float> {
            val distance=exitDistance(path,size)*progress.coerceIn(0f,1f)
            return (cell.x+path.direction.x*distance) to (cell.y+path.direction.y*distance)
        }
    }
}
