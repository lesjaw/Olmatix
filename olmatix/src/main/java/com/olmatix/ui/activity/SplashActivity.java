package com.olmatix.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.olmatix.helper.PreferenceHelper;
import com.olmatix.service.OlmatixService;
import com.olmatix.utils.OlmatixUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by Lesjaw on 04/12/2016.
 */

public class SplashActivity extends AppCompatActivity {

    int flagReceiver =0;
    private Geocoder geocoder;
    private String mProvider;
    private LocationManager mLocateMgr;
    private Location mLocation;
    private Context mContext;
    private Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //enableFullScreen(true);
        mContext = mActivity;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mUserName = sharedPref.getString("user_name", "olmatix1");
        mProvider  = sharedPref.getString("setHomeLocation", "");
        Log.d("DEBUG", "onCreate: HomeLocation  " + mProvider);

        if (mUserName.equals("olmatix1") || mProvider.equals("")) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else {
            if (flagReceiver == 0);
            {
                Intent i = new Intent(this, OlmatixService.class);
                i.putExtra("node_id", "true");
                startService(i);
                /*LocalBroadcastManager.getInstance(this).registerReceiver(
                        mMessageReceiver, new IntentFilter("MQTTStatus"));
                flagReceiver = 1;
                Log.d("Splash = ", "Starting OlmatixService");*/
                flagReceiver = 1;
            }
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }


    protected void enableFullScreen(boolean enabled) {
        int newVisibility =  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

        if(enabled) {
            newVisibility |= View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            Log.d("DEBUG", "enableFullScreen: ");

        }

        getDecorView().setSystemUiVisibility(newVisibility);
    }

    private View getDecorView() {
        return getWindow().getDecorView();
    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case OlmatixUtils.OLMATIX_PERMISSIONS_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mProvider = mLocateMgr.getBestProvider(OlmatixUtils.getGeoCriteria(), true);
                    Log.w("DEBUG", "MY_PERMISSIONS_ACCESS_COARSE_LOCATION permission granted");

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.wtf("DEBUG", "boh. permesso negato su risposta permesso");
                        return;
                    }
                    mLocateMgr.requestLocationUpdates(mProvider, OlmatixUtils.POSITION_UPDATE_INTERVAL, OlmatixUtils.POSITION_UPDATE_MIN_DIST, (LocationListener) this);
                    mLocation = mLocateMgr.getLastKnownLocation(mProvider);
                    // Initialize the location fields
                    if (mLocation != null) {
                        onLocationChanged(mLocation);
                    }

                }
                return;
            }
        }
    }

    private void onLocationChanged(Location mLocation) {
        final double lat = (mLocation.getLatitude());
        final double lng = (mLocation.getLongitude());

        new Thread(new Runnable() {
            @Override
            public void run() {
                String adString = "";
                String loc = null;
                try {

                    List<Address> list;
                    list = geocoder.getFromLocation(lat, lng, 1);

                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        loc = address.getLocality();
                        if (address.getAddressLine(0) != null)
                            adString = ", " + address.getAddressLine(0);
                    }
                } catch (final IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("DEBUG", "Geocoder ERROR", e);
                            /*homedist.setVisibility(View.VISIBLE);
                            homedist.setText(Html.fromHtml("Geocoder <font color=\"#FF4444\">ERROR</font>: " + e.getMessage()));
                            posInfoLine.setBackgroundColor(ContextCompat.getColor(SplashActivity.this, R.color.std_red));*/
                        }
                    });
                    loc = OlmatixUtils.gpsDecimalFormat.format(lat) + " : " + OlmatixUtils.gpsDecimalFormat.format(lng);
                }

                final float[] res = new float[3];
                // Location.distanceBetween(lat, lng, 44.50117265d, 11.34518103, res);
                final PreferenceHelper mPrefHelper = new PreferenceHelper(mContext);
                Location.distanceBetween(lat, lng, mPrefHelper.getHomeLatitude(), mPrefHelper.getHomeLongitude(), res);
                if (mPrefHelper.getHomeLatitude() != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String unit = "m";
                            if (res[0] > 2000) {// usa chilometri
                                unit = "km";
                                res[0] = res[0] / 1000;
                            }
                        }
                    });
                }
            }
        }).start();
    }
}
