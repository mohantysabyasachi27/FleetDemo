package com.example.android.fleetdemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Azuga on 27-02-2018.
 */

public class AzugaPreferences {

    public static final String PREFS_NAME = "sharedPrefsFile";
    private SharedPreferences settings;

    private static AzugaPreferences instance;

    private AzugaPreferences(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static AzugaPreferences getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new AzugaPreferences(context);
        }
        return instance;
    }
    public boolean resetUser() {
        // invalidate cache

        SharedPreferences.Editor editor = settings.edit();
        editor.remove("LOGGED_IN");
        editor.remove("LOGGED_IN_USER");
        editor.remove("LOC_IN_USER");
        editor.remove("LOCATION_FOUND");

        // editor.clear();
        return editor.commit();
    }

    public boolean setIsUserLoggedIn(boolean loggedInState) {
        return settings.edit().putBoolean("LOGGED_IN", loggedInState).commit();
    }


    public boolean isUserLoggedIn() {
        return settings.getBoolean("LOGGED_IN", false);
    }

    public boolean setUserLogged(String user) {
        return settings.edit().putString("LOGGED_IN_USER", user).commit();
    }


    public String getLoggedInUser() {
        return settings.getString("LOGGED_IN_USER", "");
    }

    public void setUserLocation(boolean b) {
         settings.edit().putBoolean("LOC_IN_USER", b).commit();
    }

    public boolean getUserLocation() {
        return settings.getBoolean("LOC_IN_USER", false);
    }

    public void isArrayLocationFound(boolean b) {
        settings.edit().putBoolean("LOCATION_FOUND", b).commit();
    }

    public boolean isArrayLocationFound() {
        return settings.getBoolean("LOCATION_FOUND", false);
    }
}
