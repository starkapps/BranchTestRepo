package com.apps.stark.branchapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CurrencyActivity extends ListActivity {

    private Button mSaveButton;
    private CurrencyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String[] currencies = intent.getStringArrayExtra(MainActivity.KEY_CURRENCIES);
        setContentView(R.layout.activity_currency);

        mAdapter = new CurrencyAdapter(this, currencies);
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

