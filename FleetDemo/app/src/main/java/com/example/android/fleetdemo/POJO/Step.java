
package com.example.android.fleetdemo.POJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Step {

    @SerializedName("lat")
    @Expose
    public String lat;
    @SerializedName("lon")
    @Expose
    public String lon;
    @SerializedName("process")
    @Expose
    public Boolean process;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Step() {
    }

    /**
     * 
     * @param process
     * @param lon
     * @param lat
     */
    public Step(String lat, String lon, Boolean process) {
        super();
        this.lat = lat;
        this.lon = lon;
        this.process = process;
    }

}
