package com.rsquared.robert.bustrackri;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by R^2 on 11/28/2016.
 */
public interface DirectionAPIRequestListener {
    void onDirectionAPIRequestStart();
    void onDirectionAPIRequestSuccess(List<LatLng> latLngList, String markerId);
    void onDirectionAPIFailure(LatLng originalLatLng, String markerId);
}
