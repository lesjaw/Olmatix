package com.olmatix.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by USER on 27/05/2017.
 */

public class RingtonePlayingService extends Service
{
    private Ringtone ringtone;
    private Vibrator v;

    private BroadcastReceiver alarmRec = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receive = intent.getStringExtra("message");
            if (receive.equals("ok")){
                stopService();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DEBUG", "onStartCommand: ");
        LocalBroadcastManager.getInstance(this).registerReceiver(
                alarmRec, new IntentFilter("alarm"));

        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);

        if (ringtoneSound != null) {
            ringtoneSound.play();
        }
        // Get instance of Vibrator from current Context
        v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);

        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 100, 1000};

        // The '0' here means to repeat indefinitely
        // '0' is actually the index at which the pattern keeps repeating from (the start)
        // To repeat the pattern from any other point, you could increase the index, e.g. '1'
        v.vibrate(pattern, 0);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        ringtone.stop();
        v.cancel();
    }

    private void stopService (){
        stopSelf();
    }
}