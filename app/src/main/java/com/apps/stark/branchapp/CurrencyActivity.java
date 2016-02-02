package com.apps.stark.branchapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class CurrencyActivity extends AppCompatActivity {

    private Button mSaveButton;
    private CurrencyAdapter mAdapter;
    private HashMap<String, String> mCurrencyCountryMap = new HashMap<>();
    private ListView mLv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String[] currencies = intent.getStringArrayExtra(MainActivity.KEY_CURRENCIES);
        setContentView(R.layout.activity_currency);

        mLv = (ListView) findViewById(R.id.list_view);

        Util.readAssetFile(this, "currencies", mCurrencyCountryMap);

        mAdapter = new CurrencyAdapter(this, currencies, mCurrencyCountryMap);
        mLv.setAdapter(mAdapter);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_currency, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_select_all) {
            mAdapter.selectAllCurrencies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

