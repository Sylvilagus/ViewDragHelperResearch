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
class DraggableLayout1 : RelativeLayout {
    private var mViewDragHelper: ViewDragHelper? = null
    private val mChildProperties = HashMap<Int, ChildProperty>()
    private var leftEdge: Int = 0
    private var rightEdge: Int = 0
    private var topEdge: Int = 0
    private var bottomEdge: Int = 0
    private val mCallback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return mChildProperties[child.id]!!.type and ChildProperty.SELF_STEADY == 0
        }

        override fun getViewHorizontalDragRange(child: View?): Int {
            return measuredWidth - child!!.measuredWidth
        }

        override fun getViewVerticalDragRange(child: View?): Int {
            return measuredHeight - child!!.measuredHeight
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val currentChild = mChildProperties[child.id]
            val listSteady = mChildProperties.map { currentChild!!.collisionX(it.value, dx) ?: dx }
            return edgeConstraintX(((if (dx > 0) listSteady.min() else listSteady.max()) ?: dx) + child.left, child.width)
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val currentChild = mChildProperties[child.id]
            val listSteady = mChildProperties.map { currentChild!!.collisionY(it.value, dy) ?: dy }
            return edgeConstraintY(((if (dy > 0) listSteady.min() else listSteady.max()) ?: dy) + child.top, child.height)
        }

        override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
            val cp = mChildProperties[releasedChild!!.id]
            if (cp != null && cp.type and ChildProperty.SELF_ELASTIC == 0) {
                mViewDragHelper!!.settleCapturedViewAt(cp.originalX, cp.originalY)
                invalidate()
            }
        }

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {

        }

        fun edgeConstraintX(x: Int, w: Int): Int = if (x < leftEdge) leftEdge else if (x > rightEdge - w) rightEdge - w else x
        fun edgeConstraintY(y: Int, h: Int): Int = if (y < topEdge) topEdge else if (y > bottomEdge - h) bottomEdge - h else y
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

    fun init() {
        mViewDragHelper = ViewDragHelper.create(this, 1f, mCallback)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        leftEdge = paddingLeft
        rightEdge = measuredWidth - paddingRight
        topEdge = paddingTop
        bottomEdge = measuredHeight - paddingBottom
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        (0 until childCount).map { getChildAt(it) }.
                forEach { mChildProperties.put(it.id, ChildProperty(it, ChildProperty.SELF_MOVEABLE or ChildProperty.RELATION_MOVEABLE)) }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return mViewDragHelper!!.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mViewDragHelper!!.processTouchEvent(event)
        return true
    }

    class ChildProperty(val child: View, val type: Int) {
        val originalX: Int
        val originalY: Int

        init {
            originalX = child.left
            originalY = child.top
        }

        constructor(child: View) : this(child, SELF_MOVEABLE or RELATION_STEADY)

        companion object {
            const val SELF_STEADY = 0x1
            const val SELF_MOVEABLE = 0x2
            const val SELF_ELASTIC = 0x3
            const val RELATION_STEADY = 0x1 shl 4
            const val RELATION_MOVEABLE = 0x2 shl 4
            const val RELATION_ELASTIC = 0x3 shl 4
        }

        fun hasTypeSignature(typeSignature: Int): Boolean {
            return (typeSignature and type) > 0
        }

        fun collisionX(target: ChildProperty, moveIntent: Int): Int? {
            if (target == this)
                return null
            if (target.child.top < child.top - target.child.height || target.child.top > child.bottom)
                return moveIntent
            if (target.hasTypeSignature(ChildProperty.RELATION_ELASTIC)) {
                return moveIntent / Math.abs(target.originalX - target.child.left)
            } else if (target.hasTypeSignature(ChildProperty.RELATION_STEADY)) {
                if (child.right <= target.child.left && moveIntent > 0) {
                    val dist = target.child.left - child.right
                    if (moveIntent > dist)
                        return dist
                }
                if (child.left >= target.child.right && moveIntent < 0) {
                    val dist = target.child.right - child.left
                    if (moveIntent < dist)
                        return dist
                }
                return moveIntent
            } else if (target.hasTypeSignature(ChildProperty.RELATION_MOVEABLE)) {
                return moveIntent
            } else {
                return moveIntent
            }
        }

        fun collisionY(target: ChildProperty, moveIntent: Int): Int? {
            if (target == this)
                return null
            if (target.child.left < child.left - target.child.width || target.child.left > child.right)
                return moveIntent
            if (target.hasTypeSignature(ChildProperty.RELATION_ELASTIC)) {
                return moveIntent / Math.abs(target.originalY - target.child.top)
            } else if (target.hasTypeSignature(ChildProperty.RELATION_STEADY)) {
                if (child.bottom <= target.child.top && moveIntent > 0) {
                    val dist = target.child.top - child.bottom
                    if (moveIntent > dist)
                        return dist
                    else
                        return moveIntent
                }
                if (child.top >= target.child.bottom && moveIntent < 0) {
                    val dist = target.child.bottom - child.top
                    if (moveIntent < dist)
                        return dist
                    else
                        return moveIntent
                }
                return moveIntent
            } else if (target.hasTypeSignature(ChildProperty.RELATION_MOVEABLE)) {
                return moveIntent
            } else {
                return moveIntent
            }

        }

    }
}