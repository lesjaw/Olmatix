package com.olmatix.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.olmatix.database.dbNode;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.ui.activity.SplashActivity;

import java.text.SimpleDateFormat;

/**
 * Created by Lesjaw on 30/12/2016.
 */

public class OlmatixReceiver extends BroadcastReceiver {

    String textNode;
    int homestat;
    int homestatcur;
    OlmatixAlarmReceiver alarm;
    dbNode dbnode;
    dbNodeRepo mDbNodeRepo;
    PreferenceHelper mPrefHelper;



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mPrefHelper = new PreferenceHelper(context);


        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent serviceIntent = new Intent(context, OlmatixService.class);
            context.startService(serviceIntent);
            alarm = new OlmatixAlarmReceiver();
            alarm.setAlarm(context);
        }
        if (intent.getAction().equals("com.olmatix.lesjaw.olmatix.ProximityAlert")) {
             String k = LocationManager.KEY_PROXIMITY_ENTERING;
             // Key for determining whether user is leaving or entering
             boolean state = intent.getBooleanExtra(k, false);
             //Gives whether the user is entering or leaving in boolean form
             if (state) {
                 // Call the Notification Service or anything else that you would like to do here
                 textNode = "You are entering home radius location..";
                 mPrefHelper.setHomeCurrent(0);

             } else {
                 //Other custom Notification
                 textNode = "You are leaving home..";
                 mPrefHelper.setHomeCurrent(1);
             }

            homestat = mPrefHelper.getHomeCurrent();
            homestatcur  = mPrefHelper.getHome();

            Log.i("DEBUG", "Broadcast received: " + action+" : " +homestat +" : "+homestatcur);

            if (homestat!=homestatcur) {
                 NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                 Intent notificationIntent = new Intent(context, SplashActivity.class);
                 PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
                 mPrefHelper.setHome(homestat);
                 Log.d("DEBUG", "onReceive: "+homestatcur +" ; "+homestat);

                 dbnode = new dbNode();
                 mDbNodeRepo = new dbNodeRepo(context);

                 SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm");
                 dbnode.setTopic(textNode);
                 dbnode.setMessage("at "+timeformat.format(System.currentTimeMillis()));
                 mDbNodeRepo.insertDbMqtt(dbnode);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                final Boolean mSwitch_Notif = sharedPref.getBoolean("switch_loc", true);
                if (mSwitch_Notif) {

                    Notification notification = new Notification.Builder(context)
                            .setSmallIcon(R.drawable.ic_location_red)  // the status icon
                            .setTicker(textNode)  // the status text
                            .setWhen(System.currentTimeMillis())  // the time stamp
                            .setContentTitle("Olmatix location Alert!")  // the label of the entry
                            .setContentText(textNode)  // the contents of the entry
                            .setContentIntent(pendingIntent)  // The intent to send when the entry is clicked
                            .setAutoCancel(true)
                            .build();

                    notificationManager.notify(7, notification);
                }
             }
         }
    }


}