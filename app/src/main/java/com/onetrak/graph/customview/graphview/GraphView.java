package com.onetrak.graph.customview.graphview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.onetrak.graph.customview.graphview.data.MultiGraphData;
import com.onetrak.graph.customview.graphview.data.UnoGraphData;
import com.onetrak.graph.customview.graphview.util.BaseGraphView;
import com.onetrak.graph.customview.graphview.util.MultiGraphView;
import com.onetrak.graph.customview.graphview.util.UnoGraphView;

/**
 * Created by aleksey.ivanov on 08.04.2016.
 */
public class GraphView extends View {
    HorizontalScrollView hsv;
    BaseGraphView curGraph;

    Context context;
    AttributeSet attrs;
    boolean fillNa;
    int desiredWidth;


    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.attrs = attrs;

        curGraph = new BaseGraphView(context, attrs) {
            @Override
            protected void findMinAndMax() {
                // just nothing
            }
        };


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (hsv == null)
            hsv = (HorizontalScrollView) getParent();
        hsv.removeAllViews();
        hsv.addView(curGraph);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        curGraph.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        curGraph.draw(canvas);
    }

    public void setupUnoGraph(UnoGraphData data) {
        fillNa = curGraph.ismFillNa();
        desiredWidth = curGraph.getmDesiredWidth();

        curGraph = new UnoGraphView(context, attrs);
        curGraph.setMonths(data.getMonths());
        curGraph.setGoal(data.getGoal());
        curGraph.setMonths(data.getMonths());
        curGraph.setmFillNa(fillNa);
        curGraph.setmDesiredWidth(desiredWidth);
        ((UnoGraphView) curGraph).setValues(data.getValues());

        if (hsv == null)
            hsv = (HorizontalScrollView) getParent();

        hsv.removeAllViews();
        hsv.addView(curGraph);

        curGraph.invalidate();
        curGraph.requestLayout();
    }

    public void setupMultiGraph(MultiGraphData data) {
        fillNa = curGraph.ismFillNa();
        desiredWidth = curGraph.getmDesiredWidth();

        curGraph = new MultiGraphView(context, attrs);
        curGraph.setMonths(data.getMonths());
        curGraph.setGoal(data.getGoal());
        curGraph.setmFillNa(fillNa);
        curGraph.setmDesiredWidth(desiredWidth);
        ((MultiGraphView) curGraph).setValuesPerStripe(data.getValuesPerStripe());
        ((MultiGraphView) curGraph).setValues(data.getValues());
        ((MultiGraphView) curGraph).setColors(data.getColors());

        if (hsv == null)
            hsv = (HorizontalScrollView) getParent();

        hsv.removeAllViews();
        hsv.addView(curGraph);
    }
}
