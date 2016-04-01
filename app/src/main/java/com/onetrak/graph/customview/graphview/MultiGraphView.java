package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by aleksey.ivanov on 01.04.2016.
 */
public class MultiGraphView extends BaseGraphView {
    // Public data
    int valuesPerStripe;
    double[][] values;
    int[] colors;
    float[][] convertedX;
    float[][] convertedY;


    // Layout
    double linesMax;
    double linesMin;

    // Paints
    Paint[] graphsPaints;
    public MultiGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        calcXAndYValues();
    }

    private void calcXAndYValues() {
        for (int i = 0; i < values.length; ++i) {
            for (int k = 0; k < values[k].length; ++k) {
                convertedX[i][k] = leftStripe + (k * stripeWidth / values[i].length);
                convertedY[i][k] = conve
            }
        }
    }

    private void findMinAndMax() {
        // Precalculate data for lines
        double localMax = 0;
        double localMin = 0;
        for (int i = 0; i < values.length; ++i)
            for (int k = 0; k < values[i].length; ++k)
            {
            if (values[i][k] > localMax)
                localMax = values[i][k];
            if (localMin < values[i][k])
                localMin = values[i][k];
        }

        linesMax = localMax;

        // If we need to fill zeros, we will recount minimum
        linesMin = mFillNa ? countMinFNa(values, linesMax) : localMin;
    }

    public int getValuesPerStripe() {
        return valuesPerStripe;
    }

    public void setValuesPerStripe(int valuesPerStripe) {
        this.valuesPerStripe = valuesPerStripe;
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
    }

    public int[] getColors() {
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }
}
