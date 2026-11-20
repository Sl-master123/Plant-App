package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

/**
 * ============================================================================
 * SettingsActivity.java
 * ============================================================================
 * Role: Application Settings and Configuration Screen.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Dark Mode Management:
 *    Toggles app-wide day/night theme using AppCompatDelegate.setDefaultNightMode().
 *    Persists choice in SharedPreferences so it survives app restarts.
 * 2. Notification Permission Diagnostics (Android 13+ / API 33):
 *    Demonstrates real-time permission status checking for POST_NOTIFICATIONS.
 *    Displays green badge if granted, or red indicator if disabled.
 * 3. Deep Linking to System Settings:
 *    Provides one-tap navigation to Android's system App Notification Settings
 *    via Settings.ACTION_APP_NOTIFICATION_SETTINGS intent.
 * 4. Project & Developer Attribution:
 *    Highlights project group ("OAKSHADE") and Mobile Application Development subject.
 * ============================================================================
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PlantAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkModePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Back navigation button
        ImageButton btnBack = findViewById(R.id.btnSettingsBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // 2. Dark Mode Toggle Switch
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        Switch switchDark = findViewById(R.id.switchDarkMode);
        if (switchDark != null) {
            switchDark.setChecked(isDark);
            switchDark.setOnCheckedChangeListener((btn, checked) -> {
                // Save setting
                prefs.edit().putBoolean("dark_mode", checked).apply();
                // Apply theme immediately
                AppCompatDelegate.setDefaultNightMode(
                        checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
                // Recreate activity to refresh styles cleanly
                recreate();
            });
        }

        // 3. Update notification permission status indicator
        updateNotifStatus();

        // 4. Notification Settings Card -> Opens system app settings
        LinearLayout btnNotif = findViewById(R.id.btnCheckNotifPermission);
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            });
        }
    }

    /**
     * Checks if notification permission is active and updates UI status indicator.
     */
    private void updateNotifStatus() {
        TextView tvStatus = findViewById(R.id.tvNotifStatus);
        if (tvStatus == null) return;

        // Android 13 (Tiramisu, API 33) introduced runtime POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                tvStatus.setText("✅ Enabled – reminders are active");
                tvStatus.setTextColor(getResources().getColor(R.color.text_green, null));
            } else {
                tvStatus.setText("❌ Disabled – tap to enable in Settings");
                tvStatus.setTextColor(0xFFD32F2F);
            }
        } else {
            // Older Android versions grant notification permission by default at install time
            tvStatus.setText("✅ Enabled");
            tvStatus.setTextColor(getResources().getColor(R.color.text_green, null));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh status in case user granted permission in system settings and came back
        updateNotifStatus();
    }

    private void applyDarkModePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
