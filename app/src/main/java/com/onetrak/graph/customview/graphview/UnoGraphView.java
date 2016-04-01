package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
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
    double mGoal;

    // Rects
    RectF mHighRectF;

    // Paints
    Paint mGoalPaint;
    TextPaint mGoalTextPaint;
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

    float[] intervals;

    // Layout sizes

    float goalStart;
    float goalEnd;
    double linesMin;
    double linesMax;
    double firstLineHeight;

    Point[] lowerTrianglePoints;
    Point[] upperTrianglePoints;
    int stripeId;

    // Layout arrays
    float[] valuesRealHeight;
    float[] circleCentresX;
    long[] timeAnim;


    float[] originalX;
    float[] originalY;
    float[] horizontalLinesH;
    StaticLayout[] weightsTextLayout;
    StaticLayout goalUnderStripes;

    // Constants
    public static final float viewRatio = (float) 9 / 16;

    public static final float bigCircleRatio = 0.025f;
    public static final float smallCircleRatio = 0.0125f;
    public static final float stripLength = 5f;
    public static final int preferredNumLines = 5;
    public static int graphStep = 10;
    public static int framesPerSecond = 60;
    public static long segmentDuration = 250;


    // Event handling
    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime = 0;

    // Parent
    ArrowedHorizontalScrollView hsv;

    // Strings from context
    String localMeasurementSystem;
    String goalLineText;
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
        localMeasurementSystem = getContext().getString(R.string.localMeasurementSystem);
        goalLineText = getContext().getString(R.string.goalLineText);
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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Changing width of lines with corrections after measurement
        if (months != null && values != null) {

            mGoalPaint.setStrokeWidth(graphStrokeWidth / 2);

            mGraphPaint.setStrokeWidth(graphStrokeWidth);
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


        findMinAndMax();


        if (stripeId != -1) {
            goalUnderStripes = new StaticLayout(months[stripeId], mGoalTextPaint,
                    (int) (textRatio * stripeWidth),
                    Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
        }

        calculateLinesHeights(h);

    }

    private void findMinAndMax() {
        // Precalculate data for lines
        double localMax = 0;
        double localMin = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] > localMax)
                localMax = values[i];
            if (localMin < values[i])
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

    private void calculateLinesHeights(int h) {
        graphStep = (int) (linesMax - linesMin) / preferredNumLines;
        firstLineHeight = (int) (linesMin + linesMin % graphStep);


        // Sorry for that shitty piece of code, I'm just lazy to make it right
        int actualNumberOfLines = 0;
        for (double curHeight = firstLineHeight; curHeight < linesMax; curHeight += graphStep)
            ++actualNumberOfLines;

        horizontalLinesH = new float[actualNumberOfLines];
        weightsText = new String[actualNumberOfLines];
        weightsTextLayout = new StaticLayout[actualNumberOfLines];


        int i = 0;
        for (double curHeight = firstLineHeight; curHeight < linesMax; curHeight += graphStep, i++) {
            horizontalLinesH[i] = convertValuetoHeight(mGoal, curHeight, h);
            weightsText[i] = Integer.toString((int) curHeight) + " "
                    + localMeasurementSystem;
            weightsTextLayout[i] = new StaticLayout(weightsText[i], mTextPaint,
                    (int) (leftStripe), Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (months != null && values != null) {
            hsv = (ArrowedHorizontalScrollView) getParent();

            drawAdditionalBackground(canvas);

            // Measure animation time
            curTime = System.currentTimeMillis() - startTime;
            drawGraphLines(canvas);

            if (curTime < animationDuration)
                postInvalidateDelayed(1000 / framesPerSecond);
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

                            //init();
                            invalidate();
                            requestLayout();
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
        drawGoalLineAndText(canvas);
        drawHorizontalLinesAndText(canvas);
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
        int i = 0;
        for (double curHeight = firstLineHeight; curHeight < linesMax; curHeight += graphStep, i++) {
            if (horizontalLinesH[i] < goalStart || horizontalLinesH[i] > goalEnd + 5 * mTextSize / 4) {
                canvas.drawLine(0, horizontalLinesH[i], canvas.getWidth(), horizontalLinesH[i], mLinePaint);
                float xPos = mTextSize / 4;
                float yPos = horizontalLinesH[i] - 5 * mTextSize / 4;
                canvas.translate(xPos, yPos);
                weightsTextLayout[i].draw(canvas);
                canvas.translate(-xPos, -yPos);
//                canvas.drawText(weightsText[i], mTextSize / 4,
//                        horizontalLinesH[i] - mTextSize / 4, mTextPaint);
            }
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


    private float convertValuetoHeight(double mGoal, Double value, float canvasHeight) {
        float indentValue = (headerRatio + borderRatio) * canvasHeight;
        float scaledValue = (float) ((linesMax - value) / (linesMax - linesMin) * graphRatio * canvasHeight);
        return indentValue + scaledValue;
    }

    private double countMinFNa(double[] valuesAndGoal, double max, double mGoal) {
        double min = max;
        for (int i = 0; i < valuesAndGoal.length; ++i)
            if (valuesAndGoal[i] != 0 && valuesAndGoal[i] < min)
                min = valuesAndGoal[i];

        if (mGoal < min)
            min = mGoal;
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
