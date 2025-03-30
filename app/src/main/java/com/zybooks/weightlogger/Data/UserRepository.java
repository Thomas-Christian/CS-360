package com.zybooks.weightlogger.Data;

import android.content.Context;

/**
 * Repository class for user data operations following the MVVM architecture pattern.
 * Acts as a single source of truth for user data and abstracts the data sources.
 * Handles database interactions related to user accounts and profiles.
 */
public class UserRepository {
    private final UserDatabaseHelper userDatabaseHelper;

    /**
     * Creates a new UserRepository instance.
     *
     * @param context The context used to initialize the database helper
     */
    public UserRepository(Context context) {
        this.userDatabaseHelper = new UserDatabaseHelper(context);
    }

    /**
     * Gets a user's ID by their username.
     *
     * @param username The username to look up
     * @return The user's ID, or -1 if not found
     */
    public int getUserId(String username) {
        return userDatabaseHelper.getUserId(username);
    }

    /**
     * Gets a user's goal weight from the database.
     *
     * @param userId The ID of the user
     * @return The user's goal weight, or 0 if not set or user not found
     */
    public double getGoalWeight(int userId) {
        return userDatabaseHelper.getGoalWeight(userId);
    }

    /**
     * Updates a user's goal weight in the database.
     *
     * @param userId The ID of the user
     * @param goalWeight The new goal weight to set
     * @return true if update was successful, false otherwise
     */
    public boolean updateGoalWeight(int userId, double goalWeight) {
        return userDatabaseHelper.updateGoalWeight(userId, goalWeight);
    }

    /**
     * Validates a user's credentials for authentication.
     *
     * @param username The username to authenticate
     * @param password The password to verify
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateUser(String username, String password) {
        return userDatabaseHelper.validateUser(username, password);
    }

    /**
     * Inserts a new user with hashed password into the database.
     *
     * @param username The username for the new account
     * @param password The password for the new account (will be hashed before storage)
     * @param goalWeight The initial goal weight for the user
     * @return true if insertion was successful, false otherwise
     */
    public boolean insertUser(String username, String password, double goalWeight) {
        return userDatabaseHelper.insertUser(username, password, goalWeight);
    }

    /**
     * Updates a user's password with secure hashing.
     *
     * @param username The username of the account to update
     * @param newPassword The new password to hash and store
     * @return true if update was successful, false otherwise
     */
    public boolean updatePassword(String username, String newPassword) {
        return userDatabaseHelper.updatePassword(username, newPassword);
    }

    /**
     * Checks if a username already exists in the database.
     *
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    public boolean userExists(String username) {
        return userDatabaseHelper.userExists(username);
    }
}