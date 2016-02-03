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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

public class CurrencyAdapter extends ArrayAdapter<String> {
    private final Activity mContext;
    private ArrayList<String> mCurrencies;
    public ArrayList<String> mSelectedCurrencies = new ArrayList<>();
    public ArrayList<String> mAllCurrencies = new ArrayList<>();
    public ArrayList<String> mAllCountries = new ArrayList<>();
    private HashMap<String, String> mCountryMap = new HashMap<String, String>();

    static class ViewHolder {
        public CurrencyAdapter mAdapter;
        public TextView mCurrencyName;
        public TextView mCountryName;
        public CheckBox mCheckbox;

        public ViewHolder(CurrencyAdapter adapter) {
            this.mAdapter = adapter;
        }
    }

    public CurrencyAdapter(Activity context, ArrayList<String> currencies, HashMap<String, String>countryMap) {
        super(context, R.layout.currency_layout, currencies);
        this.mContext = context;
        this.mCurrencies = currencies;
        this.mAllCurrencies = new ArrayList<>(currencies);
        mCountryMap = countryMap;
        Iterator it = mCountryMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            mAllCountries.add((String)pair.getValue());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        LayoutInflater inflater = mContext.getLayoutInflater();
        rowView = inflater.inflate(R.layout.currency_layout, null);

        ViewHolder viewHolder = new ViewHolder(this);
        viewHolder.mCurrencyName = (TextView) rowView.findViewById(R.id.currency_name);
        viewHolder.mCountryName = (TextView) rowView.findViewById(R.id.country_name);
        viewHolder.mCheckbox = (CheckBox) rowView.findViewById(R.id.cb_currency);
        rowView.setTag(viewHolder);

        final ViewHolder holder = (ViewHolder) rowView.getTag();
        String s = mCurrencies.get(position);
        holder.mCurrencyName.setText(s);
        holder.mCountryName.setText(mCountryMap.get(s));
        boolean isChecked = mSelectedCurrencies.contains(s);
        holder.mCheckbox.setChecked(isChecked);
        holder.mCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String name = holder.mCurrencyName.getText().toString();
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
        Collections.sort(mSelectedCurrencies);
        return mSelectedCurrencies.toArray(new String[mSelectedCurrencies.size()]);
    }

    public ArrayList<String> getAllCurrencies() {
        return mAllCurrencies;
    }

    public ArrayList<String> getAllCountries() {
        return mAllCountries;
    }
    public void selectAllCurrencies() {
        mSelectedCurrencies = new ArrayList<>(mCurrencies);
        notifyDataSetChanged();
    }

    public void unselectAllCurrencies() {
        mSelectedCurrencies.clear();
        notifyDataSetChanged();
    }

    public ArrayList<String> filter(ArrayList<String> currs, String query) {
        query = query.toLowerCase();
        final ArrayList<String> filtered = new ArrayList<>();
        for (String name : currs) {
            if (name.toLowerCase().contains(query)) {
                filtered.add(name);
            }
        }
        return filtered;
    }

    public String removeItem(int position) {
        final String info = mCurrencies.get(position);
        this.remove(info);
        notifyDataSetChanged();
        return info;
    }

    public void addItem(int position, String info) {
        this.add(info);
        notifyDataSetChanged();
    }

    public void animateTo(ArrayList<String> currs) {
        applyAndAnimateRemovals(currs);
        applyAndAnimateAdditions(currs);
        Collections.sort(mCurrencies);
        notifyDataSetChanged();
    }

    private void applyAndAnimateRemovals(ArrayList<String> currs) {
        for (int i = mCurrencies.size() - 1; i >= 0; i--) {
            final String info = mCurrencies.get(i);
            if (! currs.contains(info)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<String> currs) {
        for (int i = 0, count = currs.size(); i < count; i++) {
            final String info = currs.get(i);
            if (! mCurrencies.contains(info)) {
                addItem(i, info);
            }
        }
    }

}