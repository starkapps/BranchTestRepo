package com.apps.stark.branchapp;

/**
 * Created by lisa on 2/7/16.
 */
public interface CommStrategy {

    public void sendRequest(String URL, CommCallback.CallbackOptions option);
    public Object getResponse();
    public String getErrorResponse();
}
