package com.example.android.fleetdemo.database;

/**
 * Created by Azuga on 07-12-2017.
 */

import java.util.HashMap;

public interface DatabaseConfig {

    String getDatabaseName();

    int getDatabaseVersion();

    HashMap<Class<? extends DatabaseObject>, String> getTableMapping();
}