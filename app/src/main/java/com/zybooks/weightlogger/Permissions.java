package com.zybooks.weightlogger;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissions {
    private static final int PERMISSIONS_REQUEST_CODE = 101;
    private static AppCompatActivity activity = null;

    public Permissions(AppCompatActivity activity) {
        Permissions.activity = activity;
    }

    public void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Check SMS permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }

        // Check notification permission only for Android 13+ (API 33+)
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Request permissions if needed
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSIONS_REQUEST_CODE
            );
        }
    }

    public static void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    // Show appropriate message based on which permission was denied
                    if (Manifest.permission.SEND_SMS.equals(permissions[i])) {
                        showPermissionDeniedDialog("We won't be able to send SMS messages for weight updates.");
                    } else if (Manifest.permission.POST_NOTIFICATIONS.equals(permissions[i])) {
                        showPermissionDeniedDialog("We won't be able to send you notifications about your weight goals.");
                    }
                }
            }
        }
    }

    private static void showPermissionDeniedDialog(String message) {
        new AlertDialog.Builder(activity)
                .setTitle("Permission Denied")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}