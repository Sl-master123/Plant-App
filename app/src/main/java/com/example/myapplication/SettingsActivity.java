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

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PlantAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkModePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Back button
        ImageButton btnBack = findViewById(R.id.btnSettingsBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Dark Mode Switch
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        Switch switchDark = findViewById(R.id.switchDarkMode);
        if (switchDark != null) {
            switchDark.setChecked(isDark);
            switchDark.setOnCheckedChangeListener((btn, checked) -> {
                prefs.edit().putBoolean("dark_mode", checked).apply();
                AppCompatDelegate.setDefaultNightMode(
                        checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
                // Recreate to apply theme properly
                recreate();
            });
        }

        // Notification permission status
        updateNotifStatus();

        // Notification card tap → open system notification settings
        LinearLayout btnNotif = findViewById(R.id.btnCheckNotifPermission);
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            });
        }
    }

    private void updateNotifStatus() {
        TextView tvStatus = findViewById(R.id.tvNotifStatus);
        if (tvStatus == null) return;

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
            tvStatus.setText("✅ Enabled");
            tvStatus.setTextColor(getResources().getColor(R.color.text_green, null));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
