package com.olmatix.model;

/**
 * Created by Rahman on 12/28/2016.
 */

public class Duration_Model {
    int id;
    String nodeId, channel, status;
    Long timeStampOn, timeStampOff, duration;

    public Duration_Model() {
    }

    public Duration_Model(int id, String nodeId, String channel, String status, Long timeStampOn, Long timeStampOff) {
        this.id = id;
        this.nodeId = nodeId;
        this.channel = channel;
        this.status = status;
        this.timeStampOn = timeStampOn;
        this.timeStampOff = timeStampOff;
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
