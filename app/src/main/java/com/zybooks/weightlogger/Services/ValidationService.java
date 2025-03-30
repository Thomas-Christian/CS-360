package com.zybooks.weightlogger.Services;

import androidx.lifecycle.MutableLiveData;
import com.zybooks.weightlogger.Utilities.InputValidator;
import com.zybooks.weightlogger.Utilities.InputValidator.ValidationResult;

/**
 * Service class responsible for all form validation logic.
 * Centralizes validation to eliminate duplication across ViewModels.
 */
public class ValidationService {

    /**
     * Validates a username and updates the corresponding LiveData objects.
     *
     * @param username The username to validate
     * @param isValid LiveData to update with validation result
     * @param errorMessage LiveData to update with error message if invalid
     */
    public void validateUsername(String username,
                                 MutableLiveData<Boolean> isValid,
                                 MutableLiveData<String> errorMessage) {
        ValidationResult result = InputValidator.validateUsername(username);
        isValid.setValue(result.isValid());
        errorMessage.setValue(result.isValid() ? null : result.getErrorMessage());
    }

    /**
     * Validates a password and updates the corresponding LiveData objects.
     * Optionally updates password strength.
     *
     * @param password The password to validate
     * @param isValid LiveData to update with validation result
     * @param errorMessage LiveData to update with error message if invalid
     * @param strengthIndicator Optional LiveData to update with password strength
     */
    public void validatePassword(String password,
                                 MutableLiveData<Boolean> isValid,
                                 MutableLiveData<String> errorMessage,
                                 MutableLiveData<Integer> strengthIndicator) {
        ValidationResult result = InputValidator.validatePassword(password);
        isValid.setValue(result.isValid());
        errorMessage.setValue(result.isValid() ? null : result.getErrorMessage());

        if (strengthIndicator != null) {
            int strength = InputValidator.calculatePasswordStrength(password);
            strengthIndicator.setValue(strength);
        }
    }

    /**
     * Validates password confirmation matches the original password.
     *
     * @param password The original password
     * @param confirmPassword The confirmation password to validate
     * @param isValid LiveData to update with validation result
     * @param errorMessage LiveData to update with error message if invalid
     */
    public void validatePasswordMatch(String password,
                                      String confirmPassword,
                                      MutableLiveData<Boolean> isValid,
                                      MutableLiveData<String> errorMessage) {
        ValidationResult result = InputValidator.validatePasswordMatch(password, confirmPassword);
        isValid.setValue(result.isValid());
        errorMessage.setValue(result.isValid() ? null : result.getErrorMessage());
    }

    /**
     * Validates a weight value and updates the corresponding LiveData objects.
     *
     * @param weightStr The weight string to validate
     * @param isValid LiveData to update with validation result
     * @param errorMessage LiveData to update with error message if invalid
     */
    public void validateWeight(String weightStr,
                               MutableLiveData<Boolean> isValid,
                               MutableLiveData<String> errorMessage) {
        ValidationResult result = InputValidator.validateWeight(weightStr);
        isValid.setValue(result.isValid());
        errorMessage.setValue(result.isValid() ? null : result.getErrorMessage());
    }

    /**
     * Validates a date string and updates the corresponding LiveData objects.
     *
     * @param dateStr The date string to validate
     * @param isValid LiveData to update with validation result
     * @param errorMessage LiveData to update with error message if invalid
     */
    public void validateDate(String dateStr,
                             MutableLiveData<Boolean> isValid,
                             MutableLiveData<String> errorMessage) {
        ValidationResult result = InputValidator.validateDate(dateStr);
        isValid.setValue(result.isValid());
        errorMessage.setValue(result.isValid() ? null : result.getErrorMessage());
    }

    /**
     * Determines if a form is valid based on multiple validation states.
     *
     * @param validationStates Array of Boolean values representing validation states
     * @return true if all states are non-null and true, false otherwise
     */
    public boolean isFormValid(Boolean... validationStates) {
        for (Boolean state : validationStates) {
            if (state == null || !state) {
                return false;
            }
        }
        return true;
    }
}