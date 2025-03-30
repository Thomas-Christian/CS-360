package com.zybooks.weightlogger.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.zybooks.weightlogger.MainActivity;
import com.zybooks.weightlogger.R;
import com.zybooks.weightlogger.Utilities.ValidationUIHelper;
import com.zybooks.weightlogger.ViewModels.ProfileViewModel;

/**
 * Fragment for managing user profile with enhanced validation.
 * Handles goal weight updates and password changes with validation.
 */
public class ProfileFragment extends Fragment {

    private TextView usernameTextView;
    private TextView goalWeightTextView;
    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Initialize views
        usernameTextView = view.findViewById(R.id.usernameTextView);
        goalWeightTextView = view.findViewById(R.id.goalWeightTextView);
        Button editGoalButton = view.findViewById(R.id.editGoalButton);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);

        // Set up button click listeners
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> viewModel.logout());
        }

        if (editGoalButton != null) {
            editGoalButton.setOnClickListener(v -> showEditGoalDialog());
        }

        // Add listener for change password button if it exists
        if (changePasswordButton != null) {
            changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        }

        setupLiveDataObservers();

        return view;
    }

    // Public method for MainActivity to call
    public void updateWeightGoalInfo() {
        viewModel.updateWeightGoalInfo();
    }

    /**
     * Sets up observers for ViewModel LiveData.
     */
    private void setupLiveDataObservers() {
        // Observe username
        viewModel.getUsernameLiveData().observe(getViewLifecycleOwner(), username -> {
            if (username != null) {
                usernameTextView.setText(String.format("%s%s", getString(R.string.username), username));
            }
        });

        // Observe goal weight text
        viewModel.getGoalWeightTextLiveData().observe(getViewLifecycleOwner(), goalText -> {
            if (goalText != null) {
                goalWeightTextView.setText(goalText);
            }
        });

        // Observe status messages
        viewModel.getStatusMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe logout
        viewModel.getLogoutLiveData().observe(getViewLifecycleOwner(), loggedOut -> {
            if (loggedOut != null && loggedOut) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.resetNavBarAfterLogout();
                }
            }
        });
    }

    /**
     * Shows a dialog for editing goal weight with validation.
     */
    private void showEditGoalDialog() {
        // Create dialog view
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_goal_weight, null);

        EditText goalWeightEditText = dialogView.findViewById(R.id.goalWeightEditText);
        TextInputLayout goalWeightLayout = dialogView.findViewById(R.id.goalWeightLayout);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Set Goal Weight")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .create();

        // Set up validation
        goalWeightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateGoalWeight(s.toString());
            }
        });

        // Observe validation results
        viewModel.getGoalWeightValidLiveData().observe(getViewLifecycleOwner(), saveButton::setEnabled);

        viewModel.getGoalWeightErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            goalWeightLayout.setError(error);
            goalWeightLayout.setErrorEnabled(error != null);
        });

        // Set up save button
        saveButton.setOnClickListener(v -> {
            String goalStr = goalWeightEditText.getText().toString().trim();
            viewModel.saveGoalWeight(goalStr);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Shows a dialog for changing password with validation.
     */
    private void showChangePasswordDialog() {
        // Create dialog view
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null);

        // Initialize dialog components
        EditText currentPasswordEditText = dialogView.findViewById(R.id.currentPasswordEditText);
        EditText newPasswordEditText = dialogView.findViewById(R.id.newPasswordEditText);
        EditText confirmNewPasswordEditText = dialogView.findViewById(R.id.confirmNewPasswordEditText);

        TextInputLayout currentPasswordLayout = dialogView.findViewById(R.id.currentPasswordLayout);
        TextInputLayout newPasswordLayout = dialogView.findViewById(R.id.newPasswordLayout);
        TextInputLayout confirmNewPasswordLayout = dialogView.findViewById(R.id.confirmNewPasswordLayout);

        ProgressBar passwordStrengthBar = dialogView.findViewById(R.id.passwordStrengthBar);
        TextView passwordStrengthText = dialogView.findViewById(R.id.passwordStrengthText);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Change Password")
                .setView(dialogView)
                .setNegativeButton("Cancel", (d, which) -> {
                    viewModel.resetPasswordValidation();
                    d.dismiss();
                })
                .create();

        // Set up password strength visualization
        if (passwordStrengthBar != null && passwordStrengthText != null) {
            ValidationUIHelper.setupPasswordStrengthVisualization(
                    newPasswordEditText, passwordStrengthBar, passwordStrengthText, requireContext());
        }

        // Set up validation for current password
        currentPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateCurrentPassword(s.toString());
            }
        });

        // Set up validation for new password
        newPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String newPassword = s.toString();
                viewModel.validateNewPassword(newPassword);

                // Also validate confirm new password if it has content
                String confirmPassword = confirmNewPasswordEditText.getText().toString();
                if (!confirmPassword.isEmpty()) {
                    viewModel.validateConfirmPassword(newPassword, confirmPassword);
                }
            }
        });

        // Set up validation for confirm new password
        confirmNewPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateConfirmPassword(
                        newPasswordEditText.getText().toString(), s.toString());
            }
        });

        // Observe validation results
        viewModel.getCurrentPasswordErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            currentPasswordLayout.setError(error);
            currentPasswordLayout.setErrorEnabled(error != null);
        });

        viewModel.getNewPasswordErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            newPasswordLayout.setError(error);
            newPasswordLayout.setErrorEnabled(error != null);
        });

        viewModel.getConfirmPasswordErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            confirmNewPasswordLayout.setError(error);
            confirmNewPasswordLayout.setErrorEnabled(error != null);
        });

        viewModel.getPasswordFormValidLiveData().observe(getViewLifecycleOwner(), saveButton::setEnabled);

        // Set up password strength indicator
        viewModel.getPasswordStrengthLiveData().observe(getViewLifecycleOwner(), strength -> {
            if (passwordStrengthBar != null) {
                passwordStrengthBar.setProgress(strength);
            }

            if (passwordStrengthText != null) {
                if (strength < 25) {
                    passwordStrengthText.setText(R.string.weak);
                    passwordStrengthText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
                } else if (strength < 50) {
                    passwordStrengthText.setText(R.string.medium);
                    passwordStrengthText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark));
                } else if (strength < 75) {
                    passwordStrengthText.setText(R.string.strong);
                    passwordStrengthText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light));
                } else {
                    passwordStrengthText.setText(R.string.very_strong);
                    passwordStrengthText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
                }
            }
        });

        // Set up save button
        saveButton.setOnClickListener(v -> {
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

            viewModel.changePassword(currentPassword, newPassword, confirmNewPassword);
            dialog.dismiss();
        });

        dialog.show();

        // Reset password validation when dialog is dismissed
        dialog.setOnDismissListener(dialog1 -> viewModel.resetPasswordValidation());
    }
}