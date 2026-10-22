package com.indianservers.iqlabs.workshop

import com.indianservers.iqlabs.ui.components.rememberQuickPraise
import com.indianservers.iqlabs.ui.components.QuickPraiseBanner
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.indianservers.iqlabs.puzzle.PuzzleMode
import kotlinx.coroutines.*
import kotlin.math.*

@Composable
private fun GlossButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,primary:Boolean=false,enabled:Boolean=true,icon:ImageVector?=null) {
    val colours=if(primary)listOf(Color(0xFFEAB95C),Color(0xFFB46B24)) else listOf(Color(0xFFFFFDF5),Color(0xFFF4E7CF))
    Row(modifier.heightIn(min=50.dp).shadow(if(enabled)5.dp else 0.dp,RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(Brush.verticalGradient(colours)).border(1.5.dp,Color.White.copy(alpha=.85f),RoundedCornerShape(18.dp)).clickable(enabled=enabled,onClick=onClick).padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
        val colour=if(!enabled)warmInk.copy(alpha=.35f) else if(primary)Color.White else warmInk
        if(icon!=null) { Icon(icon,null,tint=colour,modifier=Modifier.size(22.dp));Spacer(Modifier.width(7.dp)) }
        Text(text,color=colour,fontWeight=FontWeight.Bold,fontSize=15.sp)
    }
}
@Composable
private fun ToolButton(label:String,icon:ImageVector,enabled:Boolean=true,onClick:()->Unit) {
    Column(horizontalAlignment=Alignment.CenterHorizontally) {
        GlossButton("",onClick,Modifier.width(66.dp).semantics { contentDescription=label },enabled=enabled,icon=icon)
        Spacer(Modifier.height(5.dp));Text(label,color=warmInk,fontSize=11.sp)
    }
}

@Composable
fun WorkshopScreen(kind:WorkshopKind,sound:Boolean,haptics:Boolean,reducedMotion:Boolean,onSound:(Boolean)->Unit,onHaptics:(Boolean)->Unit,onExit:()->Unit,onSolved:(WorkshopSession)->Unit={},storage:WorkshopStore?=null) {
    val context=LocalContext.current;val view=LocalView.current
    val compact=LocalConfiguration.current.screenHeightDp<740
    val store=remember(storage) { storage ?: WorkshopStore(context) }
    val scope=rememberCoroutineScope()
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    var foreground by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) { val observer=LifecycleEventObserver { _,_->foreground=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) };lifecycle.addObserver(observer);onDispose { lifecycle.removeObserver(observer) } }
    DisposableEffect(view) {
        var owner:android.content.Context=view.context
        while(owner is android.content.ContextWrapper && owner !is android.app.Activity)owner=owner.baseContext
        val controller=(owner as? android.app.Activity)?.let { androidx.core.view.WindowCompat.getInsetsController(it.window,view) }
        val old=controller?.isAppearanceLightStatusBars;controller?.isAppearanceLightStatusBars=true
        onDispose { if(old!=null)controller?.isAppearanceLightStatusBars=old }
    }
    var session by remember { mutableStateOf<WorkshopSession?>(null) }
    var selection by rememberSaveable { mutableStateOf(true) }
    var mode by remember { mutableStateOf(PuzzleMode.Normal) }
    var loading by remember { mutableStateOf(true) }
    var paused by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }
    var rules by remember { mutableStateOf(false) }
    var hintMenu by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf(if(kind==WorkshopKind.Laser)"Tap a mirror to rotate it. Light every crystal." else "Drag around a ring to turn it. Align its opening with the collars.") }
    var selected by remember { mutableStateOf<Int?>(null) }
    var highlight by remember { mutableStateOf<Int?>(null) }
    var laserPreview by remember { mutableStateOf<Pair<Int,Int>?>(null) }
    var ringPreview by remember { mutableStateOf<RingMove?>(null) }
    var motionId by remember { mutableStateOf<Int?>(null) }
    var releasing by remember { mutableStateOf(false) }
    var motionGoal by remember { mutableDoubleStateOf(0.0) }
    var motionJob by remember { mutableStateOf<Job?>(null) }
    val praise=rememberQuickPraise()
    LaunchedEffect(selection,session?.puzzle,paused,settings,foreground) { praise.resetWindow() }
    val progress=remember { Animatable(0f) };val heartScale=remember { Animatable(1f) }
    var page by remember { mutableLongStateOf(1) }
    fun update(s:WorkshopSession) { session=s;store.save(s) }
    fun feedback(bad:Boolean=false) {
        if(sound)view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
        if(haptics)view.performHapticFeedback(if(bad)android.view.HapticFeedbackConstants.LONG_PRESS else android.view.HapticFeedbackConstants.CLOCK_TICK)
    }
    fun clearHints() { highlight=null;laserPreview=null;ringPreview=null }
    fun levels() { motionJob?.cancel();motionId=null;selection=true;paused=false;clearHints() }
    fun load(level:Long,different:Boolean=false) {
        motionJob?.cancel();motionId=null;loading=true;clearHints();selected=null
        scope.launch {
            val prepared=runCatching {
                val saved=withContext(Dispatchers.IO) { store.saved(kind,level) }
                runInterruptible(Dispatchers.Default) {
                    if(saved!=null && !different)WorkshopSession.fresh(saved.puzzle,mode)
                    else store.generate(kind,level,mode,if(different)WorkshopSeeds.next(saved?.puzzle?.variation ?: 0) else 0)
                }
            }.getOrElse { if(it is CancellationException)throw it;null }
            if(prepared==null)message="A fresh validated puzzle could not be prepared. Please try again."
            else { update(prepared);selection=false }
            loading=false
        }
    }
    fun retry() {
        val s=session ?: return
        motionJob?.cancel();motionId=null;selected=null;clearHints()
        update(WorkshopSession.fresh(s.puzzle,s.mode))
        message=if(kind==WorkshopKind.Laser)"Explore freely. Normal rotations never cost a life." else "Turn freely. Align the opening with every remaining collar."
    }
    LaunchedEffect(kind) {
        session=withContext(Dispatchers.IO) { store.active(kind)?.copy(practice=false) }
        mode=session?.mode ?: PuzzleMode.Normal
        page=((store.highest(kind)-1)/12)*12+1;loading=false
    }
    LaunchedEffect(selection,paused,settings,session?.complete,session?.lives,foreground) {
        while(foreground && !selection && !paused && !settings && session!=null && session?.complete==false && session!!.lives>0) {
            delay(1000);session?.let { update(it.copy(seconds=if(it.seconds==Long.MAX_VALUE)it.seconds else it.seconds+1)) }
        }
    }
    LaunchedEffect(session?.mistakes) { if((session?.mistakes ?: 0)>0 && !reducedMotion) { heartScale.animateTo(1.15f,tween(100));heartScale.animateTo(1f,tween(180)) } }
    LaunchedEffect(session?.complete,session?.puzzle) { session?.takeIf { it.complete }?.let { if(withContext(Dispatchers.IO) { store.claimReward(it) })onSolved(it) } }
    BackHandler { if(settings)settings=false else if(paused)paused=false else if(selection)onExit() else levels() }
    fun mistake(reason:String) {
        val s=session ?: return
        if(s.lives<=0 || s.complete || motionId!=null)return
        update(s.copy(lives=if(s.mode==PuzzleMode.Relaxed)s.lives else s.lives-1,mistakes=s.mistakes+1))
        message=reason;feedback(true)
    }
    fun laserTurn(id:Int,target:Int?=null) {
        val s=session ?: return;val p=s.puzzle as? LaserLevel ?: return
        if(motionId!=null || loading || s.complete || s.lives<=0)return
        val o=p.objects.firstOrNull { it.id==id && it.movable } ?: return
        val old=s.state.mirrors[id] ?: o.rotation;val goal=target ?: (1-old)
        if(goal==old)return
        motionId=id;motionGoal=goal.toDouble();releasing=false;clearHints()
        motionJob=scope.launch {
            progress.snapTo(0f);progress.animateTo(1f,tween(if(reducedMotion)1 else 230))
            session?.let { current -> update(current.changed(current.state.copy(mirrors=current.state.mirrors+(id to goal)))) }
            motionId=null;feedback()
            val now=session!!;val trace=LaserEngine.trace(p,now.state.mirrors)
            if(target==null && (trace.lit.size>LaserEngine.trace(p,s.state.mirrors).lit.size ||
                p.solution.count { (key,value)->now.state.mirrors[key]==value }>p.solution.count { (key,value)->s.state.mirrors[key]==value })) praise.success()
            message=if(trace.complete)"Every receiver has the correct light." else if(o.type==OpticType.Prism)"Dichroic prism: red and blue reflect; green transmits." else "${trace.lit.size} of ${p.objects.count { it.type==OpticType.Receiver }} crystals illuminated."
        }
    }
    fun ringTurn(id:Int,angle:Double,animate:Boolean=false,explicitRelease:Boolean=false) {
        val s=session ?: return;val p=s.puzzle as? RingLevel ?: return
        if(motionId!=null || loading || s.complete || s.lives<=0 || id in s.state.released)return
        val goal=RingEngine.snap(angle);val clearance=RingEngine.clearance(p,id,goal,s.state.released)
        if(explicitRelease && !clearance.free) { highlight=clearance.blockedBy.firstOrNull() ?: id;mistake(clearance.reason);return }
        val old=s.state.angles[id] ?: 0.0
        if(!explicitRelease && RingEngine.distance(old,goal)<.01 && !clearance.free)return
        selected=id;motionId=id;motionGoal=goal;releasing=false;clearHints()
        motionJob=scope.launch {
            if(animate) { progress.snapTo(0f);progress.animateTo(1f,tween(if(reducedMotion)1 else 300)) }
            session?.let { current -> update(current.changed(current.state.copy(angles=current.state.angles+(id to goal)))) }
            if(clearance.free) {
                releasing=true;progress.snapTo(0f);progress.animateTo(1f,tween(if(reducedMotion)1 else 450))
                session?.let { update(it.copy(state=it.state.copy(released=it.state.released+id))) }
                praise.success(byHint=animate)
                selected=null;message="Ring ${id+1} is free. Its connector links retract."
            } else message=clearance.reason
            motionId=null;releasing=false;feedback()
        }
    }
    fun requestHint(type:Int) {
        val s=session ?: return
        if(loading || motionId!=null || s.complete || s.lives<=0)return
        hintMenu=false;loading=true;praise.usedHint()
        scope.launch {
            val advice=withContext(Dispatchers.Default) {
                when(val p=s.puzzle) {
                    is LaserLevel->LaserSolver.hint(p,s.state.mirrors)?.let { it.first to it.second.toDouble() }
                    is RingLevel->RingSolver.hint(p,s.state.released,s.state.angles)?.let { it.id to it.angle }
                    else->null
                }
            }
            loading=false
            if(advice==null) { message="No verified move found within the solver budget. Try undoing your last move.";return@launch }
            session?.let { update(it.copy(hintsUsed=it.hintsUsed+1,practice=false)) }
            highlight=advice.first;selected=advice.first
            if(kind==WorkshopKind.Laser) {
                message=if(type==1)"The highlighted optical component is useful next." else "Set the highlighted component to ${if(advice.second.toInt()==0)"/" else "\\"}."
                if(type==2)laserPreview=advice.first to advice.second.toInt()
                if(type==3)laserTurn(advice.first,advice.second.toInt())
            } else {
                val old=s.state.angles[advice.first] ?: 0.0
                val delta=((advice.second-old+540)%360)-180
                message=if(type==1)"The highlighted ring can be freed next." else "Turn ring ${advice.first+1} ${if(delta>=0)"clockwise" else "counterclockwise"} to the gold guide (${advice.second.toInt()}°)."
                if(type==2)ringPreview=RingMove(advice.first,advice.second)
                if(type==3)ringTurn(advice.first,advice.second,true)
            }
            feedback()
        }
    }
    val backdrop=if(kind==WorkshopKind.Laser)listOf(Color(0xFFFFFAEF),Color(0xFFF4E8CE)) else listOf(Color(0xFFFFFBE8),Color(0xFFFFF0CC))
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(backdrop))) {
        Column(Modifier.widthIn(max=620.dp).fillMaxSize().align(Alignment.TopCenter).statusBarsPadding().navigationBarsPadding().padding(horizontal=18.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            if(selection) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    IconButton(onClick=onExit) { Icon(Icons.Rounded.ArrowBack,"Games home",tint=warmInk) }
                    Spacer(Modifier.weight(1f));Text("IQ LABS",color=warmInk.copy(alpha=.55f),letterSpacing=3.sp,fontSize=12.sp)
                    Spacer(Modifier.weight(1f));IconButton(onClick={ settings=true }) { Icon(Icons.Rounded.Settings,"Settings",tint=warmInk) }
                }
                Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        val titleSize=if(compact)36.sp else if(kind==WorkshopKind.Laser)48.sp else 44.sp
                        Text(if(kind==WorkshopKind.Laser)"Laser" else "Rotate",fontSize=titleSize,fontWeight=FontWeight.Black,color=if(kind==WorkshopKind.Laser)gold else Color(0xFFF0A317),style=LocalTextStyle.current.copy(shadow=Shadow(warmInk.copy(alpha=.6f),Offset(2f,5f),2f)))
                        Text(if(kind==WorkshopKind.Laser)"Mirrors" else "Rings",fontSize=titleSize,fontWeight=FontWeight.Black,color=if(kind==WorkshopKind.Laser)warmInk else Color(0xFF248AD5),style=LocalTextStyle.current.copy(shadow=Shadow(Color(0xFF54279C).copy(alpha=.45f),Offset(2f,5f),2f)))
                    }
                }
                Text(if(kind==WorkshopKind.Laser)"∞  I N F I N I T E   P U Z Z L E S  ∞" else "Unlock and free every ring",color=warmInk.copy(alpha=.75f),fontSize=13.sp,modifier=Modifier.align(Alignment.CenterHorizontally))
                WorkshopHero(kind,Modifier.fillMaxWidth().height(if(compact)60.dp else 105.dp))
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    PuzzleMode.entries.forEach { m ->
                        Column(Modifier.weight(1f).shadow(3.dp,RoundedCornerShape(20.dp)).clip(RoundedCornerShape(20.dp)).background(if(mode==m)Brush.verticalGradient(listOf(Color(0xFFF1C36A),Color(0xFFC98737))) else Brush.verticalGradient(listOf(Color.White.copy(alpha=.85f),Color(0xFFF9EDD7)))).border(1.5.dp,Color.White,RoundedCornerShape(20.dp)).clickable { mode=m }.padding(vertical=13.dp,horizontal=3.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            Icon(when(m){PuzzleMode.Normal->Icons.Rounded.WbSunny;PuzzleMode.Challenge->Icons.Rounded.LocalFireDepartment;PuzzleMode.Relaxed->Icons.Rounded.Spa},null,tint=if(mode==m)Color.White else gold,modifier=Modifier.size(27.dp))
                            Spacer(Modifier.height(5.dp));Text(if(m==PuzzleMode.Normal)"Regular" else m.name,color=if(mode==m)Color.White else warmInk,fontWeight=FontWeight.Bold,fontSize=13.sp)
                            Text(when(m){PuzzleMode.Normal->"5 lives";PuzzleMode.Challenge->"3 lives";PuzzleMode.Relaxed->"No pressure"},color=if(mode==m)Color.White.copy(alpha=.85f) else warmInk.copy(alpha=.65f),fontSize=10.sp)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                    Text("Levels",fontWeight=FontWeight.Bold,color=warmInk)
                    TextButton(onClick={ rules=true }) { Text("How to play",fontSize=12.sp,color=warmInk) }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    repeat(3) { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        repeat(4) { col ->
                            val offset=row*4L+col;val n=if(page>Long.MAX_VALUE-offset)Long.MAX_VALUE else page+offset
                            val unlocked=n<=store.highest(kind);val stars=store.best(kind,n)
                            Column(Modifier.weight(1f).height(if(compact)56.dp else 64.dp).shadow(2.dp,RoundedCornerShape(15.dp)).clip(RoundedCornerShape(15.dp)).background(if(session?.puzzle?.level==n)Color(0xFFF2D69A) else Color(0xFFFFFBF1)).border(1.dp,Color.White,RoundedCornerShape(15.dp)).clickable(enabled=unlocked && !loading) { load(n) },horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                                Text("$n",color=if(unlocked)warmInk else warmInk.copy(alpha=.35f),fontWeight=FontWeight.Bold,fontSize=16.sp)
                                if(!unlocked)Icon(Icons.Rounded.Lock,null,Modifier.size(15.dp),tint=warmInk.copy(alpha=.3f)) else if(stars>0)Text("★".repeat(stars),color=Color(0xFFF5AD19),fontSize=11.sp)
                            }
                        }
                    } }
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    TextButton(onClick={ page=(page-12).coerceAtLeast(1) },enabled=page>1) { Text("Previous",color=warmInk) }
                    TextButton(onClick={ if(page<=Long.MAX_VALUE-12)page+=12 },enabled=page<=store.highest(kind)-12) { Text("More levels",color=warmInk) }
                }
                GlossButton(if(session!=null)"Continue · Level ${session!!.puzzle.level}" else "Play Level 1",{
                    val old=session
                    if(old!=null && old.mode==mode)selection=false else load(old?.puzzle?.level ?: 1)
                },Modifier.fillMaxWidth(),primary=true,enabled=!loading,icon=Icons.Rounded.PlayArrow)
            } else {
                val s=session
                if(s==null) { CircularProgressIndicator();return@Column }
                val busy=loading || motionId!=null;val playable=!busy && s.lives>0 && !s.complete
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    IconButton(onClick=::levels) { Icon(Icons.Rounded.ArrowBack,"Level selection",tint=warmInk) }
                    Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
                        Text("Level ${s.puzzle.level}",color=warmInk,fontSize=24.sp,fontWeight=FontWeight.Black)
                        Text("${WorkshopSeeds.tiers[WorkshopSeeds.tier(s.puzzle.level)]} · ${if(s.mode==PuzzleMode.Normal)"Regular" else s.mode.name}",fontSize=11.sp,color=warmInk.copy(alpha=.6f))
                    }
                    com.indianservers.iqlabs.ui.components.PlayAssistButtons(s.lives,
                        onLives={ message=if(s.mode==PuzzleMode.Relaxed)"Unlimited lives in Relaxed mode." else "${s.lives} / ${s.mode.lives} lives. Rotating is free; failed Challenge beam checks and blocked ring releases cost one life." },
                        onHint={ requestHint(2) },hintEnabled=playable,unlimited=s.mode==PuzzleMode.Relaxed)
                    IconButton(onClick={ paused=true }) { Icon(Icons.Rounded.Pause,"Pause",tint=warmInk) }
                }
                QuickPraiseBanner(praise)
                Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                    if(kind==WorkshopKind.Laser) LaserBoard(s,Modifier.fillMaxSize(),highlight,laserPreview,motionId,motionGoal.toInt(),progress.value,playable,{ laserTurn(it) })
                    else RingsBoard(s,Modifier.fillMaxSize(),selected,highlight,ringPreview,if(releasing)motionId else null,progress.value,if(!releasing)motionId else null,motionGoal,progress.value,playable,{ selected=it;clearHints() },{ id,a->ringTurn(id,a) })
                    if(loading)CircularProgressIndicator(color=gold)
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    val completed=when(val p=s.puzzle) { is LaserLevel->LaserEngine.trace(p,s.state.mirrors).lit.size to p.objects.count { it.type==OpticType.Receiver };is RingLevel->s.state.released.size to p.rings.size;else->0 to 1 }
                    Text("${completed.first}/${completed.second} ${if(kind==WorkshopKind.Laser)"crystals lit" else "rings free"}",fontSize=12.sp,color=warmInk)
                    Text("${s.moves} moves · ${s.seconds/60}:${(s.seconds%60).toString().padStart(2,'0')}",fontSize=12.sp,color=warmInk)
                }
                Text(message,color=warmInk.copy(alpha=.75f),fontSize=12.sp,minLines=2,maxLines=3)
                if(kind==WorkshopKind.Laser) {
                    GlossButton(if(s.mode==PuzzleMode.Challenge)"Test beam · failed check costs 1 life" else "Check light · experimenting is free",{
                        if(!s.complete) { if(s.mode==PuzzleMode.Challenge)mistake("Some receivers are not correctly illuminated. Try another orientation.") else message="Keep exploring. Valid rotations and checks cost no lives in Regular mode." }
                    },Modifier.fillMaxWidth(),enabled=playable,icon=Icons.Rounded.Bolt)
                } else Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    GlossButton("Turn 15°",{ selected?.let { ringTurn(it,(session!!.state.angles[it] ?: 0.0)+15) } },Modifier.weight(1f),enabled=playable && selected!=null,icon=Icons.Rounded.RotateRight)
                    GlossButton("Release ring",{ selected?.let { ringTurn(it,session!!.state.angles[it] ?: 0.0,explicitRelease=true) } },Modifier.weight(1f),primary=true,enabled=playable && selected!=null,icon=Icons.Rounded.NorthEast)
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    ToolButton("Undo",Icons.Rounded.Undo,playable && s.undo.isNotEmpty()) {
                        val now=session!!;update(now.copy(state=now.undo.last(),undo=now.undo.dropLast(1)));selected=null;clearHints();message="Last board move undone. Lives and hint use are unchanged."
                    }
                    ToolButton("Restart",Icons.Rounded.Refresh,!busy,::retry)
                    ToolButton("Hint",Icons.Rounded.Lightbulb,playable) { hintMenu=true }
                    ToolButton("Settings",Icons.Rounded.Settings) { settings=true }
                }
                if(s.complete) WorkshopCompletion(s,{
                    if(s.puzzle.level==Long.MAX_VALUE)load(s.puzzle.level,true) else load(WorkshopSeeds.next(s.puzzle.level))
                },::retry,::levels,onExit,reducedMotion)
                else if(s.lives==0) AlertDialog(onDismissRequest={},title={ Text("Try a new approach",color=warmInk) },text={ Text("Your completed levels are safe. Retry this same puzzle with full lives, or choose an equivalent variation.") },confirmButton={ TextButton(onClick=::retry) { Text("Retry Level") } },dismissButton={ Column { TextButton(onClick={ load(s.puzzle.level,true) }) { Text("Different puzzle") };TextButton(onClick=::levels) { Text("Exit to levels") } } })
            }
        }
        if(loading && selection)CircularProgressIndicator(Modifier.align(Alignment.Center),color=gold)
    }
    if(hintMenu)AlertDialog(onDismissRequest={ hintMenu=false },title={ Text("Choose your hint",color=warmInk) },text={ Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("Hints are always available. No lives are used, and scoring stays enabled.",fontSize=13.sp)
        GlossButton("1 · Highlight a useful object",{ requestHint(1) },Modifier.fillMaxWidth())
        GlossButton("2 · Preview the manipulation",{ requestHint(2) },Modifier.fillMaxWidth())
        GlossButton("3 · Perform one valid move",{ requestHint(3) },Modifier.fillMaxWidth(),primary=true)
    } },confirmButton={ TextButton(onClick={ hintMenu=false }) { Text("Close") } })
    if(paused)AlertDialog(onDismissRequest={ paused=false },title={ Text("Take a breath",color=warmInk) },text={ Text("Your puzzle is paused and saved.") },confirmButton={ TextButton(onClick={ paused=false }) { Text("Resume") } },dismissButton={ TextButton(onClick=::levels) { Text("Levels") } })
    if(settings)AlertDialog(onDismissRequest={ settings=false },title={ Text("Your play space",color=warmInk) },text={ Column {
        Row(verticalAlignment=Alignment.CenterVertically) { Text("Sound",Modifier.weight(1f));Switch(sound,onSound) }
        Row(verticalAlignment=Alignment.CenterVertically) { Text("Haptics",Modifier.weight(1f));Switch(haptics,onHaptics) }
        TextButton(onClick={ settings=false;rules=true }) { Text("Rules and mechanical model",color=warmInk) }
        Text("Reduced motion follows the shared IQ LABS preference. All progress works offline.",fontSize=12.sp)
    } },confirmButton={ TextButton(onClick={ settings=false }) { Text("Done") } })
    if(rules)AlertDialog(onDismissRequest={ rules=false },title={ Text(kind.title,color=warmInk) },text={ Text(if(kind==WorkshopKind.Laser)"Tap a silver mirror or blue splitter to flip its surface. Rays reflect from the real surface geometry. A splitter sends half the light straight and half reflected. Filters remove RGB channels. The coated prism reflects red and blue into opposite branches while transmitting green. Overlapping source light mixes additively. Every crystal must receive its exact colour simultaneously. Regular rotations are free; Challenge deducts a life only on an explicit failed Test beam." else "These are open C-rings in fixed retaining collars, not physically intersecting 3D rings. Drag around a ring to rotate it, or select it and use Turn 15°. Gold collars mark base guides; blue collars connect neighbours. The opening must clear every active collar before the ring lifts out of its track. Connector links retract when either ring leaves. Rotating is free; an explicit blocked Release costs one life. Pinch to zoom and drag empty space to move the board.") },confirmButton={ TextButton(onClick={ rules=false }) { Text("Let's play") } })
}

@Composable
private fun WorkshopCompletion(s:WorkshopSession,onNext:()->Unit,onReplay:()->Unit,onLevels:()->Unit,onHome:()->Unit,reducedMotion:Boolean) {
    val celebration=remember { Animatable(0f) }
    LaunchedEffect(Unit) { celebration.animateTo(1f,tween(if(reducedMotion)1 else 1400)) }
    AlertDialog(onDismissRequest={},title={ Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(86.dp),contentAlignment=Alignment.Center) {
            if(!reducedMotion)Canvas(Modifier.fillMaxSize()) {
                repeat(35) { i->
                    val x=size.width*((i*37%101)/100f);val y=size.height*((i*19%67)/67f+celebration.value*.9f)%size.height
                    rotate(i*13f+celebration.value*120,Offset(x,y)) { drawRect(jewelColours[i%jewelColours.size].copy(alpha=1-celebration.value*.75f),Offset(x,y),androidx.compose.ui.geometry.Size(5f,11f)) }
                }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(4.dp),modifier=Modifier.graphicsLayer(scaleX=.7f+.3f*celebration.value,scaleY=.7f+.3f*celebration.value)) {
                repeat(3) { i->Icon(Icons.Rounded.Star,null,tint=if(i<s.stars)Color(0xFFFFBB25) else Color(0xFFD7CDBA),modifier=Modifier.size(if(i==1)56.dp else 45.dp)) }
            }
        }
        Text("Level Complete!",fontWeight=FontWeight.Black,color=warmInk,fontSize=27.sp)
        Text("Level ${s.puzzle.level} · Beautifully solved",fontSize=13.sp,color=warmInk.copy(alpha=.7f))
    } },text={ Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        listOf("Moves" to s.moves.toString(),"Mistakes" to s.mistakes.toString(),"Time" to "${s.seconds/60}:${(s.seconds%60).toString().padStart(2,'0')}","Hints Used" to s.hintsUsed.toString(),"Score" to if(s.practice)"Practice" else s.score.toString()).forEach { (label,value)->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text(label,color=warmInk);Text(value,fontWeight=FontWeight.Bold,color=warmInk) } }
        GlossButton("Next Level",onNext,Modifier.fillMaxWidth(),true,icon=Icons.Rounded.ChevronRight)
        GlossButton("Replay",onReplay,Modifier.fillMaxWidth(),icon=Icons.Rounded.Refresh)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) { TextButton(onClick=onLevels) { Text("Levels",color=warmInk) };TextButton(onClick=onHome) { Text("Games Home",color=warmInk) } }
    } },confirmButton={},containerColor=Color(0xFFFFF7E5))
}
