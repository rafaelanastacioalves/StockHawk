package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.app.Activity;

import com.example.sam_chordas.stockhawk.R;

public class StockHistoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_history);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
