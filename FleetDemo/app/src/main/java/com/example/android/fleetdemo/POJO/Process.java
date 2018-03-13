
package com.example.android.fleetdemo.POJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Process {

    @SerializedName("processInstanceId")
    @Expose
    public String processInstanceId;
    @SerializedName("step")
    @Expose
    public List<Step> step = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Process() {
    }

    /**
     * 
     * @param processInstanceId
     * @param step
     */
    public Process(String processInstanceId, List<Step> step) {
        super();
        this.processInstanceId = processInstanceId;
        this.step = step;
    }

}
