package com.apps.stark.branchapp;

/**
 * Created by lisa on 1/29/16.
 */
public class QuoteInfo {

    private String mCurrencyName;

    private String mCountryName;
    private String mAskingPrice;
    private String mTimeStamp;

    public String getAskingPrice() {
        return mAskingPrice;
    }

    public void setAskingPrice(String mAskingPrice) {
        this.mAskingPrice = mAskingPrice;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(String mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
    }

    public String getCurrencyName() {
        return mCurrencyName;
    }

    public void setCurrencyName(String mCurrencyName) {
        this.mCurrencyName = mCurrencyName;
    }

    public String getCountryName() {
        return mCountryName;
    }

    public void setCountryName(String mCountryName) {
        this.mCountryName = mCountryName;
    }
}
