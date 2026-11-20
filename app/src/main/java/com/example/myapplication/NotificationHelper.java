package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * ============================================================================
 * NotificationHelper.java
 * ============================================================================
 * Role: Manages Android Notification Channels and builds rich interactive notifications.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Notification Channels (Android 8.0 / API 26+):
 *    Starting with Android 8.0, all notifications must belong to a channel.
 *    Channels let users configure importance, sounds, vibration, and visual badge
 *    settings in system preferences.
 * 2. Interactive Notifications:
 *    Provides two direct action buttons right on the notification shade:
 *    - "✓ Watered": Marks the plant as watered and calculates the next schedule.
 *    - "⏰ Snooze 1h": Snoozes reminder for 1 hour.
 * 3. PendingIntents & Immutability:
 *    Uses PendingIntent.FLAG_IMMUTABLE (mandated for security since Android 12)
 *    and unique request codes so actions for different plants don't collide.
 * ============================================================================
 */
public class NotificationHelper {

    // Notification Channel constants
    public static final String CHANNEL_ID = "plant_watering_channel_v2";
    public static final String CHANNEL_NAME = "Plant Watering Reminders";

    /**
     * Creates and registers the notification channel with Android system services.
     * Safe to call repeatedly; if the channel already exists, Android leaves it intact.
     *
     * @param context Application context
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH // High importance pops on screen (heads-up notification)
            );
            channel.setDescription("Notifications reminding you to water your plants on schedule");
            channel.enableVibration(true);
            channel.setLightColor(Color.GREEN);
            channel.setShowBadge(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Builds and posts an interactive watering reminder notification for a specific plant.
     *
     * @param context Context used to access system NotificationManager
     * @param plant Plant requiring watering
     */
    public static void sendWateringNotification(Context context, Plant plant) {
        createNotificationChannel(context);

        int plantId = plant.getId();
        String plantName = plant.getName();
        String category = plant.getCategory() != null ? plant.getCategory() : "Plant";

        // 1. PendingIntent when user taps the notification body -> Open PlantDetailsActivity
        Intent openIntent = new Intent(context, PlantDetailsActivity.class);
        openIntent.putExtra("plant_id", plantId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingOpenIntent = PendingIntent.getActivity(
                context,
                plantId, // Unique request code per plant
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 2. Action Button 1: "✓ Watered" -> Broadcasts to WateringReminderReceiver
        Intent wateredIntent = new Intent(context, WateringReminderReceiver.class);
        wateredIntent.setAction(WateringReminderReceiver.ACTION_MARK_WATERED);
        wateredIntent.putExtra("plant_id", plantId);
        PendingIntent pendingWateredIntent = PendingIntent.getBroadcast(
                context,
                plantId * 10 + 1,
                wateredIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 3. Action Button 2: "⏰ Snooze 1h" -> Broadcasts snooze intent
        Intent snoozeIntent = new Intent(context, WateringReminderReceiver.class);
        snoozeIntent.setAction(WateringReminderReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra("plant_id", plantId);
        PendingIntent pendingSnoozeIntent = PendingIntent.getBroadcast(
                context,
                plantId * 10 + 2,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Dynamic text content
        String title = "💧 Time to water " + plantName + "!";
        String contentText = "Your [" + category + "] plant needs water today!";
        String bigText = "Don't forget to take care of your " + plantName + " (" + category + ")!\nRegular watering keeps your plant healthy and vibrant 🌱";

        // Build notification using NotificationCompat for backward compatibility
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText)) // Expandable card style
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xFF2E7D32) // Forest green theme color
                .setAutoCancel(true)  // Dismisses notification when tapped
                .setContentIntent(pendingOpenIntent)
                .addAction(android.R.drawable.checkbox_on_background, "✓ Watered", pendingWateredIntent)
                .addAction(android.R.drawable.ic_popup_reminder, "⏰ Snooze 1h", pendingSnoozeIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // plantId is used as the notification ID so each plant has its own distinct notification
            manager.notify(plantId, builder.build());
        }
    }

    /**
     * Cancels an active notification for a plant (e.g. after it is watered or deleted).
     *
     * @param context Application context
     * @param plantId ID of the plant whose notification should be dismissed
     */
    public static void cancelNotification(Context context, int plantId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(plantId);
        }
    }
}
