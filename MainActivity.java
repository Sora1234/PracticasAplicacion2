package com.example.cristianxool.practicasaplicacion2;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Cristian Xool on 10/03/2016.
 * cris.xool@gmail.com
 */

public class MainActivity extends ActionBarActivity {

    public static boolean appRunning=true;
    public static String lastMacAp = "";
    public static int numCheck=0;
    public static String texto="";

    ArrayList<HashMap<String, String>> wifi_Info_List;
    ListView lista;

    //Creating JSON Parser object
    JsonConnectorRole jParserRole = new JsonConnectorRole();

    //Data JSONArray
    JSONArray namePlace = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Hashmap for ListView
        wifi_Info_List = new ArrayList<>();

        //Check connection
        if(isNetworkAvailable()) {
            /*startService(new Intent(this, ManagerUser.class));
            startService(new Intent(this, ManagerRole.class));
            startService(new Intent(this, SupervisorRole.class));
            startService(new Intent(this, Alarm.class));*/

            //Start service
                sendBroadcast(new Intent(MainActivity.this, Receiver.class));
                pDialog = new ProgressDialog(MainActivity.this);
                pDialog.setMessage("Cargando datos. Por favor espere...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
        }else {
            Toast.makeText(getApplicationContext(), "CONNECTION NO FOUND", Toast.LENGTH_SHORT).show();
        }

        lista = (ListView) findViewById(R.id.listAllData);

        appRunning=true;

        Button buttonStop = (Button) findViewById(R.id.button_id3);
        buttonStop.setText("Stop");
        buttonStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                stopService(new Intent(MainActivity.this, ManagerUser.class));
                stopService(new Intent(MainActivity.this, ManagerRole.class));
                stopService(new Intent(MainActivity.this, SupervisorRole.class));
                stopService(new Intent(MainActivity.this, Alarm.class));

                appRunning=false;
            }
        });

    }//End onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                //metodoSettings()

                return true;
            case R.id.action_info:
                Intent infoWindow = new Intent(getApplicationContext(),InformerApp.class);
                startActivity(infoWindow);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(isNetworkAvailable()) {
            try {
                timer = new Timer();
                subTimer sub = new subTimer();
                timer.scheduleAtFixedRate(sub, 0, 10000);
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }

    private class subTimer extends TimerTask{
        @Override
        public void run() {
            //Check connection
            if(isNetworkAvailable()) {
                try {
                    if(ManagerRole.getStatus==true) {
                        new GetList().execute();
                        wifi_Info_List.clear();
                        adapter.clear();
                    }
                }catch (RuntimeException e){
                   e.printStackTrace();
                }
            }
        }
    }

    private class GetList extends AsyncTask<String, String, String>{
        /**
         * Getting all datas
         * */

        @Override
        protected String doInBackground(String... args) {
            List params = new ArrayList();
            JSONObject getJsonRole = jParserRole.makeHttpRequest(url, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Datas: ", getJsonRole.toString());

            try {
                // Checking for SUCCESS TAG
                int successPlace = getJsonRole.getInt(TAG_SUCCESS);

                if (successPlace == 1) {

                    // products found
                    // Getting Array of Datas
                    namePlace = getJsonRole.getJSONArray(TAG_ROLE_INFO);
                    JSONObject placeData = namePlace.getJSONObject(0);
                    String place = placeData.getString(TAG_PLACE);

                    if (clavePlace != place){
                        clavePlace = place;
                        places[0]=place;

                        // creating new HashMap
                        HashMap map = new HashMap();
                        // adding each child node to HashMap key => value
                        map.put(TAG_PLACE, place);
                        wifi_Info_List.add(map);

                        check=true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            super.onPostExecute(file_url);

            pDialog.dismiss();

            if(check==true){
                runOnUiThread(new Runnable() {
                    public void run() {

                        /*adapter = new SimpleAdapter(
                                MainActivity.this,
                                wifi_Info_List,
                                R.layout.list_item,
                                new String[]{
                                        TAG_PLACE,
                                },
                                new int[]{
                                        R.id.single_post_tv_place,

                                });*/
                        // updating listview
                        //setListAdapter(adapter);
                        adapter = new ArrayAdapter(MainActivity.this,R.layout.list_item, R.id.single_post_tv_place, places);
                        System.out.println("Actualizando");
                        try {
                            adapter.notifyDataSetChanged();
                            lista.setAdapter(adapter);
                        }catch (RuntimeException e){
                            e.printStackTrace();
                        }
                    }
                });
                check=false;
            }

        }
    }//End GetList

    @Override
    public void onStop(){
        super.onStop();
        appRunning=true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        appRunning=true;
    }

    //This function check connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null;
    }

    // Progress Dialog
    private ProgressDialog pDialog;

    // url to get all datas list
    private static String url = "http://148.209.80.124/getPlaceRole.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ROLE_INFO = "role_info";
    private static final String TAG_PLACE = "PLACE";

    private Timer timer;
    private String clavePlace = "a";
    private String[] places = new String[1];
    private boolean check=false;
    private ArrayAdapter adapter;
}