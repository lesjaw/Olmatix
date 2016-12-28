package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbHelper extends SQLiteOpenHelper {
    //version number to upgrade database version
    //each time if you Add, Edit table, you need to change the
    //version number.
    private static final int DATABASE_VERSION = 11;

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
                + dbNode.KEY_NICE_NAME_N + " TEXT, " //this name will be from user input
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
                + dbNode.KEY_ONDURATION + " TEXT, "
                + dbNode.KEY_SENSOR + " TEXT )";

        String CREATE_TABLE_FAVORITE = "CREATE TABLE " + dbNode.TABLE_FAV  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_NICE_NAME_D + " TEXT) ";

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
                + dbNode.KEY_TIMESTAMPS_OFF + " TEXT) ";



        db.execSQL(CREATE_TABLE_NODE);
        db.execSQL(CREATE_TABLE_NODE_INSTALLED);
        db.execSQL(CREATE_TABLE_FAVORITE);
        db.execSQL(CREATE_TABLE_MQTT);
        db.execSQL(CREATE_TABLE_NODE_DURATION);

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



        // Create tables again
        onCreate(db);

    }
}
