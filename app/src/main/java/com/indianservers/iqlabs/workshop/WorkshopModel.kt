package com.indianservers.iqlabs.workshop

import java.security.MessageDigest
import java.util.Locale
import kotlin.math.*

enum class WorkshopKind(val title:String,val id:String) { Laser("Laser Mirrors","laser-mirrors"),Rings("Rotate Rings","rotate-rings") }
object WorkshopSeeds {
    fun hash(s:String)=MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(Locale.ROOT,it) }
    fun seed(kind:WorkshopKind,level:Long,version:Int,variation:Long):Long {
        val bytes=MessageDigest.getInstance("SHA-256").digest("IQ:${kind.name}:$level:$version:$variation".toByteArray(Charsets.UTF_8))
        return bytes.take(8).fold(0L) { n,b -> (n shl 8) or (b.toLong() and 255) }
    }
    fun next(n:Long)=if(n==Long.MAX_VALUE)n else n+1
    fun tier(n:Long)=when {n<=10->0;n<=30->1;n<=75->2;n<=150->3;n<=300->4;n<=600->5;n<=1000->6;else->7}
    val tiers=listOf("Tutorial","Beginner","Easy","Medium","Hard","Expert","Master","Infinite Genius")
}
data class Vec(val x:Double,val y:Double) {
    operator fun plus(b:Vec)=Vec(x+b.x,y+b.y)
    operator fun minus(b:Vec)=Vec(x-b.x,y-b.y)
    operator fun times(k:Double)=Vec(x*k,y*k)
    fun dot(b:Vec)=x*b.x+y*b.y
    fun cross(b:Vec)=x*b.y-y*b.x
    fun length()=sqrt(dot(this))
    fun unit()=this*(1/length())
    fun left()=Vec(y,-x)
    fun right()=Vec(-y,x)
}
interface WorkshopPuzzle {
    val kind:WorkshopKind
    val level:Long
    val variation:Long
    val version:Int
    val seed:Long
    val signature:String
}
