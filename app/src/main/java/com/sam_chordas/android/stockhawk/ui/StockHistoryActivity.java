package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;


public class StockHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_history);

        // TODO Add rescuiing savedInstance
        if (savedInstanceState == null ){
            Bundle arguments = new Bundle();

            StockHistoryActivityFragment fragment =  new StockHistoryActivityFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_history_fragment_container,  fragment)
                    .commit();


        }
    }

}
