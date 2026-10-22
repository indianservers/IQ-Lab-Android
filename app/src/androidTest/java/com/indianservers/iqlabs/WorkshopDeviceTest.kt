package com.indianservers.iqlabs

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.indianservers.iqlabs.puzzle.PuzzleMode
import com.indianservers.iqlabs.workshop.*
import org.junit.*
import org.junit.Assert.*
import kotlin.math.*

class WorkshopDeviceTest {
    @get:Rule val compose=createComposeRule()
    private val context get()=InstrumentationRegistry.getInstrumentation().targetContext
    private val store get()=WorkshopStore(context,"iq_workshop_device_tests")
    @Before fun isolatedProgress() { context.getSharedPreferences("iq_workshop_device_tests",0).edit().clear().commit() }
    private fun waitText(text:String) { compose.waitUntil(15000) { runCatching { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) } }
    private fun open(kind:WorkshopKind,puzzle:WorkshopPuzzle,mode:PuzzleMode=PuzzleMode.Normal) {
        store.save(WorkshopSession.fresh(puzzle,mode))
        compose.setContent { MaterialTheme { WorkshopScreen(kind,false,false,true,{}, {},{},storage=store) } }
        waitText("Continue · Level ${puzzle.level}")
        screenshot(if(kind==WorkshopKind.Laser)"laser-home" else "rings-home")
        compose.onNodeWithText("Continue · Level ${puzzle.level}").performClick()
    }
    private fun screenshot(name:String) {
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()?.let { bitmap-> context.openFileOutput("workshop-$name.png",0).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) } }
    }
    @Test fun realMirrorTapCompletesTheOpticalPuzzleWithoutLifeCost() {
        val p=LaserGenerator.generate(1,73)
        open(WorkshopKind.Laser,p)
        screenshot("laser-play")
        val mirror=p.objects.first { it.movable }
        compose.onNodeWithContentDescription("Laser puzzle board").performTouchInput {
            val unit=min((width-20f)/p.columns,(height-20f)/p.rows)
            val origin=Offset((width-p.columns*unit)/2,(height-p.rows*unit)/2)
            click(origin+Offset(mirror.position.x.toFloat(),mirror.position.y.toFloat())*unit)
        }
        compose.waitUntil(15000) { store.active(WorkshopKind.Laser)!!.complete }
        waitText("Level Complete!")
        assertEquals(5,store.active(WorkshopKind.Laser)!!.lives)
        assertTrue(store.highest(WorkshopKind.Laser)>=2)
        screenshot("laser-complete")
    }
    @Test fun challengeChecksCostOneLifeAndRetryRestoresTheSameBoard() {
        val p=LaserGenerator.generate(11,74)
        open(WorkshopKind.Laser,p,PuzzleMode.Challenge)
        repeat(3) { i->
            compose.onNodeWithText("Test beam · failed check costs 1 life").performClick()
            compose.waitUntil(15000) { store.active(WorkshopKind.Laser)!!.lives==2-i }
        }
        compose.onNodeWithText("Retry Level").performClick()
        compose.waitUntil(15000) { store.active(WorkshopKind.Laser)!!.lives==3 }
        assertEquals(p,store.active(WorkshopKind.Laser)!!.puzzle)
        assertEquals(p.initial,store.active(WorkshopKind.Laser)!!.state.mirrors)
    }
    @Test fun laserSolverHintsReachAllTargetsFromTheCurrentConfiguration() {
        val p=LaserGenerator.generate(1001,77)
        open(WorkshopKind.Laser,p)
        screenshot("laser-advanced")
        repeat(4) { index ->
            compose.onNodeWithContentDescription("Next move hint").performClick()
            compose.waitUntil(15000) { store.active(WorkshopKind.Laser)!!.hintsUsed >= index+1 }
        }
        assertFalse(store.active(WorkshopKind.Laser)!!.practice)
        assertTrue(store.active(WorkshopKind.Laser)!!.score > 0)
        repeat(10) {
            if(!store.active(WorkshopKind.Laser)!!.complete) {
                val before=store.active(WorkshopKind.Laser)!!.moves
                compose.onNodeWithContentDescription("Hint").performClick()
                compose.onNodeWithText("3 · Perform one valid move").performClick()
                compose.waitUntil(15000) { store.active(WorkshopKind.Laser)!!.moves>before }
            }
        }
        assertTrue(store.active(WorkshopKind.Laser)!!.complete)
        assertEquals(5,store.active(WorkshopKind.Laser)!!.lives)
    }
    @Test fun realRingDragsReleaseEveryRingAndUndoRestoresTheRemovedRing() {
        val p=RingGenerator.generate(1,83)
        open(WorkshopKind.Rings,p)
        screenshot("rings-play")
        fun drag(move:RingMove) {
            val ring=p.rings.first { it.id==move.id }
            val old=store.active(WorkshopKind.Rings)!!.state.angles.getValue(move.id)
            val delta=((move.angle-old+540)%360)-180
            compose.onNodeWithContentDescription("Ring puzzle board").performTouchInput {
                val projection=RingProjection.of(p,width.toFloat(),height.toFloat())
                down(projection.screen(ring.centre+Vec(ring.radius,0.0)))
                val steps=max(2,ceil(abs(delta)/15).toInt())
                repeat(steps) { index->val a=Math.toRadians(delta*(index+1)/steps);moveTo(projection.screen(ring.centre+Vec(cos(a),sin(a))*ring.radius),45) }
                up()
            }
            compose.waitUntil(15000) { move.id in store.active(WorkshopKind.Rings)!!.state.released }
        }
        val first=p.solution.first();drag(first)
        compose.onNodeWithContentDescription("Undo").performClick()
        compose.waitUntil(15000) { store.active(WorkshopKind.Rings)!!.state.released.isEmpty() }
        assertEquals(p.initial,store.active(WorkshopKind.Rings)!!.state.angles)
        p.solution.forEach(::drag)
        assertTrue(store.active(WorkshopKind.Rings)!!.complete)
        assertEquals(5,store.active(WorkshopKind.Rings)!!.lives)
        screenshot("rings-complete")
    }
    @Test fun blockedReleaseCostsOneLifeWhileOrdinaryRotationIsFree() {
        val p=RingGenerator.generate(30,89)
        open(WorkshopKind.Rings,p)
        val ring=p.rings.first()
        compose.onNodeWithContentDescription("Ring puzzle board").performTouchInput {
            val projection=RingProjection.of(p,width.toFloat(),height.toFloat())
            click(projection.screen(ring.centre+Vec(1.0,0.0)))
        }
        compose.onNodeWithText("Release ring").performClick()
        compose.waitUntil(15000) { store.active(WorkshopKind.Rings)!!.lives==4 }
        assertTrue(store.active(WorkshopKind.Rings)!!.state.released.isEmpty())
        compose.onNodeWithText("Turn 15°").performClick()
        compose.waitUntil(15000) { store.active(WorkshopKind.Rings)!!.moves==1 }
        assertEquals(4,store.active(WorkshopKind.Rings)!!.lives)
        compose.onNodeWithContentDescription("Restart").performClick()
        compose.waitUntil(15000) { store.active(WorkshopKind.Rings)!!.lives==5 }
        assertEquals(p.initial,store.active(WorkshopKind.Rings)!!.state.angles)
    }
    @Test fun canonicalGeometryUndoAndProgressSurviveStoreRecreation() {
        val p=RingGenerator.generate(301,92)
        val initial=WorkshopSession.fresh(p,PuzzleMode.Relaxed)
        val move=p.solution.first()
        val changed=initial.changed(initial.state.copy(angles=initial.state.angles+(move.id to move.angle),released=setOf(move.id))).copy(seconds=94,hintsUsed=1)
        store.save(changed)
        val restored=WorkshopStore(context,"iq_workshop_device_tests").active(WorkshopKind.Rings)!!
        assertEquals(changed,restored)
        assertEquals(initial.state,restored.undo.last())
        assertEquals(p,store.saved(WorkshopKind.Rings,p.level)!!.puzzle)
        val completed=changed.copy(state=changed.state.copy(released=p.rings.map { it.id }.toSet()))
        store.save(completed)
        assertTrue(store.claimReward(completed));assertFalse(store.claimReward(completed))
        assertEquals(p.level+1,store.highest(WorkshopKind.Rings))
        store.save(WorkshopSession.fresh(RingGenerator.generate(p.level,93),PuzzleMode.Normal))
        assertEquals(p,store.saved(WorkshopKind.Rings,p.level)!!.puzzle)
        assertEquals(p.level+1,store.highest(WorkshopKind.Rings))
    }
}
