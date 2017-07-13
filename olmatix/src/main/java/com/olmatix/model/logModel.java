package com.olmatix.model;

/**
 * Created by USER on 05/07/2017.
 */

public class logModel {

    private  int id;
    private String NiceName, status, nodeid, channel,on, off;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNiceName() {
        return NiceName;
    }

    public void setNiceName(String niceName) {
        NiceName = niceName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNodeid() {
        return nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getOn() {
        return on;
    }

    public void setOn(String on) {
        this.on = on;
    }

    public String getOff() {
        return off;
    }

    public void setOff(String off) {
        this.off = off;
    }
}
