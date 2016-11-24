package com.rsquared.robert.bustrackri;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.maps.model.Polyline;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();

    private GoogleMap mMap;
    private static LatLng MIDDLE_LOCATION = null;
    private int latLngIndex = 0;
    private boolean isRunnablePosted = false;
    private String route_id = "";
    private long delayTIme = 20000;
    private long DELAY_URL_TRHEAD = 9;
    private long DELAY_JSON_THREAD = 6500;
    private boolean attemptedTosetJson = false;
    private Map<String, List<String>> busInfoListMap;
    private List<String> busIdList;
    private List<LatLng> mDecodedLatLng;
    private Set<MarkerController> markerControllerSet;
    private String apiURL = "";
    private Handler handler = new Handler();
    private String textResult = "";
    private ScheduledExecutorService scheduledExecutorService;
    private Runnable runnable;
    private String directionURL;
    private String apiResults = "";
    private String snapToRoadURL;
    private String snapToRoadResults = "";
    protected GoogleApiClient mGoogleApiClient;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize variables and service calls
        init();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mapFragment.getMapAsync(this);


    }



    private void init() {
        apiURL = getString(R.string.real_time_url);
        route_id = getUrlNumber(getFormedUrl());
        markerControllerSet = new HashSet<>();
        pullRealTimeData();
//        if(!jsonFile.contains(jsonField)) {
//            jsonFile = readRawFile("vehicleposition" + fileNumberString);
//        }


//        if()
    }

    private void testAnimation(boolean b, Marker marker) {
        try {
            MarkerOptions markerOptions;
//            Marker marker = null;
            MarkerController markerController = null;
            MarkerAnimation markerAnimation;

            LatLng latLng = new LatLng(41.8249528, -71.4113005);
            LatLng newLatLng;
            if (b){
                double latitude = marker.getPosition().latitude - .0039;
                double longitude = marker.getPosition().longitude - .000137;
                newLatLng = new LatLng(latitude, longitude);
                String origin = latLng.latitude + ","+latLng.longitude;
                String destination = newLatLng.latitude + "," + newLatLng.longitude;

                pullDirectionAPIData(origin, destination, MAPConstants.OUTPUT_FORMAT_JSON, MAPConstants.TRAVEL_MODES_TRANSIT,
                        MAPConstants.TRANSIT_MODE_BUS, MAPConstants.DEPARTURE_TIME_NOW, MAPConstants.TRANFFIC_MODEL_BEST_GUEST,  markerController);
                List<String> polylineList = getDirectionJSONPolyLine(markerController);
//                                for(String polyline: polylinesArray){
                PolylineOptions polylineOptions = new PolylineOptions().width(9).color(Color.RED);

                for(String polyline: polylineList) {
                    List<LatLng> latLngList = PolyUtil.decode(polyline);
                    for (LatLng latLngPolyline : latLngList) {
                        polylineOptions.add(latLngPolyline);
                    }

//                                    polylineOptions.addAll(latLngList);
                }
                Log.i("polylines" , "polylines = " + polylineList);

                mMap.addPolyline(polylineOptions);

            }else{
                newLatLng = new LatLng(41.8249528, -71.4113005);
            }


            markerOptions = createBusMarkerOptionDefaultRealTime(latLng, "test_bus");
            marker = mMap.addMarker(markerOptions);
            markerAnimation = new MarkerAnimation();
            markerController = new MarkerController(marker, "test_bus", latLng, System.currentTimeMillis(), markerOptions, markerAnimation, 30000, true);
            markerControllerSet.add(markerController);

            markerAnimation.animateMarkerToGB(marker, newLatLng, new LatLngInterpolator.Linear(), markerController);



            mMap.setOnMarkerClickListener(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /** Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {


        testAnimation(true, marker);
        // Retrieve the data from the marker.
//        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
//        if (clickCount != null) {
//            clickCount = clickCount + 1;
//            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + /*clickCount*/ " times.",
                    Toast.LENGTH_SHORT).show();

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }


    private void pullRealTimeData() {

        scheduledExecutorService = Executors.newScheduledThreadPool(1);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                realTimeData();
                attemptedTosetJson = true;
                String threadName = Thread.currentThread().getName();
                Log.i("manageURLGET","Done Thread: " + threadName);
                Log.i("textResult","JSON: " + textResult);
                try {
                    this.finalize();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        };
        scheduledExecutorService.scheduleWithFixedDelay(runnable, DELAY_URL_TRHEAD, DELAY_URL_TRHEAD, TimeUnit.SECONDS);
        scheduledExecutorService.submit(runnable);
    }

    private void manageMarkerThreads(){
        while(!attemptedTosetJson){
            // Wait
        }
//        setBusInfoListMap(route_id);
        runnable = new Runnable() {
            @Override
            public void run() {
                setBusInfoListMap(route_id);
                Log.i("ManageMarkerThread", "MarkerControllerSet = " + markerControllerSet);
                handler.postDelayed(this, DELAY_JSON_THREAD);
            }
        };
        handler.post(runnable);
    }

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
            handler.removeCallbacks(runnable);
        }
        if(scheduledExecutorService != null){
            scheduledExecutorService.shutdownNow();
        }
        Log.i("destroyThreads", "Handler calls removed");
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
            manageMarkerThreads();
            String url = getFormedUrl();
            setMyLocation();
            setRoutePath(url);
            setMapInfo(url);
//            testAnimation(false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void realTimeData(){
        URL textUrl;
        try {
            textUrl = new URL(apiURL);
            InputStreamReader inputStream = new InputStreamReader(textUrl.openStream());
            BufferedReader bufferReader = new BufferedReader(inputStream);
            String stringBuffer;
            String stringText = "";
            while((stringBuffer = bufferReader.readLine()) != null) {
                stringText += stringBuffer;
            }
            bufferReader.close();
            inputStream.close();
            textResult = stringText;
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private String getDirectionJSONFromURL(String url){
        URL textUrl;
        String json = "";
        try {
            textUrl = new URL(url);
            BufferedReader bufferReader = new BufferedReader(
                    new InputStreamReader(textUrl.openStream()));
            String stringBuffer;
            while((stringBuffer = bufferReader.readLine()) != null) {
                json += stringBuffer;
            }
            bufferReader.close();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    private Map<String, List<String>> setBusInfoListMap(String route_id) {
        List<String> busInfoList;
        JSONParser jsonParser = new JSONParser();
        String jsonFile = "";
        if(!textResult.isEmpty()){
            jsonFile = textResult;
        }
        String jsonField = getJsonField("route_id", route_id);
        if(!jsonFile.contains(jsonField)) {
            jsonFile = readRawFile("vehicleposition");
        }
        try {
            JSONObject obj =  (JSONObject) jsonParser.parse(jsonFile);
            JSONArray jsonArray = (JSONArray) obj.get("entity");
            busInfoListMap = new HashMap<>();
            busIdList = new ArrayList<>();
            for(int i = 0; i < jsonArray.size(); i++){
                busInfoList = new ArrayList<>();
                JSONObject entityJsonObject = (JSONObject) jsonArray.get(i);
                JSONObject vehicleJsonObject = (JSONObject) entityJsonObject.get("vehicle");
                JSONObject tripJsonObject = (JSONObject) vehicleJsonObject.get("trip");
                String routeIdJsonObject = (String) tripJsonObject.get("route_id");
                if(routeIdJsonObject.equalsIgnoreCase(route_id)) {
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
                        LatLng latLng = new LatLng(latitude, longitude);
                        Location newLocation = new Location(String.valueOf(latLng));
                        MarkerOptions markerOptions;
                        Marker marker;
                        MarkerController markerController;
                        MarkerAnimation markerAnimation;
                        if (doesMarkerExist(label)) {
                            marker = getMarkerById(label);
                            markerController = getMarkerControllerById(label);

                            if (hasMarkerLatLngChanged(markerController, latLng)) {
                                long oldTimeStamp = markerController.getTimeStamp();
                                long newTimeStamp = System.currentTimeMillis();
                                markerController.setTimeStamp(newTimeStamp);
                                long animationDuration = newTimeStamp - oldTimeStamp;

//                                float animationDuration1000 = animationDuration/1000;
                                String origin = markerController.getLatLng().latitude + ","+markerController.getLatLng().longitude;
                                String destination = latLng.latitude + "," + latLng.longitude;

                                // getting polylines from the direction services and adding the lines to the screen
                                pullDirectionAPIData(origin, destination, MAPConstants.OUTPUT_FORMAT_JSON, MAPConstants.TRAVEL_MODES_TRANSIT,
                                        MAPConstants.TRANSIT_MODE_BUS, MAPConstants.DEPARTURE_TIME_NOW, MAPConstants.TRANFFIC_MODEL_BEST_GUEST,  markerController);
                                List<String> polylineArray = getDirectionJSONPolyLine(markerController);

                                pullRoadAPIData(origin, destination, markerController);
//                                for(String polyline: polylinesArray){
                                PolylineOptions polylineOptions = new PolylineOptions().width(9).color(Color.RED);
                                for(String polyline: polylineArray) {
                                    List<LatLng> latLngList = PolyUtil.decode(polyline);
                                    Log.i("polylines", "polylines = " + polyline);

                                    for (LatLng latLngPolyline : latLngList) {
                                        polylineOptions.add(latLngPolyline);
                                        Log.i("polylines", "latLngPolyline = " + latLngPolyline + " Added!!! ");
                                    }
                                }
//                                    polylineOptions.addAll(latLngList);
                                mMap.addPolyline(polylineOptions);

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
                                markerController.setAnimationDuration(animationDuration);
                                markerAnimation.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Linear(), markerController);


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
        }catch (Exception e){
            e.printStackTrace();
        }
        return busInfoListMap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class DirectionServiceTask extends AsyncTask<MarkerController, Void, String> {

//        String textResult;

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

    private class RoadServiceTask extends AsyncTask<MarkerController, Void, String> {

//        String textResult;

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


    @Override
    public void onDirectionFinderStart() {
//        progressDialog = ProgressDialog.show(this, "Please wait.",
//                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
//        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
//            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
//            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    private void sendRequest() {
        String origin = "etOrigin.getText().toString()";
        String destination = "etDestination.getText().toString()";
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void pullDirectionAPIData(String Origin, String destination, String outputFormat, String travelMode, String transitMode, String departureTime, String trafficModel, MarkerController markerController){
        /*String url =  "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + "origin=" + Origin
                + "&destination=" + destination + "&key=" + getString(R.string.google_maps_key)+ "&mode=" + travelMode + "&traffic_model=" + trafficModel
                + "&transit_mode=" + transitMode + "&departure_time=" + departureTime;
        */
        String apiKey = getString(R.string.google_maps_key);
        String url =  "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + "origin=" + Origin
                + "&destination=" + destination +/* "&key=" + apiKey + */"&mode=" + travelMode + "&traffic_model=" + trafficModel
                + /*"&transit_mode=" + transitMode +*/ "&departure_time=" + departureTime;
        directionURL = url;
        Log.i("getDirectionsAPI", " The url to call to Googles Dirrection API is: " + url);

        new DirectionServiceTask().execute(markerController);

     /*   mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        MapsInitializer.initialize(this);
        mGoogleApiClient.connect();*/

//        return url;
    }

    private void pullRoadAPIData(String Origin, String destination, MarkerController markerController ){
        /*String url =  "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + "origin=" + Origin
                + "&destination=" + destination + "&key=" + getString(R.string.google_maps_key)+ "&mode=" + travelMode + "&traffic_model=" + trafficModel
                + "&transit_mode=" + transitMode + "&departure_time=" + departureTime;
        */
        String apiKey = getString(R.string.google_maps_key);
;        String url =  "https://roads.googleapis.com/v1/snapToRoads?path=" + Origin + "|" + destination + "&key=" + apiKey;
        snapToRoadURL = url;
        Log.i("getDirectionsAPI", " The url to call to Googles Dirrection API is: " + url);
//        String directionsJSON = getDirectionJSONFromURL(url);
//        Log.i("getDirections", "directionsJSON = " + directionsJSON);
        new RoadServiceTask().execute(markerController);
    }

    private void getNearestRoadLocation(LatLng latLng) {

        Path.Direction direction = Path.Direction.valueOf(String.valueOf(latLng));
        Log.i("getNearestRoadLocation", " direction = " + direction);
    }

    private boolean hasMarkerLatLngChanged(MarkerController markerController, LatLng latLng) {
        LatLng oldLatLng = markerController.getLatLng();
//        boolean equalsign = oldLatLng == latLng;    cannot use == to compare
        boolean equalfunction = oldLatLng.equals(latLng);
        return !equalfunction;
    }

    private boolean hasMarkerLocationChanged(MarkerController markerController, Location newLocation) {
        Location oldLocation = markerController.getLocation();
        boolean equalsign = oldLocation == newLocation;
        boolean equalfunction = oldLocation.equals(newLocation);
        return oldLocation != newLocation;
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


    private String getJsonField(String key, String value) {
        return "\""+key+"\":\""+value+"\"";
    }

    private String readAPIFile() {
        String apiFile = "";
        BufferedReader bufferedReader = getBufferReaderFromUrl(apiURL);
        String inputLine;
        try {
            while ((inputLine = bufferedReader.readLine()) != null) {
                apiFile += inputLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return apiFile;
    }

    private String readRawFile(String fileName) {
        String rawFile = "";
        /*int id = this.getResources().getIdentifier(fileName, "raw", this.getPackageName());
        try {
            InputStream iS = getResources().openRawResource(id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line;
            while ((line = reader.readLine()) != null) {
                rawFile += line + "\n";
            }
        }catch (IOException e) {
            e.printStackTrace();
        }*/
        return rawFile;
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
                mDecodedLatLng = decodedLatLng;
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

        String directionJson = markerController.getDirectionJson();
        if(directionJson == null){
            markerController.setDirectionJson(apiResults);
        } else {

            while (markerController.getDirectionJson().equals(apiResults)) {
                markerController.setDirectionJson(apiResults);
            }
        }

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject directionJSONObject = (JSONObject) jsonParser.parse(apiResults);

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
}
