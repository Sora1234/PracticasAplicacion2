package com.example.cristianxool.practicasaplicacion2;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.text.format.Time;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristian Xool on 25/04/2016.
 * cris.xool@gmail.com
 */
public class ManagerUser extends IntentService {

    public String getMacAp;
    public String getMacUser;
    public static boolean serviceStatus=true;

    public ManagerUser() {
        super("ManagerUser");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        WifiManager wf = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wifiLock = wf.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"wifiLock");
        wifiLock.acquire();

        PowerManager pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "wakeLock");
        wakeLock.acquire();

        if(MainActivity.appRunning == true) {

            long ct = System.currentTimeMillis(); //get current time
            AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(getApplicationContext(), ManagerUser.class);
            PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, i, 0);
            mgr.set(AlarmManager.RTC_WAKEUP, ct + 30000, pi);

        /*time = new Timer();
        subTimer sub = new subTimer();
        time.scheduleAtFixedRate(sub, 0, 30000);*/

            register();
            Receiver.completeWakefulIntent(intent);

            stopSelf();
        }
    }

    private void register(){


            if(isNetworkAvailable()) {

                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();

                getMacUser = connectionInfo.getMacAddress();
                String getBssid = connectionInfo.getBSSID();
                //The last character of the string is changed to obtain the macaddress AP
                char[] bssi = getBssid.toCharArray();
                bssi[16] = '0';
                getMacAp = String.valueOf(bssi);

                today = new Time(Time.getCurrentTimezone());
                today.setToNow();

                if (today.monthDay < 10) {
                    getDay = "0" + Integer.toString(today.monthDay);
                } else {
                    getDay = Integer.toString(today.monthDay);
                }

                if ((today.month + 1) < 10) {
                    getMonth = "0" + Integer.toString(today.month + 1);
                } else {
                    getMonth = Integer.toString(today.month + 1);
                }

                if (today.hour < 10) {
                    getHour = "0" + Integer.toString(today.hour);
                } else {
                    getHour = Integer.toString(today.hour);
                }

                if (today.minute < 10) {
                    getMinute = "0" + Integer.toString(today.minute);
                } else {
                    getMinute = Integer.toString(today.minute);
                }

                getYear = Integer.toString(today.year);

                String macAp = getMacAp;
                String day = getDay;
                String month = getMonth;
                String year = getYear;
                String hour = getHour;
                String minute = getMinute;
                String macUser = getMacUser;

                Insert insert = new Insert();
                insert.execute(macUser, day, month, year, hour, minute, macAp);
            }
    }//End register

    private class Insert extends AsyncTask<String, String, String> {

        String macAp = getMacAp;
        String day = getDay;
        String month = getMonth;
        String year = getYear;
        String hour = getHour;
        String minute = getMinute;
        String macUser = getMacUser;

        @Override
        protected String doInBackground(String... args) {

            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair("MAC_USER", macUser));
            nameValuePairs.add(new BasicNameValuePair("DAY", day));
            nameValuePairs.add(new BasicNameValuePair("MONTH", month));
            nameValuePairs.add(new BasicNameValuePair("YEAR", year));
            nameValuePairs.add(new BasicNameValuePair("HOUR", hour));
            nameValuePairs.add(new BasicNameValuePair("MINUTE", minute));
            nameValuePairs.add(new BasicNameValuePair("MAC_AP", macAp));

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://148.209.80.124/insertUserInfo.php");
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();
                publishProgress(EntityUtils.toString(entity));

            } catch (ClientProtocolException e) {

            } catch (IOException e) {

            }
            return "success";
        }
    }//End Insert

    //This function check connection
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private Time today;
    private String getDay;
    private String getMonth;
    private String getYear;
    private String getHour;
    private String getMinute;
}
