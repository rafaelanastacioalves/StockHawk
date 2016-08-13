package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          if (jsonObject.getString("Bid")!= "null") {
            batchOperations.add(buildBatchOperation(jsonObject));
          }
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      builder.withValue(QuoteColumns.LAST_TRADE_DATE, jsonObject.getString("LastTradeDate"));
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }

  public static String[] getLabelsForStockHistory(Cursor mCursor) {
    ArrayList<String> valuesList = new ArrayList<>();
    if (mCursor != null && mCursor.moveToFirst() ){
      do {
        valuesList.add(
                removeYearFromString(
                        mCursor.getString(mCursor.getColumnIndex(QuoteColumns.LAST_TRADE_DATE))
                )
        );
      }while(mCursor.moveToNext());
    }
    return  valuesList.toArray(new String[mCursor.getCount()]);
  }

  private static String removeYearFromString(String string) {

    Date d = new Date(string);
    String finalString = d.getDate()
            + "/"
            + (d.getMonth() + 1);
    return finalString;
  }

  public static float[] getValuesForStockHistory(Cursor mCursor) {
    float[] valuesList = new float[mCursor.getCount()];
    if (mCursor != null && mCursor.moveToFirst() ){
      int position =0;
      do {
        valuesList[position]=
                parseBidPriceToFloat(
                        mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)
                        )

        );
        position++;

      }while(mCursor.moveToNext());
    }
    return valuesList;
  }

  private static float parseBidPriceToFloat(String string) {
    return Float.parseFloat(
            string.replace(",", ".")
    );
  }

  public static int getMinValue(float[] values) {
    float minValue = values[0];
    for (float value:values){
      if (value < minValue){
        minValue = value;

      }
    }

    return (int) Math.floor(minValue) ;
  }

  public static int getMaxValue(float[] values) {
    float maxValue = values[0];
    for (float value : values) {
      if (value > maxValue) {
        maxValue = value;

      }
    }
    return (int) Math.ceil(maxValue) ;

  }
  @SuppressWarnings("ResourceType")
  static public @StockTaskService.LocationStatus
  int getLocationStatus(Context c){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
    return sp.getInt(c.getString(R.string.pref_stock_query_status_key), StockTaskService.LOCATION_STATUS_UNKNOWN);
  }

  public static boolean isNetworkAvailable(Context context) {

    ConnectivityManager cm =
            (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null &&
            activeNetwork.isConnectedOrConnecting();
  }
}
