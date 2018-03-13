package com.example.android.fleetdemo.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Base class that represent a database table in DB. {@link DatabaseService} operate on these objects only. When you want to
 * create a new table in db just implement this class
 *
 * @author manish
 */
public interface DatabaseObject<T extends DatabaseObject> {

	/**
	 * @return Table name for this table
	 */
	String getTableName();

	/**
	 * @return Create table SQL for this table.
	 */
	String getCreateTableSql();

	/**
	 * Handles the database upgrade for the given table.
	 *
	 * @param db         SQLiteDatabase Object
	 * @param oldVersion Old Database version from which we are upgrading.
	 * @param newVersion New version to which we are upgrading.
	 */
	void onDatabaseUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

	/**
	 * Build the Database object from the cursor.
	 *
	 * @param cursor Cursor from where we are going to read data.
	 * @return Database Object.
	 */
	T buildObjectFromCursor(Cursor cursor);

	/**
	 * Generate and return the content values for given object.
	 *
	 * @return Content Values to store in database.
	 */
	ContentValues getContentValues();

	/**
	 * @return String array of all primary key/columns names.
	 */
	String[] getPrimaryKeys();

	/**
	 * @return Values for all the primary keys/columns in same order in which which primary keys are returned from
	 * {@link DatabaseObject#getPrimaryKeys()}
	 */
	Object[] getValuesForPrimaryKeys();

	// Define whether data goes in db or local cache

	/**
	 * Define where we are going to store data in database or local cache. 'NOT IMPLEMENTED YET'
	 *
	 * @return Boolean
	 */
	boolean isCacheData();


}