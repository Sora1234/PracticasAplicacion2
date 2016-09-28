package com.example.cristianxool.practicasaplicacion2;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Cristian Xool on 29/04/2016.
 */
public class Receiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Start the service, keeping the device awake while the service is
        // launching. This is the Intent to deliver to the service.
        Intent service = new Intent(context, ManagerUser.class);
        Intent service2 = new Intent(context, ManagerRole.class);
        Intent service3 = new Intent(context, SupervisorRole.class);
        Intent service4 = new Intent(context, Alarm.class);

        Receiver.startWakefulService(context, service);
        Receiver.startWakefulService(context, service2);
        Receiver.startWakefulService(context, service3);
        Receiver.startWakefulService(context, service4);
    }
}
