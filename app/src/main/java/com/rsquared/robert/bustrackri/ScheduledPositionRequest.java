package com.rsquared.robert.bustrackri;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

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
    private Context context;

    public ScheduledPositionRequest(ScheduledPositionRequestListener listener, InputStream inputStream, boolean initialize) {
        this.listener = listener;
        this.inputStream = inputStream;
        this.initialize = initialize;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onScheduledPositionRequestStart();
        new ProcessRawData().execute(inputStream);
    }

    private class ProcessRawData extends AsyncTask<InputStream, Void, Map<String, List<String[]>>> {

        @Override
        protected Map<String, List<String[]>> doInBackground(InputStream... params) {
            InputStream inputStream = params[0];
            Map<String, List<String[]>> dataFileArrayListMap = new HashMap<>();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String lineReader;
                // comparing time
                long currentTimeMillis = System.currentTimeMillis();
                String currentTime = getFormattedTime(currentTimeMillis);
                String currentHourMin = getHourMinFormat(currentTime);
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
                return dataFileArrayListMap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Map<String, List<String[]>> response) {
            if(response != null) {
                List<VehiclePosition> vehiclePosition = createVehiclePositionList(response);
                listener.onScheduledPositionRequestSuccess(vehiclePosition, initialize);
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
                String latitude = currentDataFileLineArray[DataFileContants.STOP_LAT_INDEX];
                String longitude = currentDataFileLineArray[DataFileContants.STOP_LON_INDEX];
                LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

                String tripId = currentDataFileLineArray[DataFileContants.TRIP_ID_INDEX];
                String arrivalTime = currentDataFileLineArray[DataFileContants.ARRIVAL_TIME_INDEX];
                String stopName = currentDataFileLineArray[DataFileContants.STOP_NAME_INDEX];
                String stopSequence = currentDataFileLineArray[DataFileContants.STOP_SEQUENCE_INDEX];

                // Set data into vehicleposition
                VehiclePosition vehiclePosition = new VehiclePosition();
                vehiclePosition.setLatLng(latLng);
                vehiclePosition.setTripId(tripId);
                vehiclePosition.setStartTime(arrivalTime);
                vehiclePosition.setStopName(stopName);
                vehiclePosition.setStopSequence(Integer.valueOf(stopSequence));

                // Add the vehicleposition to list and send it over to the other side
                vehiclePositionList.add(vehiclePosition);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return vehiclePositionList;
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
        currentTimeMillis -= 40000000;
        Date date = new Date(currentTimeMillis);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String timeFormatted = formatter.format(date);
        return timeFormatted;
    }
}
