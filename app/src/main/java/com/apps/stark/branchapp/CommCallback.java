package com.apps.stark.branchapp;

import org.json.JSONObject;

/**
 * Created by lisa on 2/7/16.
 */
public interface CommCallback {

    public void quoteResponseCallback(JSONObject response);
    public void quoteErrorCallback(String error);
    public void currencyResponseCallback(JSONObject response);
    public void currencyErrorCallback(String error);

    public static enum CallbackOptions {
        QUOTE,
        CURRENCY_LIST
    }

}
