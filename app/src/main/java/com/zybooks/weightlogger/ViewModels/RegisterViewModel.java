package com.zybooks.weightlogger.ViewModels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.zybooks.weightlogger.Data.UserRepository;
import com.zybooks.weightlogger.Data.UserSessionManager;

/**
 * ViewModel for user registration with enhanced validation.
 * Extends BaseValidationViewModel to leverage centralized validation logic.
 */
public class RegisterViewModel extends BaseValidationViewModel {
    private final UserRepository userRepository;
    private final UserSessionManager sessionManager;

    // Status indicators
    private final MutableLiveData<Integer> passwordStrengthLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> registrationSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccessLiveData = new MutableLiveData<>();

    // Validation states
    private final MutableLiveData<Boolean> usernameValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> confirmPasswordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> goalWeightValidLiveData = new MutableLiveData<>(true); // Optional field
    private final MutableLiveData<Boolean> formValidLiveData = new MutableLiveData<>(false);

    // Error messages
    private final MutableLiveData<String> usernameErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> passwordErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> confirmPasswordErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> goalWeightErrorLiveData = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = new UserSessionManager(application);
    }

    // LiveData getters
    public LiveData<String> getStatusMessageLiveData() { return statusMessageLiveData; }
    public LiveData<Integer> getPasswordStrengthLiveData() { return passwordStrengthLiveData; }
    public LiveData<Boolean> getRegistrationSuccessLiveData() { return registrationSuccessLiveData; }
    public LiveData<Boolean> getFormValidLiveData() { return formValidLiveData; }
    public LiveData<String> getUsernameErrorLiveData() { return usernameErrorLiveData; }
    public LiveData<String> getPasswordErrorLiveData() { return passwordErrorLiveData; }
    public LiveData<String> getConfirmPasswordErrorLiveData() { return confirmPasswordErrorLiveData; }
    public LiveData<String> getGoalWeightErrorLiveData() { return goalWeightErrorLiveData; }

    /**
     * Validates username in real-time.
     *
     * @param username The username to validate
     */
    public void validateUsername(String username) {
        validationService.validateUsername(
                username,
                usernameValidLiveData,
                usernameErrorLiveData
        );
        updateFormValidity();
    }

    /**
     * Validates password in real-time and updates the password strength indicator.
     *
     * @param password The password to validate
     */
    public void validatePassword(String password) {
        validationService.validatePassword(
                password,
                passwordValidLiveData,
                passwordErrorLiveData,
                passwordStrengthLiveData
        );
        updateFormValidity();
    }

    /**
     * Validates that the confirmation password matches the original password.
     *
     * @param password The original password
     * @param confirmPassword The confirmation password
     */
    public void validateConfirmPassword(String password, String confirmPassword) {
        validationService.validatePasswordMatch(
                password,
                confirmPassword,
                confirmPasswordValidLiveData,
                confirmPasswordErrorLiveData
        );
        updateFormValidity();
    }

    /**
     * Validates goal weight input.
     * Since goal weight is optional, an empty string is considered valid.
     *
     * @param goalWeightStr The goal weight as a string
     */
    public void validateGoalWeight(String goalWeightStr) {
        // Goal weight is optional, so an empty string is valid
        if (goalWeightStr.isEmpty()) {
            goalWeightValidLiveData.setValue(true);
            goalWeightErrorLiveData.setValue(null);
            updateFormValidity();
            return;
        }

        validationService.validateWeight(
                goalWeightStr,
                goalWeightValidLiveData,
                goalWeightErrorLiveData
        );
        updateFormValidity();
    }

    /**
     * Updates the overall form validity based on individual field validities.
     */
    private void updateFormValidity() {
        super.updateFormValidity(
                formValidLiveData,
                usernameValidLiveData.getValue(),
                passwordValidLiveData.getValue(),
                confirmPasswordValidLiveData.getValue(),
                goalWeightValidLiveData.getValue()
        );
    }

    /**
     * Registers a new user after performing validation on all inputs.
     *
     * @param username The username for the new account
     * @param password The password for the new account
     * @param confirmPassword The confirmation password
     * @param goalWeightStr The goal weight as a string (optional)
     */
    public void registerUser(String username, String password, String confirmPassword, String goalWeightStr) {
        // Validate all fields
        validateUsername(username);
        validatePassword(password);
        validateConfirmPassword(password, confirmPassword);
        validateGoalWeight(goalWeightStr);

        // Check if form is valid
        if (Boolean.FALSE.equals(formValidLiveData.getValue())) {
            statusMessageLiveData.setValue("Please correct the errors before registering.");
            return;
        }

        // Check for username uniqueness
        if (userRepository.userExists(username)) {
            usernameErrorLiveData.setValue("This username is already taken.");
            usernameValidLiveData.setValue(false);
            updateFormValidity();
            statusMessageLiveData.setValue("Username already exists. Please choose another.");
            return;
        }

        // Process goal weight
        double goalWeight = 0;
        if (!goalWeightStr.isEmpty()) {
            try {
                goalWeight = Double.parseDouble(goalWeightStr);
            } catch (NumberFormatException e) {
                goalWeightErrorLiveData.setValue("Invalid weight format.");
                goalWeightValidLiveData.setValue(false);
                updateFormValidity();
                return;
            }
        }

        // Register the user
        boolean success = userRepository.insertUser(username, password, goalWeight);
        if (success) {
            statusMessageLiveData.setValue("Registration successful! You can now log in.");
            registrationSuccessLiveData.setValue(true);
            loginUser(username, password);
        } else {
            statusMessageLiveData.setValue("Registration failed. Please try again later.");
            registrationSuccessLiveData.setValue(false);
        }
    }

    /**
     * Attempts to log in the newly registered user.
     */
    private void loginUser(String username, String password) {
        if (userRepository.validateUser(username, password)) {
            sessionManager.saveLoginSession(username);
            loginSuccessLiveData.setValue(true);
        }
    }

    /**
     * Resets all validation states and error messages.
     */
    public void resetValidation() {
        usernameValidLiveData.setValue(false);
        passwordValidLiveData.setValue(false);
        confirmPasswordValidLiveData.setValue(false);
        goalWeightValidLiveData.setValue(true); // Optional field
        formValidLiveData.setValue(false);

        usernameErrorLiveData.setValue(null);
        passwordErrorLiveData.setValue(null);
        confirmPasswordErrorLiveData.setValue(null);
        goalWeightErrorLiveData.setValue(null);

        passwordStrengthLiveData.setValue(0);
    }
}