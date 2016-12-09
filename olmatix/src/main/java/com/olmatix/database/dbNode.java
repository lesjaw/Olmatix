package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

public class dbNode {

    // Labels table name
    public static final String TABLE = "Node";

    // Labels Table Columns names
    public static final String KEY_ID           = "node_id";
    public static final String KEY_NODE_ID      = "key_node_id";
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

    // property help us to keep data
    public int node_id;
    public String nodes;
    public String name;
    public String localip;
    public String fwname;
    public String fwversion;
    public String online;
    public String icon;
    public String adding;
    public String signal;
    public String uptime;
    public String reset;
    public String ota;
    public String key_node_id;

}
