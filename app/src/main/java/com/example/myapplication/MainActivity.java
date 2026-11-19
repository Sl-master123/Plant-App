/** Displays, filters, and loads the user's saved plants. */
package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPlants;
    private PlantAdapter plantAdapter;
    private List<Plant> plantList;
    private View layoutEmptyState;

    private Button btnFilterAll, btnFilterFav, btnFilterIndoor, btnFilterVeg, btnFilterFruit, btnFilterHerb, btnFilterSucculent, btnFilterFlower;
    private String currentFilter = "ALL";

    private AppDatabase database;
    private ExecutorService databaseExecutor;

    private static final int REQ_NOTIF_PERM = 1001;
    private static final String PREFS_NAME = "PlantAppPrefs";
    private static final String KEY_PERM_ASKED = "permission_asked";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode before layout inflation
        applyDarkModePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RecyclerView & Empty State
        recyclerViewPlants = findViewById(R.id.recyclerViewPlants);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        plantList = new ArrayList<>();
        database = AppDatabase.getInstance(this);
        plantAdapter = new PlantAdapter(plantList, database.plantDao());

        recyclerViewPlants.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPlants.setAdapter(plantAdapter);

        databaseExecutor = Executors.newSingleThreadExecutor();

        // Initialize Notification Channel
        NotificationHelper.createNotificationChannel(this);

        // Find Filter Buttons
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterFav = findViewById(R.id.btnFilterFav);
        btnFilterIndoor = findViewById(R.id.btnFilterIndoor);
        btnFilterVeg = findViewById(R.id.btnFilterVeg);
        btnFilterFruit = findViewById(R.id.btnFilterFruit);
        btnFilterHerb = findViewById(R.id.btnFilterHerb);
        btnFilterSucculent = findViewById(R.id.btnFilterSucculent);
        btnFilterFlower = findViewById(R.id.btnFilterFlower);

        // Filter Click Listeners
        btnFilterAll.setOnClickListener(v -> setFilter("ALL"));
        btnFilterFav.setOnClickListener(v -> setFilter("FAV"));
        btnFilterIndoor.setOnClickListener(v -> setFilter("Indoor"));
        btnFilterVeg.setOnClickListener(v -> setFilter("Vegetables"));
        btnFilterFruit.setOnClickListener(v -> setFilter("Fruits"));
        btnFilterHerb.setOnClickListener(v -> setFilter("Herbs"));
        btnFilterSucculent.setOnClickListener(v -> setFilter("Succulents"));
        btnFilterFlower.setOnClickListener(v -> setFilter("Flowers"));

        // Settings Button
        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Add Plant button
        Button btnAddPlant = findViewById(R.id.btnAddPlant);
        btnAddPlant.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddPlantActivity.class);
            startActivity(intent);
        });

        // Check & show first-launch permission dialog
        checkFirstLaunchPermission();

        // Load plants from database
        loadPlantsFromDatabase();
    }

    private void checkFirstLaunchPermission() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean askedBefore = prefs.getBoolean(KEY_PERM_ASKED, false);

        if (!askedBefore && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            if (!hasPermission) {
                // Show custom explanation dialog
                new AlertDialog.Builder(this)
                        .setTitle("🌱 Enable Plant Reminders")
                        .setMessage("Plant Care needs notification permission so we can remind you when it's time to water your plants on schedule!")
                        .setPositiveButton("Allow Notifications", (dialog, which) -> {
                            prefs.edit().putBoolean(KEY_PERM_ASKED, true).apply();
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                    REQ_NOTIF_PERM
                            );
                        })
                        .setNegativeButton("Maybe Later", (dialog, which) -> {
                            prefs.edit().putBoolean(KEY_PERM_ASKED, true).apply();
                        })
                        .setCancelable(false)
                        .show();
            } else {
                prefs.edit().putBoolean(KEY_PERM_ASKED, true).apply();
            }
        }
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateFilterButtonStyles();
        loadPlantsFromDatabase();
    }

    private void updateFilterButtonStyles() {
        // Reset all buttons to default grey
        resetButtonStyle(btnFilterAll);
        resetButtonStyle(btnFilterFav);
        resetButtonStyle(btnFilterIndoor);
        resetButtonStyle(btnFilterVeg);
        resetButtonStyle(btnFilterFruit);
        resetButtonStyle(btnFilterHerb);
        resetButtonStyle(btnFilterSucculent);
        resetButtonStyle(btnFilterFlower);

        // Highlight selected
        if ("ALL".equals(currentFilter)) highlightButtonStyle(btnFilterAll);
        else if ("FAV".equals(currentFilter)) highlightButtonStyle(btnFilterFav);
        else if ("Indoor".equals(currentFilter)) highlightButtonStyle(btnFilterIndoor);
        else if ("Vegetables".equals(currentFilter)) highlightButtonStyle(btnFilterVeg);
        else if ("Fruits".equals(currentFilter)) highlightButtonStyle(btnFilterFruit);
        else if ("Herbs".equals(currentFilter)) highlightButtonStyle(btnFilterHerb);
        else if ("Succulents".equals(currentFilter)) highlightButtonStyle(btnFilterSucculent);
        else if ("Flowers".equals(currentFilter)) highlightButtonStyle(btnFilterFlower);
    }

    private void resetButtonStyle(Button button) {
        if (button != null) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_chip_unselected));
            button.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void highlightButtonStyle(Button button) {
        if (button != null) {
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.green_primary));
            button.setTextColor(ContextCompat.getColor(this, R.color.text_on_primary));
        }
    }

    private void applyDarkModePreference() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void loadPlantsFromDatabase() {
        databaseExecutor.execute(() -> {
            List<Plant> plants;
            if ("FAV".equals(currentFilter)) {
                plants = database.plantDao().getFavoritePlants();
            } else if (!"ALL".equals(currentFilter)) {
                plants = database.plantDao().getPlantsByCategory(currentFilter);
            } else {
                plants = database.plantDao().getAllPlants();
            }

            runOnUiThread(() -> {
                plantList.clear();
                plantList.addAll(plants);
                plantAdapter.notifyDataSetChanged();

                if (plantList.isEmpty()) {
                    layoutEmptyState.setVisibility(View.VISIBLE);
                    recyclerViewPlants.setVisibility(View.GONE);
                } else {
                    layoutEmptyState.setVisibility(View.GONE);
                    recyclerViewPlants.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null && databaseExecutor != null) {
            loadPlantsFromDatabase();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseExecutor != null) {
            databaseExecutor.shutdown();
        }
    }
}