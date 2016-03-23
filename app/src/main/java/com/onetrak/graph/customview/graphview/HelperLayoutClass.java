package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by aleksey.ivanov on 21.03.2016.
 */
public class HelperLayoutClass {
    public static float dpToPixels(Resources r, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public static float pixelsToDp(Context ctx, float px) {
        WindowManager wm = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        float logicalDensity = metrics.density;
        return (float) Math.ceil(px * logicalDensity);
    }

    public static float setTextSizeForWidth(Paint paint, float desiredWidth,
                                            String text) {

        final float testWidth = paint.measureText(text);
        final float testTextSize = paint.getTextSize();

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / testWidth;

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
        return desiredTextSize;
    }

    public static void calculateOKTextSize(Paint paint, float stripeWidth, String[] text) {
        float minTextSize = 1000;
        for (int i = 0; i < text.length; ++i) {
            float currSize = HelperLayoutClass.setTextSizeForWidth(paint, stripeWidth, text[i]);
            if (currSize < minTextSize)
                minTextSize = currSize;
        }
        paint.setTextSize(minTextSize);
    }

    public static int getScreenWidth(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        return metrics.widthPixels;
    }

    static float convertValuetoHeight(double mGoal, Double value, Double[] array, float canvasHeigth) {
        List<Double> valuesAndGoal = new ArrayList<>(Arrays.asList(array));

        if (mGoal != 0)
            valuesAndGoal.add(mGoal);

        double min = Collections.min(valuesAndGoal);
        double max = Collections.max(valuesAndGoal);

        float indentValue = (GraphView.headerRatio + GraphView.borderRatio) * canvasHeigth;
        float scaledValue = (float) ((max - value) / (max - min) * GraphView.graphRatio * canvasHeigth);
        return indentValue + scaledValue;
    }
}
