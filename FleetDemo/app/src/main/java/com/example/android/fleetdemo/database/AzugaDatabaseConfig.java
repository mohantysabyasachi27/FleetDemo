package com.example.android.fleetdemo.database;

import com.example.android.fleetdemo.POJO.UserTask;

import java.util.HashMap;

/**
 * Created by Azuga on 27-02-2018.
 */

public class AzugaDatabaseConfig implements DatabaseConfig {
    // Database Versions...
    public static final int DB_VERSION_2_0 = 11;
    private static final String RUC_DB_NAME = "azugaRUC.db";

    @Override
    public String getDatabaseName() {
        return RUC_DB_NAME;
    }

    @Override
    public int getDatabaseVersion() {
        return DB_VERSION_2_0;
    }

    @Override
    public HashMap<Class<? extends DatabaseObject>, String> getTableMapping() {
        HashMap<Class<? extends DatabaseObject>, String> mapping = new HashMap<>();
        mapping.put(UserTask.class, UserTask.TABLE_NAME);
        return mapping;
    }
}