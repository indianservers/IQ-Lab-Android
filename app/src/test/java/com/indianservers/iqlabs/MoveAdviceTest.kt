package com.indianservers.iqlabs

import com.indianservers.iqlabs.game.MoveAdvice
import org.junit.Assert.*
import org.junit.Test

class MoveAdviceTest {
    @Test fun towerAdviceUsesCurrentStacksAndHeldDisc() {
        assertEquals("Lift disc 1 from peg A, then place it on C.", MoveAdvice.tower(listOf(listOf(3,2,1), emptyList(), emptyList()), null))
        assertEquals("Place disc 1 on peg C.", MoveAdvice.tower(listOf(listOf(3,2), emptyList(), emptyList()), 1))
        assertEquals("Lift disc 2 from peg A, then place it on B.", MoveAdvice.tower(listOf(listOf(3,2), emptyList(), listOf(1)), null))
    }
    @Test fun latinAdviceCorrectsConflictsAndRecognisesCompletion() {
        val solved = listOf(1,2,3,4,2,3,4,1,3,4,1,2,4,1,2,3)
        val given = solved.toMutableList().apply { this[0] = 0; this[1] = 0 }
        assertEquals("Select row 1, column 1 and enter 1.", MoveAdvice.latin(given, given))
        assertEquals("Select row 1, column 1 and enter 1.", MoveAdvice.latin(given.toMutableList().apply { this[0] = 2 }, given))
        assertEquals("Tap Check grid.", MoveAdvice.latin(solved, given))
    }
}
