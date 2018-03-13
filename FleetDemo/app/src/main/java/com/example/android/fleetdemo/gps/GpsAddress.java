package com.example.android.fleetdemo.gps;

import android.location.Address;

import com.example.android.fleetdemo.FrameworkUtils;

public class GpsAddress {

    private String city;
    private String state;
    private String zip;
    private String country;
    private String formattedAddress;

    public GpsAddress() {
    }

    public GpsAddress(Address address) {
        if (address != null) {
            this.city = address.getSubAdminArea();
            this.state = address.getAdminArea();
            this.zip = address.getPostalCode();
            this.country = address.getCountryCode();

            StringBuilder buffer = new StringBuilder();
            if (address.getMaxAddressLineIndex() == -1) {
                if (!FrameworkUtils.isEmptyOrWhitespace(city)) {
                    buffer.append(city).append(", ");
                }

                if (!FrameworkUtils.isEmptyOrWhitespace(state)) {
                    buffer.append(state).append(", ");
                }

                if (!FrameworkUtils.isEmptyOrWhitespace(country)) {
                    buffer.append(country);
                }
            } else {
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }

                    buffer.append(address.getAddressLine(i));
                }
            }

            this.formattedAddress = buffer.toString();
        }
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public String getCountry() {
        return country;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    @Override
    public String toString() {
        return "GpsAddress [city=" + city + ", state=" + state + ", zip=" + zip + ", country=" + country
                + ", formattedAddress=" + formattedAddress + "]";
    }
}