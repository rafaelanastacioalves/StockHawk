package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;


/**
 * A placeholder fragment containing a simple view.
 */
public class StockHistoryActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_HISTORY_LOADER = 0;
    private final String LOG_TAG = getClass().getSimpleName();
    private String[] labels = {"p1", "p2", "p3" };
    private float[] values = {(float) 1.2, (float) 2.0, (float) 3.0};
    LineChartView lChart;
    private Cursor mCursor;

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

        lChart = (LineChartView) viewRoot.findViewById(R.id.linechart);


        return viewRoot;
        }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "onCreateLoader");
        // TODO - Make a range for history...
        return new CursorLoader(getContext(), QuoteProvider.Quotes.withSymbol("YHOO"),
                new String[]{ QuoteColumns._ID, QuoteColumns.BIDPRICE, QuoteColumns.ISUP, QuoteColumns.LAST_TRADE_DATE}, null ,null, null);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(LOG_TAG, "onLoadFinished");
        mCursor = data;

        labels = Utils.getLabelsForStockHistory(mCursor);
        values = Utils.getValuesForStockHistory(mCursor);

        LineSet ls = new LineSet(labels,  values);
        lChart.addData(ls);
        Log.i(LOG_TAG,"Showing chart Data!");

        lChart.show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
