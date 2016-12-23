package com.olmatix.model;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class Dashboard_NodeModel {

    private  int id;
    private String nice_name_d;
    private String sensor;
    private String status;

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
}
