package com.olmatix.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.olmatix.service.OlmatixService;

/**
 * Created by Lesjaw on 04/12/2016.
 */

public class SplashActivity extends AppCompatActivity {

    int flagReceiver =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mUserName = sharedPref.getString("user_name", "olmatix1");

        /*if (flagReceiver==0) {
            Intent i = new Intent(this, OlmatixService.class);
            startService(i);
            flagReceiver =1;
        }*/

        if (mUserName.equals("olmatix1") ) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else {
            if (flagReceiver == 0);
            {
                Intent i = new Intent(this, OlmatixService.class);
                startService(i);
                flagReceiver = 1;
            }
           /* Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);*/
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("StartMain");
            if (message==null){
                message = "false";
            }
            if (message.equals("true")) {
            finish();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

    }
}
