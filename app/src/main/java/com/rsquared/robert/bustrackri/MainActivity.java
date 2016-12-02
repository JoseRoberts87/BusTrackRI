package com.rsquared.robert.bustrackri;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TabHost.OnTabChangeListener {

    private String itemTitle = "";
    private String route_Id = "";
    private List<String> stopNameArrayInbound;
    private List<String> stopNameArrayOutbound;

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


        initialize();
        TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab_test1").setIndicator(getString(R.string.tab_weekday)).setContent(R.id.tab1));
        tabHost.addTab(tabHost.newTabSpec("tab_test2").setIndicator(getString(R.string.tab_saturday)).setContent(R.id.tab2));
        tabHost.addTab(tabHost.newTabSpec("tab_test3").setIndicator(getString(R.string.tab_sunday_holiday)).setContent(R.id.tab3));
        tabHost.setOnTabChangedListener(this);

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

            bundle.putString("url", urlTitle);

            startActivity(new Intent("android.intent.action.MAPACTIVITY").putExtras(bundle));
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        String itemTitle = (String) item.getTitle();
        this.itemTitle = itemTitle;

        // Handle navigation view item clicks here.
        /*String urlTitle = String.valueOf(item.getTitle());
        Bundle bundle = new Bundle();

        bundle.putString("url", urlTitle);

        startActivity(new Intent("android.intent.action.MAPACTIVITY").putExtras(bundle));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);*/
        return true;
    }

    private void createDialog(String stopName){


        AlertDialog.Builder builder = new AlertDialog.Builder(this);



//            // Add the buttons
//        builder.setPositiveButton(R.string.inbound, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                // User clicked OK button
//
//            }
//        });
//        builder.setNegativeButton(R.string.outbound, new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                // User cancelled the dialog
//            }
//        });

        String[] firstarr = {"first","second","third","first","first","first"};
        builder.setTitle(stopName);
        builder.setItems(firstarr, null);
        builder.setIcon(R.drawable.red_dot);
        //builder.setIconAttribute(R.);


//        List<String> listofstuff2 = getBusList("route_1");
//        String[] nerArr2 = new String[listofstuff2.size()];
//        nerArr2 = listofstuff.toArray(nerArr2);
//
//        String[] firstarr1 = {"second\t\t\t\t\t\t\tLine","second","second","second","second","second", "second","second","second","second","second","second"};
//        builder.setItems(firstarr1, null);

// Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

/*    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this));
        builder.setTitle("stops");
///                .setItems(R.array.colors_array, null);
        return builder.create();
    }*/


    private void initialize(){
        try {

            final ListView listView = (ListView) findViewById(R.id.listview);


            String routeName = "";
            if(itemTitle.isEmpty()){
                routeName = "route_1";
            }else{
                routeName = "route_" + route_Id;
            }

            setUIData(routeName);

            List<String> stopList;

            if(isInbound) {
                stopList = stopNameArrayInbound;
            }else{
                stopList = stopNameArrayOutbound;
            }
            ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stopList);
            listView.setAdapter(stringArrayAdapter);
            AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String stopName = (String) listView.getItemAtPosition(position);
                    createDialog(stopName);

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

    private List<String> getBusList(String fileName){

        int resourceId = this.getResources().getIdentifier(fileName, "raw", this.getPackageName());
        List<String> busArray = new ArrayList<>();

        try {
            InputStream iS = getResources().openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line;
            while ((line = reader.readLine()) != null) {
                busArray.add(line);
            }
            Log.i("getTextFromFIle", busArray.toString());

//            stopNameArray = busArray;
        }catch (Exception e){
            e.printStackTrace();
        }
        return busArray;
    }

    private void setUIData(String fileName){

        int resourceId = this.getResources().getIdentifier(fileName, "raw", this.getPackageName());

        try {
            InputStream iS = getResources().openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line;
            Set<String> stopNameInboundSet = new HashSet<>();
            Set<String> stopNameOutboundSet = new HashSet<>();

            while ((line = reader.readLine()) != null) {
                    String[] lineArray = line.split(",");

                    if(line.length() >= DataFileContants.ARRAY_LENGTH) {

                        // set everything in here
                        if (lineArray[DataFileContants.DIRECTION_ID].trim().equalsIgnoreCase("0")) {
                            stopNameInboundSet.add(lineArray[DataFileContants.STOP_NAME_INDEX]);
                        } else {
                            stopNameOutboundSet.add(lineArray[DataFileContants.STOP_NAME_INDEX]);
                        }



                    }else{
                        // Array is not of correct length
                        Log.i("setUIData", "the line array is not the correct length");
                    }
                stopNameArrayInbound = new ArrayList<>(stopNameInboundSet);
                stopNameArrayOutbound = new ArrayList<>(stopNameOutboundSet);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onTabChanged(String tabId) {


    }
}
