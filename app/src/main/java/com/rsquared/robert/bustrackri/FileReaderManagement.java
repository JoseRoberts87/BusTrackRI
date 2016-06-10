package com.rsquared.robert.bustrackri;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Robert on 5/18/2016.
 */
public class FileReaderManagement {
    public List<String> getTextFromFile(String path){
        List list = new ArrayList();
        try{
/*            InputStream iS = resources.getAssets().open("bla.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));*/
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;

            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            Log.i("getTextFromFIle", list.toString());


        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
