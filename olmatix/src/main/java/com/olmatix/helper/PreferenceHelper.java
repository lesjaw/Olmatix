package com.olmatix.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.Serializable;

/**
 * Created by Rahman on 12/26/2016.
 */

public class PreferenceHelper {
    private SharedPreferences customCachedPrefs;
    private Context context;
    private int homeThold;

    public PreferenceHelper(Context context) {
        super();
        this.context = context;
        customCachedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        initializePrefs();
    }

    public Context getContex() {
        return context;
    }

    public SharedPreferences getCustomPref() {
        return customCachedPrefs;
    }


    public double getHomeLatitude() {
        return Double.parseDouble(customCachedPrefs.getString("homelatitude", "0"));
    }

    public void setHomeLatitude(double lat) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("homelatitude", String.valueOf(lat));
        mEditor.apply();
    }

    public double getHomeLongitude() {
        return Double.parseDouble(customCachedPrefs.getString("homelongitude", "0"));
    }

    public void setHomeLongitude(double lat) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("homelongitude", String.valueOf(lat));
        mEditor.apply();
    }

    public int getHomeThresholdDistance() {
        return homeThold;
    }

    public void initializePrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        homeThold = prefs.getInt("distanceThold", 150);

    }
}
