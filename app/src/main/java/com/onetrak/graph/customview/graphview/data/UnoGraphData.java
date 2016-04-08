package com.onetrak.graph.customview.graphview.data;

/**
 * Created by aleksey.ivanov on 08.04.2016.
 */
public class UnoGraphData extends BaseGraphData{
    private double[] values;

    public UnoGraphData(String[] months, double[] values, double goal) {
        this.months = months;
        this.values = values;
        this.goal = goal;
    }

    public double[] getValues() {
        return values;
    }

    @Override
    public Graphs getType() {
        return Graphs.Uno;
    }
}
