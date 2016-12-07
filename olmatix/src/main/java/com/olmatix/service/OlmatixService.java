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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.olmatix.utils.Connection;
import com.olmatix.lesjaw.olmatix.R;
import com.olmatix.ui.activity.MainActivity;

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

/**
 * Created              : Rahman on 12/2/2016.
 * Date Created         : 12/2/2016 / 4:29 PM.
 * ===================================================
 * Package              : com.olmatix.service.
 * Project Name         : Olmatix.
 * Copyright            : Copyright @ 2016 OLMATIX.
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

    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    private String messageMQQT;
    private String topicMQQT;
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
                Log.d("DEBUG", "MQTT Status No Internet: " +stateoffMqtt);
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
        String mUserName = sharedPref.getString("user_name", "olmatix");
        Log.d(TAG, "doConnect: " + mUserName);
        String mPassword = sharedPref.getString("password", "olmatix");

        final MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("olmatix");
        options.setPassword("olmatix".toCharArray());
        final MqttAndroidClient client = new MqttAndroidClient(getApplicationContext(),"tcp://"+mServerURL+":"+mServerPort,deviceId, new MemoryPersistence());
        options.setCleanSession(false);
        String topic = "status/"+deviceId+"/$online";
        byte[] payload = "false".getBytes();
        options.setWill(topic, payload ,1,true);
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
                            Connection.getClient().publish(topic, message);
                        } catch (UnsupportedEncodingException | MqttException e) {
                            e.printStackTrace();
                        }

                        Connection.getClient().subscribe("test", 0, getApplicationContext(), new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {

                                Log.i("sub","Subscribe success");
                                Toast.makeText(getApplicationContext(), R.string.sub_success, Toast.LENGTH_SHORT).show();
                                stateoffMqtt = "true";
                                Log.d("DEBUG", "MQTT Status after sub: " +stateoffMqtt);
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

        sendMessage();

        return START_STICKY;
    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message MQTT status = " +stateoffMqtt);
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("MQTT State", stateoffMqtt);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessageHandle() {
        Log.d("sender", "topic = " +topicMQQT + "message = "+ messageMQQT);
        Intent intent = new Intent("info-device");
        // You can also include some extra data.

        intent.putExtra("device", topicMQQT);
        intent.putExtra("message", messageMQQT);
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
            Toast.makeText(getApplicationContext(), "Message arrived -> "+topic+" : "+message.toString(), Toast.LENGTH_SHORT).show();
            topicMQQT = topic;
            messageMQQT = message.toString();
            sendMessageHandle();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }

    }
}
