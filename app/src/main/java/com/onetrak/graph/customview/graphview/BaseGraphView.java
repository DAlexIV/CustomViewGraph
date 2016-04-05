package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.onetrak.graph.customview.R;

/**
 * Created by aleksey.ivanov on 01.04.2016.
 */
public abstract class BaseGraphView extends View {
    // Public data
    int mBackColor1;
    int mBackColor2;
    int mTextColor;
    int mBackLineColor;
    int mDesiredWidth;
    boolean mFillNa;
    String[] months;

    // Rects
    RectF mErrRectF;
    RectF mStripeRectF;

    // Paints
    Paint mErrRectPaint;
    TextPaint mErrTextPaint;
    Paint mStripePaint;
    TextPaint mTextPaint;
    Paint mLinePaint;


    // Layout data
    int w, h;
    float topIndent;
    float belowIndent;
    float stripeWidth;
    int mTextSize;
    float graphStrokeWidth;
    double linesMin;
    double linesMax;

    // Layout arrays
    float[] monthsMeasured;
    float[] labelsUnderX;
    float[] labelsUnderY;
    StaticLayout[] textUnderStripes;


    // Constants
    public static float leftStripe;
    public static final float headerRatio = 0.05f;
    public static final float footerRatio = 0.1f;
    public static final float borderRatio = 0.1f;
    public static final double graphRatio = (float) 1 - headerRatio - footerRatio - 2 * borderRatio;
    public static final int minStripeDp = 50;
    public static final float textRatio = 0.62f;
    public final String testText = "70 " + getContext().getString(R.string.localMeasurementSystem);

    // String constants
    public static String graphErrorText;


    public BaseGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UnoGraphView,
                0, 0
        );
        mBackColor1 = a.getInteger(R.styleable.UnoGraphView_back_color1, Color.parseColor("#f0f1f2"));
        mBackColor2 = a.getInteger(R.styleable.UnoGraphView_back_color2, Color.parseColor("#e7e9eb"));
        mBackLineColor = a.getInteger(R.styleable.UnoGraphView_back_line_color, Color.parseColor("#cdd1d6"));
        mTextColor = a.getInteger(R.styleable.UnoGraphView_text_color, Color.parseColor("#2a2a2a"));
        mDesiredWidth = a.getInteger(R.styleable.UnoGraphView_real_width, 0);
        mFillNa = a.getBoolean(R.styleable.UnoGraphView_fill_na, false);

        a.recycle();
        init();
    }

    protected void init() {
        initPaints();

        mErrRectF = new RectF();
        mStripeRectF = new RectF();

    }

    private void initPaints() {
        mErrRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mErrRectPaint.setColor(Color.RED);

        mErrTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mErrTextPaint.setTextSize(30);
        mErrTextPaint.setColor(Color.BLACK);

        mStripePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(mBackLineColor);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Resolving height
        int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        h = resolveSizeAndState(minh, heightMeasureSpec, 1);

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

        if (months == null)
        // If data or months are not provided
        {
            mErrRectF.set(0, 0, w, h);
        } else {
            // Calculating indents
            topIndent = (int) (h * headerRatio);
            belowIndent = (int) (h * footerRatio);

            // Counting stripeWidth and indents
            stripeWidth = w / (months.length + 1);

            findMinAndMax();


            // If stripe is too narrow, then
            // we will increase width of graph to required minimum
            if (stripeWidth < HelperLayoutClass.dpToPixels(getResources(), minStripeDp)) {
                stripeWidth = (int) HelperLayoutClass.dpToPixels(getResources(), minStripeDp);
                w = (int) (stripeWidth * (months.length + 1));
            }

            // Calculating textSize for labels under stripes months
            HelperLayoutClass.calculateOKTextSize(mTextPaint, textRatio * stripeWidth, months,
                    belowIndent / 2);
            mTextSize = (int) mTextPaint.getTextSize();
            leftStripe = mTextPaint.measureText(testText) + mTextSize / 2;
            stripeWidth = (w - leftStripe) / months.length;
            graphStrokeWidth = h / 100;
            mLinePaint.setStrokeWidth(graphStrokeWidth / 4);

            // Precalc textSizes
            monthsMeasured = new float[months.length];
            for (int i = 0; i < months.length; ++i) {
                monthsMeasured[i] = mTextPaint.measureText(months[i]);
            }

            // Precalculating data for text
            labelsUnderX = new float[months.length];
            labelsUnderY = new float[months.length];
            for (int i = 0; i < months.length; ++i) {
                labelsUnderX[i] = leftStripe + stripeWidth * i
                        + 0.5f * (stripeWidth - mTextPaint.measureText(months[i]));
                labelsUnderY[i] = h - belowIndent;
            }

            // Calculating static layouts
            textUnderStripes = new StaticLayout[months.length];
            for (int i = 0; i < months.length; ++i)
                textUnderStripes[i] = new StaticLayout(months[i], mTextPaint,
                        (int) (textRatio * stripeWidth),
                        Layout.Alignment.ALIGN_NORMAL, 1, 1, true);

        }
        setMeasuredDimension(w, h);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (months == null) {
            displayError(canvas);
        } else {
            drawBackground(canvas);
        }
    }

    protected void drawBackground(Canvas canvas) {
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
    }

    protected void drawRectsTopAndBelow(Canvas canvas) {
        mStripeRectF.set(0, 0, canvas.getWidth(), topIndent);
        mStripePaint.setColor(mBackColor2);
        canvas.drawRect(mStripeRectF, mStripePaint);

        mStripeRectF.set(0, canvas.getHeight() - belowIndent, canvas.getWidth(), canvas.getHeight());
        mStripePaint.setColor(mBackColor2);
        canvas.drawRect(mStripeRectF, mStripePaint);
    }

    protected void displayError(Canvas canvas) {
        float errWidth = mErrTextPaint.measureText(graphErrorText);

        int xPos = (int) (canvas.getWidth() - errWidth) / 2;
        int yPos = (int) ((canvas.getHeight() / 2)
                - ((mErrTextPaint.descent() + mErrTextPaint.ascent()) / 2));

        canvas.drawRect(mErrRectF, mErrRectPaint);
        canvas.drawText(graphErrorText, xPos, yPos, mErrTextPaint);
    }

    private void drawBorderLines(Canvas canvas) {
        canvas.drawLine(0, topIndent, canvas.getWidth(), topIndent, mLinePaint);
        canvas.drawLine(0, canvas.getHeight() - belowIndent, canvas.getWidth(),
                canvas.getHeight() - belowIndent, mLinePaint);
    }

    protected void drawTextLabelsUnderStripes(Canvas canvas) {
        for (int i = 0; i < months.length; ++i) {
            canvas.translate(labelsUnderX[i], labelsUnderY[i]);
            textUnderStripes[i].draw(canvas);
            canvas.translate(-labelsUnderX[i], -labelsUnderY[i]);
        }
    }


    public float convertValuetoHeight(Double value, float canvasHeight) {
        float indentValue = (headerRatio + borderRatio) * canvasHeight;
        float scaledValue = (float) ((linesMax - value) / (linesMax - linesMin) * graphRatio * canvasHeight);
        return indentValue + scaledValue;
    }



    // TODO move this to appropriate classes




    protected void initStrings() {
        graphErrorText = getContext().getString(R.string.graphError);
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

    public int getmTextColor() {
        return mTextColor;
    }

    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;

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

    public String[] getMonths() {
        return months;
    }

    public void setMonths(String[] months) {
        this.months = months;

        init();
        invalidate();
        requestLayout();
    }

    protected abstract void findMinAndMax();
}
