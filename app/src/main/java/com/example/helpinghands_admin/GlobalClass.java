package com.example.helpinghands_admin;
import android.app.Application;

public class GlobalClass extends Application{

    private String station;


    public String getStation() {

        return station;
    }

    public void setStation(String aName) {

        station = aName;

    }

}