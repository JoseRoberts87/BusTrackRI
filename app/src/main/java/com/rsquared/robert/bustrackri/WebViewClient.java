package com.rsquared.robert.bustrackri;

import android.app.Activity;
import android.webkit.WebView;

/**
 * Created by Robert on 3/18/2016.
 */
public class WebViewClient extends android.webkit.WebViewClient {

    Activity activity = null;

    WebViewClient(Activity activity){
        this.activity = activity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.equalsIgnoreCase(activity.getString(R.string.ignore_url_map))){
/*            MainActivity mainActivity = new MainActivity();
            mainActivity.startMap();*/
            //TODO start Map activity or do nothing
        }else if(url.equalsIgnoreCase(activity.getString(R.string.ignore_url_schedule))){
            // TODO load schedules or do nothing
        }else{
            view.loadUrl(url);
        }
        return true;
    }
}
