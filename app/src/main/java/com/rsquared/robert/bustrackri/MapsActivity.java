package com.rsquared.robert.bustrackri;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private static LatLng MIDDLE_LOCATION = null;
    private Marker marker = null;
    private MarkerOptions markerOptions = null;
    private boolean isMarkerAdded = false;
    private Button button = null;
    private Context context;
    private Handler handler;
    private int latLngIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        try{
            mMap = googleMap;
            setMyLocation();
            String url = getIntent().getExtras().getString("url");
            url = getFormedUrl(url);
            setRoutePath(url);
            setMapInfo(url);

            final List<LatLng> decodedLatLngList = getListDecodedLatLng(url);


            button = new Button(this);
            button.setText("Click me");
            addContentView(button, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

            markerOptions = new MarkerOptions();

            button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
 /*                   Button button = (Button) v;
                    String url = button.getText().toString();
                    animateBus(url);*/
                    if(marker !=null){
                        marker.remove();
                    }
                    markerOptions.position(decodedLatLngList.get(latLngIndex)).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
                    marker = mMap.addMarker(markerOptions);
                    Toast.makeText(button.getContext(), "Button was Pressed", Toast.LENGTH_LONG).show();
                    latLngIndex++;
                }
            });

//            runOnUiThread();
            runTimedThread(url, marker);
        }catch (Exception e){
            e.printStackTrace();
        }
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

    private void runTimedThread(String url, final Marker finalMarker) {


        handler = new Handler();
        final boolean run = true;
        final Runnable runnable = new Runnable(){
            @Override
            public void run() {
                    button.performClick();

/*                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition));

                markerOptions.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
                marker = mMap.addMarker(markerOptions);*/
                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(runnable, 5000);
    }


    public void manipulateButton(String url) {
        button.setText(url);
        button.callOnClick();
    }

    private void animateBus(String url) {
        Handler handler = new Handler();
        List<String> arrayRoutePath = getMapInfoAndRoute(url, "var route_path");
        String route = getRouteString(arrayRoutePath.get(0));
        List<LatLng> decodedLatLng = null;
        while (route.contains("\\\\")) {
            route = cleanBackSlash(route);
        }
        try {
            decodedLatLng = PolyUtil.decode(route);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final List<LatLng> finalDecodedLatLng = decodedLatLng;
        handler.post(new Runnable() {
            @Override
            public void run() {
                    for(LatLng latLng: finalDecodedLatLng) {
                        if(markerOptions == null){
                            markerOptions = new MarkerOptions();
                        }
                        if(marker != null){
                            marker.remove();
                        }

                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        markerOptions.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
                        marker = mMap.addMarker(markerOptions);
    /*                    try {
                                markerOptions.position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_stop_blue_small));
    //                        Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
    //                    animateMarker(marker, latLng, false);
                    }
            }
        });
    }

    private void createMapInfo(String url) {

    }

    public void createRoute(String url) {

        PolylineOptions polylineOptions = new PolylineOptions().width(4).color(Color.BLUE);
        List<LatLng> decodedLatLng = null;
        String route = "";

        List<String> arrayRoutePath = getMapInfoAndRoute(url, "var route_path");

        for(String routePath: arrayRoutePath) {
            route = getRouteString(routePath);
        }
        try {
            decodedLatLng = PolyUtil.decode(route);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (LatLng latLng : decodedLatLng) {
            polylineOptions.add(latLng);
        }
        mMap.addPolyline(polylineOptions);
    }

    private void setMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
            // Show rationale and request permission.
        }
        mMap.setMyLocationEnabled(true);
    }

    private String getFormedUrl(String url) {
        String number = url.substring(0, url.indexOf(" "));
        url = getString(R.string.url_ripta) + number;
        return url;
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
/*        if(latLongFirst.latitude < latLongLast.latitude) {
            LatLngBounds middleLatLng = new LatLngBounds(
                    latLongLast, latLongFirst);
            MIDDLE_LOCATION = middleLatLng.getCenter();
            String done = "Done!";
        }else{
            LatLngBounds middleLatLng = new LatLngBounds(
                    latLongFirst, latLongLast);
            MIDDLE_LOCATION = middleLatLng.getCenter();
            String done = "Done!";
        }*/
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
/*        LatLngBounds middleLatLng = new LatLngBounds(
                latLongFirst, latLongLast);*/

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(middleLatLng.getCenter(), 20));

/*        if(MIDDLE_LOCATION == null){
            MIDDLE_LOCATION = new LatLng(latitude/totalLatLng, -longitude/totalLatLng);
        }*/
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

        animateBus(location);

    }

    private void animateBus(Location location) {

//        double[] startValues = new double[]{marker.getPosition().latitude, marker.getPosition().longitude};
        double[] startValues = new double[]{location.getLatitude(), location.getLongitude()};

//        double[] endValues = new double[]{destLatLng.latitude, destLatLng.longitude};
        double[] endValues = new double[]{location.getLatitude(), location.getLongitude()};

        ValueAnimator latLngAnimator = ValueAnimator.ofObject(new DoubleArrayEvaluator(), startValues, endValues);
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
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
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

 /*   private void getGPSLocation(String url ){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }
//            ...
        };
                String GPS_PROVIDER = "";
                String intervall = "";
        long distance = 1222;
        locationManager.requestLocationUpdates(GPS_PROVIDER, intervall, distance, listener);
    }*/


}
