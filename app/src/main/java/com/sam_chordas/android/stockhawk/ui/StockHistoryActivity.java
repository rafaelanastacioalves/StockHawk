package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;


public class StockHistoryActivity extends AppCompatActivity {

    private final String LOG_TAG = getClass().getSimpleName() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_history);

        // TODO Add rescuiing savedInstance
        if (savedInstanceState == null ){
            Bundle arguments = new Bundle();
            Log.i(LOG_TAG, "No SavedInstance! Creating a new Fragment");
            arguments.putParcelable(StockHistoryActivityFragment.SYMBOL_EXTRA, getIntent().getData());
            StockHistoryActivityFragment fragment =  new StockHistoryActivityFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_history_fragment_container,  fragment)
                    .commit();


        }
    }

}
