package com.example.android.fleetdemo.gps;

import android.Manifest;
import android.app.Activity;
import android.location.Location;
import android.os.Looper;

import com.example.android.fleetdemo.FleetApplication;
import com.example.android.fleetdemo.FrameworkUtils;
import com.example.android.fleetdemo.framework.AppStateListener;
import com.example.android.fleetdemo.framework.Logger;
import com.example.android.fleetdemo.framework.PermissionHelper;
import com.example.android.fleetdemo.framework.UIService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.CopyOnWriteArraySet;

public class GpsService implements  LocationListener  {

	public final static int PLAY_SERVICE_GPS_REQUEST_CODE = 29842;
	private static final String TAG = "GpsService";

	private LocationRequest locationRequest;
	private FusedLocationProviderClient fusedLocationProviderClient;
	private static GpsService instance = null;
	private CopyOnWriteArraySet<GpsListener> gpsListeners;
	private long updateInterval = 5000;
	private boolean isRequetingUpdates;
	private LocationSettingsRequest locationSettingsRequest;


	private GpsService() {
		gpsListeners = new CopyOnWriteArraySet<>();
	}

	public static GpsService getInstance() {
		synchronized (TAG) {
			if (instance == null) {
				instance = new GpsService();
			}
		}

		return instance;
	}

	public void addGpsListener(GpsListener listener) {
		if (listener == null)
			return;

		gpsListeners.add(listener);
		if (gpsListeners.size() > 0)
			start();
	}

	public void removeGpsListener(GpsListener listener) {
		if (listener == null)
			return;

		gpsListeners.remove(listener);
		if (gpsListeners.size() == 0)
			stop();
	}

	public synchronized void start() {
		Logger.d(TAG, "Starting the service");
		if (!FrameworkUtils.checkPlayServiceAvailability(null,PLAY_SERVICE_GPS_REQUEST_CODE) || isRequetingUpdates) {
			Logger.d(TAG, "Play service is not avavilabe or Update is already running. " + isRequetingUpdates);
			return;
		}

		if (locationRequest == null) {
			// Google Play service is available let request for the Locations..
			locationRequest = LocationRequest.create();
			// Right now get every 5second.
			locationRequest.setInterval(updateInterval);
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
			locationRequest.setFastestInterval(1000);
		}

		if (fusedLocationProviderClient == null) {
			fusedLocationProviderClient =LocationServices.getFusedLocationProviderClient(FleetApplication.getAppContext());
		}

		if (locationSettingsRequest == null) {
			locationSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
					.setAlwaysShow(true).build();
		}
		checkLocationSettings();

	}

	public synchronized void stop() {
		Logger.d(TAG, "Stop the service.");
		if (fusedLocationProviderClient == null) {
			Logger.d(TAG, "Client is null return");
			return;
		}

		endUpdates();


		locationRequest = null;
	}

	public void onPermissionDenied() {
		sendError("Permission Denied");
		stop();
	}

//	@Override
//	public void onConnected(Bundle connectionHint) {
//		Logger.d(TAG, "Connected to GoogleApiClient.");
//		checkLocationSettings();
//	}

	private void checkLocationSettings() {

		Logger.d(TAG, "Check Location settings start.");
		SettingsClient settingsClient = LocationServices.getSettingsClient(FleetApplication.getAppContext());
		Task<LocationSettingsResponse> result =settingsClient.checkLocationSettings(locationSettingsRequest);
		result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
			@Override
			public void onComplete(Task<LocationSettingsResponse> task) {
				try {
					LocationSettingsResponse response = task.getResult(ApiException.class);
					beginUpdates();

				} catch (ApiException exception) {
					switch (exception.getStatusCode()) {
						case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
							// Location settings are not satisfied. But could be fixed by showing the user
							// a dialog.
							Logger.d(TAG, "Location Service Error, Resolution Required.");
							Activity activity = UIService.getInstance().getActivity();
							ResolvableApiException resolvable = null;
							if (UIService.getInstance().getActivity() != null && AppStateListener.isAppVisible()) {

								try {
									 resolvable= (ResolvableApiException) exception;
									resolvable.startResolutionForResult(activity, PLAY_SERVICE_GPS_REQUEST_CODE);
								} catch (Exception e) {
									Logger.e(TAG, "Location Service Error, Can't show dialog", e);
									sendError(resolvable.getMessage());
								}
							} else {
								Logger.e(TAG, "Location Service Error, Activity is null.");
								sendError(resolvable.getMessage());
							}
							break;
						case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
							resolvable= (ResolvableApiException) exception;
							Logger.d(TAG, "Location Service Error, Resolution not found.");
							sendError(resolvable.getMessage());
							break;
					}
				}
			}
		});

	}

	@SuppressWarnings("MissingPermission")
	private void beginUpdates() {
		Logger.d(TAG, "beginUpdates Start.");
		if (fusedLocationProviderClient != null && !isRequetingUpdates) {
			Logger.d(TAG, "beginUpdates condition matched going to request.");
			//Check if we have the GPS permission
			try {
				if (PermissionHelper.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
						|| PermissionHelper.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
					fusedLocationProviderClient.requestLocationUpdates(LocationRequest.create(), locationCallback
					, Looper.getMainLooper());

				} else {
					Logger.e(TAG, "GPS service doesn't has the permission.");
					sendError("Location permission denied.");
					PermissionHelper.requestAllPermissions(null, 0);
					isRequetingUpdates = false;
				}
			} catch (Exception e) {
				Logger.e(TAG, "Error while starting location updates.", e);
				isRequetingUpdates = false;
			}
		}
	}

	private LocationCallback locationCallback = new LocationCallback() {

		@Override
		public void onLocationResult(LocationResult locationResult) {
			super.onLocationResult(locationResult);
			Location lastLocation = locationResult.getLastLocation();
			onLocationChanged(lastLocation);
		}
	};

	private void endUpdates() {
		Logger.d(TAG, "endUpdates Start.");
		if (fusedLocationProviderClient != null && isRequetingUpdates) {
			Logger.d(TAG, "endUpdates condition matched going to end updates.");
			try {
				fusedLocationProviderClient.removeLocationUpdates(locationCallback);
				isRequetingUpdates = false;
			} catch (Exception e) {
				Logger.e(TAG, "Error while ending location updates.", e);
			}
		}
	}



	@Override
	public void onLocationChanged(Location paramLocation) {
		Logger.d(TAG, "Gps Point received. " + paramLocation);
		if (gpsListeners != null && gpsListeners.size() > 0) {
			sendGpsPoint(paramLocation);
		} else {
			stop();
		}
	}

	private void sendError(String err) {
		Logger.d(TAG, "Error occured. " + err);
		if (gpsListeners != null) {
			for (GpsListener listener : gpsListeners) {
				listener.onError(err);
			}
		}
	}

	private void sendGpsPoint(Location location) {
		Logger.d(TAG, "Gps Point received. " + location);
		if (gpsListeners != null) {
			for (GpsListener listener : gpsListeners) {
				listener.onGpsPointReceived(location);
			}
		}
	}


}