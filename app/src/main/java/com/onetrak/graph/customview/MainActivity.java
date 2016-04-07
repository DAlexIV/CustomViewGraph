package com.onetrak.graph.customview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.onetrak.graph.customview.graphview.MultiGraphView;
import com.onetrak.graph.customview.graphview.UnoGraphView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    class Data {
        private String[] months;
        private double[] values;
        private double goal;

        public Data(String[] months, double[] values, double goal) {
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

    UnoGraphView mUnoGraphView;
    MultiGraphView multiGraphView;
    int dataId = -2;
    Data[] datasets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUnoGraphView = (UnoGraphView) findViewById(R.id.graph);
        multiGraphView = (MultiGraphView) findViewById(R.id.multi_graph);

        datasets = new Data[]{new Data(new String[]{"March", "April", "May", "June", "July", "August",
                "March", "April", "May", "June", "July", "August"},
                new double[]{95.4, 86.3, 70.0, 65.5, 59.3, 49.3,
                        45.34, 65.5, 59.3, 49.3, 65.5, 59.3}, 75),
                new Data(new String[]{"March", "April", "May", "June", "July", "August",
                        "March", "April", "May", "June", "July", "August"},
                        new double[]{95.4, 86.3, 0d, 65.5, 59.3, 49.3,
                                45.34, 0d, 0d, 0d, 65.5, 59.3}, 75),
                new Data(new String[]{"March", "April", "May", "June", "July", "August",
                        "March", "April", "May", "June", "July", "August"},
                        new double[]{0d, 65.5, 59.3, 49.3,
                                45.34, 0d, 0d, 0d, 65.5, 59.3, 0d, 0d}, 75),
                new Data(new String[]{"March", "April", "May", "March", "April"},
                        new double[]{64.1, 54.3, 67.4, 64.1, 54.3}, 58.5),
                new Data(new String[]{"March", "April", "May", "March", "April"},
                        new double[]{34.5, 65.3, 45.4, 70.1, 45.3}, 50),
                new Data(new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"},
                        new double[]{60.0, 64.0, 67.0, 70.0, 69.0, 72.0, 68.0, 77.0, 71.0}, 80),
                new Data(new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"},
                        new double[]{68.0, 77.0, 71.0, 70.0, 69.0, 72.0, 67.0, 64.0, 60.0}, 55)};
        setOtherData();


    }

    private void setData(String[] months, double[] values, double mGoal) {
        mUnoGraphView.setMonths(months);
        mUnoGraphView.setValues(values);
        mUnoGraphView.setGoal(mGoal);

    }

    public void onClick(View v) {
        setOtherData();
    }

    private void setOtherData() {
        if (++dataId == datasets.length)
            dataId = 0;

        if (dataId >= 0) {
            setData(datasets[dataId].getMonths(),
                    datasets[dataId].getValues(),
                    datasets[dataId].getGoal());


            // Set random values
            if (dataId % 3 == 0)
                setRandomValues();
            else if (dataId % 3 == 1)
                setGoodValues();
            else
                setNAValues();
        }


    }

    private void setNAValues() {
        multiGraphView.setMonths(new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"});
        multiGraphView.setValuesPerStripe(2);
        double[][] rndVals = new double[4][];
        Random rnd = new Random();
        for (int i = 0; i < 4; ++i) {
            rndVals[i] = new double[9 * 2];
            for (int k = 0; k < 9 * 2; ++k)
                if (i == 0)
                    rndVals[i][k] = (k % 2 == 0) ? 50 : 0;
                else
                    rndVals[i][k] = rnd.nextBoolean() ? 100 * rnd.nextDouble() : 0;
        }
        multiGraphView.setValues(rndVals);
        multiGraphView.setColors(new int[]{ContextCompat.getColor(this, R.color.graphColor),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)});
    }

    private void setRandomValues() {
        multiGraphView.setMonths(new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"});
        multiGraphView.setValuesPerStripe(2);
        double[][] rndVals = new double[4][];
        Random rnd = new Random();
        for (int i = 0; i < 4; ++i) {
            rndVals[i] = new double[9 * 2];
            for (int k = 0; k < 9 * 2; ++k)
                rndVals[i][k] = 100 * rnd.nextDouble();
        }
        multiGraphView.setValues(rndVals);
        multiGraphView.setColors(new int[]{ContextCompat.getColor(this, R.color.graphColor),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)});
    }

    private void setGoodValues() {
        multiGraphView.setMonths(new String[]{"January", "February", "March", "April", "May", "June", "July", "August"});
        multiGraphView.setValuesPerStripe(10);

        double[][] rndVals = new double[3][];
        Random rnd = new Random();
        for (int i = 0; i < 3; ++i) {
            rndVals[i] = new double[8 * 10];
            for (int k = 0; k < 8 * 10; ++k)
                rndVals[i][k] = rnd.nextBoolean() ? 20 * i + k + 10 * rnd.nextDouble() : 0;
        }

        multiGraphView.setValues(rndVals);
        multiGraphView.setColors(new int[]{Color.parseColor("#009BA1"),
                Color.parseColor("#8AD9DB"),
                Color.parseColor("#006569")});
    }
}
