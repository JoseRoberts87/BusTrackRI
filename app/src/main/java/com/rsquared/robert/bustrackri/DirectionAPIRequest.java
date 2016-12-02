package com.rsquared.robert.bustrackri;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by R^2 on 11/28/2016.
 */
public class DirectionAPIRequest {

    private DirectionAPIRequestListener listener;
    private Context context;
    private String markerId;
    private LatLng originLatLng;
    private LatLng destinationLatLng;

    public DirectionAPIRequest(DirectionAPIRequestListener listener, Context context, String markerId, LatLng originLatLng, LatLng destinationLatLng) {
        this.listener = listener;
        this.context = context;
        this.originLatLng = originLatLng;
        this.destinationLatLng = destinationLatLng;
        this.markerId = markerId;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onDirectionAPIRequestStart();
        String requestURL = createURL(originLatLng, destinationLatLng, MAPConstants.OUTPUT_FORMAT_JSON, MAPConstants.TRAVEL_MODES_TRANSIT,
                MAPConstants.TRANSIT_MODE_BUS, MAPConstants.DEPARTURE_TIME_NOW, MAPConstants.TRANFFIC_MODEL_BEST_GUEST);
        if (!isConnected()) {
            LatLng latLng = new LatLng(41.3534, -71.3645);
            listener.onDirectionAPIFailure(latLng, markerId, "No internet Connection");
        } else {
            listener.onDirectionAPIRequestStart();
            new ProcessRawData().execute(requestURL);

        }
    }

    private class ProcessRawData extends AsyncTask<String, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(String... params) {
            String requestURL = params[0];
            URL textUrl;
            String result = "";
            try {
                textUrl = new URL(requestURL);
                BufferedReader bufferReader = new BufferedReader(
                        new InputStreamReader(textUrl.openStream()));
                String stringBuffer;
                String stringText = "";
                while((stringBuffer = bufferReader.readLine()) != null) {
                    stringText += stringBuffer;
                }
                bufferReader.close();
                result = stringText;
                List<LatLng> latLngList = getLatLngList(result);
                return latLngList;
            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<LatLng> latLngList) {
            if(latLngList != null && latLngList.size() > 0) {
                listener.onDirectionAPIRequestSuccess(latLngList, markerId);
            }else{
                listener.onDirectionAPIFailure(destinationLatLng, markerId, "LatLng list is empty");
            }
        }
    }

    private List<LatLng> getLatLngList(String jsonResponse) {
        List<LatLng> latLngList = new ArrayList<>();
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject directionJSONObject = (JSONObject) jsonParser.parse(jsonResponse);

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
                    latLngList = PolyUtil.decode(points);
                }
        }catch (Exception e){
            e.printStackTrace();
        }
        return latLngList;
    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private String createURL(LatLng originLatLng, LatLng destinationLng, String outputFormat, String travelMode, String transitMode, String departureTime, String trafficModel){
        String apiKey = context.getString(R.string.google_maps_key);
        String url = "";
        String origin = originLatLng.latitude + ","+ originLatLng.longitude;
        String destination = destinationLng.latitude + "," + destinationLng.longitude;

        if(false) {
            url = "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + "origin=" + origin
                    + "&destination=" + destination + "&key=" + apiKey + "&mode=" + travelMode + "&traffic_model=" + trafficModel
                    + "&transit_mode=" + transitMode + "&departure_time=" + departureTime;
        }else {

            url = "https://maps.googleapis.com/maps/api/directions/" + outputFormat + "?" + "origin=" + origin
                    + "&destination=" + destination + "&departure_time=" + departureTime;
        }
        Log.i("getDirectionsAPI", " The url to call to Googles Dirrection API is: " + url);
        return url;
    }
}
