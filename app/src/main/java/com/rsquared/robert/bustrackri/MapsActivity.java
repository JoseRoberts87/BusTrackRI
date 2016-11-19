package com.rsquared.robert.bustrackri;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.PolyUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private static LatLng MIDDLE_LOCATION = null;
//    private MarkerOptions markerOptions = null;
    final private Context context = this.getBaseContext();
    //    private Handler handler;
    private int latLngIndex = 0;
    private List<LatLng> decodedLatLngList;
    private boolean isRunnablePosted = false;
//    protected Location currentLocation;

    // Vehicle info constant
    public static final int LATITUDE = 0;
    public static final int LONGITUDE = 1;
    public static final int STOP_ID = 3;
    public static final int START_TIME = 3;
    public static final int TRIP_ID = 3;
    public static final int BEARING = 3;

    private int fileNumber = 0;
    private String route_id = "";
    private long delayTIme = 20000;
    private long DELAY_URL_TRHEAD = 17;
    private long DELAY_JSON_THREAD = 6500;
    private boolean attemptedTosetJson = false;
    private Map<String, List<String>> busInfoListMap;
    private Map<String, List<Marker>> markerMap;
    private MarkerManager markerManager;
    private MarkerManager.Collection markerManagerCollection;
    private List<String> busIdList;
//    private MarkerController markerController;
    private Set<MarkerController> markerControllerSet;
    private String apiURL = "";

    String textResult = "";


    private class MyTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            setJSONFromURL();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
            String newString = " ";
            newString = "do something";
            new MyTask().execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();

        mapFragment.getMapAsync(this);

        apiURL = getString(R.string.api_url);
//        new MyTask().execute();

        manageURLThread();
        String jsonFile = textResult;
    }

    private void init() {
        route_id = getUrlNumber(getFormedUrl());
        markerControllerSet = new HashSet<>();
        markerManager = new MarkerManager(mMap);
        markerManagerCollection = markerManager.newCollection();
//        setJSONFromURL();
//        setBusInfoListMap(route_id);
    }

    private Runnable runnable = new Runnable(){

        @Override
        public void run() {

        }
    };

    private void manageURLThread() {

        final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                setJSONFromURL();
//                setBusInfoListMap(route_id);
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
        setBusInfoListMap(route_id);
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                setBusInfoListMap(route_id);
                handler.postDelayed(this, DELAY_JSON_THREAD);
            }
        };
        handler.post(runnable);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        String url = getFormedUrl();
//        runUpdaterThread(url);
        if (!isRunnablePosted) {
            isRunnablePosted = new Handler().postDelayed(runnable, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isRunnablePosted) {
            isRunnablePosted = new Handler().postDelayed(runnable, 1000);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRunnablePosted) {
            new Handler().removeCallbacks(runnable);
            isRunnablePosted = false;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Handler handler = new Handler();
        if (hasFocus && !isRunnablePosted) {
            isRunnablePosted = handler.postDelayed(runnable, 1000);
        } else if(!hasFocus && isRunnablePosted){
            handler.removeCallbacks(runnable);
            isRunnablePosted = false;
        }
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
//            animateMarkers();
//            manageThreads(route_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateMarkers() {

        Thread thread = new Thread();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                setBusInfoListMap(route_id);
            }
        };


    }

    private void setJSONFromURL(){
        URL textUrl;

        try {
            textUrl = new URL(apiURL);
            BufferedReader bufferReader = new BufferedReader(
                    new InputStreamReader(textUrl.openStream()));
            String stringBuffer;
            String stringText = "";
            while((stringBuffer = bufferReader.readLine()) != null) {
                stringText += stringBuffer;
            }

            bufferReader.close();
            textResult = stringText;

        } catch(MalformedURLException e) {
            e.printStackTrace();
//                textResult = e.toString();
        } catch(IOException e) {
            e.printStackTrace();
//                textResult = e.toString();
        }
    }

    private void manageThreads(final String route_id) {
        while (!attemptedTosetJson) {
//            Log.i("ManageThread", "wait for setJSONFromURL() to start manageThreads: " + counter++);
        }

        int counter = 0;

        while (busInfoListMap.size() == 0) {
            Log.i("ManageThread", "wait for busInfoListMap to start manageThreads: " + counter++);
        }

        fileNumber++;
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
//                    int i = 0;
                    Marker marker = null;
                    MarkerAnimation markerAnimation = null;
                    MarkerOptions markerOptions = null;
                    Location currentLocation;


                    for(int i = 0; i < busInfoListMap.get(route_id).size(); i++) {
/*                        if(marker != null){
                            marker.remove();
                        }*/
                        LatLng latLng = new LatLng(Double.parseDouble(busInfoListMap.get(busIdList.get(i)).
                                get(LATITUDE)), Double.parseDouble(busInfoListMap.get(busIdList.get(i)).get(LONGITUDE)));
                        currentLocation = new Location(String.valueOf(latLng));
                        if(markerOptions == null){


/*                            LocationListener locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    Log.i("LocationListener", "Location was changed yeaahahahahahhahahahaha");
                                }
                            };
                            if (locationListener != null && currentLocation != null) {
                                locationListener.onLocationChanged(currentLocation);
                            }*/
                            markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk))
                                    .snippet("This is bus " + route_id + " Snippet").title("Bus " + route_id + " Lat: " + latLng.latitude + ", Lng: " + latLng.longitude);
                        }
                        if(marker == null) {
                            marker = mMap.addMarker(markerOptions);
                        }
                        if(markerAnimation == null) {
                            markerAnimation = new MarkerAnimation();
                        }

                        animateMarkers(marker, markerOptions, markerAnimation, currentLocation, i);
                        Log.i("animateMarker", "busInfoListMap: " + busInfoListMap + ". timestamp: " + System.currentTimeMillis());
                    }
                    handler.postDelayed(this, DELAY_JSON_THREAD);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        for(int i = 0; i < busInfoListMap.size(); i++) {
            handler.post(runnable);
        }
    }

    private void updateAPIFile(){
        URL textUrl;

        try {
            textUrl = new URL(apiURL);
            BufferedReader bufferReader = new BufferedReader(
                    new InputStreamReader(textUrl.openStream()));
            String stringBuffer;
            String stringText = "";
            while((stringBuffer = bufferReader.readLine()) != null) {
                stringText += stringBuffer;
            }

            bufferReader.close();
            textResult = stringText;
        } catch(MalformedURLException e) {
            e.printStackTrace();
//                textResult = e.toString();
        } catch(IOException e) {
            e.printStackTrace();
//                textResult = e.toString();
        }
    }

/*    private Map<String, List<List<String>>> getBusInfoMap(String route_id) {
        Map<String, List<List<String>>> busInfoMap = setBusInfoListMap(route_id);
        return busInfoMap;
    }*/

    private Map<String, List<String>> setBusInfoListMap(String route_id) {
//        Map<String, List<List<String>>> busInfoMap = new HashMap<>();

        List<String> busInfoList;
//        List<List<String>> busInfoListList = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        String fileNumberString = String.valueOf(fileNumber + 1);
        String jsonFile = "";
        if(!textResult.isEmpty()){
//            jsonFile = readAPIFile();
            jsonFile = textResult;
        }

        String jsonField = getJsonField("route_id", route_id);
        if(!jsonFile.contains(jsonField)) {
            jsonFile = readRawFile("vehicleposition" + fileNumberString);
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
//                    busInfoListList.add(busInfoList);
                        busIdList.add(label);
                        busInfoListMap.put(label, busInfoList);
//                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latitude, longitude));
//                    markerManagerCollection.addMarker(createBusMarkerOptionDefault(new LatLng(latitude, longitude)));
                        LatLng latLng = new LatLng(latitude, longitude);
                        Location newLocation = new Location(String.valueOf(latLng));

/*                    LocationListener locationListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i("LocationListener", "Location was changed yeaahahahahahhahahahaha");
                        }
                    };*/
//                    if (locationListener != null && markerController.location != null) {
//                        onLocationChanged(markerController.getLocation());
//                    }

//                    if (currentLocation == null) {
//                        currentLocation = newLocation;
//                    } else if(currentLocation != newLocation ) {
//                        currentLocation.setLatitude(latitude);
//                        currentLocation.setLongitude(longitude);
//                    }

                        MarkerOptions markerOptions;
                        Marker marker;
                        MarkerController markerController;
                        MarkerAnimation markerAnimation;

                        if (doesMarkerExist(label)) {

                            marker = getMarkerById(label);
                            markerController = getMarkerControllerById(label);

                            if (hasMarkerLatLngChanged(markerController, latLng)) {
                                markerAnimation = getMarkerAnimationById(label);
                                markerController.setLatLng(latLng);
                                markerController.setLocation(newLocation);
                                markerController.setRealTime(true);
                                markerController.setTimeStamp(timestamp);
                                markerController.setStopId(stopId);
                                markerController.setBearing(bearing);
                                markerController.setTripId(tripId);
                                markerController.setStartTime(startTime);
                                markerAnimation.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Linear());
                            }

                        } else {
                            markerOptions = createBusMarkerOptionDefault(latLng);
                            marker = mMap.addMarker(markerOptions);
                            markerAnimation = new MarkerAnimation();
                            markerAnimation.animateMarkerToGB(marker, latLng, new LatLngInterpolator.Linear());

                            markerController = new MarkerController(marker, label, latLng, timestamp, markerOptions, markerAnimation, true);
                            markerControllerSet.add(markerController);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return busInfoListMap;
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
        int id = this.getResources().getIdentifier(fileName, "raw", this.getPackageName());
        try {
            InputStream iS = getResources().openRawResource(id);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line;
            while ((line = reader.readLine()) != null) {
                rawFile += line + "\n";
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
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

    private void animateMarkers(Marker marker, MarkerOptions markerOptions, MarkerAnimation markerAnimation, Location currentLocation, int arrayIndex) {

        try {

//            List<String> busInfoList = busInfoListMap.get(route_id).get(arrayIndex);
            double latitude = Double.parseDouble(busInfoListMap.get(busIdList.get(arrayIndex)).get(LATITUDE));
            double longitude = Double.parseDouble(busInfoListMap.get(busIdList.get(arrayIndex)).get(LONGITUDE));

            double oldLatitude = 0;
            double oldLongitude = 0;

            long currenTimestamp = System.currentTimeMillis();
//          long timestamp = Integer.valueOf(busInfoList.get(TIMESTAMP));

//            delayTIme = oldTimestamp - currenTimestamp;

            LatLng latLngNew = new LatLng(latitude, longitude);
            /*if (markerOptions == null) {
                markerOptions.position(latLngNew).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk))
                        .snippet("This is bus " + route_id + " Snippet").title("Bus " + route_id + " Lat: " + latitude + ", Lng: " + longitude);
//            }
//            if(marker == null){
//
            }else{
                oldLatitude = marker.getPosition().latitude;
                oldLongitude = marker.getPosition().longitude;
//                marker.remove();

            }*/

/*            if(markerAnimation == null){
                String isnull = "true";
            }
            marker = mMap.addMarker(markerOptions);*/
            markerAnimation.animateMarkerToGB(marker, latLngNew, new LatLngInterpolator.Linear());

            marker.showInfoWindow();
            latLngIndex++;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*private void animateMarker(String url) {

        LatLng latLngOld;
        LatLng latLngNew;

        if (false *//*connect to Ripta*//*) {
            latLngNew = riptaAPICoordinates(url);
        } else {
            latLngNew = fileAPICoordinates(url);
        }

        if (markerOptions == null) {
            markerOptions = new MarkerOptions();
        }

        if (marker != null) {
//            marker.remove();
            latLngOld = marker.getPosition();

            MarkerAnimation markerAnimation = new MarkerAnimation();
            markerAnimation.animateMarkerToGB(marker, latLngNew, new LatLngInterpolator.Linear());
        } else {
            markerOptions = new MarkerOptions();
            markerOptions.position(latLngNew).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
            marker = mMap.addMarker(markerOptions);
            latLngOld = latLngNew;
        }



*//*        markerOptions.position(latLngNew).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
        marker = mMap.addMarker(markerOptions);
        *//*
        latLngIndex++;
    }
*/
    private LatLng fileAPICoordinates(String url) {
        List<LatLng> decodedLatLngList = getListDecodedLatLng(url);
        return decodedLatLngList.get(latLngIndex);
    }

/*    private void getGPSLocation(String url) {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }
//            ...
        };

        String GPS_PROVIDER = "";

        long intervall = 1212;
        float distance = 1222;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(GPS_PROVIDER, intervall, distance, (android.location.LocationListener) listener);
    }*/


    private LatLng riptaAPICoordinates(String url) {
//        locationManager.requestLocationUpdates(GPS_PROVIDER, intervall, distance, (android.location.LocationListener) listener);
        return new LatLng(0, 0);
    }

   /* private void runUpdaterThread(final String url) {

        Map<String,List<String>> runnableMap = new HashMap<>();
        List<String> vehichleLocationData = getListVehichleLocationData(url);
        runnableMap.put("bus_id_or_#", vehichleLocationData);
        List<Map> listOfMaps = new ArrayList<>();
        listOfMaps.add(runnableMap);
        Runnable runnable1;
        final Handler handler1 = new Handler();
        for(int i = 0; i < listOfMaps.size(); i++){
            runnable1 = new Runnable() {
                @Override
                public void run() {
                    animateMarker(url);
                    handler1.postDelayed(this, 3000);
                }
            };

            handler1.postDelayed(runnable1, 10);

        }
        final Handler handler = new Handler();
        runnable = new Runnable(){
            @Override
            public void run() {
                animateMarker(url);
                handler.postDelayed(this, 3000);
            }
        };

        handler.postDelayed(runnable, 10);
        String string = "bla";
        String string1 = "bla";
        String string2 = "bla";
        String string3 = "bla";
        String string4 = "bla";
        String string5 = "bla";
        String string6 = "bla";
        String string7 = "bla";
        String string8 = "bla";
        String string9 = "bla";
        String string0 = "bla";
        String string10 = "bla";
        String string11 = "bla";
        String string12 = "bla";
        String string14 = "bla";
        String string16 = "bla";
    }*/

    private List<String> getListVehichleLocationData(String url) {
        return new ArrayList<String>(10);
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

    private void animateBus(LatLng latLngNew, LatLng latLngOld) {
        ValueAnimator latLngAnimator = ValueAnimator.ofObject(new DoubleArrayEvaluator(), latLngOld, latLngNew);
        latLngAnimator.setDuration(600);
        latLngAnimator.setInterpolator(new DecelerateInterpolator());
        latLngAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                double[] animatedValue = (double[]) animation.getAnimatedValue();
//                marker.setPosition(new LatLng(animatedValue[0], animatedValue[1]));
            }
        });
        latLngAnimator.start();
        LatLng latLng = new LatLng(latLngNew.latitude, latLngNew.longitude);
//        markerOptions.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    private Button createButton(String buttonName) {
        Button button = new Button(this);
        button.setText("Click me");
//        button.setOnClickListener();
        addContentView(button, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        return button;
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
