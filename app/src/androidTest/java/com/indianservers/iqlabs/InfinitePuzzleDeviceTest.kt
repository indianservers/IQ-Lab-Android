package com.indianservers.iqlabs

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.indianservers.iqlabs.puzzle.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class InfinitePuzzleDeviceTest {
    @get:Rule val compose = createComposeRule()
    @org.junit.Before fun isolateTestProgress() {
        context.getSharedPreferences("iq_puzzle_device_tests",0).edit().clear().commit()
        context.getSharedPreferences("iq_puzzle_reward_test",0).edit().clear().commit()
    }
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun screenshot(name: String) {
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()?.let { bitmap ->
            context.openFileOutput("puzzle-$name.png",0).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
        }
    }
    @Test fun canonicalSessionSurvivesStoreRecreation() {
        val store = PuzzleStore(context, "iq_puzzle_device_tests")
        val p = InfiniteEngine.generate(PuzzleKind.Colours,9876,41).copy(version=99)
        val s = PuzzleSession(p, PuzzleMode.Challenge, routes=mapOf(p.paths.first().id to p.paths.first().cells), lives=2,hints=1,hintsUsed=2,mistakes=1,seconds=87)
        store.save(s)
        assertEquals(s,PuzzleStore(context, "iq_puzzle_device_tests").active(PuzzleKind.Colours))
        assertEquals(s,PuzzleStore(context, "iq_puzzle_device_tests").saved(PuzzleKind.Colours,9876))
    }
    @Test fun arrowScreenCanCompleteUsingVerifiedMoves() {
        val p = InfiniteEngine.generate(PuzzleKind.Arrows,1,71)
        PuzzleStore(context, "iq_puzzle_device_tests").save(PuzzleSession(p))
        compose.setContent { MaterialTheme { InfinitePuzzleScreen(PuzzleKind.Arrows,false,false,true,{}, {}, storage = PuzzleStore(context, "iq_puzzle_device_tests")) } }
        compose.waitUntil(10000) { runCatching { compose.onAllNodesWithText("Continue · Level 1").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        compose.onNodeWithText("Continue · Level 1").performClick()
        screenshot(p.kind.name)
        repeat(p.paths.size) {
            compose.waitUntil(10000) { runCatching { compose.onAllNodesWithText("Show one move").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
            compose.onNodeWithText("Show one move").performClick()
            compose.waitUntil(10000) { PuzzleStore(context, "iq_puzzle_device_tests").active(PuzzleKind.Arrows)!!.removed.size == it+1 }
        }
        compose.onNodeWithText("Beautifully solved!").assertIsDisplayed()
    }
    @Test fun colourScreenCompletesAndUnlocksNextLevel() {
        val p = InfiniteEngine.generate(PuzzleKind.Colours,1,72)
        PuzzleStore(context, "iq_puzzle_device_tests").save(PuzzleSession(p))
        compose.setContent { MaterialTheme { InfinitePuzzleScreen(PuzzleKind.Colours,false,false,true,{}, {}, storage = PuzzleStore(context, "iq_puzzle_device_tests")) } }
        compose.waitUntil(10000) { runCatching { compose.onAllNodesWithText("Continue · Level 1").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        compose.onNodeWithText("Continue · Level 1").performClick()
        screenshot(p.kind.name)
        repeat(p.paths.size) {
            compose.onNodeWithText("Show one move").performClick()
            compose.waitUntil(10000) { PuzzleStore(context, "iq_puzzle_device_tests").active(PuzzleKind.Colours)!!.routes.size == it+1 }
        }
        compose.onNodeWithText("Beautifully solved!").assertIsDisplayed()
        assertTrue(PuzzleStore(context, "iq_puzzle_device_tests").highest(PuzzleKind.Colours)>=2)
    }
    @Test fun realDragGesturesConnectCanonicalColourRoutes() {
        val p = InfiniteEngine.generate(PuzzleKind.Colours,1,129)
        val store = PuzzleStore(context, "iq_puzzle_device_tests")
        store.save(PuzzleSession(p))
        compose.setContent { MaterialTheme { InfinitePuzzleScreen(PuzzleKind.Colours,false,false,true,{}, {},storage=store) } }
        compose.waitUntil(10000) { runCatching { compose.onAllNodesWithText("Continue · Level 1").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        compose.onNodeWithText("Continue · Level 1").performClick()
        p.paths.forEachIndexed { index, path ->
            compose.onNodeWithContentDescription("Puzzle board").performTouchInput {
                fun position(c: Cell) = androidx.compose.ui.geometry.Offset((c.x+1)*width.toFloat()/(p.size+1),(c.y+1)*height.toFloat()/(p.size+1))
                down(position(path.cells.first()))
                path.cells.drop(1).forEach { moveTo(position(it),100) }
                up()
            }
            compose.waitUntil(10000) { store.active(PuzzleKind.Colours)!!.routes.size == index+1 }
            if (index == 0) {
                compose.onNodeWithText("\u21b6 Undo").performClick()
                compose.waitUntil(10000) { store.active(PuzzleKind.Colours)!!.routes.isEmpty() }
                compose.onNodeWithContentDescription("Puzzle board").performTouchInput {
                    fun position(c: Cell) = androidx.compose.ui.geometry.Offset((c.x+1)*width.toFloat()/(p.size+1),(c.y+1)*height.toFloat()/(p.size+1))
                    down(position(path.cells.first()))
                    path.cells.drop(1).forEach { moveTo(position(it),100) }
                    up()
                }
                compose.waitUntil(10000) { store.active(PuzzleKind.Colours)!!.routes.size == 1 }
            }
        }
        assertEquals(0,store.active(PuzzleKind.Colours)!!.mistakes)
        compose.onNodeWithText("Beautifully solved!").assertIsDisplayed()
    }
    @Test fun realArrowTapsFollowVerifiedRemovalOrder() {
        val p = InfiniteEngine.generate(PuzzleKind.Arrows,1,134)
        val store = PuzzleStore(context, "iq_puzzle_device_tests")
        store.save(PuzzleSession(p))
        compose.setContent { MaterialTheme { InfinitePuzzleScreen(PuzzleKind.Arrows,false,false,true,{}, {},storage=store) } }
        compose.waitUntil(10000) { runCatching { compose.onAllNodesWithText("Continue · Level 1").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        compose.onNodeWithText("Continue · Level 1").performClick()
        p.solution.forEachIndexed { index, id ->
            val cell = p.paths.first { it.id == id }.cells.first()
            compose.onNodeWithContentDescription("Puzzle board").performTouchInput {
                val camera=ArrowCamera.initial(p.size,p.level)
                val point=camera.worldToScreen(cell.x+1f,cell.y+1f,width.toFloat(),height.toFloat(),p.size)
                click(androidx.compose.ui.geometry.Offset(point.x,point.y))
            }
            compose.waitUntil(10000) { store.active(PuzzleKind.Arrows)!!.removed.size == index+1 }
        }
        assertEquals(0,store.active(PuzzleKind.Arrows)!!.mistakes)
        compose.onNodeWithText("Beautifully solved!").assertIsDisplayed()
    }

    @Test fun invalidColourGestureCostsOneLifeAndPreservesCorrectRoute() {
        val paths=listOf(
            PuzzlePath(0,listOf(Cell(0,1),Cell(1,1),Cell(2,1))),
            PuzzlePath(1,listOf(Cell(1,0),Cell(2,0),Cell(3,0),Cell(3,1),Cell(3,2),Cell(2,2),Cell(1,2)))
        )
        val p=InfinitePuzzle(PuzzleKind.Colours,1,812,1,812,6,paths,listOf(0,1))
        val store=PuzzleStore(context,"iq_puzzle_device_tests")
        store.save(PuzzleSession(p,routes=mapOf(0 to paths[0].cells)))
        compose.setContent { MaterialTheme { InfinitePuzzleScreen(PuzzleKind.Colours,false,false,true,{}, {},storage=store) } }
        compose.waitUntil(10000) { runCatching { compose.onAllNodesWithText("Continue \u00b7 Level 1").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        compose.onNodeWithText("Continue \u00b7 Level 1").performClick()
        compose.onNodeWithContentDescription("Puzzle board").performTouchInput {
            fun pos(x:Int,y:Int)=androidx.compose.ui.geometry.Offset((x+1)*width.toFloat()/7,(y+1)*height.toFloat()/7)
            down(pos(1,0)); moveTo(pos(1,1),100); moveTo(pos(1,2),100); moveTo(pos(1,1),100); up()
        }
        compose.waitUntil(10000) { store.active(PuzzleKind.Colours)!!.mistakes==1 }
        val saved=store.active(PuzzleKind.Colours)!!
        assertEquals(4,saved.lives)
        assertEquals(mapOf(0 to paths[0].cells),saved.routes)
        compose.onNodeWithText("\u21bb Retry").performClick()
        compose.waitUntil(10000) { store.active(PuzzleKind.Colours)!!.lives==5 }
        assertTrue(store.active(PuzzleKind.Colours)!!.routes.isEmpty())
    }
    @Test fun completionRewardsCannotBeFarmedAndVariantsPreserveCanonicalLevel() {
        val store=PuzzleStore(context,"iq_puzzle_reward_test")
        val p=InfiniteEngine.generate(PuzzleKind.Arrows,1,987)
        val completed=PuzzleSession(p,removed=p.paths.map { it.id }.toSet())
        store.save(completed)
        val canonical=store.saved(PuzzleKind.Arrows,1)!!.puzzle
        val different=InfiniteEngine.generate(PuzzleKind.Arrows,1,988)
        store.save(PuzzleSession(different))
        assertEquals(canonical,store.saved(PuzzleKind.Arrows,1)!!.puzzle)
        val claimed=store.claimReward(completed)
        assertFalse(store.claimReward(completed))
        if(claimed) assertFalse(PuzzleStore(context,"iq_puzzle_reward_test").claimReward(completed))
    }

    @Test fun arrowCameraControlsPanAndRestoreWithoutRemovingArrows() {
        val p=ArrowLevelGenerator.generate(1,846)
        val store=PuzzleStore(context,"iq_puzzle_device_tests")
        store.save(PuzzleSession(p))
        compose.setContent { MaterialTheme { InfinitePuzzleScreen(PuzzleKind.Arrows,false,false,true,{}, {},storage=store) } }
        compose.waitUntil(10000) { runCatching { compose.onAllNodesWithText("Continue \u00b7 Level 1").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        compose.onNodeWithText("Continue \u00b7 Level 1").performClick()
        compose.onNodeWithContentDescription("Zoom in").performClick()
        compose.waitUntil(10000) { store.active(PuzzleKind.Arrows)!!.cameraZoom>.25f }
        val beforePinch=store.active(PuzzleKind.Arrows)!!.cameraZoom
        compose.onNodeWithContentDescription("Puzzle board").performTouchInput {
            down(0,center+androidx.compose.ui.geometry.Offset(-40f,0f))
            down(1,center+androidx.compose.ui.geometry.Offset(40f,0f))
            moveTo(0,center+androidx.compose.ui.geometry.Offset(-80f,0f),100)
            moveTo(1,center+androidx.compose.ui.geometry.Offset(80f,0f),100)
            up(0);up(1)
        }
        compose.waitUntil(10000) { store.active(PuzzleKind.Arrows)!!.cameraZoom>beforePinch }
        val beforePan=store.active(PuzzleKind.Arrows)!!.cameraX
        compose.onNodeWithContentDescription("Puzzle board").performTouchInput {
            down(center);moveTo(center+androidx.compose.ui.geometry.Offset(70f,0f),200);up()
        }
        compose.waitUntil(10000) { store.active(PuzzleKind.Arrows)!!.cameraX!=beforePan }
        assertTrue(store.active(PuzzleKind.Arrows)!!.removed.isEmpty())
        val viewBeforeRetry=store.active(PuzzleKind.Arrows)!!
        compose.onNodeWithText("\u21bb Retry").performClick()
        assertEquals(viewBeforeRetry.cameraZoom,store.active(PuzzleKind.Arrows)!!.cameraZoom,0f)
        compose.onNodeWithText("Map",useUnmergedTree=true).performClick()
        compose.onNodeWithContentDescription("Arrow mini-map").performTouchInput { click(center) }
        compose.onNodeWithText("Fit").performClick()
        compose.waitUntil(10000) { store.active(PuzzleKind.Arrows)!!.cameraZoom==.25f }
        val restored=PuzzleStore(context,"iq_puzzle_device_tests").active(PuzzleKind.Arrows)!!
        assertEquals(ArrowCamera.fit(p.size).x,restored.cameraX,0f)
        assertEquals(ArrowCamera.fit(p.size).y,restored.cameraY,0f)
    }

    @Test fun largeFoldedBoardRendersAndMakesASolverApprovedMove() {
        val p=ArrowLevelGenerator.generate(1000,927)
        val store=PuzzleStore(context,"iq_puzzle_device_tests")
        store.save(PuzzleSession(p))
        compose.setContent { MaterialTheme { InfinitePuzzleScreen(PuzzleKind.Arrows,false,false,true,{}, {},storage=store) } }
        compose.waitUntil(10000) { runCatching { compose.onAllNodesWithText("Continue \u00b7 Level 1000").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
        compose.onNodeWithText("Continue \u00b7 Level 1000").performClick()
        compose.onNodeWithContentDescription("Arrow mini-map").assertExists()
        screenshot("Arrows650")
        compose.onNodeWithText("Show one move").performClick()
        compose.waitUntil(10000) { store.active(PuzzleKind.Arrows)!!.removed.size==1 }
        assertEquals(5,store.active(PuzzleKind.Arrows)!!.lives)
        assertEquals(650,p.paths.size)
    }

}
