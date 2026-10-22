package com.indianservers.iqlabs

import com.indianservers.iqlabs.game.QuickSolvePraise
import org.junit.Assert.*
import org.junit.Test

class QuickSolvePraiseTest {
    @Test fun fourteenMessagesRotateWithoutRepeatingUntilTheCycleEnds() {
        val praise = QuickSolvePraise()
        val shown = (0..13).map { praise.success(1500, it * 15000L)!! }
        assertEquals(14, shown.distinct().size)
        assertEquals(shown.first(), praise.success(2000, 210000))
    }
    @Test fun slowAssistedAndRapidRepeatedEventsDoNotTriggerPraise() {
        val praise = QuickSolvePraise()
        assertNull(praise.success(8001, 0))
        assertNull(praise.success(1000, 1000, assisted = true))
        assertNotNull(praise.success(8000, 2000))
        assertNull(praise.success(1000, 3000))
        assertNotNull(praise.success(1000, 14000))
    }
}
