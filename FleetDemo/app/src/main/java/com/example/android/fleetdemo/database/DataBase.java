package com.example.android.fleetdemo.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.fleetdemo.FleetApplication;

public class DataBase extends SQLiteOpenHelper {

    private static DataBase dataBase;

    private DataBase(String dbName, int dbVersion) {
        super(FleetApplication.getAppContext(), dbName, null, dbVersion);
    }

    public static synchronized DataBase getInstance(String dbName, int dbVersion) {
        if (dataBase == null)
            dataBase = new DataBase(dbName, dbVersion);

        return dataBase;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (Class<? extends DatabaseObject> tableClass : DatabaseService.dbTables.keySet()) {
            try {
                DatabaseObject databaseObject = tableClass.newInstance();
                String query = databaseObject.getCreateTableSql();
                if (query != null)
                    db.execSQL(query);
            } catch (Exception e) {
                Log.e("DataBase", "Error getting object", e);
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (Class<? extends DatabaseObject> tableClass : DatabaseService.dbTables.keySet()) {
            try {
                tableClass.newInstance().onDatabaseUpgrade(db, oldVersion, newVersion);
            } catch (Exception e) {
                Log.e("DataBase", "Error updating object ", e);
            }
        }
        // Create Tables
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        Log.e("DataBase", "Database downgraded. This should have never happen...");
    }
}