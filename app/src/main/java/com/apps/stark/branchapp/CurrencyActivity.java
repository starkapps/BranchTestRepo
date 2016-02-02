package com.apps.stark.branchapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class CurrencyActivity extends ListActivity {

    private Button mSaveButton;
    private CurrencyAdapter mAdapter;
    private HashMap<String, String> mCurrencyCountryMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String[] currencies = intent.getStringArrayExtra(MainActivity.KEY_CURRENCIES);
        setContentView(R.layout.activity_currency);

        Util.readAssetFile(this, "currencies", mCurrencyCountryMap);

        mAdapter = new CurrencyAdapter(this, currencies, mCurrencyCountryMap);
        setListAdapter(mAdapter);

        mSaveButton = (Button) findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String[] selected = mAdapter.getSelectedCurrencies();
                Intent intent = new Intent();
                intent.putExtra(MainActivity.KEY_SELECTED, selected);
                setResult(MainActivity.CURRENCY_CODE, intent);
                finish();
            }
        });
    }
}

