package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
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
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;


/**
 * A placeholder fragment containing a simple view.
 */
public class StockHistoryActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int STOCK_HISTORY_LOADER = 0;
    public static  final String SYMBOL_EXTRA = "SYMBOL_EXTRA";
    private static final int CURSOR_LOADER_STOCK_HISTORY_ID = 1;
    private final String LOG_TAG = getClass().getSimpleName();
    private String[] labels = {"p1", "p2", "p3" };
    private float[] values = {(float) 1.2, (float) 2.0, (float) 3.0};
    LineChartView lChart;
    private Cursor mCursor;
    private Uri mData;
    ;


    public StockHistoryActivityFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(SYMBOL_EXTRA)){
            Log.i(LOG_TAG,"Has Extra!");
            mData = arguments.getParcelable(SYMBOL_EXTRA);
        }
        View viewRoot = inflater.inflate(R.layout.fragment_stock_history, container, false);

        lChart = (LineChartView) viewRoot.findViewById(R.id.linechart);

        getLoaderManager().initLoader(CURSOR_LOADER_STOCK_HISTORY_ID, null, this);

        return viewRoot;
        }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "onCreateLoader");
        // TODO - Make a range for history...
        if (mData != null && id == CURSOR_LOADER_STOCK_HISTORY_ID){
            Log.i(LOG_TAG, "on CreateLoader with data and same ID for Stock History!");
            String selection = QuoteColumns.BIDPRICE + " NOT NULL )" + " GROUP BY ( " + QuoteColumns.BIDPRICE + ") ORDER BY ( " + QuoteColumns.LAST_TRADE_DATE + ") LIMIT ( 10 ";
            return new CursorLoader(getContext(), mData,
                    new String[]{ QuoteColumns._ID, QuoteColumns.BIDPRICE, QuoteColumns.ISUP, QuoteColumns.LAST_TRADE_DATE}, selection ,null, null);

        }else {
            Log.i(LOG_TAG, "on CreateLoader with NO data or no ID required!");

            return null;
        }


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (loader.getId() == CURSOR_LOADER_STOCK_HISTORY_ID){
            Log.i(LOG_TAG, "onLoadFinished for STOCK HISTORY LOADER");

            mCursor = data;
            Log.d(LOG_TAG,"The cursor returned was: \n"
            + DatabaseUtils.dumpCursorToString(mCursor));
            labels = Utils.getLabelsForStockHistory(mCursor);
            values = Utils.getValuesForStockHistory(mCursor);



            LineSet ls = new LineSet(labels,  values);

            ls.setFill(getResources().getColor(R.color.material_blue_500));
            Animation animation = new Animation();
            animation.setDuration(1000);

            ls.setDotsColor(getResources().getColor(R.color.material_red_700));

            lChart.addData(ls);
            lChart.setAxisBorderValues(Utils.getMinValue(values) , Utils.getMaxValue(values));

            if (lChart.isShown()){
                Log.i(LOG_TAG,"Showing chart Data!");

                lChart.show(animation);
            }else {
                Log.i(LOG_TAG,"NOT Showing chart Data! Because it is not visible");

            }
        }

    }

    @Override
    public void onResume() {
        getLoaderManager().restartLoader(CURSOR_LOADER_STOCK_HISTORY_ID,null, this);
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG,"Closing cursor for Stock History");

        mCursor.close();
        Log.i(LOG_TAG,"onPause: Destroying LOADER " + "CURSOR_LOADER_STOCK_HISTORY_ID");

        getLoaderManager().destroyLoader(CURSOR_LOADER_STOCK_HISTORY_ID);
        super.onPause();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(LOG_TAG,"onLoaderReset");


    }

}
