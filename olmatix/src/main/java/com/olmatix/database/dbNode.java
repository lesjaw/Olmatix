package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

public class dbNode {

    // Labels table name
    public static final String TABLE = "Node";
    public static final String TABLE_NODE = "Node_Installed";
    public static final String TABLE_FAV = "Node_Favorite";
    public static final String TABLE_MQTT = "mqtt";



    // Labels Table Columns names
    public static final String KEY_ID           = "id";
    public static final String KEY_NODE_ID      = "node_id";
    public static final String KEY_NODES        = "nodes";
    public static final String KEY_NAME         = "name";
    public static final String KEY_LOCALIP      = "localip";
    public static final String KEY_FWNAME       = "fwname";
    public static final String KEY_FWVERSION    = "fwversion";
    public static final String KEY_ONLINE       = "online";
    public static final String KEY_SIGNAL       = "signal";
    public static final String KEY_ICON         = "icon";
    public static final String KEY_ADDING       = "adding";
    public static final String KEY_UPTIME       = "uptime";
    public static final String KEY_RESET        = "reset";
    public static final String KEY_OTA          = "ota";
    public static final String KEY_STATUS       = "status";
    public static final String KEY_CHANNEL      = "channel";
    public static final String KEY_NICE_NAME_D  = "nice_name_d" ;
    public static final String KEY_NICE_NAME_N  = "nice_name_n" ;
    public static final String KEY_SENSOR       = "sensor" ;
    public static final String KEY_STATUS_SENSOR  = "status_sensor" ;
    public static final String KEY_STATUS_THEFT  = "status_theft" ;
    public static final String KEY_ONDURATION  = "onduration" ;
    public static final String KEY_TOPIC  = "topic" ;
    public static final String KEY_MESSAGE  = "message" ;



    // property help us to keep data
    public int id;
    public String topic;
    public String message;
    public String name;
    public String icon;
    public String uptime;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
