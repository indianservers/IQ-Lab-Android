package com.indianservers.iqlabs

import com.indianservers.iqlabs.workshop.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class WorkshopEngineTest {
    private fun laser(objects:List<Optic>,cols:Int=8,rows:Int=8)=LaserLevel(1,0,1,0,cols,rows,objects,emptyMap(),emptyMap())
    @Test fun twentyThousandSampledLevelsAreIndependentlyValidated() {
        val levels=listOf(1L,8,11,30,31,75,76,150,151,300,301,600,601,1000,1001,50000,Long.MAX_VALUE)
        repeat(10000) { sample ->
            val level=levels[sample%levels.size]
            val l=LaserGenerator.generate(level,sample.toLong())
            assertTrue(LaserGenerator.validate(l))
            val solved=LaserSolver.solve(l,l.initial)!!
            assertTrue(LaserEngine.trace(l,solved.orientations).complete)
            val r=RingGenerator.generate(level,sample.toLong())
            assertTrue(RingGenerator.validate(r))
            val remaining=mutableSetOf<Int>()
            RingSolver.solve(r)!!.forEach { m -> assertTrue(RingEngine.clearance(r,m.id,m.angle,remaining).free);remaining+=m.id }
            assertEquals(r.rings.size,remaining.size)
            if(sample%500==0) { assertEquals(l,LaserGenerator.generate(level,sample.toLong()));assertEquals(r,RingGenerator.generate(level,sample.toLong())) }
        }
    }
    @Test fun reflectionUsesTheVectorLaw() {
        val n=Vec(1.0,1.0).unit()
        val reflected=LaserEngine.reflect(Vec(1.0,0.0),n)
        assertEquals(0.0,reflected.x,1e-9);assertEquals(-1.0,reflected.y,1e-9)
        assertEquals(1.0,reflected.length(),1e-9)
        assertEquals(2.0,LaserEngine.raySegment(Vec(0.0,1.0),Vec(1.0,0.0),Vec(2.0,0.0),Vec(2.0,2.0))!!,1e-9)
        assertNull(LaserEngine.raySegment(Vec(0.0,3.0),Vec(1.0,0.0),Vec(2.0,0.0),Vec(2.0,2.0)))
    }
    @Test fun splitterConservesPerChannelPowerAndIlluminatesTwoTargets() {
        val l=laser(listOf(Optic(0,OpticType.Emitter,Vec(.5,3.5),colour=1),Optic(1,OpticType.Splitter,Vec(3.5,3.5)),Optic(2,OpticType.Receiver,Vec(3.5,.5),colour=1),Optic(3,OpticType.Receiver,Vec(7.5,3.5),colour=1)))
        val trace=LaserEngine.trace(l,emptyMap());assertTrue(trace.complete)
        assertTrue(trace.beams.drop(1).all { it.intensity==.5 })
    }
    @Test fun filtersAndDichroicPrismUseActualRgbChannels() {
        val objects=listOf(Optic(0,OpticType.Emitter,Vec(.5,3.5),colour=7),Optic(1,OpticType.Prism,Vec(3.5,3.5)),Optic(2,OpticType.Receiver,Vec(3.5,.5),colour=1),Optic(3,OpticType.Receiver,Vec(7.5,3.5),colour=2),Optic(4,OpticType.Receiver,Vec(3.5,7.5),colour=4))
        assertTrue(LaserEngine.trace(laser(objects),emptyMap()).complete)
        assertFalse(LaserEngine.trace(laser(objects),mapOf(1 to 1)).complete)
        val filter=laser(listOf(Optic(0,OpticType.Emitter,Vec(.5,1.5),colour=7),Optic(1,OpticType.Filter,Vec(3.5,1.5),colour=3),Optic(2,OpticType.Receiver,Vec(7.5,1.5),colour=3)))
        assertTrue(LaserEngine.trace(filter,emptyMap()).complete)
        assertFalse(LaserEngine.trace(filter.copy(objects=filter.objects.map { if(it.id==2)it.copy(colour=1)else it }),emptyMap()).complete)
    }
    @Test fun additiveMixingAndNearestAbsorberAreCorrect() {
        val l=laser(listOf(Optic(0,OpticType.Emitter,Vec(.5,3.5),colour=1),Optic(1,OpticType.Emitter,Vec(4.5,.5),rotation=1,colour=2),Optic(2,OpticType.Receiver,Vec(4.5,3.5),colour=3)))
        assertTrue(LaserEngine.trace(l,emptyMap()).complete)
        val blocked=l.copy(objects=l.objects+Optic(3,OpticType.Absorber,Vec(2.5,3.5)))
        assertFalse(LaserEngine.trace(blocked,emptyMap()).complete)
        assertEquals(3,LaserEngine.nearest(blocked,Vec(.9,3.5),Vec(1.0,0.0),emptyMap())!!.optic.id)
    }
    @Test fun opticalLoopsTerminateAndBudgetFailureIsNotASolution() {
        val l=laser(listOf(Optic(0,OpticType.Emitter,Vec(.5,1.5)),Optic(1,OpticType.Splitter,Vec(2.5,1.5),rotation=1),Optic(2,OpticType.FixedMirror,Vec(2.5,5.5),rotation=0),Optic(3,OpticType.FixedMirror,Vec(5.5,5.5),rotation=1),Optic(4,OpticType.FixedMirror,Vec(5.5,1.5),rotation=0)))
        val trace=LaserEngine.trace(l,emptyMap());assertFalse(trace.truncated);assertTrue(trace.beams.size<40)
        assertNull(LaserSolver.solve(LaserGenerator.generate(30),emptyMap(),0))
    }
    @Test fun ringGeometryAndConnectedCollarsPreventPrematureRelease() {
        val a=CaptiveRing(0,Vec(1.0,1.0),1.0,.14,80.0,0.0,0)
        val b=a.copy(id=1,centre=Vec(3.72,1.0),guide=180.0)
        val c=a.copy(id=2,centre=Vec(1.0,3.72),guide=270.0)
        val l=RingLevel(1,0,1,0,listOf(a,b,c),listOf(RingLink(0,1),RingLink(0,2)),emptyMap(),emptyList())
        assertFalse(RingEngine.clearance(l,0,0.0,emptySet()).free)
        assertTrue(RingEngine.clearance(l,1,180.0,emptySet()).free)
        assertTrue(RingEngine.clearance(l,0,0.0,setOf(1,2)).free)
        assertFalse(RingEngine.clearance(l,0,180.0,setOf(1,2)).free)
        assertFalse(RingEngine.tracksCollide(a,b))
        assertTrue(RingEngine.tracksCollide(a,b.copy(centre=Vec(2.0,1.0))))
        assertFalse(RingEngine.arcOccupied(a,0.0,Vec(2.0,1.0)))
        assertTrue(RingEngine.arcOccupied(a,0.0,Vec(0.0,1.0)))
        assertEquals(0.0,RingEngine.snap(359.0),0.0)
    }
    @Test fun hintsAreValidFromPartialRingStatesAndCurrentMirrorStates() {
        val r=RingGenerator.generate(700,2);val released=mutableSetOf<Int>()
        repeat(r.rings.size) { val hint=RingSolver.hint(r,released,r.initial)!!;assertTrue(RingEngine.clearance(r,hint.id,hint.angle,released).free);released+=hint.id }
        val l=LaserGenerator.generate(800,3);var state=l.initial
        repeat(12) { if(!LaserEngine.trace(l,state).complete) { val hint=LaserSolver.hint(l,state)!!;state=state+(hint.first to hint.second) } }
        assertTrue(LaserEngine.trace(l,state).complete)
    }
}
