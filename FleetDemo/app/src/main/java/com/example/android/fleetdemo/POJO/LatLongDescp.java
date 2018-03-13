package com.example.android.fleetdemo.POJO;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Azuga on 09-03-2018.
 */

public class LatLongDescp implements Parcelable {

    @SerializedName("latitude")
    @Expose
    public double latitude;
    @SerializedName("longitute")
    @Expose
    public double longitute;
    @SerializedName("name")
    @Expose
    public String name;

    public LatLongDescp(double latitude, double logitude, String name) {
        this.latitude = latitude;
        this.longitute = logitude;
        this.name = name;
    }

    public LatLongDescp(){

    }

    protected LatLongDescp(Parcel in) {
        latitude = in.readDouble();
        longitute = in.readDouble();
        name = in.readString();
    }

    public static final Creator<LatLongDescp> CREATOR = new Creator<LatLongDescp>() {
        @Override
        public LatLongDescp createFromParcel(Parcel in) {
            return new LatLongDescp(in);
        }

        @Override
        public LatLongDescp[] newArray(int size) {
            return new LatLongDescp[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLogitude() {
        return longitute;
    }

    public void setLogitude(double logitude) {
        this.longitute = logitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LatLongDescp{" +
                "latitude='" + latitude + '\'' +
                ", logitude='" + longitute + '\'' +
                ", name=" + name +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitute);
        dest.writeString(name);
    }
}
