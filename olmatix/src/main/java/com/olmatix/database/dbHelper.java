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
    private static final int DATABASE_VERSION = 8;

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
                + dbNode.KEY_NODES + " TEXT, "
                + dbNode.KEY_NAME + " TEXT, "
                + dbNode.KEY_LOCALIP + " TEXT, "
                + dbNode.KEY_FWNAME + " INTEGER, "
                + dbNode.KEY_FWVERSION + " TEXT, "
                + dbNode.KEY_ONLINE + " BOOLEAN, "
                + dbNode.KEY_SIGNAL + " BOOLEAN, "
                + dbNode.KEY_UPTIME + " TEXT, "
                + dbNode.KEY_RESET + " BOOLEAN, "
                + dbNode.KEY_OTA + " TEXT )";

        db.execSQL(CREATE_TABLE_NODE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE);

        // Create tables again
        onCreate(db);

    }
}
