package com.onetrak.graph.customview;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.onetrak.graph.customview.graphview.GraphView;
import com.onetrak.graph.customview.graphview.data.MultiGraphData;
import com.onetrak.graph.customview.graphview.data.UnoGraphData;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    GraphView mGraphView;
    int dataUnoId = 0;
    int dataMultiId = 0;
    UnoGraphData[] datasets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGraphView = (GraphView) findViewById(R.id.graph);

        datasets = new UnoGraphData[]{new UnoGraphData(new String[]{"March", "April", "May", "June", "July", "August",
                "March", "April", "May", "June", "July", "August"},
                new double[]{95.4, 86.3, 70.0, 65.5, 59.3, 49.3,
                        45.34, 65.5, 59.3, 49.3, 65.5, 59.3}, 75),
                new UnoGraphData(new String[]{"March", "April", "May", "June", "July", "August",
                        "March", "April", "May", "June", "July", "August"},
                        new double[]{95.4, 86.3, 0d, 65.5, 59.3, 49.3,
                                45.34, 0d, 0d, 0d, 65.5, 59.3}, 75),
                new UnoGraphData(new String[]{"March", "April", "May", "June", "July", "August",
                        "March", "April", "May", "June", "July", "August"},
                        new double[]{0d, 65.5, 59.3, 49.3,
                                45.34, 0d, 0d, 0d, 65.5, 59.3, 0d, 0d}, 75),
                new UnoGraphData(new String[]{"March", "April", "May", "March", "April"},
                        new double[]{64.1, 54.3, 67.4, 64.1, 54.3}, 58.5),
                new UnoGraphData(new String[]{"March", "April", "May", "March", "April"},
                        new double[]{34.5, 65.3, 45.4, 70.1, 45.3}, 50),
                new UnoGraphData(new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"},
                        new double[]{60.0, 64.0, 67.0, 70.0, 69.0, 72.0, 68.0, 77.0, 71.0}, 80),
                new UnoGraphData(new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"},
                        new double[]{68.0, 77.0, 71.0, 70.0, 69.0, 72.0, 67.0, 64.0, 60.0}, 55)};

    }

    public void onClick1(View v) {
        setUnoData();
    }

    public void onClick2(View v) {
        setMultiData();
    }

    private void setUnoData() {
        if (++dataUnoId == datasets.length)
            dataUnoId = 0;

        if (dataUnoId >= 0) {
            mGraphView.setupUnoGraph(datasets[dataUnoId]);
        }
    }

    private void setMultiData() {
        // Set random values
        if (++dataUnoId % 3 == 0)
            setRandomValues();
        else if (dataUnoId % 3 == 1)
            setGoodValues();
        else
            setNAValues();

    }


    private void setNAValues() {
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

        MultiGraphData data = new MultiGraphData(
                new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"},
                rndVals, 50, 2, new int[]{ContextCompat.getColor(this, R.color.graphColor),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)});

        mGraphView.setupMultiGraph(data);
    }

    private void setRandomValues() {
        double[][] rndVals = new double[4][];
        Random rnd = new Random();
        for (int i = 0; i < 4; ++i) {
            rndVals[i] = new double[9 * 2];
            for (int k = 0; k < 9 * 2; ++k)
                rndVals[i][k] = 100 * rnd.nextDouble();
        }

        MultiGraphData data = new MultiGraphData(
                new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"},
                rndVals, 50, 2, new int[]{ContextCompat.getColor(this, R.color.graphColor),
                ContextCompat.getColor(this, R.color.colorAccent),
                ContextCompat.getColor(this, R.color.colorPrimary),
                ContextCompat.getColor(this, R.color.colorPrimaryDark)});

        mGraphView.setupMultiGraph(data);

    }

    private void setGoodValues() {
        double[][] rndVals = new double[3][];
        Random rnd = new Random();
        for (int i = 0; i < 3; ++i) {
            rndVals[i] = new double[16 * 10];
            for (int k = 0; k < 16 * 10; ++k)
                rndVals[i][k] = rnd.nextBoolean() ? 20 * i + k + 10 * rnd.nextDouble() : 0;
        }

        MultiGraphData data = new MultiGraphData(
                new String[]{"January", "February", "March", "April", "May", "June", "July", "August",
                        "January", "February", "March", "April", "May", "June", "July", "August"},
                rndVals, 50, 10, new int[]{Color.parseColor("#009BA1"),
                Color.parseColor("#8AD9DB"),
                Color.parseColor("#006569")});

        mGraphView.setupMultiGraph(data);
    }
}
