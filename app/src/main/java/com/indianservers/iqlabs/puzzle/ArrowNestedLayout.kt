package com.indianservers.iqlabs.puzzle

import java.util.Random
import kotlin.math.*

/** Nested open frames with an unobstructed common exit corridor.
 * Each wrapper waits for its contents, while its contents cannot hit the wrapper
 * on their way out. Across groups, outward Manhattan potential orders blockers.
 * The resulting lexicographic order rules out both local and global cycles. */
internal object ArrowNestedLayout {
    fun depth(level: Long) = when { level<14->4;level<30->5;level<60->6;level<100->7;else->8 }

    fun generate(level:Long,folds:IntArray,maxFolds:Int,rng:Random):Pair<Int,List<PuzzlePath>> {
        val depth=depth(level)
        val groups=(folds.size+depth-1)/depth
        val columns=ceil(sqrt(groups.toDouble())).toInt()
        val rows=(groups+columns-1)/columns
        val base=maxFolds*2+7
        val pitch=base+(depth-1)*14+4
        val size=max(columns,rows)*pitch+2
        val rotation=rng.nextInt(4)
        fun rotate(c:Cell)=when(rotation) {1->Cell(size-1-c.y,c.x);2->Cell(size-1-c.x,size-1-c.y);3->Cell(c.y,size-1-c.x);else->c}
        val paths=ArrayList<PuzzlePath>(folds.size)
        repeat(groups) { group->
            val first=group*depth;val count=min(depth,folds.size-first)
            val outlines=Array(count) { emptyList<Cell>() }
            outlines[count-1]=ArrowLevelGenerator.localPath(folds[first+count-1],base,rng,rng.nextBoolean())
            var left=0;var top=0;var right=base;var bottom=base
            for(layer in count-2 downTo 0) {
                val centreY=top+bottom
                val margin=2+rng.nextInt(2)
                left-=3+rng.nextInt(5);right+=3+rng.nextInt(5);top-=margin;bottom+=margin+8
                // Tail folds stay below ALL child geometry. The right side is
                // open above this strip, including every child's swept lane.
                val gap=2+rng.nextInt(2)
                var x=right;var y=bottom
                val prefix=mutableListOf(Cell(x,y))
                repeat(folds[first+layer]-2) { segment->
                    if(segment%2==0)y=bottom-gap*(if(segment%4==0)1 else 2)
                    else x-=2+rng.nextInt(2)
                    prefix+=Cell(x,y)
                }
                prefix.reverse()
                val outline=prefix+listOf(Cell(left,bottom),Cell(left,top),Cell(right,top))
                // Alternate upper/lower exit tips independently. All retaining
                // strokes still stay outside the child rectangle and exit lanes.
                if(rng.nextBoolean()) {
                    outlines[layer]=outline.map {Cell(it.x,centreY-it.y)}
                    val previousTop=top;top=centreY-bottom;bottom=centreY-previousTop
                } else outlines[layer]=outline
            }
            val offsetX=(pitch-(right-left))/2-left
            val offsetY=(pitch-(bottom-top))/2-top
            val col=group%columns;val row=group/columns
            val horizontal=rng.nextBoolean()
            val coordinate=if(horizontal)col.toDouble() else row.toDouble()
            val centre=if(horizontal)(columns-1)/2.0 else (rows-1)/2.0
            val positive=if(coordinate==centre)rng.nextBoolean() else coordinate>centre
            outlines.forEachIndexed { layer,outline->
                val controls=outline.map { c->
                    val local=Cell(c.x+offsetX,c.y+offsetY)
                    val q=if(positive)local else Cell(pitch-local.x,local.y)
                    if(horizontal)q else Cell(q.y,q.x)
                }
                val cells=mutableListOf(controls.first())
                controls.zipWithNext().forEach { (a,b)->
                    val d=Cell((b.x-a.x).sign,(b.y-a.y).sign);var c=a
                    while(c!=b) {c+=d;cells+=c}
                }
                paths+=PuzzlePath(first+layer,cells.map {rotate(Cell(1+col*pitch+it.x,1+row*pitch+it.y))})
            }
        }
        return size to paths
    }
}
