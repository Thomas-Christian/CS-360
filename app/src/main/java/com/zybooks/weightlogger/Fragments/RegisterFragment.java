package com.zybooks.weightlogger.Fragments;

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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.zybooks.weightlogger.R;
import com.zybooks.weightlogger.Utilities.ValidationUIHelper;
import com.zybooks.weightlogger.ViewModels.RegisterViewModel;

/**
 * Fragment for new user registration with enhanced validation.
 * Provides real-time input validation and password strength indication.
 */
public class RegisterFragment extends Fragment {

    private EditText usernameEditText, passwordEditText, confirmPasswordEditText, goalWeightEditText;
    private TextInputLayout usernameLayout, passwordLayout, confirmPasswordLayout, goalWeightLayout;
    private TextView passwordStrengthText;
    private ProgressBar passwordStrengthBar;
    private Button signUpButton;
    private RegisterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Initialize views
        initializeViews(view);

        // Set up validation
        setupValidation();

        // Set up button click listeners
        signUpButton.setOnClickListener(v -> submitRegistration());

        // Set up observers for ViewModel LiveData
        setupLiveDataObservers();

        return view;
    }

    /**
     * Initializes all UI components.
     *
     * @param view The root view
     */
    private void initializeViews(View view) {
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        goalWeightEditText = view.findViewById(R.id.goalWeightEditText);

        usernameLayout = view.findViewById(R.id.usernameLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordLayout);
        goalWeightLayout = view.findViewById(R.id.goalWeightLayout);

        signUpButton = view.findViewById(R.id.signUpButton);
        passwordStrengthText = view.findViewById(R.id.passwordStrengthText);
        passwordStrengthBar = view.findViewById(R.id.passwordStrengthBar);
    }

    /**
     * Sets up validation for all input fields.
     */
    private void setupValidation() {
        // Set up validation for username field
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateUsername(s.toString().trim());
            }
        });

        // Set up validation for password field with strength visualization
        ValidationUIHelper.setupPasswordStrengthVisualization(
                passwordEditText, passwordStrengthBar, passwordStrengthText, requireContext());

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String password = s.toString();
                viewModel.validatePassword(password);

                // Also validate confirm password field if it has content
                String confirmPassword = confirmPasswordEditText.getText().toString();
                if (!confirmPassword.isEmpty()) {
                    viewModel.validateConfirmPassword(password, confirmPassword);
                }
            }
        });

        // Set up validation for confirm password field
        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateConfirmPassword(passwordEditText.getText().toString(), s.toString());
            }
        });

        // Set up validation for goal weight field (optional)
        goalWeightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validateGoalWeight(s.toString().trim());
            }
        });
    }

    /**
     * Sets up observers for ViewModel LiveData.
     */
    private void setupLiveDataObservers() {
        // Observe field error messages
        viewModel.getUsernameErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            usernameLayout.setError(error);
            usernameLayout.setErrorEnabled(error != null);
        });

        viewModel.getPasswordErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            passwordLayout.setError(error);
            passwordLayout.setErrorEnabled(error != null);
        });

        viewModel.getConfirmPasswordErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            confirmPasswordLayout.setError(error);
            confirmPasswordLayout.setErrorEnabled(error != null);
        });

        viewModel.getGoalWeightErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            goalWeightLayout.setError(error);
            goalWeightLayout.setErrorEnabled(error != null);
        });

        // Observe password strength for the progress bar
        viewModel.getPasswordStrengthLiveData().observe(getViewLifecycleOwner(), strength -> {
            if (strength != null) {
                passwordStrengthBar.setProgress(strength);

                // Update text description based on strength
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

        // Observe form validity to enable/disable submit button
        viewModel.getFormValidLiveData().observe(getViewLifecycleOwner(), isValid -> signUpButton.setEnabled(isValid));

        // Observe status messages
        viewModel.getStatusMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe registration success
        viewModel.getRegistrationSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                // Navigate back to login screen
                Navigation.findNavController(requireView()).navigate(R.id.navigation_login);
            }
        });
    }

    /**
     * Submits the registration form.
     * Collects data from all fields and passes them to the ViewModel.
     */
    private void submitRegistration() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String goalWeightStr = goalWeightEditText.getText().toString().trim();

        viewModel.registerUser(username, password, confirmPassword, goalWeightStr);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add this to ensure the Up button works
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup back navigation
        view.findViewById(R.id.backButton).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.navigation_login));
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Navigation.findNavController(requireView()).navigate(R.id.navigation_login);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset validation when leaving this fragment
        viewModel.resetValidation();
    }
}