package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.olmatix.model.Dashboard_NodeModel;
import com.olmatix.model.Detail_NodeModel;
import com.olmatix.model.Installed_NodeModel;

import java.util.ArrayList;
import java.util.List;

import static com.olmatix.database.dbNode.KEY_ADDING;
import static com.olmatix.database.dbNode.KEY_CHANNEL;
import static com.olmatix.database.dbNode.KEY_FWNAME;
import static com.olmatix.database.dbNode.KEY_FWVERSION;
import static com.olmatix.database.dbNode.KEY_ICON;
import static com.olmatix.database.dbNode.KEY_LOCALIP;
import static com.olmatix.database.dbNode.KEY_NAME;
import static com.olmatix.database.dbNode.KEY_NICE_NAME_D;
import static com.olmatix.database.dbNode.KEY_NICE_NAME_N;
import static com.olmatix.database.dbNode.KEY_NODES;
import static com.olmatix.database.dbNode.KEY_NODE_ID;
import static com.olmatix.database.dbNode.KEY_ONLINE;
import static com.olmatix.database.dbNode.KEY_OTA;
import static com.olmatix.database.dbNode.KEY_RESET;
import static com.olmatix.database.dbNode.KEY_SENSOR;
import static com.olmatix.database.dbNode.KEY_SIGNAL;
import static com.olmatix.database.dbNode.KEY_STATUS;
import static com.olmatix.database.dbNode.KEY_STATUS_SENSOR;
import static com.olmatix.database.dbNode.KEY_STATUS_THEFT;
import static com.olmatix.database.dbNode.KEY_TIMESTAMPS;
import static com.olmatix.database.dbNode.KEY_UPTIME;
import static com.olmatix.database.dbNode.TABLE;
import static com.olmatix.database.dbNode.TABLE_FAV;
import static com.olmatix.database.dbNode.TABLE_NODE;

public class dbNodeRepo {
    private dbHelper dbHelper;

    public dbNodeRepo(Context context) {
        dbHelper = new dbHelper(context);
    }

    public int insertDb(Installed_NodeModel installedNodeModel){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NODE_ID, installedNodeModel.getNodesID());
        values.put(KEY_NODES, installedNodeModel.getNodes());
        values.put(KEY_NAME, installedNodeModel.getName());
        values.put(KEY_NICE_NAME_N, installedNodeModel.getNice_name_n());
        values.put(KEY_LOCALIP, installedNodeModel.getLocalip());
        values.put(KEY_FWNAME, installedNodeModel.getFwName());
        values.put(KEY_FWVERSION, installedNodeModel.getFwVersion());
        values.put(KEY_ONLINE, installedNodeModel.getOnline());
        values.put(KEY_ICON, installedNodeModel.getIcon());
        values.put(KEY_ADDING, String.valueOf(installedNodeModel.getAdding()));
        values.put(KEY_SIGNAL, installedNodeModel.getSignal());
        values.put(KEY_UPTIME, installedNodeModel.getUptime());
        values.put(KEY_RESET, installedNodeModel.getReset());
        values.put(KEY_OTA, installedNodeModel.getOta());

        long Id = db.insert(TABLE, null, values);
        db.close(); // Closing database connection
        Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public int insertInstalledNode(Detail_NodeModel nodeModel){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID,nodeModel.getNode_id());
        values.put(KEY_CHANNEL, nodeModel.getChannel());
        values.put(KEY_STATUS, nodeModel.getStatus());
        values.put(KEY_NICE_NAME_D, nodeModel.getNice_name_d());
        values.put(KEY_SENSOR, nodeModel.getSensor());
        values.put(KEY_STATUS_SENSOR, nodeModel.getStatus_sensor());

        long node_Id = db.insert(TABLE_NODE, null, values);
        db.close(); // Closing database connection
        Log.d("DEBUG", "insertDetail: " + String.valueOf(KEY_NODE_ID));

        return (int) node_Id;
    }

    public void insertFavNode(Dashboard_NodeModel dashboardNodeModel){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NICE_NAME_D,dashboardNodeModel.getNice_name_d());

        //long node_Id = db.insert(TABLE_FAV, null, values);
        db.insert(TABLE_FAV, null, values);

        db.close(); // Closing database connection
        Log.d("DEBUG", "insertDetail: " + String.valueOf(KEY_NICE_NAME_D));

        return;
    }

    public void deleteNode(String node_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(TABLE, dbNode.KEY_NODE_ID + "= ?", new String[] {
                String.valueOf(node_Id) });

        db.delete(TABLE_NODE, dbNode.KEY_NODE_ID + "= ?", new String[] {
                String.valueOf(node_Id) });

        db.close(); // Closing database connection
    }

    public void update(Installed_NodeModel installedNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, installedNodeModel.getNodesID());
        if (installedNodeModel.getNodes()!=null) {
            values.put(KEY_NODES, installedNodeModel.getNodes());
        }
        if (installedNodeModel.getName()!=null) {
            values.put(KEY_NAME, installedNodeModel.getName());
        }
        if (installedNodeModel.getNice_name_n()!=null) {
            values.put(KEY_NICE_NAME_N, installedNodeModel.getNice_name_n());
        }
        if (installedNodeModel.getLocalip()!=null) {
            values.put(KEY_LOCALIP, installedNodeModel.getLocalip());
        }
        if (installedNodeModel.getFwName()!=null) {
            values.put(KEY_FWNAME, installedNodeModel.getFwName());
        }
        if (installedNodeModel.getFwVersion()!=null) {
            values.put(KEY_FWVERSION, installedNodeModel.getFwVersion());
        }
        if (installedNodeModel.getOnline()!=null) {
            values.put(KEY_ONLINE, installedNodeModel.getOnline());
//            Log.d("DEBUG", "updateNode Online: " +installedNodeModel.getOnline());

        }
        if (installedNodeModel.getNodesID()!=null) {
            values.put(KEY_ICON, installedNodeModel.getIcon());
        }
        if (Long.valueOf(installedNodeModel.getAdding())!=null) {
            values.put(KEY_ADDING, Long.valueOf(installedNodeModel.getAdding()));
        }
        if (installedNodeModel.getSignal()!=null) {
            values.put(KEY_SIGNAL, installedNodeModel.getSignal());
        }
        if (installedNodeModel.getUptime()!=null) {
            values.put(KEY_UPTIME, installedNodeModel.getUptime());
        }
        if (installedNodeModel.getReset()!=null) {
            values.put(KEY_RESET, installedNodeModel.getReset());
        }
        if (installedNodeModel.getOta()!=null) {
            values.put(KEY_OTA, installedNodeModel.getOta());
        }
        db.update(TABLE, values, dbNode.KEY_NODE_ID + "= ?", new String[] {
                String.valueOf(installedNodeModel.getNodesID())
        });
        db.close(); // Closing database connection
        //Log.d("DEBUG", "updateNode: " + String.valueOf(installedNodeModel.getNodesID()));

    }

    public void updateNameNice(Installed_NodeModel installedNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, installedNodeModel.getNodesID());

        if (installedNodeModel.getNice_name_n()!=null) {
            values.put(KEY_NICE_NAME_N, installedNodeModel.getNice_name_n());
        }

        db.update(TABLE, values, dbNode.KEY_NODE_ID + "= ?", new String[] {
                String.valueOf(installedNodeModel.getNodesID())
        });
        db.close(); // Closing database connection

    }

    public void update_detail(Detail_NodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getChannel()!=null) {
            values.put(KEY_CHANNEL, detailNodeModel.getChannel());
        }
        if (detailNodeModel.getStatus()!=null || detailNodeModel.getStatus() != "ON" || detailNodeModel.getStatus() != "OFF") {
            values.put(KEY_STATUS, detailNodeModel.getStatus());
            Log.d("DEBUG", "updateDetail Status: " +detailNodeModel.getStatus());
        }
        /*if (detailNodeModel.getSensor()!=null) {
            values.put(KEY_SENSOR, detailNodeModel.getSensor());
        }*/

        if (detailNodeModel.getTimestamps()!=null) {
            values.put(KEY_TIMESTAMPS, detailNodeModel.getTimestamps());
            Log.d("DEBUG", "updateDetail timestamps : " +detailNodeModel.getTimestamps());
        }

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " +dbNode.KEY_CHANNEL +"=?", new String[] {
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection
        //Log.d("DEBUG", "updateDetail: " + String.valueOf(detailNodeModel.getNode_id()) +" : "+
        //        String.valueOf(detailNodeModel.getChannel()));

    }

    public void update_detail_NiceName(Detail_NodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getNice_name_d()!=null) {
            values.put(KEY_NICE_NAME_D, detailNodeModel.getNice_name_d());
        }
        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " +dbNode.KEY_CHANNEL +"=?", new String[] {
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection

    }

    public void update_detailSensor(Detail_NodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getStatus_sensor()!=null) {
            values.put(KEY_STATUS_SENSOR, detailNodeModel.getStatus_sensor());
            Log.d("DEBUG", "updateDetail Status Sensor : " +detailNodeModel.getStatus_sensor());
        }
        if (detailNodeModel.getStatus_theft()!=null) {
            values.put(KEY_STATUS_THEFT, detailNodeModel.getStatus_theft());
            Log.d("DEBUG", "updateDetail Status Theft : " +detailNodeModel.getStatus_theft());
        }

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " +dbNode.KEY_CHANNEL +"=?", new String[] {
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection
        Log.d("DEBUG", "updateDetailSensor: " + String.valueOf(detailNodeModel.getNode_id()) +" : "+
                String.valueOf(detailNodeModel.getChannel()));

    }

    public ArrayList<Installed_NodeModel> getNodeList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " + TABLE;

        ArrayList<Installed_NodeModel> nodeList = new ArrayList<Installed_NodeModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Installed_NodeModel node = new Installed_NodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNodesID( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNodes( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES)));
                node.setName( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.setNice_name_n( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_N)));
                node.setLocalip( cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP)));
                node.setFwName( cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME)));
                node.setFwVersion( cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWVERSION)));
                node.setOnline( cursor.getString(cursor.getColumnIndex(dbNode.KEY_ONLINE)));
                node.setIcon( cursor.getString(cursor.getColumnIndex(dbNode.KEY_ICON)));
                node.setAdding( cursor.getLong(cursor.getColumnIndex(dbNode.KEY_ADDING)));
                node.setSignal( cursor.getString(cursor.getColumnIndex(dbNode.KEY_SIGNAL)));
                node.setUptime( cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.setReset( cursor.getString(cursor.getColumnIndex(dbNode.KEY_RESET)));
                node.setOta( cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        //Log.d("getlist", "getNodeList: " +cursor.getCount());
        cursor.close();
        db.close();
        return nodeList;
    }

    public List<String> getAllLabels(){
        List<String> labels = new ArrayList<String>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NODE;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                //labels.add(cursor.getString(1)+" | "+ cursor.getString(3));
                labels.add(cursor.getString(3));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return labels;
    }

    public ArrayList<Detail_NodeModel> getNodeDetail(String node_id, String Channel) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT * FROM " + dbNode.TABLE_NODE + " WHERE " + KEY_NODE_ID + " =? AND "+KEY_CHANNEL +" =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        ArrayList<Detail_NodeModel> nodeList = new ArrayList<Detail_NodeModel>();

        Cursor cursor = db.rawQuery(selectString, new String[] {
                String.valueOf(node_id),
                String.valueOf(Channel) });


        if (cursor.moveToFirst()) {
            do {
                Detail_NodeModel node = new Detail_NodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNode_id( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setChannel( cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setStatus( cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setNice_name_d( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setSensor( cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));
                node.setStatus_sensor( cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft( cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_THEFT)));
                node.setTimestamps(cursor.getString(cursor.getColumnIndex(dbNode.KEY_TIMESTAMPS)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<Dashboard_NodeModel> getNodeFav() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " + TABLE_FAV;

        ArrayList<Dashboard_NodeModel> favList = new ArrayList<Dashboard_NodeModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Dashboard_NodeModel node = new Dashboard_NodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNice_name_d( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));

                favList.add(node);

            } while (cursor.moveToNext());
        }
        //Log.d("getlist", "getNodeList: " +cursor.getCount());
        cursor.close();
        db.close();
        return favList;
    }

    public ArrayList<Dashboard_NodeModel> getNodeDetailDash() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM "+TABLE_FAV + " favorite_node INNER JOIN "+  TABLE_NODE +
                " detail_node ON favorite_node."+KEY_NICE_NAME_D+" = detail_node."+KEY_NICE_NAME_D;
        



        ArrayList<Dashboard_NodeModel> nodeList = new ArrayList<>();
        Cursor cursor = db.rawQuery(selectQuery,  null);

        if (cursor.moveToFirst()) {
            do {
                Dashboard_NodeModel node = new Dashboard_NodeModel();

                node.setFwName( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNode_id( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setChannel( cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setStatus( cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setNice_name_d( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setName( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.setUptime( cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.setSensor( cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_THEFT)));
                node.setTimestamps(cursor.getString(cursor.getColumnIndex(dbNode.KEY_TIMESTAMPS)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<Detail_NodeModel> getNodeDetailID(String node_id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM "+TABLE + " installed_node INNER JOIN "+  TABLE_NODE +
                " detail_node ON installed_node."+KEY_NODE_ID+" = detail_node."+KEY_NODE_ID +
                " WHERE detail_node." +KEY_NODE_ID +"=?";

        ArrayList<Detail_NodeModel> nodeList = new ArrayList<Detail_NodeModel>();
        Cursor cursor = db.rawQuery(selectQuery,  new String[]{String.valueOf(node_id)});

        if (cursor.moveToFirst()) {
            do {
                Detail_NodeModel node = new Detail_NodeModel();
                //ArrayList<String> node = new ArrayList<>();

                node.setFwName( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNode_id( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setChannel( cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setStatus( cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setNice_name_d( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setName( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.setUptime( cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.setSensor( cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_THEFT)));
                node.setTimestamps(cursor.getString(cursor.getColumnIndex(dbNode.KEY_TIMESTAMPS)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public boolean hasObject(Installed_NodeModel installedNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selectString = "SELECT * FROM " + dbNode.TABLE + " WHERE " + KEY_NODE_ID + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[] {String.valueOf(installedNodeModel.getNodesID())});

        boolean hasObject = false;
        if(cursor.moveToFirst()){
            hasObject = true;

            //region if you had multiple records to check for, use this region.

            int count = 0;
            while(cursor.moveToNext()){
                count++;
            }
            //here, count is records found
            Log.d("hasObject", String.format("%d records found", count));

            //endregion

        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return hasObject;
    }

    public boolean hasDetailObject(Detail_NodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selectString = "SELECT * FROM " + dbNode.TABLE_NODE + " WHERE " + KEY_NODE_ID + " =? AND "+KEY_CHANNEL +" =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[] {
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel()) });

        boolean hasObject = false;
        if(cursor.moveToFirst()){
            hasObject = true;

            //region if you had multiple records to check for, use this region.

            int count = 0;
            while(cursor.moveToNext()){
                count++;
            }
            //here, count is records found
            Log.d("hasObjectDetail", String.format("%d records found", count));

            //endregion

        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return hasObject;
    }

    public dbNode getNodeByNode(Installed_NodeModel installedNodeModel){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " + dbNode.TABLE
                + " WHERE " +
                dbNode.KEY_NODES + "=?";// It's a good practice to use parameter ?, instead of concatenate string

        int iCount =0;
        dbNode node = new dbNode();

        Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(installedNodeModel.getNodesID()) } );

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
