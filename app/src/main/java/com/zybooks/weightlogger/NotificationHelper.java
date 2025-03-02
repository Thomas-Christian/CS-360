package com.zybooks.weightlogger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class NotificationHelper {
    private static final String CHANNEL_ID = "weight_goal_channel";
    private static final String CHANNEL_NAME = "Weight Goal Notifications";
    private static final String CHANNEL_DESC = "Notifications related to your weight goals";
    private static final String TAG = "NotificationHelper";

    private final Context context;
    private static final Random random = new Random();

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

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

    private boolean hasNotificationPermission() {

        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;

    }

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

        String title = "WOW!";

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