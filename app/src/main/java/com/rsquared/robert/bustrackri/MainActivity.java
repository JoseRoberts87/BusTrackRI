package com.rsquared.robert.bustrackri;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TabHost;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabHost.OnTabChangeListener {

    private String itemTitle = "";
    private String route_Id = "";
    private List<String> stopNameArrayInbound;
    private List<String> stopNameArrayOutbound;

    private List<String[]> timeArrayWeekDaysInbound;
    private List<String[]> timeArraySaturdaysInbound;
    private List<String[]> timeArraySundaysHolidaysInbound;

    private List<String[]> timeArrayWeekDaysOutbound;
    private List<String[]> timeArraySaturdaysOutbound;
    private List<String[]> timeArraySundaysHolidaysOutbound;

    private String stopLatLng = "";

    private boolean isInbound = true;
    private boolean isWeekDay = true;
    private boolean isSaturday = false;
    private boolean isSundayHoliday = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMap();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        List<String> busRoutesArray = new ArrayList<>();

        try {
            InputStream iS = getResources().openRawResource(R.raw.bus_stop_names);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line;
            while ((line = reader.readLine()) != null) {
                busRoutesArray.add(line);
            }
            Log.i("getTextFromFIle", busRoutesArray.toString());
        }catch (Exception e){
            e.printStackTrace();
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        Menu menu = navigationView.getMenu();

        populateMenu(menu, busRoutesArray);

        navigationView.setNavigationItemSelectedListener(this);


        initialize(busRoutesArray.get(0));
        TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("1").setIndicator(getString(R.string.tab_weekday)).setContent(R.id.tab1));
        tabHost.addTab(tabHost.newTabSpec("2").setIndicator(getString(R.string.tab_saturday)).setContent(R.id.tab2));
        tabHost.addTab(tabHost.newTabSpec("3").setIndicator(getString(R.string.tab_sunday_holiday)).setContent(R.id.tab3));
        tabHost.setOnTabChangedListener(this);

    }

    private void initialize(String itemTitle) {
        setWeekDays();
        setSwitch();
        if(this.itemTitle.isEmpty()){
            this.itemTitle = itemTitle;
            route_Id = "1";
        }
        setUIData();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void startMap(){
        if(!this.itemTitle.isEmpty()) {
            String urlTitle = String.valueOf(this.itemTitle);
            Bundle bundle = new Bundle();

            bundle.putString("url", urlTitle + "?latLng=" + stopLatLng);

            startActivity(new Intent("android.intent.action.MAPACTIVITY").putExtras(bundle));
        }else{
//            do nothing or something
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private void populateMenu(Menu menu, List<String> busRoutesArray) {

        for (int i = 0; i < busRoutesArray.size(); i++) {
            menu.add(i, i, i, busRoutesArray.get(i));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getUrlNumber(String url){
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    private String getFormedUrl() {
        String url = getIntent().getExtras().getString("url");
        String number = url.substring(0, url.indexOf(" "));
        return getString(R.string.url_ripta) + number;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        String itemTitle = (String) item.getTitle();
        this.itemTitle = itemTitle;
        String routeNumber = itemTitle.substring(0, itemTitle.indexOf(" "));
        route_Id = routeNumber;
        setUIData();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void createDialog(String stopName, List<String[]> dataLineList){
        List<String> timeByStopNameList = new ArrayList<>();
        boolean stopLatLngDone = false;
        for(int i = 0; i < dataLineList.size(); i++){
            String[] dataLineArray = dataLineList.get(i);
            String dataStopName = dataLineArray[DataFileContants.STOP_NAME_INDEX].replaceAll("\"", "").trim();
            if(dataStopName.trim().equalsIgnoreCase(stopName.trim())){
                timeByStopNameList.add(dataLineArray[DataFileContants.ARRIVAL_TIME_INDEX]);
                if(!stopLatLngDone) {
                    stopLatLng = dataLineArray[DataFileContants.STOP_LAT_INDEX] + "," + dataLineArray[DataFileContants.STOP_LON_INDEX];
                    stopLatLngDone = true;
                }

            }
        }
        timeByStopNameList = formatTime(timeByStopNameList);
        String[] timeArray = new String[timeByStopNameList.size()];
        timeArray = timeByStopNameList.toArray(timeArray);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(stopName);
        builder.setItems(timeArray, null);
        builder.setIcon(R.drawable.red_dot);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static List<String> formatTime(List<String> timeList) {
        List<String> formattedTimeList = new ArrayList<String>();
        Collections.sort(timeList);

        for(int i = 0; i < timeList.size(); i++){
            String time = timeList.get(i);
            String suffix = getSuffix(time);
            Date date = null;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("KK:mm");
            try {
                date = simpleDateFormat.parse(time);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String formattedTIme = simpleDateFormat.format(date);
            formattedTimeList.add(formattedTIme + " " + suffix);
        }
        return formattedTimeList;
    }

    private static String getSuffix(String time) {
        String suffix = "a.m.";
        String hourString = time.substring(0, 2);
        int hourInt = Integer.valueOf(hourString);
        if(hourInt > 12 && hourInt < 24){
            suffix = "p.m.";
        }
        return suffix;
    }

    private void setListView(){
        try {
            List<String> stopList;
            if(isInbound) {
                stopList = stopNameArrayInbound;
            }else{
                stopList = stopNameArrayOutbound;
            }

            final ListView listView = (ListView) findViewById(R.id.listview);
            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stopList);
            listView.setAdapter(stringArrayAdapter);
            AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String stopName = (String) listView.getItemAtPosition(position);

                    if(isInbound) {
                        if (isWeekDay) {
                            createDialog(stopName, timeArrayWeekDaysInbound);
                        } else if (isSaturday) {
                            createDialog(stopName, timeArraySaturdaysInbound);
                        } else {
                            createDialog(stopName, timeArraySundaysHolidaysInbound);
                        }
                    }else {
                        if (isWeekDay) {
                            createDialog(stopName, timeArrayWeekDaysOutbound);
                        } else if (isSaturday) {
                            createDialog(stopName, timeArraySaturdaysOutbound);
                        } else {
                            createDialog(stopName, timeArraySundaysHolidaysOutbound);
                        }
                    }

                }
            };
            listView.setOnItemClickListener(onItemClickListener);

//            WebView webView = (WebView) findViewById(R.id.webView);
//            webView.setWebViewClient(new WebViewClient(this));
//            webView.loadUrl("http://m.ripta.com/1");
//            webView.loadUrl("http://localhost:8080/UIStevens/UI_Project_1/Ripta/index.html");
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setSwitch() {
        final Switch mySwitch = (Switch) findViewById(R.id.switch3);

        //set the switch to ON
        mySwitch.setChecked(true);
        //attach a listener to check for changes in state

        //check the current state before we display the screen
        if(mySwitch.isChecked()){
            mySwitch.setText(getString(R.string.inbound));
        }
        else {
            mySwitch.setText(getString(R.string.outbound));
        }

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if(isChecked){
                    isInbound = true;
                    mySwitch.setText(getString(R.string.inbound));
                }else{
                    isInbound = false;
                    mySwitch.setText(getString(R.string.outbound));
                }
                setListView();
            }
        });


    }


    private void setUIData(){
        int resourceId = getResources().getIdentifier("route_" + route_Id, "raw", getPackageName());
        new DataCompilerTask().execute(resourceId);
    }


    private class DataCompilerTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {

            int resourceId = params[0];
            try {
                InputStream iS = getResources().openRawResource(resourceId);
                BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
                String line;
                // push Inbound outbound data set
                Set<String> stopNameInboundSet = new HashSet<>();
                Set<String> stopNameOutboundSet = new HashSet<>();

                // push weekdays vs saturdays vs sundays Inbound data set
                Set<String[]> timeArrayWeekDaysSetInbound = new HashSet<>();
                Set<String[]> timeArraySaurdaysSetInbound = new HashSet<>();
                Set<String[]> timeArraySundaysHolidaysSetInbound = new HashSet<>();

                // push weekdays vs saturdays vs sundays Outbound data set
                Set<String[]> timeArrayWeekDaysSetOutbound = new HashSet<>();
                Set<String[]> timeArraySaurdaysSetOutbound = new HashSet<>();
                Set<String[]> timeArraySundaysHolidaysSetOutbound = new HashSet<>();

                while ((line = reader.readLine()) != null) {
                    String[] lineArray = line.split(",");

                    if(line.length() >= DataFileContants.ARRAY_LENGTH) {

                        // set everything in here

                        // setting the stop name arrays for inbound and outbound
                        if (lineArray[DataFileContants.DIRECTION_ID].trim().equalsIgnoreCase("0")) {
                            String dataStopName = lineArray[DataFileContants.STOP_NAME_INDEX].replaceAll("\"", " ").trim();
                            stopNameInboundSet.add(dataStopName);
                            if(stopLatLng.isEmpty()) {
                                stopLatLng = lineArray[DataFileContants.STOP_LAT_INDEX] + "," + lineArray[DataFileContants.STOP_LON_INDEX];
                            }

                            // setting the time arrays for weekdays, saturdays and sundays/holidays Inbound
                            if (lineArray[DataFileContants.SERVICE_ID_INDEX].trim().equalsIgnoreCase(getString(R.string.weekday))) {
                                timeArrayWeekDaysSetInbound.add(lineArray);
                            } else if (lineArray[DataFileContants.SERVICE_ID_INDEX].trim().equalsIgnoreCase(getString(R.string.saturday))) {
                                timeArraySaurdaysSetInbound.add(lineArray);
                            } else if (lineArray[DataFileContants.SERVICE_ID_INDEX].trim().equalsIgnoreCase(getString(R.string.sunday))) {
                                timeArraySundaysHolidaysSetInbound.add(lineArray);
                            }

                        }else if(lineArray[DataFileContants.DIRECTION_ID].trim().equalsIgnoreCase("1")) {
                            String dataStopName = lineArray[DataFileContants.STOP_NAME_INDEX].replaceAll("\"", " ").trim();
                            stopNameOutboundSet.add(dataStopName);

                            // setting the time arrays for weekdays, saturdays and sundays/holidays outbound
                            if (lineArray[DataFileContants.SERVICE_ID_INDEX].trim().equalsIgnoreCase(getString(R.string.weekday))) {
                                timeArrayWeekDaysSetOutbound.add(lineArray);
                            } else if (lineArray[DataFileContants.SERVICE_ID_INDEX].trim().equalsIgnoreCase(getString(R.string.saturday))) {
                                timeArraySaurdaysSetOutbound.add(lineArray);
                            } else if (lineArray[DataFileContants.SERVICE_ID_INDEX].trim().equalsIgnoreCase(getString(R.string.sunday))) {
                                timeArraySundaysHolidaysSetOutbound.add(lineArray);
                            }
                        }
                        // Array is not of correct length
                        Log.i("setUIData", "the line array is not the correct length");
                    }
                }
                timeArrayWeekDaysInbound = new ArrayList<>(timeArrayWeekDaysSetInbound);
                timeArraySaturdaysInbound = new ArrayList<>(timeArraySaurdaysSetInbound);
                timeArraySundaysHolidaysInbound = new ArrayList<>(timeArraySundaysHolidaysSetInbound);

                timeArrayWeekDaysOutbound = new ArrayList<>(timeArrayWeekDaysSetOutbound);
                timeArraySaturdaysOutbound = new ArrayList<>(timeArraySaurdaysSetOutbound);
                timeArraySundaysHolidaysOutbound = new ArrayList<>(timeArraySundaysHolidaysSetOutbound);

                stopNameArrayInbound = new ArrayList<>(stopNameInboundSet);
                stopNameArrayOutbound = new ArrayList<>(stopNameOutboundSet);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Boolean isDataSet) {

            if(isDataSet == true){
                setListView();
            }
        }
    }

    @Override
    public void onTabChanged(String tabId) {

        int tabNumber = Integer.valueOf(tabId);

        switch (tabNumber){
            case 1 : setWeekDays();
                break;
            case 2 : setSaturdays();
                break;
            case 3 : setSundaysHolidays();
                break;
            default: setWeekDays();
                break;
        }
    }

    private void setSundaysHolidays() {
        isWeekDay = false;
        isSaturday = false;
        isSundayHoliday = true;
    }

    private void setSaturdays() {
        isWeekDay = false;
        isSaturday = true;
        isSundayHoliday = false;
    }

    private void setWeekDays(){
        isWeekDay = true;
        isSaturday = false;
        isSundayHoliday = false;
    }
}
