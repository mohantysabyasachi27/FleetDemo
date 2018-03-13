package com.example.android.fleetdemo.gps;

import android.location.Location;

public interface GpsListener {

	void onGpsPointReceived(Location location);

	void onError(String error);
}
