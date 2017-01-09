package com.olmatix.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Lesjaw on 07/01/2017.
 */

public class OlmatixAlarmService extends IntentService {

    public OlmatixAlarmService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("DEBUG", "Intent Service run ");



        OlmatixAlarmReceiver.completeWakefulIntent(intent);
        // END_INCLUDE(service_onhandle)
    }


}
