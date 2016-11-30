package com.rsquared.robert.bustrackri;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by R^2 on 11/28/2016.
 */
public class RealTimePositionRequest {

    private RealTimePositionRequestListener listener;
    private String requestURL;
    private Context context;
    private boolean initialize;
    private String route_id;

    public RealTimePositionRequest(RealTimePositionRequestListener listener, String requestURL, Context context, boolean initialiseRealTime, String route_id) {
        this.listener = listener;
        this.requestURL = requestURL;
        this.context = context;
        this.initialize = initialiseRealTime;
        this.route_id = route_id;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onRealTimePositionRequestStart();
        if (!isConnected()) {
            listener.onRealTimeConnectionFailure("No internet Connection");
        } else {
            listener.onRealTimePositionRequestStart();
            new ProcessRawData().execute(requestURL);

        }
    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private class ProcessRawData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String requestURL = params[0];
            URL textUrl;
            try {
                textUrl = new URL(requestURL);
                InputStreamReader inputStream = new InputStreamReader(textUrl.openStream());
                BufferedReader bufferReader = new BufferedReader(inputStream);
                String stringBuffer;
                String stringText = "";
                while((stringBuffer = bufferReader.readLine()) != null) {
                    stringText += stringBuffer;
                }
                bufferReader.close();
                inputStream.close();
                return stringText;
            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if(jsonResponse != null) {
                List<VehiclePosition> vehiclePosition = createVehiclePositionList(jsonResponse);
                listener.onRealTimePositionRequestSuccess(vehiclePosition, initialize);
            }
        }
    }

    private List<VehiclePosition> createVehiclePositionList(String responseJson) {
        List<VehiclePosition> vehiclePositionList = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        try {
            if (!responseJson.isEmpty()) {
                JSONObject obj = (JSONObject) jsonParser.parse(responseJson);
                JSONArray jsonArray = (JSONArray) obj.get("entity");
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject entityJsonObject = (JSONObject) jsonArray.get(i);
                    JSONObject vehicleJsonObject = (JSONObject) entityJsonObject.get("vehicle");
                    JSONObject tripJsonObject = (JSONObject) vehicleJsonObject.get("trip");
                    String routeIdJsonObject = (String) tripJsonObject.get("route_id");
                    if (routeIdJsonObject.equalsIgnoreCase(route_id)) {
                        JSONObject positionJSONObject = (JSONObject) vehicleJsonObject.get("position");
                        JSONObject vehicleVehicleObject = (JSONObject) vehicleJsonObject.get("vehicle");
//                    if (!busInfoList.contains(vehicleVehicleObject.get("label"))) {
                        double latitude = (double) positionJSONObject.get("latitude");
                        double longitude = (double) positionJSONObject.get("longitude");
                        LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

                        long timestamp = (long) vehicleJsonObject.get("timestamp");
                        String stopId = (String) vehicleJsonObject.get("stop_id");
                        String startTime = (String) tripJsonObject.get("start_time");
                        String tripId = (String) tripJsonObject.get("trip_id");
                        double bearing = (double) positionJSONObject.get("bearing");
                        String label = (String) vehicleVehicleObject.get("label");
                        String stopName = getStopName(stopId);
                        int stopSequence = 0;

                        // Set data into vehicleposition
                        VehiclePosition vehiclePosition = new VehiclePosition();
                        vehiclePosition.setLatLng(latLng);
                        vehiclePosition.setTripId(tripId);
                        vehiclePosition.setStartTime(startTime);
                        vehiclePosition.setStopName(stopName);
                        vehiclePosition.setStopSequence(Integer.valueOf(stopSequence));

                        // Add the vehicleposition to list and send it over to the other side
                        vehiclePositionList.add(vehiclePosition);
                    }
                }
            }
        }catch (Exception e) {
                e.printStackTrace();
        }
        return vehiclePositionList;
    }

    private String getStopName(String stopId) {
        return "Unknown";
    }
}
