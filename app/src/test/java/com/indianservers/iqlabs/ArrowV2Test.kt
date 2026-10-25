package com.indianservers.iqlabs

import com.indianservers.iqlabs.puzzle.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.ceil

class ArrowV2Test {
    private fun puzzle(paths:List<List<Cell>>) = InfinitePuzzle(PuzzleKind.Arrows,4,0,2,0,12,paths.mapIndexed { id,c -> PuzzlePath(id,c) },emptyList())
    @Test fun inwardCoilIsRejectedInEveryDirectionAndSelfEdgesBlockTheSolver() {
        val controls=listOf(Cell(0,0),Cell(4,0),Cell(4,4),Cell(0,4),Cell(0,1),Cell(2,1))
        val cells=mutableListOf(controls.first())
        controls.zipWithNext().forEach { (a,b)->
            val d=Cell((b.x-a.x).coerceIn(-1,1),(b.y-a.y).coerceIn(-1,1));var c=a
            while(c!=b) {c+=d;cells+=c}
        }
        repeat(4) { rotation->
            val rotated=cells.map { c->when(rotation) {1->Cell(6-c.y,c.x);2->Cell(6-c.x,6-c.y);3->Cell(c.y,6-c.x);else->c} }
            val a=PuzzlePath(0,rotated)
            val p=InfinitePuzzle(PuzzleKind.Arrows,3,0,4,0,8,listOf(a),emptyList())
            assertTrue(ArrowCollisionEngine.selfBlocked(a))
            assertEquals(setOf(0),p.arrowGeometry.blockers[0])
            assertTrue(p.arrowGeometry.removable(setOf(0)).isEmpty())
            assertNull(p.arrowGeometry.solve())
            assertFalse(InfiniteEngine.validate(p))
        }
    }
    @Test fun mutuallyFacingArrowsCannotPassValidation() {
        val p=puzzle(listOf(listOf(Cell(1,1),Cell(2,1)),listOf(Cell(5,1),Cell(4,1))))
        assertNull(p.arrowGeometry.solve())
        assertFalse(InfiniteEngine.validate(p))
    }
    @Test fun rigidBodyIsBlockedEvenWhenTheHeadRayIsClear() {
        val p=puzzle(listOf(
            listOf(Cell(1,1),Cell(1,2),Cell(2,2),Cell(2,1),Cell(3,1)),
            listOf(Cell(5,2),Cell(6,2))
        ))
        assertTrue(InfiniteEngine.escape(p.paths[0],p.size).none { it in p.paths[1].cells })
        assertEquals(setOf(1),p.arrowGeometry.blockers[0])
        assertEquals(listOf(1),p.arrowGeometry.removable(setOf(0,1)))
        assertEquals(listOf(1,0),p.arrowGeometry.solve())
    }
    @Test fun rigidAnimationPreservesEverySegmentAtEveryProgress() {
        val p=ArrowLevelGenerator.generate(100,54)
        p.paths.take(12).forEach { a ->
            listOf(0f,.07f,.3f,.8f,1f).forEach { progress ->
                a.cells.zipWithNext().forEach { (u,v) ->
                    val x=ArrowCollisionEngine.rigidPosition(u,a,p.size,progress)
                    val y=ArrowCollisionEngine.rigidPosition(v,a,p.size,progress)
                    assertEquals((v.x-u.x).toFloat(),y.first-x.first,.0001f)
                    assertEquals((v.y-u.y).toFloat(),y.second-x.second,.0001f)
                }
            }
        }
    }
    @Test fun intricateCurriculumStartsAtThreeAndIncreasesEachLevel() {
        (1L..30L).forEach { level ->
            repeat(10) { seed ->
                val p=ArrowLevelGenerator.generate(level,seed.toLong())
                val profile=ArrowLevelGenerator.profile(level)
                assertEquals(if(level<=2)3+level.toInt() else level.toInt()+9,p.paths.size)
                assertTrue(p.paths.all { ArrowCollisionEngine.folds(it) in profile.minFolds..profile.maxFolds })
                assertEquals(profile.count*(profile.minFolds+profile.maxFolds)/2,p.paths.sumOf { ArrowCollisionEngine.folds(it) })
                assertTrue(p.paths.all { it.cells.distinct().size==it.cells.size })
                if(level>=3) assertTrue(p.arrowGeometry.removable(p.paths.map { it.id }.toSet()).size<p.paths.size)
            }
        }
        val levelThree=ArrowLevelGenerator.generate(3,18)
        assertEquals(12,levelThree.paths.size)
        assertTrue(levelThree.paths.any { a->val d=a.cells.zipWithNext().map { (u,v)->v-u }.toSet();d.containsAll(listOf(Cell(1,0),Cell(-1,0),Cell(0,1),Cell(0,-1))) })
        (3L..300L).zipWithNext().forEach { (a,b)->
            val x=ArrowLevelGenerator.profile(a);val y=ArrowLevelGenerator.profile(b)
            assertTrue(y.count>x.count)
            assertTrue(y.count*(y.minFolds+y.maxFolds)>x.count*(x.minFolds+x.maxFolds))
        }
        assertTrue(ArrowLevelGenerator.generate(1000,18).paths.any { ArrowCollisionEngine.folds(it)==12 })
    }
    @Test fun indexedSweepsAgreeWithIndependentPairwiseGeometry() {
        repeat(80) { seed ->
            val p=ArrowLevelGenerator.generate(4L+seed%40,seed.toLong())
            p.paths.forEach { a ->
                val brute=p.paths.filter { it.id!=a.id && ArrowCollisionEngine.sweptBlocked(a,it) }.map { it.id }.toSet()
                assertEquals(brute,p.arrowGeometry.blockers[a.id])
            }
        }
    }
    @Test fun levelSevenNestsArrowsWithFewOpeningsAndAnInsideOutOrder() {
        listOf(7L,14,30,100,300,1000).forEach { level->
            repeat(4) { seed->
                val p=ArrowLevelGenerator.generate(level,seed.toLong())
                data class Bounds(val left:Int,val top:Int,val right:Int,val bottom:Int)
                val boxes=p.paths.associate { a->a.id to Bounds(a.cells.minOf { it.x },a.cells.minOf { it.y },a.cells.maxOf { it.x },a.cells.maxOf { it.y }) }
                val order=p.solution.withIndex().associate { it.value to it.index }
                var nested=0
                p.paths.forEach { child->
                    val c=boxes.getValue(child.id)
                    val wrappers=p.paths.filter { parent->
                        val b=boxes.getValue(parent.id)
                        parent.id!=child.id && b.left<c.left && b.top<c.top && b.right>c.right && b.bottom>c.bottom
                    }
                    if(wrappers.isNotEmpty())nested++
                    wrappers.forEach { parent->
                        assertTrue(p.arrowGeometry.blockers.getValue(parent.id).contains(child.id))
                        assertFalse(p.arrowGeometry.blockers.getValue(child.id).contains(parent.id))
                        assertTrue(order.getValue(child.id)<order.getValue(parent.id))
                    }
                }
                assertTrue(nested>=p.paths.size*2/3)
                assertTrue(p.arrowGeometry.removable(p.paths.map { it.id }.toSet()).size<=ceil(p.paths.size/4.0).toInt())
                assertTrue(p.arrowGeometry.depth>=4)
                assertTrue(p.paths.none(ArrowCollisionEngine::selfBlocked))
            }
        }
    }
    @Test fun thousandSeedsAcrossTiersNeverHaveDeadlocks() {
        val levels=listOf(1L,3,4,10,20,30,31,60,100,200,500,1000,5000,Long.MAX_VALUE)
        repeat(1000) { seed ->
            val p=ArrowLevelGenerator.generate(levels[seed%levels.size],seed.toLong())
            assertTrue(InfiniteEngine.validate(p))
            assertTrue(p.paths.all { ArrowCollisionEngine.folds(it) in 2..12 })
            // Independent support-plane oracle, stronger than a single head ray:
            // no part of the body may project in front of its own head.
            p.paths.forEach { a->assertTrue(a.cells.all { c->(c.x-a.head.x)*a.direction.x+(c.y-a.head.y)*a.direction.y<=0 }) }
            val alive=p.paths.map { it.id }.toMutableSet()
            // Any valid removal can only delete blockers; it cannot introduce a cycle.
            p.solution.forEach { id -> assertTrue(p.arrowGeometry.blocking(id,alive).isEmpty());alive.remove(id) }
            assertTrue(alive.isEmpty())
            if(seed%100==0) assertEquals(p,ArrowLevelGenerator.generate(p.level,p.variation))
        }
    }
    @Test fun cameraTransformsInvertAtEveryZoomAndKeepTheFocalWorldPoint() {
        listOf(.25f,1f,5f).forEach { zoom ->
            val c=ArrowCamera(50f,50f,zoom)
            val screen=c.worldToScreen(45f,52f,400f,600f,100)
            val world=c.screenToWorld(screen.x,screen.y,400f,600f,100)
            assertEquals(45f,world.x,.0001f);assertEquals(52f,world.y,.0001f)
        }
        val c=ArrowCamera(50f,50f,1f)
        val before=c.screenToWorld(220f,280f,400f,600f,100)
        val changed=c.zoomAt(1.5f,220f,280f,400f,600f,100)
        val after=changed.screenToWorld(220f,280f,400f,600f,100)
        assertEquals(before.x,after.x,.0001f);assertEquals(before.y,after.y,.0001f)
        assertEquals(.25f,ArrowCamera.fit(250).zoom,0f)
    }
    @Test fun benchmarkRequestedBoardSizes() {
        listOf(41L to 50,91L to 100,241L to 250,491L to 500,641L to 650,1000L to 750).forEach { (level,count) ->
            val start=System.nanoTime()
            val p=ArrowLevelGenerator.generate(level,178)
            assertEquals(count,p.paths.size)
            assertNotNull(p.arrowGeometry.solve())
            println("Arrow benchmark: $count arrows, ${p.size}x${p.size}, ${(System.nanoTime()-start)/1000000}ms generation + validation")
        }
    }
}
