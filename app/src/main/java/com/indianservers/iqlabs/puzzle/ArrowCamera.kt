package com.indianservers.iqlabs.puzzle

import kotlin.math.*

/** Invertible world/screen camera. .25x fits the complete board; 1x is the
 * normal working view, and 5x keeps very large worlds navigable. */
data class ArrowCamera(val x:Float,val y:Float,val zoom:Float) {
    data class Point(val x:Float,val y:Float)
    fun scale(width:Float,height:Float,board:Int)=4f*min(width,height)/(board+1)*zoom
    fun worldToScreen(wx:Float,wy:Float,width:Float,height:Float,board:Int):Point {
        val scale=scale(width,height,board)
        return Point(width/2+(wx-x)*scale,height/2+(wy-y)*scale)
    }
    fun screenToWorld(sx:Float,sy:Float,width:Float,height:Float,board:Int):Point {
        val scale=scale(width,height,board)
        return Point(x+(sx-width/2)/scale,y+(sy-height/2)/scale)
    }
    fun zoomAt(factor:Float,sx:Float,sy:Float,width:Float,height:Float,board:Int):ArrowCamera {
        val anchor=screenToWorld(sx,sy,width,height,board)
        val z=(zoom*factor).coerceIn(.25f,5f)
        val scale=4f*min(width,height)/(board+1)*z
        return copy(x=anchor.x-(sx-width/2)/scale,y=anchor.y-(sy-height/2)/scale,zoom=z).constrained(width,height,board)
    }
    fun pan(dx:Float,dy:Float,width:Float,height:Float,board:Int):ArrowCamera {
        val scale=scale(width,height,board)
        return copy(x=x-dx/scale,y=y-dy/scale).constrained(width,height,board)
    }
    fun constrained(width:Float,height:Float,board:Int):ArrowCamera {
        val extent=board+1f
        val halfX=width/scale(width,height,board)/2;val halfY=height/scale(width,height,board)/2
        fun bound(c:Float,half:Float)=if(half>=extent/2)extent/2 else c.coerceIn(half*.8f,extent-half*.8f)
        return copy(x=bound(x,halfX),y=bound(y,halfY),zoom=zoom.coerceIn(.25f,5f))
    }
    companion object {
        fun fit(board:Int)=ArrowCamera((board+1)/2f,(board+1)/2f,.25f)
        fun initial(board:Int,level:Long)=fit(board).copy(zoom=if(level<=3).25f else ((board+1)/80f).coerceIn(.25f,5f))
    }
}
