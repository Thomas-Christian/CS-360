package com.zybooks.weightlogger.Utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Centralized error handling utility that provides consistent error reporting,
 * logging, and user feedback throughout the application.
 */
public class ErrorHandler {
    private static final String TAG = "WeightLogger";

    // Error severity levels
    public enum Severity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    /**
     * Handles an exception with appropriate logging and optional user feedback.
     *
     * @param context The application context for showing UI feedback
     * @param e The exception to handle
     * @param component The component where the error occurred
     * @param operation The operation that failed
     * @param severity The severity of the error
     * @param userMessage Optional user-friendly message (null for no UI feedback)
     */
    public static void handleException(Context context, Exception e, String component,
                                       String operation, Severity severity, String userMessage) {
        // Construct a detailed log message
        String logMessage = String.format("%s failed in %s: %s", operation, component, e.getMessage());

        // Log based on severity
        switch (severity) {
            case INFO:
                Log.i(TAG, logMessage, e);
                break;
            case WARNING:
                Log.w(TAG, logMessage, e);
                break;
            case ERROR:
                Log.e(TAG, logMessage, e);
                break;
            case CRITICAL:
                Log.wtf(TAG, logMessage, e);
                break;
        }

        // Show user feedback if requested
        if (userMessage != null && context != null) {
            Toast.makeText(context, userMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Logs an error without an exception.
     *
     * @param component The component where the error occurred
     * @param message The error message
     * @param severity The severity of the error
     */
    public static void logError(String component, String message, Severity severity) {
        switch (severity) {
            case INFO:
                Log.i(TAG, component + ": " + message);
                break;
            case WARNING:
                Log.w(TAG, component + ": " + message);
                break;
            case ERROR:
                Log.e(TAG, component + ": " + message);
                break;
            case CRITICAL:
                Log.wtf(TAG, component + ": " + message);
                break;
        }
    }

    /**
     * Provides a user-friendly error message without logging.
     * Useful for validation errors and other expected conditions.
     *
     * @param context The application context
     * @param message The message to display
     */
    public static void showUserMessage(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}