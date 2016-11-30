package com.rsquared.robert.bustrackri;

import android.content.Context;

import java.util.List;
import java.util.Map;

/**
 * Created by R^2 on 11/28/2016.
 */
public interface ScheduledPositionRequestListener {
    void onScheduledPositionRequestStart();
    void onScheduledPositionRequestSuccess(List<VehiclePosition> s, boolean initialize);
    void onScheduledPositionFailure(String failureMsg);
}
