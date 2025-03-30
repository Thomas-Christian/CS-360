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
 * ViewModel for login operations with enhanced validation.
 * Handles user authentication and validation of login credentials.
 */
public class LoginViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final UserSessionManager sessionManager;
    private final MutableLiveData<String> statusMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dataRefreshNeededLiveData = new MutableLiveData<>();

    // Field validation
    private final MutableLiveData<Boolean> usernameValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> formValidLiveData = new MutableLiveData<>(false);

    // Field error messages
    private final MutableLiveData<String> usernameErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> passwordErrorLiveData = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = new UserSessionManager(application);
    }

    // LiveData getters
    public LiveData<String> getStatusMessageLiveData() { return statusMessageLiveData; }
    public LiveData<Boolean> getLoginSuccessLiveData() { return loginSuccessLiveData; }

//    public LiveData<Boolean> getUsernameValidLiveData() { return usernameValidLiveData; }
//    public LiveData<Boolean> getPasswordValidLiveData() { return passwordValidLiveData; }
    public LiveData<Boolean> getFormValidLiveData() { return formValidLiveData; }
    public LiveData<String> getUsernameErrorLiveData() { return usernameErrorLiveData; }
    public LiveData<String> getPasswordErrorLiveData() { return passwordErrorLiveData; }
    public LiveData<Boolean> getDataRefreshNeededLiveData() { return dataRefreshNeededLiveData; }

    /**
     * Validates the username field.
     * For login, we only require that the username is not empty and meets basic criteria.
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
     * Validates the password field.
     * For login, we only require that the password is not empty.
     *
     * @param password The password to validate
     */
    public void validatePassword(String password) {
        ValidationResult result = InputValidator.validateRequired(password, "Password");
        passwordValidLiveData.setValue(result.isValid());
        passwordErrorLiveData.setValue(result.isValid() ? null : result.getErrorMessage());
        updateFormValidity();
    }

    /**
     * Updates the overall form validity state based on individual field validities.
     */
    private void updateFormValidity() {
        Boolean usernameValid = usernameValidLiveData.getValue();
        Boolean passwordValid = passwordValidLiveData.getValue();

        boolean formValid = usernameValid != null && usernameValid &&
                passwordValid != null && passwordValid;

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
            dataRefreshNeededLiveData.setValue(true); // Signal that data needs refreshing
        } else {
            statusMessageLiveData.setValue("Invalid username or password.");
            loginSuccessLiveData.setValue(false);
        }
    }

    /**
     * Signals that the user wants to sign up for a new account.
     */
    public void signUp() {
        // Just signal that the signup screen should be shown
        statusMessageLiveData.setValue("NAVIGATE_TO_REGISTER");
    }

    /**
     * Resets all validation states and error messages.
     */
    public void resetValidation() {
        usernameValidLiveData.setValue(false);
        passwordValidLiveData.setValue(false);
        formValidLiveData.setValue(false);

        usernameErrorLiveData.setValue(null);
        passwordErrorLiveData.setValue(null);
    }
}