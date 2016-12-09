package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.olmatix.model.NodeModel;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;

import static com.olmatix.database.dbNode.KEY_ADDING;
import static com.olmatix.database.dbNode.KEY_FWNAME;
import static com.olmatix.database.dbNode.KEY_FWVERSION;
import static com.olmatix.database.dbNode.KEY_ICON;
import static com.olmatix.database.dbNode.KEY_LOCALIP;
import static com.olmatix.database.dbNode.KEY_NAME;
import static com.olmatix.database.dbNode.KEY_NICE_NAME;
import static com.olmatix.database.dbNode.KEY_NODES;
import static com.olmatix.database.dbNode.KEY_NODE_ID;
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

    public int insertDb(NodeModel nodeModel){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NODE_ID,nodeModel.getNodesID());
        values.put(KEY_NODES, nodeModel.getNodes());
        values.put(KEY_NAME, nodeModel.getName());
        values.put(KEY_NICE_NAME, nodeModel.getNiceName());
        values.put(KEY_LOCALIP, nodeModel.getLocalip());
        values.put(KEY_FWNAME, nodeModel.getFwName());
        values.put(KEY_FWVERSION, nodeModel.getFwVersion());
        values.put(KEY_ONLINE, nodeModel.getOnline());
        values.put(KEY_ICON, nodeModel.getIcon());
        values.put(KEY_ADDING, nodeModel.getAdding());
        values.put(KEY_SIGNAL, nodeModel.getSignal());
        values.put(KEY_UPTIME, nodeModel.getUptime());
        values.put(KEY_RESET, nodeModel.getReset());
        values.put(KEY_OTA, nodeModel.getOta());

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
        db.delete(TABLE, dbNode.KEY_NODE_ID + "= ?", new String[] {
                String.valueOf(dbNode.KEY_NODE_ID) });
        db.close(); // Closing database connection
    }

    /**
     * SQL UPDATE PROSES DATA
     * */

    public void update(NodeModel nodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID,nodeModel.getNodesID());
        values.put(KEY_NODES, nodeModel.getNodes());
        values.put(KEY_NAME, nodeModel.getName());
        values.put(KEY_NICE_NAME, nodeModel.getNiceName());
        values.put(KEY_LOCALIP, nodeModel.getLocalip());
        values.put(KEY_FWNAME, nodeModel.getFwName());
        values.put(KEY_FWVERSION, nodeModel.getFwVersion());
        values.put(KEY_ONLINE, nodeModel.getOnline());
        values.put(KEY_ICON, nodeModel.getIcon());
        values.put(KEY_ADDING, nodeModel.getAdding());
        values.put(KEY_SIGNAL, nodeModel.getSignal());
        values.put(KEY_UPTIME, nodeModel.getUptime());
        values.put(KEY_RESET, nodeModel.getReset());
        values.put(KEY_OTA, nodeModel.getOta());

        db.update(TABLE, values, dbNode.KEY_NODE_ID + "= ?", new String[] {
                String.valueOf(dbNode.KEY_NODE_ID)
        });
        db.close(); // Closing database connection

    }


    public ArrayList<NodeModel> getNodeList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " + TABLE;

        ArrayList<NodeModel> nodeList = new ArrayList<NodeModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                NodeModel node = new NodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNodesID( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNodes( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES)));
                node.setName( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.setLocalip( cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP)));
                node.setFwName( cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME)));
                node.setFwVersion( cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWVERSION)));
                node.setOnline( cursor.getString(cursor.getColumnIndex(dbNode.KEY_ONLINE)));
                node.setIcon( cursor.getString(cursor.getColumnIndex(dbNode.KEY_ICON)));
                node.setAdding( cursor.getString(cursor.getColumnIndex(dbNode.KEY_ADDING)));
                node.setSignal( cursor.getString(cursor.getColumnIndex(dbNode.KEY_SIGNAL)));
                node.setUptime( cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.setReset( cursor.getString(cursor.getColumnIndex(dbNode.KEY_RESET)));
                node.setOta( cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
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
                node.node_id =cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID));
                node.nodes = cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES));
                node.name = cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME));
                node.localip = cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP));
                node.fwname = cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME));
                node.fwversion = cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWVERSION));
                node.online = cursor.getString(cursor.getColumnIndex(dbNode.KEY_ONLINE));
                node.icon = cursor.getString(cursor.getColumnIndex(dbNode.KEY_ICON));
                node.adding = cursor.getString(cursor.getColumnIndex(dbNode.KEY_ADDING));
                node.signal = cursor.getString(cursor.getColumnIndex(dbNode.KEY_SIGNAL));
                node.uptime = cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME));
                node.reset = cursor.getString(cursor.getColumnIndex(dbNode.KEY_RESET));
                node.ota = cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return node;
    }
}
