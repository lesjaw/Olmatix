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

public class dbNodeRepo {
    private dbHelper dbHelper;

    public dbNodeRepo(Context context) {
        dbHelper = new dbHelper(context);
    }

    public int insert(dbNode DBNode) {

        //Open connection to write data
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(dbNode.KEY_node, DBNode.node);
        values.put(dbNode.KEY_fwname,DBNode.fwname);
        values.put(dbNode.KEY_version, DBNode.version);

        // Inserting Row
        long node_Id = db.insert(dbNode.TABLE, null, values);
        db.close(); // Closing database connection
        return (int) node_Id;
    }

    public void delete(int node_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(dbNode.TABLE, dbNode.KEY_ID + "= ?", new String[] { String.valueOf(node_Id) });
        db.close(); // Closing database connection
    }

    public void update(dbNode DBNode) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(dbNode.KEY_node, DBNode.node);
        values.put(dbNode.KEY_fwname,DBNode.fwname);
        values.put(dbNode.KEY_version, DBNode.version);

        // It's a good practice to use parameter ?, instead of concatenate string
        db.update(dbNode.TABLE, values, dbNode.KEY_ID + "= ?", new String[] { String.valueOf(DBNode.node_ID) });
        db.close(); // Closing database connection
    }

    public ArrayList<HashMap<String, String>>  getNodeList() {
        //Open connection to read only
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                dbNode.KEY_ID + "," +
                dbNode.KEY_node + "," +
                dbNode.KEY_fwname + "," +
                dbNode.KEY_version +
                " FROM " + dbNode.TABLE;

        //Student student = new Student();
        ArrayList<HashMap<String, String>> nodeList = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> node = new HashMap<String, String>();
                node.put("id", cursor.getString(cursor.getColumnIndex(dbNode.KEY_ID)));
                node.put("name", cursor.getString(cursor.getColumnIndex(dbNode.KEY_node)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return nodeList;

    }

    public dbNode getNodeById(int Id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT  " +
                dbNode.KEY_ID + "," +
                dbNode.KEY_node + "," +
                dbNode.KEY_fwname + "," +
                dbNode.KEY_version +
                " FROM " + dbNode.TABLE
                + " WHERE " +
                dbNode.KEY_ID + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        dbNode node = new dbNode();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(Id) } );

        if (cursor.moveToFirst()) {
            do {
                node.node_ID =cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID));
                node.node =cursor.getString(cursor.getColumnIndex(dbNode.KEY_node));
                node.fwname  =cursor.getString(cursor.getColumnIndex(dbNode.KEY_fwname));
                node.version =cursor.getInt(cursor.getColumnIndex(dbNode.KEY_version));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return node;
    }

}
