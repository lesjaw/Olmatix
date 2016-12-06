package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

public class dbNode {

    // Labels table name
    public static final String TABLE = "Node";

    // Labels Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_node = "node";
    public static final String KEY_fwname = "fwname";
    public static final String KEY_version = "version";

    // property help us to keep data
    public int node_ID;
    public String node;
    public String fwname;
    public int version;
}
