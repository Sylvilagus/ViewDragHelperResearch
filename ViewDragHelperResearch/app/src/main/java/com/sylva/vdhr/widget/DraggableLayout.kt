package com.sylva.vdhr.widget

import android.content.Context
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout

import java.util.HashMap

/**
 * Created by sylva on 2016/3/23.
 */
class DraggableLayout : RelativeLayout {
    private var mViewDragHelper: ViewDragHelper? = null
    private val mChildrenProperties = HashMap<Int, ChildProperty>()
    fun Any.le(prefix:String=":")= Log.e("DraggableLayout",prefix+this.toString())

    private val mCallback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        override fun clampViewPositionHorizontal(child: View?, left: Int, dx: Int): Int {
            dx.le()
            val childProperty=mChildrenProperties[child?.id]
            var leftFinal:Int
            childProperty?:throw IllegalStateException()
            childProperty.currentX=left
            var intersectResult:Int=0
            val barriers=mChildrenProperties.map{it.value}.
                    filter {
                        if(it==childProperty)
                            false
                        else {
                            val ir = childProperty.intersectX(it)
                            if (ir > 0)
                                intersectResult = ir
                            it.type != ChildProperty.Type.INTANGIBLE && ir > 0
                        }
                    }
            barriers.size.le("barriersX.size")
            intersectResult.le("intersectXResult:")

            leftFinal= barriers.map {
                when(intersectResult){
                    ChildProperty.INTERSECT_LT->return it.currentX-childProperty.width-1
                    ChildProperty.SUPERPOSE->return it.currentX-childProperty.width-1
                    ChildProperty.INTERSECT_GT->return it.currentX+it.width+1
                    else->left
                }
            }.min()?:left

            childProperty.currentX=leftFinal
            return leftFinal
        }

        override fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int {
            val childProperty=mChildrenProperties[child?.id]
            var topFinal:Int
            childProperty?:throw IllegalStateException()
            childProperty.currentY=top
            topFinal=mChildrenProperties.map{it.value to childProperty.intersectY(it.value)}.
                    filter {it.first!=childProperty&&it.first.type != ChildProperty.Type.INTANGIBLE && it.second>0}.
                    map {
                        when(it.second){
                            ChildProperty.INTERSECT_LT->return it.first.currentY-childProperty.height-1
                            ChildProperty.SUPERPOSE->return it.first.currentY-childProperty.height-1
                            ChildProperty.INTERSECT_GT->return it.first.currentY+it.first.height+1
                            else->top
                        }
                    }.min()?:top

            childProperty.currentY=topFinal
            return topFinal
        }

        override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
            val cp = mChildrenProperties[releasedChild!!.id]
            if (cp!=null&&cp.type == ChildProperty.Type.ELASTIC) {
                mViewDragHelper!!.settleCapturedViewAt(cp.originalX, cp.originalY)
                invalidate()
                cp.restore()
            }
        }

        override fun getViewHorizontalDragRange(child: View?): Int {
            return measuredWidth - child!!.measuredWidth
        }

        override fun getViewVerticalDragRange(child: View?): Int {
            return measuredHeight - child!!.measuredHeight
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

    private fun init() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mCallback)
        mViewDragHelper!!.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mViewDragHelper!!.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mViewDragHelper!!.processTouchEvent(event)
        return true
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        for (i in 0..childCount - 1) {
            val child = getChildAt(i)
            mChildrenProperties.put(child.id, ChildProperty.fromChild(child, ChildProperty.Type.TANGIBLE))
        }
    }

    override fun computeScroll() {
        if (mViewDragHelper!!.continueSettling(true)) {
            invalidate()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    class ChildProperty private constructor(child: View, type: ChildProperty.Type) {
        val originalX: Int
        val originalY: Int
        val width:Int
        val height:Int
        var currentX:Int
        var currentY:Int
        var type = Type.TANGIBLE
        init {
            originalX = child.left
            originalY = child.top
            currentX=originalX
            currentY=originalY
            width=child.width
            height=child.height
            this.type = type
        }

        enum class Type {
            STEADY, INTANGIBLE, ELASTIC, TANGIBLE
        }

        companion object {
            const val SEPARATE=-1
            const val INTERSECT_LT=1
            const val SUPERPOSE=2
            const val INTERSECT_GT=3
            fun fromChild(child: View, type: Type = Type.TANGIBLE): ChildProperty {
                return ChildProperty(child, type)
            }
        }
        fun intersectX(cp:ChildProperty):Int{
            if(currentY<=cp.currentY-height||currentY>=cp.currentY+cp.height)
                return SEPARATE
            if(currentX>=cp.currentX&&currentX<=cp.currentX+cp.width)
                return INTERSECT_GT
            else if(currentX==cp.currentX)
                return SUPERPOSE
            else if(currentX>=cp.currentX-width&&currentX<=cp.currentX)
                return INTERSECT_LT
            else
                return SEPARATE
        }
        fun intersectY(cp:ChildProperty):Int{
            if(currentX<=cp.currentX-width||currentX>=cp.currentX+cp.width)
                return SEPARATE
            if(currentY>=cp.currentY&&currentY<=cp.currentY+cp.height)
                return INTERSECT_GT
            else if(currentY==cp.currentY)
                return SUPERPOSE
            else if(currentY>=cp.currentY-height&&currentY<=cp.currentY)
                return INTERSECT_LT
            else
                return SEPARATE
        }
        fun restore(){
            currentX=originalX
            currentY=originalY
        }
    }
}
