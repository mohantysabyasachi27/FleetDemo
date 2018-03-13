
package com.example.android.fleetdemo.POJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Field {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("variableType")
    @Expose
    private String variableType;
    @SerializedName("defaultValue")
    @Expose
    private String defaultValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", variableType='" + variableType + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
