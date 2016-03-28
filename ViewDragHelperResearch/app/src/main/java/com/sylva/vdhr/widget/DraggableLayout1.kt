package com.sylva.vdhr.widget

import android.content.Context
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import java.util.*

/**
 * Created by sylva on 2016/3/25.
 */
class DraggableLayout1:RelativeLayout {
    private var mViewDragHelper:ViewDragHelper?=null
    private val mChildProperties = HashMap<Int,ChildProperty>()
    private val mCallback=object: ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return mChildProperties[child.id]!!.type and ChildProperty.SELF_STEADY==0
        }
        override fun getViewHorizontalDragRange(child: View?): Int {
            return measuredWidth - child!!.measuredWidth
        }

        override fun getViewVerticalDragRange(child: View?): Int {
            return measuredHeight - child!!.measuredHeight
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val currentChild=mChildProperties[child.id]
            val list=mChildProperties.map { currentChild!!.collisionX(it.value,dx)?:dx }
            return ((if(dx>0)list.min()else list.max())?:dx)+child.left
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val currentChild=mChildProperties[child.id]
            val list=mChildProperties.map { currentChild!!.collisionY(it.value,dy)?:dy }
            return ((if(dy>0)list.min()else list.max())?:dy)+child.top
        }

    }
    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    fun init(){
        mViewDragHelper= ViewDragHelper.create(this,1f,mCallback)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        (0 until childCount).map { getChildAt(it) }.
                forEach { mChildProperties.put(it.id, ChildProperty(it,ChildProperty.SELF_STEADY)) }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mViewDragHelper!!.shouldInterceptTouchEvent(ev)
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        mViewDragHelper!!.processTouchEvent(event)
        return true
    }
    class ChildProperty(val child:View,val type:Int){
        constructor(child:View):this(child,SELF_MOVEABLE or RELATION_STEADY)
        companion object{
            const val SELF_STEADY=0x1
            const val SELF_MOVEABLE=0x2
            const val SELF_ELASTIC=0x3
            const val RELATION_STEADY=0x1 shl 4
            const val RELATION_MOVEABLE=0x2 shl 4
            const val RELATION_ELASTIC=0x3 shl 4
        }
        fun collisionX(target:ChildProperty,moveIntent:Int):Int?{
            if(target==this)
                return null
            if(target.child.top<child.top-target.child.height||target.child.top>child.bottom)
                return moveIntent
            if(child.right<=target.child.left&&moveIntent>0){
                    val dist = target.child.left - child.right
                    if (moveIntent > dist)
                        return dist
            }
            if(child.left>=target.child.right&&moveIntent<0) {
                val dist = target.child.right - child.left
                if (moveIntent < dist)
                    return dist
            }

            return moveIntent
        }
        fun collisionY(target:ChildProperty,moveIntent:Int):Int?{
            if(target==this)
                return null
            if(target.child.left<child.left-target.child.width||target.child.left>child.right)
                return moveIntent
            if(child.bottom<=target.child.top&&moveIntent>0) {
                val dist = target.child.top - child.bottom
                if (moveIntent > dist)
                    return dist
                else
                    return moveIntent
            }
            if(child.top>=target.child.bottom&&moveIntent<0) {
                val dist = target.child.bottom - child.top
                if (moveIntent < dist)
                    return dist
                else
                    return moveIntent
            }
            return moveIntent
        }
    }
}