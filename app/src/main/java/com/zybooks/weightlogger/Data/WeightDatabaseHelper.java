package com.zybooks.weightlogger.Data;

import android.content.ContentValues;
import android.content.Context;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Database helper class for managing weight entry data operations.
 * Handles storing, retrieving, updating, and deleting weight records.
 * Extends the base DatabaseHelper with weight-specific functionality.
 */
public class WeightDatabaseHelper extends DatabaseHelper {

    /**
     * Creates a new WeightDatabaseHelper instance.
     *
     * @param context The context used to access the database
     */
    public WeightDatabaseHelper(Context context) {
        super(context);
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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("date", date);
        values.put("weight", weight);
        long result = db.insert("weight_entries", null, values);
        db.close();
        return result != -1;
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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("weight", weight);
        int rowsAffected = db.update("weight_entries", values, "id = ?", new String[]{String.valueOf(entryId)});
        db.close();
        return rowsAffected > 0;
    }

    /**
     * Deletes a weight entry from the database.
     *
     * @param entryId The ID of the entry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteWeightEntry(int entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete("weight_entries", "id = ?", new String[]{String.valueOf(entryId)});
        db.close();
        return rowsAffected > 0;
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
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor tableCheck = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{"weight_entries"}
        );

        boolean tableExists = tableCheck.getCount() > 0;
        tableCheck.close();

        if (!tableExists) {
            db.execSQL(CREATE_WEIGHT_TABLE);
            db.close();
            return entries;
        }

        Cursor cursor = db.rawQuery(
                "SELECT id, date, weight FROM weight_entries WHERE user_id = ? ORDER BY date DESC",
                new String[]{String.valueOf(userId)}
        );

        while (cursor.moveToNext()) {
            entries.add(new WeightEntry(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2)));
        }

        cursor.close();
        db.close();
        return entries;
    }
}