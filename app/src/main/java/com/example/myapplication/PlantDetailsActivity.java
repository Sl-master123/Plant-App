package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ============================================================================
 * PlantDetailsActivity.java
 * ============================================================================
 * Role: Detailed Plant Profile, Live Status, and Care Operations Screen.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Deep Dive UI:
 *    Presents complete details for a single plant: high-resolution photo header
 *    or fallback category emoji, category, next watering countdown, full schedule,
 *    care notes, and date added.
 * 2. Interactive Care Actions:
 *    - "💧 Water Now": Updates last-watered timestamp to current time, recalculates
 *      next schedule, and updates Room DB.
 *    - Favorite Toggle: Toggles favorite flag with immediate database persistence.
 *    - Reminder Toggle Switch: Enables or disables notifications on the fly,
 *      automatically registering or cancelling AlarmManager alarms.
 *    - Edit Mode: Launches AddPlantActivity in edit mode with plant ID.
 *    - Delete Action: Displays confirmation dialog, removes from DB, cancels alarms,
 *      and closes screen.
 * 3. Thread-Safe Architecture:
 *    All Room database queries and updates are dispatched through an ExecutorService.
 * ============================================================================
 */
public class PlantDetailsActivity extends AppCompatActivity {

    private AppDatabase database;
    private ExecutorService databaseExecutor;
    private Plant currentPlant;
    private int plantId = -1;

    private static final String PREFS_NAME = "PlantAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Step 1: Apply user dark mode preference
        applyDarkModePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_details);

        database = AppDatabase.getInstance(this);
        databaseExecutor = Executors.newSingleThreadExecutor();

        // Step 2: Validate plant ID passed via Intent
        plantId = getIntent().getIntExtra("plant_id", -1);
        if (plantId == -1) {
            Toast.makeText(this, "Invalid plant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Step 3: Setup Navigation & Action Buttons
        ImageButton btnBack = findViewById(R.id.btnDetailsBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        // Favorite Toggle
        TextView tvFav = findViewById(R.id.tvDetailsFavorite);
        if (tvFav != null) {
            tvFav.setOnClickListener(v -> {
                if (currentPlant != null) {
                    currentPlant.setFavorite(!currentPlant.isFavorite());
                    updatePlantInDatabase();
                }
            });
        }

        // "Water Now" Action Button
        Button btnWater = findViewById(R.id.btnWaterNow);
        if (btnWater != null) {
            btnWater.setOnClickListener(v -> {
                if (currentPlant != null) {
                    currentPlant.setLastWateredTimestamp(System.currentTimeMillis());
                    updatePlantInDatabase();
                    Toast.makeText(this, "✓ " + currentPlant.getName() + " watered!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Reminder Toggle Switch
        Switch swReminder = findViewById(R.id.switchDetailsReminder);
        if (swReminder != null) {
            swReminder.setOnCheckedChangeListener((btn, checked) -> {
                if (currentPlant != null) {
                    currentPlant.setReminderEnabled(checked);
                    updatePlantInDatabase();
                    String msg = checked ? "Reminders enabled 🔔" : "Reminders paused";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Edit Button -> Launches AddPlantActivity with current plant ID
        Button btnEdit = findViewById(R.id.btnDetailsEdit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddPlantActivity.class);
                intent.putExtra("plant_id", currentPlant.getId());
                startActivity(intent);
            });
        }

        // Delete Button
        Button btnDelete = findViewById(R.id.btnDetailsDelete);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> showDeleteConfirmation());
        }
    }

    private void applyDarkModePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data from Room DB in case plant was modified in AddPlantActivity
        loadPlantData();
    }

    /**
     * Queries Room DB for current plant record on background executor thread.
     */
    private void loadPlantData() {
        databaseExecutor.execute(() -> {
            Plant plant = database.plantDao().getPlantById(plantId);
            if (isFinishing()) return;
            runOnUiThread(() -> {
                if (plant != null) {
                    currentPlant = plant;
                    updateUI();
                } else {
                    Toast.makeText(this, "Plant not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    /**
     * Binds all plant properties to their corresponding UI views.
     */
    private void updateUI() {
        if (currentPlant == null) return;

        // 1. Photo header or fallback emoji
        ImageView ivPhoto = findViewById(R.id.ivDetailsPlantImage);
        TextView tvEmoji = findViewById(R.id.tvDetailsEmoji);
        LinearLayout layoutHeader = findViewById(R.id.layoutDetailsHeader);

        if (currentPlant.getImageUri() != null && !currentPlant.getImageUri().isEmpty()
                && new File(currentPlant.getImageUri()).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPlant.getImageUri());
            if (bitmap != null && ivPhoto != null) {
                ivPhoto.setImageBitmap(bitmap);
                ivPhoto.setVisibility(View.VISIBLE);
                if (layoutHeader != null) layoutHeader.setVisibility(View.GONE);
            } else {
                showEmojiHeader(ivPhoto, layoutHeader, tvEmoji);
            }
        } else {
            showEmojiHeader(ivPhoto, layoutHeader, tvEmoji);
        }

        // 2. Name and Category title
        TextView tvName = findViewById(R.id.tvDetailsPlantName);
        TextView tvCategory = findViewById(R.id.tvDetailsCategory);
        if (tvName != null) tvName.setText(currentPlant.getName());
        if (tvCategory != null)
            tvCategory.setText((currentPlant.getCategory() != null ? currentPlant.getCategory() : "Indoor") + " Plant");

        // 3. Favorite heart status
        TextView tvFav = findViewById(R.id.tvDetailsFavorite);
        if (tvFav != null) tvFav.setText(currentPlant.isFavorite() ? "♥" : "♡");

        // 4. Live watering countdown calculation
        long nextTime = WateringReminderReceiver.calculateNextTriggerTime(currentPlant);
        if (nextTime <= 0) {
            nextTime = currentPlant.getLastWateredTimestamp()
                    + (currentPlant.getWateringFrequency() * 24L * 60 * 60 * 1000);
        }
        long timeLeft = nextTime - System.currentTimeMillis();
        long daysLeft = timeLeft / (24L * 60 * 60 * 1000);

        TextView tvNext = findViewById(R.id.tvDetailsNextWatering);
        if (tvNext != null) {
            if (timeLeft < 0) {
                tvNext.setText("⚠️ Overdue – water now!");
                tvNext.setTextColor(0xFFC62828);
            } else if (daysLeft == 0) {
                tvNext.setText("💧 Today – water now!");
                tvNext.setTextColor(0xFF2E7D32);
            } else if (daysLeft == 1) {
                tvNext.setText("💧 Tomorrow");
                tvNext.setTextColor(0xFF2E7D32);
            } else {
                tvNext.setText("💧 In " + daysLeft + " days");
                tvNext.setTextColor(0xFF4CAF50);
            }
        }

        // 5. Reminder Schedule Summary Text
        TextView tvSchedule = findViewById(R.id.tvDetailsReminderSchedule);
        if (tvSchedule != null) {
            if (currentPlant.isReminderEnabled()) {
                int interval = currentPlant.getReminderIntervalDays() > 0
                        ? currentPlant.getReminderIntervalDays()
                        : currentPlant.getWateringFrequency();
                String freq = interval == 1 ? "Every day" : "Every " + interval + " days";
                String time = String.format(Locale.getDefault(), "%02d:%02d",
                        currentPlant.getReminderHour(), currentPlant.getReminderMinute());
                tvSchedule.setText(freq + " at " + time);
            } else {
                tvSchedule.setText("Reminders are paused");
            }
        }

        // 6. Reminder Switch synchronization (disable listener during programmatic setChecked)
        Switch swReminder = findViewById(R.id.switchDetailsReminder);
        if (swReminder != null) {
            swReminder.setOnCheckedChangeListener(null);
            swReminder.setChecked(currentPlant.isReminderEnabled());
            swReminder.setOnCheckedChangeListener((btn, checked) -> {
                currentPlant.setReminderEnabled(checked);
                updatePlantInDatabase();
                String msg = checked ? "Reminders enabled 🔔" : "Reminders paused";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        // 7. Care Notes description
        TextView tvDesc = findViewById(R.id.tvDetailsDescription);
        if (tvDesc != null) {
            String desc = currentPlant.getDescription();
            tvDesc.setText((desc != null && !desc.isEmpty()) ? desc : "No care notes added.");
        }

        // 8. Date Added formatting
        TextView tvAdded = findViewById(R.id.tvDetailsAddedDate);
        if (tvAdded != null) {
            long created = currentPlant.getCreatedTimestamp() > 0
                    ? currentPlant.getCreatedTimestamp()
                    : currentPlant.getLastWateredTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            tvAdded.setText(sdf.format(new Date(created)));
        }
    }

    private void showEmojiHeader(ImageView ivPhoto, LinearLayout layoutHeader, TextView tvEmoji) {
        if (ivPhoto != null) {
            ivPhoto.setImageBitmap(null);
            ivPhoto.setVisibility(View.GONE);
        }
        if (layoutHeader != null) layoutHeader.setVisibility(View.VISIBLE);
        if (tvEmoji != null) {
            String cat = currentPlant.getCategory() != null ? currentPlant.getCategory() : "";
            switch (cat) {
                case "Vegetables": tvEmoji.setText("🥦"); break;
                case "Fruits":     tvEmoji.setText("🍓"); break;
                case "Herbs":      tvEmoji.setText("🌿"); break;
                case "Succulents": tvEmoji.setText("🌵"); break;
                case "Flowers":    tvEmoji.setText("🌸"); break;
                default:           tvEmoji.setText("🌿"); break;
            }
        }
    }

    /**
     * Persists changes to Room DB and updates the AlarmManager alarm accordingly.
     */
    private void updatePlantInDatabase() {
        if (currentPlant == null) return;
        databaseExecutor.execute(() -> {
            database.plantDao().updatePlant(currentPlant);
            if (currentPlant.isReminderEnabled()) {
                WateringReminderReceiver.schedulePlantAlarm(this, currentPlant);
            } else {
                WateringReminderReceiver.cancelPlantAlarm(this, currentPlant.getId());
            }
            runOnUiThread(this::updateUI);
        });
    }

    /**
     * Shows confirmation dialog before permanently removing plant.
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Plant")
                .setMessage("Remove \"" + currentPlant.getName() + "\" from your garden?")
                .setPositiveButton("Delete 🗑", (dialog, which) -> deletePlant())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePlant() {
        databaseExecutor.execute(() -> {
            database.plantDao().deletePlant(currentPlant);
            WateringReminderReceiver.cancelPlantAlarm(this, currentPlant.getId());
            runOnUiThread(() -> {
                Toast.makeText(this, currentPlant.getName() + " deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseExecutor != null) databaseExecutor.shutdown();
    }
}
