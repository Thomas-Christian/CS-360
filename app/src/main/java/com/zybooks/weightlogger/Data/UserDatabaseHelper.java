package com.zybooks.weightlogger.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import com.zybooks.weightlogger.Utilities.ErrorHandler;
import com.zybooks.weightlogger.Utilities.PasswordHash;

/**
 * Database helper class for managing user-related data operations.
 * Handles user accounts, authentication, and goal weight storage.
 * Extends the base DatabaseHelper with user-specific functionality.
 */
public class UserDatabaseHelper extends DatabaseHelper {
    private final Context context;
    private static final String COMPONENT_NAME = "UserDatabaseHelper";

    /**
     * Creates a new UserDatabaseHelper instance.
     *
     * @param context The context used to access the database
     */
    public UserDatabaseHelper(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * SQL statement to create the users table in the database.
     * Defines columns for ID, username, password, and goal weight.
     */
    protected static final String CREATE_USER_TABLE =
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT, " +
                    "password TEXT, " +
                    "goal_weight REAL)";

    /**
     * SQL statement to insert a default user account.
     * This account has no password and is used when no user is logged in.
     */
    protected static final String INSERT_DEFAULT_USER =
            "INSERT INTO users (username, password, goal_weight) VALUES ('DefaultUser', '', 0)";

    /**
     * Inserts a new user with hashed password into the database.
     * Checks if the username already exists to prevent duplicates.
     *
     * @param username The username for the new account
     * @param password The password for the new account (will be hashed before storage)
     * @param goalWeight The initial goal weight for the user
     * @return true if insertion was successful, false otherwise
     */
    public boolean insertUser(String username, String password, double goalWeight) {
        if (userExists(username)) {
            return false;
        }

        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("username", username);

            // Hash the password before storing it
            String hashedPassword = PasswordHash.hashPassword(password);
            if (hashedPassword == null) {
                ErrorHandler.logError(COMPONENT_NAME, "Failed to hash password for new user",
                        ErrorHandler.Severity.ERROR);
                return false;
            }
            values.put("password", hashedPassword);
            values.put("goal_weight", goalWeight);

            long result = db.insert("users", null, values);
            return result != -1;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "insertUser",
                    ErrorHandler.Severity.ERROR,
                    "Failed to create account");
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    public boolean userExists(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
            return cursor.getCount() > 0;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "userExists",
                    ErrorHandler.Severity.ERROR, null);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Gets a user's ID by their username.
     *
     * @param username The username to look up
     * @return The user's ID, or -1 if not found
     */
    public int getUserId(String username) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{username});
            return cursor.moveToFirst() ? cursor.getInt(0) : -1;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "getUserId",
                    ErrorHandler.Severity.ERROR, null);
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Validates a user's credentials for authentication.
     * Handles both plain text passwords and salted hashes.
     * Updates passwords to the secure hash format if authentication succeeds.
     *
     * @param username The username to authenticate
     * @param password The password to verify
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT password FROM users WHERE username = ?", new String[]{username});

            boolean valid = false;
            if (cursor.moveToFirst()) {
                String storedPassword = cursor.getString(0);

                // Handle the default user with empty password
                if (username.equals("DefaultUser") && storedPassword.isEmpty() && password.isEmpty()) {
                    valid = true;
                }
                // Handle normal users with hashed passwords
                else if (!storedPassword.isEmpty()) {
                    if (storedPassword.contains(":")) {
                        // It's a salted hash (new format)
                        valid = PasswordHash.verifyPassword(password, storedPassword);
                    } else {
                        // It's a plain text password - convert it now
                        valid = password.equals(storedPassword);
                        if (valid) {
                            // Update to salted hash format for next login
                            updatePassword(username, password);
                        }
                    }
                }
            }
            return valid;
        } catch (Exception e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "validateUser",
                    ErrorHandler.Severity.ERROR, "Authentication error");
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Gets a user's goal weight from the database.
     *
     * @param userId The ID of the user
     * @return The user's goal weight, or 0 if not set or user not found
     */
    public double getGoalWeight(int userId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT goal_weight FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
            return cursor.moveToFirst() ? cursor.getDouble(0) : 0;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "getGoalWeight",
                    ErrorHandler.Severity.ERROR, null);
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Updates a user's goal weight in the database.
     *
     * @param userId The ID of the user
     * @param goalWeight The new goal weight to set
     * @return true if update was successful, false otherwise
     */
    public boolean updateGoalWeight(int userId, double goalWeight) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("goal_weight", goalWeight);
            int rowsUpdated = db.update("users", values, "id = ?", new String[]{String.valueOf(userId)});
            return rowsUpdated > 0;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "updateGoalWeight",
                    ErrorHandler.Severity.ERROR, "Failed to update goal weight");
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Updates a user's password with secure hashing.
     *
     * @param username The username of the account to update
     * @param newPassword The new password to hash and store
     * @return true if update was successful, false otherwise
     */
    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            // Hash the new password
            String hashedPassword = PasswordHash.hashPassword(newPassword);
            if (hashedPassword == null) {
                ErrorHandler.logError(COMPONENT_NAME, "Failed to hash password for update",
                        ErrorHandler.Severity.ERROR);
                return false;
            }
            values.put("password", hashedPassword);

            // Update
            int rowsUpdated = db.update("users", values, "username = ?", new String[]{username});
            return rowsUpdated > 0;
        } catch (SQLiteException e) {
            ErrorHandler.handleException(context, e, COMPONENT_NAME, "updatePassword",
                    ErrorHandler.Severity.ERROR, "Failed to update password");
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }
}