package com.example.android.fleetdemo.database;


import com.example.android.fleetdemo.POJO.UserTask;

/**
 * Enum which act as central repo of all database tables. Each entry has table name and representing class.
 * 
 * @author manish
 */
public enum DatabaseTablesEnum {

	USER_TASK(UserTask.TABLE_NAME, UserTask.class);


	private final String tableName;
	private final Class<? extends DatabaseObject> tableClass;

	private DatabaseTablesEnum(String name, Class<? extends DatabaseObject> tableClass) {
		tableName = name;
		this.tableClass = tableClass;
	}

	public String getTableName() {
		return tableName;
	}

	public Class<? extends DatabaseObject> getTableClass() {
		return tableClass;
	}

	public static DatabaseTablesEnum getTableEnum(String name) {
		if (USER_TASK.getTableName().equals(name)) {
			return USER_TASK;
		}
		return null;
	}

}