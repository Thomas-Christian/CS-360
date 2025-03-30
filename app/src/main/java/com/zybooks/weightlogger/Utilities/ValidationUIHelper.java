package com.zybooks.weightlogger.Utilities;

import android.content.Context;
import android.content.res.ColorStateList;

import android.text.Editable;
import android.text.TextWatcher;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import com.zybooks.weightlogger.R;
import com.zybooks.weightlogger.Utilities.InputValidator.ValidationResult;

/**
 * UI helper class for implementing validation in the application interface.
 * Provides methods to set up validation for EditText fields and display validation results.
 * Integrates with the InputValidator utility class for input validation logic.
 */
public class ValidationUIHelper {

    /**
     * Sets up real-time validation for a text input field.
     * Shows error messages in the TextInputLayout when input is invalid.
     *
     * @param context The context for accessing resources
     * @param editText The EditText field to validate
     * @param textInputLayout The TextInputLayout that contains the EditText
     * @param validationType The type of validation to perform
     */
    public static void setupInputValidation(Context context, EditText editText,
                                            TextInputLayout textInputLayout, ValidationType validationType) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                ValidationResult result = performValidation(text, validationType);

                if (result.isValid()) {
                    textInputLayout.setError(null);
                    textInputLayout.setErrorEnabled(false);
                } else {
                    textInputLayout.setError(result.getErrorMessage());
                    textInputLayout.setErrorEnabled(true);
                }
            }
        });
    }

    /**
     * Sets up password strength visualization.
     * Updates a progress bar and text view to show password strength.
     *
     * @param editText The password EditText field
     * @param strengthBar The progress bar to display strength
     * @param strengthText The TextView to display strength text
     * @param context The context for accessing resources
     */
    public static void setupPasswordStrengthVisualization(EditText editText, ProgressBar strengthBar, TextView strengthText, Context context) {

        int strength_weak = 25;
        int strength_medium = 50;
        int strength_strong = 75;

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                int strength = InputValidator.calculatePasswordStrength(s.toString());
                strengthBar.setProgress(strength);

                // Update text and color based on strength
                if (strength < strength_weak) {
                    strengthText.setText(R.string.weak);
                    strengthText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    strengthBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.holo_red_dark)));
                } else if (strength < strength_medium) {
                    strengthText.setText(R.string.medium);
                    strengthText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                    strengthBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.holo_orange_dark)));
                } else if (strength < strength_strong) {
                    strengthText.setText(R.string.strong);
                    strengthText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
                    strengthBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.holo_green_light)));
                } else {
                    strengthText.setText(R.string.very_strong);
                    strengthText.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                    strengthBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.holo_green_dark)));
                }
            }
        });
    }

    /**
     * Sets up validation for a form submit button.
     * Enables/disables the button based on validation of all form fields.
     *
     * @param fields The array of EditText fields to validate
     * @param validationTypes The corresponding validation types for each field
     * @param submitButton The button to enable/disable
     */
    public static void setupFormValidation(EditText[] fields, ValidationType[] validationTypes,
                                           Button submitButton) {
        // Create a text watcher for each field
        for (EditText field : fields) {
            field.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not used
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Not used
                }

                @Override
                public void afterTextChanged(Editable s) {
                    validateAllFields(fields, validationTypes, submitButton);
                }
            });
        }

        // Initial validation
        validateAllFields(fields, validationTypes, submitButton);
    }

    /**
     * Validates all fields in a form and updates the submit button state.
     *
     * @param fields The array of EditText fields to validate
     * @param validationTypes The corresponding validation types for each field
     * @param submitButton The button to enable/disable
     */
    private static void validateAllFields(EditText[] fields, ValidationType[] validationTypes,
                                          Button submitButton) {
        boolean allValid = true;

        for (int i = 0; i < fields.length; i++) {
            String text = fields[i].getText().toString();
            ValidationResult result = performValidation(text, validationTypes[i]);

            if (!result.isValid()) {
                allValid = false;
                break;
            }
        }

        submitButton.setEnabled(allValid);
    }

    /**
     * Sets up password comparison validation.
     * Validates that the confirmation password matches the original password.
     *
     * @param passwordField The original password field
     * @param confirmField The confirmation password field
     * @param confirmLayout The TextInputLayout for the confirmation field
     */
    public static void setupPasswordComparison(EditText passwordField, EditText confirmField,
                                               TextInputLayout confirmLayout) {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = passwordField.getText().toString();
                String confirm = confirmField.getText().toString();

                if (confirm.isEmpty()) {
                    confirmLayout.setError(null);
                    confirmLayout.setErrorEnabled(false);
                    return;
                }

                ValidationResult result = InputValidator.validatePasswordMatch(password, confirm);
                if (result.isValid()) {
                    confirmLayout.setError(null);
                    confirmLayout.setErrorEnabled(false);
                } else {
                    confirmLayout.setError(result.getErrorMessage());
                    confirmLayout.setErrorEnabled(true);
                }
            }
        };

        // Add watcher to both fields since either changing should trigger validation
        passwordField.addTextChangedListener(watcher);
        confirmField.addTextChangedListener(watcher);
    }

    /**
     * Validates user input based on its type.
     * Delegates to the appropriate method in InputValidator.
     *
     * @param text The text to validate
     * @param validationType The type of validation to perform
     * @return A ValidationResult object with the validation status and any error message
     */
    private static ValidationResult performValidation(String text, ValidationType validationType) {
        switch (validationType) {
            case USERNAME:
                return InputValidator.validateUsername(text);
            case PASSWORD:
                return InputValidator.validatePassword(text);
            case WEIGHT:
                return InputValidator.validateWeight(text);
            case DATE:
                return InputValidator.validateDate(text);
            case EMAIL:
                return InputValidator.validateEmail(text);
            case PHONE:
                return InputValidator.validatePhoneNumber(text);
            case REQUIRED:
                return InputValidator.validateRequired(text, "Field");
            default:
                return new ValidationResult(true, "");
        }
    }

    /**
     * Enum defining the different types of validation supported by this helper.
     */
    public enum ValidationType {
        USERNAME,
        PASSWORD,
        WEIGHT,
        DATE,
        EMAIL,
        PHONE,
        REQUIRED
    }
}