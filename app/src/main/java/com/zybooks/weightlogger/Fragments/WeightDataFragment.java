package com.zybooks.weightlogger.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.zybooks.weightlogger.Data.UserRepository;
import com.zybooks.weightlogger.Data.UserSessionManager;
import com.zybooks.weightlogger.MainActivity;
import com.zybooks.weightlogger.R;
import com.zybooks.weightlogger.Utilities.WeightAdapter;
import com.zybooks.weightlogger.Utilities.WeightChartView;
import com.zybooks.weightlogger.ViewModels.MainViewModel;
import com.zybooks.weightlogger.ViewModels.WeightDataViewModel;
import com.zybooks.weightlogger.Data.WeightDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for managing weight data entries with enhanced validation.
 * Handles adding, editing, and deleting weight records with validation.
 */
public class WeightDataFragment extends Fragment {

    private RecyclerView weightRecyclerView;
    private EditText dateEditText, weightEditText;
    private TextInputLayout dateLayout, weightLayout;
    private TextView currentWeightValue;
    private TextView goalWeightValue;
    private LinearProgressIndicator progressIndicator;
    private TextView progressText;
    private Button addWeightButton;
    private WeightDataViewModel viewModel;
    private WeightAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weight_data, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(WeightDataViewModel.class);

        MainViewModel mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        mainViewModel.getIsLoggedInLiveData().observe(getViewLifecycleOwner(), isLoggedIn -> {
            // When login state changes, refresh the data
            viewModel.refreshDataAfterLogin();
        });

        // Initialize UI components
        weightRecyclerView = view.findViewById(R.id.weightRecyclerView);
        dateEditText = view.findViewById(R.id.dateEditText);
        weightEditText = view.findViewById(R.id.weightEditText);
        dateLayout = view.findViewById(R.id.dateLayout);
        weightLayout = view.findViewById(R.id.weightLayout);
        addWeightButton = view.findViewById(R.id.addWeightButton);
        currentWeightValue = view.findViewById(R.id.currentWeightValue);
        goalWeightValue = view.findViewById(R.id.goalWeightValue);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        progressText = view.findViewById(R.id.progressText);
        ExtendedFloatingActionButton chartFab = view.findViewById(R.id.chartFab);

        // Set up chart button click listener
        chartFab.setOnClickListener(v -> showWeightChart());

        // Set current date as default
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateEditText.setText(dateFormat.format(new Date()));

        // Set up validation
        setupValidation();

        // Set up button click listener
        addWeightButton.setOnClickListener(v -> {
            viewModel.addWeightEntry(
                    dateEditText.getText().toString().trim(),
                    weightEditText.getText().toString().trim()
            );
            // Clear weight input field after submission (if successful)
            if (Boolean.TRUE.equals(viewModel.getFormValidLiveData().getValue())) {
                weightEditText.setText("");
            }
        });

        // Setup RecyclerView
        setupRecyclerView();

        // Observe LiveData from ViewModel
        setupLiveDataObservers();

        return view;
    }

    /**
     * Sets up the RecyclerView with adapter and layout manager
     */
    private void setupRecyclerView() {
        // Set layout manager
        weightRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with empty list
        adapter = new WeightAdapter(getContext(), new ArrayList<>(), viewModel);
        weightRecyclerView.setAdapter(adapter);

        // Set up adapter click listeners
        adapter.setOnWeightEntryActionListener(new WeightAdapter.OnWeightEntryActionListener() {
            @Override
            public void onEditClick(WeightDatabaseHelper.WeightEntry entry, int position) {
                showEditDialog(entry);
            }

            @Override
            public void onDeleteClick(WeightDatabaseHelper.WeightEntry entry, int position) {
                // Show confirmation dialog before deleting
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.confirm_delete_message)
                        .setPositiveButton(R.string.delete, (dialog, which) -> viewModel.deleteWeightEntry(entry.getId()))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
    }

    /**
     * Sets up validation for input fields.
     */
    private void setupValidation() {
        // Date validation
        dateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateDate(s.toString().trim());
            }
        });

        // Weight validation
        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateWeight(s.toString().trim());
            }
        });
    }

    /**
     * Sets up observers for ViewModel LiveData.
     */
    private void setupLiveDataObservers() {
        // Observe date validation
        viewModel.getDateErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            dateLayout.setError(error);
            dateLayout.setErrorEnabled(error != null);
        });

        // Observe weight validation
        viewModel.getWeightErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            weightLayout.setError(error);
            weightLayout.setErrorEnabled(error != null);
        });

        // Observe form validity for button enabling
        viewModel.getFormValidLiveData().observe(getViewLifecycleOwner(), isValid -> addWeightButton.setEnabled(isValid));

        // Observe weight entries data for the RecyclerView
        viewModel.getWeightEntriesLiveData().observe(getViewLifecycleOwner(), this::updateWeightEntriesUI);

        // Observe status messages for toast notifications
        viewModel.getStatusMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe profile update signals
        viewModel.getProfileUpdateNeededLiveData().observe(getViewLifecycleOwner(), updateNeeded -> {
            if (updateNeeded != null && updateNeeded) {
                // Update the profile fragment if it's visible
                MainActivity.updateProfileIfVisible();
            }
        });

        viewModel.getWeightEntriesLiveData().observe(getViewLifecycleOwner(), entries -> {
            // Existing code for updating weight entries UI
            updateWeightEntriesUI(entries);

            // NEW: Update progress card whenever entries change
            updateProgressCard();
        });
    }

    /**
     * Updates the UI with the new list of weight entries.
     *
     * @param entries The list of weight entries to display
     */
    private void updateWeightEntriesUI(List<WeightDatabaseHelper.WeightEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            weightRecyclerView.setVisibility(View.GONE);
        } else {
            weightRecyclerView.setVisibility(View.VISIBLE);
            adapter.updateData(entries);
        }
    }

    /**
     * Updates the progress card with the current weight, goal weight, and progress.
     */

    private void updateProgressCard() {
        // Get user ID
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        UserRepository userRepository = new UserRepository(requireContext());
        int userId = getUserId(sessionManager, userRepository);

        if (userId == -1) {
            // Handle case where user is not found
            resetProgressCard();
            return;
        }

        // Get goal weight
        double goalWeight = userRepository.getGoalWeight(userId);
        if (goalWeight <= 0) {
            // Handle case where goal weight is not set
            resetProgressCard();
            goalWeightValue.setText(R.string.goal_weight_not_set);
            return;
        }

        // Get current weight (most recent entry)
        List<WeightDatabaseHelper.WeightEntry> entries =
                viewModel.getWeightEntriesLiveData().getValue();
        if (entries == null || entries.isEmpty()) {
            // Handle case where no weight entries exist
            resetProgressCard();
            currentWeightValue.setText(R.string.no_data);
            return;
        }

        // Get most recent weight entry
        double currentWeight = entries.get(0).getWeight();

        // Update UI elements
        currentWeightValue.setText(String.format(Locale.getDefault(), "%.1f lbs", currentWeight));
        goalWeightValue.setText(String.format(Locale.getDefault(), "%.1f lbs", goalWeight));

        // Calculate and display progress
        updateProgressIndicator(currentWeight, goalWeight);
    }

    /**
     * Updates the progress indicator and text based on current and goal weights.
     */
    private void updateProgressIndicator(double currentWeight, double goalWeight) {
        // Determine if goal is to lose or gain weight
        boolean isWeightLoss = currentWeight > goalWeight;

        // Calculate progress percentage
        double difference = Math.abs(currentWeight - goalWeight);
        double progressValue;

        double startWeight = getStartWeight();

        if (isWeightLoss) {
            // For weight loss goal
            if (startWeight <= goalWeight) {
                // Invalid start weight
                resetProgressCard();
                return;
            }

            double totalToLose = startWeight - goalWeight;
            double lost = startWeight - currentWeight;
            progressValue = (lost / totalToLose) * 100;
        } else {
            // For weight gain goal
            if (startWeight >= goalWeight) {
                // Invalid start weight
                resetProgressCard();
                return;
            }

            double totalToGain = goalWeight - startWeight;
            double gained = currentWeight - startWeight;
            progressValue = (gained / totalToGain) * 100;
        }

        // Cap progress at 100%
        progressValue = Math.min(100, Math.max(0, progressValue));

        // Update progress indicator
        progressIndicator.setProgress((int) progressValue);

        // Update progress text
        if (progressValue >= 100) {
            progressText.setText(R.string.goal_achieved);
            progressText.setTextColor(ContextCompat.getColor(requireContext(), R.color.teal_700));
        } else {
            double remaining = Math.abs(currentWeight - goalWeight);
            progressText.setText(String.format(Locale.getDefault(),
                    "%.1f lbs to %s!",
                    remaining,
                    isWeightLoss ? "lose" : "gain"));
        }
    }

    /**
     * Gets the starting weight (oldest entry) for progress calculation.
     */
    private double getStartWeight() {
        List<WeightDatabaseHelper.WeightEntry> entries =
                viewModel.getWeightEntriesLiveData().getValue();
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        // Get oldest entry (last in the list)
        return entries.get(entries.size() - 1).getWeight();
    }

    /**
     * Resets the progress card to its default state.
     */
    private void resetProgressCard() {
        currentWeightValue.setText("--");
        goalWeightValue.setText("--");
        progressIndicator.setProgress(0);
        progressText.setText(R.string.set_a_goal_weight);
    }

    /**
     * Shows a dialog with a chart of weight history.
     */
    private void showWeightChart() {
        List<WeightDatabaseHelper.WeightEntry> entries =
                viewModel.getWeightEntriesLiveData().getValue();

        if (entries == null || entries.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No weight data available to chart",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a dialog with a chart
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_weight_chart, null);

        // Find the chart view
        WeightChartView chartView = dialogView.findViewById(R.id.weightChartView);

        // Get goal weight
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        UserRepository userRepository = new UserRepository(requireContext());
        int userId = getUserId(sessionManager, userRepository);
        double goalWeight = userRepository.getGoalWeight(userId);

        // Set the data
        chartView.setData(entries, goalWeight);

        // Create and show dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Weight Progress Chart")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    /**
     * Helper method to get user ID.
     */
    private int getUserId(UserSessionManager sessionManager, UserRepository userRepository) {
        if (sessionManager.isLoggedIn()) {
            String username = sessionManager.getUsername();
            return userRepository.getUserId(username);
        } else {
            return userRepository.getUserId("DefaultUser");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Validate initial date input since it's pre-filled
        viewModel.validateDate(dateEditText.getText().toString().trim());
    }

    /**
     * Shows a dialog for editing a weight entry with validation.
     *
     * @param entry The weight entry to edit
     */
    private void showEditDialog(WeightDatabaseHelper.WeightEntry entry) {
        // Create dialog view
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_weight, null);
        EditText dateEditText = dialogView.findViewById(R.id.editDateEditText);
        EditText weightEditText = dialogView.findViewById(R.id.editWeightEditText);
        TextInputLayout dateLayout = dialogView.findViewById(R.id.editDateLayout);
        TextInputLayout weightLayout = dialogView.findViewById(R.id.editWeightLayout);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        // Pre-fill with current values
        dateEditText.setText(entry.getDate());
        weightEditText.setText(String.format(Locale.getDefault(), "%.1f", entry.getWeight()));

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_weight_entry)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = dialogBuilder.create();

        // Set up validation
        final boolean[] dateValid = {false};
        final boolean[] weightValid = {false};

        // Initial validation
        viewModel.validateDate(dateEditText.getText().toString().trim());
        viewModel.validateWeight(weightEditText.getText().toString().trim());

        // Date validation
        dateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateDate(s.toString().trim());
            }
        });

        // Weight validation
        weightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateWeight(s.toString().trim());
            }
        });

        // Observe validation results
        viewModel.getDateErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            dateLayout.setError(error);
            dateLayout.setErrorEnabled(error != null);
            dateValid[0] = error == null;
            saveButton.setEnabled(dateValid[0] && weightValid[0]);
        });

        viewModel.getWeightErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            weightLayout.setError(error);
            weightLayout.setErrorEnabled(error != null);
            weightValid[0] = error == null;
            saveButton.setEnabled(dateValid[0] && weightValid[0]);
        });

        // Set up save button
        saveButton.setOnClickListener(v -> {
            String newDate = dateEditText.getText().toString().trim();
            String newWeight = weightEditText.getText().toString().trim();

            viewModel.updateWeightEntry(entry.getId(), newDate, newWeight);
            dialog.dismiss();
        });

        dialog.show();
    }
}