package com.onetrak.graph.customview.graphview.data;

/**
 * Created by aleksey.ivanov on 08.04.2016.
 */
public class UnoGraphData {
    private String[] months;
    private double[] values;
    private double goal;

    public UnoGraphData(String[] months, double[] values, double goal) {
        this.months = months;
        this.values = values;
        this.goal = goal;
    }

    public String[] getMonths() {
        return months;
    }

    public double[] getValues() {
        return values;
    }

    public double getGoal() {
        return goal;
    }
}
