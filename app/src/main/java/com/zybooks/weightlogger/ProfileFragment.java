package com.zybooks.weightlogger;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private TextView usernameTextView;
    private TextView goalWeightTextView;
    private Button logoutButton;
    private Button editGoalButton;
    private UserSessionManager sessionManager;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize session manager and database helper
        sessionManager = new UserSessionManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        // Initialize views
        usernameTextView = view.findViewById(R.id.usernameTextView);
        goalWeightTextView = view.findViewById(R.id.goalWeightTextView);
        editGoalButton = view.findViewById(R.id.editGoalButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Set up button click listeners
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> logout());
        }

        if (editGoalButton != null) {
            editGoalButton.setOnClickListener(v -> showEditGoalDialog());
        }

        // Display the username
        String username = sessionManager.getUsername();
        usernameTextView.setText("Username: " + username);

        // Update goal weight and progress display
        updateWeightGoalInfo();

        return view;
    }

    // Made public so it can be called from MainActivity
    public void updateWeightGoalInfo() {
        // Get user ID
        int userId = getUserId();
        if (userId == -1) return;

        // Get goal weight
        double goalWeight = dbHelper.getGoalWeight(userId);

        // Update UI
        if (goalWeight <= 0) {
            goalWeightTextView.setText("Goal Weight: Not set");
        } else {
            goalWeightTextView.setText(String.format(Locale.getDefault(),
                    "Goal Weight: %.1f lbs", goalWeight));
        }
    }

    private void showEditGoalDialog() {
        // Create dialog view
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_goal_weight, null);

        EditText goalWeightEditText = dialogView.findViewById(R.id.goalWeightEditText);

        // Get current goal weight to pre-fill
        int userId = getUserId();
        if (userId != -1) {
            double currentGoal = dbHelper.getGoalWeight(userId);
            if (currentGoal > 0) {
                goalWeightEditText.setText(String.format(Locale.getDefault(), "%.1f", currentGoal));
            }
        }

        // Show dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Set Goal Weight")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String goalStr = goalWeightEditText.getText().toString().trim();
                    if (goalStr.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a weight", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double goalWeight = Double.parseDouble(goalStr);
                        saveGoalWeight(goalWeight);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveGoalWeight(double goalWeight) {
        int userId = getUserId();
        if (userId == -1) return;

        boolean success = dbHelper.updateGoalWeight(userId, goalWeight);

        if (success) {
            Toast.makeText(requireContext(), "Goal weight updated", Toast.LENGTH_SHORT).show();
            updateWeightGoalInfo();
        } else {
            Toast.makeText(requireContext(), "Failed to update goal weight", Toast.LENGTH_SHORT).show();
        }
    }

    private int getUserId() {
        if (sessionManager.isLoggedIn()) {
            String username = sessionManager.getUsername();
            return dbHelper.getUserId(username);
        } else {
            // Use default user
            return dbHelper.getUserId("DefaultUser");
        }
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to login screen and update navigation bar
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.resetNavBarAfterLogout();
        }
    }
}