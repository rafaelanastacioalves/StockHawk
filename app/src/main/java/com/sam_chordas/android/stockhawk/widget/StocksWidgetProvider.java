package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.ui.StockHistoryActivity;

/**
 * Created by rafaelanastacioalves on 8/10/16.
 *  Adapted from the Udacity Lessions about WidgetProviders.
 */
public class StocksWidgetProvider extends AppWidgetProvider {
    private  final String LOG_TAG = getClass().getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_stocks);


            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }

            Intent clickIntentTemplate = new Intent(context,StockHistoryActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            // Tell the AppWidgetManager to perform an update on the current app widget
            Log.i(LOG_TAG,"Calling updateAppWidget" );
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.i(LOG_TAG, "OnReceive: start");
        if (StockTaskService.ACTION_NEW_DATA.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

        }
        Log.i(LOG_TAG, "OnReceive:end ");

    }



    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)

    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        Log.i(LOG_TAG, "setRemoteAdapter");
        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, WidgetStocksRemoteViewsService.class));
    }


    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        Log.i(LOG_TAG, "setRemoteAdapterV11");
        views.setRemoteAdapter(0, R.id.widget_list,
                new Intent(context, WidgetStocksRemoteViewsService.class));
    }
}
