package com.olmatix.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.ui.activity.SplashActivity;

/**
 * Created by android on 4/28/2017.
 */

public class AlarmService extends IntentService {

    private NotificationManager alarmNotificationManager;
    public AlarmService() {
        super("AlarmService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        sendNotification("Time Scheduled Alarm Test");
        //Jolly this is where the scene detail should run.. you should put the code logic here, how
        //we execute the node id and node channel run with command (ON or OFF)
        //I think the much better way is we make some data comparison, when the intent run (got executed)
        //check scene table time, if the time in scene = time of this intent occur, then exucute the scene detail table
        //but if you have better way just do it..
        //this is currently the logic i can think of for this moment..
        //thank you dear.. btw tommorow i send olmatix device to gujarat..
    }

    private void sendNotification(String msg) {
        // NotificationManager class to notify the user of events            // that happen. This is how you tell the user that something           //   has   happened in the background.
        alarmNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class), 0);

        // set icon, title and message for notification

        NotificationCompat.Builder alamNotificationBuilder = (NotificationCompat.Builder) new   NotificationCompat.Builder(
                this).setContentTitle("Time Alaram")
                .setSmallIcon(R.mipmap.olmatixlogo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        alamNotificationBuilder.setContentIntent(contentIntent);
        alarmNotificationManager.notify(1, alamNotificationBuilder. build());
    }

}