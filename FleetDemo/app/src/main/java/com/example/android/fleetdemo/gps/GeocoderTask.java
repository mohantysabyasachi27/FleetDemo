package com.example.android.fleetdemo.gps;

import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.HandlerThread;

import com.example.android.fleetdemo.FleetApplication;
import com.example.android.fleetdemo.framework.Logger;

import java.util.List;
import java.util.Locale;

public class GeocoderTask extends HandlerThread {

    private static final String TAG = "GeocoderTask";
    private static GeocoderTask geocoderTask;
    private static Handler handler;

    private GeocoderTask() {
        super(TAG);
        start();
        handler = new Handler(getLooper());
    }

    public static synchronized GeocoderTask getInstance() {
        if (geocoderTask == null)
            geocoderTask = new GeocoderTask();

        return geocoderTask;
    }

    public void geocodeLatLng(final double latitude, final double longitude,
                              final GeocoderTaskListener listener) {
        if (listener == null)
            return;

        if (Geocoder.isPresent()) {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        List<Address> addresses = new Geocoder(FleetApplication.getAppContext(), Locale.US)
                                .getFromLocation(latitude, longitude, 1);
                        if (addresses != null && addresses.size() > 0) {
                            GpsAddress gpsAddress = new GpsAddress(addresses.get(0));
                            Logger.d(TAG, "LatLng Address received : " + gpsAddress);
                            listener.onAddressReceived(gpsAddress);
                        } else {
                            listener.onAddressReceived(null);
                        }
                    } catch (Exception e) {
                        Logger.e(TAG, "Error while Reverse Geocoding the latlng.", e);
                        listener.onAddressReceived(null);
                    }
                }
            });
        } else {
            listener.onAddressReceived(null);
        }
    }


    public void cleanup() {
        quit();
        geocoderTask = null;
        handler = null;
    }

    public interface GeocoderTaskListener {
        void onAddressReceived(GpsAddress address);
    }
}