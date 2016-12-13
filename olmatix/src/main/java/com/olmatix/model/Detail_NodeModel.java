package com.olmatix.model;

/**
 * Created by android on 12/9/2016.
 */

public class Detail_NodeModel {

    private  int id;
    private String node_id;
    private String channel;
    private String status;
    private String nice_name;
    private String uptime;
    private String name;


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

    public String getNice_name() {
        return nice_name;
    }

    public void setNice_name(String nice_name) {
        this.nice_name = nice_name;
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
}
