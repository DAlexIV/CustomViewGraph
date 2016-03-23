package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.onetrak.graph.customview.R;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by aleksey.ivanov on 21.03.2016.
 */
public class GraphView extends View {
    // Public data
    String[] months;
    Double[] values;
    int mBackColor1;
    int mBackColor2;
    int mBackLineColor;
    int mGraphLineColor;
    int mTextColor;
    int mDesiredWidth;
    double mGoal;

    // Rects
    RectF mErrRectF;
    RectF mStripeRectF;

    // Paints
    Paint mStripePaint;
    Paint mErrRectPaint;
    Paint mErrTextPaint;
    Paint mLinePaint;
    Paint mGoalPaint;
    Paint mGoalTextPaint;
    Paint mTextPaint;
    Paint mGraphPaint;
    Paint mBigCirclePaint;
    Paint mSmallCirclePaint;

    // Paths
    Path graphPath;
    Path goalPath;
    float[] intervals;

    // Layout sizes
    float stripeWidth;
    float topIndent;
    float belowIndent;
    float graphStrokeWidth;
    float goalStart;
    float goalEnd;

    // Layout arrays
    float[] valuesRealHeight;
    float[] circleCentresX;
    float[] labelsUnderX;
    float[] labelsUnderY;
    float[] monthsMeasured;

    // Constants
    public static final float viewRatio = (float) 9 / 16;
    public static final float headerRatio = 0.05f;
    public static final float footerRatio = 0.1f;
    public static final float arrowRatio = 0.1f;
    public static final int minStripeDp = 50;
    public static final float textRatio = 0.5f;
    public static final double graphStep = 10;
    public static final float borderRatio = 0.1f;
    public static final float bigCircleRatio = 0.025f;
    public static final float smallCircleRatio = 0.0125f;
    public static final double graphRatio = (float) 1 - headerRatio - footerRatio - 2 * borderRatio;
    public static final float stripLength = 5f;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GraphView,
                0, 0
        );
        mBackColor1 = a.getInteger(R.styleable.GraphView_back_color1, 0);
        mBackColor2 = a.getInteger(R.styleable.GraphView_back_color2, 0);
        mBackLineColor = a.getInteger(R.styleable.GraphView_back_line_color, 0);
        mGraphLineColor = a.getInteger(R.styleable.GraphView_graph_line_color, 0);
        mTextColor = a.getInteger(R.styleable.GraphView_text_color, 0);
        mDesiredWidth = a.getInteger(R.styleable.GraphView_real_width, 0);

        a.recycle();
        reinit();
    }

    private void reinit() {
        mErrRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mErrRectPaint.setColor(ContextCompat.getColor(getContext(), R.color.red));

        mErrTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mErrTextPaint.setTextSize(30);
        mErrTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));

        mStripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mBackLineColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);

        mGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraphPaint.setColor(mGraphLineColor);
        mGraphPaint.setDither(true);
        mGraphPaint.setStyle(Paint.Style.STROKE);
        mGraphPaint.setStrokeJoin(Paint.Join.ROUND);
        mGraphPaint.setStrokeCap(Paint.Cap.ROUND);
        mGraphPaint.setAntiAlias(true);

        mGoalPaint = new Paint();
        mGoalPaint.setAntiAlias(false);
        mGoalPaint.setColor(mGraphLineColor);
        mGoalPaint.setStyle(Paint.Style.STROKE);
        intervals = new float[]{stripLength, stripLength};
        mGoalPaint.setPathEffect(new DashPathEffect(intervals, 0));

        mGoalTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGoalTextPaint.setColor(mGraphLineColor);

        mBigCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBigCirclePaint.setColor(mGraphLineColor);
        mBigCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mSmallCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSmallCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        mSmallCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mErrRectF = new RectF();
        mStripeRectF = new RectF();
        graphPath = new Path();

        goalPath = new Path();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Resolving height
        int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 1);

        // Acquiring width of the scrollView parent
        ViewGroup parent = ((ViewGroup) getParent().getParent());

        // Setting desired width if it's given
        // Otherwise filling ScrollView parent
        int minw = (mDesiredWidth == 0)
                ? HelperLayoutClass.getScreenWidth(getContext())
                - parent.getPaddingRight() - parent.getPaddingLeft()
                : (int) (getPaddingLeft() + getPaddingRight()
                + HelperLayoutClass.pixelsToDp(getContext(), mDesiredWidth)), w;

        // Resolving width
        w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        if (months == null || values == null)
        // If data or months are not provided
        {
            mErrRectF.set(0, 0, w, h);
        } else {
            // Calculating indents
            topIndent = (int) (h * headerRatio);
            belowIndent = (int) (h * footerRatio);

            // Counting stripeWidth and indents
            stripeWidth = w / months.length;

            // If stripe is too narrow, then
            // we will increase width of graph to required minimum
            if (stripeWidth < HelperLayoutClass.dpToPixels(getResources(), minStripeDp)) {
                stripeWidth = (int) HelperLayoutClass.dpToPixels(getResources(), minStripeDp);
                w = (int) stripeWidth * months.length;
            }

            // Calculating textSize for labels under stripes months
            HelperLayoutClass.calculateOKTextSize(mTextPaint, textRatio * stripeWidth, months);


            // Changing width of lines with corrections after measurement
            graphStrokeWidth = h / 100;
            mGoalPaint.setStrokeWidth(graphStrokeWidth / 2);
            mGraphPaint.setStrokeWidth(graphStrokeWidth);
            mGraphPaint.setPathEffect(new CornerPathEffect(stripeWidth / 10));

            precalculateLayoutArrays(h);
        }

        setMeasuredDimension(w, h);
    }

    private void precalculateLayoutArrays(int h) {
        // Precalculating data for circles
        valuesRealHeight = new float[values.length];
        circleCentresX = new float[values.length];

        for (int i = 0; i < values.length; ++i) {
            circleCentresX[i] = stripeWidth * ((float) i + 0.5f);
            valuesRealHeight[i] = HelperLayoutClass.convertValuetoHeight(mGoal, values[i], values, h);
        }

        // Precalc textSizes
        monthsMeasured = new float[values.length];
        for (int i = 0; i < months.length; ++i) {
            monthsMeasured[i] = mTextPaint.measureText(months[i]);
        }

        // Precalculating data for text
        labelsUnderX = new float[values.length];
        labelsUnderY = new float[values.length];
        for (int i = 0; i < months.length; ++i) {
            labelsUnderX[i] = stripeWidth * i + 0.5f * (stripeWidth - mTextPaint.measureText(months[i]));
            labelsUnderY[i] = h - belowIndent + mTextPaint.getTextSize();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (months == null || values == null) {
            displayError(canvas);
        } else {
            drawBackground(canvas);
            drawGraphLines(canvas);
        }
    }

    // Remove multiplication ?
    private void drawBackground(Canvas canvas) {
        drawRectsTopAndBelow(canvas);

        for (int i = 0; i < months.length; ++i) {
            mStripeRectF.set(stripeWidth * i, topIndent,
                    stripeWidth * (i + 1), canvas.getHeight() - belowIndent);
            int curColorRes = (i % 2 == 0) ? mBackColor1 : mBackColor2;
            mStripePaint.setColor(curColorRes);

            canvas.drawRect(mStripeRectF, mStripePaint);
        }

        drawBorderLines(canvas);
        drawTextLabelsUnderStripes(canvas);
        drawGoalLineAndText(canvas);
        drawHorizontalLinesAndText(canvas);
    }

    private void drawTextLabelsUnderStripes(Canvas canvas) {
        for (int i = 0; i < months.length; ++i) {
            canvas.drawText(months[i], labelsUnderX[i], labelsUnderY[i], mTextPaint);
        }
    }

    private void drawRectsTopAndBelow(Canvas canvas) {
        mStripeRectF.set(0, 0, canvas.getWidth(), topIndent);
        mStripePaint.setColor(mBackColor2);
        canvas.drawRect(mStripeRectF, mStripePaint);

        mStripeRectF.set(0, canvas.getHeight() - belowIndent, canvas.getWidth(), canvas.getHeight());
        mStripePaint.setColor(mBackColor2);
        canvas.drawRect(mStripeRectF, mStripePaint);
    }

    private void drawBorderLines(Canvas canvas) {
        canvas.drawLine(0, topIndent, canvas.getWidth(), topIndent, mLinePaint);
        canvas.drawLine(0, canvas.getHeight() - belowIndent, canvas.getWidth(),
                canvas.getHeight() - belowIndent, mLinePaint);
    }

    private void drawGoalLineAndText(Canvas canvas) {
        if (mGoal != 0) {
            float value = HelperLayoutClass.convertValuetoHeight(mGoal, mGoal, values, canvas.getHeight());

            // Draw line
            goalPath.reset();
            goalPath.moveTo(0, value);
            goalPath.lineTo(canvas.getWidth(), value);
            canvas.drawPath(goalPath, mGoalPaint);

            // Draw text
            mGoalTextPaint.setTextSize(mTextPaint.getTextSize());
            canvas.drawText(getContext().getString(R.string.goalLineText),
                    mTextPaint.getTextSize() / 2, value - mTextPaint.getTextSize() / 2, mGoalTextPaint);

            // Count constraints
            goalStart = value - mTextPaint.getTextSize() / 2;
            goalStart = value - mTextPaint.getTextSize() / 2;
            goalEnd = value + mTextPaint.getTextSize();
        }
    }

    private void drawHorizontalLinesAndText(Canvas canvas) {
        double min = Collections.min(Arrays.asList(values));
        double max = Collections.max(Arrays.asList(values));

        double firstLineHeight = (((int) min) / 10 + 1) * 10;

        for (double curHeight = firstLineHeight; curHeight < max; curHeight += graphStep) {
            float value = HelperLayoutClass.convertValuetoHeight(mGoal, curHeight, values, canvas.getHeight());

            if (value + 5 * mTextPaint.getTextSize() / 4 < goalStart || value > goalEnd) {
                canvas.drawLine(0, value, canvas.getWidth(), value, mLinePaint);
                canvas.drawText(Integer.toString((int) curHeight) + " "
                                + getContext().getString(R.string.localMeasurementSystem), mTextPaint.getTextSize() / 4,
                        value - mTextPaint.getTextSize() / 4, mTextPaint);
            }
        }

    }

    private void drawGraphLines(Canvas canvas) {
        graphPath.reset();

        // draw lines
        for (int i = 0; i < months.length; ++i) {
            if (i == 0)
                graphPath.moveTo(circleCentresX[i], valuesRealHeight[i]);
            else
                graphPath.lineTo(circleCentresX[i], valuesRealHeight[i]);
        }

        canvas.drawPath(graphPath, mGraphPaint);

        for (int i = 0; i < months.length; ++i) {
            canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                    bigCircleRatio * canvas.getHeight(), mBigCirclePaint);
            canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                    smallCircleRatio * canvas.getHeight(), mSmallCirclePaint);
        }
    }

    private void displayError(Canvas canvas) {
        float errWidth = mErrTextPaint.measureText(getContext().getString(R.string.graphError));

        int xPos = (int) (canvas.getWidth() - errWidth) / 2;
        int yPos = (int) ((canvas.getHeight() / 2)
                - ((mErrTextPaint.descent() + mErrTextPaint.ascent()) / 2));

        canvas.drawRect(mErrRectF, mErrRectPaint);
        canvas.drawText(getContext().getString(R.string.graphError), xPos, yPos, mErrTextPaint);
    }

    public int getColor() {
        return mGraphLineColor;
    }

    public void setColor(int mColor) {
        this.mGraphLineColor = mColor;

        reinit();
        invalidate();
        requestLayout();
    }

    public String[] getMonths() {
        return months;
    }

    public void setMonths(String[] months) {
        this.months = months;

        invalidate();
        requestLayout();
    }

    public Double[] getValues() {
        return values;
    }

    public void setValues(Double[] values) {
        this.values = values;

        invalidate();
        requestLayout();
    }

    public int getmDesiredWidth() {
        return mDesiredWidth;
    }

    public void setmDesiredWidth(int mDesiredWidth) {
        this.mDesiredWidth = mDesiredWidth;

        invalidate();
        requestLayout();
    }


    public double getGoal() {
        return mGoal;
    }

    public void setGoal(double mGoal) {
        this.mGoal = mGoal;

        invalidate();
        requestLayout();
    }

    public int getmBackColor1() {
        return mBackColor1;
    }

    public void setmBackColor1(int mBackColor1) {
        this.mBackColor1 = mBackColor1;

        reinit();
        invalidate();
        requestLayout();
    }

    public int getmBackColor2() {
        return mBackColor2;
    }

    public void setmBackColor2(int mBackColor2) {
        this.mBackColor2 = mBackColor2;

        reinit();
        invalidate();
        requestLayout();
    }

    public int getmBackLineColor() {
        return mBackLineColor;
    }

    public void setmBackLineColor(int mBackLineColor) {
        this.mBackLineColor = mBackLineColor;

        reinit();
        invalidate();
        requestLayout();
    }

    public int getmGraphLineColor() {
        return mGraphLineColor;
    }

    public void setmGraphLineColor(int mGraphLineColor) {
        this.mGraphLineColor = mGraphLineColor;

        reinit();
        invalidate();
        requestLayout();
    }

    public int getmTextColor() {
        return mTextColor;
    }

    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;

        reinit();
        invalidate();
        requestLayout();
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return 320;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return 240;
    }
}
