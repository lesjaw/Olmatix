package com.olmatix.model;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class Dashboard_NodeModel {

    private  int id;
    private String node_id;
    private String channel;
    private String status;
    private String nice_name_d;
    private String uptime;
    private String name;
    private String sensor;
    private String status_sensor;
    private String fwName;
    private String status_theft;
    private String timestamps;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNode_id() {
        return node_id;
    }

    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNice_name_d() {
        return nice_name_d;
    }

    public void setNice_name_d(String nice_name_d) {
        this.nice_name_d = nice_name_d;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
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

    public String getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(String timestamps) {
        this.timestamps = timestamps;
    }

    public String getFwName() {
        return fwName;
    }

    public void setFwName(String fwName) {
        this.fwName = fwName;
    }
}
