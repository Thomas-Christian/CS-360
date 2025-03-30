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

/**
 * ViewModel for user registration with enhanced validation.
 * Handles new user creation with real-time input validation.
 */
public class RegisterViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final UserSessionManager sessionManager;
    private final MutableLiveData<String> statusMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> passwordStrengthLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registrationSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccessLiveData = new MutableLiveData<>();

    // Field validity tracking
    private final MutableLiveData<Boolean> usernameValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> confirmPasswordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> goalWeightValidLiveData = new MutableLiveData<>(true); // Optional field
    private final MutableLiveData<Boolean> formValidLiveData = new MutableLiveData<>(false);

    // Field error messages
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

//    public LiveData<Boolean> getUsernameValidLiveData() { return usernameValidLiveData; }
//    public LiveData<Boolean> getPasswordValidLiveData() { return passwordValidLiveData; }
//    public LiveData<Boolean> getConfirmPasswordValidLiveData() { return confirmPasswordValidLiveData; }
//    public LiveData<Boolean> getGoalWeightValidLiveData() { return goalWeightValidLiveData; }
    public LiveData<Boolean> getFormValidLiveData() { return formValidLiveData; }

    public LiveData<String> getUsernameErrorLiveData() { return usernameErrorLiveData; }
    public LiveData<String> getPasswordErrorLiveData() { return passwordErrorLiveData; }
    public LiveData<String> getConfirmPasswordErrorLiveData() { return confirmPasswordErrorLiveData; }
    public LiveData<String> getGoalWeightErrorLiveData() { return goalWeightErrorLiveData; }

    /**
     * Validates username in real-time.
     * Updates usernameValidLiveData and usernameErrorLiveData based on validation results.
     *
     * @param username The username to validate
     */
    public void validateUsername(String username) {
        ValidationResult result = InputValidator.validateUsername(username);
        usernameValidLiveData.setValue(result.isValid());
        usernameErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
        updateFormValidity();
    }

    /**
     * Validates password in real-time and updates the password strength indicator.
     *
     * @param password The password to validate
     */
    public void validatePassword(String password) {
        ValidationResult result = InputValidator.validatePassword(password);
        passwordValidLiveData.setValue(result.isValid());
        passwordErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
        passwordStrengthLiveData.setValue(InputValidator.calculatePasswordStrength(password));
        updateFormValidity();
    }

    /**
     * Validates that the confirmation password matches the original password.
     *
     * @param password The original password
     * @param confirmPassword The confirmation password
     */
    public void validateConfirmPassword(String password, String confirmPassword) {
        ValidationResult result = InputValidator.validatePasswordMatch(password, confirmPassword);
        confirmPasswordValidLiveData.setValue(result.isValid());
        confirmPasswordErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
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

        ValidationResult result = InputValidator.validateWeight(goalWeightStr);
        goalWeightValidLiveData.setValue(result.isValid());
        goalWeightErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
        updateFormValidity();
    }

    /**
     * Updates the overall form validity based on individual field validities.
     */
    private void updateFormValidity() {
        Boolean usernameValid = usernameValidLiveData.getValue();
        Boolean passwordValid = passwordValidLiveData.getValue();
        Boolean confirmPasswordValid = confirmPasswordValidLiveData.getValue();
        Boolean goalWeightValid = goalWeightValidLiveData.getValue();

        boolean formValid = usernameValid != null && usernameValid &&
                passwordValid != null && passwordValid &&
                confirmPasswordValid != null && confirmPasswordValid &&
                goalWeightValid != null && goalWeightValid;

        formValidLiveData.setValue(formValid);
    }

    /**
     * Attempts to log in a user with the provided credentials.
     * Performs validation before attempting authentication.
     *
     * @param username The username for authentication
     * @param password The password for authentication
     */
    public void loginUser(String username, String password) {
        // Validate inputs
        validateUsername(username);
        validatePassword(password);

        // Check form validity
        if (Boolean.FALSE.equals(formValidLiveData.getValue())) {
            statusMessageLiveData.setValue("Please enter both username and password.");
            return;
        }

        // Attempt authentication
        if (userRepository.validateUser(username, password)) {
            sessionManager.saveLoginSession(username);
            statusMessageLiveData.setValue("Login successful!");
            loginSuccessLiveData.setValue(true);
        } else {
            statusMessageLiveData.setValue("Invalid username or password.");
            loginSuccessLiveData.setValue(false);
        }
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
            formValidLiveData.setValue(false);
            statusMessageLiveData.setValue("Username already exists. Please choose another.");
            return;
        }

        // Process goal weight
        double goalWeight = 0;
        if (!goalWeightStr.isEmpty()) {
            try {
                goalWeight = Double.parseDouble(goalWeightStr);
            } catch (NumberFormatException e) {
                // This shouldn't happen due to prior validation
                goalWeightErrorLiveData.setValue("Invalid weight format.");
                goalWeightValidLiveData.setValue(false);
                formValidLiveData.setValue(false);
                return;
            }
        }

        // Register the user
        boolean success = userRepository.insertUser(username, password, goalWeight);
        if (success) {
            statusMessageLiveData.setValue("Registration successful! You can now log in.");
            registrationSuccessLiveData.setValue(true);
            try {
                loginUser(username, password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            statusMessageLiveData.setValue("Registration failed. Please try again later.");
            registrationSuccessLiveData.setValue(false);
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