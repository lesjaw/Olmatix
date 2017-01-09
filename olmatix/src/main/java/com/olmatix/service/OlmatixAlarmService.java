package com.olmatix.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.olmatix.database.dbNode;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.ui.activity.MainActivity;
import com.olmatix.utils.Connection;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Lesjaw on 07/01/2017.
 */

public class OlmatixAlarmService extends IntentService {

    public OlmatixAlarmService() {
        super("SchedulingService");
    }
    SharedPreferences sharedPref;
    Boolean mStatusServer;
    int numMessages = 0;
    Boolean Status;
    private dbNode dbnode;
    public static dbNodeRepo mDbNodeRepo;
    private ArrayList<String> notifications;


    @Override
    protected void onHandleIntent(Intent intent) {

        mDbNodeRepo = new dbNodeRepo(getApplicationContext());
        dbnode = new dbNode();

        Log.d("DEBUG", "Intent Service run ");
        Status = Connection.getClient().isConnected();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        //showNotificationNode();
        SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm");

            dbnode.setLog("M = "+Status+" | P = "+mStatusServer + " at " +timeformat.format(System.currentTimeMillis()));
            mDbNodeRepo.insertLog(dbnode);

        if (!Status || !mStatusServer){
            Intent a = new Intent("addNode");
            intent.putExtra("Connect", "con");
            LocalBroadcastManager.getInstance(this).sendBroadcast(a);

        }

        OlmatixAlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }

    private void showNotificationNode() {
        notifications = new ArrayList<>();

        numMessages++;

        SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm");

        notifications.add(numMessages +". MQTT "+String.valueOf(Status) + " : Pref "+String.valueOf(mStatusServer)+ " at " +timeformat.format(System.currentTimeMillis()));
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("New Olmatix server");
        mBuilder.setContentText("You've received new status..");
        mBuilder.setTicker("Olmatix server alert!");
        mBuilder.setAutoCancel(true);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setNumber(numMessages);
        //mBuilder.setGroup(GROUP_KEY_NOTIF);
        //mBuilder.setGroupSummary(true);
        mBuilder.setSound(defaultSoundUri);
        mBuilder.setSmallIcon(R.drawable.ic_conn_green);
        mBuilder.setPriority(Notification.PRIORITY_MAX);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Olmatix status");
        Collections.sort(notifications,Collections.reverseOrder());
        for (int i=0; i < notifications.size(); i++) {
            inboxStyle.addLine(notifications.get(i));
        }

        mBuilder.setStyle(inboxStyle);

        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(4,mBuilder.build());
    }


}
