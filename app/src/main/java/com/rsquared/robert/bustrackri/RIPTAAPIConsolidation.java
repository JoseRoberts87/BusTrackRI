package com.rsquared.robert.bustrackri;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 10/21/2016.
 */
public class RIPTAAPIConsolidation extends Activity {

    public static void main(String[] args){

        RIPTAAPIConsolidation riptaapiConsolidation = new RIPTAAPIConsolidation();
        List<String> headers = riptaapiConsolidation.getHeaders();
        List<String> titles = getTitlesHeader(headers);
        List<String> overLappingTitles = getOverlappingTitles(titles);

    }

    private static List<String> getOverlappingTitles(List<String> titles) {
        List<String>  overlappingTitleList = new ArrayList<>();
        String previousTitle = titles.get(0);
        for (int i = 1; i < titles.size(); i++){
            String nextTitle = titles.get(i);
            if(previousTitle == nextTitle){
                overlappingTitleList.add(nextTitle);
            }
        }
        return overlappingTitleList;
    }

    private static List<String> getTitlesHeader(List<String> headers) {

        List<String> titleList = new ArrayList<>();
        for( int i = 0; i < headers.size(); i++){
            String[] titles = headers.get(i).split(",");
            for(String title: titles ){
                titleList.add(title);
            }
        }
        return titleList;
    }


    public List<String> getHeaders() {

        List<String> headers = new ArrayList<>();

        try {
            InputStream iS1 = getResources().openRawResource(R.raw.bus_api_file1);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS1));
            String line = reader.readLine();
            headers.add(line);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            InputStream iS2 = getResources().openRawResource(R.raw.bus_api_file2);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS2));
            String line = reader.readLine();
            headers.add(line);
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            InputStream iS3 = getResources().openRawResource(R.raw.bus_api_file3);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS3));
            String line = reader.readLine();
            headers.add(line);
        }catch (Exception e){
            e.printStackTrace();
        }
        return headers;
    }
}
