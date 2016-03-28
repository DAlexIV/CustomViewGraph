package com.onetrak.graph.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.onetrak.graph.customview.graphview.GraphView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    GraphView mGraphView;
    Random gen;

    boolean firstTypeSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGraphView = (GraphView) findViewById(R.id.graph);
        setOtherData();
        gen = new Random();
    }

    private void setData(String[] months, Double[] values, double mGoal) {
        mGraphView.setMonths(months);
        mGraphView.setValues(values);
        mGraphView.setGoal(mGoal);
    }

    public void onClick(View v) {
        setOtherData();
    }
    
    private void setOtherData() {
        if (firstTypeSet) {
            setData(new String[]{"March", "April", "May", "June", "July", "August",
                            "March", "April", "May", "June", "July", "August"},
                    new Double[]{95.4, 86.3, 70.0, 65.5, 59.3, 49.3,
                            45.34, 65.5, 59.3, 49.3, 65.5, 59.3}, 80);
            firstTypeSet = false;
        }
        else {
            setData(new String[]{"March", "April", "May", "March", "April"},
                    new Double[]{64.1, 54.3, 67.4, 64.1, 54.3}, 58.5);
            firstTypeSet = true;
        }
    }
}
