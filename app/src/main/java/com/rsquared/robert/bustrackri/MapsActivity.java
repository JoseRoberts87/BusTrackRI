package com.rsquared.robert.bustrackri;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.internal.LocationRequestUpdateData;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, RealTimeAPIPositionRequestListener, ScheduledPositionRequestListener, DirectionAPIRequestListener,  LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static LatLng MIDDLE_LOCATION = null;
    private boolean isRunnablePosted = false;
    private String route_id = "";
    private long POSITION_REQUEST_DELAY = 35;
    private boolean requestedRealData = false;
    private boolean requestedScheduledata = false;
    private Set<MarkerController> markerControllerSet;
    private Handler handler = new Handler();
    private ScheduledExecutorService scheduledExecutorServiceRealTime;
    private ScheduledExecutorService scheduledExecutorServiceScheduled;
    boolean initializeRealTime = true;
    boolean initializeScheduled = true;
    final private Context context = this;
    private boolean isRawDataDisplayed = false;
    private Runnable runnable;
    private LatLng stopLatLng = null;
    private  Marker stopMarker = null;
    private final static int PERMISSION_FINE_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mapFragment.getMapAsync(this);
    }

    private void init() {
        // initialize global variables and request data and services
        String url = getFormedUrl();
        stopLatLng = getStopLatLong();
        route_id = getUrlNumber(url);
        markerControllerSet = new HashSet<>();
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        requestMyLocation();
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

            // Initialize variables and data calls and service calls
            init();
            String url = getFormedUrl();
            setRoutePath(url);
            setMapInfo(url);
            requestPositionData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestPositionData() {

        // request real time data from RIPTA API URL
        scheduledExecutorServiceRealTime = Executors.newScheduledThreadPool(1);

        Runnable realTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (isConnected()) {
                    Log.i("realTimeRunnable =>", " Thread for scheduledExecutorServiceRealTime");
                    requestRealTimePosition(initializeRealTime);
                    String threadName = Thread.currentThread().getName();
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
//                if (isRawDataDisplayed) {
                    Log.i("scheduledRunnable =>", " Thread for scheduledExecutorServiceScheduled");
                    requestScheduledPosition(initializeScheduled);
                    String threadName = Thread.currentThread().getName();
                    try {
                        this.finalize();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
//                } else {
                    // Do not display and whatever....
                    // maybe in the future create a class to manage changes on isRawDataDisplayed
                    // to then decide if execute the scheduledExecutorServiceScheduled
//                }
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
            new RealTimeAPIPositionRequest(this, getString(R.string.real_time_url), this.getBaseContext(), initialize, route_id).execute();
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
        manageMarkerThreads(vehiclePositionList, initialize);
        if (initializeRealTime) {
            initializeRealTime = false;
        }else{

        }
    }

    @Override
    public void onRealTimeConnectionFailure(String failureMsg) {
        // show message that no internet connection and show scheduled data
        isRawDataDisplayed = true;
        setAllScheduledMarkersVisibility(true);
        setAllRealTimeMarkersVisibility(false);

//        Toast.makeText(this, "Real Time Not Available\n" + failureMsg, Toast.LENGTH_SHORT).show();
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
        manageMarkerThreads(vehiclePositionList, initialize);
        if(initializeScheduled){
            initializeScheduled = false;
        }
    }

    @Override
    public void onScheduledPositionFailure(String failureMsg) {
        // show message that schedule not available
        isRawDataDisplayed = false;
//        Toast.makeText(this, "Scheduled Time Not Available\n" + failureMsg, Toast.LENGTH_SHORT).show();
    }

    private void manageMarkerThreads(final List<VehiclePosition> vehiclePositionList, final boolean initialize){
        runnable = new Runnable() {
            @Override
            public void run() {
                manageMarkerController(vehiclePositionList, initialize);
                Log.i("manageMarkerThreads =>", " Thread for manageMarkerController");
                try {
                    this.finalize();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        };
        handler.post(runnable);
    }

    /**
     * This method will take the vehicle position list and create markerControllers
     * Then it will set the markerController into Global MarkerControllerSet
     * @param vehiclePositionList
     * @param initialize
     */
    private void manageMarkerController(List<VehiclePosition> vehiclePositionList, boolean initialize) {
        MarkerController markerController;
        for(VehiclePosition vehiclePosition: vehiclePositionList){
            // Get vehicle information to create MarkerController
            String stopName = vehiclePosition.getStopName().trim();
            String startTime = vehiclePosition.getStartTime().trim();
            int stopSequence = vehiclePosition.getStopSequence();
            double bearing = vehiclePosition.getBearing();
            boolean isRealTime = vehiclePosition.isRealTime();
            String tripId = vehiclePosition.getTripId().trim();
            LatLng destinationLatLng = vehiclePosition.getLatLng();
            long timeStamp = System.currentTimeMillis();

            if(getMarkerControllerById(tripId) == null) {
                // Create Brand New MarkerController to add to Global markerControllerSet
                MarkerAnimation markerAnimation = new MarkerAnimation();

                MarkerOptions markerOptions;
                if(isRealTime){
                    markerOptions = createBusMarkerOptionDefaultRealTime(destinationLatLng, tripId);
                }else{
                    markerOptions = createBusMarkerOptionDefault(destinationLatLng);
                }

                Marker marker = mMap.addMarker(markerOptions);
                markerController = new MarkerController(marker, tripId, destinationLatLng, timeStamp, markerOptions, markerAnimation, 100, false);
                markerController.setBeenAnimated(false);
                markerController.setBearing(bearing);
                markerControllerSet.add(markerController);
//                Toast.makeText(this, "Initializing markerId: " + markerController.getMarkerId() , Toast.LENGTH_SHORT).show();
            }else if(!initialize){
                // Get old markerController by Id from MarkerControllerSet
                markerController = getMarkerControllerById(tripId);
                Marker marker = markerController.getMarker();
                LatLng originLatLng = marker.getPosition();

                // check if location changed, initiate animation
                if (originLatLng != destinationLatLng) {
                    requestDirectionAPIData(tripId, originLatLng, destinationLatLng);
                    Toast.makeText(this, "Animating bus: " + markerController.getMarkerId() + " this is realtime = " + isRealTime, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void animateSingleMarkerController(final MarkerController markerController, final LatLng destinationLatLng, final boolean isRealTime){
        Marker marker = markerController.getMarker();
        MarkerAnimation markerAnimation = markerController.getMarkerAnimation();
        if(!markerController.isBeenAnimated()) {
            Toast.makeText(this, "Animating bus: " + markerController.getMarkerId() + " this is realtime = " + isRealTime, Toast.LENGTH_SHORT).show();
            markerAnimation.animateMarkerToGB(marker, destinationLatLng, new LatLngInterpolator.Linear(), markerController);
        }
    }

    /**
     * Animates a marker in the map based on new latLng and timestamp
     * @param markerController
     */
    private void animateMarkerControllerArray(final MarkerController markerController){
        long oldTimeStamp = markerController.getTimeStamp();
        long newTimeStamp = System.currentTimeMillis();
        markerController.setTimeStamp(newTimeStamp);
        long animationDuration = newTimeStamp - oldTimeStamp;

        final MarkerAnimation markerAnimation = markerController.getMarkerAnimation();
        final Marker marker = markerController.getMarker();
        List<LatLng> latLngList = markerController.getLatLngArray();

        animationDuration = animationDuration / latLngList.size();
        final float postDuration = animationDuration;

        markerController.setAnimationDuration(animationDuration);
        markerController.setAnimationCounter(0);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int animationCounter = markerController.getAnimationCounter();
                if(!markerController.isBeenAnimated()) {
                    markerController.setBeenAnimated(true);
                    markerAnimation.animateMarkerToGB(marker, markerController.getLatLngArray().get(animationCounter), new LatLngInterpolator.Linear(), markerController);
                    markerController.setAnimationCounter(markerController.getAnimationCounter() + 1);
                }
                if (markerController.getAnimationCounter() < markerController.getLatLngArray().size()) {
                    handler.postDelayed(this, (long) 50);
//                    if (markerController.getAnimationCounter() < markerController.getLatLngArray().size() - 1) {
//                    }
                }
            }
        });
    }

/*    */
    /**
     * Animates a marker in the map based on new latLng and timestamp
//     * @param markerController
     *//*
    private void animateMarkerControllerArray(final MarkerController markerController){
        final MarkerAnimation markerAnimation = markerController.getMarkerAnimation();
        final Marker marker = markerController.getMarker();
        final float postDuration = 24000/markerController.getLatLngArray().size();
        markerController.setAnimationCounter(markerController.getLatLngArray().size());

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
    }*/

    private void requestDirectionAPIData(String tripId, LatLng originLatLng, LatLng destinationLatLng) {
        try {
            new DirectionAPIRequest(this, this.getBaseContext(), tripId, originLatLng, destinationLatLng).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onDirectionAPIRequestStart() {

    }

    @Override
    public void onDirectionAPIRequestSuccess(List<LatLng> latLngList, String markerId) {

        if(latLngList != null && latLngList.size() >0) {
            MarkerController markerController = getMarkerControllerById(markerId);
            markerController.setLatLngArray(latLngList);
            double bearing = markerController.getBearing();
            drawPolyLines(latLngList);
            setMarkerArrow(markerController, latLngList.get(latLngList.size() - 1), bearing);
            animateMarkerControllerArray(getMarkerControllerById(markerId));
//            animateSingleMarkerController(markerController, latLngList.get(latLngList.size() - 1), true);
        }else{
            // do nothing for now
        }
    }

    private void setMarkerArrow(MarkerController markerController, LatLng latLng, double bearing) {

        double adjBearing = Math.round(bearing / 3) * 3;
        while (adjBearing >= 120) {
            adjBearing -= 120;
        }

//        new DownloadWebpageTask().execute(markerController);

    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes awaas
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<MarkerController, Void, MarkerController> {
        @Override
        protected MarkerController doInBackground(MarkerController... params) {

            // params comes from the execute() call: params[0] is the url.
            MarkerController markerController = params[0];
            double adjBearing = markerController.getBearing();

            try {
                URL url = new URL("http://maps.google.com/mapfiles/dir_" + String.valueOf((int)adjBearing) + ".png");
                try {
                    Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    markerController.setArrowImage(image);
                    return markerController;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(MarkerController markerController) {
            Bitmap image = markerController.getArrowImage();
            LatLng lastLatLng = markerController.getLatLngArray().get(markerController.getLatLngArray().size());
            if (image != null) {
                mMap.addMarker(new MarkerOptions().position(lastLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_arrow)));
            }
        }
    }

    private void drawPolyLines(List<LatLng> latLngList) {
        PolylineOptions polylineOptions = new PolylineOptions().width(15).color(Color.RED);
            for (int i = 0; i < latLngList.size(); i++) {
                polylineOptions.add(latLngList.get(i));
            }
        if(latLngList.size() > 0){
            LatLng lastLatLng = latLngList.get(latLngList.size() - 1);
//            polylineOptions.ic
        }
        mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onDirectionAPIFailure(LatLng destinationLatLng, String markerId, String failureMsg) {
        MarkerController markerController = getMarkerControllerById(markerId);
//        animateSingleMarkerController(markerController, destinationLatLng, markerController.isRealTime());
        animateMarkerControllerArray(getMarkerControllerById(markerId));

    }

    private MarkerController getMarkerControllerById(String markerId){
        MarkerController markerController = null;
        List<MarkerController> markerControllerList = new ArrayList(markerControllerSet);
        for(int i = 0; i < markerControllerList.size(); i++){
            if(markerControllerList.get(i).getMarkerId().trim().equalsIgnoreCase(markerId)){
                markerController = markerControllerList.get(i);
            }
        }
        return markerController;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_FINE_LOCATION:
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }

            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(this.getBaseContext(), "permission denied", Toast.LENGTH_SHORT).show();
            }
                break;
        }

            // other 'case' lines to check for other
            // permissions this app might request
    }

        private void permission
                () {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_FINE_LOCATION);

           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);
            }*/
        }
    }

    private void requestMyLocation() {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                checkIfNetworkLocationAvailable();
            }else {
                // do nothing
            }
            mMap.setMyLocationEnabled(true);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            // TODO Show rationale and request permission.
        }
        mMap.setMyLocationEnabled(true);
    }


    /**
     * checks if NETWORK LOCATION PROVIDER is available
     */
    private void checkIfNetworkLocationAvailable() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean networkLocationEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!networkLocationEnabled){
            //show dialog to allow user to enable location settings
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("Enable GPS");
            dialog.setMessage("For Better Service Enale GPS");

            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            });

            dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //nothing to do
                }
            });

            dialog.show();
        }

    }

    private LatLng getStopLatLong(){
        String url = getIntent().getExtras().getString("url");

        if(url.contains("latLng=")) {
            int firstIndexLat = url.lastIndexOf("=") + 1;
            int lastIndexLat = url.lastIndexOf(",");
            int firstIndexLon = url.lastIndexOf(",") + 1;
            int lastIndexLon = url.length();

            String lat = url.substring(firstIndexLat, lastIndexLat);
            String lon = url.substring(firstIndexLon, lastIndexLon);

            stopLatLng = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
        }else {
            stopLatLng = null;
        }
        return stopLatLng;
    }

    private String getUrlNumber(String url){
        int firstIndex = url.lastIndexOf("/") + 1;
        int lastIndex = url.length();
        String urlNumber = url.substring(url.lastIndexOf("/") + 1, url.length());
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


         CameraUpdate cameraUpdate = null;
         if(stopLatLng == null) {
             cameraUpdate = CameraUpdateFactory.newLatLngZoom(MIDDLE_LOCATION, 12);
         }else{
             MarkerOptions stopMarkerOptions = createStopMarkerOptionDefault(stopLatLng);
             stopMarker = mMap.addMarker(stopMarkerOptions);
             stopMarker.showInfoWindow();
             cameraUpdate = CameraUpdateFactory.newLatLngZoom(stopLatLng, 15);
         }
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

    private boolean hasMarkerLatLngChanged(MarkerController markerController, LatLng latLng) {
        LatLng oldLatLng = markerController.getLatLng();
        return !oldLatLng.equals(latLng);
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
        List<MarkerController> markerControllerList = new ArrayList(markerControllerSet);

        for(int i = 0; i < markerControllerList.size(); i++){
            MarkerController markerController = markerControllerList.get(i);
            if(markerController.isSameMarkerId(markerId)){
                return markerController.getMarker();
            }
        }
        return null;
    }

    private void setAllRealTimeMarkersVisibility(boolean isVisible){
        List<MarkerController> markerControllerList = new ArrayList(markerControllerSet);
        for(int i = 0; i < markerControllerList.size(); i++){
            if(markerControllerList.get(i).isRealTime()){
                markerControllerList.get(i).getMarker().setVisible(isVisible);
            }
        }
    }

    private void setAllScheduledMarkersVisibility(boolean isVisible){
        List<MarkerController> markerControllerList = new ArrayList(markerControllerSet);
        for(int i = 0; i < markerControllerList.size(); i++){
            if(!markerControllerList.get(i).isRealTime()){
                markerControllerList.get(i).getMarker().setVisible(isVisible);
            }
        }
    }

    private MarkerOptions createStopMarkerOptionDefault(LatLng latLng){
        return new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus))
                .snippet("StopId: " + route_id).title("Stop " + route_id + " Lat: " + latLng.latitude + ", Lng: " + latLng.longitude).visible(true);
    }

    private MarkerOptions createBusMarkerOptionDefault(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk))
                .snippet("This is bus " + route_id + " Snippet").title("Bus " + route_id + " Lat: " + latLng.latitude + ", Lng: " + latLng.longitude).visible(isRawDataDisplayed);
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

    @Override
    public void onLocationChanged(Location location) {
        Log.i("LocationChanged", "Location has chnged to: " + location.getLatitude() + ", " + location.getLongitude());
    }

    // Activity management to stop and run background threads
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        } else if(!hasFocus){
            destroyThreads();
        }
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

    @Override
    public boolean onMyLocationButtonClick() {
        requestMyLocation();

        return true;
    }
}
