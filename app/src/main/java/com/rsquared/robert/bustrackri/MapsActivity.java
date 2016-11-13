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
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private static LatLng MIDDLE_LOCATION = null;
    private MarkerOptions markerOptions = null;
    final private Context context = this.getBaseContext();
    //    private Handler handler;
    private int latLngIndex = 0;
    private List<LatLng> decodedLatLngList;
    Runnable runnable;
    private boolean isRunnablePosted = false;

    // Vehicle info constant
    public static final int BUS_ID = 0;
    public static final int LATITUDE = 0;
    public static final int LONGITUDE = 1;
    public static final int TIMESTAMP = 3;

    private int fileNumber = 0;
    private String route_id = "";
    private long delayTIme = 5000;
    private long oldTimestamp = 0;

    private String apiURL = "";

    String textResult;


    private class MyTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
            String newString = " ";
            newString = "do something";

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        apiURL = getString(R.string.api_url);
        new MyTask().execute();
        String jsonFile = textResult;
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
            String url = getFormedUrl();
            route_id = getUrlNumber(url);
            setMyLocation();
            setRoutePath(url);
            setMapInfo(url);
            manageThreads(route_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void manageThreads(final String route_id) {
        int counter = 0;
        while (textResult.isEmpty()) {
            Log.i("ManageThread", "manageThreads: " + counter++);
        }

//            final Map<String, List<String>> busInfoMap = getBusInfoMap(route_id, i);
//            final List<String> busInfoList = getBusInfoList(route_id);
            fileNumber++;
            final Runnable runnable1;

            Marker marker = null;
            final Handler handler = new Handler();
//            for(int j = 0; j < busInfoMap.size(); j++){
        final Marker finalMarker = marker;
        runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Map<String, List<List<String>>> busInfoListMap = getBusInfoListMap(route_id, finalMarker);
                            for(int i = 0; i < busInfoListMap.get(route_id).size(); i++) {
                                new MyTask().execute();
                                animateMarkers(busInfoListMap, finalMarker, i);
                            }
                            handler.postDelayed(this, delayTIme);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                };

                handler.postDelayed(runnable1, 10);

/*            final Handler handler = new Handler();
            runnable = new Runnable(){
                @Override
                public void run() {
                    animateMarker(busInfoMap);
                    handler.postDelayed(this, delayTIme);
                }
            };*/
//        }

    }

    private Map<String, List<List<String>>> getBusInfoMap(String route_id) {
        Map<String, List<List<String>>> busInfoMap = getBusInfoListMap(route_id, null);
        return busInfoMap;
    }

    private Map<String, List<List<String>>> getBusInfoListMap(String route_id, Marker marker) {
        Map<String, List<List<String>>> busInfoMap = new HashMap<>();
        List<String> busInfoList;
        List<List<String>> busInfoListList = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        String fileNumberString = String.valueOf(fileNumber + 1);
        String jsonFile = "";
        if(!textResult.isEmpty()){
//            jsonFile = readAPIFile();
            jsonFile = textResult;
        }


        if(!jsonFile.contains("route_id/" + route_id)) {
            jsonFile = readRawFile("vehicleposition" + fileNumberString);
        }
        try {
            JSONObject obj =  (JSONObject) jsonParser.parse(jsonFile);
            JSONArray jsonArray = (JSONArray) obj.get("entity");
            for(int i = 0; i < jsonArray.size(); i++){
                busInfoList = new ArrayList<>();
                JSONObject entityJsonObject = (JSONObject) jsonArray.get(i);
                JSONObject vehicleJsonObject = (JSONObject) entityJsonObject.get("vehicle");
                JSONObject tripJsonObject = (JSONObject) vehicleJsonObject.get("trip");
                String routeIdJsonObject = (String) tripJsonObject.get("route_id");
                if(routeIdJsonObject.equalsIgnoreCase(route_id)){
                    JSONObject positionJSONObject = (JSONObject) vehicleJsonObject.get("position");
                    double latitude = (double) positionJSONObject.get("latitude");
                    double longitude = (double) positionJSONObject.get("longitude");
                    long timestamp = (long) vehicleJsonObject.get("timestamp");
                    busInfoList.add(String.valueOf(latitude));
                    busInfoList.add(String.valueOf(longitude));
                    busInfoList.add(String.valueOf(timestamp));
                    busInfoListList.add(busInfoList);
                }
            }
            busInfoMap.put(String.valueOf(route_id), busInfoListList);
        }catch (Exception e){
            e.printStackTrace();
        }
        return busInfoMap;
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

    private void animateMarkers(Map<String, List<List<String>>> busInfoListMap, Marker finalMarker, int arrayIndex) {

        try {

//            Map<String, List<List<String>>> busInfoMap = getBusInfoMap(route_id);
//            for(int i = 0; i <busInfoMap.size(); i++) {
            List<String> busInfoList = busInfoListMap.get(route_id).get(arrayIndex);
            double latitude = Double.parseDouble(busInfoList.get(LATITUDE));
            double longitude = Double.parseDouble(busInfoList.get(LONGITUDE));

            long currenTimestamp = System.currentTimeMillis();
//          long timestamp = Integer.valueOf(busInfoList.get(TIMESTAMP));

//            delayTIme = oldTimestamp - currenTimestamp;

            LatLng latLngNew = new LatLng(latitude, longitude);
            if (markerOptions == null) {
                markerOptions = new MarkerOptions();
                markerOptions.position(latLngNew).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk)).snippet("This is bus " + route_id + " Snippet")
            }
            Marker marker = marker = mMap.addMarker(markerOptions);
            MarkerAnimation markerAnimation = new MarkerAnimation();
            markerAnimation.animateMarkerToGB(marker, latLngNew, new LatLngInterpolator.Linear());
            marker = mMap.addMarker(markerOptions);
            marker.showInfoWindow();
            markerOptions = new MarkerOptions();
            markerOptions.position(latLngNew).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_big_medium_turk)).snippet("This is bus " + route_id + " Snippet")
            .title("Bus " + route_id + " Lat: " + latitude + ", Lng: " + longitude);

            marker.showInfoWindow();
//            latLngOld = latLngNew;



/*        markerOptions.position(latLngNew).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
        marker = mMap.addMarker(markerOptions);
        */
                latLngIndex++;
//            }
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

                    Log.i("decodedLatLng: ", decodedLatLng.toString());
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
                marker.setPosition(new LatLng(animatedValue[0], animatedValue[1]));
            }
        });
        latLngAnimator.start();
        LatLng latLng = new LatLng(latLngNew.latitude, latLngNew.longitude);
        markerOptions.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
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
