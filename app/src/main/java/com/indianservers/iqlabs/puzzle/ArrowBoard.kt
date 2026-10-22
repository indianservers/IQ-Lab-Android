package com.indianservers.iqlabs.puzzle

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.*
import kotlin.math.*

private val arrowBrown=Color(0xFF755237)
private val arrowGold=Color(0xFFB67C45)
private data class ArrowDrawing(val model:PuzzlePath,val path:Path,val minX:Float,val minY:Float,val maxX:Float,val maxY:Float)

@Composable
fun ArrowBoard(s:PuzzleSession,modifier:Modifier,highlight:Int?,animating:Int?,travel:Float,enabled:Boolean,preview:Boolean=false,onArrow:(Int)->Unit,onCamera:(ArrowCamera)->Unit) {
    val puzzle=s.puzzle
    var camera by remember(puzzle) { mutableStateOf(if(s.cameraZoom>0) ArrowCamera(s.cameraX,s.cameraY,s.cameraZoom) else ArrowCamera.initial(puzzle.size,puzzle.level)) }
    var viewport by remember { mutableStateOf(IntSize.Zero) }
    var minimap by remember { mutableStateOf(puzzle.size>80) }
    val scope=rememberCoroutineScope()
    val current by rememberUpdatedState(s)
    val canTap by rememberUpdatedState(enabled)
    val tap by rememberUpdatedState(onArrow)
    val saveCamera by rememberUpdatedState(onCamera)
    val pulse=remember { Animatable(.18f) }
    LaunchedEffect(highlight) {
        if(highlight!=null) {
            if(viewport.width>0) puzzle.paths.firstOrNull { it.id==highlight }?.let { path ->
                val middle=path.cells[path.cells.size/2]
                val projected=camera.worldToScreen(middle.x+1f,middle.y+1f,viewport.width.toFloat(),viewport.height.toFloat(),puzzle.size)
                if(projected.x<24 || projected.y<24 || projected.x>viewport.width-24 || projected.y>viewport.height-70) {
                    camera=camera.copy(x=middle.x+1f,y=middle.y+1f).constrained(viewport.width.toFloat(),viewport.height.toFloat(),puzzle.size)
                    saveCamera(camera)
                }
            }
            repeat(2) { pulse.animateTo(.48f,tween(230));pulse.animateTo(.18f,tween(230)) }
        }
    }
    val drawings=remember(puzzle) { puzzle.paths.map { a ->
        val path=Path().apply {
            val first=a.cells.first();moveTo(first.x+1f,first.y+1f)
            for(i in 1 until a.cells.lastIndex) if(a.cells[i]-a.cells[i-1]!=a.cells[i+1]-a.cells[i]) lineTo(a.cells[i].x+1f,a.cells[i].y+1f)
            lineTo(a.head.x+1f,a.head.y+1f)
        }
        ArrowDrawing(a,path,a.cells.minOf{it.x}+1f,a.cells.minOf{it.y}+1f,a.cells.maxOf{it.x}+1f,a.cells.maxOf{it.y}+1f)
    } }
    val body=remember { Stroke(.23f,cap=StrokeCap.Round,join=StrokeJoin.Round) }
    val halo=remember { Stroke(.58f,cap=StrokeCap.Round,join=StrokeJoin.Round) }
    fun persist() { saveCamera(camera) }
    fun changeZoom(factor:Float) {
        if(viewport.width>0) { camera=camera.zoomAt(factor,viewport.width/2f,viewport.height/2f,viewport.width.toFloat(),viewport.height.toFloat(),puzzle.size);persist() }
    }
    Box(modifier.clipToBounds().background(Color(0xFFF8F1E3))) {
        Canvas(Modifier.fillMaxSize().onSizeChanged { viewport=it }.semantics { contentDescription="Puzzle board" }.pointerInput(puzzle) {
            var pending:Job?=null
            var lastTap=0L
            var lastTapPoint=Offset.Zero
            coroutineScope {
                val gestureScope=this
                awaitEachGesture {
                    val down=awaitFirstDown(requireUnconsumed=false)
                    val start=down.position
                    var dragged=false;var pinch=false
                    var previous=start
                    var released=start
                    do {
                        val event=awaitPointerEvent()
                        val pressed=event.changes.count { it.pressed }
                        if(pressed>=2) {
                            pending?.cancel();pinch=true;dragged=true
                            val focal=event.calculateCentroid(useCurrent=false)
                            val delta=event.calculatePan()
                            camera=camera.zoomAt(event.calculateZoom(),focal.x,focal.y,size.width.toFloat(),size.height.toFloat(),puzzle.size)
                                .pan(delta.x,delta.y,size.width.toFloat(),size.height.toFloat(),puzzle.size)
                            event.changes.forEach { it.consume() }
                        } else {
                            val change=event.changes.firstOrNull { it.id==down.id }
                            if(change!=null) {
                                released=change.position
                                if(change.pressed) {
                                    if((change.position-start).getDistance()>viewConfiguration.touchSlop) dragged=true
                                    if(dragged && !pinch) {
                                        pending?.cancel()
                                        val delta=change.position-previous
                                        camera=camera.pan(delta.x,delta.y,size.width.toFloat(),size.height.toFloat(),puzzle.size)
                                        change.consume()
                                    }
                                    previous=change.position
                                }
                            }
                        }
                    } while(event.changes.any { it.pressed })
                    if(dragged) { saveCamera(camera);lastTap=0L }
                    else {
                        val now=SystemClock.uptimeMillis()
                        if(now-lastTap<300 && (released-lastTapPoint).getDistance()<viewConfiguration.touchSlop*2) {
                            pending?.cancel()
                            camera=camera.zoomAt(2f,released.x,released.y,size.width.toFloat(),size.height.toFloat(),puzzle.size)
                            saveCamera(camera);lastTap=0L
                        } else {
                            pending?.cancel();lastTap=now;lastTapPoint=released
                            val point=released
                            pending=gestureScope.launch {
                                delay(300)
                                if(canTap) {
                                    val world=camera.screenToWorld(point.x,point.y,size.width.toFloat(),size.height.toFloat(),puzzle.size)
                                    val q=Offset(world.x-1,world.y-1)
                                    var best:Int?=null;var distance=Float.MAX_VALUE
                                    drawings.forEach { drawing -> if(drawing.model.id !in current.removed) {
                                        drawing.model.cells.zipWithNext().forEach { (a,b) ->
                                            val v=Offset((b.x-a.x).toFloat(),(b.y-a.y).toFloat())
                                            val w=q-Offset(a.x.toFloat(),a.y.toFloat())
                                            val t=((w.x*v.x+w.y*v.y)/(v.x*v.x+v.y*v.y)).coerceIn(0f,1f)
                                            val d=(w-v*t).getDistance()
                                            if(d<distance) { distance=d;best=drawing.model.id }
                                        }
                                    } }
                                    val scale=camera.scale(size.width.toFloat(),size.height.toFloat(),puzzle.size)
                                    if(best!=null && distance<=max(.45f,16.dp.toPx()/scale)) tap(best!!)
                                }
                            }
                        }
                    }
                }
            }
        }) {
            val scale=camera.scale(size.width,size.height,puzzle.size)
            val origin=Offset(size.width/2-camera.x*scale,size.height/2-camera.y*scale)
            val top=camera.screenToWorld(0f,0f,size.width,size.height,puzzle.size)
            val bottom=camera.screenToWorld(size.width,size.height,size.width,size.height,puzzle.size)
            val stride=max(1,ceil(4f/scale).toInt())
            withTransform({ translate(origin.x,origin.y);scale(scale,scale,Offset.Zero) }) {
                val left=(floor(top.x).toInt()-1).coerceAtLeast(0)
                val right=(ceil(bottom.x).toInt()+1).coerceAtMost(puzzle.size-1)
                val upper=(floor(top.y).toInt()-1).coerceAtLeast(0)
                val lower=(ceil(bottom.y).toInt()+1).coerceAtMost(puzzle.size-1)
                if(left<=right && upper<=lower) for(x in left..right step stride) for(y in upper..lower step stride) drawCircle(Color(0xFFDCCBB2),.045f,Offset(x+1f,y+1f))
                drawings.forEach { a -> if(a.model.id !in s.removed) {
                    val distance=if(a.model.id==animating) ArrowCollisionEngine.exitDistance(a.model,puzzle.size)*travel else 0f
                    val dx=a.model.direction.x*distance;val dy=a.model.direction.y*distance
                    if(a.maxX+dx+.5f>=top.x && a.minX+dx-.5f<=bottom.x && a.maxY+dy+.5f>=top.y && a.minY+dy-.5f<=bottom.y) {
                        translate(dx,dy) {
                            if(preview && highlight==a.model.id) {
                                val dir=Offset(a.model.direction.x.toFloat(),a.model.direction.y.toFloat())
                                translate(dir.x*1.5f,dir.y*1.5f) { drawPath(a.path,arrowGold.copy(alpha=.30f),style=Stroke(.12f,cap=StrokeCap.Round,join=StrokeJoin.Round,pathEffect=PathEffect.dashPathEffect(floatArrayOf(.25f,.2f)))) }
                                val h=Offset(a.model.head.x+1f,a.model.head.y+1f)
                                drawLine(arrowGold.copy(alpha=.4f),h,h+dir*ArrowCollisionEngine.exitDistance(a.model,puzzle.size),.08f,pathEffect=PathEffect.dashPathEffect(floatArrayOf(.3f,.25f)))
                            }
                            if(highlight==a.model.id || animating==a.model.id) drawPath(a.path,arrowGold.copy(alpha=pulse.value),style=halo)
                            val color=arrowBrown.copy(alpha=if(a.model.id==animating)(1f-travel*travel).coerceIn(0f,1f) else 1f)
                            drawPath(a.path,color,style=body)
                            val head=Offset(a.model.head.x+1f,a.model.head.y+1f)
                            val d=Offset(a.model.direction.x.toFloat(),a.model.direction.y.toFloat());val normal=Offset(-d.y,d.x)
                            drawLine(color,head,head-d*.30f+normal*.22f,.21f,StrokeCap.Round)
                            drawLine(color,head,head-d*.30f-normal*.22f,.21f,StrokeCap.Round)
                        }
                    }
                } }
            }
        }
        if(minimap) Canvas(Modifier.align(Alignment.BottomEnd).padding(end=8.dp,bottom=53.dp).size(100.dp).clipToBounds().background(Color(0xFFFCF8F0),RoundedCornerShape(10.dp)).semantics { contentDescription="Arrow mini-map" }.pointerInput(puzzle) {
            fun navigate(o:Offset) { camera=camera.copy(x=o.x/size.width*(puzzle.size+1),y=o.y/size.height*(puzzle.size+1)).constrained(viewport.width.toFloat(),viewport.height.toFloat(),puzzle.size);saveCamera(camera) }
            awaitEachGesture {
                val down=awaitFirstDown();navigate(down.position);down.consume()
                do { val e=awaitPointerEvent();e.changes.firstOrNull()?.let { navigate(it.position);it.consume() } } while(e.changes.any { it.pressed })
            }
        }) {
            val ratio=size.width/(puzzle.size+1)
            drawRect(arrowGold.copy(alpha=.5f),style=Stroke(2f))
            drawings.forEach { a -> if(a.model.id !in s.removed) drawCircle(arrowBrown,1.5f,Offset((a.minX+a.maxX)/2*ratio,(a.minY+a.maxY)/2*ratio)) }
            if(viewport.width>0) {
                val a=camera.screenToWorld(0f,0f,viewport.width.toFloat(),viewport.height.toFloat(),puzzle.size)
                val b=camera.screenToWorld(viewport.width.toFloat(),viewport.height.toFloat(),viewport.width.toFloat(),viewport.height.toFloat(),puzzle.size)
                drawRect(Color(0xFF4EA9EB),Offset(a.x*ratio,a.y*ratio),androidx.compose.ui.geometry.Size((b.x-a.x)*ratio,(b.y-a.y)*ratio),style=Stroke(2f))
            }
        }
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xFFF8F1E3).copy(alpha=.94f)),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceEvenly) {
            TextButton(onClick={ changeZoom(.8f) },modifier=Modifier.semantics { contentDescription="Zoom out" }) { Text("−",color=arrowBrown) }
            TextButton(onClick={ changeZoom(1.25f) },modifier=Modifier.semantics { contentDescription="Zoom in" }) { Text("+",color=arrowBrown) }
            TextButton(onClick={ camera=ArrowCamera.fit(puzzle.size);persist() }) { Text("Fit",color=arrowBrown) }
            TextButton(onClick={ camera=ArrowCamera.initial(puzzle.size,puzzle.level);persist() }) { Text("Reset view",color=arrowBrown,fontSize=11.sp) }
            TextButton(onClick={ minimap=!minimap }) { Text("Map",color=arrowBrown) }
        }
    }
}
