package com.olmatix.model;

/**
 * Created by android on 12/9/2016.
 */

public class DetailNodeModel {

    private  int id;
    private String node_id,channel, status, nice_name_d,uptime, name, sensor, status_sensor, fwName, status_theft,  status_temp, status_hum, duration, status_jarak, status_range;


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

    public String getFwName() {
        return fwName;
    }

    public void setFwName(String fwName) {
        this.fwName = fwName;
    }

    public String getStatus_theft() {
        return status_theft;
    }

    public void setStatus_theft(String status_theft) {
        this.status_theft = status_theft;
    }

    public String getStatus_temp() {
        return status_temp;
    }

    public void setStatus_temp(String status_temp) {
        this.status_temp = status_temp;
    }

    public String getStatus_hum() {
        return status_hum;
    }

    public void setStatus_hum(String status_hum) {
        this.status_hum = status_hum;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStatus_jarak() {
        return status_jarak;
    }

    public void setStatus_jarak(String status_jarak) {
        this.status_jarak = status_jarak;
    }

    public String getStatus_range() {
        return status_range;
    }

    public void setStatus_range(String status_range) {
        this.status_range = status_range;
    }
}
