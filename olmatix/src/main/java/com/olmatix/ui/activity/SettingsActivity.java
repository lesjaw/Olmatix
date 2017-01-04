package com.olmatix.ui.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.Toast;

import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.ui.fragment.SettingsFragment;
import com.olmatix.utils.OlmatixUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by Lesjaw on 02/12/2016.
 */

public class SettingsActivity extends SettingsFragment {


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener
            = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);

            SharedPreferences sharedPref = preference.getSharedPreferences();
            boolean mSwitch_Conn = sharedPref.getBoolean("switch_conn", true);

           /* if (mSwitch_Conn) {
                Log.d("DEBUG", "SwitchConnPreff: " + mSwitch_Conn);
            }
            if (!mSwitch_Conn){
                Log.d("DEBUG", "SwitchConnPreff: "+ mSwitch_Conn);
            }*/

            return true;
        }


    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            //actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }


    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || NetworkPreferenceFragment.class.getName().equals(fragmentName)
                || MiscPreferenceFragment.class.getName().equals(fragmentName)
                || SetupPreferenceFragment.class.getName().equals(fragmentName)
                || AboutPreferenceFragment.class.getName().equals(fragmentName);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NetworkPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_network);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            // bindPreferenceSummaryToValue(findPreference("server_conn"));
            bindPreferenceSummaryToValue(findPreference("server_address"));
            bindPreferenceSummaryToValue(findPreference("server_port"));
            bindPreferenceSummaryToValue(findPreference("user_name"));
            bindPreferenceSummaryToValue(findPreference("password"));

            SwitchPreference pref = (SwitchPreference) findPreference("switch_conn");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MiscPreferenceFragment extends PreferenceFragment {
        private SharedPreferences pref;
        private LocationManager mLocationMgr;
        private String mProvider;
        private Preference setLocation;
        String loc = null;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_misc);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("setLocation"));

            initLocationPref();
            //resetMesg(setLocation);
        }


        private void initLocationPref() {
            final Preference setLocation = findPreference("setLocation");
            mLocationMgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            setLocation.setOnPreferenceClickListener(LocationClickListener());
            resetMesg(setLocation);
        }

        private Preference.OnPreferenceClickListener LocationClickListener() {

            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final PreferenceHelper mPrefHelper;
                    try {
                        mProvider = mLocationMgr.getBestProvider(OlmatixUtils.getGeoCriteria(), true);
                        Location mLocation = mLocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (mLocation != null){
                            mLocation= mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }

//                        Log.d("DEBUG", "location mpref: " + mLocation.getLatitude() + " "+mLocation.getLongitude() );
                        mPrefHelper = new PreferenceHelper(getActivity());

                        if (mLocation!=null){
                            mPrefHelper.setHomeLatitude(mLocation.getLatitude());
                            mPrefHelper.setHomeLongitude(mLocation.getLongitude());
                            mPrefHelper.initializePrefs();
                            resetMesg(setLocation);

                            Toast.makeText(getActivity(), getString(R.string.opt_homepos_set), Toast.LENGTH_SHORT).show();
                        } else {
                            mPrefHelper.setHomeLatitude(0);
                            mPrefHelper.setHomeLongitude(0);
                            Toast.makeText(getActivity(), getString(R.string.opt_homepos_set_false), Toast.LENGTH_SHORT).show();

                        }



                    } catch (SecurityException ex) {
                        Log.d("DEBUG", "Permission Denied: " + ex );
                        Toast.makeText(getActivity(), "Permission location denied from user", Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e){
                        Log.e("DEBUG", getString(R.string.opt_homepos_err), e);
                        Toast.makeText(getActivity(), getString(R.string.opt_homepos_err), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            };

        }



        private void resetMesg(Preference setLocation) {
            final PreferenceHelper mPrefHelper = new PreferenceHelper(getActivity().getApplicationContext());
            if (mPrefHelper.getHomeLatitude() != 0) {

                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> list;
                try {
                    list = geocoder.getFromLocation(mPrefHelper.getHomeLatitude(), mPrefHelper.getHomeLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        loc = address.getLocality();
                        Log.d("DEBUG", "resetMesg: " + loc);
                    }
                } catch (IOException e) {
                    Log.e("DEBUG", "LOCATION ERR:" + e.getMessage());
                }

                Log.d("DEBUG", "resetMesg: " + mPrefHelper.getHomeLatitude() + ":" + mPrefHelper.getHomeLongitude());

                if (setLocation!=null) {

                    setLocation.setSummary(getString(R.string.opt_homepos_set) + ": " + (loc == null ? "" : loc) + " ("
                            + mPrefHelper.getHomeLatitude() + " : " + mPrefHelper.getHomeLongitude() + ")");
                }
            }
           // setLocation.setSummary(mPrefHelper.getHomeLatitude() + " : " + mPrefHelper.getHomeLongitude() );
        }



    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SetupPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_setup);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("ssid"));
            bindPreferenceSummaryToValue(findPreference("password_wifi"));

        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
/*
            Intent i = new Intent(getActivity(),AboutActivity.class);
            startActivity(i);*/
        }
    }


}
