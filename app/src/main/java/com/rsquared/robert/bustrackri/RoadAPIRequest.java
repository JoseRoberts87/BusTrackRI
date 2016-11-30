package com.rsquared.robert.bustrackri;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is NOT been implemented at the moment...
 * Created by R^2 on 11/28/2016.
 */
public class RoadAPIRequest {

    private RoadAPIRequestListener listener;
    private InputStream inputStream;
    private boolean initialize;
    private Context context;

    public RoadAPIRequest(RoadAPIRequestListener listener, InputStream inputStream, boolean initialize) {
        this.listener = listener;
        this.inputStream = inputStream;
        this.initialize = initialize;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onRoadAPIRequestStart();
        if (!isConnected()) {
            listener.onRoadAPIFailure("No internet Connection");
        } else {
            listener.onRoadAPIRequestStart();
            new RoadAPIRequest.ProcessRawData().execute("");

        }
    }

    private class ProcessRawData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            URL textUrl;
            String result = "";
            try {
                textUrl = new URL("");
                BufferedReader bufferReader = new BufferedReader(
                        new InputStreamReader(textUrl.openStream()));
                String stringBuffer;
                String stringText = "";
                while((stringBuffer = bufferReader.readLine()) != null) {
                    stringText += stringBuffer;
                }
                bufferReader.close();
                result = stringText;
                return result;
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
                List<LatLng> latLngList = getLatLngList(jsonResponse);
                // TODO get list of LatLng
                listener.onRoadAPIRequestSuccess(latLngList);
            }
        }
    }

    private List<LatLng> getLatLngList(String jsonResponse) {
        List<LatLng> latLngList = new ArrayList<>();
        String polyLines = "";
        List<String> polyLinesArray = new ArrayList<>();
        try {

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

}
