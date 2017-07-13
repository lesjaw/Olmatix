package com.olmatix.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.service.OlmatixService;
import com.olmatix.utils.OlmatixUtils;

import java.util.ArrayList;

/**
 * Created by Lesjaw on 04/12/2016.
 */

public class SplashActivity extends Activity {
    private static final int TAG_CODE_PERMISSION_LOCATION = 1;
    SharedPreferences sharedPref;
    Boolean mStatusServer;
    int count;
    boolean starting = false;


    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
           // Toast.makeText(SplashActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(SplashActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);


        /*new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA)
                .check();*/

        ImageView imgSplash = (ImageView) findViewById(R.id.splash);
        Animation animConn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);

        imgSplash.startAnimation(animConn);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mStatusServer = sharedPref.getBoolean("conStatus", false);


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

    @Override
    protected void onStart() {
        super.onStart();
        starting = true;
        mStatusServer = sharedPref.getBoolean("conStatus", false);
        if (mStatusServer) {
            Intent i = new Intent(getApplication(), MainActivity.class);
            startActivity(i);
            finish();

        } else {
            Toast.makeText(SplashActivity.this, "  No reply from server, exiting now..  ", Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    System.exit(0);
                }
            }, 3000);
        }

            OlmatixUtils.calculateNoOfColumns(this);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("ConnectionStatus");
            Log.d("DEBUG", "onReceive1: "+message);

                if (message!=null) {

                    if (message.equals("NotAuth")) {
                        Intent i = new Intent(getApplication(), LoginActivity.class);
                        startActivity(i);
                        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
                        finish();
                    }
                    if (message.equals("AuthOK")) {
                        if (starting) {
                            Intent i = new Intent(getApplication(), MainActivity.class);
                            startActivity(i);
                            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
                            finish();
                        }
                    }


                }
            if (message==null){
                count++;
                if (count<3){
                    Intent i = new Intent(getApplication(), OlmatixService.class);
                    startService(i);

                }
                if (count==3){

                    Toast.makeText(SplashActivity.this, "  No reply from server, exiting now..  ", Toast.LENGTH_LONG).show();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 5000);

                }
            }
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("splashauth"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        starting = false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        starting = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        starting = false;

    }
}
