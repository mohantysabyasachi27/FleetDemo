
package com.example.android.fleetdemo.POJO;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.fleetdemo.database.DatabaseObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserTask implements DatabaseObject {

    public static final String TABLE_NAME = "UserTask";
    public static final String NAME = "NAME";
    public static final String ID = "ID";
    public static final String ASSIGNEE = "ASSIGNEE";
    public static final String P_DEF_ID = "PROCESSDEFINITIONID";
    public static final String P_ID = "PROCESSINSTANCEID";



    @SerializedName("id")
    @Expose
    public String id;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("assignee")
    @Expose
    public String assignee;

    @SerializedName("processDefinitionId")
    @Expose
    public String processDefinitionId;

    @SerializedName("processInstanceId")
    @Expose
    public String processInstanceId;




    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateTableSql() {
        StringBuffer query = new StringBuffer("CREATE TABLE IF NOT EXISTS ").append(TABLE_NAME).append(" (");
        query.append(ID).append(" TEXT, ");
        query.append(NAME).append(" TEXT, ");
        query.append(ASSIGNEE).append(" TEXT, ");
        query.append(P_DEF_ID).append(" TEXT, ");
        query.append(P_ID).append(" TEXT, ");
        query.append("PRIMARY KEY (").append(ID).append("));");
        return query.toString();

    }

    @Override
    public void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public DatabaseObject buildObjectFromCursor(Cursor cursor) {
        UserTask userTask = new UserTask();
        userTask.id = cursor.getString(cursor.getColumnIndexOrThrow(ID));
        userTask.name = cursor.getString(cursor.getColumnIndexOrThrow(NAME));
        userTask.assignee = cursor.getString(cursor.getColumnIndexOrThrow(ASSIGNEE));
        userTask.processDefinitionId = cursor.getString(cursor.getColumnIndexOrThrow(P_DEF_ID));
        userTask.processInstanceId = cursor.getString(cursor.getColumnIndexOrThrow(P_ID));
        return userTask;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID,id);
        contentValues.put(NAME,name);
        contentValues.put(ASSIGNEE,assignee);
        contentValues.put(P_DEF_ID,processDefinitionId);
        contentValues.put(P_ID,processInstanceId);
        return contentValues;
    }

    @Override
    public String[] getPrimaryKeys() {
        return new String[] { ID };
    }

    @Override
    public Object[] getValuesForPrimaryKeys() {
        return new Object[] { id };
    }

    @Override
    public boolean isCacheData() {
        return false;
    }


    @java.lang.Override
    public java.lang.String toString() {
        return "UserTask{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", assignee='" + assignee + '\'' +
                ", processDefinitionId='" + processDefinitionId + '\'' +
                ", processInstanceId='" + processInstanceId + '\'' +
                '}';
    }
}
