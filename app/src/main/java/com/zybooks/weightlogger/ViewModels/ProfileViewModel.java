package com.zybooks.weightlogger.ViewModels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.zybooks.weightlogger.Data.UserRepository;
import com.zybooks.weightlogger.Data.UserSessionManager;
import java.util.Locale;

/**
 * ViewModel for profile operations with enhanced validation.
 * Extends BaseValidationViewModel to leverage centralized validation logic.
 */
public class ProfileViewModel extends BaseValidationViewModel {
    private final UserRepository userRepository;
    private final UserSessionManager sessionManager;

    // UI state data
    private final MutableLiveData<String> usernameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> goalWeightTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutLiveData = new MutableLiveData<>();

    // Goal weight validation
    private final MutableLiveData<Boolean> goalWeightValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> goalWeightErrorLiveData = new MutableLiveData<>();

    // Password change validation
    private final MutableLiveData<Boolean> currentPasswordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> newPasswordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> confirmPasswordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordFormValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentPasswordErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> newPasswordErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordErrorLiveData = new MutableLiveData<>();
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
     * Validates goal weight input using the ValidationService.
     *
     * @param goalWeightStr The goal weight as a string
     */
    public void validateGoalWeight(String goalWeightStr) {
        validationService.validateWeight(
                goalWeightStr,
                goalWeightValidLiveData,
                goalWeightErrorLiveData
        );
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
     * Validates current password input using the ValidationService.
     *
     * @param currentPassword The current password
     */
    public void validateCurrentPassword(String currentPassword) {
        // For current password, we only need to check it's not empty
        if (currentPassword.isEmpty()) {
            currentPasswordValidLiveData.setValue(false);
            currentPasswordErrorLiveData.setValue("Current password is required");
        } else {
            currentPasswordValidLiveData.setValue(true);
            currentPasswordErrorLiveData.setValue(null);
        }
        updatePasswordFormValidity();
    }

    /**
     * Validates new password input with strength calculation using the ValidationService.
     *
     * @param newPassword The new password
     */
    public void validateNewPassword(String newPassword) {
        validationService.validatePassword(
                newPassword,
                newPasswordValidLiveData,
                newPasswordErrorLiveData,
                passwordStrengthLiveData
        );
        updatePasswordFormValidity();
    }

    /**
     * Validates that the confirmation password matches the new password.
     *
     * @param newPassword The new password
     * @param confirmPassword The confirmation of the new password
     */
    public void validateConfirmPassword(String newPassword, String confirmPassword) {
        validationService.validatePasswordMatch(
                newPassword,
                confirmPassword,
                confirmPasswordValidLiveData,
                confirmPasswordErrorLiveData
        );
        updatePasswordFormValidity();
    }

    /**
     * Updates the password form validity state based on individual field validities.
     */
    private void updatePasswordFormValidity() {
        updateFormValidity(
                passwordFormValidLiveData,
                currentPasswordValidLiveData.getValue(),
                newPasswordValidLiveData.getValue(),
                confirmPasswordValidLiveData.getValue()
        );
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
            updatePasswordFormValidity();
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
     * @return The user ID, or Default if user not found
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