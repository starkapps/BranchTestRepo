package com.apps.stark.branchapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.SearchView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CurrencyActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Button mSaveButton;
    private CurrencyAdapter mAdapter;
    private HashMap<String, String> mCurrencyCountryMap = new HashMap<>();
    private ListView mLv;
    private ArrayList<String> mCurrencyArray;
    private ArrayList<String> mAllCurrencyArray;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String[] currencies = intent.getStringArrayExtra(MainActivity.KEY_CURRENCIES);
        setContentView(R.layout.activity_currency);
        mCurrencyArray = new ArrayList<>(Arrays.asList(currencies));
        mAllCurrencyArray = new ArrayList<>(mCurrencyArray);

        mLv = (ListView) findViewById(R.id.list_view);

        Util.readAssetFile(this, "currencies", mCurrencyCountryMap);

        mAdapter = new CurrencyAdapter(this, mCurrencyArray, mCurrencyCountryMap);
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
        final MenuItem item = menu.findItem(R.id.search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(item);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.search_hint));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_select_all:
                mAdapter.selectAllCurrencies();
                return true;

            case R.id.action_unselect_all:
                mAdapter.unselectAllCurrencies();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final ArrayList<String> filtered = mAdapter.filter(mAdapter.getAllCurrencies(), query);
        mAdapter.animateTo(filtered);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
}

