package com.zybooks.weightlogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weightlogger.db";
    private static final int VERSION = 2; // Incremented for the goal_weight column addition

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class UserTable {
        private static final String TABLE = "users";
        private static final String COL_USER_ID = "id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
        private static final String COL_GOAL_WEIGHT = "goal_weight";
    }

    private static final class WeightTable {
        private static final String TABLE = "weight_entries";
        private static final String COL_ENTRY_ID = "id";
        private static final String COL_USER_ID = "user_id";
        private static final String COL_DATE = "date";
        private static final String COL_WEIGHT = "weight";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        db.execSQL("CREATE TABLE " + UserTable.TABLE + "(" +
                UserTable.COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UserTable.COL_USERNAME + " TEXT, " +
                UserTable.COL_PASSWORD + " TEXT, " +
                UserTable.COL_GOAL_WEIGHT + " REAL) ");

        // Create weight entries table
        db.execSQL("CREATE TABLE " + WeightTable.TABLE + "(" +
                WeightTable.COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WeightTable.COL_USER_ID + " INTEGER, " +
                WeightTable.COL_DATE + " TEXT, " +
                WeightTable.COL_WEIGHT + " REAL, " +
                "FOREIGN KEY (" + WeightTable.COL_USER_ID + ") REFERENCES " +
                UserTable.TABLE + "(" + UserTable.COL_USER_ID + ")) ");

        // Insert a default user
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_USERNAME, "DefaultUser");
        values.put(UserTable.COL_PASSWORD, "");
        values.put(UserTable.COL_GOAL_WEIGHT, 0); // Default user has no goal
        db.insert(UserTable.TABLE, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean insertUser(String username, String password) {
        return insertUser(username, password, 0);
    }

    public boolean insertUser(String username, String password, double goalWeight) {
        // First check if user already exists
        if (userExists(username)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(UserTable.COL_USERNAME, username);
        values.put(UserTable.COL_PASSWORD, password);
        values.put(UserTable.COL_GOAL_WEIGHT, goalWeight);

        long result = db.insert(UserTable.TABLE, null, values);
        db.close();
        return result != -1; // Return true if insertion was successful
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + UserTable.TABLE + " WHERE " +
                UserTable.COL_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + UserTable.COL_USER_ID + " FROM " + UserTable.TABLE +
                " WHERE " + UserTable.COL_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return userId;
    }

    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + UserTable.TABLE +
                " WHERE " + UserTable.COL_USERNAME + " = ? AND " +
                UserTable.COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean valid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valid;
    }

    public static class WeightEntry {
        private final int id;
        private final String date;
        private final double weight;

        public WeightEntry(int id, String date, double weight) {
            this.id = id;
            this.date = date;
            this.weight = weight;
        }

        public int getId() { return id; }
        public String getDate() { return date; }
        public double getWeight() { return weight; }
    }

    public boolean addWeightEntry(int userId, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WeightTable.COL_USER_ID, userId);
        values.put(WeightTable.COL_DATE, date);
        values.put(WeightTable.COL_WEIGHT, weight);

        long result = db.insert(WeightTable.TABLE, null, values);
        db.close();
        return result != -1;
    }

    public boolean updateWeightEntry(int entryId, String date, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(WeightTable.COL_DATE, date);
        values.put(WeightTable.COL_WEIGHT, weight);

        int rowsAffected = db.update(
                WeightTable.TABLE,
                values,
                WeightTable.COL_ENTRY_ID + " = ?",
                new String[] { String.valueOf(entryId) }
        );

        db.close();
        return rowsAffected > 0;
    }

    // Get a user's goal weight
    public double getGoalWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double goalWeight = 0;

        Cursor cursor = db.query(
                UserTable.TABLE,
                new String[]{UserTable.COL_GOAL_WEIGHT},
                UserTable.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            goalWeight = cursor.getDouble(0);
        }

        cursor.close();
        db.close();

        return goalWeight;
    }

    public boolean deleteWeightEntry(int entryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(
                WeightTable.TABLE,
                WeightTable.COL_ENTRY_ID + " = ?",
                new String[] { String.valueOf(entryId) }
        );
        db.close();
        return rowsAffected > 0;
    }

    // Update a user's goal weight
    public boolean updateGoalWeight(int userId, double goalWeight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_GOAL_WEIGHT, goalWeight);

        int rowsUpdated = db.update(
                UserTable.TABLE,
                values,
                UserTable.COL_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );

        db.close();
        return rowsUpdated > 0;
    }

    // Get a user's latest weight entry
    public double getLatestWeight(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double latestWeight = 0;

        // Get the latest weight entry
        String query = "SELECT " + WeightTable.COL_WEIGHT + ", " + WeightTable.COL_DATE +
                " FROM " + WeightTable.TABLE +
                " WHERE " + WeightTable.COL_USER_ID + " = ? " +
                " ORDER BY " + WeightTable.COL_DATE + " DESC " +
                " LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            latestWeight = cursor.getDouble(0);
        }

        cursor.close();
        db.close();

        return latestWeight;
    }

    public List<WeightEntry> getWeightEntries(int userId) {
        List<WeightEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // First check if the weight_entries table exists
        Cursor tableCheck = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{WeightTable.TABLE});

        boolean tableExists = tableCheck.getCount() > 0;
        tableCheck.close();

        if (!tableExists) {
            // If table doesn't exist, create it
            db.execSQL("CREATE TABLE " + WeightTable.TABLE + "(" +
                    WeightTable.COL_ENTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    WeightTable.COL_USER_ID + " INTEGER, " +
                    WeightTable.COL_DATE + " TEXT, " +
                    WeightTable.COL_WEIGHT + " REAL, " +
                    "FOREIGN KEY (" + WeightTable.COL_USER_ID + ") REFERENCES " +
                    UserTable.TABLE + "(" + UserTable.COL_USER_ID + ")) ");
            db.close();
            return entries; // Return empty list since we just created the table
        }

        String query = "SELECT " + WeightTable.COL_ENTRY_ID + ", " + WeightTable.COL_DATE + ", " + WeightTable.COL_WEIGHT +
                " FROM " + WeightTable.TABLE +
                " WHERE " + WeightTable.COL_USER_ID + " = ? " +
                " ORDER BY " + WeightTable.COL_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String date = cursor.getString(1);
                double weight = cursor.getDouble(2);
                entries.add(new WeightEntry(id, date, weight));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return entries;
    }
}