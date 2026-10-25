package com.indianservers.iqlabs.puzzle

import java.util.Random
import kotlin.math.*

/** Procedural paths and nested open-frame groups. Between routing groups,
 * blocker edges strictly increase outward Manhattan potential. Inside a group,
 * blockers lie in deeper nesting layers. This lexicographic order is acyclic;
 * a separate swept-body solver independently validates the actual geometry. */
object ArrowLevelGenerator {
    const val VERSION=5
    data class Profile(val count:Int,val minFolds:Int,val maxFolds:Int,val columns:Int,val radial:Boolean)
    fun profile(level:Long): Profile {
        require(level>0)
        val count=if(level<=2)3+level.toInt() else 12+(level-3).coerceAtMost(738).toInt()
        val minFolds=when {level<=2->2;level<=20->3;else->4}
        val maxFolds=when {level<=2->2;level<=5->5;level<=10->6;level<=20->7;level<=30->8;level<=60->9;level<=100->10;else->12}
        val columns=if(level<=2)2 else ceil(sqrt(count.toDouble())).toInt()
        return Profile(count,minFolds,maxFolds,columns,level>=3)
    }
    fun tier(level:Long)=when {level<=2->0;level<=10->1;level<=30->2;level<=60->3;level<=100->4;level<=200->5;else->6}

    internal fun localPath(folds:Int,pitch:Int,rng:Random,spiral:Boolean):List<Cell> {
        val controls=mutableListOf<Cell>()
        if(spiral) {
            // A variable rectangular spiral, rather than repeatedly placing the
            // same L-shape. Insets leave at least two lattice cells of clearance.
            val gap=2
            var left=0;var top=0
            var right=pitch-4-rng.nextInt(2);var bottom=pitch-4-rng.nextInt(2)
            var x=left;var y=top;controls+=Cell(x,y)
            for(segment in 0..folds) {
                when(segment%4) {
                    0->{x=right;top+=gap}
                    1->{y=bottom;right-=gap}
                    2->{x=left;bottom-=gap}
                    else->{y=top;left+=gap}
                }
                controls+=Cell(x,y)
            }
            // Trace the spiral from inside to outside. The former outer start
            // is now the head, and all body points lie behind its support plane.
            // This gives a geometric proof that its forward ray cannot hit itself.
            controls.reverse()
            controls.indices.forEach { i->val c=controls[i];controls[i]=Cell(-c.x,-c.y) }

        } else {
            var x=0;var y=0;controls+=Cell(x,y)
            val firstHorizontal=folds%2==0
            var high=false
            for(segment in 0..folds) {
                val along=if(segment%2==0)firstHorizontal else !firstHorizontal
                if(along)x+=2+rng.nextInt(3)
                else {
                    high=!high
                    y=if(high)pitch/2+1+rng.nextInt(max(1,pitch/2-2)) else rng.nextInt(max(1,pitch/2-2))
                }
                controls+=Cell(x,y)
            }
        }
        val minX=controls.minOf { it.x };val maxX=controls.maxOf { it.x }
        val minY=controls.minOf { it.y };val maxY=controls.maxOf { it.y }
        val flip=rng.nextBoolean()
        return controls.map { c->
            Cell(c.x-minX+(pitch-(maxX-minX))/2,
                (if(flip)maxY-c.y else c.y-minY)+(pitch-(maxY-minY))/2)
        }
    }

    fun generate(level:Long,variation:Long):InfinitePuzzle {
        val seed=InfiniteEngine.seed(PuzzleKind.Arrows,level,variation)
        val rng=Random(seed);val profile=profile(level)
        val columns=profile.columns;val rows=(profile.count+columns-1)/columns
        val pitch=profile.maxFolds*2+7
        val size=max(columns,rows)*pitch+2
        val paths=ArrayList<PuzzlePath>(profile.count)
        val rowRight=BooleanArray(rows) { rng.nextBoolean() }
        val rotation=rng.nextInt(4)
        fun rotate(c:Cell):Cell=when(rotation) {1->Cell(size-1-c.y,c.x);2->Cell(size-1-c.x,size-1-c.y);3->Cell(c.y,size-1-c.x);else->c}
        // Keep a fixed total fold budget per level. Random individual shapes
        // cannot accidentally make the next level's overall bend load smaller.
        val folds=IntArray(profile.count) {profile.minFolds+rng.nextInt(profile.maxFolds-profile.minFolds+1)}
        val target=profile.count*(profile.minFolds+profile.maxFolds)/2
        var balance=target-folds.sum()
        for(i in 0 until profile.count*(profile.maxFolds-profile.minFolds+1)) {
            if(balance==0)break
            val id=i%profile.count
            if(balance>0 && folds[id]<profile.maxFolds) {folds[id]++;balance--}
            else if(balance<0 && folds[id]>profile.minFolds) {folds[id]--;balance++}
        }
        if(level>=7) {
            val (nestedSize,nestedPaths)=ArrowNestedLayout.generate(level,folds,profile.maxFolds,rng)
            return validated(level,variation,seed,nestedSize,nestedPaths,rng)
        }
        repeat(profile.count) { id->
            if(Thread.currentThread().isInterrupted)throw java.util.concurrent.CancellationException()
            val col=id%columns;val row=id/columns
            val horizontal=if(profile.radial)rng.nextBoolean() else true
            val coordinate=if(horizontal)col.toDouble() else row.toDouble()
            val centre=if(horizontal)(columns-1)/2.0 else (rows-1)/2.0
            val positive=if(!profile.radial)rowRight[row] else if(coordinate==centre)rng.nextBoolean() else coordinate>centre
            val controls=localPath(folds[id],pitch,rng,profile.radial && id%3==0)
            val oriented=controls.map { c->
                val q=if(positive)c else Cell(pitch-c.x,c.y)
                if(horizontal)q else Cell(q.y,q.x)
            }
            val cells=mutableListOf<Cell>()
            oriented.zipWithNext().forEach { (a,b)->
                if(cells.isEmpty())cells+=a
                val d=Cell((b.x-a.x).sign,(b.y-a.y).sign)
                var c=a
                while(c!=b) {c+=d;cells+=c}
            }
            paths+=PuzzlePath(id,cells.map {rotate(Cell(1+col*pitch+it.x,1+row*pitch+it.y))})
        }
        return validated(level,variation,seed,size,paths,rng)
    }
    private fun validated(level:Long,variation:Long,seed:Long,size:Int,paths:List<PuzzlePath>,rng:Random):InfinitePuzzle {
        val shuffled=paths.toMutableList();java.util.Collections.shuffle(shuffled,rng)
        val puzzle=InfinitePuzzle(PuzzleKind.Arrows,level,variation,VERSION,seed,size,shuffled,emptyList())
        check(paths.all { a->a.cells.all { c->(c.x-a.head.x)*a.direction.x+(c.y-a.head.y)*a.direction.y<=0 } }) {"Arrow head must lie on its outward support plane"}
        check(InfiniteEngine.validate(puzzle)) {"Folded arrow construction failed swept validation"}
        val solution=puzzle.arrowGeometry.solve() ?: error("Cyclic arrow dependencies")
        if(level>=3)check(puzzle.arrowGeometry.removable(paths.map {it.id}.toSet()).size<paths.size) {"Trivial arrow puzzle"}
        if(level>=7) {
            val depth=ArrowNestedLayout.depth(level)
            check(puzzle.arrowGeometry.depth>=depth) {"Nested board must require a multi-step release chain"}
            check(puzzle.arrowGeometry.removable(paths.map {it.id}.toSet()).size<=(paths.size+depth-1)/depth) {"Too many independent opening moves"}
        }
        return puzzle.copy(solution=solution).also {it.arrowGeometry}
    }
}
