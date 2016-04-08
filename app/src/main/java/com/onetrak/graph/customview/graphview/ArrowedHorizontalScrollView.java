package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import com.onetrak.graph.customview.graphview.util.BaseGraphView;

/**
 * Created by aleksey.ivanov on 28.03.2016.
 */
public class ArrowedHorizontalScrollView extends HorizontalScrollView {



    public boolean isAnimationFinished = false;


    BaseGraphView chld;

    public ArrowedHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (chld == null) {
            chld = (BaseGraphView) getChildAt(0);
        }

    }

    public boolean isAnimationFinished() {
        return isAnimationFinished;
    }

    public void setAnimationFinished(boolean animationFinished) {
        isAnimationFinished = animationFinished;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (isAnimationFinished) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }
}

