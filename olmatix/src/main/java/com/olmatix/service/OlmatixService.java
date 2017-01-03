package com.olmatix.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.olmatix.database.DbNode;
import com.olmatix.database.DbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.DurationModel;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.ui.activity.MainActivity;
import com.olmatix.ui.fragment.Detail_Node;
import com.olmatix.utils.Connection;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


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
    public static DbNodeRepo mDbNodeRepo;
    private static String TAG = OlmatixService.class.getSimpleName();
    private static boolean hasWifi = false;
    private static boolean hasMmobile = false;
    private static ArrayList<InstalledNodeModel> data;
    public volatile MqttAsyncClient mqttClient;
    HashMap<String, String> messageReceive = new HashMap<>();
    HashMap<String, String> message_topic = new HashMap<>();
    CharSequence text;
    CharSequence textNode;
    CharSequence titleNode;
    ArrayList<DetailNodeModel> data1;
    ArrayList<InstalledNodeModel> data2;
    String add_NodeID;
    boolean flagSub = true;
    boolean flagNode = false;
    boolean flagConn = true;
    boolean connSuccess = false;
    int notifyID = 0;
    String currentApp = "NULL";
    String topic;
    String topic1;
    private Thread thread;
    private ConnectivityManager mConnMan;
    private String deviceId;
    private String stateoffMqtt="false";
    private InstalledNodeModel installedNodeModel;
    private DetailNodeModel detailNodeModel;
    private DurationModel durationModel;
    private DbNode dbnode;
    private String NodeID, Channel;
    private String mMessage;
    private NotificationManager mNM;
    private int NOTIFICATION = R.string.local_service_label;
    private String mNodeID;
    private String mNiceName;
    private String mNiceNameN;
    private String NodeIDSensor;
    private String TopicID;
    private String mChange = "";

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            add_NodeID = intent.getStringExtra("NodeID");
            Log.d("DEBUG", "onReceive: " + add_NodeID);
            doAddNodeSub();
        }
    };

    private void callDis() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!flagConn) {
                    doDisconnect();
                    callCon();
                    flagConn = true;
                }
            }
        }, 1000);
    }

    private void callCon() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doConnect();
            }
        }, 1000);
    }

    private void setFlagSub() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                flagSub = true;
                Log.d(TAG, "run: " + flagSub);
                unSubIfnotForeground();
            }
        }, 25000);
    }

    private void doDisconnect() {
        Log.d(TAG, "doDisconnect()");
        try {
            Connection.getClient().disconnect();
            stateoffMqtt = "false";
            sendMessage();
            flagConn = true;
            Log.d(TAG, "doDisconnect() done");


        } catch (MqttException e) {
            e.printStackTrace();
            Log.d(TAG, "onReceive: " + String.valueOf(e.getMessage()));
        }
    }

    @Override
    public void onCreate() {

        IntentFilter intent = new IntentFilter();
        setClientID();
        intent.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new OlmatixBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        data = new ArrayList<>();
        data1 = new ArrayList<>();
        data2 = new ArrayList<>();


        mDbNodeRepo = new DbNodeRepo(getApplicationContext());
        installedNodeModel = new InstalledNodeModel();
        detailNodeModel = new DetailNodeModel();
        durationModel = new DurationModel();
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("addNode"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendMessage();
        return START_STICKY;
    }

    private void unSubIfnotForeground() {

        printForegroundTask();
        Log.d("Unsubscribe", " uptime and signal");

        if (!currentApp.equals("com.olmatix.lesjaw.olmatix")) {
            int countDB = mDbNodeRepo.getNodeList().size();
            Log.d("DEBUG", "Count list Node: " + countDB);
            data.addAll(mDbNodeRepo.getNodeList());
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    final String mNodeID1 = data.get(i).getNodesID();
                    Log.d("DEBUG", "Count list: " + mNodeID1);
                    for (int a = 0; a < 2; a++) {
                        if (a == 0) {
                            topic = "devices/" + mNodeID1 + "/$signal";
                        }
                        if (a == 1) {
                            topic = "devices/" + mNodeID1 + "/$uptime";
                        }

                        try {
                            Connection.getClient().unsubscribe(topic);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }

                    }
                    Log.d("Unsubscribe", " device = " + mNodeID1);
                }
            }
            data.clear();
        }
    }

    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        //text = getText(R.string.local_service_started);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.olmatixlogo)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setOngoing(true)
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    private void showNotificationNode() {
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.olmatixsmall)
                        .setContentTitle(titleNode)
                        .setContentText(textNode);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notifyID, builder.build());
    }

    private void setClientID() {
        // Context mContext;
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        deviceId = "OlmatixApp-" + wInfo.getMacAddress();

        if (deviceId == null) {
            deviceId = MqttAsyncClient.generateClientId();
        }
    }

    private void doConnect() {
        Log.d(TAG, "Check Stateof MQTT: " + stateoffMqtt);

        if (!stateoffMqtt.equals("true")) {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String mServerURL = sharedPref.getString("server_address", "cloud.olmatix.com");
            String mServerPort = sharedPref.getString("server_port", "1883");
            String mUserName = sharedPref.getString("user_name", "olmatix1");
            String mPassword = sharedPref.getString("password", "olmatix");

            final Boolean mSwitch_conn = sharedPref.getBoolean("switch_conn", true);
            //Log.d("DEBUG", "SwitchConnPreff: " + mSwitch_conn);

            final MqttConnectOptions options = new MqttConnectOptions();

            options.setUserName(mUserName);
            options.setPassword(mPassword.toCharArray());
            final MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(), "tcp://" + mServerURL + ":" + mServerPort, deviceId);

            if (mSwitch_conn) {
                options.setCleanSession(false);
            } else {
                options.setCleanSession(true);
            }
            String topic = "status/" + deviceId + "/$online";
            byte[] payload = "false".getBytes();
            options.setWill(topic, payload, 1, true);
            options.setKeepAliveInterval(300);
            Connection.setClient(client);

            text = "Connecting to server..";
            showNotification();
            Log.d(TAG, "doConnect: " + deviceId);

            try {

                IMqttToken token = client.connect(options);
                token.setActionCallback(new IMqttActionListener() {

                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                        text = "Connected";
                        showNotification();
                        stateoffMqtt = "true";
                        sendMessage();

                        Connection.getClient().setCallback(new MqttEventCallback());

                        try {
                            String topic = "status/" + deviceId + "/$online";
                            String payload = "true";
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

                            Connection.getClient().subscribe("test", 0, getApplicationContext(), new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    text = "Connected";
                                    showNotification();
                                    stateoffMqtt = "true";
                                    sendMessage();

                                    Log.d(TAG, "Check FlasgConn: " + flagConn);
                                    if (flagConn) {
                                        if (stateoffMqtt.equals("true")) {
                                            if (!mSwitch_conn) {
                                                doSubAll();
                                            }
                                        }
                                    } else {
                                        //Log.d(TAG, "Call Disconn: " + flagConn);
                                        callDis();
                                    }
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    //Toast.makeText(getApplicationContext(), R.string.sub_fail, Toast.LENGTH_SHORT).show();
                                    Log.e("error", exception.toString());

                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        //Toast.makeText(getApplicationContext(), R.string.conn_fail+exception.toString(), Toast.LENGTH_SHORT).show();
                        Log.e("mqtt", exception.toString());
                        stateoffMqtt = "false";
                        Log.d("Sender", "After fail: " + stateoffMqtt);
                        sendMessage();
                        text = "Not Connected";
                        showNotification();
                    }
                });

            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                switch (e.getReasonCode()) {
                    case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                        Toast.makeText(getApplicationContext(), "Server Offline", Toast.LENGTH_SHORT).show();
                        break;

                    case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                        Toast.makeText(getApplicationContext(), "Olmatix connect timed out", Toast.LENGTH_SHORT).show();
                        break;

                    case MqttException.REASON_CODE_CONNECTION_LOST:
                        Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_SHORT).show();
                        break;

                    case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                        Log.v(TAG, "c" + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Server connection error", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        break;
                    case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                        Intent i = new Intent("RAISEALLARM");
                        i.putExtra("ALLARM", e);
                        Log.e(TAG, "b" + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Failed wrong auth (bad user name or password", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Log.e(TAG, "a" + e.getMessage());
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        text = "Disconnected";
                        showNotification();
                }
            }
        }
    }

    private void sendMessage() {
        Intent intent = new Intent("MQTTStatus");
        intent.putExtra("MQTT State", stateoffMqtt);
        intent.putExtra("NotifyChangeNode", mChange);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageDetail() {
        Intent intent = new Intent("MQTTStatusDetail");
        intent.putExtra("ServerConn", stateoffMqtt);
        intent.putExtra("NotifyChangeDetail", mChange);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public String getThread() {
        return Long.valueOf(thread.getId()).toString();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        // Tell the user we stopped.
        doDisconnect();
        Toast.makeText(this, R.string.service_stop, Toast.LENGTH_SHORT).show();
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
        checkValidation();
    }

    private void checkValidation() {
        if (flagNode) {
            if (messageReceive.containsKey("online")) {
                Log.d("CheckValid online", "Passed");
                if (mMessage.equals("true")) {
                    Log.d("CheckValid online", " true Passed");
                    saveFirst();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.deviceoffline, Toast.LENGTH_SHORT).show();
                    doUnsubscribe();
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
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            now.getTimeInMillis();
            installedNodeModel.setAdding(now.getTimeInMillis());

            mDbNodeRepo.insertDb(installedNodeModel);
            Toast.makeText(getApplicationContext(), "Add Node Successfully", Toast.LENGTH_SHORT).show();
            Log.d("saveFirst", "Add Node success, ");
            doSub();
        } else {
            installedNodeModel.setNodesID(NodeID);
            if (mDbNodeRepo.hasObject(installedNodeModel)) {
                Toast.makeText(getApplicationContext(), "Checking this Node ID : " + NodeID + ", its exist, we are updating Node status", Toast.LENGTH_SHORT).show();
                Log.d("saveFirst", "You already have this Node, DB = " + NodeID + ", Exist, we are updating Node status");
                saveDatabase();

            } else {
                installedNodeModel.setNodesID(NodeID);
                installedNodeModel.setNodes(messageReceive.get("online"));
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.getTimeInMillis();
                installedNodeModel.setAdding(now.getTimeInMillis());

                mDbNodeRepo.insertDb(installedNodeModel);
                Toast.makeText(getApplicationContext(), "Successfully Add Node", Toast.LENGTH_SHORT).show();
                Log.d("saveFirst", "Add Node success, ");
                doSub();

            }
        }
    }

    private void doSub() {
        for (int a = 0; a < 4; a++) {
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
        messageReceive.clear();
    }

    private void printForegroundTask() {
        //String currentApp = "NULL";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        //Log.e(TAG, "Current App in foreground is: " + currentApp);
    }

    private void toastAndNotif() {

        int id = Integer.parseInt(NodeID.replaceAll("[\\D]", ""));
        int ch = Integer.parseInt(Channel.replaceAll("[\\D]", ""));
        int notid = id + ch;

        printForegroundTask();
        //checkActivityForeground();
        if (!currentApp.equals("com.olmatix.lesjaw.olmatix")) {
            if (flagSub) {
                String state = "";
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
                        state = data1.get(i).getStatus();
                    }
                }

                if (state.equals("true") || state.equals("ON")) {
                    state = "ON";
                }
                if (state.equals("false") || state.equals("OFF")) {
                    state = "OFF";
                }

                if (mNiceName != null) {
                    if (!state.equals("")) {
                        // Toast.makeText(getApplicationContext(), mNiceName + " is " + state, Toast.LENGTH_SHORT).show();
                        titleNode = mNiceName;
                        textNode = state;
                        notifyID = notid;
                        showNotificationNode();
                    }
                }
                messageReceive.clear();
                message_topic.clear();
                data1.clear();
            }
        }
    }

    protected void checkActivityForeground() {
        //Log.d(TAG, "start checking for Activity in foreground");
        Intent intent = new Intent();
        intent.setAction(Detail_Node.UE_ACTION);
        sendOrderedBroadcast(intent, null, new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int result = getResultCode();

                if (result != Activity.RESULT_CANCELED) { // Activity caught it
                    //Log.d(TAG, "An activity caught the broadcast, result " + result);
                    activityInForeground();
                    return;
                }
                //Log.d(TAG, "No activity did catch the broadcast.");
                noActivityInForeground();
            }
        }, null, Activity.RESULT_CANCELED, null, null);
    }

    protected void activityInForeground() {
        // TODO something you want to happen when an Activity is in the foreground
        flagSub = true;
    }

    protected void noActivityInForeground() {
        // TODO something you want to happen when no Activity is in the foreground
        flagSub = false;
        //stopSelf(); // quit
    }

    private void updateSensorDoor() {

        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");
            detailNodeModel.setStatus_sensor(mMessage);

            mDbNodeRepo.update_detailSensor(detailNodeModel);
            mChange = "2";
            sendMessageDetail();
        }
    }

    private void updateSensorTheft() {
        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");
            detailNodeModel.setStatus_theft(mMessage);

            mDbNodeRepo.update_detailSensor(detailNodeModel);
            mChange = "2";
            sendMessageDetail();
            data1.addAll(mDbNodeRepo.getNodeDetail(NodeIDSensor, "0"));
            int countDB = mDbNodeRepo.getNodeDetail(NodeIDSensor, "0").size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    if (data1.get(i).getNice_name_d() != null) {
                        mNiceName = data1.get(i).getNice_name_d();
                    } else {
                        mNiceName = data1.get(i).getName();
                    }
                }
            }
            if (mMessage.equals("true")) {
                titleNode = mNiceName;
                textNode = "ALARM!!";
                showNotificationNode();
            }
            data1.clear();
        }
    }

    private void updateDetail() {
        String[] outputDevices = TopicID.split("/");
        NodeID = outputDevices[1];
        Channel = outputDevices[3];
        message_topic.put(Channel, mMessage);
        saveDatabase_Detail();
        toastAndNotif();
    }

    private void addNodeDetail() {

        if (installedNodeModel.getFwName() != null) {

            if (installedNodeModel.getFwName().equals("smartfitting")) {
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
        installedNodeModel.setOnline(messageReceive.get("online"));
        if (messageReceive.containsKey("online")) {
            //checkActivityForeground();
            printForegroundTask();
            if (!currentApp.equals("com.olmatix.lesjaw.olmatix")) {
                if (flagSub) {
                    installedNodeModel.setNodesID(NodeID);
                    data2.addAll(mDbNodeRepo.getNodeListbyNode(NodeID));
                    int countDB = mDbNodeRepo.getNodeListbyNode(NodeID).size();
                    if (countDB != 0) {
                        for (int i = 0; i < countDB; i++) {
                            if (data2.get(i).getNice_name_n() != null) {
                                mNiceNameN = data2.get(i).getNice_name_n();
                            } else {
                                mNiceNameN = data2.get(i).getFwName();
                            }
                            int id = Integer.parseInt(NodeID.replaceAll("[\\D]", ""));

                            notifyID = id + 2;

                            if (mMessage.equals("true")) {
                                titleNode = mNiceNameN;
                                textNode = "ONLINE";
                                showNotificationNode();
                            } else {
                                titleNode = mNiceNameN;
                                textNode = "OFFLINE";
                                showNotificationNode();

                            }
                        }
                    }
                }
                data2.clear();
            }
        }

        installedNodeModel.setSignal(messageReceive.get("signal"));
        installedNodeModel.setUptime(messageReceive.get("uptime"));
        if (messageReceive.containsKey("uptime")) {
            if (mMessage != null) {
                installedNodeModel.setOnline("true");
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
        sendMessage();

    }

    private void saveDatabase_Detail() {

        if (!mNodeID.contains("door")) {
            detailNodeModel.setNode_id(NodeID);
            detailNodeModel.setChannel(Channel);
            if (mMessage.equals("true")) {
                detailNodeModel.setStatus(mMessage);

                saveOnTime();

            } else if (mMessage.equals("false")) {
                detailNodeModel.setStatus(mMessage);

                saveOffTime();
            }

            mDbNodeRepo.update_detail(detailNodeModel);
            mChange = "2";
            sendMessageDetail();
            data1.clear();
        }
    }

    private void saveOnTime() {
        new Handler().post(new Runnable() {

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
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                //Log.d(TAG, "run OFF: "+Channel);
                durationModel.setNodeId(NodeID);
                durationModel.setChannel(Channel);
                durationModel.setStatus(mMessage);
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.getTimeInMillis();
                durationModel.setTimeStampOff(now.getTimeInMillis());
                if(durationModel.getTimeStampOn()!=null) {
                    //Log.d(TAG, "run: " + Long.valueOf(durationModel.getTimeStampOn()));
                    durationModel.setDuration((now.getTimeInMillis() - durationModel.getTimeStampOn())/1000);
                }
                mDbNodeRepo.updateOff(durationModel);
            }
        });
    }

    private void doAddNodeSub() {
        String topic = "devices/" + add_NodeID + "/$online";
        int qos = 2;
        try {
            IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Subscribe", " device = " + NodeID);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        flagNode = true;
    }

    private void doUnsubscribe() {
        String topic = "devices/" + NodeID + "/$online";
        try {
            Connection.getClient().unsubscribe(topic);
            Log.d("Unsubscribe", " device = " + NodeID);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void doSubAll() {

        int countDB = mDbNodeRepo.getNodeList().size();
        Log.d("DEBUG", "Count list Node: " + countDB);
        data.addAll(mDbNodeRepo.getNodeList());
        if (countDB != 0) {
            for (int i = 0; i < countDB; i++) {
                final String mNodeID = data.get(i).getNodesID();
                Log.d("DEBUG", "Count list: " + mNodeID);
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
                topic1 = "devices/" + mNodeID + "/light/" + mChannel;
                int qos = 2;
                try {
                    IMqttToken subToken = Connection.getClient().subscribe(topic1, qos);
                    subToken.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("SubscribeButton", " device = " + mNodeID);
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
        data1.clear();
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
                Log.d("DEBUG", "Count list Sensor: " + mSensorT);
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
            }
            flagSub = false;
            setFlagSub();
            data1.clear();
        }
    }

    class OlmatixBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean hasConnectivity = false;
            boolean hasChanged = false;

            NetworkInfo nInfo = mConnMan.getActiveNetworkInfo();
            if (nInfo != null) {
                if (nInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    hasChanged = true;
                    hasWifi = nInfo.isConnected();
                } else if (nInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    hasChanged = true;
                    hasMmobile = nInfo.isConnected();
                }
            } else {
                //Not Connected info
                String msg = getString(R.string.err_internet);
                Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
                stateoffMqtt = "false";
                sendMessage();
                text = "Disconnected";
                showNotification();
                flagConn = false;
            }

            hasConnectivity = hasMmobile || hasWifi;
            Log.d(TAG, "hasConn: " + hasConnectivity + " hasChange: " + hasChanged + " - " +
                    (mqttClient == null || !mqttClient.isConnected()));

            if (hasConnectivity && hasChanged && (mqttClient == null || !mqttClient.isConnected())) {
                doConnect();
                Log.d(TAG, "Connecting");
                //callDis();

            } else if (!hasConnectivity && mqttClient != null && mqttClient.isConnected()) {
                doDisconnect();
                Log.d(TAG, "Disconnecting");
            }
        }
    }

    public class OlmatixBinder extends Binder {
        public OlmatixService getService() {
            return OlmatixService.this;
        }
    }

    private class MqttEventCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {

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
            } else if (mNodeID.contains("close")) {
                updateSensorDoor();
            } else if (mNodeID.contains("theft")) {
                updateSensorTheft();
            } else {
                updateDetail();
            }
            if (flagSub) {
                dbnode.setTopic(topic);
                dbnode.setMessage(mMessage);
                mDbNodeRepo.insertDbMqtt(dbnode);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }


}






