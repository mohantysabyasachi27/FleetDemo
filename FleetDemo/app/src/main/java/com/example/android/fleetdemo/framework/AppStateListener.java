package com.example.android.fleetdemo.framework;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


/**
 * Created by Azuga on 07-03-2018.
 */

public class AppStateListener implements Application.ActivityLifecycleCallbacks {
    private static AppStateListener instance = null;

    private UIProtocol protocolCompliantActivity = null;
    private Activity nonProtocolCompliantActivity = null;
    private Activity topMostActivity = null;

    private static int resumeCount;
    private static int pauseCount;
    private static int startCount;
    private static int stopCount;

    private AppStateListener() {
    }

    public synchronized static AppStateListener getInstance() {
        if (instance == null) {
            instance = new AppStateListener();
        }

        return instance;
    }

    public static boolean isAppVisible() {
        return ((startCount > stopCount));
    }

    public static boolean isAppActive() {
        return isAppVisible() && !isDialogOnScreen() && (getInstance().topMostActivity != null);
    }

    public static boolean isDialogOnScreen() {
        return (resumeCount <= pauseCount);
    }

    public boolean isUIActive() {
        return topMostActivity != null;
    }

    UIProtocol getUIProtocolCompliantActivity() {
        return protocolCompliantActivity;
    }

    Activity getActivity() {
        if (protocolCompliantActivity != null) {
            return protocolCompliantActivity;
        }

        return nonProtocolCompliantActivity;
    }

    Activity getTopMostActivity() {
        return topMostActivity;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Logger.d("AppStateListener", "onActivityStarted");
        ++startCount;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Logger.d("AppStateListener", "onActivityResumed");
        ++resumeCount;
        topMostActivity = activity;
        if (activity instanceof UIProtocol) {
            protocolCompliantActivity = (UIProtocol) activity;
        } else {
            nonProtocolCompliantActivity = activity;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Logger.d("AppStateListener", "onActivityPaused");
        ++pauseCount;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Logger.d("AppStateListener", "onActivityStopped");
        ++stopCount;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Logger.d("AppStateListener", "onActivityCreated");
        if (protocolCompliantActivity == null && nonProtocolCompliantActivity == null) {
            resumeCount = 0;
            pauseCount = 0;
            startCount = 0;
            stopCount = 0;
        }

        if (activity instanceof UIProtocol) {
            protocolCompliantActivity = (UIProtocol) activity;
        } else {
            nonProtocolCompliantActivity = activity;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }
    @Override
    public void onActivityDestroyed(Activity activity) {
        Logger.d("AppStateListener", "onActivityDestroyed");
        if (activity == protocolCompliantActivity) {
            protocolCompliantActivity = null;
        } else if (activity == nonProtocolCompliantActivity) {
            nonProtocolCompliantActivity = null;
        }
        if (topMostActivity == activity) {
            topMostActivity = null;
        }
    }
}