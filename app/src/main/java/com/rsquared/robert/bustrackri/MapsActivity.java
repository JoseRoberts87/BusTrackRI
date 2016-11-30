package com.rsquared.robert.bustrackri;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RealTimePositionRequestListener, ScheduledPositionRequestListener,  LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static LatLng MIDDLE_LOCATION = null;
    private int latLngIndex = 0;
    private boolean isRunnablePosted = false;
    private String route_id = "";
    private long POSITION_REQUEST_DELAY = 15;
    private boolean requestedRealData = false;
    private boolean requestedScheduledata = false;
    private Set<MarkerController> markerControllerSet;
    private Handler handler = new Handler();
    private ScheduledExecutorService scheduledExecutorServiceRealTime;
    private ScheduledExecutorService scheduledExecutorServiceScheduled;
    private String directionURL;
    private String apiResults = "";
    private String snapToRoadURL;
    boolean initializeRealTime = true;
    boolean initializeScheduled = true;
    final private Context context = this;
    private boolean isRawDataDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize variables and data calls and service calls
        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mapFragment.getMapAsync(this);
    }

    private void init() {
        // initialize global variables and request data and services
        route_id = getUrlNumber(getFormedUrl());
        markerControllerSet = new HashSet<>();
    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        Toast.makeText(this,
                marker.getTitle() +
                        " has been clicked " + /*clickCount*/ " times.",
                Toast.LENGTH_SHORT).show();

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;
            String url = getFormedUrl();
            setMyLocation();
            setRoutePath(url);
            setMapInfo(url);
            requestPositionData(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPositionData(final boolean initialize) {

        // request real time data from RIPTA API URL
        scheduledExecutorServiceRealTime = Executors.newScheduledThreadPool(1);

        Runnable realTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    requestRealTimePosition(initializeRealTime);
                    String threadName = Thread.currentThread().getName();
                    Log.i("requestRealTimePosition", "Done Thread: " + threadName);
                    Log.i("requestRealTimePosition", "JSON to be printed.... **** -----");
                    try {
                        this.finalize();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } else {
                    isRawDataDisplayed = true;
                    // display that real time is not available because there is no internet connection
                    // might want to remove this call and make a listener for when connectivity changes
                    // for now use this sheat
                }
            }
        };
        scheduledExecutorServiceRealTime.scheduleWithFixedDelay(realTimeRunnable, POSITION_REQUEST_DELAY, POSITION_REQUEST_DELAY, TimeUnit.SECONDS);
        scheduledExecutorServiceRealTime.submit(realTimeRunnable);
        requestedRealData = true;

        // Getting data from google_transit zip files
        scheduledExecutorServiceScheduled = Executors.newScheduledThreadPool(1);

        isRawDataDisplayed = !isConnected();
        Runnable scheduledRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRawDataDisplayed) {
                    requestScheduledPosition(initializeScheduled);
                    String threadName = Thread.currentThread().getName();
                    Log.i("ScheduledPosition", "Thread: " + threadName);
                    Log.i("ScheduledPosition", "JSON to be printed.... **** ");
                    try {
                        this.finalize();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } else {
                    // Do not display and whatever....
                    // maybe in the future create a class to manage changes on isRawDataDisplayed
                    // to then decide if execute the scheduledExecutorServiceScheduled
                }
            }
        };
        scheduledExecutorServiceScheduled.scheduleWithFixedDelay(scheduledRunnable, POSITION_REQUEST_DELAY, POSITION_REQUEST_DELAY, TimeUnit.SECONDS);
        scheduledExecutorServiceScheduled.submit(scheduledRunnable);
        requestedScheduledata = true;
    }

    /**
     * Checks to see if device has internet connection
     * @return
     */
    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Request Real time vehicla position from RIPTA API http://realtime.ripta.com:81/api/...
     * @param initialize
     */
    private void requestRealTimePosition(boolean initialize) {
        try {
            new RealTimePositionRequest(this, getString(R.string.real_time_url), this.getBaseContext(), initialize, route_id).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRealTimePositionRequestStart() {
        // Do stuff whatever you want or need
    }

    @Override
    public void onRealTimePositionRequestSuccess(List<VehiclePosition> vehiclePositionList, boolean initialize) {
        createScheduledMarkerController(vehiclePositionList, initialize);
        if (initializeRealTime) {
            initializeRealTime = false;
        }
    }

    @Override
    public void onRealTimeConnectionFailure(String failureMsg) {
        // show message that no internet connection and show scheduled data
        isRawDataDisplayed = true;
        Toast.makeText(this, "Real Time Not Available\n" + failureMsg, Toast.LENGTH_LONG).show();
    }

    /**
     * Get Vehicle Position from google_transit.zip files
     * @param initialize
     */

    private void requestScheduledPosition(boolean initialize) {
        String fileName = "route_" + route_id;
        int resourceId = this.getResources().getIdentifier(fileName, "raw", this.getPackageName());
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        try {
            new ScheduledPositionRequest(this, inputStream, initialize).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScheduledPositionRequestStart() {
        // Do stuff whatever you want or need
    }

    @Override
    public void onScheduledPositionRequestSuccess(List<VehiclePosition> vehiclePositionList, boolean initialize) {
        createScheduledMarkerController(vehiclePositionList, initialize);
        if(initializeScheduled){
            initializeScheduled = false;
        }
    }

    @Override
    public void onScheduledPositionFailure(String failureMsg) {
        // show message that schedule not available
        isRawDataDisplayed = false;
        Toast.makeText(this, "Scheduled Time Not Available\n" + failureMsg, Toast.LENGTH_LONG).show();
    }

    /**
     * This method will take the vehicle position list and create markerControllers
     * Then it will set the markerController into Global MarkerControllerSet
     * @param vehiclePositionList
     * @param initialize
     */
    private void createScheduledMarkerController(List<VehiclePosition> vehiclePositionList, boolean initialize) {

        MarkerController markerContoller;
            for(VehiclePosition vehiclePosition: vehiclePositionList){
                // Get vehicle information to create MarkerController
                String stopName = vehiclePosition.getStopName();
                String startTime = vehiclePosition.getStartTime();
                int stopSequence = vehiclePosition.getStopSequence();
                String tripId = vehiclePosition.getTripId();
                LatLng latLng = vehiclePosition.getLatLng();
                long timeStamp = System.currentTimeMillis();

                if(initialize) {
                    // Create Brand New MarkerController to add to Global markerControllerSet
                    MarkerAnimation markerAnimation = new MarkerAnimation();
                    MarkerOptions markerOptions = createBusMarkerOptionDefault(latLng);
                    Marker marker = mMap.addMarker(markerOptions);
                    markerContoller = new MarkerController(marker, tripId, latLng, timeStamp, markerOptions, markerAnimation, 30000, false);
                    markerControllerSet.add(markerContoller);
                }else{
                    // Get old markerController by Id from MarkerControllerSet
                    markerContoller = getMarkerControllerById(tripId);

                    long markerControllerTimeStamp = markerContoller.getTimeStamp();
                    long animationDuration = markerControllerTimeStamp - timeStamp;

                    markerContoller.setLatLng(latLng);
                    markerContoller.setAnimationDuration(animationDuration);

                    markerControllerSet.add(markerContoller);
                }
            }
    }

    /**
     * Animates a marker in the map based on new latLng and timestamp
     * @param markerController
     */
    private void animateMarkerController(final MarkerController markerController){
        final MarkerAnimation markerAnimation = markerController.getMarkerAnimation();
        final Marker marker = markerController.getMarker();
        final float postDuration = markerController.getAnimationDuration();


        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int animationCounter = markerController.getAnimationCounter();
                markerAnimation.animateMarkerToGB(marker, markerController.getLatLngArray().get(animationCounter), new LatLngInterpolator.Linear(), markerController);
                if (markerController.getAnimationCounter() < markerController.getLatLngArray().size()) {
                    handler.postDelayed(this, (long) postDuration - 150);
                    if (markerController.getAnimationCounter() < markerController.getLatLngArray().size() - 1) {
                        markerController.setAnimationCounter(markerController.getAnimationCounter() + 1);
                    }
                }
            }
        });
    }

      /*  private Map<String, List<String>> setBusInfoListMap() {
        try {
            List<String> busInfoList;
            JSONParser jsonParser = new JSONParser();
            String jsonFile = "";
            if (!textResult.isEmpty()) {
                jsonFile = textResult;
            }
            String jsonField = getJsonField("route_id", route_id);

            String rawFile = "";
//            if (!jsonFile.contains(jsonField)) {
//                rawFile = readRawFile("route_files" + route_id);
                jsonFile = createJson(rawFile);

//            }

            if(!jsonFile.isEmpty()) {
                JSONObject obj = (JSONObject) jsonParser.parse(jsonFile);
                JSONArray jsonArray = (JSONArray) obj.get("entity");
                busInfoListMap = new HashMap<>();
                busIdList = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    busInfoList = new ArrayList<>();
                    JSONObject entityJsonObject = (JSONObject) jsonArray.get(i);
                    JSONObject vehicleJsonObject = (JSONObject) entityJsonObject.get("vehicle");
                    JSONObject tripJsonObject = (JSONObject) vehicleJsonObject.get("trip");
                    String routeIdJsonObject = (String) tripJsonObject.get("route_id");
                    if (routeIdJsonObject.equalsIgnoreCase(route_id)) {
                        JSONObject positionJSONObject = (JSONObject) vehicleJsonObject.get("position");
                        JSONObject vehicleVehicleObject = (JSONObject) vehicleJsonObject.get("vehicle");
                        if (!busInfoList.contains(vehicleVehicleObject.get("label"))) {
                            double latitude = (double) positionJSONObject.get("latitude");
                            double longitude = (double) positionJSONObject.get("longitude");
                            long timestamp = (long) vehicleJsonObject.get("timestamp");
                            String stopId = (String) vehicleJsonObject.get("stop_id");
                            String startTime = (String) tripJsonObject.get("start_time");
                            String tripId = (String) tripJsonObject.get("trip_id");
                            double bearing = (double) positionJSONObject.get("bearing");
                            String label = (String) vehicleVehicleObject.get("label");
                            busInfoList.add(String.valueOf(latitude));
                            busInfoList.add(String.valueOf(longitude));
                            busInfoList.add(String.valueOf(timestamp));
                            busInfoList.add(String.valueOf(stopId));
                            busInfoList.add(String.valueOf(startTime));
                            busInfoList.add(String.valueOf(tripId));
                            busInfoList.add(String.valueOf(bearing));
                            busInfoList.add(String.valueOf(label));
                            busIdList.add(label);
                            busInfoListMap.put(label, busInfoList);
                            final LatLng latLng = new LatLng(latitude, longitude);
                            Location newLocation = new Location(String.valueOf(latLng));
                            MarkerOptions markerOptions;
                            final Marker marker;
                            final MarkerController markerController;
                            final MarkerAnimation markerAnimation;
                            if (doesMarkerExist(label)) {
                                marker = getMarkerById(label);
                                markerController = getMarkerControllerById(label);

                                if (hasMarkerLatLngChanged(markerController, latLng)) {
                                    long oldTimeStamp = markerController.getTimeStamp();
                                    long newTimeStamp = System.currentTimeMillis();
                                    markerController.setTimeStamp(newTimeStamp);
                                    long animationDuration = newTimeStamp - oldTimeStamp;

//                                float animationDuration1000 = animationDuration/1000;
                                    String origin = markerController.getLatLng().latitude + "," + markerController.getLatLng().longitude;
                                    String destination = latLng.latitude + "," + latLng.longitude;


                                    // Polyline animation
//                                }
//                                Log.i("polylines" , "polylines = " + polylinesArray);
//                                getNearestRoadLocation(latLng);
                                    markerAnimation = getMarkerAnimationById(label);
                                    markerController.setLatLng(latLng);
                                    markerController.setLocation(newLocation);
                                    markerController.setRealTime(true);
//                                markerController.setTimeStamp(timestamp);
                                    markerController.setStopId(stopId);
                                    markerController.setBearing(bearing);
                                    markerController.setTripId(tripId);
                                    markerController.setStartTime(startTime);

//                                markerAnimation.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Linear(), markerController);

                                    // getting polylines from the direction services and adding the lines to the screen
                                    pullDirectionAPIData(origin, destination, MAPConstants.OUTPUT_FORMAT_JSON, MAPConstants.TRAVEL_MODES_TRANSIT,
                                            MAPConstants.TRANSIT_MODE_BUS, MAPConstants.DEPARTURE_TIME_NOW, MAPConstants.TRANFFIC_MODEL_BEST_GUEST, markerController);
                                    List<String> polylineArray = getDirectionJSONPolyLine(markerController);

                                    pullRoadAPIData(origin, destination, markerController);
//                                for(String polyline: polylinesArray){
                                    PolylineOptions polylineOptions = new PolylineOptions().width(9).color(Color.RED);
                                    List<LatLng> latLngoAnimate = new ArrayList<>();
                                    for (String polyline : polylineArray) {
                                        List<LatLng> latLngList = PolyUtil.decode(polyline);
                                        latLngoAnimate = PolyUtil.decode(polyline);
                                        Log.i("polylines", "polylines = " + polyline);

                                        for (LatLng latLngPolyline : latLngList) {
                                            polylineOptions.add(latLngPolyline);
                                            Log.i("polylines", "latLngPolyline = " + latLngPolyline + " Added!!! ");
                                        }
                                    }
                                    latLngoAnimate.add(latLng);  // Added this afterwards..... TODO check this sheat
                                    animationDuration = animationDuration / latLngoAnimate.size();
                                    final float postDuration = animationDuration;

                                    markerController.setAnimationDuration(animationDuration);

//                                for(LatLng latLngAn: latLngoAnimate){
                                    markerController.setLatLngArray(latLngoAnimate);
                                    markerController.setAnimationCounter(0);
                                    final Handler handler = new Handler();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            int animationCounter = markerController.getAnimationCounter();
                                            markerAnimation.animateMarkerToGB(marker, markerController.getLatLngArray().get(animationCounter), new LatLngInterpolator.Linear(), markerController);
                                            if (markerController.getAnimationCounter() < markerController.getLatLngArray().size()) {
                                                handler.postDelayed(this, (long) postDuration - 150);
                                                if (markerController.getAnimationCounter() < markerController.getLatLngArray().size() - 1) {
                                                    markerController.setAnimationCounter(markerController.getAnimationCounter() + 1);
                                                }
                                            }
                                        }
                                    });

//                                }
//                                markerAnimation.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Linear(), markerController);


//                                    polylineOptions.addAll(latLngList);
                                    mMap.addPolyline(polylineOptions);


                                    Log.i("MarkerAnimation", "Bus Label: " + markerController.getMarkerId() + ", was animated from laLng = " + marker.getPosition() + ", to latLng = " + latLng);
                                }
                            } else {
                                markerOptions = createBusMarkerOptionDefaultRealTime(latLng, label);
                                marker = mMap.addMarker(markerOptions);
                                markerAnimation = new MarkerAnimation();
                                markerController = new MarkerController(marker, label, latLng, System.currentTimeMillis(), markerOptions, markerAnimation, System.currentTimeMillis(), true);
                                markerControllerSet.add(markerController);
//                            markerAnimation.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Linear(), markerController);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return busInfoListMap;
    }
*/
    private class DirectionServiceTask extends AsyncTask<MarkerController, Void, String> {
        @Override
        protected String doInBackground(MarkerController... params) {
            URL textUrl;
            String result = "";
            try {
                textUrl = new URL(directionURL);
                BufferedReader bufferReader = new BufferedReader(
                        new InputStreamReader(textUrl.openStream()));
                String stringBuffer;
                String stringText = "";
                while((stringBuffer = bufferReader.readLine()) != null) {
                    stringText += stringBuffer;
                }
                bufferReader.close();
                result = stringText;
                params[0].setDirectionJson(result);
                apiResults = result;
                Log.i("directionResults", "directionResults = " + apiResults);
                return result;
            } catch(MalformedURLException e) {
                e.printStackTrace();
                apiResults = e.toString();
            } catch(IOException e) {
                e.printStackTrace();
                apiResults = e.toString();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // do whatever with results
        }
    }

    // this is not implemented yet
    private class RoadServiceTask extends AsyncTask<MarkerController, Void, String> {
        @Override
        protected String doInBackground(MarkerController... params) {
            URL textUrl;
            String result = "";
            try {
                textUrl = new URL(directionURL);
                BufferedReader bufferReader = new BufferedReader(
                        new InputStreamReader(textUrl.openStream()));
                String stringBuffer;
                String stringText = "";
                while((stringBuffer = bufferReader.readLine()) != null) {
                    stringText += stringBuffer;
                }
                bufferReader.close();
                result = stringText;
                params[0].setRoadJson(result);
                Log.i("directionResults", "directionResults = " + apiResults);
                return result;
            } catch(MalformedURLException e) {
                e.printStackTrace();
                apiResults = e.toString();
            } catch(IOException e) {
                e.printStackTrace();
                apiResults = e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // do whatever with results

        }
    }

    private void pullDirectionAPIData(String Origin, String destination, String outputFormat, String travelMode, String transitMode, String departureTime, String trafficModel, MarkerController markerController){
        String apiKey = getString(R.string.google_maps_key);

        /*String url =  "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + "origin=" + Origin
                + "&destination=" + destination + "&key=" + getString(R.string.google_maps_key)+ "&mode=" + travelMode + "&traffic_model=" + trafficModel
                + "&transit_mode=" + transitMode + "&departure_time=" + departureTime;
        */
        String url =  "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + "origin=" + Origin
                + "&destination=" + destination + "&departure_time=" + departureTime;
        directionURL = url;
        Log.i("getDirectionsAPI", " The url to call to Googles Dirrection API is: " + url);

        new DirectionServiceTask().execute(markerController);
    }

    private void pullRoadAPIData(String Origin, String destination, MarkerController markerController ){
        String apiKey = getString(R.string.google_maps_key);
        /*String url =  "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + "origin=" + Origin
                + "&destination=" + destination + "&key=" + getString(R.string.google_maps_key)+ "&mode=" + travelMode + "&traffic_model=" + trafficModel
                + "&transit_mode=" + transitMode + "&departure_time=" + departureTime;
        */
;        String url =  "https://roads.googleapis.com/v1/snapToRoads?path=" + Origin + "|" + destination + "&key=" + apiKey;
        snapToRoadURL = url;
        Log.i("getRoadAPI", " The url to call to Googles Dirrection API is: " + url);
    }

    private boolean hasMarkerLatLngChanged(MarkerController markerController, LatLng latLng) {
        LatLng oldLatLng = markerController.getLatLng();
//        boolean equalsign = oldLatLng == latLng;    cannot use == to compare
        boolean equalfunction = oldLatLng.equals(latLng);
        return !equalfunction;
    }

    private MarkerAnimation getMarkerAnimationById(String markerId) {
        MarkerAnimation markerAnimation = null;

        List<MarkerController> markerControllerList = new ArrayList(markerControllerSet);

        for(int i = 0; i < markerControllerList.size(); i++){
            MarkerController markerController = markerControllerList.get(i);
            if(markerController.isSameMarkerId(markerId)){
                markerAnimation = markerController.getMarkerAnimation();
            }
        }
        return markerAnimation;
    }

    private boolean doesMarkerExist(String markerId){
        List<MarkerController> markerControllerList = new ArrayList(markerControllerSet);
        for(int i = 0; i < markerControllerList.size(); i++){
            if(markerControllerList.get(i).isSameMarkerId(markerId)){
                return true;
            }
        }
        return false;
    }

    private Marker getMarkerById(String markerId){
//        Marker marker = null;

        List<MarkerController> markerControllerList = new ArrayList(markerControllerSet);

        for(int i = 0; i < markerControllerList.size(); i++){
            MarkerController markerController = markerControllerList.get(i);
            if(markerController.isSameMarkerId(markerId)){
                return markerController.getMarker();
            }
        }
        return null;
    }

    private MarkerController getMarkerControllerById(String markerId){
        MarkerController markerController = null;
        List<MarkerController> markerControllerList = new ArrayList(markerControllerSet);
        for(int i = 0; i < markerControllerList.size(); i++){
            if( markerControllerList.get(i).isSameMarkerId(markerId)){
                markerController = markerControllerList.get(i);
            }
        }
        return markerController;
    }

    private MarkerOptions createBusMarkerOptionDefault(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk))
                .snippet("This is bus " + route_id + " Snippet").title("Bus " + route_id + " Lat: " + latLng.latitude + ", Lng: " + latLng.longitude);
        return markerOptions;
    }

    private MarkerOptions createBusMarkerOptionDefaultRealTime(LatLng latLng, String busLabel){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk))
                .snippet("This is bus " + route_id + " Snippet").title("Bus:" + busLabel + " on Route: " + route_id + " Lat: " + latLng.latitude + ", Lng: " + latLng.longitude);
        return markerOptions;
    }

    private MarkerOptions createBusMarkerOptionWithSnippet(LatLng latLng, String snippet){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk))
                .snippet(snippet);
        return markerOptions;
    }

    private MarkerOptions createBusMarkerOptionWithTitle(LatLng latLng, String title){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk))
                .title(title);
        return markerOptions;
    }
    private MarkerOptions createBusMarkerOptionWithSnippetWithTitle(LatLng latLng, String snippet, String title){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk))
                .snippet(snippet).title(title);
        return markerOptions;
    }

    private List<LatLng> getListDecodedLatLng(String url) {
        List<LatLng> decodedLatLngList = null;
        List<String> arrayRoutePath = getMapInfoAndRoute(url, "var route_path");
        for (String routePath : arrayRoutePath) {
            if (!routePath.isEmpty()) {
                String route = getRouteString(routePath);
                while (route.contains("\\\\")) {
                    route = cleanBackSlash(route);
                }
                try {
                    decodedLatLngList = PolyUtil.decode(route);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return decodedLatLngList;
    }

    private void showToast(Context ctx) {
        Toast.makeText(ctx, "Hi!", Toast.LENGTH_SHORT).show();
    }


    private LatLng fileAPICoordinates(String url) {
        List<LatLng> decodedLatLngList = getListDecodedLatLng(url);
        return decodedLatLngList.get(latLngIndex);
    }

    private LatLng riptaAPICoordinates(String url) {
//        locationManager.requestLocationUpdates(GPS_PROVIDER, intervall, distance, (android.location.LocationListener) listener);
        return new LatLng(0, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;

            // other 'case' lines to check for other
            // permissions this app might request
    }

    private void setMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            // TODO Show rationale and request permission.
        }
        mMap.setMyLocationEnabled(true);
    }

    private String getUrlNumber(String url){
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    private String getFormedUrl() {
        String url = getIntent().getExtras().getString("url");
        String number = url.substring(0, url.indexOf(" "));
        return getString(R.string.url_ripta) + number;
    }

    private void setRoutePath(String url){
        LatLng latLongFirst = null;
        LatLng latLongLast = null;
        double latitude = 0;
        double longitude = 0;
        int totalLatLng = 0;
        List<String> arrayRoutePath = getMapInfoAndRoute(url, "var route_path");
        for(String routePath: arrayRoutePath) {
            if (!routePath.isEmpty()) {
                PolylineOptions polylineOptions = new PolylineOptions().width(4).color(Color.BLUE);
                String route = getRouteString(routePath);
                List<LatLng> decodedLatLng = null;
                while (route.contains("\\\\")) {
                    route = cleanBackSlash(route);
                }

                try {
                    decodedLatLng = PolyUtil.decode(route);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (decodedLatLng != null) {

//                    Log.i("decodedLatLng: ", decodedLatLng.toString());
                    for (LatLng latLng : decodedLatLng) {
                        if(latLng.latitude > 0){
                            latitude += latLng.latitude;
                        }else{
                            latitude -= latLng.latitude;
                        }
                        if(latLng.longitude > 0){
                            longitude += latLng.longitude;
                        }else{
                            longitude -= latLng.longitude;
                        }
                        totalLatLng++;
                        if(latLongFirst == null){
                            latLongFirst = latLng;
                        }
                        latLongLast = latLng;
                        polylineOptions.add(latLng);
                    }
                    if (decodedLatLng != null) {
                        mMap.addPolyline(polylineOptions);
                    }

                }
            }
        }
        if(MIDDLE_LOCATION == null){
            MIDDLE_LOCATION = new LatLng(latitude/totalLatLng, -longitude/totalLatLng);
        }

    }

    private List<String> getDirectionJSONPolyLine(MarkerController markerController){
        String polyLines = "";
        List<String> polyLinesArray = new ArrayList<>();
        
        
//        apiResults = "{\"geocoded_waypoints\":[{\"geocoder_status\":\"OK\",\"place_id\":\"EiozOS00NyBGdWx0b24gU3QsIFByb3ZpZGVuY2UsIFJJIDAyOTAzLCBVU0E\",\"types\":[\"street_address\"]},{\"geocoder_status\":\"OK\",\"place_id\":\"Ei42MS05OSBLZW5uZWR5IFBsYXphLCBQcm92aWRlbmNlLCBSSSAwMjkwMywgVVNB\",\"types\":[\"street_address\"]}],\"routes\":[{\"bounds\":{\"northeast\":{\"lat\":41.82495,\"lng\":-71.4113},\"southwest\":{\"lat\":41.824604,\"lng\":-71.41177}},\"copyrights\":\"Map data ©2016 Google\",\"legs\":[{\"distance\":{\"text\":\"180 ft\",\"value\":55},\"duration\":{\"text\":\"1 min\",\"value\":39},\"end_address\":\"61-99 Fulton St, Providence, RI 02903, USA\",\"end_location\":{\"lat\":41.824604,\"lng\":-71.41177},\"start_address\":\"39-47 Fulton St, Providence, RI 02903, USA\",\"start_location\":{\"lat\":41.82495,\"lng\":-71.4113},\"steps\":[{\"distance\":{\"text\":\"180 ft\",\"value\":55},\"duration\":{\"text\":\"1 min\",\"value\":39},\"end_location\":{\"lat\":41.824604,\"lng\":-71.41177},\"html_instructions\":\"Walk to 61-99 Fulton St, Providence, RI 02903, USA\",\"polyline\":{\"points\":\"}|g~FrozrLT\\\\f@t@FH\"},\"start_location\":{\"lat\":41.82495,\"lng\":-71.4113},\"steps\":[{\"distance\":{\"text\":\"180 ft\",\"value\":55},\"duration\":{\"text\":\"1 min\",\"value\":39},\"end_location\":{\"lat\":41.824604,\"lng\":-71.41177},\"html_instructions\":\"Head <b>southwest<\\/b> toward <b>Dorrance St<\\/b><div style=\\\"font-size:0.9em\\\">Destination will be on the right<\\/div>\",\"polyline\":{\"points\":\"}|g~FrozrLT\\\\f@t@FH\"},\"start_location\":{\"lat\":41.82495,\"lng\":-71.4113},\"travel_mode\":\"WALKING\"}],\"travel_mode\":\"WALKING\"}],\"traffic_speed_entry\":[],\"via_waypoint\":[]}],\"overview_polyline\":{\"points\":\"}|g~FrozrLdA|A\"},\"summary\":\"\",\"warnings\":[\"Walking directions are in beta.    Use caution – This route may be missing sidewalks or pedestrian paths.\"],\"waypoint_order\":[]}],\"status\":\"OK\"}";
        while(apiResults.isEmpty()){
            // do nothing...
        }


        if( markerController.getDirectionJson() == null){
            markerController.setDirectionJson(apiResults);
        }
        String directionJson = markerController.getDirectionJson();

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject directionJSONObject = (JSONObject) jsonParser.parse(directionJson);

            JSONArray routeJsonArray = (JSONArray) directionJSONObject.get("routes");
            for(int i = 0 ; i < routeJsonArray.size(); i++){
                JSONObject routeJSONObject = (JSONObject) routeJsonArray.get(i);
                JSONArray legsJSOJsonArray = (JSONArray) routeJSONObject.get("legs");
                for(int j = 0; j < legsJSOJsonArray.size(); j++){
                    JSONObject legJSONObject = (JSONObject) legsJSOJsonArray.get(j);

                    JSONObject durationJSObject = (JSONObject) legJSONObject.get("duration");
                    String durationText = (String) durationJSObject.get("text");
                    long durationValue = (long) durationJSObject.get("value");

                    JSONObject distanceJSObject = (JSONObject) legJSONObject.get("distance");
                    String distanceText = (String) distanceJSObject.get("text");
                    long distanceValue = (long) distanceJSObject.get("value");

                    JSONArray stepsJSONArray = (JSONArray) legJSONObject.get("steps");
                    for(int k = 0; k <stepsJSONArray.size(); k++){
                        JSONObject stepJSONObject = (JSONObject) stepsJSONArray.get(i);
                        JSONObject polylineJSONObject = (JSONObject) stepJSONObject.get("polyline");
                        String points = (String) polylineJSONObject.get("points");
//                        polyLinesArray.add(points);

                        JSONArray steps2JSONArray = (JSONArray) legJSONObject.get("steps");
                        for(int l = 0; l <steps2JSONArray.size(); l++) {
                            JSONObject step2JSONObject = (JSONObject) steps2JSONArray.get(l);
                            JSONObject polyline2JSONObject = (JSONObject) step2JSONObject.get("polyline");
                            String points2 = (String) polyline2JSONObject.get("points");
//                            polyLinesArray.add(points2);
                        }
                    }
                }
                JSONObject routeJsonObject = (JSONObject) routeJsonArray.get(0);
                JSONObject overviewPolyline = (JSONObject) routeJsonObject.get("overview_polyline");
                String points = (String) overviewPolyline.get("points");
                polyLines = points;
                polyLinesArray.add(polyLines);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return polyLinesArray;
    }



    private void setMapInfo(String url){
        List<String> arrayMapInfo = getMapInfoAndRoute(url, "var stops");
        double latitude = 0;
        double longitude = 0;
        int totalLatLng = 0;
        LatLng latLongFirst = null;
        LatLng latLongLast = null;
        for(String mapInfo: arrayMapInfo){
            if(!mapInfo.isEmpty()){
                String stopName = getStopName(mapInfo);
                LatLng latLng = getMarkerLatLng(mapInfo);
                if(latLongFirst == null){
                    latLongFirst = latLng;
                }
                String markerName = getMarkerName(mapInfo);
                if(latLng != null){
                    mMap.addMarker(new MarkerOptions().position(latLng).title(markerName).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small)));
                    latitude = latitude + latLng.latitude;
                    longitude = longitude - latLng.longitude;
                    totalLatLng++;
                }
                if(latLng != null) {
                    latLongLast = latLng;
                }
            }
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(MIDDLE_LOCATION,11);
        mMap.moveCamera(cameraUpdate);
        MIDDLE_LOCATION = null;
    }

    private List<String> getMapInfoAndRoute(String url, String value) {

        List<String> mapInfoAndRoute = new ArrayList<>();
        try {
            InputStream iS = getResources().openRawResource(R.raw.bus_map_info_and_route);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line;
            boolean endOfValue = false;
            while ((line = reader.readLine()) != null && !endOfValue) {
                if (line.contains(url)) {
                    while((line = reader.readLine()) != null&& !endOfValue) {
                        if (line.contains(value)) {
                            mapInfoAndRoute.add(line);
                            while((line = reader.readLine()) != null && !endOfValue) {
                                if(line.contains("End Route Info") || line.contains("]]")){
                                    endOfValue = true;
                                }else{
                                    mapInfoAndRoute.add(line);
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mapInfoAndRoute;
    }

    private String cleanBackSlash(String route){
        return route.replace("\\\\", "\\");
    }

    private String getRouteString(String routePath){
        int startIndex = routePath.indexOf('"');
        routePath = routePath.substring(startIndex + 1);
        int endIndex = routePath.indexOf('"');
        routePath = routePath.substring(0, endIndex);
        return routePath;
    }

    private String getMarkerName(String markerName){
        int startIndex = markerName.indexOf('"');
        markerName = markerName.substring(startIndex + 1);
        int endIndex = markerName.indexOf('"');
        markerName = markerName.substring(0, endIndex);
        return markerName;
    }

    private LatLng getMarkerLatLng(String markerLatLng){
        double latitude = Double.parseDouble(getMarkerNumber(markerLatLng));

        int startIndex = markerLatLng.indexOf(',');
        markerLatLng = markerLatLng.substring(startIndex + 1);
        int secondIndex = markerLatLng.indexOf(',');
        markerLatLng = markerLatLng.substring(secondIndex + 1);
        int lastIndex = markerLatLng.indexOf(',');
        markerLatLng = markerLatLng.substring(0, lastIndex);
        double longitude = Double.parseDouble(markerLatLng);
        LatLng latLng = null;
        if(latitude != 0 && longitude != 0) {
            latLng = new LatLng(latitude,longitude);
        }
        return latLng;
    }

    private String getMarkerNumber(String latitude){
        int startIndex = latitude.indexOf(',');
        latitude = latitude.substring(startIndex + 1);
        int endIndex = latitude.indexOf(',');
        latitude = latitude.substring(0, endIndex);
        return latitude;
    }

    private String getStopName(String stop){
        stop = stop.substring( stop.indexOf("\"") + 1, stop.indexOf(",") - 1);
        return stop;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("LocationChanged", "Location has chnged to: " + location.getLatitude() + ", " + location.getLongitude());
    }

    public BufferedReader getBufferReaderFromUrl(String apiURL) {
        BufferedReader bufferReaderFromUrl = null;
        try {
            URL oracle = new URL(apiURL);
            bufferReaderFromUrl = new BufferedReader(new InputStreamReader(oracle.openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferReaderFromUrl;
    }

    // Activity management to stop and run background threads

    @Override
    protected void onStart() {
        super.onStart();
//        startThreads();
    }

    @Override
    protected void onRestart() {
//        super.onRestart();
        startThreads();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startThreads();
    }

    @Override
    protected void onStop() {
        super.onStop();
        destroyThreads();

    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyThreads();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyThreads();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isRunnablePosted) {
            startThreads();
        } else if(!hasFocus){
            destroyThreads();
        }
    }

    private void startThreads() {


    }


    private void destroyThreads() {
        if(handler != null){
//            handler.removeCallbacks(runnable);
        }
        if(scheduledExecutorServiceRealTime != null){
            scheduledExecutorServiceRealTime.shutdownNow();
        }
        if(scheduledExecutorServiceScheduled != null){
            scheduledExecutorServiceScheduled.shutdownNow();
        }
        Log.i("destroyThreads", "Handler calls removed");
    }
}
