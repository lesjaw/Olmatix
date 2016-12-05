package com.olmatix.utils;

import org.eclipse.paho.android.service.MqttAndroidClient;

/**
 * Created by Lesjaw on 02/12/2016.
 */

public class Connection {
    private static MqttAndroidClient client;

    public static void setClient(MqttAndroidClient cl){
        client = cl;
    }
    public static MqttAndroidClient getClient(){
        return client;
    }


}
