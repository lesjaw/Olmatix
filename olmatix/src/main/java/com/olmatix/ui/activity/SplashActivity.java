package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.olmatix.service.OlmatixService;

/**
 * Created by Lesjaw on 04/12/2016.
 */

public class SplashActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    Boolean mStatusServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mUserName = sharedPref.getString("user_name", "olmatix1");
        if (mUserName.equals("olmatix1")){
            Intent i = new Intent(getApplication(), LoginActivity.class);
            startActivity(i);
            finish();
        } else {

            Intent i = new Intent(this, OlmatixService.class);
            startService(i);
        }

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("ConnectionStatus");
            Log.d("DEBUG", "onReceive1: "+message);

                if (message!=null) {
                    Log.d("DEBUG", "onReceive2: " + message);

                    if (message.equals("NotAuth")) {
                        Intent i = new Intent(getApplication(), LoginActivity.class);
                        startActivity(i);
                        Log.d("DEBUG", "onReceive2: " + message);
                        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
                        finish();
                    }
                    if (message.equals("AuthOK")) {
                        Intent i = new Intent(getApplication(), MainActivity.class);
                        startActivity(i);
                        Log.d("DEBUG", "onReceive2: " + message);
                        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
                        finish();
                    }
                }
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("MQTTStatus"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);

    }
}
