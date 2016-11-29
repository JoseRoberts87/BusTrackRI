package com.rsquared.robert.bustrackri;

/**
 * Created by R^2 on 11/28/2016.
 */
public interface RealTimePositionRequestListener {
    void onRealTimePositionRequestStart();
    void onRealTimePositionRequestSuccess(String s);
}
