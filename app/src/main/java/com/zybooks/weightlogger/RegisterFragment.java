package com.zybooks.weightlogger;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RegisterFragment extends Fragment {

    private EditText usernameEditText, passwordEditText, confirmPasswordEditText, goalWeightEditText;
    private Button signUpButton;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize views
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        signUpButton = view.findViewById(R.id.signUpButton);
        goalWeightEditText = view.findViewById(R.id.goalWeightEditText);

        dbHelper = new DatabaseHelper(requireContext());

        signUpButton.setOnClickListener(v -> signUpUser());

        return view;
    }

    private void signUpUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String goalWeightStr = goalWeightEditText.getText().toString().trim();

        double goalWeight = 0;

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!goalWeightStr.isEmpty()) {
            try {
               goalWeight = Double.parseDouble(goalWeightStr);

               if (goalWeight == 0) {
                   throw new NumberFormatException();
               }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid goal weight.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean inserted = dbHelper.insertUser(username, password, goalWeight);

        if (inserted) {
            Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT).show();
            // Navigate back to the login screen
            requireActivity().getSupportFragmentManager().popBackStack();
        } else {
            Toast.makeText(getContext(), "Registration failed. Username may already exist.", Toast.LENGTH_SHORT).show();
        }
    }
}
