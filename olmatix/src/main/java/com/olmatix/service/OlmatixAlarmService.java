package com.olmatix.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.olmatix.utils.Connection;

/**
 * Created by Lesjaw on 07/01/2017.
 */

public class OlmatixAlarmService extends IntentService {

    public OlmatixAlarmService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // BEGIN_INCLUDE(service_onhandle)
        // The URL from which to fetch content.
        Log.d("DEBUG", "Intent Service run ");


        Intent i = new Intent(getApplication(), OlmatixService.class);
        startService(i);
        Log.d("DEBUG", "Starting Service: ");
        boolean conn = Connection.getClient().isConnected();
        Log.d("DEBUG", "onHandleIntent: "+conn);
        OlmatixAlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }

    private void callDis() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplication(), OlmatixService.class);
                stopService(i);
                Log.d("DEBUG", "Stoping Service: ");
                callConn();
            }
        },1000);
    }

    private void callConn() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplication(), OlmatixService.class);
                startService(i);
                Log.d("DEBUG", "Starting Service: ");

            }
        },1000);
    }

}
