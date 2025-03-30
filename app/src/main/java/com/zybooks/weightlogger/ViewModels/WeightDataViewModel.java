package com.zybooks.weightlogger.ViewModels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.zybooks.weightlogger.Data.UserRepository;
import com.zybooks.weightlogger.Data.UserSessionManager;
import com.zybooks.weightlogger.Data.WeightDatabaseHelper;
import com.zybooks.weightlogger.Data.WeightRepository;
import com.zybooks.weightlogger.Utilities.NotificationHelper;
import java.util.List;

/**
 * ViewModel for weight data operations with enhanced validation.
 * Extends BaseValidationViewModel to leverage centralized validation logic.
 */
public class WeightDataViewModel extends BaseValidationViewModel {
    private static final double GOAL_PROXIMITY_THRESHOLD = 5.0;

    private final WeightRepository weightRepository;
    private final UserRepository userRepository;
    private final UserSessionManager sessionManager;
    private final NotificationHelper notificationHelper;

    // Weight entries data
    private final MutableLiveData<List<WeightDatabaseHelper.WeightEntry>> weightEntriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> profileUpdateNeededLiveData = new MutableLiveData<>();

    // Validation for new weight entry
    private final MutableLiveData<Boolean> dateValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> weightValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> formValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> dateErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> weightErrorLiveData = new MutableLiveData<>();

    /**
     * Creates a new WeightDataViewModel instance.
     *
     * @param application The application context
     */
    public WeightDataViewModel(@NonNull Application application) {
        super(application);
        weightRepository = new WeightRepository(application);
        userRepository = new UserRepository(application);
        sessionManager = new UserSessionManager(application);
        notificationHelper = new NotificationHelper(application);

        // Load initial data
        loadWeightEntries();
    }

    // LiveData getters
    public LiveData<List<WeightDatabaseHelper.WeightEntry>> getWeightEntriesLiveData() {
        return weightEntriesLiveData;
    }
    public LiveData<String> getStatusMessageLiveData() { return statusMessageLiveData; }
    public LiveData<Boolean> getProfileUpdateNeededLiveData() { return profileUpdateNeededLiveData; }
    public LiveData<Boolean> getFormValidLiveData() { return formValidLiveData; }
    public LiveData<String> getDateErrorLiveData() { return dateErrorLiveData; }
    public LiveData<String> getWeightErrorLiveData() { return weightErrorLiveData; }

    /**
     * Loads weight entries from the repository for the current user.
     */
    public void loadWeightEntries() {
        int userId = getUserId();
        if (userId == -1) {
            statusMessageLiveData.setValue("User not found");
            return;
        }

        // Clear existing entries first
        weightEntriesLiveData.setValue(null);

        // Now load entries for the current user
        List<WeightDatabaseHelper.WeightEntry> entries = weightRepository.getWeightEntries(userId);
        weightEntriesLiveData.setValue(entries);

        if (entries.isEmpty()) {
            statusMessageLiveData.setValue("No weight entries yet");
        }
    }

    /**
     * Refresh weight entries from the repository for the current user after login.
     */
    public void refreshDataAfterLogin() {
        // Clear any cached data
        weightEntriesLiveData.setValue(null);
        // Reload from the database with the current user ID
        loadWeightEntries();
    }

    /**
     * Validates date input using the ValidationService.
     *
     * @param dateStr The date string to validate
     */
    public void validateDate(String dateStr) {
        validationService.validateDate(
                dateStr,
                dateValidLiveData,
                dateErrorLiveData
        );
        updateFormValidity();
    }

    /**
     * Validates weight input using the ValidationService.
     *
     * @param weightStr The weight string to validate
     */
    public void validateWeight(String weightStr) {
        validationService.validateWeight(
                weightStr,
                weightValidLiveData,
                weightErrorLiveData
        );
        updateFormValidity();
    }

    /**
     * Updates the form validity state based on individual field validities.
     */
    private void updateFormValidity() {
        updateFormValidity(
                formValidLiveData,
                dateValidLiveData.getValue(),
                weightValidLiveData.getValue()
        );
    }

    /**
     * Adds a new weight entry after validation.
     *
     * @param dateStr The date string for the entry
     * @param weightStr The weight string to be parsed
     */
    public void addWeightEntry(String dateStr, String weightStr) {
        // Validate inputs
        validateDate(dateStr);
        validateWeight(weightStr);

        // Check form validity
        if (Boolean.FALSE.equals(formValidLiveData.getValue())) {
            statusMessageLiveData.setValue("Please correct the errors before submitting");
            return;
        }

        try {
            double weight = Double.parseDouble(weightStr);
            int userId = getUserId();
            if (userId == -1) {
                statusMessageLiveData.setValue("User not found");
                return;
            }

            boolean success = weightRepository.addWeightEntry(userId, dateStr, weight);

            if (success) {
                statusMessageLiveData.setValue("Weight entry added successfully");

                // Reload entries
                loadWeightEntries();

                // Check goal progress
                checkWeightGoalProgress(userId, weight);

                // Signal profile update needed
                profileUpdateNeededLiveData.setValue(true);

                // Reset validation for next entry
                resetValidation();
            } else {
                statusMessageLiveData.setValue("Failed to add weight entry");
            }
        } catch (NumberFormatException e) {
            weightErrorLiveData.setValue("Please enter a valid weight");
            weightValidLiveData.setValue(false);
            updateFormValidity();
        }
    }

    /**
     * Validates inputs for updating a weight entry.
     *
     * @param newDate The new date value
     * @param newWeightStr The new weight value as a string
     * @return True if validation passes, false otherwise
     */
    public boolean validateUpdateInputs(String newDate, String newWeightStr) {
        validateDate(newDate);
        validateWeight(newWeightStr);

        return Boolean.TRUE.equals(formValidLiveData.getValue());
    }

    /**
     * Updates an existing weight entry after validation.
     *
     * @param entryId The ID of the entry to update
     * @param newDate The new date value
     * @param newWeightStr The new weight value as a string
     */
    public void updateWeightEntry(int entryId, String newDate, String newWeightStr) {
        if (!validateUpdateInputs(newDate, newWeightStr)) {
            statusMessageLiveData.setValue("Please correct the errors before updating");
            return;
        }

        try {
            double newWeight = Double.parseDouble(newWeightStr);
            boolean success = weightRepository.updateWeightEntry(entryId, newDate, newWeight);

            if (success) {
                statusMessageLiveData.setValue("Weight entry updated");
                loadWeightEntries();
                profileUpdateNeededLiveData.setValue(true);
            } else {
                statusMessageLiveData.setValue("Failed to update entry");
            }
        } catch (NumberFormatException e) {
            statusMessageLiveData.setValue("Please enter a valid weight");
        }
    }

    /**
     * Deletes a weight entry from the repository.
     *
     * @param entryId The ID of the entry to delete
     */
    public void deleteWeightEntry(int entryId) {
        boolean success = weightRepository.deleteWeightEntry(entryId);

        if (success) {
            statusMessageLiveData.setValue("Weight entry deleted");
            loadWeightEntries();
            profileUpdateNeededLiveData.setValue(true);
        } else {
            statusMessageLiveData.setValue("Failed to delete entry");
        }
    }

    /**
     * Checks the user's progress toward their weight goal.
     * Sends appropriate notifications based on how close they are to their goal.
     *
     * @param userId The ID of the user
     * @param currentWeight The user's current weight
     */
    private void checkWeightGoalProgress(int userId, double currentWeight) {
        // Get the user's goal weight from the repository
        double goalWeight = userRepository.getGoalWeight(userId);

        // If no goal weight is set or it's invalid, return
        if (goalWeight <= 0) {
            return;
        }

        // Calculate the difference between current and goal weight
        double difference = Math.abs(currentWeight - goalWeight);

        // If user has reached their goal
        if (difference <= 0.5) {
            // Send goal achieved notification
            notificationHelper.sendGoalAchievedNotification();
        }
        // If user is getting close to their goal (within the threshold)
        else if (difference <= GOAL_PROXIMITY_THRESHOLD) {
            // Send progress notification
            notificationHelper.sendGoalProgressNotification(currentWeight, goalWeight);
        }
    }

    /**
     * Gets the current user's ID from the repository.
     *
     * @return The user ID, or default user if user not found
     */
    private int getUserId() {
        if (sessionManager.isLoggedIn()) {
            // Use logged-in user's ID
            String username = sessionManager.getUsername();
            return userRepository.getUserId(username);
        } else {
            // Use the default user ID
            return userRepository.getUserId("DefaultUser");
        }
    }

    /**
     * Resets validation states for adding a new entry.
     */
    public void resetValidation() {
        dateValidLiveData.setValue(false);
        weightValidLiveData.setValue(false);
        formValidLiveData.setValue(false);
        dateErrorLiveData.setValue(null);
        weightErrorLiveData.setValue(null);
    }
}