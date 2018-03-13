package com.example.android.fleetdemo;

import android.app.Application;
import android.content.Context;

import com.example.android.fleetdemo.framework.AppStateListener;

/**
 * Created by Azuga on 27-02-2018.
 */

public class FleetApplication extends Application {

    public static final String LOG_TAG = FleetApplication.class.getSimpleName();
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        registerActivityLifecycleCallbacks(AppStateListener.getInstance());
    }

    public static Context getAppContext() {
        return appContext;
    }
}
