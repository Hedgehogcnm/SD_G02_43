package com.example.project2025.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

import androidx.core.app.NotificationCompat;

import com.example.project2025.R;
import com.example.project2025.Specific_User.MainActivity;
import com.example.project2025.Specific_Admin.AdminActivity;

public class NotificationHelper {
    
    private static final String CHANNEL_ID = "feeding_channel";
    private static final String CHANNEL_NAME = "Feeding Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for feeding activities";
    
    public static void showFeedingCompletedNotification(Context context, int feedLevel) {
        android.util.Log.d("NotificationHelper", "showFeedingCompletedNotification called with level: " + feedLevel);
        // Create notification channel for Android 8.0+
        createNotificationChannel(context);
        
        // Create intent for when notification is tapped
        String role = null;
        try {
            role = context.getSharedPreferences("ROLE", 0).getString("Role", null);
        } catch (Throwable ignored) {}

        Intent intent = new Intent(context, ("Admin".equals(role) ? AdminActivity.class : MainActivity.class));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_feed) // Use your feed icon
                .setContentTitle("Feeding Completed !")
                .setContentText("Feeding completed at Level " + feedLevel)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        // Show the notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            android.util.Log.d("NotificationHelper", "Feeding completed notification sent");
        } else {
            android.util.Log.e("NotificationHelper", "NotificationManager is null");
        }
        
        // Vibrate the device
        vibrateDevice(context);
    }
    
    public static void showLowFoodLevelNotification(Context context) {
        // Create notification channel for Android 8.0+
        createNotificationChannel(context);
        
        // Create intent for when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_feed) // Use your feed icon
                .setContentTitle("Low Food Level")
                .setContentText("Food level is low. Please add more food for your pets!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        // Show the notification with a specific ID for low food level
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            // Use a specific ID for low food level notifications
            notificationManager.notify(999, builder.build());
        }
        
        // Vibrate the device
        vibrateDevice(context);
    }
    
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private static void vibrateDevice(Context context) {
        Vibrator vibrator;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = 
                    (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createWaveform(
                        new long[]{0, 500, 200, 500}, -1);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(new long[]{0, 500, 200, 500}, -1);
            }
        }
    }
}

