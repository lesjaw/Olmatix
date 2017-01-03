package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 15;

    // Database Name
    private static final String DATABASE_NAME = "olmatix";

    public DbHelper(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here

        String CREATE_TABLE_NODE = "CREATE TABLE " + DbNode.TABLE  + "("
                + DbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbNode.KEY_NODE_ID + " TEXT, "
                + DbNode.KEY_NODES + " TEXT, "
                + DbNode.KEY_NAME + " TEXT, "
                + DbNode.KEY_NICE_NAME_N + " TEXT, "
                + DbNode.KEY_LOCALIP + " TEXT, "
                + DbNode.KEY_FWNAME + " TEXT, "
                + DbNode.KEY_FWVERSION + " TEXT, "
                + DbNode.KEY_ONLINE + " TEXT, "
                + DbNode.KEY_ICON + " TEXT, "
                + DbNode.KEY_SIGNAL + " TEXT, "
                + DbNode.KEY_UPTIME + " TEXT, "
                + DbNode.KEY_RESET + " TEXT, "
                + DbNode.KEY_OTA + " TEXT, "
                + DbNode.KEY_ADDING + " LONG )";


        String CREATE_TABLE_NODE_INSTALLED = "CREATE TABLE " + DbNode.TABLE_NODE  + "("
                + DbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbNode.KEY_NODE_ID + " TEXT, "
                + DbNode.KEY_CHANNEL + " TEXT, "
                + DbNode.KEY_NICE_NAME_D + " TEXT, "
                + DbNode.KEY_STATUS + " TEXT, "
                + DbNode.KEY_STATUS_SENSOR + " TEXT, "
                + DbNode.KEY_STATUS_THEFT + " TEXT, "
                + DbNode.KEY_SENSOR + " TEXT )";

        String CREATE_TABLE_FAVORITE = "CREATE TABLE " + DbNode.TABLE_FAV  + "("
                + DbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbNode.KEY_NICE_NAME_D + " TEXT) ";

        String CREATE_TABLE_MQTT = "CREATE TABLE " + DbNode.TABLE_MQTT  + "("
                + DbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbNode.KEY_TOPIC + " TEXT, "
                + DbNode.KEY_MESSAGE + " TEXT)";

        String CREATE_TABLE_NODE_DURATION = "CREATE TABLE " + DbNode.TABLE_NODE_DURATION  + "("
                + DbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbNode.KEY_NODE_ID + " TEXT, "
                + DbNode.KEY_CHANNEL + " TEXT, "
                + DbNode.KEY_STATUS + " TEXT, "
                + DbNode.KEY_TIMESTAMPS_ON + " TEXT, "
                + DbNode.KEY_TIMESTAMPS_OFF + " TEXT, "
                + DbNode.KEY_DURATION + " TEXT) ";

        String CREATE_TABLE_SCENE = "CREATE TABLE " + DbNode.TABLE_SCENE  + "("
                + DbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbNode.KEY_SCENE_NAME + " TEXT, "
                + DbNode.KEY_SCENE_TYPE + " TEXT, "
                + DbNode.KEY_SCHEDULE + " TEXT, "
                + DbNode.KEY_ARRIVE + " TEXT, "
                + DbNode.KEY_LEAVE + " TEXT) ";

        String CREATE_TABLE_SCENE_DETAIL = "CREATE TABLE " + DbNode.TABLE_SCENE_DETAIL  + "("
                + DbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbNode.KEY_SCENE_ID + " TEXT,"
                + DbNode.KEY_PATH + " TEXT,"
                + DbNode.KEY_COMMAND + " TEXT) ";

        String CREATE_TABLE_INFO = "CREATE TABLE " + DbNode.TABLE_INFO  + "("
                + DbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + DbNode.KEY_INFO_TYPE + " TEXT, "
                + DbNode.KEY_SCENE_ID + " TEXT,"
                + DbNode.KEY_SCENE_TYPE + " TEXT) ";

        db.execSQL(CREATE_TABLE_NODE);
        db.execSQL(CREATE_TABLE_NODE_INSTALLED);
        db.execSQL(CREATE_TABLE_FAVORITE);
        db.execSQL(CREATE_TABLE_MQTT);
        db.execSQL(CREATE_TABLE_NODE_DURATION);
        db.execSQL(CREATE_TABLE_SCENE);
        db.execSQL(CREATE_TABLE_SCENE_DETAIL);
        db.execSQL(CREATE_TABLE_INFO);


    }

    //FAVORITE = DASHBOARD
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + DbNode.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbNode.TABLE_NODE);
        db.execSQL("DROP TABLE IF EXISTS " + DbNode.TABLE_FAV);
        db.execSQL("DROP TABLE IF EXISTS " + DbNode.TABLE_MQTT);
        db.execSQL("DROP TABLE IF EXISTS " + DbNode.TABLE_NODE_DURATION);
        db.execSQL("DROP TABLE IF EXISTS " + DbNode.TABLE_SCENE);
        db.execSQL("DROP TABLE IF EXISTS " + DbNode.TABLE_SCENE_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + DbNode.TABLE_INFO);

        // Create tables again
        onCreate(db);

    }
}
