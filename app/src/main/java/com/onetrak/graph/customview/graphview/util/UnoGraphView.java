package com.onetrak.graph.customview.graphview.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.onetrak.graph.customview.R;

/**
 * Created by aleksey.ivanov on 21.03.2016.
 */
public class UnoGraphView extends BaseGraphView {
    // Animation
    long startTime;
    long animationDuration;
    long curTime;

    // Public data
    double[] values;
    int mGraphLineColor;

    // Rects
    RectF mHighRectF;

    // Paints
    Paint mGraphPaint;
    Paint mBigCirclePaint;
    Paint mSmallCirclePaint;
    Paint mTrianglePaint;
    Paint mGradPaint;
    Paint mHighlightStripePaint;
    Paint mHighlightPathPaint;
    Paint mArrowPaint;

    // Paths
    Path mGraphPath;
    Path mGradPath;
    Path mUpperTrianglePath;
    Path mLowerTrianglePath;
    Path mHighlightPath;
    Point[] lowerTrianglePoints;
    Point[] upperTrianglePoints;
    int stripeId;

    // Layout
    float curX;

    // Layout arrays
    float[] valuesRealHeight;
    float[] circleCentresX;
    long[] timeAnim;
    float[] originalX;
    float[] originalY;
    StaticLayout goalUnderStripes;

    // Constants
    public static final float bigCircleRatio = 0.025f;
    public static final float smallCircleRatio = 0.0125f;
    public static final int preferredNumLines = 5;
    public static int graphStep = 10;
    public static int framesPerSecond = 60;
    public static long segmentDuration = 250;




    // Strings from context

    String[] weightsText;

    public UnoGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UnoGraphView,
                0, 0
        );
        mGraphLineColor = a.getInteger(R.styleable.UnoGraphView_graph_line_color, Color.parseColor("#a58143"));

        a.recycle();
        init();
    }

    @Override
    protected void init() {
        super.init();
        initPaints();
        initStrings();


        mHighRectF = new RectF();

        mGraphPath = new Path();
        mGoalPath = new Path();
        mGradPath = new Path();
        mHighlightPath = new Path();


        mUpperTrianglePath = new Path();
        mUpperTrianglePath.setFillType(Path.FillType.EVEN_ODD);

        mLowerTrianglePath = new Path();
        mLowerTrianglePath.setFillType(Path.FillType.EVEN_ODD);

        stripeId = -1;
        lowerTrianglePoints = new Point[3];
        for (int i = 0; i < 3; ++i)
            lowerTrianglePoints[i] = new Point();

        upperTrianglePoints = new Point[3];
        for (int i = 0; i < 3; ++i)
            upperTrianglePoints[i] = new Point();

        startTime = System.currentTimeMillis();
    }

    @Override
    protected void initStrings() {
        super.initStrings();
    }

    private void initPaints() {


        mGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraphPaint.setColor(mGraphLineColor);
        mGraphPaint.setDither(true);
        mGraphPaint.setStyle(Paint.Style.STROKE);
        mGraphPaint.setStrokeJoin(Paint.Join.ROUND);
        mGraphPaint.setStrokeCap(Paint.Cap.ROUND);
        mGraphPaint.setAntiAlias(true);

        mGradPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


        mBigCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBigCirclePaint.setColor(mGraphLineColor);
        mBigCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mSmallCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSmallCirclePaint.setColor(Color.WHITE);
        mSmallCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTrianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrianglePaint.setColor(mGraphLineColor);
        mTrianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mHighlightStripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightStripePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mHighlightPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHighlightPathPaint.setColor(mBackLineColor);
        mHighlightPathPaint.setStyle(Paint.Style.STROKE);
        mHighlightPathPaint.setShadowLayer(10f, 0.0f, 0.0f, Color.BLACK);

        mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPaint.setColor(Color.BLACK);
        mArrowPaint.setStyle(Paint.Style.STROKE);

        mGoalPaint.setColor(mGraphLineColor);
        mGoalTextPaint.setColor(mGraphLineColor);
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Changing width of lines with corrections after measurement
        if (months != null && values != null) {
            mGraphPaint.setStrokeWidth(graphStrokeWidth);
            mGradPaint.setStrokeWidth(graphStrokeWidth);

            mGradPaint.setShader(new LinearGradient(0, 0, 0, h,
                    Color.argb(160, Color.red(mGraphLineColor), Color.green(mGraphLineColor),
                            Color.blue(mGraphLineColor)),
                    Color.argb(8, Color.red(mGraphLineColor), Color.green(mGraphLineColor),
                            Color.blue(mGraphLineColor)), Shader.TileMode.MIRROR));

            // Animation
            animationDuration = segmentDuration * (months.length - 1);

            precalculateLayoutArrays(h);
            calculateTriangles(h);


        }
    }

    private void calculateTriangles(int h) {
        float lowerTrianglePadding = mTextSize / 2;
        float upperTrianglePadding = mTextSize / 4;
        float lowerTriangleBound = belowIndent / 10;


        if (stripeId != -1 && curTime / segmentDuration >= stripeId) {
            lowerTrianglePoints[0].set((int) (leftStripe + stripeId * stripeWidth + stripeWidth / 2),
                    (int) (labelsUnderY[stripeId] + mTextSize + lowerTrianglePadding));
            lowerTrianglePoints[1].set((int) (leftStripe + stripeId * stripeWidth + 3 * stripeWidth / 4),
                    (int) (h - lowerTriangleBound));
            lowerTrianglePoints[2].set((int) (leftStripe + stripeId * stripeWidth + stripeWidth / 4),
                    (int) (h - lowerTriangleBound));

            float lowerTrHeight = lowerTrianglePoints[1].y - lowerTrianglePoints[0].y;

            upperTrianglePoints[0].set((int) (leftStripe + stripeId * stripeWidth + stripeWidth / 2),
                    (int) (topIndent + upperTrianglePadding + lowerTrHeight));
            upperTrianglePoints[1].set((int) (leftStripe + stripeId * stripeWidth + 3 * stripeWidth / 4),
                    (int) (topIndent + upperTrianglePadding));
            upperTrianglePoints[2].set((int) (leftStripe + stripeId * stripeWidth + stripeWidth / 4),
                    (int) (topIndent + upperTrianglePadding));
        }
    }

    private void precalculateLayoutArrays(int h) {
        // Precalculating data for circles
        // TODO refactoring needed
        float[] valuesRealHeightCount = new float[months.length];
        float[] circleCentresXCount = new float[months.length];
        long[] tempAnim = new long[months.length];

        int last = 0;

        originalX = new float[months.length];
        originalY = new float[months.length];

        long curAnimDur = 0;
        for (int i = 0; i < values.length; ++i) {
            float valueX = leftStripe + stripeWidth * ((float) i + 0.5f);
            float valueY = convertValuetoHeight(values[i], h);

            if ((mFillNa && values[i] != 0) || !mFillNa)  {
                tempAnim[last] = curAnimDur;
                circleCentresXCount[last] = valueX;
                valuesRealHeightCount[last] = valueY;
                ++last;
            }

            curAnimDur += segmentDuration;

            originalX[i] = valueX;
            originalY[i] = valueY;
        }

        valuesRealHeight = new float[last];
        circleCentresX = new float[last];
        timeAnim = new long[last];

        for (int i = 0; i < last; ++i) {
            valuesRealHeight[i] = valuesRealHeightCount[i];
            circleCentresX[i] = circleCentresXCount[i];
            timeAnim[i] = tempAnim[i];
        }


        if (stripeId != -1) {
            goalUnderStripes = new StaticLayout(months[stripeId], mGoalTextPaint,
                    (int) (textRatio * stripeWidth),
                    Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
        }

        calculateLinesHeights(h);

    }

    protected void findMinAndMax() {
        // Precalculate data for lines
        double localMax = -1;
        double localMin = -1;
        for (int i = 0; i < values.length; ++i) {
            if (localMax == -1 || values[i] > localMax)
                localMax = values[i];
            if (localMin == -1 || localMin> values[i])
                localMin = values[i];
        }

        if (localMax < mGoal)
            localMax = mGoal;
        if (localMin > mGoal)
            localMin = mGoal;

        linesMax = localMax;

        // If we need to fill zeros, we will recount minimum
        linesMin = mFillNa ? countMinFNa(values, linesMax, mGoal) : localMin;
    }


    protected double countMinFNa(double[] valuesAndGoal, double max, double mGoal) {
        double min = max;
        for (int i = 0; i < valuesAndGoal.length; ++i)
            if (valuesAndGoal[i] != 0 && valuesAndGoal[i] < min)
                min = valuesAndGoal[i];

        if (mGoal < min)
            min = mGoal;
        return min;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (months != null && values != null) {
            drawAdditionalBackground(canvas);

            // Measure animation time
            curTime = System.currentTimeMillis() - startTime;
            drawGraphLines(canvas);

            canvas.drawRect(mLeftRect, mRectPaint);
            drawHorizontalText(canvas, hsv.getScrollX());
            drawGoalText(canvas, hsv.getScrollX());
            drawLimitedHorizontalLines(canvas, hsv.getScrollX() + leftStripe);
            drawGoalLineLimited(canvas, hsv.getScrollX() + leftStripe);
            drawArrows(canvas);

            if (curTime < animationDuration) {
                postInvalidateDelayed(1000 / framesPerSecond);
                hsv.scrollTo((int) (curX - hsv.getWidth() / 2), 0);
            }
            hsv.setAnimationFinished(!(curTime < animationDuration));

            invalidate();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startClickTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - startTime > animationDuration) {
                    long clickDuration = System.currentTimeMillis() - startClickTime;
                    if (clickDuration < MAX_CLICK_DURATION) {
                        int x = (int) event.getX();
                        if (x >= leftStripe) {
                            stripeId = (int) ((x - leftStripe) / stripeWidth);

                            if (months != null && stripeId >= months.length)
                                stripeId = months.length - 1;
                        }
                    }
                    invalidate();
                    requestLayout();
                }
                return true;
        }
        return false;
    }

    // Remove multiplication
    protected void drawAdditionalBackground(Canvas canvas) {
        drawHighlightedStripe(canvas);
        drawGoalLine(canvas);
        drawHorizontalLines(canvas);
    }

    @Override
    protected void drawTextLabelsUnderStripes(Canvas canvas) {
        for (int i = 0; i < months.length; ++i) {
            if (i == stripeId && curTime / segmentDuration >= stripeId) {
                canvas.translate(labelsUnderX[stripeId], labelsUnderY[stripeId]);
                goalUnderStripes.draw(canvas);
                canvas.translate(-labelsUnderX[stripeId], -labelsUnderY[stripeId]);
            } else {
                canvas.translate(labelsUnderX[i], labelsUnderY[i]);
                textUnderStripes[i].draw(canvas);
                canvas.translate(-labelsUnderX[i], -labelsUnderY[i]);
            }
        }
    }

    private void drawHighlightedStripe(Canvas canvas) {
        if (stripeId != -1 && curTime / segmentDuration >= stripeId) {
            float xPos1 = leftStripe + stripeWidth * (stripeId - bigCircleRatio);
            float xPos2 = leftStripe + stripeWidth * (stripeId + 1 + bigCircleRatio);

            mHighlightPath.reset();
            mHighlightPath.moveTo(xPos1, topIndent);
            mHighlightPath.lineTo(xPos2, topIndent);
            mHighlightPath.lineTo(xPos2, canvas.getHeight() - belowIndent);
            mHighlightPath.lineTo(xPos1, canvas.getHeight() - belowIndent);
            mHighlightPath.close();

            canvas.drawPath(mHighlightPath, mHighlightPathPaint);

            mHighRectF.set(leftStripe + stripeWidth * stripeId, topIndent,
                    leftStripe + stripeWidth * (stripeId + 1), canvas.getHeight() - belowIndent);
            mHighlightStripePaint.setColor(Color.WHITE);

            canvas.drawRect(mHighRectF, mHighlightStripePaint);

        }
    }

    private void drawGraphLines(final Canvas canvas) {
        drawAnimatedLines(canvas);
        drawAnimatedCircles(canvas);
        drawHighligthedCirclesAndTriangles(canvas);
    }

    private void drawAnimatedLines(Canvas canvas) {
        mGraphPath.reset();
        mGradPath.reset();


        // draw lines
        for (int i = 0; i < circleCentresX.length; ++i) {
            if (i == 0) {
                mGraphPath.moveTo(circleCentresX[i], valuesRealHeight[i]);
                mGradPath.moveTo(circleCentresX[i], valuesRealHeight[i]);
            } else {
                if (curTime > timeAnim[i]) {
                    mGraphPath.lineTo(circleCentresX[i], valuesRealHeight[i]);
                    mGradPath.lineTo(circleCentresX[i], valuesRealHeight[i]);

                    if (i == circleCentresX.length - 1)
                        mGradPath.lineTo(circleCentresX[i], canvas.getHeight() - belowIndent);
                } else if (curTime > timeAnim[i - 1]) {
                    long localSegDur = timeAnim[i] - timeAnim[i - 1];
                    float curPosX = circleCentresX[i - 1] + (circleCentresX[i] - circleCentresX[i - 1])
                            * ((float) (curTime - timeAnim[i - 1]) / localSegDur);
                    float curPosY = valuesRealHeight[i - 1] + (valuesRealHeight[i] - valuesRealHeight[i - 1])
                            * ((float) (curTime - timeAnim[i - 1]) / localSegDur);

                    mGradPath.lineTo(curPosX, curPosY);
                    mGradPath.lineTo(curPosX, canvas.getHeight() - belowIndent);
                    mGraphPath.lineTo(curPosX, curPosY);

                    curX = curPosX;

                    break;
                }
            }

        }

        mGradPath.lineTo(circleCentresX[0], canvas.getHeight() - belowIndent);
        mGradPath.close();

        canvas.drawPath(mGradPath, mGradPaint);
        canvas.drawPath(mGraphPath, mGraphPaint);
    }

    private void drawAnimatedCircles(Canvas canvas) {
        // TODO do not draw is value is null

        // draw fist one
        canvas.drawCircle(circleCentresX[0], valuesRealHeight[0],
                bigCircleRatio * canvas.getHeight(), mBigCirclePaint);
        canvas.drawCircle(circleCentresX[0], valuesRealHeight[0],
                smallCircleRatio * canvas.getHeight(), mSmallCirclePaint);

        curTime -= segmentDuration;
        for (int i = 1; i < circleCentresX.length; ++i) {
            if (curTime > timeAnim[i]) {
                canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                        bigCircleRatio * canvas.getHeight(), mBigCirclePaint);
                canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                        smallCircleRatio * canvas.getHeight(), mSmallCirclePaint);
            } else if (curTime > timeAnim[i - 1]) {
                long localSegDur = timeAnim[i] - timeAnim[i - 1];
                float value = ((float) (curTime - timeAnim[i - 1]) / localSegDur)
                        * smallCircleRatio * canvas.getHeight();
                canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                        2 * value, mBigCirclePaint);
                canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                        value, mSmallCirclePaint);
                break;
            }
        }
    }

    private void drawHighligthedCirclesAndTriangles(Canvas canvas) {
        if (stripeId != -1 && curTime / segmentDuration >= stripeId) {
            // Big circles
            canvas.drawCircle(originalX[stripeId], originalY[stripeId],
                    2 * bigCircleRatio * canvas.getHeight(), mBigCirclePaint);
            canvas.drawCircle(originalX[stripeId], originalY[stripeId],
                    2 * smallCircleRatio * canvas.getHeight(), mSmallCirclePaint);

            buildAndDrawTriangle(canvas, lowerTrianglePoints, mLowerTrianglePath, mTrianglePaint);
            buildAndDrawTriangle(canvas, upperTrianglePoints, mUpperTrianglePath, mTrianglePaint);

        }
    }

    private void buildAndDrawTriangle(Canvas canvas, Point[] trianglePoints, Path trianglePath, Paint trianglePaint) {
        trianglePath.reset();

        trianglePath.moveTo(trianglePoints[1].x, trianglePoints[1].y);
        trianglePath.lineTo(trianglePoints[0].x, trianglePoints[0].y);
        trianglePath.lineTo(trianglePoints[2].x, trianglePoints[2].y);
        trianglePath.close();
        canvas.drawPath(trianglePath, trianglePaint);

    }


    public int getColor() {
        return mGraphLineColor;
    }

    public void setColor(int mColor) {
        this.mGraphLineColor = mColor;

        init();
        invalidate();
        requestLayout();
    }


    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;

        init();
        invalidate();
        requestLayout();
    }

    public int getmDesiredWidth() {
        return mDesiredWidth;
    }

    public void setmDesiredWidth(int mDesiredWidth) {
        this.mDesiredWidth = mDesiredWidth;

        init();
        invalidate();
        requestLayout();
    }


    public int getmGraphLineColor() {
        return mGraphLineColor;
    }

    public void setmGraphLineColor(int mGraphLineColor) {
        this.mGraphLineColor = mGraphLineColor;

        init();
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