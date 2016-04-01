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

        final float testTextSize = 100;
        paint.setTextSize(testTextSize);
        final float testWidth = paint.measureText(text);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / testWidth;

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
        return desiredTextSize;
    }

    public static void calculateOKTextSize(Paint paint, float stripeWidth, String[] text, float maxAllowedTextSize) {
        float minTextSize = 1000;
        for (int i = 0; i < text.length; ++i) {
            float currSize = setTextSizeForWidth(paint, stripeWidth, text[i]);
            if (currSize < minTextSize)
                minTextSize = currSize;
        }

        if (minTextSize > maxAllowedTextSize)
            paint.setTextSize(maxAllowedTextSize);
        else
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


}
