package com.olmatix.model;

/**
 * Created by Rahman on 12/28/2016.
 */

public class SpinnerObject {

    private  int databaseId;
    private String databaseValue;

    public SpinnerObject ( int databaseId , String databaseValue ) {
        this.databaseId = databaseId;
        this.databaseValue = databaseValue;
    }

    public int getId () {
        return databaseId;
    }

    public String getValue () {
        return databaseValue;
    }

    @Override
    public String toString () {
        return databaseValue;
    }

}
