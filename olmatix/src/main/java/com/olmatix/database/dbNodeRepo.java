package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import static com.olmatix.database.dbNode.KEY_FWNAME;
import static com.olmatix.database.dbNode.KEY_FWVERSION;
import static com.olmatix.database.dbNode.KEY_LOCALIP;
import static com.olmatix.database.dbNode.KEY_NAME;
import static com.olmatix.database.dbNode.KEY_NODES;
import static com.olmatix.database.dbNode.KEY_ONLINE;
import static com.olmatix.database.dbNode.KEY_OTA;
import static com.olmatix.database.dbNode.KEY_RESET;
import static com.olmatix.database.dbNode.KEY_SIGNAL;
import static com.olmatix.database.dbNode.KEY_UPTIME;
import static com.olmatix.database.dbNode.TABLE;

public class dbNodeRepo {
    private dbHelper dbHelper;

    public dbNodeRepo(Context context) {
        dbHelper = new dbHelper(context);
    }

    /**
     * SQL INSERT PROSES DATA
     * */

    public int insertDb(dbNode mDbNode){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODES, mDbNode.nodes);
        values.put(KEY_NAME, mDbNode.name);
        values.put(KEY_LOCALIP, mDbNode.localip);
        values.put(KEY_FWNAME, mDbNode.fwname);
        values.put(KEY_FWVERSION, mDbNode.fwversion);
        values.put(KEY_ONLINE, mDbNode.online);
        values.put(KEY_SIGNAL, mDbNode.signal);
        values.put(KEY_UPTIME, mDbNode.uptime);
        values.put(KEY_RESET, mDbNode.reset);
        values.put(KEY_OTA, mDbNode.ota);

        long node_Id = db.insert(TABLE, null, values);
        db.close(); // Closing database connection
        return (int) node_Id;
    }

    /**
     * SQL DELETE PROSES DATA BY ID
     * */

    public void delete(int node_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(TABLE, dbNode.KEY_ID + "= ?", new String[] { String.valueOf(node_Id) });
        db.close(); // Closing database connection
    }

    /**
     * SQL UPDATE PROSES DATA
     * */

    public void update(dbNode mDbNode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODES, mDbNode.nodes);
        values.put(KEY_NAME, mDbNode.name);
        values.put(KEY_LOCALIP, mDbNode.localip);
        values.put(KEY_FWNAME, mDbNode.fwname);
        values.put(KEY_FWVERSION, mDbNode.fwversion);
        values.put(KEY_ONLINE, mDbNode.online);
        values.put(KEY_SIGNAL, mDbNode.signal);
        values.put(KEY_UPTIME, mDbNode.uptime);
        values.put(KEY_RESET, mDbNode.reset);
        values.put(KEY_OTA, mDbNode.ota);

        db.update(TABLE, values, mDbNode.KEY_ID + "= ?", new String[] { String.valueOf(mDbNode.node_id) });
        db.close(); // Closing database connection

    }

    /**
     * SQL GET LIST DATA
     * */

    public ArrayList<HashMap<String, String>> getNodeList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " + TABLE;

        ArrayList<HashMap<String, String>> nodeList = new ArrayList<HashMap<String, String>>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> node = new HashMap<String, String>();
                node.put("id", cursor.getString(cursor.getColumnIndex(dbNode.KEY_ID)));
                node.put("nodes", cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES)));
                node.put("name", cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.put("localip", cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP)));
                node.put("fwname", cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME)));
                node.put("fwversion", cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWVERSION)));
                node.put("online", cursor.getString(cursor.getColumnIndex(dbNode.KEY_ONLINE)));
                node.put("signal", cursor.getString(cursor.getColumnIndex(dbNode.KEY_SIGNAL)));
                node.put("uptime", cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.put("reset", cursor.getString(cursor.getColumnIndex(dbNode.KEY_RESET)));
                node.put("ota", cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }


    public dbNode getNodeById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  * FROM " + dbNode.TABLE
                + " WHERE " +
                dbNode.KEY_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        dbNode node = new dbNode();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {
                node.node_id =cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID));
                node.nodes = cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES));
                node.name = cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME));
                node.localip = cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP));
                node.fwname = cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME));
                node.fwversion = cursor.getInt(cursor.getColumnIndex(dbNode.KEY_FWVERSION));
                node.online = cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ONLINE));
                node.signal = cursor.getInt(cursor.getColumnIndex(dbNode.KEY_SIGNAL));
                node.uptime = cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME));
                node.reset = cursor.getInt(cursor.getColumnIndex(dbNode.KEY_RESET));
                node.ota = cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return node;
    }

    public dbNode getNodeByNode(String nodeName){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " + dbNode.TABLE
                + " WHERE " +
                dbNode.KEY_NODES + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        dbNode node = new dbNode();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(nodeName) } );

        if (cursor.moveToFirst()) {
            do {
                node.node_id =cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID));
                node.nodes = cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES));
                node.name = cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME));
                node.localip = cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP));
                node.fwname = cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME));
                node.fwversion = cursor.getInt(cursor.getColumnIndex(dbNode.KEY_FWVERSION));
                node.online = cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ONLINE));
                node.signal = cursor.getInt(cursor.getColumnIndex(dbNode.KEY_SIGNAL));
                node.uptime = cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME));
                node.reset = cursor.getInt(cursor.getColumnIndex(dbNode.KEY_RESET));
                node.ota = cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return node;
    }
}
