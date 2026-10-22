package com.indianservers.iqlabs.workshop

import java.util.Random
import kotlin.math.*

data class CaptiveRing(val id:Int,val centre:Vec,val radius:Double,val tube:Double,val gap:Double,val guide:Double,val material:Int)
data class RingLink(val a:Int,val b:Int)
data class RingLevel(override val level:Long,override val variation:Long,override val version:Int,override val seed:Long,
    val rings:List<CaptiveRing>,val links:List<RingLink>,val initial:Map<Int,Double>,val solution:List<RingMove>):WorkshopPuzzle {
    override val kind=WorkshopKind.Rings
    override val signature by lazy { WorkshopSeeds.hash("rings:"+rings.joinToString(";") { "${it.centre.x},${it.centre.y}:${it.gap}:${it.guide}:${it.material}" }+links.joinToString(";") { "${it.a},${it.b}" }) }
}
data class RingMove(val id:Int,val angle:Double)
data class RingClearance(val free:Boolean,val reason:String,val blockedBy:Set<Int>)

/** Fixed collar mechanism, never a simulation of physically interpenetrating 3D
 * rings. Disjoint circular tracks rotate inside annular guides. A spring-loaded
 * link joins two retaining collars; when either ring lifts free, that link
 * retracts. Each ring retains its own base guide, so even an isolated ring must
 * align its opening. Release lifts out of the plane; adjacent tracks stay fixed.
 * Renderer, generator, solver and interactions share these clearance rules. */
object RingEngine {
    const val COLLAR_HALF=10.0
    fun normal(angle:Double)=((angle%360)+360)%360
    fun distance(a:Double,b:Double)=abs(((a-b+540)%360)-180)
    fun angle(from:Vec,to:Vec)=normal(Math.toDegrees(atan2(to.y-from.y,to.x-from.x)))
    fun snap(angle:Double)=normal(round(angle/15)*15)
    fun neighbours(board:RingLevel,id:Int,released:Set<Int>):Set<Int> = board.links.mapNotNull { e -> when(id) {e.a->e.b;e.b->e.a;else->null} }.filterTo(linkedSetOf()) { it !in released }
    fun arcOccupied(ring:CaptiveRing,orientation:Double,point:Vec):Boolean {
        val delta=point-ring.centre
        if(abs(delta.length()-ring.radius)>ring.tube)return false
        val endAngle=Math.toDegrees(asin((ring.tube/ring.radius).coerceIn(0.0,1.0)))
        return distance(angle(ring.centre,point),orientation)>=ring.gap/2-endAngle
    }
    fun tracksCollide(a:CaptiveRing,b:CaptiveRing):Boolean = (a.centre-b.centre).length() <= a.radius+a.tube+b.radius+b.tube+1e-7
    fun clearance(board:RingLevel,id:Int,orientation:Double,released:Set<Int>):RingClearance {
        val ring=board.rings.firstOrNull { it.id==id } ?: return RingClearance(false,"Unknown ring",emptySet())
        if(id in released)return RingClearance(false,"Already released",emptySet())
        val active=board.rings.filter { it.id !in released && it.id!=id }
        if(active.any { tracksCollide(ring,it) })return RingClearance(false,"An adjacent track obstructs the lift",active.filter { tracksCollide(ring,it) }.map { it.id }.toSet())
        val endClearance=Math.toDegrees(asin((ring.tube/ring.radius).coerceIn(0.0,1.0)))
        fun fits(collar:Double)=distance(orientation,collar)+COLLAR_HALF+endClearance<=ring.gap/2+1e-7
        val blockers=neighbours(board,id,released).filterTo(linkedSetOf()) { other -> !fits(angle(ring.centre,board.rings.first { it.id==other }.centre)) }
        if(!fits(ring.guide))return RingClearance(false,"Line up the opening with the gold base collar",blockers)
        if(blockers.isNotEmpty())return RingClearance(false,"Release the connected ring(s) first: ${blockers.joinToString { (it+1).toString() }}",blockers)
        return RingClearance(true,"Clear to lift free",emptySet())
    }
    fun target(board:RingLevel,id:Int,released:Set<Int>):Double? = (0 until 24).map { it*15.0 }.firstOrNull { clearance(board,id,it,released).free }
}
object RingSolver {
    /** Constraint deletion is monotone: a legal release cannot add a collar or
     * obstruction. Greedy geometric peeling therefore preserves solvability. */
    fun solve(board:RingLevel,released:Set<Int> = emptySet()):List<RingMove>? {
        if(released.any { id -> board.rings.none { it.id==id } })return null
        val removed=released.toMutableSet();val plan=mutableListOf<RingMove>()
        while(removed.size<board.rings.size) {
            val next=board.rings.asSequence().filter { it.id !in removed }.mapNotNull { r -> RingEngine.target(board,r.id,removed)?.let { RingMove(r.id,it) } }.firstOrNull() ?: return null
            plan+=next;removed+=next.id
        }
        return plan
    }
    fun hint(board:RingLevel,released:Set<Int>,angles:Map<Int,Double>):RingMove? {
        val plan=solve(board,released) ?: return null
        return plan.filter { move -> RingEngine.clearance(board,move.id,move.angle,released).free }
            .minByOrNull { RingEngine.distance(angles[it.id] ?: 0.0,it.angle) }
    }
}
object RingGenerator {
    const val VERSION=1
    private data class Grid(val x:Int,val y:Int)
    fun generate(level:Long,variation:Long=0):RingLevel {
        require(level>0)
        val tier=WorkshopSeeds.tier(level);val seed=WorkshopSeeds.seed(WorkshopKind.Rings,level,VERSION,variation);val rng=Random(seed)
        val count=listOf(3,4,6,9,12,16,18,20)[tier]
        val cells=mutableListOf(Grid(0,0));val parents=mutableListOf(-1)
        repeat(count-1) {
            if(Thread.currentThread().isInterrupted)throw java.util.concurrent.CancellationException()
            val frontier=mutableListOf<Pair<Grid,Int>>()
            cells.forEachIndexed { index,c -> listOf(Grid(c.x+1,c.y),Grid(c.x-1,c.y),Grid(c.x,c.y+1),Grid(c.x,c.y-1)).forEach { next ->
                if(next !in cells && abs(next.x)<=3 && abs(next.y)<=3)frontier+=next to index
            } }
            check(frontier.isNotEmpty())
            val next=frontier[rng.nextInt(frontier.size)];cells+=next.first;parents+=next.second
        }
        val minX=cells.minOf { it.x };val minY=cells.minOf { it.y };val pitch=2.72
        val centres=cells.map { Vec((it.x-minX)*pitch+1.4,(it.y-minY)*pitch+1.4) }
        val gap=listOf(110.0,100.0,95.0,85.0,78.0,72.0,68.0,64.0)[tier]
        val rings=cells.indices.map { id -> CaptiveRing(id,centres[id],1.0,.14,gap,if(parents[id]<0)rng.nextInt(4)*90.0 else RingEngine.angle(centres[id],centres[parents[id]]),rng.nextInt(7)) }
        val links=(1 until count).map { RingLink(it,parents[it]) }
        val initial=rings.associate { r ->
            val candidates=(0 until 24).map { it*15.0 }.filter { RingEngine.distance(it,r.guide)>r.gap/2+10 }
            r.id to candidates[rng.nextInt(candidates.size)]
        }
        val raw=RingLevel(level,variation,VERSION,seed,rings,links,initial,emptyList())
        val plan=RingSolver.solve(raw) ?: error("Collar graph has no geometric peeling sequence")
        val result=raw.copy(solution=plan)
        check(validate(result)) { "Ring generator failed independent validation" }
        return result
    }
    fun validate(board:RingLevel):Boolean {
        if(board.level<=0 || board.rings.isEmpty() || board.rings.map { it.id }.distinct().size!=board.rings.size)return false
        if(board.links.any { it.a==it.b || board.rings.none { r -> r.id==it.a } || board.rings.none { r -> r.id==it.b } })return false
        if(board.rings.any { r -> r.radius<=0 || r.tube<=0 || r.gap !in 30.0..150.0 || !r.guide.isFinite() })return false
        for(i in board.rings.indices) for(j in i+1 until board.rings.size) if(RingEngine.tracksCollide(board.rings[i],board.rings[j]))return false
        val reached=mutableSetOf(board.rings.first().id)
        repeat(board.rings.size) { board.links.forEach { if(it.a in reached)reached+=it.b;if(it.b in reached)reached+=it.a } }
        if(reached.size!=board.rings.size)return false
        val plan=RingSolver.solve(board) ?: return false
        val removed=mutableSetOf<Int>()
        for(move in plan) { if(!RingEngine.clearance(board,move.id,move.angle,removed).free)return false;removed+=move.id }
        return removed.size==board.rings.size
    }
}
