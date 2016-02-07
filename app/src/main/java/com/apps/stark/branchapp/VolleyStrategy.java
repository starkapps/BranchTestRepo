package com.apps.stark.branchapp;

import android.content.Context;

import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by lisa on 2/7/16.
 */
public class VolleyStrategy implements CommStrategy {

    private RequestQueue mRequestQueue;
    private JSONObject mResponseObject;
    private String mErrorResponse;
    private Context mContext;
    private CommCallback mCallbacks;
    private CommCallback.CallbackOptions mOption;

    private Response.Listener<JSONObject> mListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            mResponseObject = response;
            switch (mOption) {
                case QUOTE:
                    mCallbacks.quoteResponseCallback(response);
                    break;
                case CURRENCY_LIST:
                    mCallbacks.currencyResponseCallback(response);
                    break;
            }
        }
    };
    private Response.ErrorListener mErrorListener =  new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mErrorResponse = "Volley request failed with network error " +
                    error.networkResponse.statusCode;
            switch (mOption) {
                case QUOTE:
                    mCallbacks.quoteErrorCallback(mErrorResponse);
                    break;
                case CURRENCY_LIST:
                    mCallbacks.currencyErrorCallback(mErrorResponse);
                    break;
            }
        }
    };

    public VolleyStrategy(Context context, CommCallback callbacks) {
        mRequestQueue = Volley.newRequestQueue(context);
        this.mContext = context;
        this.mCallbacks = callbacks;
    }

    public void sendRequest(String url, CommCallback.CallbackOptions option) {
        mOption = option;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, mListener, mErrorListener);

        mRequestQueue.add(jsObjRequest);

    }

    public JSONObject getResponse() {
        return mResponseObject;
    }

    public String getErrorResponse() {
        return mErrorResponse;
    }

}
