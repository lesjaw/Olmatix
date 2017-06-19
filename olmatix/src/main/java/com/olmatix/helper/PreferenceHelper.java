package com.olmatix.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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

    public void setHomeCurrent (int current){
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("homecurrent", String.valueOf(current));
        mEditor.apply();
    }

    public int getHomeCurrent() {
        return Integer.parseInt(customCachedPrefs.getString("homecurrent", "0"));
    }

    public void setWidht (int current){
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("widht", String.valueOf(current));
        mEditor.apply();
    }
    public float getWidht() { return Float.parseFloat(customCachedPrefs.getString("widht", "0"));
    }

    public void setLength (int current){
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("length", String.valueOf(current));
        mEditor.apply();
    }
    public float getLength() { return Float.parseFloat(customCachedPrefs.getString("length", "0"));
    }

    public void setHome (int current){
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("home", String.valueOf(current));
        mEditor.apply();
    }
    public int getHome() {
        return Integer.parseInt(customCachedPrefs.getString("home", "0"));

    }

    public double getPhoneLatitude() {
        return Double.parseDouble(customCachedPrefs.getString("phonelatitude", "0"));
    }

    public void setPhoneLatitude(double lat) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("phonelatitude", String.valueOf(lat));
        mEditor.apply();
    }

    public double getPhoneLongitude() {
        return Double.parseDouble(customCachedPrefs.getString("phonelongitude", "0"));
    }

    public void setPhoneLongitude(double lat) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("phonelongitude", String.valueOf(lat));
        mEditor.apply();
    }

    public void initializePrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        homeThold = prefs.getInt("distanceThold", 150);

    }
}
