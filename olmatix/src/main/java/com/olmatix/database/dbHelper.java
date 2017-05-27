package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 23;

    // Database Name
    private static final String DATABASE_NAME = "olmatix";

    public dbHelper(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here

        String CREATE_TABLE_NODE = "CREATE TABLE " + dbNode.TABLE  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_NODE_ID + " TEXT, "
                + dbNode.KEY_NODES + " TEXT, "
                + dbNode.KEY_NAME + " TEXT, "
                + dbNode.KEY_NICE_NAME_N + " TEXT, "
                + dbNode.KEY_LOCALIP + " TEXT, "
                + dbNode.KEY_FWNAME + " TEXT, "
                + dbNode.KEY_FWVERSION + " TEXT, "
                + dbNode.KEY_ONLINE + " TEXT, "
                + dbNode.KEY_ICON + " TEXT, "
                + dbNode.KEY_SIGNAL + " TEXT, "
                + dbNode.KEY_UPTIME + " TEXT, "
                + dbNode.KEY_RESET + " TEXT, "
                + dbNode.KEY_OTA + " TEXT, "
                + dbNode.KEY_ADDING + " LONG )";


        String CREATE_TABLE_NODE_INSTALLED = "CREATE TABLE " + dbNode.TABLE_NODE  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_NODE_ID + " TEXT, "
                + dbNode.KEY_CHANNEL + " TEXT, "
                + dbNode.KEY_NICE_NAME_D + " TEXT, "
                + dbNode.KEY_STATUS + " TEXT, "
                + dbNode.KEY_STATUS_SENSOR + " TEXT, "
                + dbNode.KEY_STATUS_THEFT + " TEXT, "
                + dbNode.KEY_STATUS_TEMP + " TEXT, "
                + dbNode.KEY_STATUS_HUM + " TEXT, "
                + dbNode.KEY_STATUS_JARAK + " TEXT, "
                + dbNode.KEY_STATUS_RANGE + " TEXT, "
                + dbNode.KEY_SENSOR + " TEXT )";

        String CREATE_TABLE_FAVORITE = "CREATE TABLE " + dbNode.TABLE_FAV  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_NODE_ID + " TEXT, "
                + dbNode.KEY_GROUP_ID + " TEXT, "
                + dbNode.KEY_ID_NODE_DETAIL + " TEXT) ";

        String CREATE_TABLE_MQTT = "CREATE TABLE " + dbNode.TABLE_MQTT  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_TOPIC + " TEXT, "
                + dbNode.KEY_MESSAGE + " TEXT)";

        String CREATE_TABLE_NODE_DURATION = "CREATE TABLE " + dbNode.TABLE_NODE_DURATION  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_NODE_ID + " TEXT, "
                + dbNode.KEY_CHANNEL + " TEXT, "
                + dbNode.KEY_STATUS + " TEXT, "
                + dbNode.KEY_TIMESTAMPS_ON + " TEXT, "
                + dbNode.KEY_TIMESTAMPS_OFF + " TEXT, "
                + dbNode.KEY_DURATION + " TEXT) ";

        String CREATE_TABLE_SCENE = "CREATE TABLE " + dbNode.TABLE_SCENE  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_SCENE_NAME + " TEXT, "
                + dbNode.KEY_SCENE_TYPE + " TEXT, "
                + dbNode.KEY_SENSOR + " TEXT) ";

/*
        String CREATE_TABLE_SCENE = "CREATE TABLE " + dbNode.TABLE_SCENE  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_SCENE_NAME + " TEXT, "
                + dbNode.KEY_SCENE_TYPE + " TEXT, "
                + dbNode.KEY_HOURS + " TEXT, "
                + dbNode.KEY_MINS + " TEXT, "
                + dbNode.KEY_SUN + " TEXT, "
                + dbNode.KEY_MON + " TEXT, "
                + dbNode.KEY_TUE + " TEXT, "
                + dbNode.KEY_WED + " TEXT, "
                + dbNode.KEY_THUR + " TEXT, "
                + dbNode.KEY_FRI + " TEXT, "
                + dbNode.KEY_SAT + " TEXT, "
                + dbNode.KEY_LOCATION + " TEXT, "
                + dbNode.KEY_SENSOR + " TEXT) ";
*/

        String CREATE_TABLE_SCENE_DETAIL = "CREATE TABLE " + dbNode.TABLE_SCENE_DETAIL  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_SCENE_NAME + " TEXT, "
                + dbNode.KEY_SCENE_TYPE + " TEXT, "
                + dbNode.KEY_PATH + " TEXT,"
                + dbNode.KEY_SCENE_ID + " TEXT,"
                + dbNode.KEY_HOURS + " TEXT, "
                + dbNode.KEY_MINS + " TEXT, "
                + dbNode.KEY_SUN + " TEXT, "
                + dbNode.KEY_MON + " TEXT, "
                + dbNode.KEY_TUE + " TEXT, "
                + dbNode.KEY_WED + " TEXT, "
                + dbNode.KEY_THUR + " TEXT, "
                + dbNode.KEY_FRI + " TEXT, "
                + dbNode.KEY_SAT + " TEXT, "
                + dbNode.KEY_NODE_ID + " TEXT, "
                + dbNode.KEY_LOCATION + " TEXT, "
                + dbNode.KEY_COMMAND + " TEXT) ";

        String CREATE_TABLE_INFO = "CREATE TABLE " + dbNode.TABLE_INFO  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_INFO_TYPE + " TEXT, "
                + dbNode.KEY_SCENE_ID + " TEXT,"
                + dbNode.KEY_SCENE_TYPE + " TEXT) ";

        String CREATE_TABLE_LOG = "CREATE TABLE " + dbNode.TABLE_LOG  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_LOG + " TEXT)";

        String CREATE_TABLE_GROUP = "CREATE TABLE " + dbNode.TABLE_GROUP  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_GROUP_NAME + " TEXT)";


        db.execSQL(CREATE_TABLE_NODE);
        db.execSQL(CREATE_TABLE_NODE_INSTALLED);
        db.execSQL(CREATE_TABLE_FAVORITE);
        db.execSQL(CREATE_TABLE_MQTT);
        db.execSQL(CREATE_TABLE_NODE_DURATION);
        db.execSQL(CREATE_TABLE_SCENE);
        db.execSQL(CREATE_TABLE_SCENE_DETAIL);
        db.execSQL(CREATE_TABLE_INFO);
        db.execSQL(CREATE_TABLE_LOG);
        db.execSQL(CREATE_TABLE_GROUP);

    }

    //FAVORITE = DASHBOARD
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_NODE);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_FAV);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_MQTT);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_NODE_DURATION);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_SCENE);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_SCENE_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_INFO);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_GROUP);

        // Create tables again
        onCreate(db);

    }
}
