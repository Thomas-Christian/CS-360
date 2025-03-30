package com.zybooks.weightlogger.Data;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Base database helper class that manages SQLite database creation and version management.
 * Serves as the parent class for database helpers in the application.
 * Creates and initializes the database tables needed for the application.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * The name of the application's SQLite database file.
     */
    protected static final String DATABASE_NAME = "weight-logger.db";

    /**
     * The current version of the database schema.
     * This value should be incremented when the database schema changes.
     */
    protected static final int VERSION = 2;

    /**
     * Creates a new instance of the DatabaseHelper.
     *
     * @param context The context used to access the database
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * This method creates all required tables for the application.
     *
     * @param db The database being created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UserDatabaseHelper.CREATE_USER_TABLE);
        db.execSQL(WeightDatabaseHelper.CREATE_WEIGHT_TABLE);
        db.execSQL(UserDatabaseHelper.INSERT_DEFAULT_USER);
    }

    /**
     * Called when the database needs to be upgraded from an older version to a newer one.
     * This implementation is empty and should be overridden when database schema changes.
     *
     * @param db The database being upgraded
     * @param oldVersion The old database version
     * @param newVersion The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed
    }
}