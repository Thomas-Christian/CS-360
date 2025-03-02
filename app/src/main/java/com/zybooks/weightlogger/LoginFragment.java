package com.zybooks.weightlogger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, createAccountButton;
    private DatabaseHelper dbHelper;
    private UserSessionManager sessionManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new UserSessionManager(requireContext());

        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        createAccountButton = view.findViewById(R.id.createAccountButton);

        loginButton.setOnClickListener(v -> loginUser());
        createAccountButton.setOnClickListener(v -> signUpUser());

        return view;
    }

    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.validateUser(username, password)) {
            // Save login session
            sessionManager.saveLoginSession(username);

            Toast.makeText(getContext(), "Login successful!", Toast.LENGTH_SHORT).show();

            // Update the navigation bar
            updateNavigationBar();
        } else {
            Toast.makeText(getContext(), "Invalid username or password.", Toast.LENGTH_SHORT).show();
        }
    }

    // Add this method to update the navigation bar
    private void updateNavigationBar() {

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateNavBarAfterLogin();
        }
    }

    private void signUpUser() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }
}