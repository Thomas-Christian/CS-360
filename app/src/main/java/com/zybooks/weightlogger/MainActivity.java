package com.zybooks.weightlogger;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zybooks.weightlogger.Data.UserSessionManager;
import com.zybooks.weightlogger.Fragments.ProfileFragment;
import com.zybooks.weightlogger.Utilities.Permissions;
import com.zybooks.weightlogger.ViewModels.MainViewModel;

/**
 * Main activity that acts as the entry point and container for all fragments in the application.
 * Manages the bottom navigation, user session state, and permissions.
 * Provides methods to update UI components based on authentication state.
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private static MainActivity instance;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set instance for safe reference
        instance = this;

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        Permissions permissionHelper = new Permissions(this);
        UserSessionManager sessionManager = new UserSessionManager(this);

        // Check SMS permission when the app starts
        permissionHelper.checkPermissions();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }

        // Check if user is already logged in
        viewModel.setLoggedInState(sessionManager.isLoggedIn());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permissions.handlePermissionResult(requestCode, permissions, grantResults);
    }

    public void updateNavBarAfterLogin() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Hide login menu item and show profile menu item
        Menu menu = bottomNav.getMenu();

        MenuItem loginItem = menu.findItem(R.id.navigation_login);
        MenuItem profileItem = menu.findItem(R.id.navigation_profile);

        if (loginItem != null) {
            loginItem.setVisible(false);
        }

        if (profileItem != null) {
            profileItem.setVisible(true);
        }

        // Update ViewModel
        viewModel.setLoggedInState(true);

        // Navigate to the ProfileFragment
        if (navController != null) {
            navController.navigate(R.id.navigation_profile);
        }
    }

    public void resetNavBarAfterLogout() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Show login menu item and hide profile menu item
        Menu menu = bottomNav.getMenu();

        MenuItem loginItem = menu.findItem(R.id.navigation_login);
        MenuItem profileItem = menu.findItem(R.id.navigation_profile);

        if (loginItem != null) {
            loginItem.setVisible(true);
        }

        if (profileItem != null) {
            profileItem.setVisible(false);
        }

        // Update ViewModel
        viewModel.setLoggedInState(false);

        // Navigate back to login
        if (navController != null) {
            navController.navigate(R.id.navigation_login);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clear the instance when the activity is destroyed
        if (instance == this) {
            instance = null;
        }

    }

    public static void updateProfileIfVisible() {
        if (instance != null) {
            instance.updateProfileFragmentIfVisible();
        }
    }

    private void updateProfileFragmentIfVisible() {
        // Find the current fragment
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // Check if the current fragment is the profile fragment
        if (currentFragment instanceof ProfileFragment) {
            ((ProfileFragment) currentFragment).updateWeightGoalInfo();
        }
    }

    public NavController getNavController() {
        return navController;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
                || super.onSupportNavigateUp();
    }
}