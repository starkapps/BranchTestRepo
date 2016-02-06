package com.apps.stark.branchapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    public static final int CURRENCY_CODE = 100;
    public static final String KEY_CURRENCIES = "Currencies";
    public static final String KEY_SELECTED = "Selected";
    private static final double NUM_POLL_SECONDS = 10d;
    private static final double VIEW_WINDOW_MILLIS = 120000d;
    private static final int DELAY = 10000;  // Millis
    private static final int NUM_DATA_POINTS = 1000;
    private static final String TAG = "BranchApp";

    private TextView mTvCurrency;
    private TextView mTVCountry;
    private TextView mTvAsk;
    private TextView mTvTime;
    private Button mExitButton;
    private GraphView mGraph;
    private LineGraphSeries<DataPoint> mDataSeries;
    private double mLastX = 0d;

    private String mSelectedCurrency = "USD";
    private String[] mCurrencies;
    private Spinner mCurrencySpinner;
    private ArrayAdapter<String> mSpinnerAdapter;
    private int mCurrencyIndex = 0;
    private ArrayList<String> mAllCurrencies = new ArrayList<>();
    private HashMap<String, String> mCurrencyCountryMap = new HashMap<>();
    private RequestQueue mRequestQueue;
    private long mStartTime;

    final Handler mQuoteHandler = new Handler();
    Runnable mQuoteRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                getQuotes(mSelectedCurrency);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Util.readAssetFile(this, "currencies", mCurrencyCountryMap);
        mRequestQueue = Volley.newRequestQueue(this);

        mTvCurrency = (TextView) findViewById(R.id.currency_name);
        mTVCountry = (TextView) findViewById(R.id.country_name);
        mTvAsk = (TextView) findViewById(R.id.ask_price);
        mTvTime = (TextView) findViewById(R.id.time_stamp);
        mCurrencies = getResources().getStringArray(R.array.selected_currencies);
        mCurrencySpinner = (Spinner) findViewById(R.id.currency_spinner);
        mCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                stopQuotes();
                mCurrencyIndex = pos;
                mSelectedCurrency = mCurrencies[mCurrencyIndex];
                startQuotes();
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mCurrencySpinner.setSelection(mCurrencyIndex);
        mSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCurrencySpinner.setAdapter(mSpinnerAdapter);
        for (String curr : mCurrencies) {
            mSpinnerAdapter.add(curr);
        }
        mSpinnerAdapter.notifyDataSetChanged();

        mExitButton = (Button) findViewById(R.id.exit_button);
        mExitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        createGraph();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopQuotes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_select) {
            getJsonCurrencyList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == CURRENCY_CODE) {
                String[] selected = data.getStringArrayExtra(KEY_SELECTED);
                if (selected.length > 0) {
                    mCurrencies = selected;
                    mSpinnerAdapter.clear();
                    for (String curr : mCurrencies) {
                        mSpinnerAdapter.add(curr);
                    }
                    mSpinnerAdapter.notifyDataSetChanged();
                    // Grab the first of current list of selected currencies
                    mCurrencyIndex = 0;
                    mSelectedCurrency = mCurrencies[mCurrencyIndex];
                    mCurrencySpinner.setSelection(mCurrencyIndex);
                    startQuotes();
                }
            }
        }
    }

    private void createGraph() {
        mGraph = (GraphView) findViewById(R.id.graph);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(40);
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setScrollable(true);
        mGraph.getGridLabelRenderer().setNumHorizontalLabels(5);
        mGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // Create a calendar object that will convert the date and time value in milliseconds to date.
                    // "value" here is # of (roughly) 10-second ticks since first started receiving quotes
                    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:\nss");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(((long) value * 1000) + mStartTime);
                    String label = formatter.format(calendar.getTime());
                    return label;
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });
    }

    private void startQuotes() {
        mQuoteHandler.post(mQuoteRunnable);
    }

    private void stopQuotes() {
        mQuoteHandler.removeCallbacks(mQuoteRunnable);
        mGraph.removeAllSeries();
        mDataSeries = null;
        mLastX = 0d;
    }

    private void getQuotes(String currency) {
        String url = getString(R.string.URL) + currency;

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        QuoteInfo qi = processJsonIntoQuote(response);
                        updateQuote(qi);
                        Log.d(TAG, "Success");
                        mQuoteHandler.postDelayed(mQuoteRunnable, DELAY);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Failed");
                        showErrorToast("Volley request failed with network error " +
                                error.networkResponse.statusCode);
                        mQuoteHandler.postDelayed(mQuoteRunnable, DELAY);
                    }
                });
        mRequestQueue.add(jsObjRequest);
    }

    private QuoteInfo processJsonIntoQuote(JSONObject jsonQuote) {
        QuoteInfo qi = new QuoteInfo();

        qi.setCurrencyName(mSelectedCurrency);
        qi.setCountryName(mCurrencyCountryMap.get(mSelectedCurrency));

        try {
            qi.setAskingPrice(jsonQuote.get(getString(R.string.ask)).toString());
            qi.setTimeStamp(jsonQuote.get(getString(R.string.timestamp)).toString());
        } catch (JSONException je) {
            Log.d(TAG, "JSONException");
            return null;
        }
        return qi;
    }

    private void updateGraph(QuoteInfo qi) {
        Double dataPoint = Double.valueOf(qi.getAskingPrice());
        if (mDataSeries == null) {
            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss");
            Date date = new Date();
            try {
                date = formatter.parse(qi.getTimeStamp());
            } catch (Exception e) {
                //Just use system time
            }
            mStartTime = date.getTime();
            mDataSeries = new LineGraphSeries<>(new DataPoint[]{
                    new DataPoint(mLastX, dataPoint)});
            mGraph.addSeries(mDataSeries);
        } else {
            mDataSeries.appendData(new DataPoint(mLastX, dataPoint), true, NUM_DATA_POINTS);
        }
        mLastX += NUM_POLL_SECONDS;
    }

    private void updateQuote(QuoteInfo qi) {
        mTvCurrency.setText(qi.getCurrencyName());
        mTVCountry.setText(qi.getCountryName());
        mTvAsk.setText(qi.getAskingPrice());
        mTvTime.setText(qi.getTimeStamp());

        // Update graph, too
        updateGraph(qi);
    }

    private void fireUpCurrencyActivity() {
        Intent intent = new Intent(this, CurrencyActivity.class);
        intent.putExtra(KEY_CURRENCIES, mAllCurrencies.toArray(new String[mAllCurrencies.size()]));
        startActivityForResult(intent, CURRENCY_CODE);
    }

    private void showErrorToast(String errStr) {
        Toast.makeText(this, (String) errStr, Toast.LENGTH_SHORT).show();
    }

    private void getJsonCurrencyList() {

        // Assume that the list of all currencies won't change, so only fetch once from BitCoin
        if (mAllCurrencies.isEmpty()) {
            String url = getString(R.string.ALL_URL);
            RequestQueue queue = Volley.newRequestQueue(this);

            JsonObjectRequest jsObjRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            mAllCurrencies = getAllCurrencies(response);
                            fireUpCurrencyActivity();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errStr = "That didn't work! Error " + error;
                            Log.d(TAG, errStr);
                            showErrorToast("Volley request failed with network error " +
                                    error.networkResponse.statusCode);
                        }
                    });
            queue.add(jsObjRequest);
        } else {
            fireUpCurrencyActivity();
        }
    }

    private ArrayList<String> getAllCurrencies(JSONObject jList) {
        ArrayList<String> currs = new ArrayList<>();
        int i = 0;
        Iterator<String> iter = jList.keys();
        while (iter.hasNext()) {
            String name = iter.next();
            currs.add(name);
        }
        Collections.sort(currs);
        return currs;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
