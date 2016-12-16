package com.olmatix.model;

import java.util.Calendar;

/**
 * Created              : Rahman on 12/6/2016.
 * Date Created         : 12/6/2016 / 11:11 PM.
 * ===================================================
 * Package              : com.olmatix.model.
 * Project Name         : Olmatix.
 * Copyright            : Copyright @ 2016 Indogamers.
 */
public class Installed_NodeModel {
    private int id;
    private String node_id;
    private String nodes;
    private String name;
    private String nice_name_n;
    private String localip;
    private String fwName;
    private String fwVersion;
    private String online;
    private String signal;
    private String icon;

    private String uptime;
    private String reset;
    private String ota;
    private Long adding;

    public Installed_NodeModel() {
    }

    public Installed_NodeModel(String node_id, String nodes, String name, String nice_name_n, String localip, String fwName,
                               String fwVersion, String icon, Long adding, String online, String signal, String uptime,
                               String reset, String ota) {

        this.node_id = node_id;
        this.nodes = nodes;
        this.name = name;
        this.nice_name_n = nice_name_n;
        this.localip = localip;
        this.fwName = fwName;
        this.fwVersion = fwVersion;
        this.icon = icon;
        this.adding = adding;
        this.online = online;
        this.signal = signal;
        this.uptime = uptime;
        this.reset = reset;
        this.ota = ota;

    }


    public int getNid() {
        return id;
    }

    public void setNid(int id) {
        this.id = id;
    }

    public String getNodesID() {
        return node_id;
    }

    public void setNodesID(String node_id) {
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

    public String getNice_name_n() {
        return nice_name_n;
    }

    public void setNice_name_n(String nice_name_n) {
        this.nice_name_n = nice_name_n;
    }

    public String getLocalip() {
        return localip;
    }

    public void setLocalip(String localip) {
        this.localip = localip;
    }

    public String getFwName() {
        return fwName;
    }

    public void setFwName(String fwName) {
        this.fwName = fwName;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Long getAdding() {
        return adding;
    }

    public void setAdding(Long adding) {
        this.adding = adding;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getReset() {
        return reset;
    }

    public void setReset(String reset) {
        this.reset = reset;
    }

    public String getOta() {
        return ota;
    }

    public void setOta(String ota) {
        this.ota = ota;
    }


}
