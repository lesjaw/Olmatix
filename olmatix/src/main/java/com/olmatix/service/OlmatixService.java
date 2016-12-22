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
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Detail_NodeModel;
import com.olmatix.model.Installed_NodeModel;
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

    private static String TAG = OlmatixService.class.getSimpleName();
    public final static String MY_ACTION = "MY_ACTION";
    private static boolean hasWifi = false;
    private static boolean hasMmobile = false;
    private Thread thread;
    private ConnectivityManager mConnMan;
    public volatile MqttAsyncClient mqttClient;
    private String deviceId;
    private String stateoffMqtt;
    public static dbNodeRepo dbNodeRepo;
    private Installed_NodeModel installedNodeModel;
    private Detail_NodeModel detailNodeModel;
    private static ArrayList<Installed_NodeModel> data;
    private String NodeID,Channel;
    private String mMessage;
    private NotificationManager mNM;
    private int NOTIFICATION = R.string.local_service_started;
    HashMap<String,String>  messageReceive = new HashMap<>();
    HashMap<String,String> message_topic = new HashMap<>();
    private String mNodeID;
    private String mNiceName;
    private String mNiceNameN;
    private String NodeIDSensor;
    private String TopicID;
    private String mChange="";
    CharSequence text;
    CharSequence textNode;
    CharSequence titleNode;
    ArrayList<Detail_NodeModel> data1;
    ArrayList<Installed_NodeModel> data2;
    String add_NodeID;
    boolean flagAct=true;
    boolean flagSub=true;
    boolean flagNode=false;
    int notifyID=0;
    String currentApp = "NULL";
    String topic;
    String topic1;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */

    class OlmatixBroadcastReceiver  extends BroadcastReceiver {

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
                Log.d("Sender", "MQTT Status No Internet: " +stateoffMqtt);
                sendMessage();
                text = "Disconnected";
                showNotification();
                //flagSub= true;
            }

            hasConnectivity = hasMmobile || hasWifi;
            Log.d(TAG, "hasConn: " + hasConnectivity + " hasChange: " + hasChanged + " - " + (mqttClient == null || !mqttClient.isConnected()));
            if (hasConnectivity && hasChanged && (mqttClient == null || !mqttClient.isConnected())) {
                doConnect();

            } else if (!hasConnectivity && mqttClient != null && mqttClient.isConnected()) {
                doDisconnect();
            }
        }

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            add_NodeID = intent.getStringExtra("NodeID");
            Log.d("DEBUG", "onReceive: "+add_NodeID);
            doAddNodeSub();

        }
    };


    private void doDisconnect() {
        IMqttToken token;
        Log.d(TAG, "doDisconnect()");
        try {
            token = mqttClient.disconnect();
            token.waitForCompletion(1000);
            stateoffMqtt = "false";
            sendMessage();
        } catch (MqttException e) {
            e.printStackTrace();
            Log.d(TAG, "onReceive: " + String.valueOf(e.getMessage()));
        }
    }

    public class OlmatixBinder extends Binder {
        public OlmatixService getService(){
            return OlmatixService.this;
        }
    }

    @Override
    public void onCreate() {

        IntentFilter intent = new IntentFilter();
        setClientID();
        intent.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new OlmatixBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        data = new ArrayList<>();
        data1 = new ArrayList<>();
        data2 = new ArrayList<>();

        dbNodeRepo = new dbNodeRepo(getApplicationContext());
        installedNodeModel = new Installed_NodeModel();
        detailNodeModel = new Detail_NodeModel();
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("addNode"));

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (flagAct) {
            // Toast.makeText(getApplicationContext(), R.string.service_start, Toast.LENGTH_SHORT).show();
            Log.d("Service = ", "Starting..");
            text = "Starting...";
            showNotification();

            flagAct = false;
        }

        sendMessage();

        return START_STICKY;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()");
        //android.os.Debug.waitForDebugger();
        super.onConfigurationChanged(newConfig);

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
        //Toast.makeText(getApplicationContext(), R.string.connecting, Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mServerURL = sharedPref.getString("server_address", "cloud.olmatix.com");
        String mServerPort = sharedPref.getString("server_port", "1883");
        String mUserName = sharedPref.getString("user_name", "olmatix1");
        String mPassword = sharedPref.getString("password", "olmatix");

        final MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mUserName);
        options.setPassword(mPassword.toCharArray());
        final MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(),"tcp://"+mServerURL+":"+mServerPort,deviceId);
        options.setCleanSession(true);
        String topic = "status/"+deviceId+"/$online";
        byte[] payload = "false".getBytes();
        options.setWill(topic, payload ,1,true);
        options.setKeepAliveInterval(300);
        Connection.setClient(client);

        text = "Connecting to server..";
        showNotification();

        try {

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Toast.makeText(getApplicationContext(),  R.string.conn_success, Toast.LENGTH_SHORT).show();
                    if (flagSub) {
                        doSubAll();
                        //flagSub = false;
                    }

                    text = "Connected";
                    showNotification();

                    Connection.getClient().setCallback(new MqttEventCallback());

                    try {
                        String topic = "status/"+deviceId+"/$online";
                        String payload = "true";
                        byte[] encodedPayload = new byte[0];
                        try {
                            encodedPayload = payload.getBytes("UTF-8");
                            MqttMessage message = new MqttMessage(encodedPayload);
                            message.setQos(1);
                            message.setRetained(true);
                            Connection.getClient().publish(topic, message);
                        }
                        catch (UnsupportedEncodingException | MqttException e)
                        {
                            e.printStackTrace();
                        }

                        Connection.getClient().subscribe("test", 0, getApplicationContext(), new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                /*Intent intent = new Intent(getApplication(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);*/
                                stateoffMqtt = "true";
                                //Log.d("Sender", "MQTT Status after sub: " +stateoffMqtt);
                                sendMessage();
                                text = "Connected";
                                showNotification();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                //Toast.makeText(getApplicationContext(), R.string.sub_fail, Toast.LENGTH_SHORT).show();
                                Log.e("error",exception.toString());

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    //Toast.makeText(getApplicationContext(), R.string.conn_fail+exception.toString(), Toast.LENGTH_SHORT).show();
                    Log.e("mqtt",exception.toString());
                    stateoffMqtt = "false";
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
                case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                    Toast.makeText(getApplicationContext(), "Olmatix connect timed out", Toast.LENGTH_SHORT).show();
                case MqttException.REASON_CODE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_SHORT).show();
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

    private void sendMessage() {
        Intent intent = new Intent("MQTTStatus");
        intent.putExtra("MQTT State", stateoffMqtt);
        intent.putExtra("NotifyChangeNode", mChange);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageDetail() {
        Intent intent = new Intent("MQTTStatusDetail");
        intent.putExtra("NotifyChangeDetail", mChange);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public String getThread(){
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

    private class MqttEventCallback implements MqttCallback  {

        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, final  MqttMessage message) throws Exception {
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
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }

    private void addNode (){
        String[] outputDevices = TopicID.split("/");
        NodeID = outputDevices[1];
        String  mNodeIdSplit = mNodeID;
        mNodeIdSplit = mNodeIdSplit.substring(mNodeIdSplit.indexOf("$")+1,mNodeIdSplit.length());
        messageReceive.put(mNodeIdSplit,mMessage);
        checkValidation();
    }

    private void checkValidation() {
        //Log.d("messageReceive ", "= " + messageReceive);
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
            statusDevices();
        }
    }

    private void saveFirst() {

        if (dbNodeRepo.getNodeList().isEmpty()) {
            installedNodeModel.setNodesID(NodeID);
            installedNodeModel.setNodes(messageReceive.get("online"));
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            now.getTimeInMillis();
            installedNodeModel.setAdding(now.getTimeInMillis());

            dbNodeRepo.insertDb(installedNodeModel);
            Toast.makeText(getApplicationContext(), "Add Node Successfully", Toast.LENGTH_SHORT).show();
            Log.d("saveFirst", "Add Node success, ");
            messageReceive.clear();
            data.clear();
            doSubscribeIfOnline();
            mChange="1";
            sendMessage();

        } else {
            installedNodeModel.setNodesID(NodeID);
            if (dbNodeRepo.hasObject(installedNodeModel)) {
                    Toast.makeText(getApplicationContext(), "Checking this Node ID : " + NodeID + ", its exist, we are updating Node status", Toast.LENGTH_SHORT).show();
                Log.d("saveFirst", "You already have this Node, DB = " + NodeID+", Exist, we are updating Node status");
                saveDatabase();

            } else {
                installedNodeModel.setNodesID(NodeID);
                installedNodeModel.setNodes(messageReceive.get("online"));
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.getTimeInMillis();
                installedNodeModel.setAdding(now.getTimeInMillis());

                dbNodeRepo.insertDb(installedNodeModel);
                Toast.makeText(getApplicationContext(), "Successfully Add Node", Toast.LENGTH_SHORT).show();
                Log.d("saveFirst", "Add Node success, ");
                messageReceive.clear();
                data.clear();
                doSubscribeIfOnline();
                mChange="1";
                sendMessage();

            }
        }
    }

    private void printForegroundTask() {
        //String currentApp = "NULL";
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
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
            ActivityManager am = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = tasks.get(0).processName;
        }

        //Log.e(TAG, "Current App in foreground is: " + currentApp);
    }

    private void toastAndNotif(){

        int id = Integer.parseInt(NodeID.replaceAll("[\\D]", ""));
        int ch = Integer.parseInt(Channel.replaceAll("[\\D]", ""));
        int notid = id+ch;

        printForegroundTask();
        checkActivityForeground();
        Log.d(TAG, "toastAndNotif: "+flagSub);
        if (!currentApp.equals("com.olmatix.lesjaw.olmatix")) {
            if (!flagSub) {
                String state = "";
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel(Channel);
                data1.addAll(dbNodeRepo.getNodeDetail(NodeID, Channel));
                int countDB = dbNodeRepo.getNodeDetail(NodeID, Channel).size();
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
                Channel = "";
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

    private void updateSensorDoor(){

        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");
            detailNodeModel.setStatus_sensor(mMessage);
            Long currentDateTimeString = Calendar.getInstance().getTimeInMillis();
            detailNodeModel.setTimestamps(String.valueOf(currentDateTimeString));

            dbNodeRepo.update_detailSensor(detailNodeModel);
            mChange = "2";
            sendMessageDetail();
        }
    }

    private void updateSensorTheft() {
        if (!mNodeID.contains("light")) {
            detailNodeModel.setNode_id(NodeIDSensor);
            detailNodeModel.setChannel("0");
            detailNodeModel.setStatus_theft(mMessage);
            Long currentDateTimeString = Calendar.getInstance().getTimeInMillis();
            detailNodeModel.setTimestamps(String.valueOf(currentDateTimeString));

            dbNodeRepo.update_detailSensor(detailNodeModel);
            mChange = "2";
            sendMessageDetail();
            //Log.d("DEBUG", "updateSensorTheft: "+mMessage);

            if (mMessage.equals("true")) {
                titleNode = mNiceName;
                textNode = "ALARM!!";
                showNotificationNode();
            }
        }
    }

    private void updateDetail(){
        String[] outputDevices = TopicID.split("/");
        NodeID = outputDevices[1];
        Channel = outputDevices[3];
        message_topic.put(Channel, mMessage);
        saveDatabase_Detail();
        toastAndNotif();
    }

    private  void addNodeDetail() {

        if(installedNodeModel.getFwName() != null) {

            if (installedNodeModel.getFwName().equals("smartfitting")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (dbNodeRepo.hasDetailObject(detailNodeModel)) {
                    saveDatabase_Detail();
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setNice_name_d(NodeID);

                    dbNodeRepo.insertInstalledNode(detailNodeModel);

                }

            } else if (installedNodeModel.getFwName().equals("smartadapter4ch")){
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (dbNodeRepo.hasDetailObject(detailNodeModel)) {
                    saveDatabase_Detail();
                    }else {
                    for (int i = 0; i < 4; i++) {
                        String a = String.valueOf(i);

                        detailNodeModel.setNode_id(NodeID);
                        detailNodeModel.setChannel(String.valueOf(i));
                        detailNodeModel.setStatus("false");
                        detailNodeModel.setNice_name_d(NodeID +" Ch "+String.valueOf(i+1));

                        dbNodeRepo.insertInstalledNode(detailNodeModel);
                    }
                }
            }else if (installedNodeModel.getFwName().equals("smartsensordoor")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (dbNodeRepo.hasDetailObject(detailNodeModel)) {
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setSensor("close");
                    detailNodeModel.setStatus("false");
                    detailNodeModel.setStatus_sensor("false");
                    detailNodeModel.setStatus_theft("false");
                    detailNodeModel.setNice_name_d(NodeID);

                    dbNodeRepo.insertInstalledNode(detailNodeModel);

                }
            }
        }

    }

    private void statusDevices(){
        installedNodeModel.setNodesID(NodeID);
        if (dbNodeRepo.hasObject(installedNodeModel)) {

        }
        messageReceive.clear();
        data.clear();
        mChange = "2";
        sendMessage();
    }

    private void saveDatabase() {

                    installedNodeModel.setNodesID(NodeID);
                    installedNodeModel.setNodes(messageReceive.get("nodes"));
                    installedNodeModel.setName(messageReceive.get("name"));
                    installedNodeModel.setLocalip(messageReceive.get("localip"));
                    installedNodeModel.setFwName(messageReceive.get("fwname"));
                    Log.d(TAG, "saveDatabase: "+messageReceive.get("fwname"));
                    installedNodeModel.setFwVersion(messageReceive.get("fwversion"));
                    if(installedNodeModel.getFwName() != null) {
                        addNodeDetail();
                    }
                    installedNodeModel.setOnline(messageReceive.get("online"));
                    if (messageReceive.containsKey("online")) {
                        checkActivityForeground();
                        printForegroundTask();
                        if (!currentApp.equals("com.olmatix.lesjaw.olmatix")) {
                            if (!flagSub) {
                                installedNodeModel.setNodesID(NodeID);
                                data2.addAll(dbNodeRepo.getNodeListbyNode(NodeID));
                                int countDB = dbNodeRepo.getNodeListbyNode(NodeID).size();
                                if (countDB != 0) {
                                    for (int i = 0; i < countDB; i++) {
                                        if (data2.get(i).getNice_name_n() != null) {
                                            mNiceNameN = data2.get(i).getNice_name_n();
                                        } else {
                                            mNiceNameN = data2.get(i).getFwName();
                                        }
                                        int id = Integer.parseInt(NodeID.replaceAll("[\\D]", ""));

                                        notifyID = id+2;

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
                    if(messageReceive.containsKey("uptime")) {
                        if (mMessage != null) {
                            installedNodeModel.setOnline("true");
                        }
                    }
                    Calendar now = Calendar.getInstance();
                    now.setTime(new Date());
                    now.getTimeInMillis();
                    //System.out.println("data " + now.getTimeInMillis());
                    installedNodeModel.setAdding(now.getTimeInMillis());

        dbNodeRepo.update(installedNodeModel);
        messageReceive.clear();
        data.clear();
        mChange="2";
                sendMessage();

    }

    private void saveDatabase_Detail() {

        if (!mNodeID.contains("door")) {
            detailNodeModel.setNode_id(NodeID);
            detailNodeModel.setChannel(Channel);
            if (mMessage.equals("ON")) {
                mMessage = "true";
                detailNodeModel.setStatus(mMessage);
            } else if (mMessage.equals("OFF")) {
                mMessage = "false";
                detailNodeModel.setStatus(mMessage);
            } else {
                detailNodeModel.setStatus(mMessage);
            }
            Long currentDateTimeString = Calendar.getInstance().getTimeInMillis();
            detailNodeModel.setTimestamps(String.valueOf(currentDateTimeString));

            dbNodeRepo.update_detail(detailNodeModel);
            mChange = "2";
            sendMessageDetail();
        }
    }

    private void doAddNodeSub(){
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

    private void doSubscribeIfOnline(){
        String topic = "devices/" + NodeID + "/#";
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

    }

    private void doUnsubscribe(){
        String topic = "devices/"+NodeID+"/$online";
        try {
            Connection.getClient().unsubscribe(topic);
            Log.d("Unsubscribe", " device = " + NodeID);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void doSubAll() {
        if (Connection.getClient().isConnected()) {
            int countDB = dbNodeRepo.getNodeList().size();
            Log.d("DEBUG", "Count list: " + countDB);
            data.addAll(dbNodeRepo.getNodeList());
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    final String mNodeID = data.get(i).getNodesID();
                    Log.d("DEBUG", "Count list: " + mNodeID);
                    for (int a=0; a < 6 ;a++){
                        if (a==0){topic = "devices/" + mNodeID + "/$online";}
                        if (a==1){topic = "devices/" + mNodeID + "/$fwname";}
                        if (a==2){topic = "devices/" + mNodeID + "/$signal";}
                        if (a==3){topic = "devices/" + mNodeID + "/$uptime";}
                        if (a==4){topic = "devices/" + mNodeID + "/$name";}
                        if (a==5){topic = "devices/" + mNodeID + "/$localip";}
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
                    //Log.d(TAG, "doSubAll: 1");

                }
                data.clear();
                doSubAllDetail();
                //Log.d(TAG, "doSubAll: 2");
            }
            //Log.d(TAG, "doSubAll: 3");

        }
        //Log.d(TAG, "doSubAll: 4");

    }

    private void doSubAllDetail() {
        if (Connection.getClient().isConnected()) {
            int countDB = dbNodeRepo.getNodeDetailList().size();
            Log.d("DEBUG", "Count list Detail: " + countDB);
            data1.addAll(dbNodeRepo.getNodeDetailList());
            countDB = dbNodeRepo.getNodeDetailList().size();
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
                }
            }
        data1.clear();
        doAllsubDetailSensor();

    }

    private void doAllsubDetailSensor() {
        if (Connection.getClient().isConnected()) {
            int countDB = dbNodeRepo.getNodeDetailList().size();
            Log.d("DEBUG", "Count list Sensor: " + countDB);
            data1.addAll(dbNodeRepo.getNodeDetailList());
            countDB = dbNodeRepo.getNodeDetailList().size();
            if (countDB != 0) {
                for (int i = 0; i < countDB; i++) {
                    final String mNodeID1 = data1.get(i).getNode_id();
                    final String mSensorT = data1.get(i).getSensor();
                    Log.d("DEBUG", "Count list Sensor: " + mSensorT);
                    if (mSensorT != null&&mSensorT.equals("close")) {
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
            }

        }
        data1.clear();
    }

}






