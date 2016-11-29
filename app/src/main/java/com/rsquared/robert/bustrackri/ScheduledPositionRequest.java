package com.rsquared.robert.bustrackri;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by R^2 on 11/28/2016.
 */
public class ScheduledPositionRequest {

    private ScheduledPositionRequestListener listener;
    private InputStream inputStream;
    private Context context;

    public ScheduledPositionRequest(ScheduledPositionRequestListener listener, InputStream inputStream) {
        this.listener = listener;
        this.inputStream = inputStream;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onScheduledPositionRequestStart();
        Map<String, List<String[]>> dataFileArrayListMap = readRawFile(inputStream);
        listener.onScheduledPositionRequestSuccess(dataFileArrayListMap);
    }


    private Map<String, List<String[]>> readRawFile(InputStream inputStream) {
        Map<String, List<String[]>> dataFileArrayListMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String lineReader;
            // comparing time
            long currentTimeMillis = System.currentTimeMillis();
            String currentTime = getFormattedTime(currentTimeMillis);
            String currentHourMin = getHourMinFormat(currentTime);
            while((lineReader = reader.readLine()) != null){
                // read everything or a chunk of it and set start time in arrays
                if(lineReader.contains(":")) {
                    String dataHourMinute = getDataHourMinute(lineReader);
                    if (dataHourMinute.equalsIgnoreCase(currentHourMin)) {
                        String tripId = getTripId(lineReader);
                        String oldTripId = tripId;
                        List<String[]> markerControllerListOfArrays = new ArrayList<>();
                        while ((lineReader = reader.readLine()) != null) {
                            tripId = getTripId(lineReader);
                            if (tripId.equalsIgnoreCase(oldTripId)) {
                                String[] tripLineArray = lineReader.split(",");
                                markerControllerListOfArrays.add(tripLineArray);
                            } else {
                                // this is a map with everything of a particular Trip_Id
                                dataFileArrayListMap.put(oldTripId, markerControllerListOfArrays);
                                break;
                            }

                        }
                    }
                }
            }
            inputStream.close();
            return dataFileArrayListMap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String getHourMinFormat(String currentTime) {
        return currentTime.substring(0, currentTime.lastIndexOf(":"));
    }

    private String getTripId(String lineReader) {
        String tripId = lineReader.substring(0, lineReader.indexOf(",")  );
        return tripId;
    }

    private String getDataHourMinute(String line) {
        int startIndex = line.indexOf(":") - 2;
        int endIndex = startIndex + 5;
        String time = line.substring(startIndex, endIndex);
        return time;
    }

    private String getFormattedTime(long currentTimeMillis){
        // manipulate time to match regular scheduled time in data files
        currentTimeMillis -= 30000000;
        Date date = new Date(currentTimeMillis);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String timeFormatted = formatter.format(date);
        return timeFormatted;
    }



}
