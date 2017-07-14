package com.olmatix.service;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.olmatix.database.dbNode;
import com.olmatix.database.dbNodeRepo;
import com.olmatix.helper.PreferenceHelper;
import com.olmatix.helper.SnackbarWrapper;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.DurationModel;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.ui.activity.MainActivity;
import com.olmatix.ui.activity.SplashActivity;
import com.olmatix.utils.Connection;
import com.olmatix.utils.OlmatixUtils;

import org.appspot.olmatixrtc.ConnectActivity;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.olmatix.lesjaw.olmatix.R.drawable;
import static com.olmatix.lesjaw.olmatix.R.string;


/**
 * Created              : Rahman on 12/2/2016.
 * Date Created         : 12/2/2016 / 4:29 PM.
 * ===================================================
 * Package              : com.olmatix.service.
 * Project Name         : Olmatix.
 * Copyright            : Copyright @ 2016 Indogamers.
 */
public class OlmatixService extends Service {

    public final static String MY_ACTION = "MY_ACTION";
    public static dbNodeRepo mDbNodeRepo;
    private static String TAG = OlmatixService.class.getSimpleName();
    private static boolean hasWifi = false;
    private static boolean hasMmobile = false;
    private static ArrayList<InstalledNodeModel> data;
    public volatile MqttAndroidClient mqttClient;
    HashMap<String, String> messageReceive = new HashMap<>();
    HashMap<String, String> message_topic = new HashMap<>();
    CharSequence text, textNode, titleNode;
    ArrayList<DetailNodeModel> data1;
    ArrayList<InstalledNodeModel> data2;
    ArrayList<DurationModel> data3;

    String add_NodeID;
    boolean flagSub = true;
    boolean flagNode = false;
    boolean flagConn = false;
    boolean flagStart = false;
    boolean flagOnForeground = true;
    int notifyID = 0;
    String topic, topic1;
    private ConnectivityManager mConnMan;
    private String deviceId;
    private InstalledNodeModel installedNodeModel;
    private DetailNodeModel detailNodeModel;
    private DurationModel durationModel;
    private String NodeID, Channel, mMessage;
    private String mNodeID;
    private String mNiceName;
    private String NodeIDSensor;
    private String TopicID;
    private String mChange = "";
    private String connectionResult;
    private ArrayList<String> notifications;
    private static final int TWO_MINUTES = 1000 * 60 * 5;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    private String Distance;
    String adString = "";
    String loc = null;
    IntentFilter filter;
    int numMessages = 0;
    int count = 0;
    boolean hasConnectivity = false;
    boolean hasChanged = false;
    SharedPreferences sharedPref;
    Boolean mStatusServer, doCon, noNotif = true;
    dbNode dbnode;
    int alarm;
    String lastValue;
    Uri ringtoneUri;
    Ringtone ringtoneSound;

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }


    };

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            add_NodeID = intent.getStringExtra("NodeID");
            String alarmService = intent.getStringExtra("Conn");

            NetworkInfo nInfo = mConnMan.getActiveNetworkInfo();
            if (nInfo != null) {
                if (nInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    hasWifi = nInfo.isConnected();
                } else if (nInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    hasMmobile = nInfo.isConnected();
                }
            }

            hasConnectivity = hasWifi || hasMmobile;

            if (alarmService == null) {
                if (!hasConnectivity) {
                    final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                            "Internet connection not avalaible", TSnackbar.LENGTH_LONG);
                    snackbarWrapper.setAction("Olmatix",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getApplicationContext(), "Action",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                    snackbarWrapper.show();
                } else {
                    if (add_NodeID == null) {
                        if (!doCon) {
                            doConnect();
                        }
                    }
                }
            }

            if (alarmService != null) {
                if (alarmService.equals("login")) {
                    doCon = false;
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean("conStatus", false);
                    editor.apply();
                    doConnect();
                }
                if (alarmService.equals("con")) {
                    if (!doCon) {
                        doConnect();
                    }
                }
                if (alarmService.equals("stop")) {
                    stopSelf();
                }
            }
            if (add_NodeID != null) {
                doAddNodeSub();
            }
        }
    };

    class OlmatixBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPref.edit();

            NetworkInfo nInfo = mConnMan.getActiveNetworkInfo();
            if (nInfo != null) {
                if (nInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    hasChanged = true;
                    hasWifi = nInfo.isConnected();
                    if (hasWifi) {
                        editor.putString("IPaddress", ip);
                        editor.apply();
                    }

                } else if (nInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    hasChanged = true;
                    hasMmobile = nInfo.isConnected();
                    if (hasMmobile) {
                        ip = getLocalIpAddress();
                        editor.putString("IPaddress", ip);
                        editor.apply();
                    }
                }
            } else {
                //Not Connected info
                final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                        "No Internet connection", TSnackbar.LENGTH_LONG);
                snackbarWrapper.setAction("Olmatix",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getApplicationContext(), "Action",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                snackbarWrapper.show();
                text = "Disconnected";
                flagConn = false;
                doCon = false;
                hasMmobile = false;
                hasWifi = false;
                editor.putBoolean("conStatus", false);
                editor.apply();
                sendMessage();
                showNotification(text);
            }

            hasConnectivity = hasMmobile || hasWifi;

            if (hasConnectivity && hasChanged) {
                if (mqttClient != null) {
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    mStatusServer = sharedPref.getBoolean("conStatus", false);
                    if (!mStatusServer) {
                        if (!doCon) {
                            doConnect();
                        }
                    }
                }
            }
        }
    }

    public String getLocalIpAddress(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        return ip;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    /*private void stopService (){
        stopSelf();
    }*/

    private void doConnect() {

        final SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mServerURL = sharedPref.getString("server_address", "cloud.olmatix.com");
        String mServerPort = sharedPref.getString("server_port", "1883");
        String mUserName = sharedPref.getString("user_name", "olmatix1");
        String mPassword = sharedPref.getString("password", "olmatix");
        mStatusServer = sharedPref.getBoolean("conStatus", false);

        Log.d(TAG, mServerURL +" login: " + mUserName + " : " + mPassword);

        final Boolean mSwitch_conn = sharedPref.getBoolean("switch_conn", true);
        Log.d(TAG, "doConnect status connection: " + mStatusServer);
        doCon = true;
        if (!mStatusServer) {

            final MqttConnectOptions options = new MqttConnectOptions();

            options.setUserName(mUserName);
            options.setPassword(mPassword.toCharArray());
            mqttClient = new MqttAndroidClient(getApplicationContext(), "tcp://" + mServerURL + ":" + mServerPort, deviceId);

            if (mSwitch_conn) {
                options.setCleanSession(false);
            } else {
                options.setCleanSession(true);
            }

            Log.d(TAG, "doConnect: " + count);
            /*dbnode.setTopic("Connecting to server");
            dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
            mDbNodeRepo.insertDbMqtt(dbnode);*/
            sendMessageDetail();

            String topic = "devices/" + deviceId + "/$online";
            byte[] payload = "false".getBytes();
            options.setWill(topic, payload, 1, true);
            options.setKeepAliveInterval(120);
            Connection.setClient(mqttClient);

            text = "Connecting to server..";
            showNotification(text);
            Log.d(TAG, "doConnect: " + deviceId);
            try {
                Log.d(TAG, "trying connect ");
                IMqttToken token = mqttClient.connect(options);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        mqttClient.setCallback(new MqttEventCallback());
                        text = "Connected";
                        showNotification(text);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("conStatus", true);
                        editor.apply();
                        connectionResult = "AuthOK";
                        Log.d(TAG, "onSuccess: "+connectionResult);
                        flagConn = true;
                        sendMessage();
                        sendMessageSplash();
                        if (!mSwitch_conn) {
                            Log.d(TAG, "Doing subscribe nodes");
                            doSubAll();
                        }

                        /*dbnode.setTopic("Connected to server");
                        dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                        mDbNodeRepo.insertDbMqtt(dbnode);*/
                        sendMessageDetail();
                        try {
                            for (int a = 0; a < 6; a++) {
                                String topic = "";
                                if (a == 0) {
                                    topic = "devices/" + deviceId + "/$online";
                                    String payload = "true";
                                    byte[] encodedPayload = new byte[0];
                                    try {
                                        if (mqttClient != null) {
                                            encodedPayload = payload.getBytes("UTF-8");
                                            MqttMessage message = new MqttMessage(encodedPayload);
                                            message.setQos(1);
                                            message.setRetained(true);
                                            Connection.getClient().publish(topic, message);
                                        }
                                    } catch (UnsupportedEncodingException | MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (a == 1) {
                                    topic = "devices/" + deviceId + "/$fwname";
                                    String payload = "olmatixapp";
                                    byte[] encodedPayload = new byte[0];
                                    try {
                                        if (mqttClient != null) {
                                            encodedPayload = payload.getBytes("UTF-8");
                                            MqttMessage message = new MqttMessage(encodedPayload);
                                            message.setQos(1);
                                            message.setRetained(true);
                                            Connection.getClient().publish(topic, message);
                                        }
                                    } catch (UnsupportedEncodingException | MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (a == 2) {
                                    topic = "devices/" + deviceId + "/$localip";
                                    String ip = sharedPref.getString("IPaddress", "127.0.0.1");
                                    String payload = ip;
                                    byte[] encodedPayload = new byte[0];
                                    try {
                                        if (mqttClient != null) {
                                            encodedPayload = payload.getBytes("UTF-8");
                                            MqttMessage message = new MqttMessage(encodedPayload);
                                            message.setQos(1);
                                            message.setRetained(true);
                                            Connection.getClient().publish(topic, message);
                                        }
                                    } catch (UnsupportedEncodingException | MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (a == 3) {
                                    topic = "devices/" + deviceId + "/$signal";
                                    WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                                    int strengthInPercentage = 0;
                                    if(wm.isWifiEnabled()) {
                                        WifiInfo wifiInfo = wm.getConnectionInfo();
                                        if(wifiInfo != null) {
                                            int dbm = wifiInfo.getRssi();
                                            strengthInPercentage = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 100);
                                        }
                                    }

                                    String payload = String.valueOf(strengthInPercentage);
                                    byte[] encodedPayload = new byte[0];
                                    try {
                                        if (mqttClient != null) {
                                            encodedPayload = payload.getBytes("UTF-8");
                                            MqttMessage message = new MqttMessage(encodedPayload);
                                            message.setQos(1);
                                            message.setRetained(true);
                                            Connection.getClient().publish(topic, message);
                                        }
                                    } catch (UnsupportedEncodingException | MqttException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (a == 4) {
                                    topic = "devices/" + deviceId + "/$uptime";
                                    String payload = "0";
                                    byte[] encodedPayload = new byte[0];
                                    try {
                                        if (mqttClient != null) {
                                            encodedPayload = payload.getBytes("UTF-8");
                                            MqttMessage message = new MqttMessage(encodedPayload);
                                            message.setQos(1);
                                            message.setRetained(true);
                                            Connection.getClient().publish(topic, message);
                                        }
                                    } catch (UnsupportedEncodingException | MqttException e) {
                                        e.printStackTrace();
                                    }
                                }

                                if (a == 5) {
                                    topic = "devices/" + deviceId + "/$calling";
                                    String payload = "false";
                                    byte[] encodedPayload = new byte[0];
                                    try {
                                        if (mqttClient != null) {
                                            encodedPayload = payload.getBytes("UTF-8");
                                            MqttMessage message = new MqttMessage(encodedPayload);
                                            message.setQos(1);
                                            message.setRetained(true);
                                            Connection.getClient().publish(topic, message);
                                        }
                                    } catch (UnsupportedEncodingException | MqttException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }


                            Connection.getClient().subscribe("devices/" + deviceId + "/$calling", 2, getApplicationContext(), new IMqttActionListener() {


                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    text = "Connected";
                                    showNotification(text);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean("conStatus", true);
                                    editor.apply();
                                    flagConn = true;
                                    sendMessage();

                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    //Toast.makeText(getApplicationContext(), R.string.sub_fail, Toast.LENGTH_SHORT).show();
                                    Log.e("error", exception.toString());
                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                            text = "Failed to subscribe";
                            showNotification(text);
                            editor.putBoolean("conStatus", false);
                            editor.apply();
                            flagConn = false;
                            sendMessage();
                            /*dbnode.setTopic("Failed to subscribe");
                            dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                            mDbNodeRepo.insertDbMqtt(dbnode);*/
                            sendMessageDetail();

                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        text = "Not Connected";
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("conStatus", false);
                        editor.apply();
                        String me = exception.toString();
                        if (me.equals("Not authorized to connect (5)")) {
                            connectionResult = "NotAuth";
                            text = "Not Connected - Bad login";
                        }
                        flagConn = false;
                        sendMessage();
                        sendMessageSplash();
                        showNotification(text);
                        dbnode.setTopic((String) text);
                        dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                        dbnode.setChannel("0");
                        dbnode.setNode_id("0");
                        mDbNodeRepo.insertDbMqtt(dbnode);
                        sendMessageDetail();
                    }
                });

            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        sendMessage();
        sendMessageSplash();
        //showNotification();
        noNotif = true;
        setFlagSub();

    }

    public String uptime (Calendar ref){
        Calendar now = Calendar.getInstance();
        long milliseconds1 = ref.getTimeInMillis();
        long milliseconds2 = now.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;

        long diffSeconds = diff / 1000;
        //Log.d("DEBUG", "getTimeAgo: " + diffSeconds);
        return String.valueOf(diffSeconds);
    }

    private void doDisconnect() {
        Log.d(TAG, "doDisconnect, flagConn = " + flagConn);
        if (flagConn) {
            try {
                mqttClient.disconnect();
                flagConn = false;
                Log.d(TAG, "doDisconnect done");

            } catch (MqttException e) {
                e.printStackTrace();
                Log.d(TAG, "onReceive: " + String.valueOf(e.getMessage()));
            }
        }
    }

    private void setFlagSub() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                flagSub = true;
                checkActivityForeground();
                noNotif = false;
                unSubIfnotForeground();
            }
        }, 20000);
    }

    private void connLose() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent("addNode");
                intent.putExtra("Connect", "con");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }, 5000);
    }

    @Override
    public void onCreate() {

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.getTimeInMillis();

        Handler h = new Handler();
        int delay = 30000; //milliseconds

        h.postDelayed(new Runnable(){
            public void run(){
                String upme = uptime(now);
                Log.d(TAG, "run: "+upme);
                topic = "devices/" + deviceId + "/$uptime";
                String payload = upme;
                byte[] encodedPayload = new byte[0];
                try {
                    if (mqttClient != null) {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        message.setQos(1);
                        message.setRetained(true);
                        Connection.getClient().publish(topic, message);
                    }
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }

                h.postDelayed(this, delay);
            }
        }, delay);

        IntentFilter intent = new IntentFilter();
        setClientID();
        intent.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new OlmatixBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        data = new ArrayList<>();
        data1 = new ArrayList<>();
        data2 = new ArrayList<>();
        data3 = new ArrayList<>();

        notifications = new ArrayList<>();
        dbnode = new dbNode();

        Connection.setClient(mqttClient);

        mDbNodeRepo = new dbNodeRepo(getApplicationContext());
        installedNodeModel = new InstalledNodeModel();
        detailNodeModel = new DetailNodeModel();
        durationModel = new DurationModel();
        // Display a notification about us starting.  We put an icon in the status bar.
        text = "Starting";
        showNotification(text);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("addNode"));

        notifications.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA)
                .check();

        ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtoneSound = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);

        OlmatixAlarmReceiver alarmCheckConn = new OlmatixAlarmReceiver();
        alarmCheckConn.setAlarm(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String mProvider = locationManager.getBestProvider(OlmatixUtils.getGeoCriteria(), true);

        checkActivityForeground();

        noNotif = true;
        if (!flagStart) {
            flagStart = true;
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mStatusServer = sharedPref.getBoolean("conStatus", false);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("conStatus", false);
            editor.apply();
            Log.d(TAG, "onStartCommand status connection: " + mStatusServer);
            doConnect();

        }

        sendMessage();

        listener = new MyLocationListener();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, OlmatixUtils.POSITION_UPDATE_INTERVAL,
                            OlmatixUtils.POSITION_UPDATE_MIN_DIST, listener);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, OlmatixUtils.POSITION_UPDATE_INTERVAL,
                            OlmatixUtils.POSITION_UPDATE_MIN_DIST, listener);
                }
            }
        }

            Location mLocation = locationManager.getLastKnownLocation(mProvider);
            if (mLocation == null) {
                mLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else if (mLocation == null) {
                mLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else {
                mLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
            if (mLocation != null) {
                Double lat = mLocation.getLatitude();
                Double lng = mLocation.getLongitude();
                locationDistance(lat, lng);
                PreferenceHelper mPrefHelper = new PreferenceHelper(this.getApplicationContext());
                double homeLat = mPrefHelper.getHomeLatitude();
                double homelng = mPrefHelper.getHomeLongitude();
                long thres = mPrefHelper.getHomeThresholdDistance();
                //Log.d("DEBUG", "proximity: " + homeLat + " | " + homelng + ":" + thres);
                String proximityIntentAction = "com.olmatix.lesjaw.olmatix.ProximityAlert";
                Intent i = new Intent(proximityIntentAction);
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
                locationManager.addProximityAlert(homeLat, homelng, thres, -1, pi);
                filter = new IntentFilter(proximityIntentAction);
                registerReceiver(new OlmatixReceiver(), filter);
            } else {

            }


            return START_STICKY;

    }

    private void unSubIfnotForeground() {

        if (!flagOnForeground&&!noNotif) {
            int countDB = mDbNodeRepo.getNodeList().size();
            data.clear();
            data.addAll(mDbNodeRepo.getNodeList());
            Log.d(TAG, "doing unsub Signal & Uptime: ");
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    final String mNodeID1 = data.get(i).getNodesID();
                    //Log.d("DEBUG", "Count list: " + mNodeID1);
                    for (int a = 0; a < 2; a++) {
                        if (a == 0) {
                            topic = "devices/" + mNodeID1 + "/$signal";
                        }
                        if (a == 1) {
                            topic = "devices/" + mNodeID1 + "/$uptime";
                        }

                        try {
                            if (mqttClient!=null) {
                                Connection.getClient().unsubscribe(topic);
                            }
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }

                    }
                    //Log.d("Unsubscribe", " device = " + mNodeID1);
                }
            }
        }
    }

    private void showNotification(CharSequence txt) {

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.olmatixsmall)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(txt)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setOngoing(true)
                .setPriority(Notification.FLAG_FOREGROUND_SERVICE)
                //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .build();

        // Send the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int NOTIFICATION = string.local_service_label;
        notificationManager.notify(NOTIFICATION, mBuilder.build());
    }

    private void showNotificationNode() {
        final Boolean mSwitch_NotifStatus = sharedPref.getBoolean("switch_notif", true);
        if (mSwitch_NotifStatus && !noNotif) {
            //Log.d(TAG, "showNotificationNode: ");
            numMessages++;

            SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm");

            notifications.add(numMessages + ". " + String.valueOf(titleNode) + " : " + String.valueOf(textNode) + " at " + timeformat.format(System.currentTimeMillis()));
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle("New "+ getText(string.app_name) +" status");
            mBuilder.setContentText("You've received new status..");
            mBuilder.setTicker(getText(string.app_name)+" status alert!");
            mBuilder.setAutoCancel(true);
            mBuilder.setWhen(System.currentTimeMillis());
            mBuilder.setNumber(numMessages);
            //mBuilder.setGroup(GROUP_KEY_NOTIF);
            //mBuilder.setGroupSummary(true);
            mBuilder.setSound(defaultSoundUri);
            mBuilder.setSmallIcon(R.drawable.ic_lightbulb);
            mBuilder.setPriority(Notification.PRIORITY_MAX);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(getText(string.app_name)+" status");
            Collections.sort(notifications, Collections.reverseOrder());
            for (int i = 0; i < notifications.size(); i++) {
                inboxStyle.addLine(notifications.get(i));
            }

            mBuilder.setStyle(inboxStyle);

            Intent resultIntent = new Intent(this, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(15, mBuilder.build());
        }
    }

    private void setClientID() {

        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ; //13 digits

        Log.d(TAG, "setClientID: "+m_szDevIDShort);

        String uniqueID = null;
        int lengUniqID = 0;
        final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";


                SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(
                        PREF_UNIQUE_ID, Context.MODE_PRIVATE);
                uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
                if (uniqueID!=null) {
                    lengUniqID = uniqueID.length();
                }
                Log.d(TAG, "check App ID length " +lengUniqID);
                if (uniqueID == null || lengUniqID>15) {
                    uniqueID = m_szDevIDShort;
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(PREF_UNIQUE_ID, uniqueID);
                    editor.commit();
                }


        deviceId = "OlmatixApp-" +uniqueID;

        if (deviceId == null) {
            deviceId = MqttAsyncClient.generateClientId();
        }
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("appID", deviceId);
        editor.apply();
    }

    private void sTopService() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, 20000);
    }

    private void sendMessage() {
        Intent intent = new Intent("MQTTStatus");
        intent.putExtra("MqttStatus", flagConn);
        //intent.putExtra("ConnectionStatus", connectionResult);
        //Log.d(TAG, "sendMessage: "+connectionResult);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageLoc(String latlng) {
        Intent intent = new Intent("Location");
        intent.putExtra("latlng", latlng);
        //intent.putExtra("ConnectionStatus", connectionResult);
        //Log.d(TAG, "sendMessage: "+connectionResult);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageSplash() {
        Intent intent = new Intent("splashauth");
        intent.putExtra("ConnectionStatus", connectionResult);
        Log.d(TAG, "sendMessage: "+connectionResult);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageDetail() {
        Intent intent = new Intent("MQTTStatusDetail");
        intent.putExtra("NotifyChangeNode", mChange);
        intent.putExtra("NotifyChangeDetail", mChange);
        intent.putExtra("distance", Distance);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        // Tell the user we stopped.
        doDisconnect();
        Log.d(TAG, "Service stop!! ");
        messageReceive.clear();
        message_topic.clear();
        data.clear();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind called");

        return null;
    }

    private void addNode() {
        String[] outputDevices = TopicID.split("/");
        NodeID = outputDevices[1];
        String mNodeIdSplit = mNodeID;
        mNodeIdSplit = mNodeIdSplit.substring(mNodeIdSplit.indexOf("$") + 1, mNodeIdSplit.length());
        messageReceive.put(mNodeIdSplit, mMessage);
        String online = outputDevices[2];

        data2.clear();

        if (online.equals("$online")){
            //checkValidation(NodeID);
            //installedNodeModel.setNodesID(NodeID);
            data2.addAll(mDbNodeRepo.getNodeListbyNode(NodeID));
            int countDB = mDbNodeRepo.getNodeListbyNode(NodeID).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    String mNiceNameN;
                    if (data2.get(i).getNice_name_n() != null) {
                        mNiceNameN = data2.get(i).getNice_name_n();
                    } else {
                        mNiceNameN = data2.get(i).getFwName();
                    }

                    lastValue = data2.get(i).getOnline();

                    if (TextUtils.isEmpty(lastValue)){
                        lastValue = "false";
                    }

                    if (mMessage.equals("true")) {
                        if (lastValue.equals("false")) {
                            titleNode = mNiceNameN;
                            textNode = "ONLINE";
                            installedNodeModel.setOnline("true");
                            installedNodeModel.setNodesID(NodeID);
                            mDbNodeRepo.updateOnline(installedNodeModel);
                            SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                            dbnode.setTopic(mNiceNameN + " is " + textNode);
                            dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                            dbnode.setNode_id(NodeID);
                            dbnode.setChannel("0");
                            mDbNodeRepo.insertDbMqtt(dbnode);

                            mChange = "2";
                            sendMessageDetail();
                            if (!flagOnForeground) {
                                if (!noNotif) {
                                    showNotificationNode();
                                }
                            }
                        }

                    } else  if (mMessage.equals("false")) {
                        if (lastValue.equals("true")) {
                            titleNode = mNiceNameN;
                            textNode = "OFFLINE";
                            installedNodeModel.setOnline("false");
                            installedNodeModel.setNodesID(NodeID);
                            mDbNodeRepo.updateOnline(installedNodeModel);
                            SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                            dbnode.setTopic(mNiceNameN + " is " + textNode);
                            dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                            dbnode.setNode_id(NodeID);
                            dbnode.setChannel("0");
                            mDbNodeRepo.insertDbMqtt(dbnode);

                            mChange = "2";
                            sendMessageDetail();
                            if (!flagOnForeground) {
                                if (!noNotif) {
                                    showNotificationNode();
                                }
                            }
                        }

                    }
                    updated(NodeID);
                }
            } else if (countDB==0){
                //checkValidation(NodeID);
                saveFirst();
            }

            lastValue="";
            data2.clear();
        } else if (online.equals("$localip")){
            installedNodeModel.setLocalip(mMessage);
            installedNodeModel.setNodesID(NodeID);
            mDbNodeRepo.updateIP(installedNodeModel);
            mChange = "2";
            sendMessageDetail();
        } else if (online.equals("$fwname")) {
            data2.clear();
            data2.addAll(mDbNodeRepo.getNodeListbyNode(NodeID));
            int countDB = mDbNodeRepo.getNodeListbyNode(NodeID).size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    String fwnamecheck = data2.get(i).getFwName();

                    if (fwnamecheck == null) {
                    Log.d(TAG, "FWName: "+mMessage);
                        installedNodeModel.setFwName(mMessage);
                        installedNodeModel.setNodesID(NodeID);
                        mDbNodeRepo.updateFwname(installedNodeModel);
                        mChange = "2";
                        sendMessageDetail();
                        addNodeDetail();
                    }
                }
            }
        } else if (online.equals("$signal")){
            installedNodeModel.setSignal(mMessage);
            installedNodeModel.setNodesID(NodeID);
            mDbNodeRepo.updateSignal(installedNodeModel);
            mChange = "2";
            sendMessageDetail();
            updated(NodeID);
        } else if (online.equals("$uptime")){
            //Log.d(TAG, "Uptime: "+NodeID+" "+mMessage);
            installedNodeModel.setUptime(mMessage);
            installedNodeModel.setNodesID(NodeID);
            mDbNodeRepo.updateUptime(installedNodeModel);
            mChange = "2";
            sendMessageDetail();
            updated(NodeID);
        } else if (online.equals("$calling")){
            String input =mMessage;
            String[] rdm = input.split("-");
            if (rdm[0].equals("true")){
                Log.d(TAG, "Calling triggered ");
                Intent i = new Intent(getBaseContext(), ConnectActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("node_id", NodeID+rdm[1]);
                getApplication().startActivity(i);
                if (mStatusServer) {
                    String topic = "devices/" + deviceId + "/$calling";
                    String payload = "false";
                    byte[] encodedPayload = new byte[0];
                    try {
                        encodedPayload = payload.getBytes("UTF-8");
                        MqttMessage message = new MqttMessage(encodedPayload);
                        message.setQos(1);
                        message.setRetained(true);
                        Connection.getClient().publish(topic, message);

                    } catch (UnsupportedEncodingException | MqttException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"No response from server, trying to connect now..",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent("addNode");
                    intent.putExtra("Connect", "con");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }

        } else if (online.equals("$location")){

            installedNodeModel.setNodesID(NodeID);
            installedNodeModel.setOta(mMessage);
            mDbNodeRepo.updateLoc(installedNodeModel);
            mChange = "2";
            sendMessageDetail();
            updated(NodeID);
            sendMessageLoc(mMessage);
        }


    }

    private void updated(String nodeid){
        installedNodeModel.setNodesID(nodeid);
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.getTimeInMillis();
        installedNodeModel.setAdding(now.getTimeInMillis());
        mDbNodeRepo.updateAdding(installedNodeModel);
        mChange = "2";
        sendMessageDetail();
        //Log.d(TAG, "updated: ");
    }

    private void checkValidation(String nodeid) {
        if (flagNode) {
            if (messageReceive.containsKey("online")) {
                Log.d("CheckValid online", "Passed " +nodeid);
                if (mMessage.equals("true")) {
                    Log.d("CheckValid online", " true Passed "+nodeid);
                    saveFirst();
                } else {
                    final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                            "Your device Offline " +nodeid,TSnackbar.LENGTH_LONG);
                    snackbarWrapper.setAction("Olmatix",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(getApplicationContext(), "Action",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                    snackbarWrapper.show();
                }
            }
            flagNode = false;
        } else {
            saveDatabase();
        }
    }

    private void saveFirst() {

        if (mDbNodeRepo.getNodeList().isEmpty()) {
            installedNodeModel.setNodesID(NodeID);
            installedNodeModel.setNodes(messageReceive.get("online"));
            installedNodeModel.setOnline(mMessage);
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            now.getTimeInMillis();
            installedNodeModel.setAdding(now.getTimeInMillis());

            mDbNodeRepo.insertDb(installedNodeModel);
            final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                    "Add "+NodeID +" success",TSnackbar.LENGTH_LONG);
            snackbarWrapper.setAction("Olmatix",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "Action",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            snackbarWrapper.show();
            doSub();
        } else {
            installedNodeModel.setNodesID(NodeID);
            if (mDbNodeRepo.hasObject(installedNodeModel)) {

                final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                        "Checking this Node ID : " + NodeID + ", its exist, we are updating Node status",TSnackbar.LENGTH_LONG);
                snackbarWrapper.setAction("Olmatix",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getApplicationContext(), "Action",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                snackbarWrapper.show();
                saveDatabase();

            } else {
                installedNodeModel.setNodesID(NodeID);
                installedNodeModel.setNodes(messageReceive.get("online"));
                installedNodeModel.setOnline(mMessage);
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.getTimeInMillis();
                installedNodeModel.setAdding(now.getTimeInMillis());

                mDbNodeRepo.insertDb(installedNodeModel);
                final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                        "Add "+NodeID +" success",TSnackbar.LENGTH_LONG);
                snackbarWrapper.setAction("Olmatix",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getApplicationContext(), "Action",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                snackbarWrapper.show();
                doSub();

            }
        }
    }

    private void doSub() {
        for (int a = 0; a < 5; a++) {
            if (a == 0) {
                topic = "devices/" + NodeID + "/$fwname";
            }
            if (a == 1) {
                topic = "devices/" + NodeID + "/$signal";
            }
            if (a == 2) {
                topic = "devices/" + NodeID + "/$uptime";
            }
            if (a == 3) {
                topic = "devices/" + NodeID + "/$localip";
            }
            if (a == 4) {
                topic = "devices/" + NodeID + "/$online";
            }
            int qos = 2;
            try {
                IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d("SubscribeNode", " device = " + mNodeID);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        //messageReceive.clear();
        //doSubOnline();
    }

    protected void checkActivityForeground() {
        //Log.d(TAG, "start checking for Activity in foreground");
        Intent intent = new Intent();
        intent.setAction(MainActivity.UE_ACTION);
        sendOrderedBroadcast(intent, null, new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int result = getResultCode();

                if (result != Activity.RESULT_CANCELED) { // Activity caught it
                    //Log.d(TAG, "An activity caught the broadcast, result " + result);
                    activityInForeground();
                    return;
                }
                noActivityInForeground();
            }
        }, null, Activity.RESULT_CANCELED, null, null);
    }

    protected void activityInForeground() {
        // TODO something you want to happen when an Activity is in the foreground
        flagOnForeground = true;
        notifications.clear();
    }

    protected void noActivityInForeground() {
        // TODO something you want to happen when no Activity is in the foreground
        flagOnForeground = false;
    }

    private void updateSensorDoor() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final Boolean mSwitch_NotifStatus_door = sharedPref.getBoolean("switch_sensor_door", true);
        final Boolean mSwitch_NotifStatus_motion = sharedPref.getBoolean("switch_sensor_motion", true);
        final Boolean mSwitch_NotifStatus_prox = sharedPref.getBoolean("switch_sensor_prox", true);

        checkActivityForeground();
        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");

            data1.addAll(mDbNodeRepo.getNodeDetail(NodeIDSensor, "0"));
            int countDB = mDbNodeRepo.getNodeDetail(NodeIDSensor, "0").size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    if (data1.get(i).getNice_name_d() != null) {
                        mNiceName = data1.get(i).getNice_name_d();
                    } else {
                        mNiceName = data1.get(i).getName();
                    }
                    lastValue = data1.get(i).getStatus_sensor();
                    if (!lastValue.equals(mMessage) ) {
                        if (mSwitch_NotifStatus_door) {
                            if (mNodeID.contains("door/close")) {
                                if (mMessage.equals("true")) {
                                    titleNode = mNiceName;
                                    textNode = "Closed";
                                    showNotificationNode();

                                    detailNodeModel.setStatus_sensor(mMessage);
                                    mDbNodeRepo.update_detailSensor(detailNodeModel);

                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName + " is " + textNode);
                                    dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel("0");
                                    mDbNodeRepo.insertDbMqtt(dbnode);

                                    mChange = "2";
                                    sendMessageDetail();

                                } else if (mMessage.equals("false")) {
                                    titleNode = mNiceName;
                                    textNode = "Open";
                                    showNotificationNode();

                                    detailNodeModel.setStatus_sensor(mMessage);
                                    mDbNodeRepo.update_detailSensor(detailNodeModel);

                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName + " is " + textNode);
                                    dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel("0");
                                    mDbNodeRepo.insertDbMqtt(dbnode);

                                    mChange = "2";
                                    sendMessageDetail();
                                }

                            }
                        }


                        if (mSwitch_NotifStatus_motion) {
                            if (mNodeID.contains("motion/motion")) {
                                if (mMessage.equals("true")) {
                                    titleNode = mNiceName;
                                    textNode = "Motion";
                                    showNotificationNode();

                                    detailNodeModel.setStatus_sensor(mMessage);
                                    mDbNodeRepo.update_detailSensor(detailNodeModel);

                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName + " is " + textNode);
                                    dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel("0");
                                    mDbNodeRepo.insertDbMqtt(dbnode);

                                    mChange = "2";
                                    sendMessageDetail();
                                } else if (mMessage.equals("false")) {
                                    titleNode = mNiceName;
                                    textNode = "No Motion";
                                    showNotificationNode();

                                    detailNodeModel.setStatus_sensor(mMessage);
                                    mDbNodeRepo.update_detailSensor(detailNodeModel);

                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName + " is " + textNode);
                                    dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel("0");
                                    mDbNodeRepo.insertDbMqtt(dbnode);

                                    mChange = "2";
                                    sendMessageDetail();
                                }
                            }
                        }


                        if (mSwitch_NotifStatus_prox) {
                            if (mNodeID.contains("prox/status")) {
                                if (mMessage.equals("true")) {
                                    titleNode = mNiceName;
                                    textNode = "Detected";
                                    showNotificationNode();

                                    detailNodeModel.setStatus_sensor(mMessage);
                                    mDbNodeRepo.update_detailSensor(detailNodeModel);

                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName + " is " + textNode);
                                    dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel("0");
                                    mDbNodeRepo.insertDbMqtt(dbnode);

                                    mChange = "2";
                                    sendMessageDetail();
                                } else if (mMessage.equals("false")) {
                                    titleNode = mNiceName;
                                    textNode = "Empty";
                                    showNotificationNode();

                                    detailNodeModel.setStatus_sensor(mMessage);
                                    mDbNodeRepo.update_detailSensor(detailNodeModel);

                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName + " is " + textNode);
                                    dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel("0");
                                    mDbNodeRepo.insertDbMqtt(dbnode);

                                    mChange = "2";
                                    sendMessageDetail();
                                }
                            }
                        }
                    }
                }
            }


            data1.clear();
            lastValue="";
        }
    }

    private void UpdateSensorTemp() {

        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");
            detailNodeModel.setStatus_temp(mMessage);
            mDbNodeRepo.update_detailSensorTemp(detailNodeModel);

            mChange = "2";
            sendMessageDetail();
        }
    }

    private void UpdateSensorHum() {

        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");
            detailNodeModel.setStatus_hum(mMessage);
            mDbNodeRepo.update_detailSensorHum(detailNodeModel);

            mChange = "2";
            sendMessageDetail();
        }
    }

    private void UpdateSensorJarak() {

        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");
            detailNodeModel.setStatus_jarak(mMessage);

            mDbNodeRepo.update_detailSensorJarak(detailNodeModel);

            mChange = "2";
            sendMessageDetail();
        }
    }

    private void UpdateSensorRange() {
        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");
            detailNodeModel.setStatus_range(mMessage);
            mDbNodeRepo.update_detailSensorRange(detailNodeModel);

            mChange = "2";
            sendMessageDetail();
        }

    }

    private void updateSensorTheft() {
        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");

                data1.addAll(mDbNodeRepo.getNodeDetail(NodeIDSensor, "0"));
                int countDB = mDbNodeRepo.getNodeDetail(NodeIDSensor, "0").size();
                if (countDB != 0) {
                    for (int i = 0; i < countDB; i++) {
                        if (data1.get(i).getNice_name_d() != null) {
                            mNiceName = data1.get(i).getNice_name_d();
                        } else {
                            mNiceName = data1.get(i).getName();
                        }
                        lastValue = data1.get(i).getStatus_theft();

                        if (!lastValue.equals(mMessage)) {
                            if (mMessage.equals("true")) {

                                detailNodeModel.setStatus_theft(mMessage);
                                mDbNodeRepo.update_detailSensorTheft(detailNodeModel);
                                mChange = "2";
                                sendMessageDetail();

                                if (alarm == 0) {
                                    titleNode = mNiceName;
                                    textNode = "ALARM!!";
                                    showNotificationNode();
                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName + " is " + textNode);
                                    dbnode.setMessage("at " + timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel("0");
                                    mDbNodeRepo.insertDbMqtt(dbnode);
                                    alarm = 1;

                                    //playAlarm(1);
                                    checkActivityForeground();
                                    /*if (!flagOnForeground){
                                        Intent iA = new Intent(getApplicationContext(), RingtonePlayingService.class);
                                        startService(iA);
                                    }*/
                                    playAlarm(1);

                                }
                            } else {

                            }
                        }
                    }
                }

                data1.clear();

        }
    }

    private void playAlarm (int code){

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM);
        Log.d(TAG, "Current Vol: "+currentVolume);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        float percent = 0.7f;
        int seventyVolume = (int) (maxVolume*percent);
        audio.setStreamVolume(AudioManager.STREAM_ALARM, seventyVolume, 0);
        audio.setStreamVolume(AudioManager.STREAM_NOTIFICATION, seventyVolume, 0);
        audio.setStreamVolume(AudioManager.STREAM_RING, seventyVolume, 0);

        if (code==1) {
            if (ringtoneSound != null) {
                ringtoneSound.play();
                Log.d(TAG, "playAlarm: ");

            }
        }
        if (code==2){

            if (ringtoneSound != null) {
                ringtoneSound.stop();
                Log.d(TAG, "stopAlarm: ");

            }
        }
    }

    private void updateDetail() {
        String[] outputDevices = TopicID.split("/");
        NodeID = outputDevices[1];
        Channel = outputDevices[3];
        message_topic.put(Channel, mMessage);
        saveDatabase_Detail();
        //toastAndNotif();
    }

    private void addNodeDetail() {

        if (installedNodeModel.getFwName() != null) {
            //Log.d(TAG, "addNodeDetail: ");
            if (installedNodeModel.getFwName().equals("smartfitting")||installedNodeModel.getFwName().equals("smartadapter1ch")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                    saveDatabase_Detail();
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setNice_name_d(NodeID);
                    detailNodeModel.setSensor("light");

                    mDbNodeRepo.insertInstalledNode(detailNodeModel);

                    durationModel.setNodeId(NodeID);
                    durationModel.setChannel("0");
                    durationModel.setStatus("false");
                    durationModel.setTimeStampOn((long) 0);
                    durationModel.setTimeStampOff((long) 0);

                    durationModel.setDuration((long) 0);

                    mDbNodeRepo.insertDurationNode(durationModel);

                    topic1 = "devices/" + NodeID + "/light/0";
                    int qos = 2;
                    try {
                        IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.d("SubscribeButton", " device = " + NodeID);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }

            } else if (installedNodeModel.getFwName().equals("smartadapter4ch")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                    saveDatabase_Detail();
                } else {
                    for (int i = 0; i < 4; i++) {
                        String a = String.valueOf(i);

                        detailNodeModel.setNode_id(NodeID);
                        detailNodeModel.setChannel(String.valueOf(i));
                        detailNodeModel.setStatus("false");
                        detailNodeModel.setNice_name_d(NodeID + " Ch " + String.valueOf(i + 1));
                        detailNodeModel.setSensor("light");

                        mDbNodeRepo.insertInstalledNode(detailNodeModel);

                        durationModel.setNodeId(NodeID);
                        durationModel.setChannel(String.valueOf(i));
                        durationModel.setStatus("false");
                        durationModel.setTimeStampOn((long) 0);
                        //durationModel.setTimeStampOff((long) 0);
                        durationModel.setDuration((long) 0);

                        mDbNodeRepo.insertDurationNode(durationModel);


                        topic1 = "devices/" + NodeID + "/light/" + i;
                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d("SubscribeButton", " device = " + NodeID);
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
            } else if (installedNodeModel.getFwName().equals("smartadapter8ch")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                    saveDatabase_Detail();
                } else {
                    for (int i = 0; i < 8; i++) {
                        String a = String.valueOf(i);

                        detailNodeModel.setNode_id(NodeID);
                        detailNodeModel.setChannel(String.valueOf(i));
                        detailNodeModel.setStatus("false");
                        detailNodeModel.setNice_name_d(NodeID + " Ch " + String.valueOf(i + 1));
                        detailNodeModel.setSensor("light");

                        mDbNodeRepo.insertInstalledNode(detailNodeModel);

                        durationModel.setNodeId(NodeID);
                        durationModel.setChannel(String.valueOf(i));
                        durationModel.setStatus("false");
                        durationModel.setTimeStampOn((long) 0);
                        //durationModel.setTimeStampOff((long) 0);
                        durationModel.setDuration((long) 0);

                        mDbNodeRepo.insertDurationNode(durationModel);


                        topic1 = "devices/" + NodeID + "/light/" + i;
                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d("SubscribeButton", " device = " + NodeID);
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
            } else if (installedNodeModel.getFwName().equals("smartsensordoor")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setSensor("close");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setStatus_sensor("false");
                    detailNodeModel.setStatus_theft("false");
                    detailNodeModel.setNice_name_d(NodeID);

                    mDbNodeRepo.insertInstalledNode(detailNodeModel);

                    durationModel.setNodeId(NodeID);
                    durationModel.setChannel("0");
                    durationModel.setTimeStampOn((long) 0);
                    durationModel.setTimeStampOff((long) 0);
                    durationModel.setDuration((long) 0);

                    mDbNodeRepo.insertDurationNode(durationModel);


                    for (int a = 0; a < 3; a++) {
                        if (a == 0) {
                            topic1 = "devices/" + NodeID + "/light/0";
                        }
                        if (a == 1) {
                            topic1 = "devices/" + NodeID + "/door/close";
                        }
                        if (a == 2) {
                            topic1 = "devices/" + NodeID + "/door/theft";
                        }

                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d("SubscribeSensor", " device = " + mNodeID);
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
            } else if (installedNodeModel.getFwName().equals("smartsensormotion")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setSensor("motion");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setStatus_sensor("false");
                    detailNodeModel.setStatus_theft("false");
                    detailNodeModel.setNice_name_d(NodeID);

                    mDbNodeRepo.insertInstalledNode(detailNodeModel);

                    durationModel.setNodeId(NodeID);
                    durationModel.setChannel("0");
                    durationModel.setTimeStampOn((long) 0);
                    durationModel.setTimeStampOff((long) 0);
                    durationModel.setDuration((long) 0);

                    mDbNodeRepo.insertDurationNode(durationModel);


                    for (int a = 0; a < 3; a++) {
                        if (a == 0) {
                            topic1 = "devices/" + NodeID + "/light/0";
                        }
                        if (a == 1) {
                            topic1 = "devices/" + NodeID + "/motion/motion";
                        }
                        if (a == 2) {
                            topic1 = "devices/" + NodeID + "/motion/theft";
                        }

                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
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
            } else if (installedNodeModel.getFwName().equals("smartsensortemp")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setSensor("temp");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setStatus_temp("0");
                    detailNodeModel.setStatus_hum("0");
                    detailNodeModel.setNice_name_d(NodeID);

                    mDbNodeRepo.insertInstalledNode(detailNodeModel);

                    durationModel.setNodeId(NodeID);
                    durationModel.setChannel("0");
                    durationModel.setTimeStampOn((long) 0);
                    durationModel.setDuration((long) 0);

                    mDbNodeRepo.insertDurationNode(durationModel);


                    for (int a = 0; a < 3; a++) {
                        if (a == 0) {
                            topic1 = "devices/" + NodeID + "/light/0";
                        }
                        if (a == 1) {
                            topic1 = "devices/" + NodeID + "/temperature/degrees";
                        }
                        if (a == 2) {
                            topic1 = "devices/" + NodeID + "/humidity/percent";
                        }

                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d("SubscribeSensor", " device = " + mNodeID);
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
            } else if (installedNodeModel.getFwName().equals("smartsensorprox")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setSensor("prox");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setStatus_sensor("false");
                    detailNodeModel.setStatus_theft("false");
                    detailNodeModel.setNice_name_d(NodeID);

                    mDbNodeRepo.insertInstalledNode(detailNodeModel);

                    durationModel.setNodeId(NodeID);
                    durationModel.setChannel("0");
                    durationModel.setTimeStampOn((long) 0);
                    durationModel.setDuration((long) 0);

                    mDbNodeRepo.insertDurationNode(durationModel);

                        for (int a = 0; a < 5; a++) {
                            if (a == 0) {
                                topic1 = "devices/" + NodeID + "/light/0";
                            }
                            if (a == 1) {
                                topic1 = "devices/" + NodeID + "/prox/status";
                            }
                            if (a == 2) {
                                topic1 = "devices/" + NodeID + "/prox/theft";
                            }
                            if (a == 3) {
                                topic1 = "devices/" + NodeID + "/prox/jarak";
                            }
                            if (a == 4) {
                                topic1 = "devices/" + NodeID + "/dist/range";
                            }

                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d("SubscribeSensor", " device = " + mNodeID);
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
            } else if (installedNodeModel.getFwName().equals("olmatixapp")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setSensor("0");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setStatus_sensor("false");
                    detailNodeModel.setStatus_theft("false");
                    detailNodeModel.setNice_name_d(NodeID);

                    mDbNodeRepo.insertInstalledNode(detailNodeModel);

                    durationModel.setNodeId(NodeID);
                    durationModel.setChannel("0");
                    durationModel.setTimeStampOn((long) 0);
                    durationModel.setDuration((long) 0);

                    mDbNodeRepo.insertDurationNode(durationModel);

                    for (int a = 0; a < 1; a++) {
                        if (a == 0) {
                            topic1 = "";
                        }


                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d("SubscribeOlmatixApp", " device = " + mNodeID);
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
            }  else if (installedNodeModel.getFwName().equals("smartcam")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (mDbNodeRepo.hasDetailObject(detailNodeModel)) {
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setSensor("0");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setStatus_sensor("false");
                    detailNodeModel.setStatus_theft("false");
                    detailNodeModel.setNice_name_d(NodeID);

                    mDbNodeRepo.insertInstalledNode(detailNodeModel);

                    durationModel.setNodeId(NodeID);
                    durationModel.setChannel("0");
                    durationModel.setTimeStampOn((long) 0);
                    durationModel.setDuration((long) 0);

                    mDbNodeRepo.insertDurationNode(durationModel);

                    for (int a = 0; a < 1; a++) {
                        if (a == 0) {
                            topic1 = "";
                        }
                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    Log.d("SubcriberCamera", " device = " + mNodeID);
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
        }
    }

    private void saveDatabase() {
        installedNodeModel.setNodesID(NodeID);
        installedNodeModel.setNodes(messageReceive.get("nodes"));
        installedNodeModel.setName(messageReceive.get("name"));
        installedNodeModel.setLocalip(messageReceive.get("localip"));
        installedNodeModel.setFwName(messageReceive.get("fwname"));
        installedNodeModel.setFwVersion(messageReceive.get("fwversion"));
        if (installedNodeModel.getFwName() != null) {
            addNodeDetail();
        }
        //installedNodeModel.setOnline(messageReceive.get("online"));
        if (messageReceive.containsKey("online")) {
            //Log.d(TAG, "ONLINE : "+messageReceive);
        }

        installedNodeModel.setSignal(messageReceive.get("signal"));

        installedNodeModel.setUptime(messageReceive.get("uptime"));
        if (messageReceive.containsKey("uptime")) {
            if (mMessage != null) {
                /*installedNodeModel.setOnline("true");
                installedNodeModel.setNodesID(NodeID);
                mDbNodeRepo.updateOnline(installedNodeModel);*/
            }
        }
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.getTimeInMillis();
        installedNodeModel.setAdding(now.getTimeInMillis());

        mDbNodeRepo.update(installedNodeModel);
        messageReceive.clear();
        data.clear();
        mChange = "2";
        sendMessageDetail();
        textNode="";
        titleNode="";
    }

    private void saveDatabase_Detail() {
        /*new Thread(new Runnable() {
            @Override
            public void run() {*/
        checkActivityForeground();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final Boolean mSwitch_NotifStatus = sharedPref.getBoolean("switch_notif", true);

                if (!mNodeID.contains("door/close")||
                        !mNodeID.contains("motion/motion")||
                        !mNodeID.contains("prox/status")) {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel(Channel);

                    data1.addAll(mDbNodeRepo.getNodeDetail(NodeID, Channel));
                    int countDB = mDbNodeRepo.getNodeDetail(NodeID, Channel).size();
                    if (countDB != 0) {
                        for (int i = 0; i < countDB; i++) {
                            if (data1.get(i).getNice_name_d() != null) {
                                mNiceName = data1.get(i).getNice_name_d();
                            } else {
                                mNiceName = data1.get(i).getName();
                            }
                            lastValue = data1.get(i).getStatus();

                            if (TextUtils.isEmpty(lastValue)){
                               lastValue = "false";
                            }

                            if (mMessage.equals("true")) {
                                if (lastValue.equals("false")) {
                                    detailNodeModel.setStatus(mMessage);
                                    saveOnTime();
                                    titleNode = mNiceName;
                                    textNode = "ON";
                                    if (mSwitch_NotifStatus) {
                                        if (!flagOnForeground) {
                                            if (!noNotif) {
                                                showNotificationNode();
                                            }
                                        }
                                    }
                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName+" is "+textNode);
                                    dbnode.setMessage("at "+timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel(Channel);
                                    mDbNodeRepo.insertDbMqtt(dbnode);
                                    mDbNodeRepo.update_detail(detailNodeModel);
                                    mChange = "2";
                                    sendMessageDetail();
                                    lastValue="";
                                }

                            } else if (mMessage.equals("false")) {
                                if (lastValue.equals("true")) {
                                    detailNodeModel.setStatus(mMessage);
                                    titleNode = mNiceName;
                                    textNode = "OFF";
                                    saveOffTime();
                                    if (mSwitch_NotifStatus) {
                                        if (!flagOnForeground) {
                                            if (!noNotif) {

                                                showNotificationNode();
                                            }
                                        }
                                    }
                                    SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
                                    dbnode.setTopic(mNiceName+" is "+textNode);
                                    dbnode.setMessage("at "+timeformat.format(System.currentTimeMillis()));
                                    dbnode.setNode_id(NodeID);
                                    dbnode.setChannel(Channel);
                                    mDbNodeRepo.insertDbMqtt(dbnode);
                                    mDbNodeRepo.update_detail(detailNodeModel);
                                    mChange = "2";
                                    sendMessageDetail();
                                    lastValue="";
                                    detailNodeModel.setStatus_theft("false");
                                    mDbNodeRepo.update_detailSensorTheft(detailNodeModel);

                                    alarm=0;

                                    playAlarm(2);
                                }
                            }
                        }
                    }

                    data1.clear();
                }
           /* }
        }).start();*/
    }

    private void saveOnTime() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                //Log.d(TAG, "run ON: "+Channel);
                    durationModel.setNodeId(NodeID);
                    durationModel.setChannel(Channel);
                    durationModel.setStatus(mMessage);
                    Calendar now = Calendar.getInstance();
                    now.setTime(new Date());
                    now.getTimeInMillis();
                    durationModel.setTimeStampOn(now.getTimeInMillis());
                    durationModel.setTimeStampOff(Long.valueOf("0"));

                mDbNodeRepo.insertDurationNode(durationModel);
            }
        });
    }

    private void saveOffTime() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            @Override
            public void run() {
                long dura;
                durationModel.setNodeId(NodeID);
                durationModel.setChannel(Channel);
                durationModel.setStatus(mMessage);
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.getTimeInMillis();
                durationModel.setTimeStampOff(now.getTimeInMillis());
                if(durationModel.getTimeStampOn()!=null) {

                    dura = (now.getTimeInMillis() - durationModel.getTimeStampOn())/1000;
                    if (dura<25292000) {
                        durationModel.setDuration(dura);
                    } else {
                        durationModel.setDuration(Long.valueOf(0));
                    }

                }
                mDbNodeRepo.updateOff(durationModel);

                data3.addAll(mDbNodeRepo.getNodeUpdateZero());
                int countDB = mDbNodeRepo.getNodeUpdateZero().size();
                if (countDB != 0) {
                    for (int i = 0; i < countDB; i++) {
                        if (data3.get(i).getTimeStampOn() != null) {
                            dura = data3.get(i).getTimeStampOff()-data3.get(i).getTimeStampOn();
                            int id = data3.get(i).getId();
                            durationModel.setId(id);
                            durationModel.setDuration(dura/1000);
                            mDbNodeRepo.updateOffbyID(durationModel);

                        }
                    }
                }
                data3.clear();
            }
        });
    }

    private void doAddNodeSub() {
        String topic = "devices/" + add_NodeID + "/$online";
        int qos = 2;
        try {
            if (mqttClient!=null) {
                IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "doAddNodeSub: "+add_NodeID);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    }
                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        flagNode = true;

    }

    private void doSubAll() {
        int countDB = mDbNodeRepo.getNodeList().size();
        data.clear();
        data.addAll(mDbNodeRepo.getNodeList());
                if (countDB != 0) {
                    for (int i = 0; i < countDB; i++) {
                        final String mNodeID = data.get(i).getNodesID();
                        //Log.d("DEBUG", "Count list: " + mNodeID);
                        for (int a = 0; a < 7; a++) {
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
                            if (a == 5) {
                                topic = "devices/" + mNodeID + "/$location";
                            }
                            if (a == 6) {
                                //topic = "devices/" + mNodeID + "/$calling";
                            }
                            int qos = 2;
                            try {
                                if (mqttClient!=null) {
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
                                }
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    doSubAllDetail();

                }
    }

    private void doSubAllDetail() {
        int countDB = mDbNodeRepo.getNodeDetailList().size();
        data1.clear();
        data1.addAll(mDbNodeRepo.getNodeDetailList());
                if (countDB != 0) {
                    for (int i = 0; i < countDB; i++) {
                        final String mNodeID = data1.get(i).getNode_id();
                        final String mChannel = data1.get(i).getChannel();
                        topic1 = "devices/" + mNodeID + "/light/" + mChannel;
                        int qos = 2;
                        try {
                            IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
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

    }

    private void doAllsubDetailSensor() {

        int countDB = mDbNodeRepo.getNodeDetailList().size();
        data1.clear();
        data1.addAll(mDbNodeRepo.getNodeDetailList());
                if (countDB != 0) {
                    for (int i = 0; i < countDB; i++) {
                        final String mNodeID1 = data1.get(i).getNode_id();
                        final String mSensorT = data1.get(i).getSensor();
                        //Log.d("DEBUG", "Count list Sensor: " + mSensorT);
                        if (mSensorT != null && mSensorT.equals("close")) {
                            for (int a = 0; a < 2; a++) {
                                if (a == 0) {
                                    topic1 = "devices/" + mNodeID1 + "/door/close";
                                }
                                if (a == 1) {
                                    topic1 = "devices/" + mNodeID1 + "/door/theft";
                                }

                                int qos = 2;
                                try {
                                    IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
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
                        if (mSensorT != null && mSensorT.equals("motion")) {
                            for (int a = 0; a < 2; a++) {
                                if (a == 0) {
                                    topic1 = "devices/" + mNodeID1 + "/motion/motion";
                                }
                                if (a == 1) {
                                    topic1 = "devices/" + mNodeID1 + "/motion/theft";
                                }

                                int qos = 2;
                                try {
                                    IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
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
                        if (mSensorT != null && mSensorT.equals("temp")) {
                            for (int a = 0; a < 2; a++) {
                                if (a == 0) {
                                    topic1 = "devices/" + mNodeID1 + "/temperature/degrees";
                                }
                                if (a == 1) {
                                    topic1 = "devices/" + mNodeID1 + "/humidity/percent";
                                }

                                int qos = 2;
                                try {
                                    IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
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
                        if (mSensorT != null && mSensorT.equals("prox")) {
                            for (int a = 0; a < 4; a++) {
                                if (a == 0) {
                                    topic1 = "devices/" + mNodeID1 + "/prox/status";
                                }
                                if (a == 1) {
                                    topic1 = "devices/" + mNodeID1 + "/prox/theft";
                                }
                                if (a == 2) {
                                    topic1 = "devices/" + mNodeID1 + "/prox/jarak";
                                }
                                if (a == 3) {
                                    topic1 = "devices/" + mNodeID1 + "/dist/range";
                                }

                                int qos = 2;
                                try {
                                    IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
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
                    flagSub = false;
                }
        setFlagSub();
    }

    private class MqttEventCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "connectionLost: "+cause);
            if (cause!=null) {
                doDisconnect();
            }
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mStatusServer = sharedPref.getBoolean("conStatus", false);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("conStatus", false);
            editor.apply();
            flagConn = false;
            doCon=false;
            sendMessage();
            SimpleDateFormat timeformat = new SimpleDateFormat("d MMM | hh:mm:ss");
            dbnode.setTopic("Connection lost");
            dbnode.setMessage("at "+timeformat.format(System.currentTimeMillis()));
            mDbNodeRepo.insertDbMqtt(dbnode);
            sendMessageDetail();
            connLose();

        }

        @Override
        public void messageArrived(String topic, final MqttMessage message) throws Exception {

            Log.d("Receive MQTTMessage", " = " + topic + " message = " + message.toString());
            TopicID = topic;
            mNodeID = topic;
            mMessage = message.toString();
            String[] outputDevices = TopicID.split("/");
            NodeIDSensor = outputDevices[1];

            if (mNodeID.contains("$")) {
                addNode();

            } else if (mNodeID.contains("door/close")||mNodeID.contains("motion/motion")||mNodeID.contains("prox/status")) {
                updateSensorDoor();

            } else if (mNodeID.contains("theft")) {
                if (mMessage.equals("true")) {
                    updateSensorTheft();
                }

            } else if (mNodeID.contains("temperature/degrees")) {
                    UpdateSensorTemp();

            } else if (mNodeID.contains("humidity/percent")) {
                    UpdateSensorHum();

            } else if (mNodeID.contains("prox/jarak")) {
                UpdateSensorJarak();

            } else if (mNodeID.contains("dist/range")) {
                UpdateSensorRange();

            } else   {
                updateDetail();
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //Log.d(TAG, "deliveryComplete: ");
        }
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location mLocation) {
            Log.i("*****************", "Location changed");
            if(isBetterLocation(mLocation, previousBestLocation)) {

                Double lat = (mLocation.getLatitude());
                Double lng = (mLocation.getLongitude());
                locationDistance(lat,lng);
            }
        }

        public void onProviderDisabled(String provider) {
            /*final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                    "Your GPS is disable.. We'll use Network for location",TSnackbar.LENGTH_LONG);
            snackbarWrapper.setAction("Olmatix",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "Action",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            snackbarWrapper.show(); */
        }

        public void onProviderEnabled(String provider) {
            /*final SnackbarWrapper snackbarWrapper = SnackbarWrapper.make(getApplicationContext(),
                    "GPS Enable.. Nice, it will be much more accurate",TSnackbar.LENGTH_LONG);
            snackbarWrapper.setAction("Olmatix",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getApplicationContext(), "Action",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            snackbarWrapper.show();*/
        }

    public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }
    }

    public void locationDistance(Double lat, Double lng){

            if (lat!=0 && lng!=0) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

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
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("DEBUG", "Geocoder ERROR", e);
                                }
                            }).start();
                            loc = OlmatixUtils.gpsDecimalFormat.format(lat) + " : " + OlmatixUtils.gpsDecimalFormat.format(lng);
                            getLocationCityName(lat,lng);
                        }

                        final float[] res = new float[3];
                        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
                        Location.distanceBetween(lat, lng, mPrefHelper.getHomeLatitude(), mPrefHelper.getHomeLongitude(), res);
                        if (mPrefHelper.getHomeLatitude() != 0) {

                            String unit = " m";
                            if (res[0] > 2000) {// uuse km
                                unit = " km";
                                res[0] = res[0] / 1000;

                            }
                            Distance = loc +", it's "+ (int) res[0] + unit ;
                            Log.d("DEBUG", "Distance SERVICE 1: " + Distance);
                            titleNode = "Current Location is ";
                            textNode = Distance + " from home";
                            notifyID = 5;
                            sendMessageDetail();
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            final Boolean mSwitch_Notif = sharedPref.getBoolean("switch_loc", true);
                            if (mSwitch_Notif) {
                                showNotificationLoc();
                            }

                            sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
                            mStatusServer = sharedPref.getBoolean("conStatus", false);
                            if (mStatusServer) {
                                topic = "devices/" + deviceId + "/$location";
                                String payload = String.valueOf(lat) + "," + String.valueOf(lng);
                                byte[] encodedPayload = new byte[0];
                                try {
                                    if (mqttClient != null) {
                                        encodedPayload = payload.getBytes("UTF-8");
                                        MqttMessage message = new MqttMessage(encodedPayload);
                                        message.setQos(1);
                                        message.setRetained(true);
                                        Connection.getClient().publish(topic, message);
                                        Log.d(TAG, "Publich Location " +message);
                                    }
                                } catch (UnsupportedEncodingException | MqttException e) {
                                    e.printStackTrace();
                                }
                            }
                            mPrefHelper.setPhoneLatitude(lat);
                            mPrefHelper.setPhoneLongitude(lng);


                        }
                    }
                }).start();

            }

    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private  void showNotificationLoc(){

        final Boolean mSwitch_Notif = sharedPref.getBoolean("switch_loc", true);
        if (mSwitch_Notif) {
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, MainActivity.class), 0);

            // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(drawable.ic_location_red)  // the status icon
                    .setTicker(textNode)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle(getText(string.local_service_label_loc))  // the label of the entry
                    .setContentText(textNode)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .setAutoCancel(true)
                    .build();
            // Add as notification
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(5, notification);
        }
    }

    public String getLocationCityName(double lat, double lon){
        JSONObject result = getLocationFormGoogle(lat + "," + lon );
        //return getCityAddress(result);
        return null;
    }

    protected JSONObject getLocationFormGoogle(String placesName) {

        RequestQueue reqQueue = Volley.newRequestQueue(this);

        String apiRequest = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+placesName; //+ "&ka&sensor=false"

        JsonObjectRequest stateReq = new JsonObjectRequest(Request.Method.GET, apiRequest, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                getCityAddress(response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response", error.toString());
            }
        });
        // add it to the RequestQueue
        reqQueue.add(stateReq);

        return null;
    }

    protected static String getCityAddress( JSONObject result ){
        if( result.has("results") ){
            try {
                JSONArray array = result.getJSONArray("results");
                if( array.length() > 0 ){
                    JSONObject place = array.getJSONObject(0);
                    JSONArray components = place.getJSONArray("address_components");
                    for( int i = 0 ; i < components.length() ; i++ ){
                        JSONObject component = components.getJSONObject(i);
                        JSONArray types = component.getJSONArray("types");
                        for( int j = 0 ; j < types.length() ; j ++ ){
                            if( types.getString(j).equals("locality") ){
                                return component.getString("long_name");
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
