package com.onetrak.graph.customview.graphview.data;

/**
 * Created by aleksey.ivanov on 08.04.2016.
 */
public class MultiGraphData {
    private String[] months;
    private double[][] values;
    private double goal;
    private int valuesPerStripe;
    private int[] colors;

    public MultiGraphData(String[] months, double[][] values, double goal, int valuesPerStripe, int[] colors) {
        this.months = months;
        this.values = values;
        this.goal = goal;
        this.valuesPerStripe = valuesPerStripe;
        this.colors = colors;
    }

    public String[] getMonths() {
        return months;
    }

    public void setMonths(String[] months) {
        this.months = months;
    }

    public int[] getColors() {
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }

    public double[][] getValues() {
        return values;
    }

    public void setValues(double[][] values) {
        this.values = values;
    }

    public double getGoal() {
        return goal;
    }

    public void setGoal(double goal) {
        this.goal = goal;
    }

    public int getValuesPerStripe() {
        return valuesPerStripe;
    }

    public void setValuesPerStripe(int valuesPerStripe) {
        this.valuesPerStripe = valuesPerStripe;
    }
}
