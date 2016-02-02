package com.apps.stark.branchapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;

public class CurrencyAdapter extends ArrayAdapter<String> {
    private final Activity mContext;
    private final String[] mCurrencies;
    public LinkedHashSet<String> mSelectedCurrencies = new LinkedHashSet<>();

    static class ViewHolder {
        public CurrencyAdapter mAdapter;
        public TextView mName;
        public CheckBox mCheckbox;

        public ViewHolder(CurrencyAdapter adapter) {
            this.mAdapter = adapter;
        }
    }

    public CurrencyAdapter(Activity context, String[] currencies) {
        super(context, R.layout.currency_layout, currencies);
        this.mContext = context;
        this.mCurrencies = currencies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = mContext.getLayoutInflater();
            rowView = inflater.inflate(R.layout.currency_layout, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder(this);
            viewHolder.mName = (TextView) rowView.findViewById(R.id.currency_name);
            viewHolder.mCheckbox = (CheckBox) rowView.findViewById(R.id.cb_currency);
            rowView.setTag(viewHolder);
        }

        // fill data
        final ViewHolder holder = (ViewHolder) rowView.getTag();
        final int index = position;
        String s = mCurrencies[position];
        holder.mName.setText(s);
        boolean isChecked = mSelectedCurrencies.contains(s);
        holder.mCheckbox.setChecked(isChecked);
        holder.mCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String name = holder.mName.getText().toString();
                        boolean checked = holder.mCheckbox.isChecked();
                        if (isChecked) {
                            holder.mAdapter.mSelectedCurrencies.add(name);
                        } else {
                            holder.mAdapter.mSelectedCurrencies.remove(name);
                        }
                    }

                });

        return rowView;
    }

    public String[] getSelectedCurrencies() {
        return mSelectedCurrencies.toArray(new String[mSelectedCurrencies.size()]);
    }
}