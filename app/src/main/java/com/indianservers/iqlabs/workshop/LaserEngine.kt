package com.indianservers.iqlabs.workshop

import java.util.ArrayDeque
import java.util.Random
import kotlin.math.*

enum class OpticType { Emitter,Mirror,FixedMirror,Splitter,Filter,Prism,Reflector,Absorber,Receiver,Shutter }
data class Optic(val id:Int,val type:OpticType,val position:Vec,val rotation:Int=0,val colour:Int=7) {
    val movable get()=type in setOf(OpticType.Mirror,OpticType.Splitter,OpticType.Prism,OpticType.Shutter)
}
data class LaserLevel(override val level:Long,override val variation:Long,override val version:Int,override val seed:Long,
    val columns:Int,val rows:Int,val objects:List<Optic>,val initial:Map<Int,Int>,val solution:Map<Int,Int>):WorkshopPuzzle {
    override val kind=WorkshopKind.Laser
    override val signature by lazy { WorkshopSeeds.hash("laser:$columns:$rows:"+objects.map { "${it.type}:${it.position.x},${it.position.y}:${it.rotation}:${it.colour}" }.sorted().joinToString(";")) }
}
data class Beam(val from:Vec,val to:Vec,val colour:Int,val intensity:Double=1.0)
data class LaserTrace(val beams:List<Beam>,val received:Map<Int,Int>,val lit:Set<Int>,val complete:Boolean,val loops:Int,val truncated:Boolean)

/** RGB masks model additive light (R=1,G=2,B=4), not wave interference.
 * Mirrors use d - 2(d.n)n. The prism is a dichroic, coated prism: R is
 * reflected left, G transmitted, B reflected right; flipping swaps the
 * reflected channels. It is a spectral router, not an invented wave model. */
object LaserEngine {
    const val EPS=1e-7
    data class Hit(val t:Double,val point:Vec,val optic:Optic,val normal:Vec)
    private data class Ray(val origin:Vec,val direction:Vec,val colour:Int,val ignore:Int,val intensity:Double=1.0)
    fun reflect(d:Vec,n:Vec)=d-n*(2*d.dot(n))
    fun surfaceAngle(rotation:Int)=if(rotation%2==0)-PI/4 else PI/4
    fun raySegment(origin:Vec,direction:Vec,a:Vec,b:Vec):Double? {
        val edge=b-a;val denominator=direction.cross(edge)
        if(abs(denominator)<EPS)return null
        val delta=a-origin;val t=delta.cross(edge)/denominator;val u=delta.cross(direction)/denominator
        return t.takeIf { it>EPS && u>=-EPS && u<=1+EPS }
    }
    private fun circle(origin:Vec,d:Vec,centre:Vec,radius:Double):Double? {
        val q=origin-centre;val b=q.dot(d);val disc=b*b-q.dot(q)+radius*radius
        if(disc<0)return null
        val t=-b-sqrt(disc)
        return t.takeIf { it>EPS } ?: (-b+sqrt(disc)).takeIf { it>EPS }
    }
    private fun box(origin:Vec,d:Vec,o:Optic,radius:Double):Hit? {
        val c=o.position;val corners=listOf(Vec(c.x-radius,c.y-radius),Vec(c.x+radius,c.y-radius),Vec(c.x+radius,c.y+radius),Vec(c.x-radius,c.y+radius))
        var best:Hit?=null
        for(i in 0..3) {
            val a=corners[i];val b=corners[(i+1)%4]
            val t=raySegment(origin,d,a,b) ?: continue
            val normal=Vec(-(b-a).y,(b-a).x).unit()
            if(best==null || t<best.t)best=Hit(t,origin+d*t,o,normal)
        }
        return best
    }
    fun nearest(level:LaserLevel,origin:Vec,direction:Vec,orientations:Map<Int,Int>,ignore:Int=-1,surfaceOverride:Pair<Int,Double>?=null):Hit? {
        var best:Hit?=null
        level.objects.forEach { o -> if(o.id!=ignore) {
            val rotation=orientations[o.id] ?: o.rotation
            val hit=when(o.type) {
                OpticType.Mirror,OpticType.FixedMirror,OpticType.Splitter,OpticType.Reflector -> {
                    val angle=if(surfaceOverride?.first==o.id)surfaceOverride.second else surfaceAngle(rotation)
                    val tangent=Vec(cos(angle),sin(angle));val normal=Vec(-tangent.y,tangent.x)
                    raySegment(origin,direction,o.position-tangent*.47,o.position+tangent*.47)?.let { Hit(it,origin+direction*it,o,normal) }
                }
                OpticType.Receiver,OpticType.Emitter,OpticType.Prism -> circle(origin,direction,o.position,if(o.type==OpticType.Receiver).29 else .30)?.let { Hit(it,origin+direction*it,o,Vec(0.0,0.0)) }
                OpticType.Shutter -> if(rotation%2==1)null else box(origin,direction,o,.35)
                else -> box(origin,direction,o,if(o.type==OpticType.Filter).25 else .36)
            }
            if(hit!=null && (best==null || hit.t<best!!.t))best=hit
        } }
        return best
    }
    private fun boundary(level:LaserLevel,p:Vec,d:Vec):Double {
        val times=mutableListOf<Double>()
        if(d.x>EPS)times+=(level.columns-p.x)/d.x
        if(d.x< -EPS)times+=-p.x/d.x
        if(d.y>EPS)times+=(level.rows-p.y)/d.y
        if(d.y< -EPS)times+=-p.y/d.y
        return times.filter { it>EPS }.minOrNull() ?: 0.0
    }
    fun trace(level:LaserLevel,orientations:Map<Int,Int>,surfaceOverride:Pair<Int,Double>?=null):LaserTrace {
        val queue=ArrayDeque<Ray>();val beams=mutableListOf<Beam>();val received=mutableMapOf<Int,Int>();val visited=mutableSetOf<String>();var loops=0
        val directions=listOf(Vec(1.0,0.0),Vec(0.0,1.0),Vec(-1.0,0.0),Vec(0.0,-1.0))
        level.objects.filter { it.type==OpticType.Emitter }.forEach { o -> val d=directions[Math.floorMod(o.rotation,4)];queue+=Ray(o.position+d*.31,d,o.colour,o.id) }
        var steps=0;val budget=max(256,level.objects.size*128)
        while(queue.isNotEmpty() && steps++<budget) {
            val ray=queue.removeFirst();if(ray.colour==0 || ray.intensity<1e-6)continue
            val hit=nearest(level,ray.origin,ray.direction,orientations,ray.ignore,surfaceOverride)
            val edge=boundary(level,ray.origin,ray.direction)
            if(hit==null || hit.t>edge) { beams+=Beam(ray.origin,ray.origin+ray.direction*edge,ray.colour,ray.intensity);continue }
            beams+=Beam(ray.origin,hit.point,ray.colour,ray.intensity)
            val key="${(hit.point.x*100000).roundToInt()},${(hit.point.y*100000).roundToInt()}:${hit.optic.id}:${(ray.direction.x*100000).roundToInt()}:${(ray.direction.y*100000).roundToInt()}:${ray.colour}"
            if(!visited.add(key)) { loops++;continue }
            val o=hit.optic;val rotation=orientations[o.id] ?: o.rotation
            fun send(d:Vec,colour:Int,fraction:Double=1.0) { if(colour!=0)queue+=Ray(hit.point+d*(EPS*20),d,colour,o.id,ray.intensity*fraction) }
            when(o.type) {
                OpticType.Mirror,OpticType.FixedMirror,OpticType.Reflector -> send(reflect(ray.direction,hit.normal).unit(),ray.colour)
                OpticType.Splitter -> { send(ray.direction,ray.colour,.5);send(reflect(ray.direction,hit.normal).unit(),ray.colour,.5) }
                OpticType.Filter -> send(ray.direction,ray.colour and o.colour)
                OpticType.Prism -> {
                    val left=if(rotation%2==0)ray.direction.left() else ray.direction.right()
                    val right=if(rotation%2==0)ray.direction.right() else ray.direction.left()
                    beams+=Beam(hit.point,o.position,ray.colour,ray.intensity)
                    fun spectral(d:Vec,mask:Int) { if(mask!=0)queue+=Ray(o.position+d*(EPS*20),d,mask,o.id,ray.intensity) }
                    spectral(left,ray.colour and 1);spectral(ray.direction,ray.colour and 2);spectral(right,ray.colour and 4)
                }
                OpticType.Receiver -> received[o.id]=(received[o.id] ?: 0) or ray.colour
                else -> Unit
            }
        }
        val receivers=level.objects.filter { it.type==OpticType.Receiver }
        val lit=receivers.filter { received[it.id]==it.colour }.map { it.id }.toSet()
        return LaserTrace(beams,received,lit,receivers.isNotEmpty() && lit.size==receivers.size && queue.isEmpty(),loops,queue.isNotEmpty())
    }
}

object LaserSolver {
    data class Result(val orientations:Map<Int,Int>,val states:Int)
    /** Exhaustive bounded finite orientation solver. A budget failure is null,
     * never a claim of impossibility. No generator witness is consulted. */
    fun solve(level:LaserLevel,current:Map<Int,Int>,budget:Int=2048):Result? {
        val rotors=level.objects.filter(Optic::movable)
        require(rotors.size<=10)
        var states=0
        for(distance in 0..rotors.size) for(bits in 0 until (1 shl rotors.size)) {
            if(Integer.bitCount(bits)!=distance)continue
            if(++states>budget)return null
            val config=rotors.mapIndexed { i,o -> o.id to (((current[o.id] ?: o.rotation) xor ((bits shr i) and 1)) and 1) }.toMap()
            val trace=LaserEngine.trace(level,config)
            if(trace.complete && !trace.truncated)return Result(config,states)
        }
        return null
    }
    fun hint(level:LaserLevel,current:Map<Int,Int>):Pair<Int,Int>? = solve(level,current)?.orientations?.entries?.firstOrNull { current[it.key]!=it.value }?.let { it.key to it.value }
}

object LaserGenerator {
    const val VERSION=1
    fun generate(level:Long,variation:Long=0):LaserLevel {
        require(level>0)
        val seed=WorkshopSeeds.seed(WorkshopKind.Laser,level,VERSION,variation);val rng=Random(seed);val tier=WorkshopSeeds.tier(level)
        val width=10+min(tier,3);val bands=if(tier>=5)2 else 1;val bandHeight=7;val height=bands*bandHeight
        val objects=mutableListOf<Optic>()
        fun add(type:OpticType,x:Int,y:Int,rotation:Int=0,colour:Int=7):Optic {
            val o=Optic(objects.size,type,Vec(x+.5,y+.5),rotation,colour);objects+=o;return o
        }
        val mirrorCount=when(tier) {0->1;1->2;2->3;else->4}
        fun required(incoming:Vec,outgoing:Vec):Int = (0..1).first { r ->
            val a=LaserEngine.surfaceAngle(r);val n=Vec(-sin(a),cos(a));(LaserEngine.reflect(incoming,n)-outgoing).length()<1e-6
        }
        // Main network is constructed from a continuous, variable rectilinear
        // route. Every mirror orientation is derived from its incident/outgoing vectors.
        val base=0;val y0=2+rng.nextInt(3);val y1=if(y0<3)5 else 1;val x1=2+rng.nextInt(2);val x2=6+rng.nextInt(2)
        val sourceColour=if(tier>=4)7 else listOf(1,2,4,3,5,6,7)[rng.nextInt(7)]
        add(OpticType.Emitter,0,y0,colour=sourceColour)
        val turns=mutableListOf(Vec(x1+.5,y0+.5))
        if(mirrorCount>=2)turns+=Vec(x1+.5,y1+.5)
        if(mirrorCount>=3)turns+=Vec(x2+.5,y1+.5)
        if(mirrorCount>=4)turns+=Vec(x2+.5,y0+.5)
        val destination=when(mirrorCount) {
            1->Vec(x1+.5,if(y0<3)6.5 else .5)
            2,4->Vec(width-.5,turns.last().y)
            else->Vec(x2+.5,y0+.5)
        }
        val points=listOf(Vec(.5,y0+.5))+turns+destination
        turns.forEachIndexed { i,p ->
            if(Thread.currentThread().isInterrupted)throw java.util.concurrent.CancellationException()
            val rotation=required((p-points[i]).unit(),(points[i+2]-p).unit())
            val type=if(tier>=3 && i==1) { if(tier%2==0)OpticType.Reflector else OpticType.FixedMirror } else OpticType.Mirror
            add(type,p.x.toInt(),p.y.toInt(),rotation)
        }
        var targetColour=sourceColour
        if(tier>=4 && mirrorCount in listOf(2,4)) {
            targetColour=listOf(1,2,4,3,5,6)[rng.nextInt(6)]
            add(OpticType.Filter,width-2,destination.y.toInt(),colour=targetColour)
        }
        add(OpticType.Receiver,destination.x.toInt(),destination.y.toInt(),colour=targetColour)
        if(tier==4 || tier>=6) {
            // A partially reflecting plate transmits towards the original turn
            // and reflects a second branch to a filtered receiver.
            val splitter=add(OpticType.Splitter,1,y0,if(y0>=3)0 else 1)
            val endpoint=if(y0>=3)0 else 6
            val filterY=if(y0>=3)y0-1 else y0+1
            add(OpticType.Filter,1,filterY,colour=1)
            add(OpticType.Receiver,1,endpoint,colour=1)
        }
        if(bands==2) {
            if(tier>=5) {
                // The defined dichroic model routes actual RGB channels from white.
                val y=bandHeight+3;val x=3+rng.nextInt(width-6)
                add(OpticType.Emitter,0,y,colour=7)
                add(OpticType.Prism,x,y,rotation=0)
                add(OpticType.Receiver,x,bandHeight,colour=1)
                add(OpticType.Receiver,width-1,y,colour=if(tier>=7)3 else 2)
                add(OpticType.Receiver,x,height-1,colour=4)
                if(tier>=7)add(OpticType.Emitter,width-1,bandHeight,rotation=1,colour=1)
                if(tier>=7)add(OpticType.Shutter,width-3,y,rotation=1)
            }
        }
        // Place solid obstacles only where they do not disrupt the valid optical
        // network; they still intercept rays from incorrect rotations.
        repeat(2+tier) {
            val x=1+rng.nextInt(width-2);val y=rng.nextInt(height)
            if(objects.any { it.position==Vec(x+.5,y+.5) })return@repeat
            val candidate=Optic(objects.size,OpticType.Absorber,Vec(x+.5,y+.5))
            val board=LaserLevel(level,variation,VERSION,seed,width,height,objects+candidate,emptyMap(),emptyMap())
            if(LaserEngine.trace(board,emptyMap()).complete)objects+=candidate
        }
        // Rotate the constructed network as one optical system when needed to
        // give phone levels a portrait playfield. Mirror surfaces and emitter
        // directions rotate with it; this is not an anisotropic visual stretch.
        val portrait=width>height
        val opticalObjects=if(portrait)objects.map { o ->
            o.copy(position=Vec(height-o.position.y,o.position.x),rotation=when(o.type) {
                OpticType.Emitter->(o.rotation+1)%4
                OpticType.Mirror,OpticType.FixedMirror,OpticType.Splitter,OpticType.Reflector->o.rotation xor 1
                else->o.rotation
            })
        } else objects.toList()
        val solution=opticalObjects.filter(Optic::movable).associate { it.id to it.rotation }
        val initial=solution.mapValues { rng.nextInt(2) }.toMutableMap()
        var board=LaserLevel(level,variation,VERSION,seed,if(portrait)height else width,if(portrait)width else height,opticalObjects,initial.toMap(),solution)
        if(LaserEngine.trace(board,initial).complete) {
            val mirror=opticalObjects.first { it.type==OpticType.Mirror };initial[mirror.id]=1-solution.getValue(mirror.id)
            board=board.copy(initial=initial.toMap())
        }
        check(validate(board)) { "Optical generator failed independent validation at $level/$variation" }
        return board
    }
    fun validate(board:LaserLevel):Boolean {
        if(board.level<=0 || board.objects.map { it.id }.distinct().size!=board.objects.size)return false
        if(board.objects.map { it.position }.distinct().size!=board.objects.size)return false
        if(board.objects.any { it.position.x !in 0.0..board.columns.toDouble() || it.position.y !in 0.0..board.rows.toDouble() })return false
        if(!LaserEngine.trace(board,board.solution).complete)return false
        return LaserSolver.solve(board,board.initial)!=null && !LaserEngine.trace(board,board.initial).complete
    }
}
