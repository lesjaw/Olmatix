package com.olmatix.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.olmatix.database.dbNodeRepo;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.model.Detail_NodeModel;
import com.olmatix.model.Installed_NodeModel;
import com.olmatix.ui.activity.MainActivity;
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
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


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
    HashMap<String,String>  checkDollar = new HashMap<>();
    HashMap<String,String>  messageReceive = new HashMap<>();
    HashMap<String,String> message_topic = new HashMap<>();
    private String mNodeID;
    private String TopicID;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */

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
                Log.d("Sender", "MQTT Status No Internet: " +stateoffMqtt);
                sendMessage();
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
        dbNodeRepo = new dbNodeRepo(getApplicationContext());
        installedNodeModel = new Installed_NodeModel();
        detailNodeModel = new Detail_NodeModel();
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged()");
        android.os.Debug.waitForDebugger();
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
        Toast.makeText(getApplicationContext(), R.string.connecting, Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mServerURL = sharedPref.getString("server_address", "cloud.olmatix.com");
        String mServerPort = sharedPref.getString("server_port", "1883");
        String mUserName = sharedPref.getString("user_name", "olmatix1");
        String mPassword = sharedPref.getString("password", "olmatix");

        final MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mUserName);
        options.setPassword(mPassword.toCharArray());
        final MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(),"tcp://"+mServerURL+":"+mServerPort,deviceId, new MemoryPersistence());
        options.setCleanSession(false);
        String topic = "status/"+deviceId+"/$online";
        byte[] payload = "false".getBytes();
        options.setWill(topic, payload ,1,true);
        options.setKeepAliveInterval(300);
        Connection.setClient(client);
        try {

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(getApplicationContext(),  R.string.conn_success, Toast.LENGTH_SHORT).show();
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
                                //Log.i("sub","Subscribe success");
                                //Toast.makeText(getApplicationContext(), R.string.sub_success, Toast.LENGTH_SHORT).show();
                                stateoffMqtt = "true";
                                Log.d("Sender", "MQTT Status after sub: " +stateoffMqtt);
                                sendMessage();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Toast.makeText(getApplicationContext(), R.string.sub_fail, Toast.LENGTH_SHORT).show();
                                Log.e("error",exception.toString());

                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), R.string.conn_fail+exception.toString(), Toast.LENGTH_SHORT).show();
                    Log.e("mqtt",exception.toString());
                    stateoffMqtt = "false";
                    sendMessage();
                }
            });

        } catch (MqttSecurityException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            switch (e.getReasonCode()) {
                case MqttException.REASON_CODE_BROKER_UNAVAILABLE:
                case MqttException.REASON_CODE_CLIENT_TIMEOUT:
                case MqttException.REASON_CODE_CONNECTION_LOST:
                case MqttException.REASON_CODE_SERVER_CONNECT_ERROR:
                    Log.v(TAG, "c" + e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    break;
                case MqttException.REASON_CODE_FAILED_AUTHENTICATION:
                    Intent i = new Intent("RAISEALLARM");
                    i.putExtra("ALLARM", e);
                    Log.e(TAG, "b" + e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(TAG, "a" + e.getMessage());
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand()");
        Toast.makeText(getApplicationContext(), R.string.service_start, Toast.LENGTH_SHORT).show();
        Log.d("Service = ", "Starting..");

        sendMessage();
        return START_STICKY;
    }

    private void sendMessage() {
        //Log.d("sender", "Broadcasting message MQTT status = " +stateoffMqtt);
        Intent intent = new Intent("MQTTStatus");
        // You can also include some extra data.
        intent.putExtra("MQTT State", stateoffMqtt);
        intent.putExtra("NotifyChange", "true");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public String getThread(){
        return Long.valueOf(thread.getId()).toString();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        // Tell the user we stopped.
        super.onDestroy();
        Toast.makeText(this, R.string.service_stop, Toast.LENGTH_SHORT).show();
    }

    @Nullable
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
            Log.d("MQTTMessage", " = " + topic + " message = " + message.toString());
            TopicID = topic;
            mNodeID = topic;
            mMessage = message.toString();

            if (mNodeID.contains("$")) {
                addNode();
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

    private void updateDetail(){
        String[] outputDevices = TopicID.split("/");
        NodeID = outputDevices[1];
        Channel = outputDevices[3];
        message_topic.put(Channel, mMessage);
        saveDatabase_Detail();

    }

    private void checkValidation() {
        Log.d("messageReceive ", "= " + messageReceive);
        if (messageReceive.containsKey("online")) {
            Log.d("CheckValid online", "Passed");
            if (mMessage.equals("true")){
                Log.d("CheckValid online", " true Passed");
                saveFirst();
            } else {
                Toast.makeText(getApplicationContext(), R.string.deviceoffline, Toast.LENGTH_LONG).show();
                installedNodeModel.setNodesID(NodeID);
                if (dbNodeRepo.hasObject(installedNodeModel)) {

                    Toast.makeText(getApplicationContext(), "Updating status device : " + NodeID, Toast.LENGTH_LONG).show();
                    Log.d("Updating", "status device = " + NodeID);
                    statusDevices();

                } else {
                    doUnsubscribe();
                }
            }
        }
        saveDatabase();
    }

    private void saveFirst() {

            if (dbNodeRepo.getNodeList().isEmpty()) {
                installedNodeModel.setNodesID(NodeID);
                installedNodeModel.setNodes(messageReceive.get("online"));
                Long currentDateTimeString = Calendar.getInstance().getTimeInMillis();
                installedNodeModel.setAdding(String.valueOf(currentDateTimeString));

                dbNodeRepo.insertDb(installedNodeModel);
                Toast.makeText(getApplicationContext(), "Add Node Successfully", Toast.LENGTH_LONG).show();
                Log.d("saveFirst", "Add Node success, ");
                messageReceive.clear();
                doSubscribeIfOnline();

            } else {
                installedNodeModel.setNodesID(NodeID);
                if (dbNodeRepo.hasObject(installedNodeModel)) {

                    Toast.makeText(getApplicationContext(), "You already have this Node ID : " + NodeID +", updating Node status", Toast.LENGTH_LONG).show();
                    //flagExist = 1;
                    Log.d("saveFirst", "You already have this Node, DB = " + NodeID+", updating Node status");
                    saveDatabase();

                } else {
                    installedNodeModel.setNodesID(NodeID);
                    installedNodeModel.setNodes(messageReceive.get("online"));
                    Long currentDateTimeString = Calendar.getInstance().getTimeInMillis();
                    installedNodeModel.setAdding(String.valueOf(currentDateTimeString));

                    dbNodeRepo.insertDb(installedNodeModel);
                    Toast.makeText(getApplicationContext(), "Add Node Successfully", Toast.LENGTH_LONG).show();
                    Log.d("saveFirst", "Add Node success, ");
                    messageReceive.clear();
                    doSubscribeIfOnline();
                }
            }
    }
    private  void addNodeDetail() {
        Log.d("messageReceiveDetail ", "= " + message_topic);

        if(installedNodeModel.getFwName() != null) {
            Log.d("addNodeDetail", "fwname, "+installedNodeModel.getFwName());

            if (installedNodeModel.getFwName().equals("smartfitting")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                Log.d("addNodeDetail", "NodeID, "+NodeID + ", channel, "+Channel);

                if (dbNodeRepo.hasDetailObject(detailNodeModel)) {
                    saveDatabase_Detail();
                } else {
                    detailNodeModel.setNode_id(NodeID);
                    detailNodeModel.setChannel("0");
                    detailNodeModel.setStatus("false");

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

                        dbNodeRepo.insertInstalledNode(detailNodeModel);
                    }
                }
            }else if (installedNodeModel.getFwName().equals("smartsensordoor")) {
                detailNodeModel.setNode_id(NodeID);
                detailNodeModel.setChannel("0");
                if (dbNodeRepo.hasDetailObject(detailNodeModel)) {
                    saveDatabase_Detail();
                } else {
                    for (int i = 0; i < 2; i++) {
                        String a = "";
                        if (i==0) {
                            a = "0";
                        }else if (i==1){
                            a = "door";
                        }
                            detailNodeModel.setNode_id(NodeID);
                            detailNodeModel.setChannel(a);
                            detailNodeModel.setStatus("false");

                            dbNodeRepo.insertInstalledNode(detailNodeModel);

                    }
                }
            }
        }

    }

    private void statusDevices(){
        installedNodeModel.setNodesID(NodeID);
        if (dbNodeRepo.hasObject(installedNodeModel)) {
            if (messageReceive.get("online") != null) {
                installedNodeModel.setOnline(messageReceive.get("online"));
            }
            dbNodeRepo.update(installedNodeModel);
            messageReceive.clear();
        }

    }

    private void saveDatabase() {

                    installedNodeModel.setNodesID(NodeID);
                    installedNodeModel.setNodes(messageReceive.get("nodes"));
                    installedNodeModel.setName(messageReceive.get("name"));
                    installedNodeModel.setLocalip(messageReceive.get("localip"));
                    installedNodeModel.setFwName(messageReceive.get("fwname"));
                    installedNodeModel.setFwVersion(messageReceive.get("fwversion"));
                    if(installedNodeModel.getFwName() != null) {
                        addNodeDetail();
                    }
                    installedNodeModel.setOnline(messageReceive.get("online"));
                    installedNodeModel.setSignal(messageReceive.get("signal"));
                    installedNodeModel.setUptime(messageReceive.get("uptime"));
                    Long currentDateTimeString = Calendar.getInstance().getTimeInMillis();
                    installedNodeModel.setAdding(String.valueOf(currentDateTimeString));

                dbNodeRepo.update(installedNodeModel);
                messageReceive.clear();
                sendMessage();

    }

    private void saveDatabase_Detail() {
        String mStatus;

        detailNodeModel.setNode_id(NodeID);
        detailNodeModel.setChannel(Channel);
/*
        if (mMessage.equals("true")) {
            mStatus = "ON";
        } else mStatus = "OFF";
*/

        detailNodeModel.setStatus(mMessage);

        dbNodeRepo.update_detail(detailNodeModel);
        message_topic.clear();
        Channel = "";
        sendMessage();

    }
    private void doSubscribeIfOnline(){
        String topic = "devices/" + NodeID + "/#";
        int qos = 1;
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

    private void doSubscribeStatusIfOnline(){
        String topic = "devices/" + NodeID + "/light/"+Channel+"/set";
        int qos = 1;
        try {
            IMqttToken subToken = Connection.getClient().subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("Subscribe", " status device = " + NodeID);
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
}






