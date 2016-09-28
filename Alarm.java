package com.example.cristianxool.practicasaplicacion2;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.os.Vibrator;

/**
 * Created by Cristian Xool on 01/06/2016.
 * Modificado.
 */
public class Alarm extends IntentService {


    public Alarm(){
        super("Alarm");
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
            Intent i = new Intent(getApplicationContext(), Alarm.class);
            PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, i, 0);
            mgr.set(AlarmManager.RTC_WAKEUP, ct + 2400000, pi); //60 seconds is 60000 milliseconds*/

            alarm = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            alarm.vibrate(1000);

            /*PowerManager pm2 = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();

            if(isScreenOn == false){

            }*/

            Receiver.completeWakefulIntent(intent);
        }
    }

    private Vibrator alarm;
}
