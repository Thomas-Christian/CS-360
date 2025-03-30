package com.zybooks.weightlogger.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.zybooks.weightlogger.Utilities.ErrorHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * Database helper class for managing weight entry data operations.
 * Handles storing, retrieving, updating, and deleting weight records.
 * Extends the base DatabaseHelper with weight-specific functionality.
 */
public class WeightDatabaseHelper extends DatabaseHelper {
    private final Context context;
    private static final String COMPONENT_NAME = "WeightDatabaseHelper";

    /**
     * Creates a new WeightDatabaseHelper instance.
     *
     * @param context The context used to access the database
     */
    public WeightDatabaseHelper(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * SQL statement to create the weight entries table in the database.
     * Defines columns for ID, user ID, date, and weight with a foreign key relationship to users.
     */
    protected static final String CREATE_WEIGHT_TABLE =
            "CREATE TABLE weight_entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "date TEXT, " +
                    "weight REAL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id))";

    /**
     * Inner class representing a weight entry record.
     * Encapsulates the data and provides accessor methods.
     */
    public static class WeightEntry {
        private final int id;
        private final String date;
        private final double weight;

        /**
         * Creates a new WeightEntry instance.
         *
         * @param id The unique identifier of the entry
         * @param date The date of the weight measurement
         * @param weight The recorded weight value
         */
        public WeightEntry(int id, String date, double weight) {
            this.id = id;
            this.date = date;
            this.weight = weight;
        }

        /**
         * Gets the entry ID.
         * @return The unique ID of this weight entry
         */
        public int getId() { return id; }

        /**
         * Gets the entry date.
         * @return The date of this weight measurement
         */
        public String getDate() { return date; }

        /**
         * Gets the weight value.
         * @return The recorded weight value
         */
        public double getWeight() { return weight; }
    }

    /**
     * Adds a new weight entry to the database.
     *
     * @param userId The ID of the user this entry belongs to
     * @param date The date of the weight measurement
     * @param weight The recorded weight value
     * @return true if insertion was successful, false otherwise
     */
    public boolean addWeightEntry(int userId, String date, double weight) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("date", date);
            values.put("weight", weight);
            long result = db.insert("weight_entries", null, values);
            return result != -1;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "addWeightEntry",
                    ErrorHandler.Severity.ERROR, "Failed to add weight entry");
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Updates an existing weight entry in the database.
     *
     * @param entryId The ID of the entry to update
     * @param date The new date value
     * @param weight The new weight value
     * @return true if update was successful, false otherwise
     */
    public boolean updateWeightEntry(int entryId, String date, double weight) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("date", date);
            values.put("weight", weight);
            int rowsAffected = db.update("weight_entries", values, "id = ?", new String[]{String.valueOf(entryId)});
            return rowsAffected > 0;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "updateWeightEntry",
                    ErrorHandler.Severity.ERROR, "Failed to update weight entry");
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Deletes a weight entry from the database.
     *
     * @param entryId The ID of the entry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteWeightEntry(int entryId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int rowsAffected = db.delete("weight_entries", "id = ?", new String[]{String.valueOf(entryId)});
            return rowsAffected > 0;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "deleteWeightEntry",
                    ErrorHandler.Severity.ERROR, "Failed to delete weight entry");
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Gets all weight entries for a user, ordered by date (most recent first).
     * Creates the weight_entries table if it doesn't exist.
     *
     * @param userId The ID of the user
     * @return A list of WeightEntry objects containing the user's weight history
     */
    public List<WeightEntry> getWeightEntries(int userId) {
        List<WeightEntry> entries = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor tableCheck = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Check if the weight_entries table exists
            tableCheck = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{"weight_entries"}
            );

            boolean tableExists = tableCheck.getCount() > 0;
            tableCheck.close();
            tableCheck = null;

            if (!tableExists) {
                db.execSQL(CREATE_WEIGHT_TABLE);
                return entries;
            }

            cursor = db.rawQuery(
                    "SELECT id, date, weight FROM weight_entries WHERE user_id = ? ORDER BY date DESC",
                    new String[]{String.valueOf(userId)}
            );

            while (cursor.moveToNext()) {
                entries.add(new WeightEntry(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2)));
            }

            return entries;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "getWeightEntries",
                    ErrorHandler.Severity.ERROR, null);
            return entries;
        } finally {
            if (tableCheck != null) {
                tableCheck.close();
            }
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}