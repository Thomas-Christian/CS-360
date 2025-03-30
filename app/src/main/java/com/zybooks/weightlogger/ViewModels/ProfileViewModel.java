package com.zybooks.weightlogger.ViewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.zybooks.weightlogger.Data.UserRepository;
import com.zybooks.weightlogger.Data.UserSessionManager;
import com.zybooks.weightlogger.Utilities.InputValidator;
import com.zybooks.weightlogger.Utilities.InputValidator.ValidationResult;

import java.util.Locale;

/**
 * ViewModel for profile operations with enhanced validation.
 * Handles user profile data, goal weight updates, and password changes.
 */
public class ProfileViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final UserSessionManager sessionManager;
    private final MutableLiveData<String> statusMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> usernameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> goalWeightTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutLiveData = new MutableLiveData<>();

    // Field validation for goal weight
    private final MutableLiveData<Boolean> goalWeightValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> goalWeightErrorLiveData = new MutableLiveData<>();

    // Field validation for password change
    private final MutableLiveData<Boolean> currentPasswordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> newPasswordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> confirmPasswordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordFormValidLiveData = new MutableLiveData<>(false);

    private final MutableLiveData<String> currentPasswordErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> newPasswordErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordErrorLiveData = new MutableLiveData<>();

    // Password strength
    private final MutableLiveData<Integer> passwordStrengthLiveData = new MutableLiveData<>(0);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = new UserSessionManager(application);

        // Initialize username
        usernameLiveData.setValue(sessionManager.getUsername());

        // Load initial goal weight
        updateWeightGoalInfo();
    }

    // LiveData getters
    public LiveData<String> getStatusMessageLiveData() { return statusMessageLiveData; }
    public LiveData<String> getUsernameLiveData() { return usernameLiveData; }
    public LiveData<String> getGoalWeightTextLiveData() { return goalWeightTextLiveData; }
    public LiveData<Boolean> getLogoutLiveData() { return logoutLiveData; }

    public LiveData<Boolean> getGoalWeightValidLiveData() { return goalWeightValidLiveData; }
    public LiveData<String> getGoalWeightErrorLiveData() { return goalWeightErrorLiveData; }

//    public LiveData<Boolean> getCurrentPasswordValidLiveData() { return currentPasswordValidLiveData; }
//    public LiveData<Boolean> getNewPasswordValidLiveData() { return newPasswordValidLiveData; }
//    public LiveData<Boolean> getConfirmPasswordValidLiveData() { return confirmPasswordValidLiveData; }
    public LiveData<Boolean> getPasswordFormValidLiveData() { return passwordFormValidLiveData; }

    public LiveData<String> getCurrentPasswordErrorLiveData() { return currentPasswordErrorLiveData; }
    public LiveData<String> getNewPasswordErrorLiveData() { return newPasswordErrorLiveData; }
    public LiveData<String> getConfirmPasswordErrorLiveData() { return confirmPasswordErrorLiveData; }

    public LiveData<Integer> getPasswordStrengthLiveData() { return passwordStrengthLiveData; }

    /**
     * Updates the goal weight information displayed in the profile.
     */
    public void updateWeightGoalInfo() {
        int userId = getUserId();
        if (userId == -1) {
            statusMessageLiveData.setValue("User not found");
            return;
        }

        double goalWeight = userRepository.getGoalWeight(userId);
        if (goalWeight <= 0) {
            goalWeightTextLiveData.setValue("Goal Weight: Not set");
        } else {
            goalWeightTextLiveData.setValue(String.format(Locale.getDefault(),
                    "%.1f lbs", goalWeight));
        }
    }

    /**
     * Validates goal weight input.
     *
     * @param goalWeightStr The goal weight as a string
     */
    public void validateGoalWeight(String goalWeightStr) {
        ValidationResult result = InputValidator.validateWeight(goalWeightStr);
        goalWeightValidLiveData.setValue(result.isValid());
        goalWeightErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
    }

    /**
     * Saves a new goal weight after validation.
     *
     * @param goalWeightStr The goal weight as a string
     */
    public void saveGoalWeight(String goalWeightStr) {
        validateGoalWeight(goalWeightStr);

        if (Boolean.FALSE.equals(goalWeightValidLiveData.getValue())) {
            statusMessageLiveData.setValue(goalWeightErrorLiveData.getValue());
            return;
        }

        try {
            double goalWeight = Double.parseDouble(goalWeightStr);
            int userId = getUserId();
            if (userId == -1) {
                statusMessageLiveData.setValue("User not found");
                return;
            }

            boolean success = userRepository.updateGoalWeight(userId, goalWeight);
            if (success) {
                statusMessageLiveData.setValue("Goal weight updated");
                updateWeightGoalInfo();
            } else {
                statusMessageLiveData.setValue("Failed to update goal weight");
            }
        } catch (NumberFormatException e) {
            statusMessageLiveData.setValue("Please enter a valid weight");
        }
    }

    /**
     * Validates current password input.
     *
     * @param currentPassword The current password
     */
    public void validateCurrentPassword(String currentPassword) {
        ValidationResult result = InputValidator.validateRequired(currentPassword, "Current password");
        currentPasswordValidLiveData.setValue(result.isValid());
        currentPasswordErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
        updatePasswordFormValidity();
    }

    /**
     * Validates new password input with strength calculation.
     *
     * @param newPassword The new password
     */
    public void validateNewPassword(String newPassword) {
        ValidationResult result = InputValidator.validatePassword(newPassword);
        newPasswordValidLiveData.setValue(result.isValid());
        newPasswordErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
        passwordStrengthLiveData.setValue(InputValidator.calculatePasswordStrength(newPassword));
        updatePasswordFormValidity();
    }

    /**
     * Validates that the confirmation password matches the new password.
     *
     * @param newPassword The new password
     * @param confirmPassword The confirmation of the new password
     */
    public void validateConfirmPassword(String newPassword, String confirmPassword) {
        ValidationResult result = InputValidator.validatePasswordMatch(newPassword, confirmPassword);
        confirmPasswordValidLiveData.setValue(result.isValid());
        confirmPasswordErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
        updatePasswordFormValidity();
    }

    /**
     * Updates the password form validity state based on individual field validities.
     */
    private void updatePasswordFormValidity() {
        Boolean currentPasswordValid = currentPasswordValidLiveData.getValue();
        Boolean newPasswordValid = newPasswordValidLiveData.getValue();
        Boolean confirmPasswordValid = confirmPasswordValidLiveData.getValue();

        boolean formValid = currentPasswordValid != null && currentPasswordValid &&
                newPasswordValid != null && newPasswordValid &&
                confirmPasswordValid != null && confirmPasswordValid;

        passwordFormValidLiveData.setValue(formValid);
    }

    /**
     * Changes the user's password after validation.
     *
     * @param currentPassword The current password
     * @param newPassword The new password
     * @param confirmNewPassword The confirmation of the new password
     */
    public void changePassword(String currentPassword, String newPassword, String confirmNewPassword) {
        // Validate all fields
        validateCurrentPassword(currentPassword);
        validateNewPassword(newPassword);
        validateConfirmPassword(newPassword, confirmNewPassword);

        // Check form validity
        if (Boolean.FALSE.equals(passwordFormValidLiveData.getValue())) {
            statusMessageLiveData.setValue("Please correct the errors before submitting");
            return;
        }

        // Get username
        String username = sessionManager.getUsername();

        // Verify current password
        if (!userRepository.validateUser(username, currentPassword)) {
            currentPasswordErrorLiveData.setValue("Current password is incorrect");
            currentPasswordValidLiveData.setValue(false);
            passwordFormValidLiveData.setValue(false);
            statusMessageLiveData.setValue("Current password is incorrect");
            return;
        }

        // Update password
        boolean success = userRepository.updatePassword(username, newPassword);
        if (success) {
            statusMessageLiveData.setValue("Password updated successfully");
            resetPasswordValidation();
        } else {
            statusMessageLiveData.setValue("Failed to update password");
        }
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        sessionManager.logout();
        statusMessageLiveData.setValue("Logged out successfully");
        logoutLiveData.setValue(true);
    }

    /**
     * Gets the ID of the current user.
     *
     * @return The user ID, or -1 if user not found
     */
    private int getUserId() {
        if (sessionManager.isLoggedIn()) {
            String username = sessionManager.getUsername();
            return userRepository.getUserId(username);
        } else {
            return userRepository.getUserId("DefaultUser");
        }
    }

    /**
     * Resets password change form validation states.
     */
    public void resetPasswordValidation() {
        currentPasswordValidLiveData.setValue(false);
        newPasswordValidLiveData.setValue(false);
        confirmPasswordValidLiveData.setValue(false);
        passwordFormValidLiveData.setValue(false);

        currentPasswordErrorLiveData.setValue(null);
        newPasswordErrorLiveData.setValue(null);
        confirmPasswordErrorLiveData.setValue(null);

        passwordStrengthLiveData.setValue(0);
    }
}