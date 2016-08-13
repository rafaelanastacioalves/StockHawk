package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by rafaelanastacioalves on 8/10/16.
 */
public class WidgetStocksRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            public  final String LOG_TAG = getClass().getSimpleName();
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                Log.i(LOG_TAG, "start of onDataSetChanged!");
                if (data != null) {
                    Log.d(LOG_TAG, "no Data!");

                    data.close();
                }

                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                Uri stockContent = QuoteProvider.Quotes.CONTENT_URI;
                data = getContentResolver().query(stockContent,
                        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
                Log.i(LOG_TAG, "End of onDataSetChanged!");

            }

            @Override
            public void onDestroy() {
                Log.i(LOG_TAG, "onDestroy: start");

                if (data != null) {
                    data.close();
                    data = null;
                }
                Log.i(LOG_TAG, "onDestroy: end");

            }

            @Override
            public int getCount() {
                Log.i(LOG_TAG, "getCount");

                return data == null ? 0 : data.getCount();



            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.i(LOG_TAG, "getViewAt: start");

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                Context mContext = getBaseContext();

                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.list_item_quote);
                views.setTextViewText(R.id.stock_symbol ,data.getString(data.getColumnIndex("symbol")));
                views.setTextViewText(R.id.bid_price ,data.getString(data.getColumnIndex("bid_price")));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, mContext);
                }

                int sdk = Build.VERSION.SDK_INT;
                if (data.getInt(data.getColumnIndex("is_up")) == 1){
                    if (sdk < Build.VERSION_CODES.JELLY_BEAN){
                        //TODO - May need a different method than "setBackgroundResource" ;
                        Log.i(LOG_TAG, "calling view.setInt ");

                        views.setInt( R.id.change  ,"setBackgroundResource",R.drawable.percent_change_pill_green );
                    }else {
                        //TODO - May need a different method than "setBackgroundResource" ;
                        Log.i(LOG_TAG, "calling view.setInt ");

                        views.setInt( R.id.change  ,"setBackgroundResource",R.drawable.percent_change_pill_green );
                    }
                } else{
                    if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                        //TODO - May need a different method than "setBackgroundResource" ;
                        Log.i(LOG_TAG, "calling view.setInt ");

                        views.setInt( R.id.change  ,"setBackgroundResource",R.drawable.percent_change_pill_red );
                    } else{
                        //TODO - May need a different method than "setBackgroundResource" ;
                        Log.i(LOG_TAG, "calling view.setInt ");

                        views.setInt( R.id.change  ,"setBackgroundResource",R.drawable.percent_change_pill_red );
                    }
                }
                if (Utils.showPercent){
                    Log.i(LOG_TAG, "showing percent");

                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("percent_change")));
                } else{
                    Log.i(LOG_TAG, "not showing percent");
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("change")));

                }


                final Intent fillInIntent = new Intent();
                String symbol = data.getString(data.getColumnIndex("symbol"));
                Uri u = QuoteProvider.Quotes.withSymbol(symbol);
                fillInIntent.setData(u);
                views.setOnClickFillInIntent(R.id.widget_stock_list_item, fillInIntent);

                Log.i(LOG_TAG, "getViewAt: end");

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                Log.i(LOG_TAG, "getLoadingView");

                return new RemoteViews(getPackageName(), R.layout.list_item_quote);
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views,  Context mContext) {
                views.setContentDescription(R.id.stock_symbol, mContext.getString(R.string.a11y_symbol ,data.getString(data.getColumnIndex("symbol"))));
                views.setContentDescription(R.id.bid_price ,mContext.getString(R.string.a11y_bid_price ,data.getString(data.getColumnIndex("bid_price"))));
                views.setContentDescription(R.id.change ,mContext.getString(R.string.a11y_percent_change ,data.getString(data.getColumnIndex("percent_change"))));
                views.setContentDescription(R.id.change ,mContext.getString(R.string.a11y_change ,data.getString(data.getColumnIndex("change"))));


            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.i(LOG_TAG, "getItemId");

                if (data.moveToPosition(position))
                    return data.getLong(data.getColumnIndex("_id"));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }


}
