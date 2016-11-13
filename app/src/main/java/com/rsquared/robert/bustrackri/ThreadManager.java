package com.rsquared.robert.bustrackri;

/**
 * Created by R^2 on 11/12/2016.
 */

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.net.MalformedURLException;
        import java.net.URL;

        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v7.app.ActionBarActivity;
        import android.widget.TextView;


public class ThreadManager extends ActionBarActivity {

    final String urlSource = "https://sites.google.com/site/androidersite/text.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new MyTask().execute();
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {

        String textResult;

        @Override
        protected Void doInBackground(Void... params) {
            URL textUrl;

            try {
                textUrl = new URL(urlSource);
                BufferedReader bufferReader = new BufferedReader(
                        new InputStreamReader(textUrl.openStream()));
                String stringBuffer;
                String stringText = "";
                while((stringBuffer = bufferReader.readLine()) != null) {
                    stringText += stringBuffer;
                }

                bufferReader.close();
                textResult = stringText;

            } catch(MalformedURLException e) {
                e.printStackTrace();
                textResult = e.toString();
            } catch(IOException e) {
                e.printStackTrace();
                textResult = e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
