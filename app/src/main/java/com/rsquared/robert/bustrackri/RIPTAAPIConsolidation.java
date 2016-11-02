package com.rsquared.robert.bustrackri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Robert on 10/21/2016.
 */
public class RIPTAAPIConsolidation {

    public static void main(String[] args){

        FileManager fileManager = new FileManager();
        List<String> headers = fileManager.getHeaders();
        List<String> titles = getTitlesHeader(headers);
        List<String> overLappingTitles = getOverlappingTitles(titles);

    }

    private static List<String> getOverlappingTitles(List<String> titles) {
        List<String>  overlappingTitleList = new ArrayList<>();
        Set<String> overlappingTitleSet = new HashSet<>();
        String previousTitle = titles.get(0);
        for (int i = 1; i < titles.size(); i++){
            String nextTitle = titles.get(i);
            if(previousTitle == nextTitle){
                overlappingTitleSet.add(nextTitle);
            }
        }
        overlappingTitleList.addAll(overlappingTitleSet);
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
}
