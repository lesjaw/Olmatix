package com.olmatix.ui.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.ui.fragment.SettingsFragment;
import com.olmatix.utils.Connection;
import com.olmatix.utils.OlmatixUtils;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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
            boolean mswitch_loc = sharedPref.getBoolean("switch_loc", true);
            boolean mswitch_notif = sharedPref.getBoolean("switch_notif", true);

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
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        if (header.id == R.id.about) {
            Intent i = new Intent(this,AboutActivity.class);
            startActivity(i);        }
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
        public static dbNodeRepo mDbNodeRepo;
        private static ArrayList<InstalledNodeModel> data;
        ArrayList<DetailNodeModel> data1;

        String topic;
        private InstalledNodeModel installedNodeModel;
        private DetailNodeModel detailNodeModel;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_network);
            setHasOptionsMenu(true);

            data = new ArrayList<>();
            data1 = new ArrayList<>();

            mDbNodeRepo = new dbNodeRepo(getActivity());
            installedNodeModel = new InstalledNodeModel();
            detailNodeModel = new DetailNodeModel();


            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            // bindPreferenceSummaryToValue(findPreference("server_conn"));
            bindPreferenceSummaryToValue(findPreference("server_address"));
            bindPreferenceSummaryToValue(findPreference("server_port"));
            bindPreferenceSummaryToValue(findPreference("user_name"));
            bindPreferenceSummaryToValue(findPreference("password"));

            CheckBoxPreference mSLoc = (CheckBoxPreference) findPreference("switch_conn");
            final Preference setConnection = findPreference("switch_conn");
            setConnection.setOnPreferenceClickListener(NetworkClickListener());
        }

        private Preference.OnPreferenceClickListener NetworkClickListener() {

            return new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    doSubAll();

                    return true;
                }

            };
        }

        private void doSubAll() {

            int countDB = mDbNodeRepo.getNodeList().size();
            Log.d("DEBUG", "Count list Node: " + countDB);
            data.addAll(mDbNodeRepo.getNodeList());
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    final String mNodeID = data.get(i).getNodesID();
                    //Log.d("DEBUG", "Count list: " + mNodeID);
                    for (int a = 0; a < 5; a++) {
                        if (a == 0) {
                            topic = "devices/" + mNodeID + "/$online";
                        }
                        if (a == 1) {
                            topic = "devices/" + mNodeID + "/$fwname";
                        }
                        if (a == 2) {
                            topic = "devices/" + mNodeID + "/$signal";
                        }
                        if (a == 3) {
                            topic = "devices/" + mNodeID + "/$uptime";
                        }
                        if (a == 4) {
                            topic = "devices/" + mNodeID + "/$localip";
                        }
                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    //Log.d("SubscribeNode", " device = " + mNodeID);
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
                data.clear();
                doSubAllDetail();
            }

        }

        private void doSubAllDetail() {

            int countDB = mDbNodeRepo.getNodeDetailList().size();
            Log.d("DEBUG", "Count list Detail: " + countDB);
            data1.addAll(mDbNodeRepo.getNodeDetailList());
            countDB = mDbNodeRepo.getNodeDetailList().size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    final String mNodeID = data1.get(i).getNode_id();
                    final String mChannel = data1.get(i).getChannel();
                    topic = "devices/" + mNodeID + "/light/" + mChannel;
                    int qos = 2;
                    try {
                        IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                //Log.d("SubscribeButton", " device = " + mNodeID);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }
                doAllsubDetailSensor();
            }
            data.clear();
        }

        private void doAllsubDetailSensor() {
            int countDB = mDbNodeRepo.getNodeDetailList().size();
            Log.d("DEBUG", "Count list Sensor: " + countDB);
            data1.addAll(mDbNodeRepo.getNodeDetailList());
            countDB = mDbNodeRepo.getNodeDetailList().size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    final String mNodeID1 = data1.get(i).getNode_id();
                    final String mSensorT = data1.get(i).getSensor();
                    //Log.d("DEBUG", "Count list Sensor: " + mSensorT);
                    if (mSensorT != null && mSensorT.equals("close")) {
                        for (int a = 0; a < 2; a++) {
                            if (a == 0) {
                                topic = "devices/" + mNodeID1 + "/door/close";
                            }
                            if (a == 1) {
                                topic = "devices/" + mNodeID1 + "/door/theft";
                            }

                            int qos = 2;
                            try {
                                IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                                subToken.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                        //Log.d("SubscribeSensor", " device = " + mNodeID);
                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    }
                                });
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }

                data1.clear();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MiscPreferenceFragment extends PreferenceFragment {
        private LocationManager mLocationMgr;
        private String mProvider;
        private Preference setLocation;
        String loc = null;
        private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
        private CheckBoxPreference pref,pref1;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_misc);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("setLocation"));
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Boolean mswitch_loc = sharedPref.getBoolean("switch_loc", true);
            final Boolean mswitch_notif = sharedPref.getBoolean("switch_notif", true);

            pref = (CheckBoxPreference) findPreference("switch_loc");
            pref1 = (CheckBoxPreference) findPreference("switch_notif");


            if (!mswitch_loc) {
                pref.setTitle(R.string.switch_loc);
            } else if (mswitch_loc) {
                pref.setTitle(R.string.switch_locDisable);
            }

            if (!mswitch_notif) {
                pref1.setTitle(R.string.switch_notif);
            } else if (mswitch_notif){
                pref1.setTitle(R.string.switch_notifDisable);
            }

            initLocationPref();

            //-- preference change listener
            prefListener = new SharedPreferences.OnSharedPreferenceChangeListener(){
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key){
                    if (key.equals("switch_loc")){
                        Log.d("DEBUG", "onSharedPreferenceChanged: "+mswitch_loc);

                        if (mswitch_loc) {
                            pref.setTitle(R.string.switch_loc);
                        } else if (!mswitch_loc) {
                            pref.setTitle(R.string.switch_locDisable);
                        }
                    }

                    if (key.equals("switch_notif")){
                        Log.d("DEBUG", "onSharedPreferenceChanged: "+mswitch_notif);

                        if (mswitch_notif) {
                            pref1.setTitle(R.string.switch_notif);
                        } else if (!mswitch_notif){
                            pref1.setTitle(R.string.switch_notifDisable);
                        }
                    }
                }
            };
            sharedPref.registerOnSharedPreferenceChangeListener(prefListener);
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
                        Location mLocation = mLocationMgr.getLastKnownLocation(mProvider);
                        if (mLocation == null){
                            mLocation= mLocationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }  else if (mLocation == null) {
                            mLocation= mLocationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        } else {
                            mLocation= mLocationMgr.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        }

                        Log.d("DEBUG", "location mpref: " + mLocation.getLatitude() + " "+mLocation.getLongitude() );
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

    @SuppressLint("ValidFragment")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SetupPreferenceFragment extends PreferenceFragment {
        private Button mSend, mConnect;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.sendbuttons, null);
            addPreferencesFromResource(R.xml.pref_setup);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference("ssid"));
            bindPreferenceSummaryToValue(findPreference("password_wifi"));

            mSend = (Button) v.findViewById(R.id.sendButton);
            mConnect = (Button) v.findViewById(R.id.connectButton);

            setOnCLickistener();

            sendConfig();

            return v;
        }

        private void setOnCLickistener() {
            mSend.setOnClickListener(sendClickLietener());
            mConnect.setOnClickListener(connectClickListener());
        }

        private View.OnClickListener connectClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            };
        }

        private View.OnClickListener sendClickLietener() {
            return new View.OnClickListener() {


                @Override
                public void onClick(View v) {

                    //  PreferenceManager.setDefaultValues(getActivity(), R.xml.pref_network, false);/
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    String mServerURL = sharedPref.getString("server_address", "");
                    String mServerPort = sharedPref.getString("server_port", "");
                    String mUserName = sharedPref.getString("user_name", "");
                    String mPassword = sharedPref.getString("password", "");
                    String mSsid = sharedPref.getString("ssid", "");
                    String mWifiPass = sharedPref.getString("password_wifi", "");

                    Log.d("DEBUG", "onClick: \nData Host " + mServerURL +"\nData Port " + mServerPort +"\nData HOST User " + mUserName +"\nData Hsst Pass " + mPassword +"\nData SSID " + mSsid  +"\nData Wifi pass " + mWifiPass);



                    try {
                        JsonWriter jsonWriter = new JsonWriter(
                                new OutputStreamWriter(getActivity().openFileOutput("config.json" , getActivity().MODE_PRIVATE)));
                        Log.d("DEBUG", "onClick: " + String.valueOf(jsonWriter.toString()));

                        try {
                            jsonWriter.setIndent(" ");
                            jsonWriter.beginObject();
                            jsonWriter.name("wifi").beginObject();
                            jsonWriter.name("ssid");
                            jsonWriter.value(mSsid);
                            jsonWriter.name("password");
                            jsonWriter.value(mWifiPass);
                            jsonWriter.endObject();
                            jsonWriter.name("mqtt").beginObject();
                            jsonWriter.name("host");
                            jsonWriter.value(mServerURL);
                            jsonWriter.name("port");
                            jsonWriter.value(mServerPort);
                            jsonWriter.name("base_topic");
                            jsonWriter.value("devices/");
                            jsonWriter.name("auth");
                            jsonWriter.value(true);
                            jsonWriter.name("username");
                            jsonWriter.value(mUserName);
                            jsonWriter.name("password");
                            jsonWriter.value(mPassword);
                            jsonWriter.endObject();
                            jsonWriter.name("ota").beginObject();
                            jsonWriter.name("enabled");
                            jsonWriter.value(true);
                            jsonWriter.endObject();
                            jsonWriter.endObject();
                            jsonWriter.close();
                        } finally {

                        }

                    } catch (IOException ex){
                        ex.printStackTrace();
                    }
                }
            };
        }

        public void sendConfig() {
            File rootDataDir = getActivity().getFilesDir();
            String configFiles =  rootDataDir.getAbsolutePath() + "/config.json";


            Log.d("DEBUG", "sendConfig: " +configFiles );



        }


    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

    }


}
