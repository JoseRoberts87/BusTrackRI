package com.rsquared.robert.bustrackri;

import java.util.List;
import java.util.Map;

/**
 * Created by R^2 on 11/28/2016.
 */
public interface ScheduledPositionRequestListener {
    void onScheduledPositionRequestStart();
    void onScheduledPositionRequestSuccess(Map<String, List<String[]>> s);
}
