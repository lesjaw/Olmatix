package com.olmatix.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Lesjaw on 30/12/2016.
 */

public class OmatixReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "OlmatixReceiver");
        Intent serviceIntent = new Intent(context, OlmatixService.class);
        context.startService(serviceIntent);
    }
}