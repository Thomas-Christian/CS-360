package com.zybooks.weightlogger.Utilities;

import android.content.Context;
import android.content.res.ColorStateList;

import android.text.Editable;
import android.text.TextWatcher;

import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.zybooks.weightlogger.R;

/**
 * UI helper class for implementing validation in the application interface.
 * Provides methods to set up validation for EditText fields and display validation results.
 * Integrates with the InputValidator utility class for input validation logic.
 */
public class ValidationUIHelper {

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

}