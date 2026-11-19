package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class WateringReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_TRIGGER_REMINDER = "com.example.myapplication.TRIGGER_REMINDER";
    public static final String ACTION_MARK_WATERED = "com.example.myapplication.MARK_WATERED";
    public static final String ACTION_SNOOZE = "com.example.myapplication.SNOOZE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        int plantId = intent.getIntExtra("plant_id", -1);

        if (ACTION_MARK_WATERED.equals(action)) {
            if (plantId != -1) {
                NotificationHelper.cancelNotification(context, plantId);
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(context);
                    Plant plant = db.plantDao().getPlantById(plantId);
                    if (plant != null) {
                        plant.setLastWateredTimestamp(System.currentTimeMillis());
                        db.plantDao().updatePlant(plant);
                        schedulePlantAlarm(context, plant);
                    }
                }).start();
            }
            return;
        }

        if (ACTION_SNOOZE.equals(action)) {
            if (plantId != -1) {
                NotificationHelper.cancelNotification(context, plantId);
                // Snooze 1 hour from now
                scheduleSnoozeAlarm(context, plantId, System.currentTimeMillis() + (60 * 60 * 1000));
            }
            return;
        }

        if (ACTION_TRIGGER_REMINDER.equals(action) || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (plantId != -1) {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(context);
                    Plant plant = db.plantDao().getPlantById(plantId);
                    if (plant != null && plant.isReminderEnabled()) {
                        // Check if current time is within reminder window (before end date)
                        long now = System.currentTimeMillis();
                        if (plant.getReminderEndDate() <= 0 || now <= plant.getReminderEndDate()) {
                            NotificationHelper.sendWateringNotification(context, plant);
                        }
                        // Re-schedule for next interval
                        schedulePlantAlarm(context, plant);
                    }
                }).start();
            } else {
                // Reschedule all if no specific plant
                rescheduleAllAlarms(context);
            }
        }
    }

    public static void schedulePlantAlarm(Context context, Plant plant) {
        if (plant == null || !plant.isReminderEnabled()) {
            if (plant != null) cancelPlantAlarm(context, plant.getId());
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long nextTriggerMillis = calculateNextTriggerTime(plant);
        if (nextTriggerMillis <= 0) {
            cancelPlantAlarm(context, plant.getId());
            return;
        }

        Intent intent = new Intent(context, WateringReminderReceiver.class);
        intent.setAction(ACTION_TRIGGER_REMINDER);
        intent.putExtra("plant_id", plant.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                plant.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private static void scheduleSnoozeAlarm(Context context, int plantId, long triggerAtMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, WateringReminderReceiver.class);
        intent.setAction(ACTION_TRIGGER_REMINDER);
        intent.putExtra("plant_id", plantId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void cancelPlantAlarm(Context context, int plantId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, WateringReminderReceiver.class);
        intent.setAction(ACTION_TRIGGER_REMINDER);
        intent.putExtra("plant_id", plantId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                plantId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    public static void rescheduleAllAlarms(Context context) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            List<Plant> plants = db.plantDao().getAllPlants();
            for (Plant plant : plants) {
                if (plant.isReminderEnabled()) {
                    schedulePlantAlarm(context, plant);
                }
            }
        }).start();
    }

    public static long calculateNextTriggerTime(Plant plant) {
        long now = System.currentTimeMillis();

        // Check if reminder end date passed
        if (plant.getReminderEndDate() > 0 && now >= plant.getReminderEndDate()) {
            return -1; // Passed end date
        }

        int intervalDays = plant.getReminderIntervalDays() > 0 ? plant.getReminderIntervalDays() : 1;
        int hour = plant.getReminderHour();
        int minute = plant.getReminderMinute();

        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(plant.getReminderStartDate() > 0 ? plant.getReminderStartDate() : now);
        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, minute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        // If target is in the past, advance by intervalDays until it's in the future
        while (target.getTimeInMillis() <= now) {
            target.add(Calendar.DAY_OF_YEAR, intervalDays);
        }

        // Check if next trigger is beyond end date
        if (plant.getReminderEndDate() > 0 && target.getTimeInMillis() > plant.getReminderEndDate()) {
            return -1;
        }

        return target.getTimeInMillis();
    }
}

