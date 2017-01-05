package com.olmatix.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.ui.activity.MainActivity;

/**
 * Created by Lesjaw on 30/12/2016.
 */

public class OlmatixReceiver extends BroadcastReceiver {

    String textNode;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DEBUG", "OlmatixReceiver");
        String action = intent.getAction();
        Log.i("DEBUG", "Broadcast received: " + action);

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent serviceIntent = new Intent(context, OlmatixService.class);
            context.startService(serviceIntent);
        }
         if (intent.getAction().equals("com.olmatix.lesjaw.olmatix.ProximityAlert")) {
             String k = LocationManager.KEY_PROXIMITY_ENTERING;
             // Key for determining whether user is leaving or entering
             boolean state = intent.getBooleanExtra(k, false);
             //Gives whether the user is entering or leaving in boolean form
             if (state) {
                 // Call the Notification Service or anything else that you would like to do here
                 //Toast.makeText(context, "You arrive at home..", Toast.LENGTH_LONG).show();
                 textNode = "You are entering home radius..";

             } else {
                 //Other custom Notification
                 //Toast.makeText(context, "You are leaving home..", Toast.LENGTH_LONG).show();
                 textNode = "You are leaving home..";
             }

             NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
             Intent notificationIntent = new Intent(context, MainActivity.class);
             //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
             Notification notification = createNotification(context, notificationIntent);

             notificationManager.notify(5, notification);
         }
    }

    private Notification createNotification(Context context, Intent intent) {
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_location_red)  // the status icon
                .setTicker(textNode)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Olmatix location Alert!")  // the label of the entry
                .setContentText(textNode)  // the contents of the entry
                //.setContentIntent(intent)  // The intent to send when the entry is clicked
                .setAutoCancel(true)
                .build();

        return notification;
    }


}