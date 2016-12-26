package com.olmatix.model;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class Dashboard_NodeModel {

    private  int id;
    private String nice_name_d;
    private String sensor;
    private String status;
    private String channel;
    private String nodeid;
    private String status_sensor;
    private String status_theft;
    private String online;
    private String onduration;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNice_name_d() {
        return nice_name_d;
    }

    public void setNice_name_d(String nice_name_d) {
        this.nice_name_d = nice_name_d;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getNodeid() {
        return nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public String getStatus_sensor() {
        return status_sensor;
    }

    public void setStatus_sensor(String status_sensor) {
        this.status_sensor = status_sensor;
    }

    public String getStatus_theft() {
        return status_theft;
    }

    public void setStatus_theft(String status_theft) {
        this.status_theft = status_theft;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getOnduration() {
        return onduration;
    }

    public void setOnduration(String onduration) {
        this.onduration = onduration;
    }
}
