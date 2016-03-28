package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by aleksey.ivanov on 28.03.2016.
 */
public class GraphWrapper extends FrameLayout {
    int deviceWidth;

    public GraphWrapper(Context context) {
        this(context, null, 0);
    }

    public GraphWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphWrapper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point deviceDisplay = new Point();
        display.getSize(deviceDisplay);
        deviceWidth = deviceDisplay.x;
    }
}
