package com.olmatix.model;

/**
 * Created by Rahman on 12/28/2016.
 */

public class DurationModel {
    int id;
    String nodeId, channel, status, niceName;
    Long timeStampOn, timeStampOff, duration;

    public DurationModel() {
    }

    public DurationModel(int id, String nodeId, String channel, String status, String niceName, Long timeStampOn, Long timeStampOff, Long duration) {
        this.id = id;
        this.nodeId = nodeId;
        this.channel = channel;
        this.status = status;
        this.niceName = niceName;
        this.timeStampOn = timeStampOn;
        this.timeStampOff = timeStampOff;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
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

    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    public Long getTimeStampOn() {
        return timeStampOn;
    }

    public void setTimeStampOn(Long timeStampOn) {
        this.timeStampOn = timeStampOn;
    }

    public Long getTimeStampOff() {
        return timeStampOff;
    }

    public void setTimeStampOff(Long timeStampOff) {
        this.timeStampOff = timeStampOff;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
