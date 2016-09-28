package com.example.cristianxool.practicasaplicacion2;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Cristian Xool on 28/04/2016.
 * cris.xool@gmail.com
 */
public class ManagerRole extends IntentService {

    public static boolean getStatus;
    public String getLocationAp;
    public String getPlaceAp;
    public String getMacApRole;

    //Creating JSON Parser object
    JsonConnectorPlace jParserPlace = new JsonConnectorPlace();
    JsonConnectorAP jParserAP = new JsonConnectorAP();
    JsonConnectorManagerRole jParserRole = new JsonConnectorManagerRole();

    ArrayList<String> role_info_mac_ap;
    ArrayList<String> role_info_location_ap;

    //Data JSONArray
    JSONArray places = null;
    JSONArray apInfos = null;
    JSONArray rolInfo = null;

    public ManagerRole() {
        super("ManagerRole");
    }

    @Override
    public void onCreate(){
        super.onCreate();

        role_info_mac_ap = new ArrayList<>();
        role_info_location_ap = new ArrayList<>();
        alarm = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        if(MainActivity.appRunning == true) {

            long ct = System.currentTimeMillis(); //get current time
            AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(getApplicationContext(), ManagerRole.class);
            PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, i, 0);
            mgr.set(AlarmManager.RTC_WAKEUP, ct + 60000, pi);

            startCreating();

            MainActivity.appRunning = true;

        /*time = new Timer();
        subTimer sub = new subTimer();
        time.scheduleAtFixedRate(sub, 0, 60000);*/

            Receiver.completeWakefulIntent(intent);
            stopSelf();
        }
    }

    private void startCreating(){

            if(isNetworkAvailable()) {
                new GetData().execute();
                role_info_mac_ap.clear();
                role_info_location_ap.clear();
            }
    }

    private void register(){

        for (int i = 0; i < role_info_mac_ap.size(); i++) {
            today = new Time(Time.getCurrentTimezone());
            today.setToNow();

            if(today.monthDay<10) {
                getDay = "0" + Integer.toString(today.monthDay);
            }else{
                getDay = Integer.toString(today.monthDay);
            }

            if((today.month+1)<10) {
                getMonth = "0" + Integer.toString(today.month+1);
            }else{
                getMonth = Integer.toString(today.month+1);
            }

            if(today.hour<10) {
                getHour = "0" + Integer.toString(today.hour);
            }else{
                getHour = Integer.toString(today.hour);
            }

            if(today.minute<10) {
                getMinute = "0" + Integer.toString(today.minute);
            }else{
                getMinute = Integer.toString(today.minute);
            }

            getYear = Integer.toString(today.year);
            getDate = getYear + getMonth + getDay + getHour + getMinute;
            getMacApRole = role_info_mac_ap.get(i).toString();
            getPlaceAp = getPlace;
            getLocationAp = role_info_location_ap.get(i).toString();
            String date = getDate;
            String macAP = getMacApRole;
            String locationAP = getLocationAp;
            String place = getPlaceAp;

            Insert insert = new Insert();
            insert.execute(date, macAP, locationAP, place);

        }
    }//End register

    private class Insert extends AsyncTask<String, String, String> {

        String date = getDate;
        String macAP = getMacApRole;
        String locationAP = getLocationAp;
        String place = getPlaceAp;

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("DATE", date));
            nameValuePairs.add(new BasicNameValuePair("MAC_AP",macAP));
            nameValuePairs.add(new BasicNameValuePair("LOCATION_AP",locationAP));
            nameValuePairs.add(new BasicNameValuePair("PLACE", place));

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://148.209.80.124/insertRoleInfo.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();
                publishProgress(EntityUtils.toString(entity));

            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }
            return "Check";
        }
    }//End Insert

    private class GetData extends AsyncTask<String, String, String> {
        /**
         * Getting all datas
         * */
        @Override
        protected String doInBackground(String... args) {

            // Building Parameters
            List paramsPlace = new ArrayList();
            List paramsAp = new ArrayList();
            List paramsRole = new ArrayList();

            // getting JSON string from URL
            JSONObject getJsonPlace = jParserPlace.makeHttpRequest(urlPlace, "GET", paramsPlace);
            JSONObject getJsonAp = jParserAP.makeHttpRequest(urlApInfo, "GET", paramsAp);
            JSONObject getJsonRole = jParserRole.makeHttpRequest(urlRoleInfo, "GET", paramsRole);

            // Check your log cat for JSON reponse
            Log.d("All Datas: ", getJsonPlace.toString());
            Log.d("All Datas: ", getJsonAp.toString());
            Log.d("All Datas: ", getJsonRole.toString());

            try {
                // Checking for SUCCESS TAG
                int successPlace = getJsonPlace.getInt(TAG_SUCCESS);
                int sucessAp = getJsonAp.getInt(TAG_SUCCESS);
                int sucessRole = getJsonRole.getInt(TAG_SUCCESS);

                if (successPlace == 1 && sucessAp == 1 && sucessRole == 1) {
                    // products found
                    // Getting Array of Datas
                    places = getJsonPlace.getJSONArray(TAG_PLACE_INFO);
                    JSONObject placeData = places.getJSONObject(0);
                    String place = placeData.getString(TAG_PLACE);
                    getPlace = place;

                    apInfos = getJsonAp.getJSONArray(TAG_AP_INFO);
                    for(int i=0;i<apInfos.length();i++) {
                        JSONObject apData = apInfos.getJSONObject(i);

                        String placeAP = apData.getString(TAG_PLACE);

                        if(place.equals(placeAP)) {

                            String macAP = apData.getString(TAG_MAC_AP);
                            String locationAP = apData.getString(TAG_LOCATION_AP);

                            role_info_mac_ap.add(macAP);
                            role_info_location_ap.add(locationAP);

                        }
                    }
                    rolInfo = getJsonRole.getJSONArray(TAG_ROLE_INFO);
                    JSONObject roleData = rolInfo.getJSONObject(0);
                    String date = roleData.getString(TAG_DATE);

                    String year = date.substring(0, 4);
                    String month = date.substring(4, 6);
                    String day = date.substring(6, 8);
                    String hour = date.substring(8, 10);
                    String minute = date.substring(10, 12);
                    String roleDate = month + day + year + hour + minute;

                    SimpleDateFormat format = new SimpleDateFormat("MMddyyyyHHmm");

                    Date d1 = null;
                    Date d2 = null;

                    try {
                        d1 = format.parse(roleDate);
                        d2 = format.parse(actualDate());

                        //in milliseconds
                        long diff = d2.getTime() - d1.getTime();

                        long diffMinutes = diff / (60 * 1000);

                        if (diffMinutes >= 5){
                            register();
                            MainActivity.numCheck=0;
                            // Vibrate for 500 milliseconds
                            alarm.vibrate(2000);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{
                    if (successPlace == 1 && sucessAp == 1 && sucessRole == 0) {
                        // products found
                        // Getting Array of Datas
                        places = getJsonPlace.getJSONArray(TAG_PLACE_INFO);
                        JSONObject placeData = places.getJSONObject(0);
                        String place = placeData.getString(TAG_PLACE);
                        getPlace = place;


                        apInfos = getJsonAp.getJSONArray(TAG_AP_INFO);
                        for(int i=0;i<apInfos.length();i++) {
                            JSONObject apData = apInfos.getJSONObject(i);

                            String placeAP = apData.getString(TAG_PLACE);

                            if(place.equals(placeAP)) {

                                String macAP = apData.getString(TAG_MAC_AP);
                                String locationAP = apData.getString(TAG_LOCATION_AP);

                                role_info_mac_ap.add(macAP);
                                role_info_location_ap.add(locationAP);

                            }
                        }
                        register();
                        // Vibrate for 500 milliseconds
                        alarm.vibrate(2000);
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

            getStatus=true;
        }
    }//End GetData

    //This function check connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private String actualDate(){


        String getDay;
        String getMonth;
        String getYear;
        String getHour;
        String getMinute;
        String getDate;

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        if(today.monthDay<10) {
            getDay = "0" + Integer.toString(today.monthDay);
        }else{
            getDay = Integer.toString(today.monthDay);
        }

        if((today.month+1)<10) {
            getMonth = "0" + Integer.toString(today.month+1);
        }else{
            getMonth = Integer.toString(today.month+1);
        }

        if(today.hour<10) {
            getHour = "0" + Integer.toString(today.hour);
        }else{
            getHour = Integer.toString(today.hour);
        }

        if(today.minute<10) {
            getMinute = "0" + Integer.toString(today.minute);
        }else{
            getMinute = Integer.toString(today.minute);
        }

        getYear = Integer.toString(today.year);
        getDate = getMonth + getDay + getYear + getHour + getMinute;

        return getDate;
    }

    // url to get all datas list
    private static String urlPlace = "http://148.209.80.124/getPlace.php";
    private static String urlApInfo = "http://148.209.80.124/getApInfo.php";
    private static String urlRoleInfo = "http://148.209.80.124/getRoleInfo.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_AP_INFO = "ap_info";
    private static final String TAG_ROLE_INFO = "role_info";
    private static final String TAG_PLACE_INFO = "place";
    private static final String TAG_MAC_AP = "MAC_AP";
    private static final String TAG_LOCATION_AP = "LOCATION_AP";
    private static final String TAG_PLACE = "PLACE";
    private static final String TAG_DATE = "DATE";

    private String getPlace;
    private Time today;
    private Vibrator alarm;
    private String getDay;
    private String getMonth;
    private String getYear;
    private String getHour;
    private String getMinute;
    private String getDate;

}
