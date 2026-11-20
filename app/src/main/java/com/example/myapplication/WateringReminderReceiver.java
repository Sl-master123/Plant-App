package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;
import java.util.List;

/**
 * ============================================================================
 * WateringReminderReceiver.java
 * ============================================================================
 * Role: Core background engine for alarm triggering and reminder rescheduling.
 * 
 * Key Concepts for Project Demonstration:
 * 1. BroadcastReceiver Component:
 *    A lightweight Android component that wakes up the app when system alarms or
 *    notification actions occur, without requiring the app to be actively open.
 * 2. Handled Actions:
 *    - ACTION_TRIGGER_REMINDER: Fires when an alarm triggers, sends the notification,
 *      and schedules the next interval.
 *    - ACTION_MARK_WATERED: Triggered directly from notification button; updates
 *      last-watered timestamp in Room DB and schedules the next alarm.
 *    - ACTION_SNOOZE: Postpones reminder by 1 hour.
 *    - BOOT_COMPLETED: Restores all alarms when the device reboots.
 * 3. Doze Mode & Exact Alarms:
 *    Uses AlarmManager.setAndAllowWhileIdle() with RTC_WAKEUP to ensure reminders
 *    fire reliably even when the device enters battery-saving Doze mode.
 * ============================================================================
 */
public class WateringReminderReceiver extends BroadcastReceiver {

    // Action identifier constants
    public static final String ACTION_TRIGGER_REMINDER = "com.example.myapplication.TRIGGER_REMINDER";
    public static final String ACTION_MARK_WATERED = "com.example.myapplication.MARK_WATERED";
    public static final String ACTION_SNOOZE = "com.example.myapplication.SNOOZE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        int plantId = intent.getIntExtra("plant_id", -1);

        // --------------------------------------------------------------------
        // Case 1: User tapped "✓ Watered" on notification action button
        // --------------------------------------------------------------------
        if (ACTION_MARK_WATERED.equals(action)) {
            if (plantId != -1) {
                NotificationHelper.cancelNotification(context, plantId);
                // Database operations must never block the main UI thread
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(context);
                    Plant plant = db.plantDao().getPlantById(plantId);
                    if (plant != null) {
                        plant.setLastWateredTimestamp(System.currentTimeMillis());
                        db.plantDao().updatePlant(plant);
                        // Schedule next reminder based on interval
                        schedulePlantAlarm(context, plant);
                    }
                }).start();
            }
            return;
        }

        // --------------------------------------------------------------------
        // Case 2: User tapped "⏰ Snooze 1h" on notification action button
        // --------------------------------------------------------------------
        if (ACTION_SNOOZE.equals(action)) {
            if (plantId != -1) {
                NotificationHelper.cancelNotification(context, plantId);
                // Schedule alarm 1 hour from now
                scheduleSnoozeAlarm(context, plantId, System.currentTimeMillis() + (60 * 60 * 1000));
            }
            return;
        }

        // --------------------------------------------------------------------
        // Case 3: Scheduled alarm triggered OR device finished booting
        // --------------------------------------------------------------------
        if (ACTION_TRIGGER_REMINDER.equals(action) || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (plantId != -1) {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(context);
                    Plant plant = db.plantDao().getPlantById(plantId);
                    if (plant != null && plant.isReminderEnabled()) {
                        long now = System.currentTimeMillis();
                        // Verify current time is before the reminder end date
                        if (plant.getReminderEndDate() <= 0 || now <= plant.getReminderEndDate()) {
                            NotificationHelper.sendWateringNotification(context, plant);
                        }
                        // Re-schedule alarm for the next cycle
                        schedulePlantAlarm(context, plant);
                    }
                }).start();
            } else {
                // If plantId is -1 (e.g. general boot trigger), reschedule all active alarms
                rescheduleAllAlarms(context);
            }
        }
    }

    /**
     * Schedules an AlarmManager reminder for a specific plant.
     *
     * @param context Application context
     * @param plant Plant entity containing interval and time configuration
     */
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
                plant.getId(), // Unique request code per plant
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            // setAndAllowWhileIdle ensures alarm fires even if device is in battery-saving Doze mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextTriggerMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Schedules a one-time snooze alarm for 1 hour.
     */
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

    /**
     * Cancels any pending AlarmManager alarm for a plant.
     *
     * @param context Application context
     * @param plantId ID of the plant
     */
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

    /**
     * Iterates over all plants in the Room database and reschedules their alarms.
     * Called after phone reboot (via BootReceiver).
     */
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

    /**
     * Intelligent Next Trigger Time Calculation Algorithm:
     * 1. Checks if the reminder period has expired (past reminderEndDate).
     * 2. Sets calendar target to plant's configured hour and minute.
     * 3. If target is in the past, advances day by intervalDays until it lands in the future.
     * 4. Verifies the calculated future date does not exceed reminderEndDate.
     *
     * @param plant Plant entity
     * @return Future epoch millisecond timestamp, or -1 if reminder is inactive or expired
     */
    public static long calculateNextTriggerTime(Plant plant) {
        long now = System.currentTimeMillis();

        // Expired check
        if (plant.getReminderEndDate() > 0 && now >= plant.getReminderEndDate()) {
            return -1;
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

        // Advance calendar by intervalDays until the target is strictly in the future
        while (target.getTimeInMillis() <= now) {
            target.add(Calendar.DAY_OF_YEAR, intervalDays);
        }

        // Verify next trigger is not beyond expiration date
        if (plant.getReminderEndDate() > 0 && target.getTimeInMillis() > plant.getReminderEndDate()) {
            return -1;
        }

        return target.getTimeInMillis();
    }
}
