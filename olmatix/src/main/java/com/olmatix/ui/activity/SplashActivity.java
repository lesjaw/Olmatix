package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.olmatix.service.OlmatixService;

/**
 * Created by Lesjaw on 04/12/2016.
 */

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mServerURL = sharedPref.getString("server_address", "cloud.olmatix.com");
        Log.d("DEBUG", "Server Address 1: " + mServerURL);
        String mServerPort = sharedPref.getString("server_port", "1883");
        Log.d("DEBUG", "Server Port 1: " + mServerPort);
        String mUserName = sharedPref.getString("user_name", "olmatix1");
        Log.d("DEBUG", "User Name 1: " + mUserName);
        String mPassword = sharedPref.getString("password", "olmatix");
        Log.d("DEBUG", "Password 1: " + mPassword);


        Intent i = new Intent(this, OlmatixService.class);
        startService(i);

        if (mUserName.equals("olmatix1") ) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }



    @Override
    protected void onStop() {
        super.onStop();

    }
}
