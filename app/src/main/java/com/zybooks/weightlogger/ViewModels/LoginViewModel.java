package com.zybooks.weightlogger.ViewModels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.zybooks.weightlogger.Data.UserRepository;
import com.zybooks.weightlogger.Data.UserSessionManager;

/**
 * ViewModel for login operations.
 */
public class LoginViewModel extends BaseValidationViewModel {
    private final UserRepository userRepository;
    private final UserSessionManager sessionManager;

    // LiveData for UI updates
    private final MutableLiveData<Boolean> loginSuccessLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dataRefreshNeededLiveData = new MutableLiveData<>();

    // Validation states
    private final MutableLiveData<Boolean> usernameValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordValidLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> formValidLiveData = new MutableLiveData<>(false);

    // Error messages
    private final MutableLiveData<String> usernameErrorLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> passwordErrorLiveData = new MutableLiveData<>();

    /**
     * Creates a new LoginViewModel.
     *
     * @param application The application context
     */
    public LoginViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        sessionManager = new UserSessionManager(application);
    }

    // Getters for LiveData
    public LiveData<String> getStatusMessageLiveData() { return statusMessageLiveData; }
    public LiveData<Boolean> getLoginSuccessLiveData() { return loginSuccessLiveData; }
    public LiveData<Boolean> getFormValidLiveData() { return formValidLiveData; }
    public LiveData<String> getUsernameErrorLiveData() { return usernameErrorLiveData; }
    public LiveData<String> getPasswordErrorLiveData() { return passwordErrorLiveData; }
    public LiveData<Boolean> getDataRefreshNeededLiveData() { return dataRefreshNeededLiveData; }

    /**
     * Validates the username field.
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
     * Validates the password field.
     *
     * @param password The password to validate
     */
    public void validatePassword(String password) {
        validationService.validatePassword(
                password,
                passwordValidLiveData,
                passwordErrorLiveData,
                null // No need for strength indicator in login
        );
        updateFormValidity();
    }

    /**
     * Updates the form validity state based on individual field validities.
     */
    private void updateFormValidity() {
        super.updateFormValidity(
                formValidLiveData,
                usernameValidLiveData.getValue(),
                passwordValidLiveData.getValue()
        );
    }

    /**
     * Attempts to log in a user with the provided credentials.
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
            dataRefreshNeededLiveData.setValue(true);
        } else {
            statusMessageLiveData.setValue("Invalid username or password.");
            loginSuccessLiveData.setValue(false);
        }
    }

    /**
     * Signals that the user wants to sign up for a new account.
     */
    public void signUp() {
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