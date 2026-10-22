package com.indianservers.iqlabs.puzzle

import java.util.Random
import kotlin.math.*

/** Seeded constraint construction, not a collection of fixed layouts.
 * Each folded path lies in a disjoint routing cell. Early levels use directed
 * bands; later levels use an outward potential field. For an outward exit every
 * blocker has strictly larger Chebyshev tile potential, hence no dependency
 * cycle can occur. A separate swept-geometry solver still validates each board.
 */
object ArrowLevelGenerator {
    const val VERSION=2
    data class Profile(val count:Int,val minFolds:Int,val maxFolds:Int,val columns:Int,val radial:Boolean)
    fun profile(level:Long): Profile {
        require(level>0)
        val count=when {
            level<=3 -> 3+level.toInt()
            level<=15 -> 7+((level-3)*1).toInt()
            level<=30 -> 18+((level-15)*1).toInt()
            level<=60 -> 33+((level-30)*1).toInt()
            level<=100 -> 63+((level-60)*1).toInt()
            level<=200 -> 103+((level-100)*1).toInt()
            level<=500 -> 203+((level-200)*197/300).toInt()
            level<=1000 -> 400+((level-500)/2).toInt()
            else -> 650+((level-1001)%101).toInt()
        }
        val maxFolds=when { level<=3 -> 2; level<=10 -> 3; level<=20 -> 4; level<=30 -> 5; level<=60 -> 7; level<=100 -> 9; else -> 12 }
        val columns=when { level<=3 -> 2+level.toInt()/3; level<=15 -> 3; level<=30 -> 4; else -> ceil(sqrt(count.toDouble())).toInt() }
        return Profile(count,2,maxFolds,columns,level>30)
    }
    fun tier(level:Long)=when {level<=3->0;level<=15->1;level<=30->2;level<=60->3;level<=100->4;level<=200->5;else->6}
    fun generate(level:Long,variation:Long):InfinitePuzzle {
        val seed=InfiniteEngine.seed(PuzzleKind.Arrows,level,variation)
        val rng=Random(seed);val profile=profile(level)
        val columns=profile.columns;val rows=(profile.count+columns-1)/columns
        val pitch=profile.maxFolds+7
        val size=max(columns,rows)*pitch+2
        val paths=ArrayList<PuzzlePath>(profile.count)
        val rowRight=BooleanArray(rows) { rng.nextBoolean() }
        val rotation=rng.nextInt(4)
        fun rotate(c:Cell):Cell=when(rotation) { 1->Cell(size-1-c.y,c.x);2->Cell(size-1-c.x,size-1-c.y);3->Cell(c.y,size-1-c.x);else->c }
        repeat(profile.count) { id ->
            val col=id%columns;val row=id/columns
            val folds=profile.minFolds+rng.nextInt(profile.maxFolds-profile.minFolds+1)
            // A simple x-monotone orthogonal walk: alternating lateral folds
            // never revisit a point, while segment lengths vary per seed.
            val horizontal=if(profile.radial) abs(col-(columns-1)/2.0)>=abs(row-(rows-1)/2.0) else true
            val positive=if(profile.radial) {
                if(horizontal) col>=(columns-1)/2.0 else row>=(rows-1)/2.0
            } else rowRight[row]
            val controls=mutableListOf(Cell(0,0));var x=0;var y=0
            val firstHorizontal=folds%2==0
            val height=2+rng.nextInt(max(1,pitch-5))
            for(segment in 0..folds) {
                val along=if(segment%2==0) firstHorizontal else !firstHorizontal
                if(along) x+=1+rng.nextInt(2) else y=if(y==0) height else 0
                controls+=Cell(x,y)
            }
            val maxX=controls.maxOf { it.x };val maxY=controls.maxOf { it.y }
            // Scale-free integer translation centers all paths on shared routing
            // axes, creating genuine blockers rather than independent buttons.
            val local=controls.map { c -> Cell(c.x+(pitch-maxX)/2,c.y+(pitch-maxY)/2) }
            val oriented=local.map { c ->
                val q=if(positive)c else Cell(pitch-c.x,c.y)
                if(horizontal)q else Cell(q.y,q.x)
            }
            val cells=mutableListOf<Cell>()
            oriented.zipWithNext().forEach { (a,b) ->
                if(cells.isEmpty())cells+=a
                val d=Cell((b.x-a.x).sign,(b.y-a.y).sign)
                var c=a
                while(c!=b) { c+=d;cells+=c }
            }
            paths+=PuzzlePath(id,cells.map { rotate(Cell(1+col*pitch+it.x,1+row*pitch+it.y)) })
        }
        val shuffled=paths.toMutableList();java.util.Collections.shuffle(shuffled,rng)
        val puzzle=InfinitePuzzle(PuzzleKind.Arrows,level,variation,VERSION,seed,size,shuffled,emptyList())
        check(InfiniteEngine.validate(puzzle)) { "Folded arrow construction failed swept validation" }
        val solution=puzzle.arrowGeometry.solve() ?: error("Cyclic arrow dependencies")
        if(level>3) check(puzzle.arrowGeometry.removable(paths.map { it.id }.toSet()).size<paths.size) { "Trivial arrow puzzle" }
        return puzzle.copy(solution=solution).also { it.arrowGeometry }
    }
}
