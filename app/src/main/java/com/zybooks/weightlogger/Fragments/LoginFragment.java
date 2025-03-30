package com.zybooks.weightlogger.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.zybooks.weightlogger.MainActivity;
import com.zybooks.weightlogger.R;
import com.zybooks.weightlogger.ViewModels.LoginViewModel;
import com.zybooks.weightlogger.ViewModels.WeightDataViewModel;

/**
 * Fragment for user login with enhanced validation.
 * Handles user authentication with real-time input validation.
 */
public class LoginFragment extends Fragment {

    private EditText usernameEditText, passwordEditText;
    private TextInputLayout usernameLayout, passwordLayout;
    private Button loginButton;
    private LoginViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Initialize UI components
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        usernameLayout = view.findViewById(R.id.usernameLayout);
        passwordLayout = view.findViewById(R.id.passwordLayout);
        loginButton = view.findViewById(R.id.loginButton);
        Button createAccountButton = view.findViewById(R.id.createAccountButton);

        // Set up input validation
        setupValidation();

        // Set up click listeners
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            viewModel.loginUser(username, password);
        });

        createAccountButton.setOnClickListener(v -> viewModel.signUp());

        // Observe LiveData from ViewModel
        setupLiveDataObservers();

        return view;
    }

    /**
     * Sets up real-time validation for input fields.
     */
    private void setupValidation() {
        // Username validation
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

        // Password validation
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.validatePassword(s.toString().trim());
            }
        });
    }

    /**
     * Sets up observers for ViewModel LiveData.
     */
    private void setupLiveDataObservers() {
        // Observe username validation
        viewModel.getUsernameErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            usernameLayout.setError(error);
            usernameLayout.setErrorEnabled(error != null);
        });

        // Observe password validation
        viewModel.getPasswordErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            passwordLayout.setError(error);
            passwordLayout.setErrorEnabled(error != null);
        });

        // Observe form validity for button enabling
        viewModel.getFormValidLiveData().observe(getViewLifecycleOwner(), isValid -> loginButton.setEnabled(isValid));

        // Observe status messages
        viewModel.getStatusMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                if (message.equals("NAVIGATE_TO_REGISTER")) {
                    navigateToRegister();
                } else {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Observe login success
        viewModel.getLoginSuccessLiveData().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                updateNavigationBar();
                // Clear the fields after successful login
                usernameEditText.setText("");
                passwordEditText.setText("");
                viewModel.resetValidation();
            }
        });
        // Observe data
        viewModel.getDataRefreshNeededLiveData().observe(getViewLifecycleOwner(), refreshNeeded -> {
            if (refreshNeeded != null && refreshNeeded) {
                WeightDataViewModel weightDataViewModel = new ViewModelProvider(requireActivity()).get(WeightDataViewModel.class);
                weightDataViewModel.refreshDataAfterLogin();
            }
        });
    }

    /**
     * Updates the navigation bar after successful login.
     */
    private void updateNavigationBar() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateNavBarAfterLogin();
        }
    }

    /**
     * Navigates to the registration screen.
     */
    private void navigateToRegister() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity.getNavController() != null) {
                mainActivity.getNavController().navigate(R.id.navigation_register);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reset validation when returning to this fragment
        viewModel.resetValidation();
    }
}