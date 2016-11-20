package com.rsquared.robert.bustrackri;

/**
 * Created by R^2 on 11/19/2016.
 */

public class MAPConstants {
    // Vehicle info constant
    public static final int LATITUDE = 0;
    public static final int LONGITUDE = 1;
    public static final int STOP_ID = 3;
    public static final int START_TIME = 3;
    public static final int TRIP_ID = 3;
    public static final int BEARING = 3;

    /** Constants for Google Maps Destination API **/

    // Output Format
    public static final String OUTPUT_FORMAT_JSON = "json";
    public static final String OUTPUT_FORMAT_XML = "xml";

    // Departure Time
    public static final String DEPARTURE_TIME_NOW = "now";



    // Travel Modes
    public static final String TRAVEL_MODES_DRIVING = "driving";
    public static final String TRAVEL_MODES_WALKING = "walking";
    public static final String TRAVEL_MODES_BICYCLING = "bicycling";
    public static final String TRAVEL_MODES_TRANSIT = "transit";

    // Traffic model
    public static final String TRANFFIC_MODEL_BEST_GUEST = "best_guess";
    public static final String TRANFFIC_MODEL_PESSIMISTIC = "pessimistic";
    public static final String TRANFFIC_MODEL_OPTIMISTIC = "optimistic";

    // Transit mode
    public static final String TRANSIT_MODE_BUS = "bus";
    public static final String TRANSIT_MODE_SUBWAY = "subway";
    public static final String TRANSIT_MODE_TRAIN = "train";
    public static final String TRANSIT_MODE_TRAM = "tram";
    public static final String TRANSIT_MODE_RAIL = "rail";  // this is equivalent to transit_mode = train|tram|subway



}
