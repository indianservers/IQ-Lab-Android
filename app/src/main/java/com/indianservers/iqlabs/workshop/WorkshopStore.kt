package com.indianservers.iqlabs.workshop

import android.content.Context
import com.indianservers.iqlabs.puzzle.PuzzleMode
import org.json.JSONArray
import org.json.JSONObject

data class WorkshopState(val mirrors:Map<Int,Int> = emptyMap(),val angles:Map<Int,Double> = emptyMap(),val released:Set<Int> = emptySet())
data class WorkshopSession(val puzzle:WorkshopPuzzle,val mode:PuzzleMode,val state:WorkshopState,
    val lives:Int=mode.lives,val hintsUsed:Int=0,val mistakes:Int=0,val moves:Int=0,val seconds:Long=0,
    val undo:List<WorkshopState> = emptyList(),val practice:Boolean=false) {
    val complete:Boolean by lazy { when(val p=puzzle) { is LaserLevel->LaserEngine.trace(p,state.mirrors).complete;is RingLevel->state.released.size==p.rings.size;else->false } }
    val credits get()=(3-hintsUsed).coerceAtLeast(0)
    val stars get()=if(practice)1 else (3-(if(mistakes>0)1 else 0)-(if(hintsUsed>1)1 else 0)).coerceAtLeast(1)
    val score get()=if(practice)0 else (3000+stars*500-mistakes*150-hintsUsed*100).coerceAtLeast(100)
    fun changed(next:WorkshopState)=copy(state=next,moves=moves+1,undo=(undo+state).takeLast(128))
    companion object {
        fun fresh(p:WorkshopPuzzle,mode:PuzzleMode)=WorkshopSession(p,mode,when(p) { is LaserLevel->WorkshopState(mirrors=p.initial);is RingLevel->WorkshopState(angles=p.initial);else->error("Unsupported puzzle") })
    }
}
class WorkshopStore(context:Context,namespace:String="iq_workshop_progress") {
    private val prefs=context.getSharedPreferences(namespace,Context.MODE_PRIVATE)
    fun highest(kind:WorkshopKind)=prefs.getLong("highest_$kind",1).coerceAtLeast(1)
    fun best(kind:WorkshopKind,level:Long)=prefs.getInt("best_${kind}_$level",0)
    fun active(kind:WorkshopKind)=prefs.getString("active_$kind",null)?.let { runCatching { decode(JSONObject(it)) }.getOrNull() }
    fun saved(kind:WorkshopKind,level:Long)=prefs.getString("level_${kind}_$level",null)?.let { runCatching { decode(JSONObject(it)) }.getOrNull() }
    fun save(s:WorkshopSession) {
        val json=encode(s).toString();val p=s.puzzle
        val key="level_${p.kind}_${p.level}";val identity="${p.version}:${p.seed}:${p.signature}"
        val edit=prefs.edit().putString("active_${p.kind}",json).putBoolean("sig_${p.signature}",true)
        if(!prefs.contains(key) || prefs.getString("identity_$key",null)==identity)edit.putString(key,json).putString("identity_$key",identity)
        else edit.putString("attempt_${p.kind}_${p.level}_${p.variation}",json)
        if(s.complete) {
            edit.putLong("highest_${p.kind}",maxOf(highest(p.kind),WorkshopSeeds.next(p.level)))
            if(s.stars>best(p.kind,p.level))edit.putInt("best_${p.kind}_${p.level}",s.stars).putString("performance_${p.kind}_${p.level}",json)
        }
        edit.apply()
    }
    fun claimReward(s:WorkshopSession):Boolean {
        val key="reward_${s.puzzle.kind}_${s.puzzle.level}"
        if(!s.complete || s.practice || prefs.getBoolean(key,false))return false
        return prefs.edit().putBoolean(key,true).commit()
    }
    fun generate(kind:WorkshopKind,level:Long,mode:PuzzleMode,variation:Long=0):WorkshopSession {
        var v=variation
        repeat(24) {
            if(Thread.currentThread().isInterrupted)throw java.util.concurrent.CancellationException()
            val p=when(kind) { WorkshopKind.Laser->LaserGenerator.generate(level,v);WorkshopKind.Rings->RingGenerator.generate(level,v) }
            if(!prefs.contains("sig_${p.signature}"))return WorkshopSession.fresh(p,mode)
            check(v<Long.MAX_VALUE);v++
        }
        error("Fresh layout generation budget exhausted")
    }
    private fun mapInt(map:Map<Int,Int>)=JSONObject().also { o->map.forEach { (k,v)->o.put(k.toString(),v) } }
    private fun mapDouble(map:Map<Int,Double>)=JSONObject().also { o->map.forEach { (k,v)->o.put(k.toString(),v) } }
    private fun ints(o:JSONObject)=o.keys().asSequence().associate { it.toInt() to o.getInt(it) }
    private fun doubles(o:JSONObject)=o.keys().asSequence().associate { it.toInt() to o.getDouble(it) }
    private fun array(items:Iterable<Any>)=JSONArray().also { a->items.forEach(a::put) }
    private fun state(s:WorkshopState)=JSONObject().put("mirrors",mapInt(s.mirrors)).put("angles",mapDouble(s.angles)).put("released",array(s.released))
    private fun state(o:JSONObject)=WorkshopState(ints(o.getJSONObject("mirrors")),doubles(o.getJSONObject("angles")),o.getJSONArray("released").let { a->(0 until a.length()).map { a.getInt(it) }.toSet() })
    private fun puzzle(p:WorkshopPuzzle)=JSONObject().apply {
        put("kind",p.kind.name);put("level",p.level);put("variation",p.variation);put("version",p.version);put("seed",p.seed)
        when(p) {
            is LaserLevel->{
                put("columns",p.columns);put("rows",p.rows);put("initial",mapInt(p.initial));put("solution",mapInt(p.solution))
                put("objects",array(p.objects.map { o->JSONObject().put("id",o.id).put("type",o.type.name).put("x",o.position.x).put("y",o.position.y).put("rotation",o.rotation).put("colour",o.colour) }))
            }
            is RingLevel->{
                put("initial",mapDouble(p.initial));put("solution",array(p.solution.map { JSONObject().put("id",it.id).put("angle",it.angle) }))
                put("rings",array(p.rings.map { r->JSONObject().put("id",r.id).put("x",r.centre.x).put("y",r.centre.y).put("radius",r.radius).put("tube",r.tube).put("gap",r.gap).put("guide",r.guide).put("material",r.material) }))
                put("links",array(p.links.map { JSONObject().put("a",it.a).put("b",it.b) }))
            }
        }
    }
    private fun puzzle(o:JSONObject):WorkshopPuzzle {
        val kind=WorkshopKind.valueOf(o.getString("kind"));val level=o.getLong("level");val variation=o.getLong("variation");val version=o.getInt("version");val seed=o.getLong("seed")
        fun objects(name:String):List<JSONObject> { val a=o.getJSONArray(name);return (0 until a.length()).map(a::getJSONObject) }
        return when(kind) {
            WorkshopKind.Laser->LaserLevel(level,variation,version,seed,o.getInt("columns"),o.getInt("rows"),objects("objects").map { p->Optic(p.getInt("id"),OpticType.valueOf(p.getString("type")),Vec(p.getDouble("x"),p.getDouble("y")),p.getInt("rotation"),p.getInt("colour")) },ints(o.getJSONObject("initial")),ints(o.getJSONObject("solution"))).also { require(LaserGenerator.validate(it)) }
            WorkshopKind.Rings->RingLevel(level,variation,version,seed,objects("rings").map { r->CaptiveRing(r.getInt("id"),Vec(r.getDouble("x"),r.getDouble("y")),r.getDouble("radius"),r.getDouble("tube"),r.getDouble("gap"),r.getDouble("guide"),r.getInt("material")) },objects("links").map { RingLink(it.getInt("a"),it.getInt("b")) },doubles(o.getJSONObject("initial")),objects("solution").map { RingMove(it.getInt("id"),it.getDouble("angle")) }).also { require(RingGenerator.validate(it)) }
        }
    }
    private fun encode(s:WorkshopSession)=JSONObject().put("puzzle",puzzle(s.puzzle)).put("mode",s.mode.name).put("state",state(s.state)).put("lives",s.lives).put("hints",s.hintsUsed).put("mistakes",s.mistakes).put("moves",s.moves).put("seconds",s.seconds).put("practice",s.practice).put("undo",array(s.undo.map { state(it) }))
    private fun decode(o:JSONObject):WorkshopSession {
        val p=puzzle(o.getJSONObject("puzzle"));val current=state(o.getJSONObject("state"));val undo=o.getJSONArray("undo").let { a->(0 until a.length()).map { state(a.getJSONObject(it)) } }
        fun valid(s:WorkshopState)=when(p) {
            is LaserLevel->s.mirrors.all { (id,value)->p.objects.any { it.id==id && it.movable } && value in 0..1 }
            is RingLevel->s.angles.all { (id,value)->p.rings.any { it.id==id } && value.isFinite() && value>=0 && value<360 } && s.released.all { id->p.rings.any { it.id==id } }
            else->false
        }
        require(valid(current) && undo.all(::valid))
        val mode=PuzzleMode.valueOf(o.getString("mode"));val lives=o.getInt("lives")
        require(lives in 0..mode.lives)
        return WorkshopSession(p,mode,current,lives,o.getInt("hints"),o.getInt("mistakes"),o.getInt("moves"),o.getLong("seconds"),undo,o.getBoolean("practice"))
    }
}
