package com.olmatix.model;

/**
 * Created by Lesjaw on 17/12/2016.
 */

public class Favorite_NodeModel {

    private int FavId;
    private String FavNodeID;
    private String FavChannel;
    private String FavNodeType;

    public int getFavId() {
        return FavId;
    }

    public void setFavId(int favId) {
        FavId = favId;
    }

    public String getFavNodeID() {
        return FavNodeID;
    }

    public void setFavNodeID(String favNodeID) {
        FavNodeID = favNodeID;
    }

    public String getFavChannel() {
        return FavChannel;
    }

    public void setFavChannel(String favChannel) {
        FavChannel = favChannel;
    }

    public String getFavNodeType() {
        return FavNodeType;
    }

    public void setFavNodeType(String favNodeType) {
        FavNodeType = favNodeType;
    }
}
