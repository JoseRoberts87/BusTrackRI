package com.rsquared.robert.bustrackri;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
    private boolean initialize;

    public ScheduledPositionRequest(ScheduledPositionRequestListener listener, InputStream inputStream, boolean initialize) {
        this.listener = listener;
        this.inputStream = inputStream;
        this.initialize = initialize;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onScheduledPositionRequestStart();
        new ProcessRawData().execute(inputStream);
    }

    private class ProcessRawData extends AsyncTask<InputStream, Void, List<VehiclePosition>> {

        @Override
        protected List<VehiclePosition> doInBackground(InputStream... params) {
            Log.i("AsyncTask Thread =>", " Thread for ScheduledPositionRequest");

            InputStream inputStream = params[0];
            Map<String, List<String[]>> dataFileArrayListMap = new HashMap<>();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String lineReader;
                // comparing time
                long currentTimeMillis = System.currentTimeMillis();
                String currentTime = getFormattedTime(currentTimeMillis);
                String currentHourMin = getHourMinFormat(currentTime, 2);
                while ((lineReader = reader.readLine()) != null) {
                    // read everything or a chunk of it and set start time in arrays
                    if (lineReader.contains(":")) {
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
                List<VehiclePosition> vehiclePositionList = createVehiclePositionList(dataFileArrayListMap);
                return vehiclePositionList;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<VehiclePosition> vehiclePositionList) {
            if(vehiclePositionList.size() > 0) {
                listener.onScheduledPositionRequestSuccess(vehiclePositionList, initialize);
            }else {
                listener.onScheduledPositionFailure("No Scheduled at the moment for this route.");
            }
        }
    }

    private List<VehiclePosition> createVehiclePositionList(Map<String, List<String[]>> scheduledArrayListMap) {
        List<VehiclePosition> vehiclePositionList = new ArrayList<>();
        try {
            for (Map.Entry<String, List<String[]>> dataFileMarkerController : scheduledArrayListMap.entrySet()) {
                String[] currentDataFileLineArray = null;
                List<String[]> dataFileLinesArrayList = dataFileMarkerController.getValue();
                for (String[] dataFileLinesArray : dataFileLinesArrayList) {
                    String currentTime = getFormattedTime(System.currentTimeMillis());
                    String currentHourMin = getHourMinFormat(currentTime) + ":00";
                    String dataFileTime = dataFileLinesArray[DataFileContants.ARRIVAL_TIME_INDEX].trim();
                    if (dataFileTime.equalsIgnoreCase(currentHourMin.trim())) {
                        currentDataFileLineArray = dataFileLinesArray;
                        break;
                    }
                }
                if (currentDataFileLineArray == null) {
                    break;
                }
                // Get data from raw data array
                String latitude = currentDataFileLineArray[DataFileContants.STOP_LAT_INDEX].trim();
                String longitude = currentDataFileLineArray[DataFileContants.STOP_LON_INDEX].trim();
                LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

                String tripId = currentDataFileLineArray[DataFileContants.TRIP_ID_INDEX].trim();
                String arrivalTime = currentDataFileLineArray[DataFileContants.ARRIVAL_TIME_INDEX].trim();
                String stopName = currentDataFileLineArray[DataFileContants.STOP_NAME_INDEX].trim();
                String stopSequence = currentDataFileLineArray[DataFileContants.STOP_SEQUENCE_INDEX].trim();

                // Set data into vehicleposition
                VehiclePosition vehiclePosition = new VehiclePosition();
                vehiclePosition.setLatLng(latLng);
                vehiclePosition.setTripId(tripId);
                vehiclePosition.setStartTime(arrivalTime);
                vehiclePosition.setStopName(stopName);
                vehiclePosition.setStopSequence(Integer.valueOf(stopSequence));
                vehiclePosition.setRealTime(false);

                // Add the vehicleposition to list and send it over to the other side
                vehiclePositionList.add(vehiclePosition);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return vehiclePositionList;
    }

    private String getHourMinFormat(String currentTime, int minus) {
        String minutes = currentTime.substring(3, 5);
        String hour = currentTime.substring(0, 2);
        int minutesInt = Integer.valueOf(minutes) - minus;
        minutes = String.valueOf(minutesInt);
        String currentHourMin = hour + ":" + minutes;
        return currentHourMin;
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
        currentTimeMillis -= 50000000;
        Date date = new Date(currentTimeMillis);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String timeFormatted = formatter.format(date);
        return timeFormatted;
    }
}
