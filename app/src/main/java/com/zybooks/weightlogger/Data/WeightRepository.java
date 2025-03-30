package com.zybooks.weightlogger.Data;

import android.content.Context;

import java.util.List;

/**
 * Repository class for weight data operations following the MVVM architecture pattern.
 * Acts as a single source of truth for weight data and abstracts the data sources.
 * Handles database interactions related to weight entries.
 */
public class WeightRepository {
    private final WeightDatabaseHelper weightDatabaseHelper;

    /**
     * Creates a new WeightRepository instance.
     *
     * @param context The context used to initialize the database helper
     */
    public WeightRepository(Context context) {
        this.weightDatabaseHelper = new WeightDatabaseHelper(context);
    }

    /**
     * Gets all weight entries for a user, ordered by date (most recent first).
     *
     * @param userId The ID of the user
     * @return A list of WeightEntry objects containing the user's weight history
     */
    public List<WeightDatabaseHelper.WeightEntry> getWeightEntries(int userId) {
        return weightDatabaseHelper.getWeightEntries(userId);
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
        return weightDatabaseHelper.addWeightEntry(userId, date, weight);
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
        return weightDatabaseHelper.updateWeightEntry(entryId, date, weight);
    }

    /**
     * Deletes a weight entry from the database.
     *
     * @param entryId The ID of the entry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteWeightEntry(int entryId) {
        return weightDatabaseHelper.deleteWeightEntry(entryId);
    }
}