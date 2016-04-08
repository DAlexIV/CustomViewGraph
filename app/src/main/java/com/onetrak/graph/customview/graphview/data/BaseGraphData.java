package com.onetrak.graph.customview.graphview.data;

/**
 * Created by dalexiv on 4/8/16.
 */
public abstract class BaseGraphData {
    public enum Graphs {Uno, Multi}

    protected String[] months;
    protected double goal;

    public double getGoal() {
        return goal;
    }

    public String[] getMonths() {
        return months;
    }

    public abstract Graphs getType();
}
