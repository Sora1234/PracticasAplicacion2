package com.example.cristianxool.practicasaplicacion2;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristian Xool on 29/04/2016.
 * cris.xool@gmail.com
 */
public class SupervisorRole extends IntentService {

    // Creating JSON Parser object
    JsonConnectorUser jParserUser = new JsonConnectorUser();
    JsonConnectorSupervisorRole jParserRole = new JsonConnectorSupervisorRole();

    // Data JSONArray
    JSONArray userInfo = null;
    JSONArray roleInfo = null;

    public SupervisorRole() {
        super("SupervisorRole");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if(MainActivity.appRunning == true) {

            long ct = System.currentTimeMillis(); //get current time
            AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(getApplicationContext(), SupervisorRole.class);
            PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, i, 0);
            mgr.set(AlarmManager.RTC_WAKEUP, ct + 30000, pi);

            startChecking();

            /*time = new Timer();
            subTimer sub = new subTimer();
            time.scheduleAtFixedRate(sub, 0, 30000);*/

            Receiver.completeWakefulIntent(intent);
            stopSelf();
        }
    }

    private void startChecking(){
            if(isNetworkAvailable()) {
                new GetData().execute();
            }
    }

    private void register(String macApUserInfo, String macUserInfo, String locationActual, String placeRole, String statusRole){
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

        if(today.second<10){
            getSecond = "0" + Integer.toString(today.second);
        } else{
            getSecond = Integer.toString(today.second);
        }

        getYear = Integer.toString(today.year);
        getDate = getYear + getMonth + getDay + getHour + getMinute + getSecond;
        String date = getDate;
        String macAP = macApUserInfo;
        String macUser = macUserInfo;
        String location = locationActual;
        String place = placeRole;
        String check = statusRole;

        Insert insert = new Insert();
        insert.execute(date, macAP, macUser, location, place, check);
    }//End regiterData

    private class Insert extends AsyncTask<String, String, String> {

        String date = getDate;
        String macAP = macApUserInfo;
        String macUser = macUserInfo;
        String location = locationActual;
        String place = placeRole;
        String check = statusRole;

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("DATE", date));
            nameValuePairs.add(new BasicNameValuePair("MAC_AP",macAP));
            nameValuePairs.add(new BasicNameValuePair("MAC_USER", macUser));
            nameValuePairs.add(new BasicNameValuePair("LOCATION_ACTUAL", location));
            nameValuePairs.add(new BasicNameValuePair("PLACE", place));
            nameValuePairs.add(new BasicNameValuePair("VERIFY", check));

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://148.209.80.124/insertCheckRole.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();
                publishProgress(EntityUtils.toString(entity));

            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }
            return "Check";
        }
    }//End InsertData

    private class GetData extends AsyncTask<String, String, String> {

        /**
         * Getting all datas
         * */
        protected String doInBackground(String... args) {

            // Building Parameters
            List paramsUser = new ArrayList();
            List paramsRole = new ArrayList();

            // getting JSON string from URL
            JSONObject getJsonUser = jParserUser.makeHttpRequest(urlUser, "GET", paramsUser);
            JSONObject getJsonRole = jParserRole.makeHttpRequest(urlRole, "GET", paramsRole);

            // Check your log cat for JSON reponse
            Log.d("All Datas: ", getJsonUser.toString());
            Log.d("All Datas: ", getJsonRole.toString());

            try {
                // Checking for SUCCESS TAG
                int successUser = getJsonUser.getInt(TAG_SUCCESS);
                int successRole = getJsonRole.getInt(TAG_SUCCESS);

                if (successUser == 1 && successRole == 1) {
                    // Data found
                    // Getting Array of Datas
                    userInfo = getJsonUser.getJSONArray(TAG_User_INFO);
                    roleInfo = getJsonRole.getJSONArray(TAG_ROLE_INFO);

                    String getMacUserInfo;
                    String getMacApUserInfo;
                    String getLocationActual;
                    String getMacApRole;
                    String getPlaceRole;
                    Boolean check = false;
                    for (int j=0;j<roleInfo.length();j++){

                        JSONObject userData = userInfo.getJSONObject(0);
                        JSONObject roleData = roleInfo.getJSONObject(j);

                        getMacUserInfo = userData.getString(TAG_MAC_USER);
                        getMacApUserInfo = userData.getString(TAG_MAC_AP);
                        getLocationActual = userData.getString(TAG_LOCATION_ACTUAL);

                        getMacApRole = roleData.getString(TAG_MAC_AP);
                        getPlaceRole = roleData.getString(TAG_PLACE);

                        if(getMacApUserInfo.equals(getMacApRole)){
                            check = true;
                            macUserInfo = getMacUserInfo;
                            macApUserInfo = getMacApUserInfo;
                            locationActual = getLocationActual;
                            placeRole = getPlaceRole;
                            break;
                        } else{
                            check=false;
                            macUserInfo = getMacUserInfo;
                            macApUserInfo = getMacApUserInfo;
                            locationActual = getLocationActual;
                            placeRole = getPlaceRole;
                        }
                    }
                    if(check.equals(true)){
                        if((macApUserInfo.compareTo(MainActivity.lastMacAp)!=0) && (MainActivity.numCheck >= 2)) {
                            statusRole = "VERIFIED";
                            id="1";
                            register(macApUserInfo, macUserInfo, locationActual, placeRole, statusRole);
                            record(macApUserInfo, macUserInfo, locationActual, statusRole, id);
                        }
                        else{
                            statusRole = "CHECKING";
                            id="2";
                            register(macApUserInfo, macUserInfo, locationActual, placeRole, statusRole);
                            record(macApUserInfo, macUserInfo, locationActual, statusRole, id);
                        }
                        MainActivity.numCheck++;
                        MainActivity.lastMacAp=null;
                        MainActivity.lastMacAp=macApUserInfo;
                    }else{
                        statusRole = "UNVERIFIED";
                        id="3";
                        register(macApUserInfo, macUserInfo, locationActual, placeRole, statusRole);
                        record(macApUserInfo, macUserInfo, locationActual, statusRole, id);
                        //MainActivity.lastMacAp="";
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }//End GetData

    //This function check connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    //This function record data in file txt
    private void record(String macApUserInfo, String macUserInfo, String locationActual, String statusRole, String id) {
        String nomarchivo = "Comparacion.txt";
        String contenido = "DATE: "+ getDate + ", Mac_AP: " + macApUserInfo + ", Mac_User: " + macUserInfo + ", Location: " + locationActual + ", Status_Role: " + statusRole + ", Last_Mac_Ap_User: " + MainActivity.lastMacAp + ", Num: " + Integer.toString(MainActivity.numCheck) + ", ID: " + id;
        MainActivity.texto = "\n" + MainActivity.texto + "\n" + contenido;
        try {
            File tarjeta = Environment.getExternalStorageDirectory();
            File file = new File(tarjeta.getAbsolutePath(), nomarchivo);
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
            osw.append(MainActivity.texto + "\n\n");
            osw.flush();
            osw.close();
        } catch (IOException ioe) {
            Toast.makeText(this, "Los datos fueron grabados correctamente",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // url to get all datas list
    private static String urlUser = "http://148.209.80.124/getUserInfo.php";
    private static String urlRole = "http://148.209.80.124/getRoleInfo.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_ROLE_INFO = "role_info";
    private static final String TAG_User_INFO = "user_info";
    private static final String TAG_MAC_AP = "MAC_AP";
    private static final String TAG_PLACE = "PLACE";
    private static final String TAG_MAC_USER = "MAC_USER";
    private static final String TAG_LOCATION_ACTUAL = "PLACE";

    private String macUserInfo;
    private String macApUserInfo;
    private String locationActual;
    private String placeRole;
    private String statusRole;
    private Time today;
    private String getDay;
    private String getMonth;
    private String getYear;
    private String getHour;
    private String getMinute;
    private String getSecond;
    private String getDate;
    private String id;
}
