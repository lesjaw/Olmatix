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
import com.olmatix.model.Duration_Model;
import com.olmatix.model.Installed_NodeModel;
import com.olmatix.model.SpinnerObject;

import java.util.ArrayList;
import java.util.List;

import static com.olmatix.database.dbNode.KEY_ADDING;
import static com.olmatix.database.dbNode.KEY_CHANNEL;
import static com.olmatix.database.dbNode.KEY_DURATION;
import static com.olmatix.database.dbNode.KEY_FWNAME;
import static com.olmatix.database.dbNode.KEY_FWVERSION;
import static com.olmatix.database.dbNode.KEY_ICON;
import static com.olmatix.database.dbNode.KEY_ID;
import static com.olmatix.database.dbNode.KEY_LOCALIP;
import static com.olmatix.database.dbNode.KEY_MESSAGE;
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
import static com.olmatix.database.dbNode.KEY_TIMESTAMPS_OFF;
import static com.olmatix.database.dbNode.KEY_TIMESTAMPS_ON;
import static com.olmatix.database.dbNode.KEY_TOPIC;
import static com.olmatix.database.dbNode.KEY_UPTIME;
import static com.olmatix.database.dbNode.TABLE;
import static com.olmatix.database.dbNode.TABLE_FAV;
import static com.olmatix.database.dbNode.TABLE_MQTT;
import static com.olmatix.database.dbNode.TABLE_NODE;
import static com.olmatix.database.dbNode.TABLE_NODE_DURATION;

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
        values.put(KEY_NAME, installedNodeModel.getFwName());
        values.put(KEY_NICE_NAME_N, installedNodeModel.getFwName());
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
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public int insertDbMqtt(dbNode dbNode){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TOPIC, dbNode.getTopic());
        values.put(KEY_MESSAGE, dbNode.getMessage());

        long Id = db.insert(TABLE_MQTT, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
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
        //Log.d("DEBUG", "insertDetail: " + String.valueOf(KEY_NODE_ID));

        return (int) node_Id;
    }

    public void insertFavNode(Dashboard_NodeModel dashboardNodeModel){
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NICE_NAME_D,dashboardNodeModel.getNice_name_d());

        db.insert(TABLE_FAV, null, values);

        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertFav: " + String.valueOf(KEY_NICE_NAME_D));

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

    public void deleteFav(int node_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(TABLE_FAV, dbNode.KEY_NICE_NAME_D + "= ?", new String[] {
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
            //Log.d("DEBUG", "updateNode: " +installedNodeModel.getFwName());
        }
        if (installedNodeModel.getFwVersion()!=null) {
            values.put(KEY_FWVERSION, installedNodeModel.getFwVersion());
        }
        if (installedNodeModel.getOnline()!=null) {
            values.put(KEY_ONLINE, installedNodeModel.getOnline());

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
            //Log.d("DEBUG", "updateDetail Status: " +detailNodeModel.getStatus());
        }

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " +dbNode.KEY_CHANNEL +"=?", new String[] {
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection


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
            //Log.d("DEBUG", "updateDetail Status Sensor : " +detailNodeModel.getStatus_sensor());
        }
        if (detailNodeModel.getStatus_theft()!=null) {
            values.put(KEY_STATUS_THEFT, detailNodeModel.getStatus_theft());
            //Log.d("DEBUG", "updateDetail Status Theft : " +detailNodeModel.getStatus_theft());
        }

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " +dbNode.KEY_CHANNEL +"=?", new String[] {
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection
        //Log.d("DEBUG", "updateDetailSensor: " + String.valueOf(detailNodeModel.getNode_id()) +" : "+
                //String.valueOf(detailNodeModel.getChannel()));

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

    public List<SpinnerObject> getAllLabels(){
        List<SpinnerObject> labels = new ArrayList<SpinnerObject>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NODE;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list


        if (cursor.moveToFirst()) {
            do {
                //labels.add(cursor.getString(0)+","+ cursor.getString(3));
                labels.add ( new SpinnerObject ( cursor.getInt(0) , cursor.getString(3) ) );
                //labels.add(cursor.getString(1));
                //labels.add(cursor.getString(3));

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

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<Detail_NodeModel> getNodeDetailList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT * FROM " + dbNode.TABLE_NODE;

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        ArrayList<Detail_NodeModel> nodeList = new ArrayList<Detail_NodeModel>();

        Cursor cursor = db.rawQuery(selectString,null);


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

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<Installed_NodeModel> getNodeListbyNode(String node_id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM " + TABLE + " WHERE " + KEY_NODE_ID + " =?";

        ArrayList<Installed_NodeModel> nodeList = new ArrayList<Installed_NodeModel>();

        Cursor cursor = db.rawQuery(selectQuery, new String[] {
                String.valueOf(node_id)});

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
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<Dashboard_NodeModel> getNodeDetailDash() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery =  "SELECT * FROM "+TABLE_FAV +
                " favorite_node INNER JOIN "+TABLE_NODE +
                " detail_node ON favorite_node."+KEY_NICE_NAME_D+" = detail_node."+KEY_ID+
                " INNER JOIN "+TABLE +
                " installed_node ON installed_node."+KEY_NODE_ID+" = detail_node."+KEY_NODE_ID;


        ArrayList<Dashboard_NodeModel> nodeList = new ArrayList<>();
        Cursor cursor = db.rawQuery(selectQuery,  null);
        if (cursor.moveToFirst()) {
            do {
                Dashboard_NodeModel node = new Dashboard_NodeModel();
                node.setId(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID)));
                node.setNice_name_d( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setSensor( cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));
                node.setStatus( cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setNodeid(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_THEFT)));
                node.setOnline(cursor.getString(cursor.getColumnIndex(dbNode.KEY_ONLINE)));
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
            //Log.d("hasObject", String.format("%d records found", count));

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
            //Log.d("hasObjectDetail", String.format("%d records found", count));

            //endregion

        }

        cursor.close();          // Dont forget to close your cursor
        db.close();              //AND your Database!
        return hasObject;
    }

    public int insertDurationNode(Duration_Model durationModel){

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID,durationModel.getNodeId());
        values.put(KEY_CHANNEL,durationModel.getChannel());
        values.put(KEY_STATUS,durationModel.getStatus());
        values.put(KEY_TIMESTAMPS_ON,String.valueOf(durationModel.getTimeStampOn()));
        values.put(KEY_TIMESTAMPS_OFF,String.valueOf(durationModel.getTimeStampOff()));
        values.put(KEY_DURATION,String.valueOf(durationModel.getDuration()));


        long Id = db.insert(TABLE_NODE_DURATION, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertDur: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public void updateOff(Duration_Model durationModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TIMESTAMPS_OFF, durationModel.getTimeStampOff());
        values.put(KEY_DURATION,durationModel.getDuration());

        db.update(TABLE_NODE_DURATION, values, dbNode.KEY_NODE_ID + "=? AND "
                +dbNode.KEY_CHANNEL +"=? AND "
                +dbNode.KEY_TIMESTAMPS_OFF+"=?", new String[] {
                String.valueOf(durationModel.getNodeId()),
                String.valueOf(durationModel.getChannel()),
                String.valueOf(0)
        });
        db.close(); // Closing database connection
        //Log.d("DEBUG", "updateDetail: " + String.valueOf(detailNodeModel.getNode_id()) +" : "+
        //        String.valueOf(detailNodeModel.getChannel()));

    }

    public ArrayList<Duration_Model> getNodeDurationList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT * FROM " + dbNode.TABLE_NODE_DURATION;

        ArrayList<Duration_Model> nodeList = new ArrayList<Duration_Model>();

        Cursor cursor = db.rawQuery(selectString,null);

        if (cursor.moveToFirst()) {
            do {
                Duration_Model node = new Duration_Model();
                //ArrayList<String> node = new ArrayList<>();
                node.setId( cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID)));
                node.setNodeId( cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setChannel( cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setStatus( cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setTimeStampOn( cursor.getLong(cursor.getColumnIndex(dbNode.KEY_TIMESTAMPS_ON)));
                node.setDuration( cursor.getLong(cursor.getColumnIndex(dbNode.KEY_DURATION)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }
    // ChartDatabase

    public ArrayList<Duration_Model> getChartDurationList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT  duration_node."+KEY_NODE_ID +
                ", strftime( '%d/%m', duration_node."+ KEY_TIMESTAMPS_ON +"/ 1000, 'unixepoch') AS act_time, " +
                " detail_node." + KEY_NICE_NAME_D + ", SUM(duration_node." +KEY_DURATION + " / 60 / 60) AS duration "+
                " FROM " + dbNode.TABLE_NODE_DURATION +" duration_node INNER JOIN " +  TABLE_NODE +
                " detail_node ON  duration_node." + KEY_NODE_ID +" = detail_node." + KEY_NODE_ID +
                " GROUP BY act_time, duration_node."+KEY_NODE_ID ;

        Log.d("DEBUG", "getChartDurationList: " + selectString);


        ArrayList<Duration_Model> nodeList = new ArrayList<Duration_Model>();

        Cursor cursor = db.rawQuery(selectString,null);

        if (cursor.moveToFirst()) {
            do {
                Duration_Model node = new Duration_Model();
                //ArrayList<String> node = new ArrayList<>();
                node.setNodeId( cursor.getString(0));
                node.setTimeStampOn( cursor.getLong(1));
                node.setNiceName(cursor.getString(2));
                node.setDuration( cursor.getLong(3));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<Detail_NodeModel> getNodeDetailAll(String node_id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT node_installed."+KEY_NODE_ID + ", node_installed."+KEY_CHANNEL +
                ", node_installed."+KEY_NICE_NAME_D +", node_installed."+KEY_STATUS+
                ", node_installed."+KEY_STATUS_SENSOR + ", node_installed."+KEY_STATUS_THEFT + ", node_installed."+KEY_SENSOR +
                ", SUM(duration_node."+KEY_DURATION +") as totaldur"+
                " FROM node_installed " + TABLE_NODE + " INNER JOIN " + TABLE_NODE_DURATION +
                " duration_node ON node_installed." + KEY_NODE_ID + " = duration_node." + KEY_NODE_ID +
                " AND node_installed."+KEY_CHANNEL + " = duration_node."+KEY_CHANNEL +" WHERE duration_node."+KEY_NODE_ID + "=?" +
                " GROUP BY node_installed."+KEY_NICE_NAME_D;


        //Log.d("DEBUG", "getNodeDetailAll: "+selectQuery);

        ArrayList<Detail_NodeModel> nodeList = new ArrayList<Detail_NodeModel>();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(node_id)});

        if (cursor.moveToFirst()) {
            do {
                Detail_NodeModel node = new Detail_NodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNode_id(cursor.getString(0));
                node.setChannel(cursor.getString(1));
                node.setNice_name_d(cursor.getString(2));
                node.setStatus(cursor.getString(3));
                node.setStatus_sensor(cursor.getString(4));
                node.setStatus_theft(cursor.getString(5));
                node.setSensor(cursor.getString(6));
                node.setDuration(cursor.getString(7));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }
}

