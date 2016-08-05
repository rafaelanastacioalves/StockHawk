package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;


/**
 * A placeholder fragment containing a simple view.
 */
public class StockHistoryActivityFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();
    private String[] labels = {"p1", "p2", "p3" };
    private float[] values = {(float) 1.2, (float) 2.0, (float) 3.0};

    public StockHistoryActivityFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("SYMBOL_EXTRA")){
            Log.i(LOG_TAG,"Has Extra!");
        }
        View viewRoot = inflater.inflate(R.layout.fragment_stock_history, container, false);

        LineChartView lChart = (LineChartView) viewRoot.findViewById(R.id.linechart);

        LineSet ls = new LineSet(labels, values);
        lChart.addData(ls);
        Log.i(LOG_TAG,"Showing chart Data!");

        lChart.show();
        return viewRoot;
        }
}
