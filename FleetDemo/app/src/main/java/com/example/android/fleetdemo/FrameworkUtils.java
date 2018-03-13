package com.example.android.fleetdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.android.fleetdemo.database.DatabaseService;
import com.example.android.fleetdemo.database.DatabaseTablesEnum;
import com.example.android.fleetdemo.framework.AppStateListener;
import com.example.android.fleetdemo.framework.Logger;
import com.example.android.fleetdemo.framework.UIService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;

/**
 * Created by Azuga on 27-02-2018.
 */

public class FrameworkUtils {

    public static boolean isEmptyOrWhitespace(String string) {
        if (string == null || "".equals(string.trim())) {
            return true;
        }

        return false;
    }

    public static boolean isDataConnectionOn() {
        try {
            ConnectivityManager connectionManager = (ConnectivityManager) FleetApplication.
                    getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            return connectionManager.getActiveNetworkInfo().isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    private static RequestQueue volleyRequestQueue = null;

    public static synchronized RequestQueue getVolleyRequestQueue() {
        if (volleyRequestQueue == null) {
            volleyRequestQueue = Volley.newRequestQueue(FleetApplication.getAppContext());
        }

        return volleyRequestQueue;
    }

    public static void resetVolleyRequestQueue() {
        try {
            if (volleyRequestQueue != null) {
                volleyRequestQueue.stop();
                volleyRequestQueue = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface AllStateVolleyCallBack {
        void onResponse(JsonArray object);
        void onResponse(JsonObject object);
        void onResponse(String response);
        void onError(VolleyError error);
    }

    public static  <T> ArrayList<T> retrieveInfoFromDB(Class<T> clazz, DatabaseTablesEnum tableName, String whereClause) {
        return (ArrayList<T>) DatabaseService.getInstance().
                read(tableName.getTableClass(), whereClause);
    }

    static final String TAG_ERROR_DIALOG_FRAGMENT="errorDialog";

    public static boolean checkPlayServiceAvailability(Activity activity, int requestCode) {
        final Activity localActivity;
        if (activity != null) {
            localActivity = activity;
        } else {
            localActivity = UIService.getInstance().getActivity();
        }

        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(FleetApplication.getAppContext());
        if (errorCode != ConnectionResult.SUCCESS) {
            Logger.e("FrameworkUtils", "Google Play Service is not available. Error Code : " + errorCode);
            try {
                if (localActivity != null && AppStateListener.isAppVisible()) {
                    if (GoogleApiAvailability.getInstance().isUserResolvableError(errorCode)) {
                        Logger.e("FrameworkUtils", "Google Play Service is not available. Error is UserRecoverable. Showing prompt to the user.");
                        if (localActivity instanceof FragmentActivity) {
                            GoogleApiAvailability.getInstance().showErrorDialogFragment(activity, errorCode, requestCode, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Logger.e("FrameworkUtils", "User Cancelled the Play Service action. Lets Finish the activity.");
                                    //Finish the activity here..
                                    UIService.getInstance().showAlert(R.string.error, R.string.play_service_required_msg, R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                }
                            });
                        } else {
                            GoogleApiAvailability.getInstance().getErrorDialog(activity, errorCode, requestCode, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Logger.e("FrameworkUtils", "User Cancelled the Play Service action. Lets Finish the activity.");
                                    //Finish the activity here..
                                    UIService.getInstance().showAlert(R.string.error, R.string.play_service_required_msg, R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                }
                            }).show();
                        }
                    } else {
                        Logger.e("FrameworkUtils", "Google Play Service is not available. Error is not UserRecoverable. Killing the app");
                        UIService.getInstance().showAlert(R.string.error, R.string.play_service_required_msg, R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                    }
                } else {
                    GoogleApiAvailability.getInstance().showErrorNotification(FleetApplication.getAppContext(), errorCode);
                }
            } catch (Exception e) {
                Logger.e("FrameworkUtils", "Error while showing error notification for play service availability.", e);
            }

            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("deprecation")
    public static boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String providers = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (isEmptyOrWhitespace(providers)) {
                return false;
            }
            return providers.contains(LocationManager.GPS_PROVIDER);
        } else {
            final int locationMode;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            switch (locationMode) {
                case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                    return true;
                case Settings.Secure.LOCATION_MODE_OFF:
                default:
                    return false;
            }
        }
    }



}
