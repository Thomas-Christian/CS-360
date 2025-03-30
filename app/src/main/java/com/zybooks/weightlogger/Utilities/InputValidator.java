package com.zybooks.weightlogger.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for validating all user inputs in the application.
 * Provides methods to validate usernames, passwords, weight values, dates, and more.
 * All methods are static for easy access throughout the application.
 */
public class InputValidator {

    /**
     * Validation constants
     */
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 24;
    private static final double MIN_WEIGHT_VALUE = 1.0;
    private static final double MAX_WEIGHT_VALUE = 1000.0;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Regex patterns
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern PASSWORD_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_NUMBER = Pattern.compile("\\d");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile("[^A-Za-z0-9]");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
    private static final Pattern WEIGHT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");

    /**
     * Validation result class to hold both the validation status and any error message.
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return isValid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * Validates a username.
     * Username must:
     * - Be between MIN_USERNAME_LENGTH and MAX_USERNAME_LENGTH characters long
     * - Contain only alphanumeric characters and underscores
     *
     * @param username The username to validate
     * @return A ValidationResult object with the validation status and any error message
     */
    public static ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "Username cannot be empty");
        }

        username = username.trim();

        if (username.length() < MIN_USERNAME_LENGTH) {
            return new ValidationResult(false, "Username must be at least " + MIN_USERNAME_LENGTH + " characters long");
        }

        if (username.length() > MAX_USERNAME_LENGTH) {
            return new ValidationResult(false, "Username cannot exceed " + MAX_USERNAME_LENGTH + " characters");
        }

        Matcher matcher = USERNAME_PATTERN.matcher(username);
        if (!matcher.matches()) {
            return new ValidationResult(false, "Username can only contain letters, numbers, and underscores");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Validates a password.
     * Password must:
     * - Be between MIN_PASSWORD_LENGTH and MAX_PASSWORD_LENGTH characters long
     * - Contain at least one lowercase letter, uppercase letter, number, and special character
     *
     * @param password The password to validate
     * @return A ValidationResult object with the validation status and any error message
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new ValidationResult(false, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            return new ValidationResult(false, "Password cannot exceed " + MAX_PASSWORD_LENGTH + " characters");
        }

        int strengthScore = 0;
        StringBuilder requirements = new StringBuilder();

        boolean hasLowercase = PASSWORD_LOWERCASE.matcher(password).find();
        boolean hasUppercase = PASSWORD_UPPERCASE.matcher(password).find();
        boolean hasNumber = PASSWORD_NUMBER.matcher(password).find();
        boolean hasSpecial = PASSWORD_SPECIAL.matcher(password).find();

        if (hasLowercase) {
            strengthScore++;
        } else {
            requirements.append("- Must contain at least one lowercase letter\n");
        }

        if (hasUppercase) {
            strengthScore++;
        } else {
            requirements.append("- Must contain at least one uppercase letter\n");
        }

        if (hasNumber) {
            strengthScore++;
        } else {
            requirements.append("- Must contain at least one number\n");
        }

        if (hasSpecial) {
            strengthScore++;
        } else {
            requirements.append("- Must contain at least one special character\n");
        }

        // Require all 4 complexity requirements
        if (strengthScore < 4) {
            return new ValidationResult(false, "Password must meet the following requirements:\n" + requirements);
        }

        return new ValidationResult(true, "");
    }

    /**
     * Calculates password strength on a scale of 0-100.
     * Useful for password strength indicators.
     *
     * @param password The password to evaluate
     * @return An integer from 0-100 representing password strength
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Basic length check (up to 25 points)
        score += Math.min(25, (password.length() * 2));

        // Check for character types
        boolean hasLowercase = PASSWORD_LOWERCASE.matcher(password).find();
        boolean hasUppercase = PASSWORD_UPPERCASE.matcher(password).find();
        boolean hasNumber = PASSWORD_NUMBER.matcher(password).find();
        boolean hasSpecial = PASSWORD_SPECIAL.matcher(password).find();

        // Add points for complexity (each type worth 15 points)
        if (hasLowercase) score += 15;
        if (hasUppercase) score += 15;
        if (hasNumber) score += 15;
        if (hasSpecial) score += 15;

        // Extra points for good length
        if (password.length() >= 12) score += 10;

        // Ensure "Very Strong" (75+ points) ONLY shows when all 4 criteria are met
        if (score >= 75 && !(hasLowercase && hasUppercase && hasNumber && hasSpecial)) {
            // Cap at 74 if not all criteria are met
            score = 74;
        }

        return score;
    }

    /**
     * Validates that the confirmation password matches the original password.
     *
     * @param password The original password
     * @param confirmPassword The confirmation password
     * @return A ValidationResult object with the validation status and any error message
     */
    public static ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return new ValidationResult(false, "Passwords cannot be null");
        }

        if (!password.equals(confirmPassword)) {
            return new ValidationResult(false, "Passwords do not match");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Validates a weight value.
     * Weight must:
     * - Be a positive number
     * - Be within reasonable limits (MIN_WEIGHT_VALUE to MAX_WEIGHT_VALUE)
     * - Have at most 2 decimal places
     *
     * @param weightStr The weight string to validate
     * @return A ValidationResult object with the validation status and any error message
     */
    public static ValidationResult validateWeight(String weightStr) {
        if (weightStr == null || weightStr.trim().isEmpty()) {
            return new ValidationResult(false, "Weight cannot be empty");
        }

        weightStr = weightStr.trim();

        // Check for valid format (number with up to 2 decimal places)
        if (!WEIGHT_PATTERN.matcher(weightStr).matches()) {
            return new ValidationResult(false, "Weight must be a number with at most 2 decimal places");
        }

        try {
            double weight = Double.parseDouble(weightStr);

            if (weight < MIN_WEIGHT_VALUE) {
                return new ValidationResult(false, "Weight must be at least " + MIN_WEIGHT_VALUE);
            }

            if (weight > MAX_WEIGHT_VALUE) {
                return new ValidationResult(false, "Weight cannot exceed " + MAX_WEIGHT_VALUE);
            }

            return new ValidationResult(true, "");
        } catch (NumberFormatException e) {
            return new ValidationResult(false, "Invalid weight format");
        }
    }

    /**
     * Validates a date string.
     * Date must:
     * - Be in the format specified by DATE_FORMAT (yyyy-MM-dd)
     * - Be a valid date (e.g., not 2023-02-30)
     *
     * @param dateStr The date string to validate
     * @return A ValidationResult object with the validation status and any error message
     */
    public static ValidationResult validateDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return new ValidationResult(false, "Date cannot be empty");
        }

        dateStr = dateStr.trim();

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        dateFormat.setLenient(false); // This makes the date parser strict

        try {
            Date parsedDate = dateFormat.parse(dateStr);

            // Check if the date is too far in the future (more than 1 day)
            Date tomorrow = new Date(System.currentTimeMillis() + 86400000); // Current time + 1 day
            if (parsedDate != null && parsedDate.after(tomorrow)) {
                return new ValidationResult(false, "Date cannot be in the future");
            }

            return new ValidationResult(true, "");
        } catch (ParseException e) {
            return new ValidationResult(false, "Invalid date format. Please use " + DATE_FORMAT);
        }
    }

    /**
     * Validates an email address.
     * Email must follow standard email format.
     *
     * @param email The email to validate
     * @return A ValidationResult object with the validation status and any error message
     */
    public static ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email cannot be empty");
        }

        email = email.trim();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new ValidationResult(false, "Invalid email format");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Validates a phone number.
     * Phone number must:
     * - Contain only digits, spaces, parentheses, dashes, and plus signs
     * - Have a reasonable length
     *
     * @param phoneNumber The phone number to validate
     * @return A ValidationResult object with the validation status and any error message
     */
    public static ValidationResult validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return new ValidationResult(false, "Phone number cannot be empty");
        }

        phoneNumber = phoneNumber.trim();

        // Remove all non-digit characters to count actual digits
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");

        if (digitsOnly.length() < 7) {
            return new ValidationResult(false, "Phone number must have at least 7 digits");
        }

        if (digitsOnly.length() > 15) {
            return new ValidationResult(false, "Phone number has too many digits");
        }

        // Verify that only valid characters are used
        if (!phoneNumber.matches("^[0-9+\\-\\s()]+$")) {
            return new ValidationResult(false, "Phone number contains invalid characters");
        }

        return new ValidationResult(true, "");
    }

    /**
     * Validates that a required field is not empty.
     *
     * @param text The text to validate
     * @param fieldName The name of the field (for error message)
     * @return A ValidationResult object with the validation status and any error message
     */
    public static ValidationResult validateRequired(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            return new ValidationResult(false, fieldName + " is required");
        }

        return new ValidationResult(true, "");
    }

}