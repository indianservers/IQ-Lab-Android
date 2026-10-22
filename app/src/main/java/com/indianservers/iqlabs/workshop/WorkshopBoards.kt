package com.indianservers.iqlabs.workshop

import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import kotlin.math.*

internal val jewelColours=listOf(Color(0xFF00B9D8),Color(0xFFFF8C17),Color(0xFFEE3D42),Color(0xFF26C535),Color(0xFFFFD329),Color(0xFF963BEA),Color(0xFF394354))
internal val warmInk=Color(0xFF744422)
internal val gold=Color(0xFFCF8C36)
internal fun light(c:Color,amount:Float)=lerp(c,Color.White,amount)
internal fun dark(c:Color,amount:Float)=lerp(c,Color.Black,amount)
internal fun beamColour(mask:Int)=Color(if(mask and 1!=0)1f else 0f,if(mask and 2!=0)1f else 0f,if(mask and 4!=0)1f else 0f)
private fun Vec.offset()=Offset(x.toFloat(),y.toFloat())
private fun DrawScope.label(text:String,p:Offset,size:Float,colour:Color,paint:android.graphics.Paint) {
    paint.color=colour.toArgb();paint.textSize=size
    drawContext.canvas.nativeCanvas.drawText(text,p.x,p.y,paint)
}

@Composable
fun LaserBoard(s:WorkshopSession,modifier:Modifier,highlight:Int?,preview:Pair<Int,Int>?,animating:Int?,goal:Int?,progress:Float,enabled:Boolean,onTap:(Int)->Unit) {
    val p=s.puzzle as LaserLevel
    val textPaint=remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textAlign=android.graphics.Paint.Align.CENTER;isFakeBoldText=true } }
    val glow=remember { Animatable(.16f) }
    LaunchedEffect(highlight) { if(highlight!=null)repeat(2) { glow.animateTo(.40f,tween(180));glow.animateTo(.16f,tween(180)) } }
    val config by rememberUpdatedState(s.state.mirrors)
    val allowed by rememberUpdatedState(enabled)
    val action by rememberUpdatedState(onTap)
    val cachedTrace=remember(p,s.state.mirrors) { LaserEngine.trace(p,s.state.mirrors) }
    Canvas(modifier.semantics { contentDescription="Laser puzzle board" }.pointerInput(p) {
        detectTapGestures { point -> if(allowed) {
            val scale=min((size.width-20f)/p.columns,(size.height-20f)/p.rows)
            val origin=Offset((size.width-p.columns*scale)/2,(size.height-p.rows*scale)/2)
            val world=(point-origin)/scale
            p.objects.filter(Optic::movable).minByOrNull { (it.position.offset()-world).getDistance() }?.takeIf { (it.position.offset()-world).getDistance()<.68f }?.let { action(it.id) }
        } }
    }) {
        val unit=min((size.width-20f)/p.columns,(size.height-20f)/p.rows)
        val origin=Offset((size.width-p.columns*unit)/2,(size.height-p.rows*unit)/2)
        val override=if(animating!=null && goal!=null) {
            val old=config[animating] ?: 0
            val type=p.objects.first { it.id==animating }.type
            if(type in listOf(OpticType.Mirror,OpticType.Splitter)) animating to (LaserEngine.surfaceAngle(old)+(LaserEngine.surfaceAngle(goal)-LaserEngine.surfaceAngle(old))*progress) else null
        } else null
        val trace=if(override==null)cachedTrace else LaserEngine.trace(p,config,override)
        withTransform({ translate(origin.x,origin.y);scale(unit,unit,Offset.Zero) }) {
            drawRoundRect(Brush.linearGradient(listOf(Color(0xFFE5CBA7),Color(0xFFFFF7DF))),Offset(-.08f,-.08f),Size(p.columns+.16f,p.rows+.16f),CornerRadius(.4f))
            drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFF6E8D1),Color(0xFFEEE2C9))),size=Size(p.columns.toFloat(),p.rows.toFloat()),cornerRadius=CornerRadius(.3f))
            for(x in 1 until p.columns)drawLine(gold.copy(alpha=.12f),Offset(x.toFloat(),0f),Offset(x.toFloat(),p.rows.toFloat()),.012f)
            for(y in 1 until p.rows)drawLine(gold.copy(alpha=.12f),Offset(0f,y.toFloat()),Offset(p.columns.toFloat(),y.toFloat()),.012f)
            p.objects.forEach { o->drawOval(Color(0xFF6E4B2A).copy(alpha=.12f),o.position.offset()+Offset(-.38f,.20f),Size(.80f,.18f)) }
            trace.beams.forEach { b ->
                val c=beamColour(b.colour);val a=(.35+.65*b.intensity).toFloat()
                drawLine(if(b.colour==7)Color(0xFFD2B97F).copy(alpha=.20f) else c.copy(alpha=.12f*a),b.from.offset(),b.to.offset(),.24f,StrokeCap.Round)
                drawLine(c.copy(alpha=.28f*a),b.from.offset(),b.to.offset(),.12f,StrokeCap.Round)
                drawLine(c.copy(alpha=a),b.from.offset(),b.to.offset(),.055f,StrokeCap.Round)
                drawLine(Color.White.copy(alpha=.9f*a),b.from.offset(),b.to.offset(),.018f,StrokeCap.Round)
            }
            p.objects.forEach { o ->
                val c=o.position.offset();val rotation=config[o.id] ?: o.rotation
                if(highlight==o.id || animating==o.id)drawCircle(gold.copy(alpha=if(highlight==o.id)glow.value else .24f),.63f,c)
                when(o.type) {
                    OpticType.Emitter->{
                        drawCircle(Brush.radialGradient(listOf(beamColour(o.colour).copy(alpha=.45f),Color.Transparent),c,.58f),.58f,c)
                        drawCircle(dark(gold,.4f),.34f,c);drawCircle(Brush.linearGradient(listOf(Color.White,gold,Color(0xFF9A4F21))),.29f,c)
                        drawCircle(beamColour(o.colour),.21f,c);drawCircle(Color.White,.10f,c)
                        val d=listOf(Offset(1f,0f),Offset(0f,1f),Offset(-1f,0f),Offset(0f,-1f))[o.rotation%4]
                        drawLine(Color.White,c,c+d*.31f,.07f,StrokeCap.Round)
                    }
                    OpticType.Mirror,OpticType.FixedMirror,OpticType.Reflector,OpticType.Splitter->{
                        val angle=if(override?.first==o.id)override.second else LaserEngine.surfaceAngle(rotation)
                        val tangent=Offset(cos(angle).toFloat(),sin(angle).toFloat());val normal=Offset(-tangent.y,tangent.x)
                        val a=c-tangent*.47f;val b=c+tangent*.47f
                        drawLine(Color(0xFF553D34).copy(alpha=.25f),a+Offset(.055f,.07f),b+Offset(.055f,.07f),.22f,StrokeCap.Round)
                        drawLine(if(o.type==OpticType.Splitter)Color(0xFF1A99B3) else Color(0xFF7B716A),a,b,.18f,StrokeCap.Round)
                        drawLine(Brush.linearGradient(listOf(Color(0xFFB4BDCC),Color.White,Color(0xFFB8C7D5)),c-normal*.08f,c+normal*.08f),a,b,.13f,StrokeCap.Round)
                        drawLine(Color.White.copy(alpha=.9f),a-normal*.035f,b-normal*.035f,.035f,StrokeCap.Round)
                        if(!o.movable)drawCircle(gold,.075f,c)
                        if(o.type==OpticType.Splitter)label("50/50",c+Offset(0f,.40f),.14f,warmInk,textPaint)
                        if(preview?.first==o.id) {
                            val theta=LaserEngine.surfaceAngle(preview.second);val v=Offset(cos(theta).toFloat(),sin(theta).toFloat())
                            drawLine(gold,c-v*.58f,c+v*.58f,.055f,pathEffect=PathEffect.dashPathEffect(floatArrayOf(.10f,.08f)))
                        }
                    }
                    OpticType.Receiver->{
                        val colour=beamColour(o.colour);val lit=o.id in trace.lit
                        drawOval(dark(gold,.3f),c+Offset(-.36f,.19f),Size(.72f,.20f));drawOval(Brush.verticalGradient(listOf(Color.White,gold)),c+Offset(-.33f,.15f),Size(.66f,.15f))
                        if(lit)drawCircle(Brush.radialGradient(listOf(colour.copy(alpha=.45f),Color.Transparent),c,.63f),.63f,c)
                        val top=c+Offset(0f,-.37f);val left=c+Offset(-.29f,-.02f);val right=c+Offset(.29f,-.02f);val bottom=c+Offset(0f,.30f)
                        val gem=Path().apply { moveTo(top.x,top.y);lineTo(right.x,right.y);lineTo(bottom.x,bottom.y);lineTo(left.x,left.y);close() }
                        drawPath(gem,Brush.linearGradient(listOf(light(colour,.7f),colour,dark(colour,.4f)),top,bottom))
                        drawPath(Path().apply { moveTo(top.x,top.y);lineTo(c.x,c.y);lineTo(left.x,left.y);close() },light(colour,.55f))
                        drawPath(Path().apply { moveTo(c.x,c.y);lineTo(right.x,right.y);lineTo(bottom.x,bottom.y);close() },dark(colour,.20f))
                        drawPath(gem,Color.White.copy(alpha=.8f),style=Stroke(.025f));drawLine(Color.White.copy(alpha=.7f),top,c,.022f)
                        if(lit)drawCircle(Color.White,.04f,c+Offset(-.06f,-.12f))
                    }
                    OpticType.Prism->{
                        drawCircle(Brush.linearGradient(listOf(Color.White.copy(alpha=.8f),Color.Cyan.copy(alpha=.4f),Color.Magenta.copy(alpha=.3f))),.30f,c)
                        drawCircle(Color.White,.30f,c,style=Stroke(.025f))
                        val triangle=Path().apply { moveTo(c.x,c.y-.25f);lineTo(c.x+.23f,c.y+.17f);lineTo(c.x-.23f,c.y+.17f);close() }
                        drawPath(triangle,Brush.linearGradient(listOf(Color(0xFFFFB2D9),Color(0xFFFFF2A7),Color(0xFF71F2EA))))
                        drawPath(triangle,Color.White,style=Stroke(.025f))
                        drawCircle(Color.Red,.035f,c+Offset(if(rotation==0)-.16f else .16f,.13f));drawCircle(Color.Blue,.035f,c+Offset(if(rotation==0).16f else -.16f,.13f))
                        drawLine(Color.White.copy(alpha=.8f),c+Offset(0f,-.25f),c+Offset(.07f,.17f),.018f)
                    }
                    OpticType.Filter->{
                        drawRoundRect(dark(beamColour(o.colour),.25f),c-Offset(.28f,.28f),Size(.56f,.56f),CornerRadius(.08f))
                        drawRoundRect(Brush.linearGradient(listOf(light(beamColour(o.colour),.7f).copy(alpha=.8f),beamColour(o.colour).copy(alpha=.55f))),c-Offset(.24f,.24f),Size(.48f,.48f),CornerRadius(.06f))
                        drawLine(Color.White.copy(alpha=.8f),c+Offset(-.18f,-.18f),c+Offset(.18f,.18f),.022f)
                    }
                    OpticType.Absorber,OpticType.Shutter->{
                        val open=o.type==OpticType.Shutter && rotation==1
                        if(!open) {
                            drawRoundRect(Color(0xFF9A9690),c-Offset(.37f,.37f),Size(.74f,.74f),CornerRadius(.10f))
                            drawRoundRect(Brush.linearGradient(listOf(Color(0xFFE2DED6),Color(0xFFA8A59F))),c-Offset(.33f,.33f),Size(.66f,.66f),CornerRadius(.08f))
                            drawLine(Color(0xFF92908A),c-Offset(.23f,.23f),c+Offset(.23f,.23f),.035f)
                            drawLine(Color(0xFF92908A),c+Offset(-.23f,.23f),c+Offset(.23f,-.23f),.035f)
                        } else drawRoundRect(gold.copy(alpha=.65f),c-Offset(.36f,.36f),Size(.72f,.72f),CornerRadius(.08f),style=Stroke(.05f))
                        if(o.type==OpticType.Shutter)label(if(open)"OPEN" else "GATE",c+Offset(0f,.48f),.13f,warmInk,textPaint)
                    }
                }
            }
        }
    }
}

data class RingProjection(val worldWidth:Double,val worldHeight:Double,val width:Float,val height:Float,val zoom:Float=1f,val pan:Offset=Offset.Zero) {
    val scale get()=min(width/worldWidth.toFloat(),height/worldHeight.toFloat())*.91f*zoom
    fun screen(point:Vec)=Offset(width/2+(point.x-worldWidth/2).toFloat()*scale+pan.x,height/2+(point.y-worldHeight/2).toFloat()*scale+pan.y)
    fun world(point:Offset)=Vec(worldWidth/2+(point.x-width/2-pan.x)/scale,worldHeight/2+(point.y-height/2-pan.y)/scale)
    companion object { fun of(board:RingLevel,width:Float,height:Float,zoom:Float=1f,pan:Offset=Offset.Zero)=RingProjection(board.rings.maxOf { it.centre.x }+1.4,board.rings.maxOf { it.centre.y }+1.4,width,height,zoom,pan) }
}

@Composable
fun RingsBoard(s:WorkshopSession,modifier:Modifier,selected:Int?,highlight:Int?,preview:RingMove?,animating:Int?,progress:Float,turning:Int?,turnGoal:Double?,turnProgress:Float,enabled:Boolean,onSelect:(Int)->Unit,onRotate:(Int,Double)->Unit) {
    val p=s.puzzle as RingLevel
    val textPaint=remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textAlign=android.graphics.Paint.Align.CENTER;isFakeBoldText=true } }
    val glow=remember { Animatable(.12f) }
    LaunchedEffect(highlight) { if(highlight!=null)repeat(2) { glow.animateTo(.30f,tween(180));glow.animateTo(.12f,tween(180)) } }
    var zoom by remember(p) { mutableFloatStateOf(1f) };var pan by remember(p) { mutableStateOf(Offset.Zero) }
    var dragged by remember(p) { mutableStateOf<Pair<Int,Double>?>(null) }
    val current by rememberUpdatedState(s)
    val allowed by rememberUpdatedState(enabled);val select by rememberUpdatedState(onSelect);val rotateAction by rememberUpdatedState(onRotate)
    Box(modifier.clipToBounds()) {
        Canvas(Modifier.fillMaxSize().padding(bottom=44.dp).semantics { contentDescription="Ring puzzle board" }.pointerInput(p) {
            fun projection()=RingProjection.of(p,size.width.toFloat(),size.height.toFloat(),zoom,pan)
            awaitEachGesture {
                val down=awaitFirstDown(requireUnconsumed=false);val start=projection().world(down.position)
                val ring=if(allowed)p.rings.filter { it.id !in current.state.released }.minByOrNull { (it.centre-start).length() }?.takeIf { (it.centre-start).length()<it.radius+.5 } else null
                if(ring!=null)select(ring.id)
                var previous=down.position;var touched=false;var multi=false
                var angle=if(ring!=null)current.state.angles[ring.id] ?: 0.0 else 0.0
                var lastPointerAngle=if(ring!=null)RingEngine.angle(ring.centre,start) else 0.0
                do {
                    val event=awaitPointerEvent()
                    if(event.changes.count { it.pressed }>=2) {
                        multi=true;dragged=null
                        val focal=event.calculateCentroid(useCurrent=false);val world=projection().world(focal)
                        val newZoom=(zoom*event.calculateZoom()).coerceIn(.8f,2.8f);zoom=newZoom
                        val projected=projection().screen(world)
                        pan+=focal-projected+event.calculatePan()
                        val limit=max(size.width,size.height)*zoom
                        pan=Offset(pan.x.coerceIn(-limit,limit),pan.y.coerceIn(-limit,limit))
                        event.changes.forEach { it.consume() }
                    } else {
                        val change=event.changes.firstOrNull { it.id==down.id }
                        if(change!=null && change.pressed && !multi) {
                            if((change.position-down.position).getDistance()>viewConfiguration.touchSlop)touched=true
                            if(touched) {
                                if(ring!=null && allowed) {
                                    val point=projection().world(change.position)
                                    if((point-ring.centre).length()>.25) {
                                        val now=RingEngine.angle(ring.centre,point)
                                        val delta=((now-lastPointerAngle+540)%360)-180
                                        angle=RingEngine.normal(angle+delta);lastPointerAngle=now;dragged=ring.id to angle
                                    }
                                } else pan+=change.position-previous
                                change.consume()
                            }
                            previous=change.position
                        }
                    }
                } while(event.changes.any { it.pressed })
                if(ring!=null && touched && !multi && allowed)rotateAction(ring.id,RingEngine.snap(angle))
                dragged=null
            }
        }) {
            val projection=RingProjection.of(p,size.width,size.height,zoom,pan);val unit=projection.scale
            val origin=projection.screen(Vec(0.0,0.0))
            withTransform({ translate(origin.x,origin.y);scale(unit,unit,Offset.Zero) }) {
                p.links.forEach { e -> if(e.a !in s.state.released && e.b !in s.state.released) {
                    val a=p.rings.first { it.id==e.a };val b=p.rings.first { it.id==e.b };val d=(b.centre-a.centre).unit()
                    val alpha=if(animating==e.a || animating==e.b)1-progress else 1f
                    var start=a.centre+d*a.radius;var end=b.centre-d*b.radius
                    if(animating==e.a)start+=(end-start)*progress.toDouble()
                    else if(animating==e.b)end+=(start-end)*progress.toDouble()
                    drawLine(Color(0xFF8D7656).copy(alpha=alpha),start.offset(),end.offset(),.14f,StrokeCap.Round)
                    drawLine(Color(0xFFFFDC77).copy(alpha=alpha),start.offset()+Offset(-.025f,-.025f),end.offset()+Offset(-.025f,-.025f),.055f,StrokeCap.Round)
                } }
                p.rings.forEach { r -> if(r.id !in s.state.released) {
                    val colour=jewelColours[r.material%jewelColours.size]
                    val original=s.state.angles[r.id] ?: 0.0
                    val orientation=if(dragged?.first==r.id)dragged!!.second else if(turning==r.id && turnGoal!=null)RingEngine.normal(original+(((turnGoal-original+540)%360)-180)*turnProgress) else original
                    val angle=orientation+r.gap/2;val sweep=360-r.gap
                    val lift=if(animating==r.id)progress else 0f
                    val c=r.centre.offset()+Offset(0f,-lift*.35f)
                    val radius=(r.radius*(1+lift*.08)).toFloat();val alpha=1-lift
                    val top=c-Offset(radius,radius);val extent=Size(radius*2,radius*2)
                    drawOval(Color(0xFF806B43).copy(alpha=.13f*alpha),c+Offset(-1.1f,.76f),Size(2.2f,.35f))
                    if(selected==r.id || highlight==r.id)drawCircle(gold.copy(alpha=if(highlight==r.id)glow.value else .10f),radius+.32f,c)
                    drawArc(Color(0xFF856B42).copy(alpha=.16f*alpha),angle.toFloat(),sweep.toFloat(),false,top+Offset(.045f,.07f),extent,style=Stroke((r.tube*2+.08).toFloat(),cap=StrokeCap.Round))
                    drawArc(dark(colour,.35f).copy(alpha=alpha),angle.toFloat(),sweep.toFloat(),false,top,extent,style=Stroke((r.tube*2+.045).toFloat(),cap=StrokeCap.Round))
                    drawArc(Brush.sweepGradient(listOf(dark(colour,.30f),colour,light(colour,.40f),colour,dark(colour,.30f)),c),angle.toFloat(),sweep.toFloat(),false,top,extent,alpha=alpha,style=Stroke((r.tube*2).toFloat(),cap=StrokeCap.Round))
                    drawArc(light(colour,.75f).copy(alpha=.65f*alpha),angle.toFloat(),sweep.toFloat(),false,top+Offset(-.035f,-.04f),extent,style=Stroke(.055f,cap=StrokeCap.Round))
                    listOf(angle,angle+sweep).forEach { a ->
                        val radians=Math.toRadians(a);val tip=c+Offset(cos(radians).toFloat(),sin(radians).toFloat())*radius
                        drawCircle(Brush.radialGradient(listOf(light(colour,.60f).copy(alpha=alpha),colour.copy(alpha=alpha),dark(colour,.18f).copy(alpha=alpha)),tip-Offset(.04f,.04f),.22f),r.tube.toFloat(),tip)
                    }
                    if(preview?.id==r.id) {
                        val a=Math.toRadians(preview.angle)
                        drawLine(gold,c+Offset(cos(a).toFloat(),sin(a).toFloat())*.7f,c+Offset(cos(a).toFloat(),sin(a).toFloat())*1.3f,.045f,StrokeCap.Round)
                        drawArc(gold.copy(alpha=.7f),(preview.angle-r.gap/2).toFloat(),r.gap.toFloat(),false,c-Offset(1.23f,1.23f),Size(2.46f,2.46f),style=Stroke(.035f,pathEffect=PathEffect.dashPathEffect(floatArrayOf(.1f,.08f))))
                    }
                    // A gold base guide remains even after its spring links retract.
                    val collars=mutableListOf(r.guide to true)
                    RingEngine.neighbours(p,r.id,s.state.released).forEach { id -> val a=RingEngine.angle(r.centre,p.rings.first { it.id==id }.centre);if(collars.none { RingEngine.distance(it.first,a)<1 })collars+=a to false }
                    collars.forEach { (a,base) ->
                        val radians=Math.toRadians(a);val at=r.centre.offset()+Offset(cos(radians).toFloat(),sin(radians).toFloat())*r.radius.toFloat()
                        rotate(a.toFloat()+90,at) {
                            drawRoundRect(if(base)Color(0xFFAA6A17) else Color(0xFF5A7081),at-Offset(.19f,.21f),Size(.38f,.42f),CornerRadius(.07f))
                            drawRoundRect(Brush.horizontalGradient(if(base)listOf(Color(0xFFFFD955),Color(0xFFFFF2AE),Color(0xFFE99D17)) else listOf(Color(0xFF4FADB5),Color(0xFFBBF6F6),Color(0xFF328E9D))),at-Offset(.16f,.18f),Size(.32f,.36f),CornerRadius(.055f))
                            drawLine(Color.White.copy(alpha=.7f),at+Offset(-.12f,-.12f),at+Offset(.12f,-.12f),.025f,StrokeCap.Round)
                        }
                    }
                    label("${r.id+1}",c+Offset(0f,.09f),.27f,colour.copy(alpha=.65f*alpha),textPaint)
                } }
            }
        }
        Row(Modifier.align(Alignment.BottomEnd),verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick={ zoom=(zoom/1.3f).coerceAtLeast(.8f) }) { Text("−",color=warmInk) }
            TextButton(onClick={ zoom=(zoom*1.3f).coerceAtMost(2.8f) }) { Text("+",color=warmInk) }
            TextButton(onClick={ zoom=1f;pan=Offset.Zero }) { Text("Fit",color=warmInk) }
        }
    }
}

@Composable
fun WorkshopHero(kind:WorkshopKind,modifier:Modifier) {
    Canvas(modifier) {
        if(kind==WorkshopKind.Laser) {
            val baseline=size.height*.84f
            listOf(Color(0xFFCABEDB),Color(0xFFADC6CF),Color(0xFFB0B9BA)).forEachIndexed { layer,colour ->
                val path=Path().apply { moveTo(0f,baseline);for(i in 0..8) { val x=size.width*i/8;lineTo(x,baseline-size.height*(.12f+((i*7+layer*3)%5)*.055f)) };lineTo(size.width,size.height);lineTo(0f,size.height);close() }
                drawPath(path,colour.copy(alpha=.18f+layer*.05f))
            }
            val a=Offset(size.width*.1f,size.height*.6f);val b=Offset(size.width*.52f,a.y);val c=Offset(b.x,size.height*.18f)
            drawLine(Color.Red.copy(alpha=.15f),a,b,15f,StrokeCap.Round);drawLine(Color.Red,a,b,4f);drawLine(Color.Red.copy(alpha=.15f),b,c,15f,StrokeCap.Round);drawLine(Color.Red,b,c,4f)
            drawLine(Color(0xFF7E7E89),b+Offset(-14f,14f),b+Offset(14f,-14f),12f,StrokeCap.Round)
            drawLine(Color.White,b+Offset(-14f,14f),b+Offset(14f,-14f),5f,StrokeCap.Round)
            drawCircle(Color(0xFFFF6D8F),13f,c);drawCircle(Color.White.copy(alpha=.8f),4f,c+Offset(-3f,-3f))
        } else {
            repeat(5) { i ->
                val radius=min(size.width/7,size.height/3.5f);val c=Offset(size.width*(.19f+i*.15f),size.height*(if(i%2==0).45f else .65f))
                val colour=jewelColours[i];val top=c-Offset(radius,radius);val extent=Size(radius*2,radius*2)
                drawArc(dark(colour,.35f),-65f+i*30,285f,false,top+Offset(1f,3f),extent,style=Stroke(radius*.30f,cap=StrokeCap.Round))
                drawArc(Brush.sweepGradient(listOf(colour,light(colour,.55f),colour),c),-65f+i*30,285f,false,top,extent,style=Stroke(radius*.25f,cap=StrokeCap.Round))
                drawArc(Color.White.copy(alpha=.65f),-65f+i*30,285f,false,top+Offset(-1f,-2f),extent,style=Stroke(radius*.055f,cap=StrokeCap.Round))
            }
        }
    }
}
