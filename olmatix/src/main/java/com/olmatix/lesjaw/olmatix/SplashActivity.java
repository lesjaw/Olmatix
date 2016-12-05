package com.olmatix.lesjaw.olmatix;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Lesjaw on 04/12/2016.
 */

public class SplashActivity extends AppCompatActivity {

    MyReceiver myReceiver;



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

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(OlmatixService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);


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

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            int datapassed = arg1.getIntExtra("DATAPASSED",0);
            if (datapassed == 0) {
              //  Toast.makeText(getApplicationContext(), "StatusMain -> "+ String.valueOf(datapassed), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onStop() {
        unregisterReceiver(myReceiver);
        super.onStop();

    }
}
