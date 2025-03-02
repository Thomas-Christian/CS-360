package com.zybooks.weightlogger;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set instance for safe reference
        instance = this;

        Permissions permissionHelper = new Permissions(this);
        UserSessionManager sessionManager = new UserSessionManager(this);

        ProfileFragment profileFragment = new ProfileFragment();

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
        if (sessionManager.isLoggedIn()) {
            updateNavBarAfterLogin();
        } else {
            // Make sure login is visible and profile is hidden for new sessions
            Menu menu = bottomNavigationView.getMenu();
            MenuItem loginItem = menu.findItem(R.id.navigation_login);
            MenuItem profileItem = menu.findItem(R.id.navigation_profile);

            if (loginItem != null) {
                loginItem.setVisible(true);
            }

            if (profileItem != null) {
                profileItem.setVisible(false);
            }
        }
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

    // Safe way to update profile without static fragment references
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

}