package com.rsquared.robert.bustrackri;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by R^2 on 11/17/2016.
 */

public class MarkerController {

    private Marker marker;
    private LatLng latLng;
    private Location location;
    private long timeStamp;
    private MarkerOptions markerOptions;
    private MarkerAnimation markerAnimation;
    private String markerId;
    private boolean isRealTime;
    private Runnable runnable;
    private String tripId;
    private double bearing;
    private String stopId;
    private String stopName;
    private String startTime;
    private int stopSequence;
    private float animationDuration;
    private String directionJson;
    private String roadJson;
    private List<LatLng> latLngArray;
    private int animationCounter;

    public MarkerController(Marker marker, String markerId, LatLng latLng, long timeStamp, MarkerOptions markerOptions, MarkerAnimation markerAnimation, float animationDuration, boolean isRealTime){
        this.marker = marker;
        this.markerId = markerId;
        this.latLng = latLng;
        this.timeStamp = timeStamp;
        this.markerOptions = markerOptions;
        this.isRealTime = isRealTime;
        this.markerAnimation = markerAnimation;
        this.location = new Location(String.valueOf(latLng));
        this.animationDuration = animationDuration;

    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public void markerVisible(boolean isVisible){
        this.marker.setVisible(isVisible);
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public MarkerOptions getMarkerOptions() {
        return markerOptions;
    }

    public void setMarkerOptions(MarkerOptions markerOptions) {
        this.markerOptions = markerOptions;
    }

    public void updateMarkerOptionsTitle(String title) {
        this.markerOptions.title(title);
    }

    public void updateMarkerOptionsSnippet(String snippet) {
        this.markerOptions.snippet(snippet);
    }

    public void updateMarkerOptionsIcon(String title) {
        this.markerOptions.title(title);
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public boolean isRealTime() {
        return isRealTime;
    }

    public void setRealTime(boolean realTime) {
        isRealTime = realTime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean compareBusLocation(Marker currentMarker, Marker oldMarker){
        LatLng currentLatLng = currentMarker.getPosition();
        LatLng oldLatLng = oldMarker.getPosition();
        boolean isLocationSame = currentLatLng == oldLatLng;
        return isLocationSame;
    }

    public boolean isNewerTimeStamp(long newTimeStamp){
        return newTimeStamp > this.timeStamp;
    }

    public boolean isSameMarkerId(String markerId){
        return markerId.equalsIgnoreCase(this.markerId);
    }

    public boolean isSameLatLng(LatLng latLng){
        return latLng == this.latLng;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public Marker getMarkerById(String markerId) {
        return this.marker;
    }

    public MarkerAnimation getMarkerAnimation() {
        return markerAnimation;
    }

    public void setMarkerAnimation(MarkerAnimation markerAnimation) {
        this.markerAnimation = markerAnimation;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public float getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(float animationDuration) {
        this.animationDuration = animationDuration;
    }

    public String getDirectionJson() {
        return directionJson;
    }

    public void setDirectionJson(String directionJson) {
        this.directionJson = directionJson;
    }

    public String getRoadJson() {
        return roadJson;
    }

    public void setRoadJson(String roadJson) {
        this.roadJson = roadJson;
    }

    public List<LatLng> getLatLngArray() {
        return latLngArray;
    }

    public void setLatLngArray(List<LatLng> latLngArray) {
        this.latLngArray = latLngArray;
    }

    public int getAnimationCounter() {
        return animationCounter;
    }

    public void setAnimationCounter(int animationCounter) {
        this.animationCounter = animationCounter;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }
}
