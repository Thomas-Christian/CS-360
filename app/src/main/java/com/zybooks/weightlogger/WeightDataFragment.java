package com.zybooks.weightlogger;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeightDataFragment extends Fragment {

    private GridView weightGridView;
    private EditText dateEditText, weightEditText;
    private Button addWeightButton;
    private DatabaseHelper dbHelper;
    private UserSessionManager sessionManager;
    private NotificationHelper notificationHelper;
    private WeightEntryAdapter adapter;

    // Define threshold for "getting close" to goal
    private static final double GOAL_PROXIMITY_THRESHOLD = 5.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weight_data, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new UserSessionManager(requireContext());
        notificationHelper = new NotificationHelper(requireContext());

        weightGridView = view.findViewById(R.id.weightGridView);
        dateEditText = view.findViewById(R.id.dateEditText);
        weightEditText = view.findViewById(R.id.weightEditText);
        addWeightButton = view.findViewById(R.id.addWeightButton);

        // Set current date as default
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateEditText.setText(dateFormat.format(new Date()));

        addWeightButton.setOnClickListener(v -> addWeightEntry());

        // Set up the grid adapter
        loadWeightData();

        return view;
    }

    // Make this public so the adapter can call it
    public void loadWeightData() {
        int userId;

        if (sessionManager.isLoggedIn()) {
            // Use logged-in user's ID
            String username = sessionManager.getUsername();
            userId = dbHelper.getUserId(username);

            if (userId == -1) {
                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Use the default user ID
            userId = dbHelper.getUserId("DefaultUser");
        }

        List<DatabaseHelper.WeightEntry> entries = dbHelper.getWeightEntries(userId);

        // Set up the grid with the entries
        if (entries.isEmpty()) {
            weightGridView.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No weight entries yet", Toast.LENGTH_SHORT).show();
        } else {
            weightGridView.setVisibility(View.VISIBLE);

            // Use the custom adapter
            adapter = new WeightEntryAdapter(requireContext(), entries, dbHelper, this);
            weightGridView.setAdapter(adapter);

            // Set to 1 column since each item now contains date, weight, and buttons
            weightGridView.setNumColumns(1);
        }
    }

    private void addWeightEntry() {
        String dateStr = dateEditText.getText().toString().trim();
        String weightStr = weightEditText.getText().toString().trim();

        if (dateStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter both date and weight", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double weight = Double.parseDouble(weightStr);

            int userId;

            if (sessionManager.isLoggedIn()) {
                // Use logged-in user's ID
                String username = sessionManager.getUsername();
                userId = dbHelper.getUserId(username);

                if (userId == -1) {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Use the default user ID
                userId = dbHelper.getUserId("DefaultUser");
            }

            boolean success = dbHelper.addWeightEntry(userId, dateStr, weight);

            if (success) {
                Toast.makeText(getContext(), "Weight entry added successfully", Toast.LENGTH_SHORT).show();
                weightEditText.setText("");
                loadWeightData(); // Refresh the data

                // Check if user has reached or is close to their goal weight
                checkWeightGoalProgress(userId, weight);

                // Update the profile fragment if it's visible
                MainActivity.updateProfileIfVisible();
            } else {
                Toast.makeText(getContext(), "Failed to add weight entry", Toast.LENGTH_SHORT).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid weight", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkWeightGoalProgress(int userId, double currentWeight) {
        // Get the user's goal weight from the database
        double goalWeight = dbHelper.getGoalWeight(userId);

        // If no goal weight is set or it's invalid, return
        if (goalWeight <= 0) {
            return;
        }

        // Calculate the difference between current and goal weight
        double difference = Math.abs(currentWeight - goalWeight);

        // If user has reached their goal
        if (difference <= 0.000) {
            // Send goal achieved notification
            notificationHelper.sendGoalAchievedNotification();

        }
        // If user is getting close to their goal (within the threshold)
        else if (difference <= GOAL_PROXIMITY_THRESHOLD) {
            // Send progress notification
            notificationHelper.sendGoalProgressNotification(currentWeight, goalWeight);
        }
    }
}