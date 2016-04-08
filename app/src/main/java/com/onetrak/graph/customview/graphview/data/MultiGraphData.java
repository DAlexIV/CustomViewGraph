package com.onetrak.graph.customview.graphview.data;

/**
 * Created by aleksey.ivanov on 08.04.2016.
 */
public class MultiGraphData extends BaseGraphData {
    private double[][] values;
    private int valuesPerStripe;
    private int[] colors;

    public MultiGraphData(String[] months, double[][] values, double goal, int valuesPerStripe, int[] colors) {
        this.months = months;
        this.values = values;
        this.goal = goal;
        this.valuesPerStripe = valuesPerStripe;
        this.colors = colors;
    }


    public int[] getColors() {
        return colors;
    }


    public double[][] getValues() {
        return values;
    }


    public int getValuesPerStripe() {
        return valuesPerStripe;
    }

}
