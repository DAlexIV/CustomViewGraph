package com.onetrak.graph.customview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.onetrak.graph.customview.graphview.GraphView;

public class MainActivity extends AppCompatActivity {

    class Data {
        private String[] months;
        private Double[] values;
        private double goal;

        public Data(String[] months, Double[] values, double goal) {
            this.months = months;
            this.values = values;
            this.goal = goal;
        }

        public String[] getMonths() {
            return months;
        }

        public Double[] getValues() {
            return values;
        }

        public double getGoal() {
            return goal;
        }
    }

    GraphView mGraphView;
    int dataId = 0;
    Data[] datasets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGraphView = (GraphView) findViewById(R.id.graph);
        datasets = new Data[]{new Data(new String[]{"March", "April", "May", "June", "July", "August",
                "March", "April", "May", "June", "July", "August"},
                new Double[]{95.4, 86.3, 70.0, 65.5, 59.3, 49.3,
                        45.34, 65.5, 59.3, 49.3, 65.5, 59.3}, 75),
                new Data(new String[]{"March", "April", "May", "June", "July", "August",
                        "March", "April", "May", "June", "July", "August"},
                        new Double[]{95.4, 86.3, 0d, 65.5, 59.3, 49.3,
                                45.34, 0d, 0d, 0d, 65.5, 59.3}, 75),
                new Data(new String[]{"March", "April", "May", "June", "July", "August",
                        "March", "April", "May", "June", "July", "August"},
                        new Double[]{0d, 65.5, 59.3, 49.3,
                                45.34, 0d, 0d, 0d, 65.5, 59.3, 0d, 0d}, 75),
                new Data(new String[]{"March", "April", "May", "March", "April"},
                        new Double[]{64.1, 54.3, 67.4, 64.1, 54.3}, 58.5),
                new Data(new String[]{"March", "April", "May", "March", "April"},
                        new Double[]{34.5, 65.3, 45.4, 70.1, 45.3}, 50),
                new Data(new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"},
                        new Double[]{60.0, 64.0, 67.0, 70.0, 69.0, 72.0, 68.0, 77.0, 71.0}, 80),
                new Data(new String[]{"March", "April", "May", "March", "April", "April", "May", "March", "April"},
                        new Double[]{68.0, 77.0, 71.0, 70.0, 69.0, 72.0, 67.0, 64.0, 60.0}, 55)};
        setOtherData();
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
        if (++dataId == datasets.length)
            dataId = 0;

        setData(datasets[dataId].getMonths(),
                datasets[dataId].getValues(),
                datasets[dataId].getGoal());
    }
}
