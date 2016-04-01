package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import com.onetrak.graph.customview.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by aleksey.ivanov on 21.03.2016.
 */
public class GraphView extends View {
    // Animation
    long startTime;
    long animationDuration;
    long curTime;

    // Public data
    String[] months;
    double[] values;
    int mBackColor1;
    int mBackColor2;
    int mBackLineColor;
    int mGraphLineColor;
    int mTextColor;
    int mDesiredWidth;
    boolean mFillNa;
    double mGoal;

    // Rects
    RectF mErrRectF;
    RectF mStripeRectF;
    RectF mHighRectF;

    // Paints
    Paint mStripePaint;
    Paint mErrRectPaint;
    TextPaint mErrTextPaint;
    Paint mLinePaint;
    Paint mGoalPaint;
    TextPaint mGoalTextPaint;
    TextPaint mTextPaint;
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
    Path mGoalPath;
    Path mUpperTrianglePath;
    Path mLowerTrianglePath;
    Path mHighlightPath;
    Path mLeftArrowPath;

    float[] intervals;

    // Layout sizes
    float stripeWidth;
    float topIndent;
    float belowIndent;
    float graphStrokeWidth;
    float goalStart;
    float goalEnd;
    int mTextSize;
    double linesMin;
    double linesMax;

    Point[] lowerTrianglePoints;
    Point[] upperTrianglePoints;
    int stripeId;

    // Layout arrays
    float[] valuesRealHeight;
    float[] circleCentresX;
    long [] timeAnim;

    float[] labelsUnderX;
    float[] labelsUnderY;
    float[] monthsMeasured;
    float[] originalX;
    float[] originalY;

    // Constants
    public static float leftStripe;
    public static final float viewRatio = (float) 9 / 16;
    public static final float headerRatio = 0.05f;
    public static final float footerRatio = 0.1f;
    public static final int minStripeDp = 50;
    public static final float textRatio = 0.62f;
    public static final float borderRatio = 0.1f;
    public static final float bigCircleRatio = 0.025f;
    public static final float smallCircleRatio = 0.0125f;
    public static final double graphRatio = (float) 1 - headerRatio - footerRatio - 2 * borderRatio;
    public static final float stripLength = 5f;
    public static final int preferredNumLines = 5;
    public final String testText = "70 " + getContext().getString(R.string.localMeasurementSystem);
    public static int graphStep = 10;
    public static int framesPerSecond = 60;
    public static long segmentDuration = 250;


    // Event handling
    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime = 0;

    // Parent
    HorizontalScrollView hsv;

    // Strings from context
    String localMeasurementSystem;
    String graphErrorText;
    String goalLineText;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GraphView,
                0, 0
        );
        mBackColor1 = a.getInteger(R.styleable.GraphView_back_color1, Color.parseColor("#f0f1f2"));
        mBackColor2 = a.getInteger(R.styleable.GraphView_back_color2, Color.parseColor("#e7e9eb"));
        mBackLineColor = a.getInteger(R.styleable.GraphView_back_line_color, Color.parseColor("#cdd1d6"));
        mGraphLineColor = a.getInteger(R.styleable.GraphView_graph_line_color, Color.parseColor("#a58143"));
        mTextColor = a.getInteger(R.styleable.GraphView_text_color, Color.parseColor("#2a2a2a"));
        mDesiredWidth = a.getInteger(R.styleable.GraphView_real_width, 0);
        mFillNa = a.getBoolean(R.styleable.GraphView_fill_na, false);

//        if (!(getParent() instanceof HorizontalScrollView))
//            throw new IllegalArgumentException("You should wrap this class into HorizontalScrollView");
//
        a.recycle();
        init();
    }

    private void init() {
        initPaints();
        initStrings();

        mErrRectF = new RectF();
        mStripeRectF = new RectF();
        mHighRectF = new RectF();

        mGraphPath = new Path();
        mGoalPath = new Path();
        mGradPath = new Path();
        mHighlightPath = new Path();
        mLeftArrowPath = new Path();


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

    private void initStrings() {
        localMeasurementSystem = getContext().getString(R.string.localMeasurementSystem);
        graphErrorText = getContext().getString(R.string.graphError);
        goalLineText = getContext().getString(R.string.goalLineText);
    }

    private void initPaints() {
        mErrRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mErrRectPaint.setColor(Color.RED);

        mErrTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mErrTextPaint.setTextSize(30);
        mErrTextPaint.setColor(Color.BLACK);

        mStripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mBackLineColor);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);

        mGraphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGraphPaint.setColor(mGraphLineColor);
        mGraphPaint.setDither(true);
        mGraphPaint.setStyle(Paint.Style.STROKE);
        mGraphPaint.setStrokeJoin(Paint.Join.ROUND);
        mGraphPaint.setStrokeCap(Paint.Cap.ROUND);
        mGraphPaint.setAntiAlias(true);

        mGradPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


        mGoalPaint = new Paint();
        mGoalPaint.setAntiAlias(false);
        mGoalPaint.setColor(mGraphLineColor);
        mGoalPaint.setStyle(Paint.Style.STROKE);
        intervals = new float[]{stripLength, stripLength};
        mGoalPaint.setPathEffect(new DashPathEffect(intervals, 0));

        mGoalTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mGoalTextPaint.setColor(mGraphLineColor);

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
            stripeWidth = w / (months.length + 1);


            // If stripe is too narrow, then
            // we will increase width of graph to required minimum
            if (stripeWidth < HelperLayoutClass.dpToPixels(getResources(), minStripeDp)) {
                stripeWidth = (int) HelperLayoutClass.dpToPixels(getResources(), minStripeDp);
                w = (int) (stripeWidth * (months.length + 1));
            }

            // Calculating textSize for labels under stripes months
            HelperLayoutClass.calculateOKTextSize(mTextPaint, textRatio * stripeWidth, months);
            mTextSize = (int) mTextPaint.getTextSize();
            leftStripe = mTextPaint.measureText(testText) + mTextSize / 2;
            stripeWidth = (w - leftStripe) / months.length;

            // Changing width of lines with corrections after measurement
            graphStrokeWidth = h / 100;
            mGoalPaint.setStrokeWidth(graphStrokeWidth / 2);
            mLinePaint.setStrokeWidth(graphStrokeWidth / 4);

            mGraphPaint.setStrokeWidth(graphStrokeWidth);
//            mGraphPaint.setPathEffect(new CornerPathEffect(stripeWidth / 10));
            mGradPaint.setStrokeWidth(graphStrokeWidth);

            mGradPaint.setShader(new LinearGradient(0, 0, 0, getHeight(),
                    Color.argb(160, Color.red(mGraphLineColor), Color.green(mGraphLineColor),
                            Color.blue(mGraphLineColor)),
                    Color.argb(8, Color.red(mGraphLineColor), Color.green(mGraphLineColor),
                            Color.blue(mGraphLineColor)), Shader.TileMode.MIRROR));

            // Animation
            animationDuration = segmentDuration * (months.length - 1);

            precalculateLayoutArrays(h);
            calculateTriangles(h);
        }

        setMeasuredDimension(w, h);
    }

    private void calculateTriangles(int h) {
        float lowerTrianglePadding = mTextSize / 2;
        float upperTrianglePadding = mTextSize / 4;
        float lowerTriangleBound = belowIndent / 10;


        if (stripeId != -1 && curTime / segmentDuration >= stripeId) {
            lowerTrianglePoints[0].set((int) (leftStripe + stripeId * stripeWidth + stripeWidth / 2),
                    (int) (labelsUnderY[stripeId] + lowerTrianglePadding));
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
        float[] circleCentresXCount  = new float[months.length];
        long[] tempAnim = new long[months.length];

        int last = 0;

        originalX = new float[months.length];
        originalY = new float[months.length];

        long curAnimDur = 0;
        for (int i = 0; i < values.length; ++i) {
            float valueX = leftStripe + stripeWidth * ((float) i + 0.5f);
            float valueY = convertValuetoHeight(mGoal, values[i], h);

            if (values[i] != 0) {
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

        // Precalc textSizes
        monthsMeasured = new float[values.length];
        for (int i = 0; i < months.length; ++i) {
            monthsMeasured[i] = mTextPaint.measureText(months[i]);
        }

        // Precalculating data for text
        labelsUnderX = new float[values.length];
        labelsUnderY = new float[values.length];
        for (int i = 0; i < months.length; ++i) {
            labelsUnderX[i] = leftStripe + stripeWidth * i
                    + 0.5f * (stripeWidth - mTextPaint.measureText(months[i]));
            labelsUnderY[i] = h - belowIndent + mTextSize;
        }

        // Precalculate data for lines
        double localMax = 0;
        double localMin = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > localMax)
                localMax = values[i];
            if (localMin < values[i])
                localMin = values[i];
        }

        linesMax = localMax;
        linesMin = mFillNa ? countMinFNa(values, linesMax) : localMin;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (months == null || values == null) {
            displayError(canvas);
        } else {
            hsv = (HorizontalScrollView) getParent();

            drawBackground(canvas);

            // Measure animation time
            curTime = System.currentTimeMillis() - startTime;
            drawGraphLines(canvas);
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
                long clickDuration = System.currentTimeMillis() - startClickTime;
                if (clickDuration < MAX_CLICK_DURATION) {
                    int x = (int) event.getX();
                    if (x >= leftStripe) {
                        stripeId = (int) ((x - leftStripe) / stripeWidth);

                        //init();
                        invalidate();
                        requestLayout();
                    }
                }
                invalidate();
                requestLayout();
                return true;
        }
        return false;
    }

    // Remove multiplication ?
    private void drawBackground(Canvas canvas) {

        mStripeRectF.set(0, topIndent, leftStripe, canvas.getHeight() - belowIndent);
        mStripePaint.setColor(mBackColor2);
        canvas.drawRect(mStripeRectF, mStripePaint);

        for (int i = 0; i < months.length; ++i) {
            mStripeRectF.set(leftStripe + stripeWidth * i, topIndent,
                    leftStripe + stripeWidth * (i + 1), canvas.getHeight() - belowIndent);
            int curColorRes = (i % 2 == 0) ? mBackColor1 : mBackColor2;
            mStripePaint.setColor(curColorRes);

            canvas.drawRect(mStripeRectF, mStripePaint);
        }


        drawBorderLines(canvas);
        drawRectsTopAndBelow(canvas);
        drawTextLabelsUnderStripes(canvas);
        drawHighlightedStripe(canvas);
        drawGoalLineAndText(canvas);
        drawHorizontalLinesAndText(canvas);
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

    private void drawTextLabelsUnderStripes(Canvas canvas) {
        for (int i = 0; i < months.length; ++i) {
            if (i == stripeId && curTime / segmentDuration >= stripeId)
                canvas.drawText(months[i], labelsUnderX[i], labelsUnderY[i], mGoalTextPaint);
            else
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
            float value = convertValuetoHeight(mGoal, mGoal, canvas.getHeight());

            // Draw line
            mGoalPath.reset();
            mGoalPath.moveTo(0, value);
            mGoalPath.lineTo(canvas.getWidth(), value);
            canvas.drawPath(mGoalPath, mGoalPaint);

            // Draw text
            mGoalTextPaint.setTextSize(mTextSize);
            canvas.drawText(goalLineText,
                    mTextSize / 2, value - mTextSize / 2, mGoalTextPaint);

            // Count constraints
            goalStart = value - 3 * mTextSize / 2;
            goalEnd = value;
        }
    }

    private void drawHorizontalLinesAndText(Canvas canvas) {
        graphStep = (int) (linesMax - linesMin) / preferredNumLines;
        double firstLineHeight = (int) (linesMin + linesMin % graphStep);


        for (double curHeight = firstLineHeight; curHeight < linesMax; curHeight += graphStep) {
            float value = convertValuetoHeight(mGoal, curHeight, canvas.getHeight());

            if (value < goalStart || value > goalEnd + 5 * mTextSize / 4) {
                canvas.drawLine(0, value, canvas.getWidth(), value, mLinePaint);
                canvas.drawText(Integer.toString((int) curHeight) + " "
                                + localMeasurementSystem, mTextSize / 4,
                        value - mTextSize / 4, mTextPaint);
            }
        }

    }

    private void drawGraphLines(final Canvas canvas) {
        drawAnimatedLines(canvas);
        drawAnimatedCircles(canvas);
        drawHighligthedCirclesAndTriangles(canvas);

        if (curTime < animationDuration)
            postInvalidateDelayed(1000 / framesPerSecond);

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


                    hsv.scrollTo((int) (curPosX - hsv.getWidth() / 2), 0);
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
        curTime -= segmentDuration;
        // TODO do not draw is value is null
        for (int i = 0; i < circleCentresX.length; ++i) {
            if (curTime / segmentDuration > i - 1) {
                canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                        bigCircleRatio * canvas.getHeight(), mBigCirclePaint);
                canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                        smallCircleRatio * canvas.getHeight(), mSmallCirclePaint);
            }
            if (curTime / segmentDuration == i - 1) {
                float value = ((float) (curTime % segmentDuration)) / segmentDuration
                        * smallCircleRatio * canvas.getHeight();
                canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                        2 * value, mBigCirclePaint);
                canvas.drawCircle(circleCentresX[i], valuesRealHeight[i],
                        value, mSmallCirclePaint);
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


    private void displayError(Canvas canvas) {
        float errWidth = mErrTextPaint.measureText(graphErrorText);

        int xPos = (int) (canvas.getWidth() - errWidth) / 2;
        int yPos = (int) ((canvas.getHeight() / 2)
                - ((mErrTextPaint.descent() + mErrTextPaint.ascent()) / 2));

        canvas.drawRect(mErrRectF, mErrRectPaint);
        canvas.drawText(graphErrorText, xPos, yPos, mErrTextPaint);
    }

    private float convertValuetoHeight(double mGoal, Double value, float canvasHeight) {
        float indentValue = (headerRatio + borderRatio) * canvasHeight;
        float scaledValue = (float) ((linesMax - value) / (linesMax - linesMin) * graphRatio * canvasHeight);
        return indentValue + scaledValue;
    }

    private double countMinFNa(double[] valuesAndGoal, double max) {
        double min = max;
        for (int i = 0; i < valuesAndGoal.length; ++i)
            if (valuesAndGoal[i] != 0 && valuesAndGoal[i] < min)
                min = valuesAndGoal[i];
        return min;
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

    public String[] getMonths() {
        return months;
    }

    public void setMonths(String[] months) {
        this.months = months;

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


    public double getGoal() {
        return mGoal;
    }

    public void setGoal(double mGoal) {
        this.mGoal = mGoal;

        init();
        invalidate();
        requestLayout();
    }

    public int getmBackColor1() {
        return mBackColor1;
    }

    public void setmBackColor1(int mBackColor1) {
        this.mBackColor1 = mBackColor1;

        init();
        invalidate();
        requestLayout();
    }

    public int getmBackColor2() {
        return mBackColor2;
    }

    public void setmBackColor2(int mBackColor2) {
        this.mBackColor2 = mBackColor2;

        init();
        invalidate();
        requestLayout();
    }

    public int getmBackLineColor() {
        return mBackLineColor;
    }

    public void setmBackLineColor(int mBackLineColor) {
        this.mBackLineColor = mBackLineColor;

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

    public int getmTextColor() {
        return mTextColor;
    }

    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;

        init();
        invalidate();
        requestLayout();
    }


    public boolean ismFillNa() {
        return mFillNa;
    }

    public void setmFillNa(boolean mFillNa) {
        this.mFillNa = mFillNa;

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
