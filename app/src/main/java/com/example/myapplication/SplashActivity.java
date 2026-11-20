package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * ============================================================================
 * SplashActivity.java
 * ============================================================================
 * Role: Welcome Screen & App Launch Orchestrator.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Entry Point: Declared in AndroidManifest.xml as the MAIN/LAUNCHER activity.
 * 2. Dark Mode Pre-loading: Applies user's dark/light mode preference before
 *    calling super.onCreate() to prevent theme flickering during launch.
 * 3. Smart First Launch: Uses SharedPreferences ("first_launch_done") so returning
 *    users transition directly into MainActivity without waiting on every launch.
 * 4. Android View Animations: Demonstrates programmatic AnimationSet combining
 *    ScaleAnimation (growth) and AlphaAnimation (fade-in) for a polished UI feel.
 * 5. Group Attribution: Displays credits for Group "OAKSHADE" and subject.
 * ============================================================================
 */
public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PlantAppPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch_done";
    private static final int SPLASH_DURATION_MS = 2200; // 2.2 seconds splash duration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Step 1: Apply dark mode preference before view inflation
        applyDarkModePreference();
        super.onCreate(savedInstanceState);

        // Step 2: Check if splash screen was already displayed on this installation
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean firstLaunchDone = prefs.getBoolean(KEY_FIRST_LAUNCH, false);

        if (firstLaunchDone) {
            // Returning user: skip splash immediately for fast app entry
            goToMain();
            return;
        }

        setContentView(R.layout.activity_splash);

        // Step 3: Mark first launch as completed
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, true).apply();

        // Step 4: Bind views and start entrance animations
        LinearLayout splashCenter = findViewById(R.id.splashCenter);
        View splashLoader = findViewById(R.id.splashLoader);
        View splashAttribution = findViewById(R.id.splashAttribution);

        // Scale + Fade entrance animation for the logo
        animateSplashIn(splashCenter);

        // Smooth fade-in animation for bottom attribution and loading dots
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        fadeIn.setStartOffset(600); // Slight delay for staggered visual effect
        fadeIn.setFillAfter(true);
        splashAttribution.startAnimation(fadeIn);
        splashLoader.startAnimation(fadeIn);

        // Step 5: Delayed navigation to MainActivity using main thread Handler
        new Handler(Looper.getMainLooper()).postDelayed(this::goToMain, SPLASH_DURATION_MS);
    }

    /**
     * Programmatic Scale + Alpha animation applied to the central splash logo.
     */
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

    /**
     * Reads the stored theme setting from SharedPreferences and applies it.
     */
    private void applyDarkModePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Transitions from SplashActivity to MainActivity and clears the back stack.
     */
    private void goToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        // Clear task stack so pressing Back from MainActivity does not return to splash
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Smooth cross-fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
