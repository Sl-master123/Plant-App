package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * ============================================================================
 * BootReceiver.java
 * ============================================================================
 * Role: System Broadcast Receiver for device boot completion.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Why is this needed?
 *    Android's AlarmManager clears all scheduled alarms whenever the phone reboots
 *    or powers down. Without this receiver, all plant watering alarms would be lost!
 * 2. Manifest Registration:
 *    Listens for android.intent.action.BOOT_COMPLETED with the permission:
 *    android.permission.RECEIVE_BOOT_COMPLETED.
 * 3. Action Taken:
 *    Calls WateringReminderReceiver.rescheduleAllAlarms() to query Room DB on a
 *    background thread and restore every active plant's reminder schedule.
 * ============================================================================
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Verify that the incoming broadcast is indeed boot completion
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Restore all scheduled alarms from the Room database
            WateringReminderReceiver.rescheduleAllAlarms(context);
        }
    }
}
