package com.example.android.fleetdemo.framework;

/**
 * Created by Azuga on 07-03-2018.
 */

public interface LoggerImpl {
    void logMessage(int level, String tag, String msg, Throwable throwable);
}
