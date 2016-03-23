package com.sylva.vdhr.widget;

import android.content.Context;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by tongzhichao on 16-3-22.
 * DrawerLayout extends FrameLayout by ViewDragHelper
 */
public class DrawerLayout extends FrameLayout {
    private ViewDragHelper mDragger;
    private ViewDragHelper.Callback mCallback;
    private int mDefaultslideWidth;
    private boolean mIsOpen = false;
    private View mContentView;
    private boolean mIsScrolled = false;
    private boolean mIsAutoScrolled = false;
    private OnStateChangedListener mOnStateChangedListener;

    public DrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCallback = new DrawerCallbak();
        mDragger = ViewDragHelper.create(this, 1.0f, mCallback);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContentView = getChildAt(1);
        if (mContentView == null) {
            throw new NullPointerException("contentview is null");
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getChildAt(0) != null) {
            mDefaultslideWidth = getChildAt(0).getWidth();
        } else {
            try {
                mDefaultslideWidth = getChildAt(0).getWidth();
            } catch (NullPointerException e) {
                Log.e("DrawerLayout", "Layout has at least one child view!");
            }

        }
    }

    private class DrawerCallbak extends ViewDragHelper.Callback {


        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if (mIsAutoScrolled) {
                return false;
            }
            return child == mContentView;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return Math.max(Math.min(mDefaultslideWidth, left), 0);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return Math.max(Math.min(mDefaultslideWidth, child.getLeft()), 0);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return super.getViewVerticalDragRange(child);
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            switch (state) {
                case ViewDragHelper.STATE_DRAGGING:
                    mIsScrolled = true;
                    break;
                case ViewDragHelper.STATE_IDLE:
                    mIsAutoScrolled = false;
                    mIsScrolled = false;
                    if (mContentView.getLeft() == 0) {
                        mIsOpen = false;
                    } else {
                        mIsOpen = true;
                    }
                    if (mOnStateChangedListener != null) {
                        if (mIsOpen) {
                            mOnStateChangedListener.onOpen(mContentView);
                        } else {
                            mOnStateChangedListener.onClosed(mContentView);
                        }
                    }
                    break;
                case ViewDragHelper.STATE_SETTLING:
                    mIsAutoScrolled = true;
                    break;

            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mContentView) {
                if (mOnStateChangedListener != null) {
                    mOnStateChangedListener.onScrolled(mContentView, (int) (((float) left / (float) mDefaultslideWidth) * 100));
                }
            }
            super.onViewPositionChanged(changedView, left, top, dx, dy);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (mIsAutoScrolled) {
                return;
            }
            if (releasedChild == mContentView) {
                if (mIsScrolled) {
                    if (xvel <= 0) {
                        mDragger.settleCapturedViewAt(0, 0);
                    } else {
                        mDragger.settleCapturedViewAt(mDefaultslideWidth, 0);
                    }
                } else if (mIsOpen) {
                    if (xvel <= 0) {
                        mDragger.settleCapturedViewAt(0, 0);
                    }
                } else {
                    if (xvel > 0) {
                        mDragger.settleCapturedViewAt(mDefaultslideWidth, 0);
                    }
                }
                invalidate();
            } else {
                super.onViewReleased(releasedChild, xvel, yvel);
            }
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


    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    public void open() {
        if (!mIsOpen) {
            controlView();
        }
    }

    public void close() {
        if (mIsOpen) {
            controlView();
        }
    }

    public void controlView() {
        if (mIsScrolled || mIsAutoScrolled) {
            return;
        }
        if (mIsOpen) {
            mDragger.smoothSlideViewTo(mContentView, 0, 0);
        } else {
            mDragger.smoothSlideViewTo(mContentView, mDefaultslideWidth, 0);
        }
        invalidate();
    }

    public interface OnStateChangedListener {
        void onOpen(View view);

        void onClosed(View view);

        void onScrolled(View view, int percentage);
    }

    public void setOnStateChangedListener(OnStateChangedListener onStateChangedListener) {
        mOnStateChangedListener = onStateChangedListener;
    }

    public boolean isOpen() {
        return mIsOpen;
    }


}
