package com.sylva.vdhr.widget;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by tongzhichao on 16-3-24.
 */
public class DragView extends FrameLayout {

    ArrayList<View> views = new ArrayList<>();

    private ViewDragHelper mDragger;
    private ViewDragHelper.Callback mCallback;

    private int width;
    private int height;

    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCallback = new DrawerCallbak();
        mDragger = ViewDragHelper.create(this, 1.0f, mCallback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        views.clear();
        for (int i = 0; i < getChildCount(); i++) {
            views.add(getChildAt(i));
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getWidth();
        height = getHeight();
    }

    public int Horizontal(View child, int dx) {
        for (View view : views) {
            if (isHorizontal(child, view)) {
                if (isLeft(child, view)) {
                    if (dx > 0) {
                        if (child.getRight() + dx > view.getLeft()) {
                            int move = view.getLeft() - child.getRight();
                            if (dx > move) {
                                dx = move;
                            }
                        }
                    }
                } else {
                    if (dx < 0) {
                        if (child.getLeft() + dx < view.getRight()) {
                            int move = view.getRight() - child.getLeft();
                            if (dx < move) {
                                dx = move;
                            }
                        }
                    }
                }
            }
        }
        int left = child.getLeft() + dx;
        int leftBound = getPaddingLeft();
        int rightBound = getWidth() - child.getWidth() - getPaddingRight();

        int newLeft = Math.min(Math.max(left, leftBound), rightBound);
        return newLeft;
    }

    private int Vertical(View child, int dy) {
        for (View view : views) {
            if (isVertical(child, view)) {
                if (isTop(child, view)) {
                    if (dy > 0) {
                        if (child.getBottom() + dy > view.getTop()) {
                            int move = view.getTop() - child.getBottom();
                            if (dy > move) {
                                dy = move;
                            }
                        }
                    }
                } else {
                    if (dy < 0) {
                        if (child.getTop() + dy < view.getBottom()) {
                            int move = view.getBottom() - child.getTop();
                            if (dy < move) {
                                dy = move;
                            }
                        }
                    }
                }
            }
        }
        int top = child.getTop() + dy;
        int topBound = getPaddingTop();
        int bottomBound = getWidth() - child.getWidth() - getPaddingBottom();

        int newTop = Math.min(Math.max(top, topBound), bottomBound);
        return newTop;
    }

    private boolean isHorizontal(View child, View view) {
        if (child == view) {
            return false;
        }
        if ((view.getTop() >= child.getTop() && view.getTop() < child.getBottom())
                || (view.getBottom() > child.getTop() && view.getBottom() <= child.getBottom())) {
            return true;
        }
        return false;
    }

    private boolean isLeft(View child, View view) {
        if (child.getRight() <= view.getLeft()) {
            return true;
        }
        return false;
    }

    private boolean isTop(View child, View view) {
        if (child.getBottom() <= view.getTop()) {
            return true;
        }
        return false;
    }

    private boolean isVertical(View child, View view) {
        if (child == view) {
            return false;
        }
        if ((view.getLeft() >= child.getLeft() && view.getLeft() < child.getRight())
                || (view.getRight() > child.getLeft() && view.getRight() <= child.getRight())) {
            return true;
        }
        return false;
    }


    class DrawerCallbak extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Horizontal(child, dx);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return Vertical(child, dy);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragger.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragger.processTouchEvent(event);
        return true;
    }
}
