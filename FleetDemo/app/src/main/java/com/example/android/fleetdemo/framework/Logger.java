package com.example.android.fleetdemo.framework;

/**
 * Created by Azuga on 07-03-2018.
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.android.fleetdemo.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public final class Logger extends HandlerThread implements LoggerImpl {
    public static final String LOG_DIR = "/logs/";
    public static final int DEFAULT_LOG_LEVEL = LOG_LEVEL_DEF.ERROR.value + LOG_LEVEL_DEF.WARN.value + LOG_LEVEL_DEF.INFO.value + LOG_LEVEL_DEF.DEBUG.value;
    private static final int BUFFER_LIMIT = 90;
    private static final String LOG_FILE_PREFIX = "Azuga_Log_";
    private static final String LOG_FILE_SUFFIX = ".log";
    private static final long MAX_FILE_SIZE = 5000000;  //Approx 5MB
    private static final String FILE_NAME_PATTERN = "yyyy-MM-dd";
    private static Logger instance;
    private int LOG_LEVEL;
    private long logEndDate;
    private ArrayList<String> logBuffer;
    private boolean isSettingModified = false;
    private Handler logHandler;
    private File logFile;
    private boolean errorDuringFileSetup = false;

    private FileWriter fileWriter;

    private Logger() {
        super("Logger");
        logBuffer = new ArrayList<>(25);

        start();
        logHandler = new Handler(getLooper());
    }

    public synchronized static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }

        return instance;
    }

    public static void i(String tag, String msg) {
        getInstance().logMessage(Log.INFO, tag, msg, null);
    }

    public static void i(String tag, String msg, Throwable error) {
        getInstance().logMessage(Log.INFO, tag, msg, error);
    }

    public static void d(String tag, String msg) {
        getInstance().logMessage(Log.DEBUG, tag, msg, null);
    }

    public static void d(String tag, String msg, Throwable error) {
        getInstance().logMessage(Log.DEBUG, tag, msg, error);
    }

    public static void e(String tag, String msg) {
        getInstance().logMessage(Log.ERROR, tag, msg, null);
    }

    public static void e(String tag, String msg, Throwable error) {
        getInstance().logMessage(Log.ERROR, tag, msg, error);
    }

    public static void w(String tag, String msg) {
        getInstance().logMessage(Log.WARN, tag, msg, null);
    }

    public static void w(String tag, String msg, Throwable error) {
        getInstance().logMessage(Log.WARN, tag, msg, error);
    }

    public int getLogLevel() {
        return LOG_LEVEL;
    }

    public void setLogLevel(int level) {
        LOG_LEVEL = level;
    }





    public void logMessage(final int level, final String tag, final String msg, final Throwable throwable) {
        logHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    logInternal(level, tag, msg, throwable);
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    System.out.println("Received out of memory while processing logs.");
                }
            }
        });
    }

    private void logInternal(int level, String tag, String msg, Throwable throwable) {
        if (!BuildConfig.DEBUG) {
            //Report the logs to Crash Analytics
            //  Crashlytics.log("[" + tag + "] " + msg + (throwable != null ? "\n" + Log.getStackTraceString(throwable) : ""));
        }

        String levelIdentifier = "I";
        switch (LOG_LEVEL_DEF.getLevelDef(level)) {
            case INFO:
                //if (BuildConfig.DEBUG)
                Log.i(tag, msg, throwable);
                levelIdentifier = "I";
                break;
            case WARN:
                //if (BuildConfig.DEBUG)
                Log.w(tag, msg, throwable);
                levelIdentifier = "W";
                break;
            case ERROR:
                //if (BuildConfig.DEBUG)
                Log.e(tag, msg, throwable);
                levelIdentifier = "E";
                break;
            case DEBUG:
                //if (BuildConfig.DEBUG)
                Log.d(tag, msg, throwable);
                levelIdentifier = "D";
                break;
        }

        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("[").append(levelIdentifier).append("]");
        msgBuilder.append("[").append(tag).append("] ");
        msgBuilder.append(msg);

        if (throwable != null) {
            msgBuilder.append("\n").append(Log.getStackTraceString(throwable));
        }

        logBuffer.add(msgBuilder.toString());

    }




    public enum LOG_LEVEL_DEF {
        ERROR(1),
        WARN(2),
        INFO(4),
        DEBUG(8);

        public final int value;

        LOG_LEVEL_DEF(int level) {
            this.value = level;
        }

        private static LOG_LEVEL_DEF getLevelDef(int level) {
            switch (level) {
                case Log.DEBUG:
                    return DEBUG;
                case Log.ERROR:
                    return ERROR;
                case Log.WARN:
                    return WARN;
                default:
                    return INFO;
            }
        }

        @Override
        public String toString() {
            return name();
        }
    }

}

