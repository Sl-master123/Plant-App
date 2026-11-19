/** Creates notification channels and displays watering reminders. */
package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "plant_watering_channel_v2";
    public static final String CHANNEL_NAME = "Plant Watering Reminders";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications reminding you to water your plants");
            channel.enableVibration(true);
            channel.setLightColor(Color.GREEN);
            channel.setShowBadge(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void sendWateringNotification(Context context, Plant plant) {
        createNotificationChannel(context);

        int plantId = plant.getId();
        String plantName = plant.getName();
        String category = plant.getCategory() != null ? plant.getCategory() : "Plant";

        // Open details intent on notification tap
        Intent openIntent = new Intent(context, PlantDetailsActivity.class);
        openIntent.putExtra("plant_id", plantId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingOpenIntent = PendingIntent.getActivity(
                context,
                plantId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action 1: Mark as Watered
        Intent wateredIntent = new Intent(context, WateringReminderReceiver.class);
        wateredIntent.setAction(WateringReminderReceiver.ACTION_MARK_WATERED);
        wateredIntent.putExtra("plant_id", plantId);
        PendingIntent pendingWateredIntent = PendingIntent.getBroadcast(
                context,
                plantId * 10 + 1,
                wateredIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action 2: Snooze 1 Hour
        Intent snoozeIntent = new Intent(context, WateringReminderReceiver.class);
        snoozeIntent.setAction(WateringReminderReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra("plant_id", plantId);
        PendingIntent pendingSnoozeIntent = PendingIntent.getBroadcast(
                context,
                plantId * 10 + 2,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "💧 Time to water " + plantName + "!";
        String contentText = "Your [" + category + "] plant needs water today!";
        String bigText = "Don't forget to take care of your " + plantName + " (" + category + ")!\nRegular watering keeps your plant healthy and vibrant 🌱";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle(title)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xFF2E7D32) // Forest green accent
                .setAutoCancel(true)
                .setContentIntent(pendingOpenIntent)
                .addAction(android.R.drawable.checkbox_on_background, "✓ Watered", pendingWateredIntent)
                .addAction(android.R.drawable.ic_popup_reminder, "⏰ Snooze 1h", pendingSnoozeIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(plantId, builder.build());
        }
    }

    public static void cancelNotification(Context context, int plantId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel(plantId);
        }
    }
}

