package com.indianservers.iqlabs

import com.indianservers.iqlabs.puzzle.*
import org.junit.Assert.*
import org.junit.Test

class InfinitePuzzleTest {
    @Test fun twentyThousandGeneratedPuzzlesAreValidAndDeterministic() {
        PuzzleKind.entries.forEach { kind ->
            val signatures = mutableSetOf<String>()
            repeat(10000) { sample ->
                val level = 1L + sample % 120
                val p = InfiniteEngine.generateFresh(kind, level, sample.toLong()) { it in signatures }
                assertTrue("$kind $sample", InfiniteEngine.validate(p))
                if (InfiniteEngine.tier(level) == 6) assertTrue("High tier object count $kind: ${p.paths.size}", p.paths.size >= if (kind == PuzzleKind.Arrows) 40 else 12)
                assertTrue("Duplicate $kind $sample", signatures.add(p.signature))
                if (sample % 100 == 0) assertEquals(p, InfiniteEngine.generate(kind, level, p.variation))
                if (kind == PuzzleKind.Arrows) {
                    val remaining = p.paths.map { it.id }.toMutableSet()
                    p.solution.forEach { id -> assertTrue(id in InfiniteEngine.removable(p, remaining)); remaining.remove(id) }
                    assertTrue(remaining.isEmpty())
                } else {
                    val solution = InfiniteEngine.solveColours(p, emptyMap())!!
                    assertTrue(solution.all { (id, route) -> InfiniteEngine.validRoute(p,id,route,solution,true) })
                }
            }
        }
    }
    private fun board(kind: PuzzleKind, paths: List<List<Cell>>) = InfinitePuzzle(kind,1,0,1,0,6,paths.mapIndexed { id, cells -> PuzzlePath(id,cells) },emptyList())
    @Test fun blockedArrowBecomesRemovableAfterObstacleLeaves() {
        val p = board(PuzzleKind.Arrows,listOf(listOf(Cell(0,1),Cell(1,1)),listOf(Cell(3,1),Cell(3,0))))
        assertEquals(listOf(1),InfiniteEngine.removable(p,setOf(0,1)))
        assertEquals(listOf(0),InfiniteEngine.removable(p,setOf(0)))
        assertEquals(Cell(1,0),p.paths[0].direction)
        assertEquals(listOf(1,0),InfiniteEngine.solveArrows(p))
    }
    @Test fun windingArrowKeepsItsDirectedHead() {
        val p = board(PuzzleKind.Arrows,listOf(listOf(Cell(0,0),Cell(0,1),Cell(1,1),Cell(2,1),Cell(2,0))))
        assertEquals(Cell(0,-1),p.paths[0].direction)
        assertTrue(InfiniteEngine.escape(p.paths[0],6).isEmpty())
        assertTrue(InfiniteEngine.validate(p))
    }
    @Test fun colourCrossingTouchAndForeignEndpointsAreRejected() {
        val p = board(PuzzleKind.Colours,listOf(listOf(Cell(0,1),Cell(1,1),Cell(2,1)),listOf(Cell(1,0),Cell(2,0),Cell(3,0),Cell(3,1),Cell(3,2),Cell(2,2),Cell(1,2))))
        val a = p.paths[0].cells
        assertFalse(InfiniteEngine.validRoute(p,1,listOf(Cell(1,0),Cell(1,1),Cell(1,2)),mapOf(0 to a),true))
        assertTrue(InfiniteEngine.validRoute(p,0,a.reversed(),emptyMap(),true))
        assertTrue(InfiniteEngine.validRoute(p,0,a.take(2),emptyMap(),false))
        assertFalse(InfiniteEngine.validRoute(p,0,a.take(2),emptyMap(),true))
        assertFalse(InfiniteEngine.validRoute(p,0,listOf(Cell(0,1),Cell(-1,1)),emptyMap(),false))
        assertFalse(InfiniteEngine.validRoute(p,0,listOf(Cell(0,1),Cell(1,1),Cell(0,1)),emptyMap(),false))
        assertFalse(InfiniteEngine.validRoute(p,0,listOf(Cell(0,1),Cell(0,0),Cell(1,0)),emptyMap(),false))
    }
    @Test fun hintsRespectExistingRoutesAndBudget() {
        val p = InfiniteEngine.generate(PuzzleKind.Colours,44)
        val first = p.paths.first()
        val result = InfiniteEngine.solveColours(p,mapOf(first.id to first.cells.reversed()))!!
        assertEquals(first.cells.reversed(),result[first.id])
        result.forEach { (id, route) -> assertTrue(InfiniteEngine.validRoute(p,id,route,result,true)) }
    }
    @Test fun progressionAndLivesHaveSafeBoundaries() {
        assertEquals(Long.MAX_VALUE,InfiniteEngine.next(Long.MAX_VALUE))
        assertEquals(6,InfiniteEngine.tier(Long.MAX_VALUE))
        assertEquals(5,PuzzleMode.Normal.lives)
        assertEquals(3,PuzzleMode.Challenge.lives)
        assertTrue(InfiniteEngine.validate(InfiniteEngine.generate(PuzzleKind.Arrows,Long.MAX_VALUE)))
    }
    @Test fun canonicalSignaturesIgnoreHiddenColourRoutesAndObjectOrdering() {
        val a = board(PuzzleKind.Colours,listOf(listOf(Cell(0,0),Cell(1,0),Cell(2,0))))
        val b = a.copy(paths=listOf(PuzzlePath(7,listOf(Cell(2,0),Cell(2,1),Cell(1,1),Cell(0,1),Cell(0,0)))))
        assertEquals(a.signature,b.signature)
        val arrows = InfiniteEngine.generate(PuzzleKind.Arrows,21)
        assertEquals(arrows.signature,arrows.copy(paths=arrows.paths.reversed()).signature)
    }
    @Test fun recoveryAdviceAlwaysHasAVerifiedContinuation() {
        val p = InfiniteEngine.generate(PuzzleKind.Colours,35)
        val first=p.paths.first()
        val advice=InfiniteEngine.colourAdvice(p,mapOf(first.id to first.cells.reversed()))
        assertTrue(advice.revise.isEmpty())
        advice.solution.forEach { (id, route) -> assertTrue(InfiniteEngine.validRoute(p,id,route,advice.solution,true)) }
    }

}
