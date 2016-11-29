package com.rsquared.robert.bustrackri;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by R^2 on 11/28/2016.
 */
public class RealTimePositionRequest {

    private RealTimePositionRequestListener listener;
    private String requestURL;
    private Context context;

    public RealTimePositionRequest(RealTimePositionRequestListener listener, String requestURL, Context context) {
        this.listener = listener;
        this.requestURL = requestURL;
        this.context = context;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onRealTimePositionRequestStart();
        if (!isConnected()) {
            listener.onRealTimePositionRequestSuccess("");
        } else {
            String response = pullRealTimeData(requestURL);
            listener.onRealTimePositionRequestSuccess(response);
        }
    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private String pullRealTimeData(String requestURL){
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
}
