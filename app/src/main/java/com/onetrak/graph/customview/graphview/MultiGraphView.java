package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by aleksey.ivanov on 01.04.2016.
 */
public class MultiGraphView extends BaseGraphView {
    // Animation
    long startTime;
    long animationDuration;
    long curTime;
    public static long microDuration;

    // Public data
    int valuesPerStripe;
    double[][] values;
    int[] colors;

    // Paints
    Paint[] graphsPaints;
    Paint linePaint;
    Paint smallCirclePaint;
    TextPaint mBlackPaint;
    Paint[] bigCirclePaint;

    // Current state
    int microId;
    int stripeId;

    // Layout arrays
    float[][] convertedX;
    float[][] convertedY;
    float[][] convertedYDraw;
    StaticLayout highlightedText;

    // Paths
    Path[] graphPaths;

    // Parent
    ArrowedHorizontalScrollView hsv;

    // Constants
    public static float microInterval;
    public static final float bigCircleRatio = 0.025f;
    public static final float smallCircleRatio = 0.025f / 2;
    public static long segmentDuration = 250;
    public static int framesPerSecond = 60;

    // Event handling
    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime = 0;


    public MultiGraphView(Context context, AttributeSet attrs) {

        super(context, attrs);

        restore();
        footerRatio = 0.07f;
        textBorder = 0.7f;
    }

    private void restore() {
        microId = -1;
        stripeId = -1;
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (months != null && values != null && colors != null) {

            microInterval = stripeWidth / (valuesPerStripe * 2);
            microDuration = segmentDuration / valuesPerStripe;
            animationDuration = segmentDuration * months.length;
            calcXAndYValues();
            calculateDrawValues();

            initPaintsAndPaths();
            calculateLinesHeights(h);
        }
    }

    private void initPaintsAndPaths() {
        graphsPaints = new Paint[colors.length];
        graphPaths = new Path[colors.length];

        for (int i = 0; i < graphsPaints.length; ++i) {
            graphsPaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            graphsPaints[i].setColor(colors[i]);
            graphsPaints[i].setStrokeWidth(graphStrokeWidth / 1.5f);
            graphsPaints[i].setStyle(Paint.Style.STROKE);
            graphsPaints[i].setStrokeJoin(Paint.Join.ROUND);
            graphsPaints[i].setStrokeCap(Paint.Cap.ROUND);
            graphsPaints[i].setPathEffect(new CornerPathEffect(graphStrokeWidth * 2));

            graphPaths[i] = new Path();
        }

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(mBackLineColor);
        linePaint.setStrokeWidth(graphStrokeWidth / 1.5f);

        smallCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        smallCirclePaint.setColor(Color.WHITE);

        bigCirclePaint = new Paint[colors.length];
        for (int i = 0; i < bigCirclePaint.length; ++i) {
            bigCirclePaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            bigCirclePaint[i].setColor(colors[i]);
        }

        mBlackPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mBlackPaint.setColor(Color.BLACK);
        mBlackPaint.setTextSize(mTextSize);
    }

    protected void findMinAndMax() {
        if (values != null) {
            // Precalculate data for lines
            double localMax = -1;
            double localMin = -1;
            for (int i = 0; i < values.length; ++i)
                for (int k = 0; k < values[i].length; ++k) {
                    if (values[i][k] > localMax || localMax == -1)
                        localMax = values[i][k];
                    if (values[i][k] < localMin || localMin == -1)
                        localMin = values[i][k];
                }

            linesMax = localMax;

            // If we need to fill zeros, we will recount minimum
            linesMin = mFillNa ? countMinFNa(values, linesMax) : localMin;
        }
    }

    private void calcXAndYValues() {
        convertedX = new float[values.length][];
        convertedY = new float[values.length][];

        for (int i = 0; i < values.length; ++i) {
            convertedX[i] = new float[valuesPerStripe * months.length];
            convertedY[i] = new float[valuesPerStripe * months.length];

            for (int k = 0; k < valuesPerStripe * months.length; ++k) {
                convertedX[i][k] = leftStripe + (k / valuesPerStripe) * stripeWidth
                        + microInterval + 2 * microInterval * (k % valuesPerStripe);
                convertedY[i][k] = convertValuetoHeight(values[i][k], h);
            }
        }
    }

    private void calculateDrawValues() {
        if (!mFillNa) {
            convertedYDraw = convertedY;
        } else {
            convertedYDraw = new float[values.length][];

            for (int i = 0; i < values.length; ++i) {
                convertedYDraw[i] = new float[valuesPerStripe * months.length];

                for (int k = 0; k < values[i].length; ++k) {
                    if (values[i][k] == 0) {
                        int cur_k = k;
                        if (k == 0) {
                            // find next given value
                            while (cur_k < values[i].length && values[i][cur_k] == 0)
                                ++cur_k;

                            convertedYDraw[i][k] = convertedY[i][cur_k];
                        } else if (k == values[i].length - 1) {
                            // find prev given value
                            while (cur_k > 0 && values[i][cur_k] == 0)
                                --cur_k;

                            convertedYDraw[i][k] = convertedY[i][cur_k];
                        } else {
                            // find next given value

                            while (cur_k < values[i].length && values[i][cur_k] == 0)
                                ++cur_k;

                            if (cur_k == values[i].length) {
                                convertedYDraw[i][k] = convertedYDraw[i][k - 1];
                            } else {
                                convertedYDraw[i][k] = convertedYDraw[i][k - 1]
                                        + ((convertedY[i][cur_k] - convertedYDraw[i][k - 1])
                                        / (cur_k - k + 1));

                            }
                        }
                    } else {
                        convertedYDraw[i][k] = convertedY[i][k];
                    }
                }

            }
        }

        if (stripeId != -1) {
            highlightedText = new StaticLayout(months[stripeId], mBlackPaint,
                    (int) (textRatio * stripeWidth),
                    Layout.Alignment.ALIGN_NORMAL, 1, 1, true);
        }
    }


    protected double countMinFNa(double[][] valuesAndGoal, double max) {
        double min = max;
        for (int i = 0; i < valuesAndGoal.length; ++i)
            for (int k = 0; k < valuesAndGoal[i].length; ++k)
                if (valuesAndGoal[i][k] != 0 && valuesAndGoal[i][k] < min)
                    min = valuesAndGoal[i][k];

        return min;
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
                            microId = (int) ((x - leftStripe) / (2 * microInterval));
                            stripeId = (int) ((x - leftStripe) / stripeWidth);

                            if (months != null && stripeId >= months.length)
                                stripeId = months.length - 1;

                            if (months != null && microId >= valuesPerStripe * months.length)
                                microId = valuesPerStripe * months.length - 1;
                        }
                    }
                    invalidate();
                    requestLayout();
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (months != null && values != null && colors != null) {
            hsv = (ArrowedHorizontalScrollView) getParent();
            // Measure animation time
            curTime = System.currentTimeMillis() - startTime;

            if (microId != -1) {
                drawVertLine(canvas);
            }

            drawHorizontalLines(canvas);
            drawHorizontalText(canvas, 0);
            drawGraph(canvas);

            if (microId != -1) {
                drawCircles(canvas);
                drawHiglightedText(canvas);
            }


            if (curTime < animationDuration) {
                postInvalidateDelayed(1000 / framesPerSecond);
            }

            hsv.setAnimationFinished(!(curTime < animationDuration));
            invalidate();

        }
    }

    private void drawGraph(Canvas canvas) {
        for (int i = 0; i < values.length; ++i)
            graphPaths[i].reset();

        boolean scrolled = false;
        for (int i = 0; i < convertedX.length; ++i) {
            for (int k = 0; k < convertedX[i].length; ++k) {
                if (k == 0)
                    graphPaths[i].moveTo(convertedX[i][k], convertedYDraw[i][k]);
                else {
                    if (curTime / microDuration > k - 1)
                        graphPaths[i].lineTo(convertedX[i][k], convertedYDraw[i][k]);
                    else if (curTime / microDuration == k - 1) {
                        float curX = convertedX[i][k - 1] + (convertedX[i][k] - convertedX[i][k - 1])
                                * ((float) (curTime - (k - 1) * microDuration) / microDuration);
                        float curY = convertedYDraw[i][k - 1] + (convertedYDraw[i][k] - convertedYDraw[i][k - 1])
                                * ((float) (curTime - (k - 1) * microDuration) / microDuration);

                        graphPaths[i].lineTo(curX, curY);

                        if (!scrolled) {
                            hsv.scrollTo((int) (curX - hsv.getWidth() / 2), 0);
                            scrolled = true;
                        }
                        break;
                    }
                }
            }
            canvas.drawPath(graphPaths[i], graphsPaints[i]);
        }
    }

    private void drawVertLine(Canvas canvas) {
        float xPos = leftStripe + microInterval + 2 * microInterval * microId;
        canvas.drawLine(xPos, topIndent, xPos, canvas.getHeight() - belowIndent, linePaint);
    }

    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < values.length; ++i) {
            canvas.drawCircle(convertedX[i][microId], convertedYDraw[i][microId], bigCircleRatio * h,
                    bigCirclePaint[i]);
            canvas.drawCircle(convertedX[i][microId], convertedYDraw[i][microId], smallCircleRatio * h,
                    smallCirclePaint);
        }
    }

    private void drawHiglightedText(Canvas canvas) {
        canvas.translate(labelsUnderX[stripeId], labelsUnderY[stripeId]);
        highlightedText.draw(canvas);
        canvas.translate(-labelsUnderX[stripeId], -labelsUnderY[stripeId]);
    }


    public int getValuesPerStripe() {
        return valuesPerStripe;
    }

    public void setValuesPerStripe(int valuesPerStripe) {
        this.valuesPerStripe = valuesPerStripe;

        reinit();
    }

    public double[][] getValues() {
        return values;
    }

    public void setValues(double[][] values) {
        if (values[0].length != valuesPerStripe * months.length) {
            throw new IllegalArgumentException("The number of values should be " +
                    "multiplication of valuesPerStripe and the number of given months");
        }
        this.values = values;

        reinit();
    }

    public int[] getColors() {
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;

        reinit();
    }


    private void reinit() {
        restore();
        invalidate();
        requestLayout();
    }
}
