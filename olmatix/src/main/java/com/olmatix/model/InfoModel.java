package com.olmatix.model;

/**
 * Created by Rahman on 12/27/2016.
 */

public class InfoModel {

    public static final int mBUTTON = 0;
    public static final int mLOCATION = 1;
    private int mDataTypes[] = {mBUTTON, mLOCATION};
    private String[] ButtonInfo = {"Button 1","Button 2"};
    private String[] LocationInfo = {"Location 1","Location 2", "Location 3"};


    public InfoModel() {
    }



    public String[] getButtonInfo() {
        return ButtonInfo;
    }

    public void setButtonInfo(String[] buttonInfo) {
        ButtonInfo = buttonInfo;
    }

    public String[] getLocationInfo() {
        return LocationInfo;
    }

    public void setLocationInfo(String[] locationInfo) {
        LocationInfo = locationInfo;
    }
}
