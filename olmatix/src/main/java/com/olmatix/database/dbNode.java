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
    public static final String TABLE_NODE_DURATION = "Node_Duration";
    public static final String TABLE_SCENE = "scene";
    public static final String TABLE_SCENE_DETAIL = "scene_detail";
    public static final String TABLE_INFO          = "info_data";
    public static final String TABLE_LOG          = "log_alarm";
    public static final String TABLE_GROUP          = "group_dash";
    public static final String TABLE_LOC          = "location";

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
    public static final String KEY_STATUS_TEMP  = "status_temp" ;
    public static final String KEY_STATUS_HUM  = "status_hum" ;
    public static final String KEY_STATUS_JARAK  = "status_jarak" ;
    public static final String KEY_STATUS_RANGE  = "status_range" ;
    public static final String KEY_GROUP_NAME  = "group_name" ;
    public static final String KEY_GROUP_ID  = "group_id" ;
    public static final String KEY_LOC  = "latlong" ;

    public static final String KEY_TOPIC  = "topic" ;
    public static final String KEY_MESSAGE  = "message" ;
    public static final String KEY_TIMESTAMPS_ON  = "timestamp_on" ;
    public static final String KEY_TIMESTAMPS_OFF  = "timestamp_off" ;
    public static final String KEY_DURATION  = "duration" ;
    public static final String KEY_SCENE_NAME  = "scene_name" ;
    public static final String KEY_SCENE_ID  = "sceneid" ;
    public static final String KEY_SCENE_TYPE  = "scene_type" ;
    public static final String KEY_INFO_TYPE  = "info_type" ;
    public static final String KEY_PATH     = "path";
    public static final String KEY_COMMAND     = "command";
    public static final String KEY_LOG     = "log";
    public static final String KEY_ID_NODE_DETAIL = "id_node_detail";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_HOURS = "hours";
    public static final String KEY_MINS = "minute";
    public static final String KEY_SUN = "sunday";
    public static final String KEY_MON = "monday";
    public static final String KEY_TUE = "tuesday";
    public static final String KEY_WED = "wednesday";
    public static final String KEY_THUR = "thursday";
    public static final String KEY_FRI = "friday";
    public static final String KEY_SAT = "saturday";


    // property help us to keep data
    public int id;
    public String topic;
    public String message, log, node_id, channel;

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

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getNode_id() {
        return node_id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
