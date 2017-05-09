package com.olmatix.model;

/**
 * Created by USER on 10/05/2017.
 */

public class SpinnerObjectDash {

    private  int databaseId;
    private String databaseValue;

    public SpinnerObjectDash ( int databaseId , String databaseValue ) {
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
