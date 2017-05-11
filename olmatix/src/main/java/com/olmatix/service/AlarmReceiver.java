package com.olmatix.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AlertDialog;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.model.SceneModel;
import com.olmatix.ui.activity.SplashActivity;
import com.olmatix.ui.fragment.Scene;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by android on 4/28/2017.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(final Context context, Intent intent) {
        // this will update the UI with message
        try {

            Uri alarmUri = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_ALARM);

            if (alarmUri == null) {
                alarmUri = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
            ringtone.play();

            // this will send a notification message

            ComponentName comp = new ComponentName(context.getPackageName(),
                    AlarmService.class.getName());
            intent.setComponent(comp);

            // If extended by BroadcastReceiver class then comment this code
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        } catch (Exception ex) {

        }

    }
}