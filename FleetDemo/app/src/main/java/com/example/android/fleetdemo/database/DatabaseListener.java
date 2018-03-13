package com.example.android.fleetdemo.database;

import java.util.ArrayList;

/**
 * Listener class to receive the callbacks from the {@link DatabaseService}
 *
 * @author manish
 */
public interface DatabaseListener<T extends DatabaseObject> {

	/**
	 * Callback will be sent whenever there is any change in database like new object inserted/updated or deleted.
	 */
	void onDatabaseChanged();

	/**
	 * Will be called when data read is done asynchronously
	 * {@link DatabaseService#readAsync(Class, String, String, DatabaseListener)}
	 *
	 * @param data List of {@link DatabaseObject} read from the table.
	 */
	void onReadComplete(ArrayList<T> data);

	/**
	 * Will be called when data read encountered an error during asynchronous read
	 *
	 * @param e Exception which occured
	 */
	void onReadError(Exception e);
}