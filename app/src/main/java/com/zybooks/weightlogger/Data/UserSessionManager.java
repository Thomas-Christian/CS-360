package com.zybooks.weightlogger.Data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages user session data for the application.
 * Handles saving, retrieving, and clearing login state using SharedPreferences.
 * Provides methods to check if a user is currently logged in.
 */
public class UserSessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    /**
     * Creates a new UserSessionManager instance.
     *
     * @param context The context used to access SharedPreferences
     */
    public UserSessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    /**
     * Saves a new login session for a user.
     * Stores the username and sets the logged-in flag to true.
     *
     * @param username The username of the logged-in user
     */
    public void saveLoginSession(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Gets the username of the currently logged-in user.
     *
     * @return The username of the logged-in user, or an empty string if no user is logged in
     */
    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    /**
     * Checks if a user is currently logged in.
     *
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED_IN, false);
    }

    /**
     * Logs out the current user by clearing all session data.
     * Removes the username and sets the logged-in flag to false.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}