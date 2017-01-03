package com.olmatix.database;

/**
 * Created by Lesjaw on 05/12/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.olmatix.model.DashboardNodeModel;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.DurationModel;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.model.SceneDetailModel;
import com.olmatix.model.SceneModel;
import com.olmatix.model.SpinnerObject;

import java.util.ArrayList;
import java.util.List;

import static com.olmatix.database.DbNode.KEY_ADDING;
import static com.olmatix.database.DbNode.KEY_ARRIVE;
import static com.olmatix.database.DbNode.KEY_CHANNEL;
import static com.olmatix.database.DbNode.KEY_COMMAND;
import static com.olmatix.database.DbNode.KEY_DURATION;
import static com.olmatix.database.DbNode.KEY_FWNAME;
import static com.olmatix.database.DbNode.KEY_FWVERSION;
import static com.olmatix.database.DbNode.KEY_ICON;
import static com.olmatix.database.DbNode.KEY_ID;
import static com.olmatix.database.DbNode.KEY_LEAVE;
import static com.olmatix.database.DbNode.KEY_LOCALIP;
import static com.olmatix.database.DbNode.KEY_MESSAGE;
import static com.olmatix.database.DbNode.KEY_NAME;
import static com.olmatix.database.DbNode.KEY_NICE_NAME_D;
import static com.olmatix.database.DbNode.KEY_NICE_NAME_N;
import static com.olmatix.database.DbNode.KEY_NODES;
import static com.olmatix.database.DbNode.KEY_NODE_ID;
import static com.olmatix.database.DbNode.KEY_ONLINE;
import static com.olmatix.database.DbNode.KEY_OTA;
import static com.olmatix.database.DbNode.KEY_PATH;
import static com.olmatix.database.DbNode.KEY_RESET;
import static com.olmatix.database.DbNode.KEY_SCENE_ID;
import static com.olmatix.database.DbNode.KEY_SCENE_NAME;
import static com.olmatix.database.DbNode.KEY_SCENE_TYPE;
import static com.olmatix.database.DbNode.KEY_SCHEDULE;
import static com.olmatix.database.DbNode.KEY_SENSOR;
import static com.olmatix.database.DbNode.KEY_SIGNAL;
import static com.olmatix.database.DbNode.KEY_STATUS;
import static com.olmatix.database.DbNode.KEY_STATUS_SENSOR;
import static com.olmatix.database.DbNode.KEY_STATUS_THEFT;
import static com.olmatix.database.DbNode.KEY_TIMESTAMPS_OFF;
import static com.olmatix.database.DbNode.KEY_TIMESTAMPS_ON;
import static com.olmatix.database.DbNode.KEY_TOPIC;
import static com.olmatix.database.DbNode.KEY_UPTIME;
import static com.olmatix.database.DbNode.TABLE;
import static com.olmatix.database.DbNode.TABLE_FAV;
import static com.olmatix.database.DbNode.TABLE_MQTT;
import static com.olmatix.database.DbNode.TABLE_NODE;
import static com.olmatix.database.DbNode.TABLE_NODE_DURATION;
import static com.olmatix.database.DbNode.TABLE_SCENE;
import static com.olmatix.database.DbNode.TABLE_SCENE_DETAIL;

public class DbNodeRepo {
    private DbHelper dbHelper;


    public DbNodeRepo(Context context) {
        dbHelper = new DbHelper(context);
    }

    public int insertDb(InstalledNodeModel installedNodeModel) {
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

    public int insertDbMqtt(DbNode DbNode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TOPIC, DbNode.getTopic());
        values.put(KEY_MESSAGE, DbNode.getMessage());

        long Id = db.insert(TABLE_MQTT, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }


    public int insertInstalledNode(DetailNodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());
        values.put(KEY_CHANNEL, detailNodeModel.getChannel());
        values.put(KEY_STATUS, detailNodeModel.getStatus());
        values.put(KEY_NICE_NAME_D, detailNodeModel.getNice_name_d());
        values.put(KEY_SENSOR, detailNodeModel.getSensor());
        values.put(KEY_STATUS_SENSOR, detailNodeModel.getStatus_sensor());

        long node_Id = db.insert(TABLE_NODE, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertDetail: " + String.valueOf(KEY_NODE_ID));

        return (int) node_Id;
    }

    public void insertFavNode(DashboardNodeModel dashboardNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NICE_NAME_D, dashboardNodeModel.getNice_name_d());

        db.insert(TABLE_FAV, null, values);

        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertFav: " + String.valueOf(KEY_NICE_NAME_D));

        return;
    }

    public int insertDbScene(SceneModel sceneModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SCENE_NAME, sceneModel.getSceneName());
        values.put(KEY_SCENE_TYPE, sceneModel.getSceneType());
        values.put(KEY_SCHEDULE, sceneModel.getSchedule());
        values.put(KEY_ARRIVE, sceneModel.getArrived());
        values.put(KEY_LEAVE, sceneModel.getLeave());

        long Id = db.insert(TABLE_SCENE, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public int insertSceneDetail(SceneDetailModel sceneDetailModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SCENE_ID, sceneDetailModel.getSceneid());
        values.put(KEY_PATH, sceneDetailModel.getPath());
        values.put(KEY_COMMAND, sceneDetailModel.getCommand());
        long Id = db.insert(TABLE_SCENE, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public ArrayList<SceneDetailModel> getAllScene() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT "+ TABLE_SCENE_DETAIL + ".*, "+TABLE_NODE+"."
                +DbNode.KEY_NODE_ID +","+TABLE_NODE+"."
                +DbNode.KEY_NICE_NAME_D +","+TABLE_SCENE+"."+DbNode.KEY_SCENE_NAME+" FROM " + TABLE_SCENE_DETAIL
                +" JOIN " + TABLE_NODE
                +" ON " + TABLE_SCENE_DETAIL+"."+DbNode.KEY_PATH +" = "+ TABLE_NODE+"." +DbNode.KEY_NODE_ID
                +" JOIN " + TABLE_SCENE
                +" ON " + TABLE_SCENE_DETAIL+"."+DbNode.KEY_SCENE_ID +" = "+ TABLE_SCENE+"." +DbNode.KEY_SCENE_TYPE;
        Log.d("DEBUG", "getAllScene: " + selectQuery);

        ArrayList<SceneDetailModel> nodeList = new ArrayList<SceneDetailModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SceneDetailModel node = new SceneDetailModel();

                node.setSceneid(cursor.getInt(cursor.getColumnIndex(DbNode.KEY_SCENE_ID)));
                node.setPath(cursor.getString(cursor.getColumnIndex(DbNode.KEY_PATH)));
                node.setCommand(cursor.getString(cursor.getColumnIndex(DbNode.KEY_COMMAND)));
                node.setNodeId(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setNiceName(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NICE_NAME_D)));
                node.setSceneType(cursor.getString(cursor.getColumnIndex(DbNode.KEY_SCENE_TYPE)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        Log.d("getlist", "getNodeList: " +cursor.getCount());
        cursor.close();
        db.close();
        return nodeList;
    }

    public void deleteNode(String node_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(TABLE, DbNode.KEY_NODE_ID + "= ?", new String[]{
                String.valueOf(node_Id)});

        db.delete(TABLE_NODE, DbNode.KEY_NODE_ID + "= ?", new String[]{
                String.valueOf(node_Id)});

        db.close(); // Closing database connection
    }

    public void deleteFav(int node_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(TABLE_FAV, DbNode.KEY_NICE_NAME_D + "= ?", new String[]{
                String.valueOf(node_Id)});

        db.close(); // Closing database connection
    }

    public void update(InstalledNodeModel installedNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, installedNodeModel.getNodesID());
        if (installedNodeModel.getNodes() != null) {
            values.put(KEY_NODES, installedNodeModel.getNodes());
        }
        if (installedNodeModel.getName() != null) {
            values.put(KEY_NAME, installedNodeModel.getName());
        }
        if (installedNodeModel.getNice_name_n() != null) {
            values.put(KEY_NICE_NAME_N, installedNodeModel.getNice_name_n());
        }
        if (installedNodeModel.getLocalip() != null) {
            values.put(KEY_LOCALIP, installedNodeModel.getLocalip());
        }
        if (installedNodeModel.getFwName() != null) {
            values.put(KEY_FWNAME, installedNodeModel.getFwName());
            //Log.d("DEBUG", "updateNode: " +installedNodeModel.getFwName());
        }
        if (installedNodeModel.getFwVersion() != null) {
            values.put(KEY_FWVERSION, installedNodeModel.getFwVersion());
        }
        if (installedNodeModel.getOnline() != null) {
            values.put(KEY_ONLINE, installedNodeModel.getOnline());

        }
        if (installedNodeModel.getNodesID() != null) {
            values.put(KEY_ICON, installedNodeModel.getIcon());
        }
        if (Long.valueOf(installedNodeModel.getAdding()) != null) {
            values.put(KEY_ADDING, Long.valueOf(installedNodeModel.getAdding()));
        }
        if (installedNodeModel.getSignal() != null) {
            values.put(KEY_SIGNAL, installedNodeModel.getSignal());
        }
        if (installedNodeModel.getUptime() != null) {
            values.put(KEY_UPTIME, installedNodeModel.getUptime());
        }
        if (installedNodeModel.getReset() != null) {
            values.put(KEY_RESET, installedNodeModel.getReset());
        }
        if (installedNodeModel.getOta() != null) {
            values.put(KEY_OTA, installedNodeModel.getOta());
        }
        db.update(TABLE, values, DbNode.KEY_NODE_ID + "= ?", new String[]{
                String.valueOf(installedNodeModel.getNodesID())
        });
        db.close();

    }

    public void updateNameNice(InstalledNodeModel installedNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, installedNodeModel.getNodesID());

        if (installedNodeModel.getNice_name_n() != null) {
            values.put(KEY_NICE_NAME_N, installedNodeModel.getNice_name_n());
        }

        db.update(TABLE, values, DbNode.KEY_NODE_ID + "= ?", new String[]{
                String.valueOf(installedNodeModel.getNodesID())
        });
        db.close(); // Closing database connection

    }

    public void update_detail(DetailNodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getChannel() != null) {
            values.put(KEY_CHANNEL, detailNodeModel.getChannel());
        }
        if (detailNodeModel.getStatus() != null || detailNodeModel.getStatus() != "ON" || detailNodeModel.getStatus() != "OFF") {
            values.put(KEY_STATUS, detailNodeModel.getStatus());
            //Log.d("DEBUG", "updateDetail Status: " +detailNodeModel.getStatus());
        }

        db.update(TABLE_NODE, values, DbNode.KEY_NODE_ID + "=? AND " + DbNode.KEY_CHANNEL + "=?", new String[]{
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection


    }

    public void update_detail_NiceName(DetailNodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getNice_name_d() != null) {
            values.put(KEY_NICE_NAME_D, detailNodeModel.getNice_name_d());
        }
        db.update(TABLE_NODE, values, DbNode.KEY_NODE_ID + "=? AND " + DbNode.KEY_CHANNEL + "=?", new String[]{
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection

    }

    public void update_detailSensor(DetailNodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getStatus_sensor() != null) {
            values.put(KEY_STATUS_SENSOR, detailNodeModel.getStatus_sensor());
            //Log.d("DEBUG", "updateDetail Status Sensor : " +detailNodeModel.getStatus_sensor());
        }
        if (detailNodeModel.getStatus_theft() != null) {
            values.put(KEY_STATUS_THEFT, detailNodeModel.getStatus_theft());
            //Log.d("DEBUG", "updateDetail Status Theft : " +detailNodeModel.getStatus_theft());
        }

        db.update(TABLE_NODE, values, DbNode.KEY_NODE_ID + "=? AND " + DbNode.KEY_CHANNEL + "=?", new String[]{
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection
        //Log.d("DEBUG", "updateDetailSensor: " + String.valueOf(detailNodeModel.getNode_id()) +" : "+
        //String.valueOf(detailNodeModel.getChannel()));

    }

    public ArrayList<InstalledNodeModel> getNodeList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE;

        ArrayList<InstalledNodeModel> nodeList = new ArrayList<InstalledNodeModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                InstalledNodeModel node = new InstalledNodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNodesID(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setNodes(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODES)));
                node.setName(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NAME)));
                node.setNice_name_n(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NICE_NAME_N)));
                node.setLocalip(cursor.getString(cursor.getColumnIndex(DbNode.KEY_LOCALIP)));
                node.setFwName(cursor.getString(cursor.getColumnIndex(DbNode.KEY_FWNAME)));
                node.setFwVersion(cursor.getString(cursor.getColumnIndex(DbNode.KEY_FWVERSION)));
                node.setOnline(cursor.getString(cursor.getColumnIndex(DbNode.KEY_ONLINE)));
                node.setIcon(cursor.getString(cursor.getColumnIndex(DbNode.KEY_ICON)));
                node.setAdding(cursor.getLong(cursor.getColumnIndex(DbNode.KEY_ADDING)));
                node.setSignal(cursor.getString(cursor.getColumnIndex(DbNode.KEY_SIGNAL)));
                node.setUptime(cursor.getString(cursor.getColumnIndex(DbNode.KEY_UPTIME)));
                node.setReset(cursor.getString(cursor.getColumnIndex(DbNode.KEY_RESET)));
                node.setOta(cursor.getString(cursor.getColumnIndex(DbNode.KEY_OTA)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        //Log.d("getlist", "getNodeList: " +cursor.getCount());
        cursor.close();
        db.close();
        return nodeList;
    }

    public List<SpinnerObject> getAllLabels() {
        List<SpinnerObject> labels = new ArrayList<SpinnerObject>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NODE;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list


        if (cursor.moveToFirst()) {
            do {
                //labels.add(cursor.getString(0)+","+ cursor.getString(3));
                labels.add(new SpinnerObject(cursor.getInt(0), cursor.getString(3)));
                //labels.add(cursor.getString(1));
                //labels.add(cursor.getString(3));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return labels;
    }

    public ArrayList<DetailNodeModel> getNodeDetail(String node_id, String Channel) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT * FROM " + DbNode.TABLE_NODE + " WHERE " + KEY_NODE_ID + " =? AND " + KEY_CHANNEL + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        ArrayList<DetailNodeModel> nodeList = new ArrayList<DetailNodeModel>();

        Cursor cursor = db.rawQuery(selectString, new String[]{
                String.valueOf(node_id),
                String.valueOf(Channel)});


        if (cursor.moveToFirst()) {
            do {
                DetailNodeModel node = new DetailNodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNode_id(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(DbNode.KEY_CHANNEL)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS)));
                node.setNice_name_d(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NICE_NAME_D)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(DbNode.KEY_SENSOR)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS_THEFT)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<DetailNodeModel> getNodeDetailList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT * FROM " + DbNode.TABLE_NODE;

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        ArrayList<DetailNodeModel> nodeList = new ArrayList<DetailNodeModel>();

        Cursor cursor = db.rawQuery(selectString, null);


        if (cursor.moveToFirst()) {
            do {
                DetailNodeModel node = new DetailNodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNode_id(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(DbNode.KEY_CHANNEL)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS)));
                node.setNice_name_d(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NICE_NAME_D)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(DbNode.KEY_SENSOR)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS_THEFT)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<InstalledNodeModel> getNodeListbyNode(String node_id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE + " WHERE " + KEY_NODE_ID + " =?";

        ArrayList<InstalledNodeModel> nodeList = new ArrayList<InstalledNodeModel>();

        Cursor cursor = db.rawQuery(selectQuery, new String[]{
                String.valueOf(node_id)});

        if (cursor.moveToFirst()) {
            do {
                InstalledNodeModel node = new InstalledNodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNodesID(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setNodes(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODES)));
                node.setName(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NAME)));
                node.setNice_name_n(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NICE_NAME_N)));
                node.setLocalip(cursor.getString(cursor.getColumnIndex(DbNode.KEY_LOCALIP)));
                node.setFwName(cursor.getString(cursor.getColumnIndex(DbNode.KEY_FWNAME)));
                node.setFwVersion(cursor.getString(cursor.getColumnIndex(DbNode.KEY_FWVERSION)));
                node.setOnline(cursor.getString(cursor.getColumnIndex(DbNode.KEY_ONLINE)));
                node.setIcon(cursor.getString(cursor.getColumnIndex(DbNode.KEY_ICON)));
                node.setAdding(cursor.getLong(cursor.getColumnIndex(DbNode.KEY_ADDING)));
                node.setSignal(cursor.getString(cursor.getColumnIndex(DbNode.KEY_SIGNAL)));
                node.setUptime(cursor.getString(cursor.getColumnIndex(DbNode.KEY_UPTIME)));
                node.setReset(cursor.getString(cursor.getColumnIndex(DbNode.KEY_RESET)));
                node.setOta(cursor.getString(cursor.getColumnIndex(DbNode.KEY_OTA)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<DashboardNodeModel> getNodeDetailDash() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_FAV +
                " favorite_node INNER JOIN " + TABLE_NODE +
                " detail_node ON favorite_node." + KEY_NICE_NAME_D + " = detail_node." + KEY_ID +
                " INNER JOIN " + TABLE +
                " installed_node ON installed_node." + KEY_NODE_ID + " = detail_node." + KEY_NODE_ID;


        ArrayList<DashboardNodeModel> nodeList = new ArrayList<>();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                DashboardNodeModel node = new DashboardNodeModel();
                node.setId(cursor.getInt(cursor.getColumnIndex(DbNode.KEY_ID)));
                node.setNice_name_d(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NICE_NAME_D)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(DbNode.KEY_SENSOR)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(DbNode.KEY_CHANNEL)));
                node.setNodeid(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS_THEFT)));
                node.setOnline(cursor.getString(cursor.getColumnIndex(DbNode.KEY_ONLINE)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return nodeList;

    }

    public ArrayList<DetailNodeModel> getNodeDetailID(String node_id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE + " installed_node INNER JOIN " + TABLE_NODE +
                " detail_node ON installed_node." + KEY_NODE_ID + " = detail_node." + KEY_NODE_ID +
                " WHERE detail_node." + KEY_NODE_ID + "=?";

        ArrayList<DetailNodeModel> nodeList = new ArrayList<DetailNodeModel>();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(node_id)});

        if (cursor.moveToFirst()) {
            do {
                DetailNodeModel node = new DetailNodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setFwName(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setNode_id(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(DbNode.KEY_CHANNEL)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS)));
                node.setNice_name_d(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NICE_NAME_D)));
                node.setName(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NAME)));
                node.setUptime(cursor.getString(cursor.getColumnIndex(DbNode.KEY_UPTIME)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(DbNode.KEY_SENSOR)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS_THEFT)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public boolean hasObject(InstalledNodeModel installedNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selectString = "SELECT * FROM " + DbNode.TABLE + " WHERE " + KEY_NODE_ID + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[]{String.valueOf(installedNodeModel.getNodesID())});

        boolean hasObject = false;
        if (cursor.moveToFirst()) {
            hasObject = true;

            //region if you had multiple records to check for, use this region.

            int count = 0;
            while (cursor.moveToNext()) {
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

    public boolean hasDetailObject(DetailNodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selectString = "SELECT * FROM " + DbNode.TABLE_NODE + " WHERE " + KEY_NODE_ID + " =? AND " + KEY_CHANNEL + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[]{
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())});

        boolean hasObject = false;
        if (cursor.moveToFirst()) {
            hasObject = true;

            //region if you had multiple records to check for, use this region.

            int count = 0;
            while (cursor.moveToNext()) {
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

    public int insertDurationNode(DurationModel durationModel) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, durationModel.getNodeId());
        values.put(KEY_CHANNEL, durationModel.getChannel());
        values.put(KEY_STATUS, durationModel.getStatus());
        values.put(KEY_TIMESTAMPS_ON, String.valueOf(durationModel.getTimeStampOn()));
        values.put(KEY_TIMESTAMPS_OFF, String.valueOf(durationModel.getTimeStampOff()));
        values.put(KEY_DURATION, String.valueOf(durationModel.getDuration()));


        long Id = db.insert(TABLE_NODE_DURATION, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertDur: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public void updateOff(DurationModel durationModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TIMESTAMPS_OFF, durationModel.getTimeStampOff());
        values.put(KEY_DURATION, durationModel.getDuration());

        db.update(TABLE_NODE_DURATION, values, DbNode.KEY_NODE_ID + "=? AND "
                + DbNode.KEY_CHANNEL + "=? AND "
                + DbNode.KEY_TIMESTAMPS_OFF + "=?", new String[]{
                String.valueOf(durationModel.getNodeId()),
                String.valueOf(durationModel.getChannel()),
                String.valueOf(0)
        });
        db.close(); // Closing database connection
        //Log.d("DEBUG", "updateDetail: " + String.valueOf(detailNodeModel.getNode_id()) +" : "+
        //        String.valueOf(detailNodeModel.getChannel()));

    }

    public ArrayList<DurationModel> getNodeDurationList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT * FROM " + DbNode.TABLE_NODE_DURATION;

        ArrayList<DurationModel> nodeList = new ArrayList<DurationModel>();

        Cursor cursor = db.rawQuery(selectString, null);

        if (cursor.moveToFirst()) {
            do {
                DurationModel node = new DurationModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setId(cursor.getInt(cursor.getColumnIndex(DbNode.KEY_ID)));
                node.setNodeId(cursor.getString(cursor.getColumnIndex(DbNode.KEY_NODE_ID)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(DbNode.KEY_CHANNEL)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(DbNode.KEY_STATUS)));
                node.setTimeStampOn(cursor.getLong(cursor.getColumnIndex(DbNode.KEY_TIMESTAMPS_ON)));
                node.setDuration(cursor.getLong(cursor.getColumnIndex(DbNode.KEY_DURATION)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }
    // ChartDatabase

    public ArrayList<DurationModel> getChartDurationList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT  duration_node." + KEY_NODE_ID +
                ", strftime( '%d/%m', duration_node." + KEY_TIMESTAMPS_ON + "/ 1000, 'unixepoch') AS act_time, " +
                " detail_node." + KEY_NICE_NAME_D + ", SUM(duration_node." + KEY_DURATION + " / 60 / 60) AS duration " +
                " FROM " + DbNode.TABLE_NODE_DURATION + " duration_node INNER JOIN " + TABLE_NODE +
                " detail_node ON  duration_node." + KEY_NODE_ID + " = detail_node." + KEY_NODE_ID +
                " GROUP BY act_time, duration_node." + KEY_NODE_ID;

        Log.d("DEBUG", "getChartDurationList: " + selectString);


        ArrayList<DurationModel> nodeList = new ArrayList<DurationModel>();

        Cursor cursor = db.rawQuery(selectString, null);

        if (cursor.moveToFirst()) {
            do {
                DurationModel node = new DurationModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNodeId(cursor.getString(0));
                node.setTimeStampOn(cursor.getLong(1));
                node.setNiceName(cursor.getString(2));
                node.setDuration(cursor.getLong(3));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<DetailNodeModel> getNodeDetailAll(String node_id) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT node_installed." + KEY_NODE_ID + ", node_installed." + KEY_CHANNEL +
                ", node_installed." + KEY_NICE_NAME_D + ", node_installed." + KEY_STATUS +
                ", node_installed." + KEY_STATUS_SENSOR + ", node_installed." + KEY_STATUS_THEFT + ", node_installed." + KEY_SENSOR +
                ", SUM(duration_node." + KEY_DURATION + ") as totaldur" +
                " FROM node_installed " + TABLE_NODE + " INNER JOIN " + TABLE_NODE_DURATION +
                " duration_node ON node_installed." + KEY_NODE_ID + " = duration_node." + KEY_NODE_ID +
                " AND node_installed." + KEY_CHANNEL + " = duration_node." + KEY_CHANNEL + " WHERE duration_node." + KEY_NODE_ID + "=?" +
                " GROUP BY node_installed." + KEY_NICE_NAME_D;


        //Log.d("DEBUG", "getNodeDetailAll: "+selectQuery);

        ArrayList<DetailNodeModel> nodeList = new ArrayList<DetailNodeModel>();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(node_id)});

        if (cursor.moveToFirst()) {
            do {
                DetailNodeModel node = new DetailNodeModel();
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

