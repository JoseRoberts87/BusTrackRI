package com.rsquared.robert.bustrackri;

import java.util.List;

/**
 * Created by R^2 on 11/28/2016.
 */
public interface RealTimePositionRequestListener {
    void onRealTimePositionRequestStart();
    void onRealTimePositionRequestSuccess(List<VehiclePosition> vehiclePositionList, boolean initialize);
    void onRealTimeConnectionFailure(String failureMsg);

}
