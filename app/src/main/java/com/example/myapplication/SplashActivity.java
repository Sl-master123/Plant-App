package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PlantAppPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch_done";
    private static final int SPLASH_DURATION_MS = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode BEFORE super.onCreate
        applyDarkModePreference();
        super.onCreate(savedInstanceState);

        // Check if we've already shown splash
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean firstLaunchDone = prefs.getBoolean(KEY_FIRST_LAUNCH, false);

        if (firstLaunchDone) {
            // Skip splash on subsequent launches
            goToMain();
            return;
        }

        setContentView(R.layout.activity_splash);

        // Mark first launch as done
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply();

        // Animate center content
        LinearLayout splashCenter = findViewById(R.id.splashCenter);
        View splashLoader = findViewById(R.id.splashLoader);
        View splashAttribution = findViewById(R.id.splashAttribution);

        // Fade + Scale animation for center
        animateSplashIn(splashCenter);

        // Fade in attribution
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setStartOffset(600);
        fadeIn.setFillAfter(true);
        splashAttribution.startAnimation(fadeIn);
        splashLoader.startAnimation(fadeIn);

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::goToMain, SPLASH_DURATION_MS);
    }

    private void animateSplashIn(View view) {
        ScaleAnimation scale = new ScaleAnimation(
                0.7f, 1f, 0.7f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(700);

        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        alpha.setDuration(700);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        set.setFillAfter(true);

        view.startAnimation(set);
    }

    private void applyDarkModePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void goToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // No transition animation (clean hand-off)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
