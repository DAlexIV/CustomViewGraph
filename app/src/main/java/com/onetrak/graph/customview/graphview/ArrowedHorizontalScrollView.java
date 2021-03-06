package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by aleksey.ivanov on 28.03.2016.
 */
public class ArrowedHorizontalScrollView extends HorizontalScrollView {
    Paint mArrowPaint;
    Path mLeftArrowPath;
    public static final float footerRatio = 0.1f;
    public static final float lineRatio = 0.01f;
    public static final float marginRatio = 0.025f;

    public ArrowedHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWillNotDraw(false);
        mLeftArrowPath = new Path();

        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setColor(Color.BLACK);
        mArrowPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        mArrowPaint.setStrokeWidth(canvas.getHeight() * lineRatio);
        if (getScrollX() != 0)
            drawLeftArrow(canvas, getScrollX());
        if (getScrollX() + canvas.getWidth() != getChildAt(0).getWidth())
            drawRightArrow(canvas, getScrollX() + canvas.getWidth());
    }


    private void drawLeftArrow(Canvas canvas, float globalIndent) {
        float indent = marginRatio * canvas.getHeight();
        float belowIndent = footerRatio * canvas.getHeight();
        mLeftArrowPath.reset();
        mLeftArrowPath.moveTo(globalIndent + 2 * indent, canvas.getHeight() - belowIndent - 3 * indent);
        mLeftArrowPath.lineTo(globalIndent + indent, canvas.getHeight() - belowIndent - 2 * indent);
        mLeftArrowPath.lineTo(globalIndent + 2 * indent, canvas.getHeight() - belowIndent - indent);
        canvas.drawPath(mLeftArrowPath, mArrowPaint);
    }

    private void drawRightArrow(Canvas canvas, float globalIndent) {
        float indent = marginRatio * canvas.getHeight();
        float belowIndent = footerRatio * canvas.getHeight();
        mLeftArrowPath.reset();
        mLeftArrowPath.moveTo(globalIndent - 2 * indent, canvas.getHeight() - belowIndent - 3 * indent);
        mLeftArrowPath.lineTo(globalIndent - indent, canvas.getHeight() - belowIndent - 2 * indent);
        mLeftArrowPath.lineTo(globalIndent - 2 * indent, canvas.getHeight() - belowIndent - indent);
        canvas.drawPath(mLeftArrowPath, mArrowPaint);
    }
}

