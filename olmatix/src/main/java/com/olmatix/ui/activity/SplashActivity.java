package com.olmatix.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Lesjaw on 04/12/2016.
 */

public class SplashActivity extends AppCompatActivity {

    int flagReceiver =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mServerURL = sharedPref.getString("server_address", "cloud.olmatix.com");
        String mServerPort = sharedPref.getString("server_port", "1883");
        String mUserName = sharedPref.getString("user_name", "olmatix1");
        String mPassword = sharedPref.getString("password", "olmatix");
        /*Log.d("DEBUG", "Server Address 1: " + mServerURL);
        Log.d("DEBUG", "Server Port 1: " + mServerPort);
        Log.d("DEBUG", "User Name 1: " + mUserName);
        Log.d("DEBUG", "Password 1: " + mPassword);
*/

        /*if (flagReceiver==0) {
            Intent i = new Intent(this, OlmatixService.class);
            startService(i);
            flagReceiver =1;
            Log.d("SplashActivity = ", "Starting OlmatixService");
        }*/

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
