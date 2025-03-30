package com.zybooks.weightlogger.Utilities;

import android.Manifest;

import android.annotation.SuppressLint;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.util.Log;
import java.util.Random;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.zybooks.weightlogger.MainActivity;


/**
 * Helper class that manages the creation and sending of notifications related to weight goals.
 * Handles notification channel setup and provides methods to send different types of
 * motivational notifications based on user progress.
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "weight_goal_channel";
    private static final String CHANNEL_NAME = "Weight Goal Notifications";
    private static final String CHANNEL_DESC = "Notifications related to your weight goals";
    private static final String TAG = "NotificationHelper";
    private final Context context;
    private static final Random random = new Random();

    /**
     * Creates a new NotificationHelper instance and initializes the notification channel.
     *
     * @param context The context used to access system services and resources
     */
    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Creates the notification channel for weight goal notifications.
     * This is required for notifications on Android 8.0 (API level 26) and higher.
     */
    private void createNotificationChannel() {

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(CHANNEL_DESC);

        // Register the channel with the system
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Checks if the app has permission to post notifications.
     *
     * @return true if notification permission is not granted, false otherwise
     */
    private boolean hasNotificationPermission() {

        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Sends a notification to inform the user about their progress toward their weight goal.
     * Includes motivational messages based on how close they are to their goal.
     *
     * @param currentWeight The user's current weight
     * @param goalWeight The user's goal weight
     */
    public void sendGoalProgressNotification(double currentWeight, double goalWeight) {
        // Check permission before sending notification
        if (hasNotificationPermission()) {
            Log.d(TAG, "Cannot send notification: Permission not granted");
            return;
        }

        // Create an explicit intent for the MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String title = "You're making progress!";
        String message;

        // Calculate the difference
        double difference = Math.abs(currentWeight - goalWeight);

        // Choose a motivational message
        @SuppressLint("DefaultLocale") String[] messages = {
                "Keep up the great work! You're getting closer to your goal weight.",
                "You're making amazing progress! Just " + String.format("%.1f", difference) + " lbs to go!",
                "Fantastic job on your weight journey! Keep going!",
                "You're getting closer to your goal of " + String.format("%.1f", goalWeight) + " lbs!"
        };

        message = messages[random.nextInt(messages.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(random.nextInt(1000), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when sending notification", e);
        }
    }

    /**
     * Sends a notification to congratulate the user on achieving their weight goal.
     * Includes celebratory messages for this significant achievement.
     */
    public void sendGoalAchievedNotification() {
        // Check permission before sending notification
        if (hasNotificationPermission()) {
            Log.d(TAG, "Cannot send notification: Permission not granted");
            return;
        }

        // Create an explicit intent for the MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String title = "Awesome Work!";

        // Choose a congratulatory message
        String[] messages = {
                "You've reached your goal weight! Amazing job!",
                "Goal achieved! You should be incredibly proud of yourself!",
                "You did it! You've reached your weight goal!",
                "Congratulations on achieving your weight goal! What an accomplishment!"
        };

        String message = messages[random.nextInt(messages.length)];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(random.nextInt(1000), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when sending notification", e);
        }
    }
}