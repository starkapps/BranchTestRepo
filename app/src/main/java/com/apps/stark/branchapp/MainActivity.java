package com.apps.stark.branchapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final int CURRENCY_CODE = 100;
    public static final String KEY_CURRENCIES = "Currencies";
    public static final String KEY_SELECTED = "Selected";
    private TextView mTvCurrency;
    private TextView mTVCountry;
    private TextView mTvAsk;
    private TextView mTvTime;
    private Button mExitButton;

    private static final int DELAY = 10000;  // Millis
    private static final String TAG = "BranchApp";
    private String mSelectedCurrency = "USD";
    private String[] mCurrencies;
    private Spinner mCurrencySpinner;
    private ArrayAdapter<String> mSpinnerAdapter;
    private int mCurrencyIndex = 0;
    private ArrayList<String> mAllCurrencies = new ArrayList<>();
    private HashMap<String, String> mCurrencyCountryMap = new HashMap<>();

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

        mTvCurrency = (TextView) findViewById(R.id.currency_name);
        mTVCountry = (TextView) findViewById(R.id.country_name);
        mTvAsk = (TextView) findViewById(R.id.ask_price);
        mTvTime = (TextView) findViewById(R.id.time_stamp);
        mCurrencies = getResources().getStringArray(R.array.selected_currencies);
        mCurrencySpinner = (Spinner) findViewById(R.id.currency_spinner);
        mCurrencySpinner.setSelection(mCurrencyIndex);
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

    }

    @Override
    public void onResume() {
        super.onResume();

        // Only poll for prices if we're in the foreground
        startQuotes();

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
            // Get the full currency list -- if it's not populated yet,
            // query BitCoin API. Once the list is available,
            getJsonCurrencyList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CURRENCY_CODE) {
            String[] selected = data.getStringArrayExtra(KEY_SELECTED);
            mCurrencies = selected;
            mSpinnerAdapter.clear();
            for (String curr : mCurrencies) {
                mSpinnerAdapter.add(curr);
            }
            mSpinnerAdapter.notifyDataSetChanged();
        }
    }

    private void startQuotes() {
        mQuoteHandler.post(mQuoteRunnable);
    }

    private void stopQuotes() {
        mQuoteHandler.removeCallbacks(mQuoteRunnable);
    }

    private void getQuotes(String currency) {
        String url = getString(R.string.URL) + currency;
        RequestQueue queue = Volley.newRequestQueue(this);

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
                        mQuoteHandler.postDelayed(mQuoteRunnable, DELAY);
                    }
                });
        queue.add(jsObjRequest);
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

    private void updateQuote(QuoteInfo qi) {
        mTvCurrency.setText(qi.getCurrencyName());
        mTVCountry.setText(qi.getCountryName());
        mTvAsk.setText(qi.getAskingPrice());
        mTvTime.setText(qi.getTimeStamp());
    }

    private void fireUpCurrencyActivity() {
        Intent intent = new Intent(this, CurrencyActivity.class);
        intent.putExtra(KEY_CURRENCIES, mAllCurrencies.toArray(new String[mAllCurrencies.size()]));
        startActivityForResult(intent, CURRENCY_CODE);
    }

    private void showErrorToast(String errStr) {
        Toast.makeText(this, (String)errStr, Toast.LENGTH_SHORT).show();
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
                            showErrorToast(errStr);
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

}
