package com.olmatix.model;


public class Subscription {


    public int node_id;
    public String nodes;
    public String name;
    public String localip;
    public String fwname;
    public int fwversion;
    public int online;
    public int signal;
    public String uptime;
    public int reset;
    public String ota;


    public int getNode_id() {
        return node_id;
    }

    public void setNode_id(int node_id) {
        this.node_id = node_id;
    }

    public String getNodes() {
        return nodes;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalip() {
        return localip;
    }

    public void setLocalip(String localip) {
        this.localip = localip;
    }

    public String getFwname() {
        return fwname;
    }

    public void setFwname(String fwname) {
        this.fwname = fwname;
    }

    public int getFwversion() {
        return fwversion;
    }

    public void setFwversion(int fwversion) {
        this.fwversion = fwversion;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public int getReset() {
        return reset;
    }

    public void setReset(int reset) {
        this.reset = reset;
    }

    public String getOta() {
        return ota;
    }

    public void setOta(String ota) {
        this.ota = ota;
    }
}
