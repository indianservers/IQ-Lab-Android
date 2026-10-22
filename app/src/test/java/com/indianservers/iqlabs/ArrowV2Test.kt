package com.indianservers.iqlabs

import com.indianservers.iqlabs.puzzle.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class ArrowV2Test {
    private fun puzzle(paths:List<List<Cell>>) = InfinitePuzzle(PuzzleKind.Arrows,4,0,2,0,12,paths.mapIndexed { id,c -> PuzzlePath(id,c) },emptyList())
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
    @Test fun gentleThirtyLevelCurriculumHasRealDependenciesAndFoldedPaths() {
        (1L..30L).forEach { level ->
            repeat(10) { seed ->
                val p=ArrowLevelGenerator.generate(level,seed.toLong())
                assertTrue(p.paths.size<=33)
                if(level>=4) assertTrue(p.paths.size>=8)
                assertTrue(p.paths.all { ArrowCollisionEngine.folds(it) in 2..5 })
                assertTrue(p.arrowGeometry.depth<=4)
                if(level>3) assertTrue(p.arrowGeometry.removable(p.paths.map { it.id }.toSet()).size<p.paths.size)
            }
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
    @Test fun thousandSeedsAcrossTiersNeverHaveDeadlocks() {
        val levels=listOf(1L,3,4,10,20,30,31,60,100,200,500,1000,5000,Long.MAX_VALUE)
        repeat(1000) { seed ->
            val p=ArrowLevelGenerator.generate(levels[seed%levels.size],seed.toLong())
            assertTrue(InfiniteEngine.validate(p))
            assertTrue(p.paths.all { ArrowCollisionEngine.folds(it) in 2..12 })
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
        listOf(47L to 50,97L to 100,272L to 250,700L to 500,1000L to 650).forEach { (level,count) ->
            val start=System.nanoTime()
            val p=ArrowLevelGenerator.generate(level,178)
            assertEquals(count,p.paths.size)
            assertNotNull(p.arrowGeometry.solve())
            println("Arrow benchmark: $count arrows, ${p.size}x${p.size}, ${(System.nanoTime()-start)/1000000}ms generation + validation")
        }
    }
}
