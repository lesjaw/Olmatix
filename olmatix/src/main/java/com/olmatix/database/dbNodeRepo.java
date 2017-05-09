package com.olmatix.database;

/**
 * Created              : Lesjaw on 05/12/2016.
 * Date Created         : 05/12/2016 / 3:50 PM.
 * ===================================================
 * Package              : com.olmatix.database.
 * Project Name         : Olmatix.
 * Copyright            : Copyright @ 2017 Olmatix.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.olmatix.model.AllSceneModel;
import com.olmatix.model.DashboardNodeModel;
import com.olmatix.model.DetailNodeModel;
import com.olmatix.model.DurationModel;
import com.olmatix.model.InstalledNodeModel;
import com.olmatix.model.SceneDetailModel;
import com.olmatix.model.SceneModel;
import com.olmatix.model.SpinnerObject;

import java.util.ArrayList;
import java.util.List;

import static com.olmatix.database.dbNode.*;

public class dbNodeRepo {
    private com.olmatix.database.dbHelper dbHelper;


    public dbNodeRepo(Context context) {
        dbHelper = new dbHelper(context);
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

    public int insertDbMqtt(dbNode DbNode) {
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
        values.put(KEY_STATUS_THEFT, detailNodeModel.getStatus_theft());
        values.put(KEY_STATUS_TEMP, detailNodeModel.getStatus_temp());
        values.put(KEY_STATUS_HUM, detailNodeModel.getStatus_hum());

        long node_Id = db.insert(TABLE_NODE, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertDetail: " + String.valueOf(KEY_NODE_ID));

        return (int) node_Id;
    }

    public void insertFavNode(DashboardNodeModel dashboardNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_ID_NODE_DETAIL, dashboardNodeModel.getNice_name_d());
        values.put(KEY_NODE_ID, dashboardNodeModel.getNodeid());


        db.insert(TABLE_FAV, null, values);

        db.close(); // Closing database connection

        return;
    }

    public void insertDbScene(SceneDetailModel sceneModel) {


        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_SCENE_NAME, sceneModel.getSceneName());
        if (sceneModel.getHour() != 0) {
            values.put(KEY_HOURS, sceneModel.getHour());
        }
        if (sceneModel.getMin() != 0) {
            values.put(KEY_MINS, sceneModel.getMin());
        }
        if (sceneModel.getMonday() != null) {
            values.put(KEY_MON, sceneModel.getMonday());
        }
        if (sceneModel.getSunday() != null) {
            values.put(KEY_SUN, sceneModel.getSunday());
        }
        if (sceneModel.getTuesday() != null) {
            values.put(KEY_TUE, sceneModel.getTuesday());
            //Log.d("DEBUG", "updateNode: " +installedNodeModel.getFwName());
        }
        if (sceneModel.getWednesday() != null) {
            values.put(KEY_WED, sceneModel.getWednesday());
        }
        if (sceneModel.getThursday() != null) {
            values.put(KEY_THUR, sceneModel.getThursday());

        }
        if (sceneModel.getFriday() != null) {
            values.put(KEY_FRI, sceneModel.getFriday());
        }
        if (sceneModel.getSaturday() != null) {
            values.put(KEY_SAT, sceneModel.getSaturday());
        }
        if (sceneModel.getNode_id() != null) {
            values.put(KEY_NODE_ID, sceneModel.getNode_id());
        }

        db.update(TABLE_SCENE_DETAIL, values, dbNode.KEY_SCENE_NAME + "= ?", new String[]{
                String.valueOf(sceneModel.getSceneName())
        });
        db.close();

    }

    public int insertScene(AllSceneModel sceneModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SCENE_NAME, sceneModel.getSceneName());
        values.put(KEY_SCENE_TYPE, sceneModel.getSceneType());
        values.put(KEY_SENSOR, sceneModel.getSensor());

        long Id = db.insert(TABLE_SCENE, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public ArrayList<SceneDetailModel> getAllSceneList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + dbNode.TABLE_SCENE_DETAIL;

        ArrayList<SceneDetailModel> nodeList = new ArrayList<SceneDetailModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SceneDetailModel node = new SceneDetailModel();

                node.setSceneName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SCENE_NAME)));
                node.setSceneType(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_SCENE_TYPE)));
                node.setHour(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_HOURS)));
                node.setMin(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_MINS)));
                node.setMonday(cursor.getString(cursor.getColumnIndex(dbNode.KEY_MON)));
                node.setSunday(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SUN)));
                node.setTuesday(cursor.getString(cursor.getColumnIndex(dbNode.KEY_TUE)));
                node.setWednesday(cursor.getString(cursor.getColumnIndex(dbNode.KEY_WED)));
                node.setThursday(cursor.getString(cursor.getColumnIndex(dbNode.KEY_THUR)));
                node.setFriday(cursor.getString(cursor.getColumnIndex(dbNode.KEY_FRI)));
                node.setSaturday(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SAT)));
                node.setLocation(cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCATION)));
                node.setNode_id(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<SceneModel> getSceneList(String sceneName) {


        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_SCENE + " WHERE " + KEY_SCENE_NAME + " =? ";

        ArrayList<SceneModel> nodeList = new ArrayList<SceneModel>();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(sceneName)});

        if (cursor.moveToFirst()) {
            do {
                SceneModel node = new SceneModel();
                node.setId(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<AllSceneModel> getScene() {


        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_SCENE ;

        ArrayList<AllSceneModel> nodeList = new ArrayList<AllSceneModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AllSceneModel node = new AllSceneModel();
                node.setId(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID)));
                node.setSceneName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SCENE_NAME)));
                node.setSceneType(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_SCENE_TYPE)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public int insertSceneDetail(SceneDetailModel sceneDetailModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, sceneDetailModel.getScene_id());
        values.put(KEY_PATH, sceneDetailModel.getPath());
        values.put(KEY_COMMAND, sceneDetailModel.getCommand());
        values.put(KEY_SCENE_NAME,sceneDetailModel.getSceneName());
        values.put(KEY_HOURS,sceneDetailModel.getHour());
        values.put(KEY_MINS,sceneDetailModel.getMin());
        values.put(KEY_MON,sceneDetailModel.getMonday());
        values.put(KEY_SUN,sceneDetailModel.getSunday());
        values.put(KEY_TUE,sceneDetailModel.getTuesday());
        values.put(KEY_WED,sceneDetailModel.getWednesday());
        values.put(KEY_THUR,sceneDetailModel.getThursday());
        values.put(KEY_FRI,sceneDetailModel.getFriday());
        values.put(KEY_SAT,sceneDetailModel.getSaturday());
        values.put(KEY_LOCATION,sceneDetailModel.getLocation());
        values.put(KEY_NODE_ID,sceneDetailModel.getNode_id());

        long Id = db.insert(TABLE_SCENE_DETAIL, null, values);


        db.close();
        Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public ArrayList<SceneDetailModel> getSceneDetailList() {


        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_SCENE_DETAIL;

        ArrayList<SceneDetailModel> nodeList = new ArrayList<SceneDetailModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SceneDetailModel node = new SceneDetailModel();
                node.setScene_id(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_SCENE_ID)));
                node.setSceneName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SCENE_NAME)));
                node.setNode_id(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        //Log.d("getlist", "getNodeList: " +cursor.getCount());
        cursor.close();
        db.close();
        return nodeList;
    }

    public int deleteSceneDetailList(SceneDetailModel sceneDetailModel) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        return db.delete(TABLE_SCENE_DETAIL,  dbNode.KEY_SCENE_ID + " = ?", new String[] { String.valueOf(sceneDetailModel.getScene_id()) });
    }

    public ArrayList<SceneDetailModel> getAllScene() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT "+ TABLE_SCENE_DETAIL + ".*, "+TABLE_NODE+"."
                + dbNode.KEY_NODE_ID +","+TABLE_NODE+"."
                + dbNode.KEY_NICE_NAME_D +","+TABLE_NODE+"."+dbNode.KEY_CHANNEL+", "+TABLE_SCENE+"."
                + dbNode.KEY_SCENE_NAME+" FROM " + TABLE_SCENE_DETAIL
                +" JOIN " + TABLE_NODE
                +" ON " + TABLE_SCENE_DETAIL+"."+ dbNode.KEY_PATH +" = "+ TABLE_NODE+"." + dbNode.KEY_NODE_ID
                +" JOIN " + TABLE_SCENE
                +" ON " + TABLE_SCENE_DETAIL+"."+ dbNode.KEY_SCENE_ID +" = "+ TABLE_SCENE+"." + dbNode.KEY_SCENE_TYPE;

        //Log.d("DEBUG", "getAllScene: " + selectQuery);

        ArrayList<SceneDetailModel> nodeList = new ArrayList<SceneDetailModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SceneDetailModel node = new SceneDetailModel();

                node.setScene_id(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_SCENE_ID)));
                node.setPath(cursor.getString(cursor.getColumnIndex(dbNode.KEY_PATH)));
                node.setCommand(cursor.getString(cursor.getColumnIndex(dbNode.KEY_COMMAND)));
                node.setNodeId(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNiceName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setSceneType(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_SCENE_TYPE)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public void deleteNode(String node_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(TABLE, dbNode.KEY_NODE_ID + "= ?", new String[]{
                String.valueOf(node_Id)});

        db.delete(TABLE_NODE, dbNode.KEY_NODE_ID + "= ?", new String[]{
                String.valueOf(node_Id)});

        db.delete(TABLE_FAV, dbNode.KEY_ID_NODE_DETAIL + "= ?", new String[]{
                String.valueOf(node_Id)});

        Log.d("DEBUG", "deleteNode: " + String.valueOf(node_Id));

        db.close(); // Closing database connection
    }

    public void deleteFav(String node_Id) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // It's a good practice to use parameter ?, instead of concatenate string
        db.delete(TABLE_FAV, dbNode.KEY_ID_NODE_DETAIL + "= ?", new String[]{
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
        db.update(TABLE, values, dbNode.KEY_NODE_ID + "= ?", new String[]{
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

        db.update(TABLE, values, dbNode.KEY_NODE_ID + "= ?", new String[]{
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

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " + dbNode.KEY_CHANNEL + "=?", new String[]{
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
        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " + dbNode.KEY_CHANNEL + "=?", new String[]{
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

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " + dbNode.KEY_CHANNEL + "=?", new String[]{
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection
    }

    public void update_detailSensorTheft(DetailNodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getStatus_theft() != null) {
            values.put(KEY_STATUS_THEFT, detailNodeModel.getStatus_theft());
            //Log.d("DEBUG", "updateDetail Status Theft : " +detailNodeModel.getStatus_theft());
        }

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " + dbNode.KEY_CHANNEL + "=?", new String[]{
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection
        //Log.d("DEBUG", "updateDetailSensor: " + String.valueOf(detailNodeModel.getNode_id()) +" : "+
        //String.valueOf(detailNodeModel.getChannel()));

    }

    public void update_detailSensorTemp(DetailNodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getStatus_temp() != null) {
            values.put(KEY_STATUS_TEMP, detailNodeModel.getStatus_temp());
            Log.d("DEBUG", "updateDetail Status Sensor : " +detailNodeModel.getStatus_temp());
        }

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " + dbNode.KEY_CHANNEL + "=?", new String[]{
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection
    }

    public void update_detailSensorHum(DetailNodeModel detailNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NODE_ID, detailNodeModel.getNode_id());

        if (detailNodeModel.getStatus_hum() != null) {
            values.put(KEY_STATUS_HUM, detailNodeModel.getStatus_hum());
            Log.d("DEBUG", "updateDetail Status Sensor : " +detailNodeModel.getStatus_hum());
        }

        db.update(TABLE_NODE, values, dbNode.KEY_NODE_ID + "=? AND " + dbNode.KEY_CHANNEL + "=?", new String[]{
                String.valueOf(detailNodeModel.getNode_id()),
                String.valueOf(detailNodeModel.getChannel())
        });
        db.close(); // Closing database connection
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
                node.setNodesID(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNodes(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES)));
                node.setName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.setNice_name_n(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_N)));
                node.setLocalip(cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP)));
                node.setFwName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME)));
                node.setFwVersion(cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWVERSION)));
                node.setOnline(cursor.getString(cursor.getColumnIndex(dbNode.KEY_ONLINE)));
                node.setIcon(cursor.getString(cursor.getColumnIndex(dbNode.KEY_ICON)));
                node.setAdding(cursor.getLong(cursor.getColumnIndex(dbNode.KEY_ADDING)));
                node.setSignal(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SIGNAL)));
                node.setUptime(cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.setReset(cursor.getString(cursor.getColumnIndex(dbNode.KEY_RESET)));
                node.setOta(cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        //Log.d("getlist", "getNodeList: " +cursor.getCount());
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<InstalledNodeModel> getNodeListReset(String nodesID) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE;

        ArrayList<InstalledNodeModel> nodeList = new ArrayList<InstalledNodeModel>();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                InstalledNodeModel node = new InstalledNodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNodesID(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNodes(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES)));
                node.setName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.setNice_name_n(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_N)));
                node.setLocalip(cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP)));
                node.setFwName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME)));
                node.setFwVersion(cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWVERSION)));
                node.setOnline(cursor.getString(cursor.getColumnIndex(dbNode.KEY_ONLINE)));
                node.setIcon(cursor.getString(cursor.getColumnIndex(dbNode.KEY_ICON)));
                node.setAdding(cursor.getLong(cursor.getColumnIndex(dbNode.KEY_ADDING)));
                node.setSignal(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SIGNAL)));
                node.setUptime(cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.setReset(cursor.getString(cursor.getColumnIndex(dbNode.KEY_RESET)));
                node.setOta(cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA)));
                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        //Log.d("getlist", "getNodeList: " +cursor.getCount());
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<SpinnerObject> getAllLabels() {
        ArrayList<SpinnerObject> labels = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_NODE;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SpinnerObject node = new SpinnerObject();

                //labels.add(cursor.getString(0)+","+ cursor.getString(3));
                node.setDatabaseId(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setDatabaseValue(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                labels.add(node);

                //labels.add(cursor.getString(2));
                //labels.add(cursor.getString(3));

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return labels;
    }


    public ArrayList<DetailNodeModel> getNodeDetail(String node_id, String Channel) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT * FROM " + dbNode.TABLE_NODE + " WHERE " + KEY_NODE_ID + " =? AND " + KEY_CHANNEL + " =?";

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
                node.setNode_id(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setNice_name_d(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_THEFT)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public ArrayList<DetailNodeModel> getNodeDetailList() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selectString = "SELECT * FROM " + dbNode.TABLE_NODE;

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        ArrayList<DetailNodeModel> nodeList = new ArrayList<DetailNodeModel>();

        Cursor cursor = db.rawQuery(selectString, null);

        if (cursor.moveToFirst()) {
            do {
                DetailNodeModel node = new DetailNodeModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setNode_id(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setNice_name_d(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_THEFT)));

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
                node.setNodesID(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNodes(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODES)));
                node.setName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.setNice_name_n(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_N)));
                node.setLocalip(cursor.getString(cursor.getColumnIndex(dbNode.KEY_LOCALIP)));
                node.setFwName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWNAME)));
                node.setFwVersion(cursor.getString(cursor.getColumnIndex(dbNode.KEY_FWVERSION)));
                node.setOnline(cursor.getString(cursor.getColumnIndex(dbNode.KEY_ONLINE)));
                node.setIcon(cursor.getString(cursor.getColumnIndex(dbNode.KEY_ICON)));
                node.setAdding(cursor.getLong(cursor.getColumnIndex(dbNode.KEY_ADDING)));
                node.setSignal(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SIGNAL)));
                node.setUptime(cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.setReset(cursor.getString(cursor.getColumnIndex(dbNode.KEY_RESET)));
                node.setOta(cursor.getString(cursor.getColumnIndex(dbNode.KEY_OTA)));
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
                " detail_node ON favorite_node." + KEY_ID_NODE_DETAIL+ " = detail_node." + KEY_ID +
                " INNER JOIN " + TABLE +
                " installed_node ON installed_node." + KEY_NODE_ID + " = detail_node." + KEY_NODE_ID;


        ArrayList<DashboardNodeModel> nodeList = new ArrayList<>();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                DashboardNodeModel node = new DashboardNodeModel();
                node.setId(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID)));
                node.setId_node_detail(cursor.getString(cursor.getColumnIndex(dbNode.KEY_ID_NODE_DETAIL)));
                node.setNice_name_d(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
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
                node.setFwName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setNode_id(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setNice_name_d(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NICE_NAME_D)));
                node.setName(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NAME)));
                node.setUptime(cursor.getString(cursor.getColumnIndex(dbNode.KEY_UPTIME)));
                node.setSensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_SENSOR)));
                node.setStatus_sensor(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_SENSOR)));
                node.setStatus_theft(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS_THEFT)));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public boolean hasObject(InstalledNodeModel installedNodeModel) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selectString = "SELECT * FROM " + dbNode.TABLE + " WHERE " + KEY_NODE_ID + " =?";

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
        String selectString = "SELECT * FROM " + dbNode.TABLE_NODE + " WHERE " + KEY_NODE_ID + " =? AND " + KEY_CHANNEL + " =?";

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

        db.update(TABLE_NODE_DURATION, values, dbNode.KEY_NODE_ID + "=? AND "
                + dbNode.KEY_CHANNEL + "=? AND "
                + dbNode.KEY_TIMESTAMPS_OFF + "=?", new String[]{
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

        String selectString = "SELECT * FROM " + dbNode.TABLE_NODE_DURATION;

        ArrayList<DurationModel> nodeList = new ArrayList<DurationModel>();

        Cursor cursor = db.rawQuery(selectString, null);

        if (cursor.moveToFirst()) {
            do {
                DurationModel node = new DurationModel();
                //ArrayList<String> node = new ArrayList<>();
                node.setId(cursor.getInt(cursor.getColumnIndex(dbNode.KEY_ID)));
                node.setNodeId(cursor.getString(cursor.getColumnIndex(dbNode.KEY_NODE_ID)));
                node.setChannel(cursor.getString(cursor.getColumnIndex(dbNode.KEY_CHANNEL)));
                node.setStatus(cursor.getString(cursor.getColumnIndex(dbNode.KEY_STATUS)));
                node.setTimeStampOn(cursor.getLong(cursor.getColumnIndex(dbNode.KEY_TIMESTAMPS_ON)));
                node.setDuration(cursor.getLong(cursor.getColumnIndex(dbNode.KEY_DURATION)));

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
                " FROM " + dbNode.TABLE_NODE_DURATION + " duration_node INNER JOIN " + TABLE_NODE +
                " detail_node ON  duration_node." + KEY_NODE_ID + " = detail_node." + KEY_NODE_ID +
                " GROUP BY act_time, duration_node." + KEY_NODE_ID;

        //Log.d("DEBUG", "getChartDurationList: " + selectString);


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
                ", node_installed." + KEY_STATUS_SENSOR + ", node_installed." + KEY_STATUS_THEFT + ", node_installed." + KEY_SENSOR  +
                ", node_installed." + KEY_STATUS_TEMP + ", node_installed." + KEY_STATUS_HUM +
                ", SUM(duration_node." + KEY_DURATION + ") as totaldur"  +
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
                node.setStatus_temp(cursor.getString(7));
                node.setStatus_hum(cursor.getString(8));
                node.setDuration(cursor.getString(9));

                nodeList.add(node);

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public int insertLog(dbNode DbNode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LOG, DbNode.getLog());

        long Id = db.insert(TABLE_LOG, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public List<String> getLogAlarm() {
        List<String> nodeList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_LOG;

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                nodeList.add(cursor.getString(1));

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public void deleteLog() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_LOG, null, null);
        db.close(); // Closing database connection
    }

    public List<String> getLogMqtt() {
        List<String> nodeList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_MQTT +" ORDER BY "+KEY_ID +" DESC limit "+30;


        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                nodeList.add(cursor.getString(1)+ "\n"+ cursor.getString(2));

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return nodeList;
    }

    public void deleteMqtt() {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE_MQTT, null, null);
        db.close(); // Closing database connection
    }
}

