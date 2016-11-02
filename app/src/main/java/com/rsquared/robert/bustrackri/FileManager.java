package com.rsquared.robert.bustrackri;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 10/24/2016.
 */
public class FileManager extends Activity{

    public List<String> getHeaders() {

        List<String> headers = new ArrayList<>();

        try {
            InputStream iS = getResources().openRawResource(R.raw.bus_api_file1);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line = reader.readLine();
            headers.add(line);
            iS.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            InputStream iS = getResources().openRawResource(R.raw.bus_api_file2);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line = reader.readLine();
            headers.add(line);
            iS.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            InputStream iS = getResources().openRawResource(R.raw.bus_api_file3);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line = reader.readLine();
            headers.add(line);
            iS.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            InputStream iS = getResources().openRawResource(R.raw.bus_api_file4);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line = reader.readLine();
            headers.add(line);
            iS.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            InputStream iS = getResources().openRawResource(R.raw.bus_api_file5);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line = reader.readLine();
            headers.add(line);
            iS.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return headers;
    }
}
