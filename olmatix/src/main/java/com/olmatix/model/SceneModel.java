package com.olmatix.model;

/**
 * Created by Lesjaw on 01/01/2017.
 */

public class SceneModel {

    private int id, sceneType;
    private String sceneName, date, time, sensor;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSceneType() {
        return sceneType;
    }

    public void setSceneType(int sceneType) {
        this.sceneType = sceneType;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }
}