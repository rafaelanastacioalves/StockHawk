package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URLEncoder;
import java.util.Calendar;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{
  public static final String ACTION_NEW_DATA = "com.sam_chordas.android.stockhawk.ACTION_NEW_DATA";
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;
  private int result;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN, LOCATION_STATUS_SERVER_INVALID,  LOCATION_STATUS_UNKNOWN, LOCATION_STATUS_INVALID})
  public @interface StockQueryStatus {}

  public static final int LOCATION_STATUS_OK = 0;
  public static final int LOCATION_STATUS_SERVER_DOWN = 1;
  public static final int LOCATION_STATUS_SERVER_INVALID = 2;
  public static final int LOCATION_STATUS_UNKNOWN = 3;
  public static final int LOCATION_STATUS_INVALID = 4;

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
  }
  String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  @Override
  public int onRunTask  (TaskParams params){
    Cursor initQueryCursor;
    if (mContext == null){
      mContext = this;
    }
    StringBuilder urlStringBuilder = new StringBuilder();
      // Base URL for the Yahoo query
      urlStringBuilder.append("https://www.quandl.com/api/v3/datasets/WIKI/");


    if (params.getTag().equals("init") || params.getTag().equals("periodic")){
      isUpdate = true;
      initQueryCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
          new String[] { "Distinct " + QuoteColumns.SYMBOL }, null,
          null, null);
      if (initQueryCursor.getCount() == 0 || initQueryCursor == null){
        // Init task. Populates DB with quotes for the symbols seen below
          String[] defaultSymbolArray = {"YHOO","AAPL","GOOG","MSFT"};
          for (String stockSymbol : defaultSymbolArray) {
            StringBuilder urlCopy = new StringBuilder(urlStringBuilder);
            finishJobFor(urlCopy, stockSymbol);

          }
        result = GcmNetworkManager.RESULT_SUCCESS;


      } else if (initQueryCursor != null){
        DatabaseUtils.dumpCursor(initQueryCursor);
        initQueryCursor.moveToFirst();
        for (int i = 0; i < initQueryCursor.getCount(); i++){
          mStoredSymbols.append("\""+
              initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol"))+"\",");
          initQueryCursor.moveToNext();
        }
        mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
        try {
          urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
      }
    } else if (params.getTag().equals("add")){
      isUpdate = false;
      // get symbol from params.getExtra and build query
      String stockInput = params.getExtras().getString("symbol");
      finishJobFor(urlStringBuilder,stockInput);
      result = GcmNetworkManager.RESULT_SUCCESS;

    }


    return result;
  }

  private void finishJobFor(StringBuilder urlStringBuilder, String stockSymbol) {
    // finalize the URL for the API query.
    try {
      urlStringBuilder.append(
              URLEncoder.encode(stockSymbol, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    urlStringBuilder.append(".json?column_index=4&start_date=2015-09-20&end_date=2017-09-20");

    String urlString;
    String getResponse;
    result = GcmNetworkManager.RESULT_FAILURE;

    if (urlStringBuilder != null){
      urlString = urlStringBuilder.toString();
      Log.i(LOG_TAG,"urlString: " + urlString);
      try{
        getResponse = fetchData(urlString);
        JSONObject jsonObject = new JSONObject(getResponse);
        if (jsonObject != null && jsonObject.length() == 0) {
          setStockQueryStatus(mContext, LOCATION_STATUS_SERVER_DOWN);
        }else {
//          result = GcmNetworkManager.RESULT_SUCCESS;

        }
        try {
          ContentValues contentValues = new ContentValues();
          // update ISCURRENT to 0 (false) so new data is current
          if (isUpdate){
            contentValues.put(QuoteColumns.ISCURRENT, 0);
            mContext.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues,
                    null, null);
          }
          try{
            mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                    Utils.quoteJsonToContentVals(getResponse));
            Utils.setLastUserStockValidSearchStatus(mContext, true);

          }catch (Utils.InvalidStockException e){
            Log.e(LOG_TAG, e.getClass().getSimpleName());
            Utils.setLastUserStockValidSearchStatus(mContext, false);
          }

          setStockQueryStatus(mContext, LOCATION_STATUS_OK);
          setSyncTimeStamp(mContext);
          updateWidget();
        }catch (RemoteException | OperationApplicationException e){
          Log.e(LOG_TAG, "Error applying batch insert", e);
        }
      } catch (IOException e){
        e.printStackTrace();
        setStockQueryStatus(mContext, LOCATION_STATUS_SERVER_DOWN);
      } catch (JSONException e) {
        Log.e(LOG_TAG, e.getMessage(), e);
        e.printStackTrace();
        setStockQueryStatus(mContext, LOCATION_STATUS_SERVER_INVALID);

      }
    }
  }


  private void updateWidget() {
    if(mContext != null){
      // Setting the package ensures that only components in our app will receive the broadcast
      Intent dataUpdatedIntent = new Intent(ACTION_NEW_DATA)
              .setPackage(mContext.getPackageName());
      mContext.sendBroadcast(dataUpdatedIntent);
    }else{
      Log.e(LOG_TAG, "Not getting context!");
    }

  }

  static private void setStockQueryStatus(Context c, @StockQueryStatus int status){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    SharedPreferences.Editor spe = sp.edit();
    spe.putInt(c.getString(R.string.pref_stock_query_status_key), status);
    spe.commit();
  }

  private void setSyncTimeStamp(Context c) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    SharedPreferences.Editor spe = sp.edit();
    Log.i(LOG_TAG,"Setting time stamp: " + Calendar.getInstance().getTimeInMillis() );
    spe.putLong(c.getString(R.string.pref_Last_sync_time_stamp_key), Calendar.getInstance().getTimeInMillis());
    spe.commit();

  }


  }
