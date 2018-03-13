package com.example.android.fleetdemo.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.android.fleetdemo.FrameworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * Base class that will handle all database operation Insert, Update, Read, Delete.
 *
 * @author manish
 */
public class DatabaseService {

	private static String TAG = "DatabaseService";

	static HashMap<Class<? extends DatabaseObject>, String> dbTables;
	private static Hashtable<String, ArrayList<DatabaseObject>> cachedData;
	private DataBase dataBaseHelper;
	private static DatabaseService databaseService;
	private static DatabaseConfig databaseConfig;
	private SQLiteDatabase db;
	private DatabaseListener databaseListener;
	private boolean isBulkUpdate = false;

	public static void init(DatabaseConfig config) {
		if (config == null)
			throw new RuntimeException("DatabaseConfig can not be null. Please initialize with valid config.");

		databaseConfig = config;
	}

	private DatabaseService() {
		dataBaseHelper = DataBase.getInstance(databaseConfig.getDatabaseName(), databaseConfig.getDatabaseVersion());
		dbTables = databaseConfig.getTableMapping();
		if (dbTables == null) {
			dbTables = new HashMap<>();
		}

		cachedData = new Hashtable<>();
		checkDatabase();
	}

	public String getDatabaseName() {
		return databaseConfig.getDatabaseName();
	}

	/**
	 * Return the singleton DatabaseService object to the caller.
	 *
	 * @return DatabaseService Object
	 */
	public static synchronized DatabaseService getInstance() {
		if (databaseConfig == null) {
			throw new RuntimeException("Database is not yet initialized. Please call init.");
		}

		if (databaseService == null) {
			databaseService = new DatabaseService();
		}

		return databaseService;
	}

	/**
	 * Closes the Database. Should be called when application is getting exited. onDestroy of the main activity.
	 */
	public void closeDatabase() {
		if (db != null) {
			db.close();
			db = null;
		}
	}

	/**
	 * Set a listener if callback is required..
	 *
	 * @param listener {@link DatabaseListener}
	 */
	@SuppressWarnings("unused")
	public void setDatabaseListener(DatabaseListener listener) {
		this.databaseListener = listener;
	}

	private boolean checkDatabase() {
		if (db != null)
			return true;

		try {
			db = dataBaseHelper.getWritableDatabase();
			return true;
		} catch (SQLException e) {
			Log.e(TAG, "Error while writing the Object.", e);
			return false;
		}
	}

	/**
	 * Insert or Update the database objects in the sqlite db.
	 *
	 * @param databaseObjects List of {@link DatabaseObject}
	 */
	public synchronized void insertOrUpdate(List<? extends DatabaseObject> databaseObjects) {
		isBulkUpdate = true;
		for (DatabaseObject databaseObject : databaseObjects) {
			insertOrUpdate(databaseObject);
		}
		isBulkUpdate = false;
		fireDatabaseUpdate();
	}

	/**
	 * Insert or update a single database object.
	 *
	 * @param databaseObject {@link DatabaseObject}
	 * @return rowsCount
	 */
	public synchronized long insertOrUpdate(DatabaseObject databaseObject) {
		if (databaseObject == null)
			return -1;

		if (databaseObject.isCacheData()) {
			ArrayList<DatabaseObject> data = cachedData.get(databaseObject.getTableName());
			if (data == null) {
				data = new ArrayList<>();
				cachedData.put(databaseObject.getTableName(), data);
			}

			if (data.contains(databaseObject)) {
				// Don't think its necessary
				data.remove(data.indexOf(databaseObject));
			}
			data.add(databaseObject);
			return 1;
		}

		String whereClause = getWhereClause(databaseObject);
		int count = count(databaseObject.getClass(), whereClause);
		long result = -1;
		if (checkDatabase()) {
			try {
				db.beginTransaction();
				if (count > 0) {
					result = db.update(databaseObject.getTableName(), databaseObject.getContentValues(), whereClause, null);
				} else {
					result = db.insert(databaseObject.getTableName(), null, databaseObject.getContentValues());
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			fireDatabaseUpdate();
		}
		return result;
	}

	public synchronized long update(Class<? extends DatabaseObject> tableClass, ContentValues contentValues, String whereClause, String[] whereArgs) {
		if (tableClass == null || contentValues == null || FrameworkUtils.isEmptyOrWhitespace(whereClause)) {
			return -1;
		}

		long result = -1;
		if (checkDatabase()) {
			try {
				db.beginTransaction();
				result = db.update(dbTables.get(tableClass), contentValues, whereClause, whereArgs);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			fireDatabaseUpdate();
		}

		return result;
	}

	/**
	 * Return the count of rows in given table. You can also specify the where clause.
	 *
	 * @param tableClass  class for which to fetch the data. Class should extend Database object
	 * @param whereClause SQL Where Clause without 'WHERE' keyword
	 * @return Count of rows in table.
	 */
	public synchronized int count(Class<? extends DatabaseObject> tableClass, String whereClause) {
		int result = -1;
		Cursor cursor = null;
		try {
			DatabaseObject object = tableClass.newInstance();
			if (object.isCacheData()) {
				ArrayList<DatabaseObject> data = cachedData.get(object.getTableName());
				if (data != null)
					result = data.size();
			} else if (checkDatabase()) {
				StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ").append(dbTables.get(tableClass));
				if (whereClause != null && !"".equals(whereClause.trim())) {
					query.append(" WHERE ").append(whereClause);
				}

				cursor = db.rawQuery(query.toString(), null);
				if (cursor != null && cursor.moveToFirst()) {
					result = cursor.getInt(0);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error while getting the count. " + dbTables.get(tableClass));
		} finally {
			closeResources(cursor);
		}
		return result;
	}

	/**
	 * Read the given table and return a list of {@link DatabaseObject}
	 *
	 * @param tableClass  class for which to fetch the data. Class should extend Database object
	 * @param whereClause SQL Where Clause without 'WHERE' keyword
	 * @return List of {@link DatabaseObject}
	 */
	public synchronized <T extends DatabaseObject> ArrayList<T> read(Class<T> tableClass,
																	 String whereClause) {
		return read(tableClass, whereClause, null);
	}

	/**
	 * Read the given table and return a list of {@link DatabaseObject}
	 *
	 * @param tableClass  class for which to fetch the data. Class should extend Database object
	 * @param whereClause SQL Where Clause without 'WHERE' keyword
	 * @param orderBy     SQL like ORDER BY attribute without 'ORDER BY' keyword.
	 * @return List of {@link DatabaseObject}
	 */
	public synchronized <T extends DatabaseObject> ArrayList<T> read(Class<T> tableClass,
																	 String whereClause, String orderBy) {
		return read(tableClass, whereClause, orderBy, null);
	}

	/**
	 * @param tableClass  class for which to fetch the data. Class should extend Database object
	 * @param whereClause SQL Where Clause without 'WHERE' keyword
	 * @param orderBy     SQL like ORDER BY attribute without 'ORDER BY' keyword.
	 * @param limit       LIMIT pass null if no limit specified.
	 * @return List of {@link DatabaseObject}
	 */
	public synchronized <T extends DatabaseObject> ArrayList<T> read(Class<T> tableClass, String whereClause, String orderBy, String limit) {
		ArrayList<T> data = new ArrayList<>();
		Cursor cursor = null;
		try {
			@SuppressWarnings("unchecked")
			DatabaseObject<T> object = tableClass.newInstance();
			if (object.isCacheData()) {
				// No where class return complete data...
				@SuppressWarnings("unchecked")
				ArrayList<T> tempData = (ArrayList<T>) cachedData.get(object.getTableName());
				if (tempData != null)
					data.addAll(tempData);
			} else if (checkDatabase()) {
				cursor = db.query(dbTables.get(tableClass), null, whereClause, null, null, null, orderBy, limit);
				if (cursor != null && cursor.moveToFirst()) {
					while (!cursor.isAfterLast()) {
						data.add(object.buildObjectFromCursor(cursor));
						cursor.moveToNext();
					}
				}
			}
		} catch (InstantiationException ie) {
			Log.e(TAG, "Error while reading the Object.", ie);
		} catch (IllegalAccessException iae) {
			Log.e(TAG, "Error while reading the Object.", iae);
		} finally {
			closeResources(cursor);
		}

		return data;
	}

	public synchronized Cursor rawQuery(String query) {
		return rawQuery(query, null);
	}

	public synchronized Cursor rawQuery(String query, String[] selectionArgs) {
		if (FrameworkUtils.isEmptyOrWhitespace(query)) {
			return null;
		}

		if (checkDatabase()) {
			return db.rawQuery(query, selectionArgs);
		}

		return null;
	}

	/**
	 * Read the data from the given table asynchronously. Once data read is over call back will given to
	 * {@link DatabaseListener#onReadComplete(ArrayList)} If any error happened callback will be sent to
	 * {@link DatabaseListener#onReadError(Exception)}
	 *
	 * @param whereClause SQL Where Clause without 'WHERE' keyword
	 * @param orderBy     SQL like ORDER BY attribute without 'ORDER BY' keyword.
	 * @param listener    {@link DatabaseListener} on which callback will be sent.
	 */
	public synchronized <T extends DatabaseObject> void readAsync(final Class<T> tableClass, final String whereClause,
																  final String orderBy, final DatabaseListener<T> listener) {
		readAsync(tableClass, whereClause, orderBy, null, listener);
	}

	public synchronized <T extends DatabaseObject> void readAsync(final Class<T> tableClass, final String whereClause,
																  final String orderBy, final String limit, final DatabaseListener<T> listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				ArrayList<T> data = new ArrayList<>();
				if (checkDatabase()) {
					Cursor cursor = null;
					try {
						@SuppressWarnings("unchecked")
						DatabaseObject<T> object = tableClass.newInstance();
						cursor = db.query(dbTables.get(tableClass), null, whereClause, null, null, null, orderBy, limit);
						if (cursor != null && cursor.moveToFirst()) {
							while (!cursor.isAfterLast()) {
								data.add(object.buildObjectFromCursor(cursor));
								cursor.moveToNext();
							}
						}

						if (listener != null) {
							listener.onReadComplete(data);
						}
					} catch (Exception e) {
						Log.e(TAG, "Error while reading the Object.", e);
						if (listener != null) {
							listener.onReadError(e);
						}
					} finally {
						closeResources(cursor);
					}
				}
			}
		}).start();
	}

	public synchronized <T extends DatabaseObject> void delete(ArrayList<T> databaseObjects) {
		if (databaseObjects == null || databaseObjects.size() == 0)
			return;

		for (DatabaseObject object : databaseObjects) {
			delete(object);
		}
	}

	/**
	 * Delete a object from the table.
	 *
	 * @param databaseObject {@link DatabaseObject} which need to be deleted.
	 */
	public synchronized void delete(DatabaseObject databaseObject) {
		if (databaseObject == null)
			return;

		if (databaseObject.isCacheData()) {
			ArrayList<DatabaseObject> data = cachedData.get(databaseObject.getTableName());
			if (data != null)
				data.remove(databaseObject);

			fireDatabaseUpdate();
			return;
		}

		if (checkDatabase()) {
			try {
				db.beginTransaction();
				db.delete(databaseObject.getTableName(), getWhereClause(databaseObject), null);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			fireDatabaseUpdate();
		}
	}

	/**
	 * Delete the data from database table.
	 *
	 * @param tableClass  class for which to delete the data. Class should extend Database object
	 * @param whereClause SQL Where Clause without 'WHERE' keyword
	 */
	public synchronized void delete(Class<? extends DatabaseObject> tableClass, String whereClause) {
		try {
			DatabaseObject object = tableClass.newInstance();
			if (object.isCacheData()) {
				// For cached data we don't run any where command complete data is removed...
				cachedData.remove(object.getTableName());
			} else if (checkDatabase()) {
				try {
					db.beginTransaction();
					db.delete(dbTables.get(tableClass), whereClause, null);
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
			fireDatabaseUpdate();
		} catch (Exception e) {
			Log.e(TAG, "Error while deleting the Objects.", e);
		}
	}

	public synchronized void clearDatabase() {
		for (Class<? extends DatabaseObject> tableClass : dbTables.keySet()) {
			delete(tableClass, null);
		}

		// Remove all cached data
		cachedData.clear();

		// Recreate the tables..
		dataBaseHelper.onCreate(db);
	}

	private void closeResources(Cursor cursor) {
		if (cursor != null) {
			cursor.close();
		}
	}

	private String getWhereClause(DatabaseObject databaseObject) {
		StringBuilder whereClause = new StringBuilder();
		String[] columns = databaseObject.getPrimaryKeys();
		Object[] values = databaseObject.getValuesForPrimaryKeys();

		for (int i = 0; i < columns.length; i++) {
			if (i > 0)
				whereClause.append(" AND ");

			if (values[i] == null)
				whereClause.append(columns[i]).append(" IS ").append("NULL");
			else {
				if (values[i] instanceof String) {
					whereClause.append(columns[i]).append(" = '").append(((String) values[i]).replaceAll("'", "''"))
							.append("'");
				} else {
					whereClause.append(columns[i]).append(" = ").append(values[i]);
				}
			}
		}

		return whereClause.toString();
	}

	private void fireDatabaseUpdate() {
		if (databaseListener != null && !isBulkUpdate) {
			databaseListener.onDatabaseChanged();
		}
	}
}