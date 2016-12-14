package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

public class dbNode {

    // Labels table name
    public static final String TABLE = "Node";
    public static final String TABLE_NODE = "Node_Installed";

    // Labels Table Columns names
    public static final String KEY_ID           = "id";
    public static final String KEY_NODE_ID      = "node_id";
    public static final String KEY_NODES        = "nodes";
    public static final String KEY_NAME         = "name";
    public static final String KEY_NICE_NAME_N    = "nice_name_n"; //this name will be from user input
    public static final String KEY_LOCALIP      = "localip";
    public static final String KEY_FWNAME       = "fwname";
    public static final String KEY_FWVERSION    = "fwversion";
    public static final String KEY_ONLINE       = "online";
    public static final String KEY_SIGNAL       = "signal";
    public static final String KEY_ICON         = "icon";
    public static final String KEY_ADDING       = "adding"; //this is the timestamps when the node added
    public static final String KEY_UPTIME       = "uptime";
    public static final String KEY_RESET        = "reset";
    public static final String KEY_OTA          = "ota";
    public static final String KEY_STATUS       = "status";
    public static final String KEY_CHANNEL      = "channel";
    public static final String KEY_NICE_NAME_D    = "nice_name_d"; //this name will be from user input


    // property help us to keep data
    public int id;
    public String node_id;
    public String nodes;
    public String name;
    public String nice_name_n;
    public String nice_name_d;
    public String localip;
    public String fwname;
    public String fwversion;
    public String online;
    public String signal;
    public String icon;
    public String adding;
    public String uptime;
    public String reset;
    public String ota;

}
