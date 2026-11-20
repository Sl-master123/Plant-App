package com.example.myapplication;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * ============================================================================
 * AddPlantActivity.java
 * ============================================================================
 * Role: Plant Creation and Editing Form with Comprehensive Reminder Setup.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Dual Mode (Create vs Edit):
 *    - If intent contains "plant_id", loads existing plant data and displays
 *      "✓ Update Plant".
 *    - Otherwise, creates a brand new Plant record.
 * 2. Modern Activity Result API (Photo Picker):
 *    Uses ActivityResultContracts.GetContent() to safely select images from
 *    device gallery without deprecated startActivityForResult().
 * 3. File System & Image Persistence:
 *    Copies selected photos into app internal storage (getFilesDir()/plant_photos)
 *    so photos remain accessible even if original files are moved/deleted.
 * 4. Interactive Dialog Pickers:
 *    - DatePickerDialog for Start Date and End Date.
 *    - TimePickerDialog for exact watering reminder time of day.
 *    - Dynamic summary preview: "Reminds every N days at HH:mm from X to Y".
 * 5. Automatic Alarm Scheduling:
 *    Upon saving, immediately schedules an exact alarm via WateringReminderReceiver.
 * ============================================================================
 */
public class AddPlantActivity extends AppCompatActivity {

    // Input fields
    private EditText etPlantName;
    private EditText etPlantDescription;

    // Category Selector Buttons
    private Button btnCatIndoor, btnCatVegetable, btnCatFruit, btnCatHerb, btnCatSucculent, btnCatFlower;
    private String selectedCategory = "Indoor";

    // Photo Selection
    private ImageView ivAddPlantPhoto;
    private View layoutPhotoPlaceholder;
    private String selectedImageUriString = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    // Reminder Configuration Views
    private Switch switchWaterReminder;
    private LinearLayout layoutReminderDetails;
    private Button btnDecInterval, btnIncInterval;
    private TextView tvIntervalText;
    private LinearLayout btnPickStartDate, btnPickEndDate, btnPickTime;
    private TextView tvStartDate, tvEndDate, tvReminderTime, tvReminderSummary;

    // Reminder State Variables
    private int intervalDays = 1;
    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();
    private int reminderHour = 9;   // Default 9:00 AM
    private int reminderMinute = 0;

    // Save Button
    private Button btnAddPlant;

    // Edit Mode State
    private int plantId = -1;
    private Plant editingPlant;
    private AppDatabase database;

    // Date/Time Formatters
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkModePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        database = AppDatabase.getInstance(this);

        // 1. Bind Form Views
        etPlantName = findViewById(R.id.etPlantName);
        etPlantDescription = findViewById(R.id.etPlantDescription);

        btnCatIndoor = findViewById(R.id.btnCatIndoor);
        btnCatVegetable = findViewById(R.id.btnCatVegetable);
        btnCatFruit = findViewById(R.id.btnCatFruit);
        btnCatHerb = findViewById(R.id.btnCatHerb);
        btnCatSucculent = findViewById(R.id.btnCatSucculent);
        btnCatFlower = findViewById(R.id.btnCatFlower);

        FrameLayout btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        ivAddPlantPhoto = findViewById(R.id.ivAddPlantPhoto);
        layoutPhotoPlaceholder = findViewById(R.id.layoutPhotoPlaceholder);

        switchWaterReminder = findViewById(R.id.switchWaterReminder);
        layoutReminderDetails = findViewById(R.id.layoutReminderDetails);
        btnDecInterval = findViewById(R.id.btnDecInterval);
        btnIncInterval = findViewById(R.id.btnIncInterval);
        tvIntervalText = findViewById(R.id.tvIntervalText);

        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        btnPickTime = findViewById(R.id.btnPickTime);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        tvReminderSummary = findViewById(R.id.tvReminderSummary);

        btnAddPlant = findViewById(R.id.btnAddPlant);

        // 2. Set default End Date = Start Date + 30 days
        endCal.add(Calendar.DAY_OF_YEAR, 30);
        updateDateDisplays();

        // 3. Register Photo Picker using modern Activity Result Contract
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Persist photo to internal storage
                        String savedPath = saveImageToInternalStorage(uri);
                        if (savedPath != null) {
                            selectedImageUriString = savedPath;
                            displaySelectedImage(savedPath);
                        }
                    }
                }
        );
        btnSelectPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 4. Setup Category Selector Buttons
        btnCatIndoor.setOnClickListener(v -> selectCategory("Indoor"));
        btnCatVegetable.setOnClickListener(v -> selectCategory("Vegetables"));
        btnCatFruit.setOnClickListener(v -> selectCategory("Fruits"));
        btnCatHerb.setOnClickListener(v -> selectCategory("Herbs"));
        btnCatSucculent.setOnClickListener(v -> selectCategory("Succulents"));
        btnCatFlower.setOnClickListener(v -> selectCategory("Flowers"));

        // 5. Setup Reminder Toggle (shows/hides reminder settings card)
        switchWaterReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutReminderDetails.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // 6. Setup Interval Stepper (+ / - buttons)
        btnDecInterval.setOnClickListener(v -> {
            if (intervalDays > 1) {
                intervalDays--;
                updateIntervalDisplay();
            }
        });
        btnIncInterval.setOnClickListener(v -> {
            if (intervalDays < 30) {
                intervalDays++;
                updateIntervalDisplay();
            }
        });

        // 7. Setup Date Pickers
        btnPickStartDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        startCal.set(Calendar.YEAR, year);
                        startCal.set(Calendar.MONTH, month);
                        startCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplays();
                    },
                    startCal.get(Calendar.YEAR),
                    startCal.get(Calendar.MONTH),
                    startCal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        btnPickEndDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        endCal.set(Calendar.YEAR, year);
                        endCal.set(Calendar.MONTH, month);
                        endCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplays();
                    },
                    endCal.get(Calendar.YEAR),
                    endCal.get(Calendar.MONTH),
                    endCal.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });

        // 8. Setup Time Picker
        btnPickTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        reminderHour = hourOfDay;
                        reminderMinute = minute;
                        updateTimeDisplay();
                    },
                    reminderHour,
                    reminderMinute,
                    false // 12-hour format with AM/PM
            );
            dialog.show();
        });

        // 9. Save Plant Button Click
        btnAddPlant.setOnClickListener(v -> savePlant());

        // 10. Check if editing an existing plant
        plantId = getIntent().getIntExtra("plant_id", -1);
        if (plantId != -1) {
            btnAddPlant.setText("✓  Update Plant");
            loadEditingPlant();
        }

        // 11. Back Navigation Button
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void applyDarkModePreference() {
        SharedPreferences prefs = getSharedPreferences("PlantAppPrefs", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Updates selected category chip style and stores active category.
     */
    private void selectCategory(String category) {
        selectedCategory = category;

        int unselBg = ContextCompat.getColor(this, R.color.bg_chip_unselected);
        int unselText = ContextCompat.getColor(this, R.color.text_secondary);
        btnCatIndoor.setBackgroundColor(unselBg);    btnCatIndoor.setTextColor(unselText);
        btnCatVegetable.setBackgroundColor(unselBg); btnCatVegetable.setTextColor(unselText);
        btnCatFruit.setBackgroundColor(unselBg);     btnCatFruit.setTextColor(unselText);
        btnCatHerb.setBackgroundColor(unselBg);      btnCatHerb.setTextColor(unselText);
        btnCatSucculent.setBackgroundColor(unselBg); btnCatSucculent.setTextColor(unselText);
        btnCatFlower.setBackgroundColor(unselBg);    btnCatFlower.setTextColor(unselText);

        int selBg = ContextCompat.getColor(this, R.color.bg_chip_selected);
        int selText = ContextCompat.getColor(this, R.color.text_green);
        if ("Indoor".equals(category)) {
            btnCatIndoor.setBackgroundColor(selBg); btnCatIndoor.setTextColor(selText);
        } else if ("Vegetables".equals(category)) {
            btnCatVegetable.setBackgroundColor(selBg); btnCatVegetable.setTextColor(selText);
        } else if ("Fruits".equals(category)) {
            btnCatFruit.setBackgroundColor(selBg); btnCatFruit.setTextColor(selText);
        } else if ("Herbs".equals(category)) {
            btnCatHerb.setBackgroundColor(selBg); btnCatHerb.setTextColor(selText);
        } else if ("Succulents".equals(category)) {
            btnCatSucculent.setBackgroundColor(selBg); btnCatSucculent.setTextColor(selText);
        } else if ("Flowers".equals(category)) {
            btnCatFlower.setBackgroundColor(selBg); btnCatFlower.setTextColor(selText);
        }
    }

    private void updateIntervalDisplay() {
        if (intervalDays == 1) {
            tvIntervalText.setText("Every 1 day");
        } else {
            tvIntervalText.setText("Every " + intervalDays + " days");
        }
        updateSummaryText();
    }

    private void updateDateDisplays() {
        tvStartDate.setText(dateFormat.format(startCal.getTime()));
        tvEndDate.setText(dateFormat.format(endCal.getTime()));
        updateSummaryText();
    }

    private void updateTimeDisplay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, reminderHour);
        cal.set(Calendar.MINUTE, reminderMinute);
        tvReminderTime.setText(timeFormat.format(cal.getTime()));
        updateSummaryText();
    }

    /**
     * Builds and updates the dynamic summary preview sentence.
     */
    private void updateSummaryText() {
        String freq = intervalDays == 1 ? "every day" : "every " + intervalDays + " days";
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, reminderHour);
        cal.set(Calendar.MINUTE, reminderMinute);
        String timeStr = timeFormat.format(cal.getTime());
        String startStr = dateFormat.format(startCal.getTime());
        String endStr = dateFormat.format(endCal.getTime());

        tvReminderSummary.setText("Reminds " + freq + " at " + timeStr + "\nFrom " + startStr + " to " + endStr);
    }

    /**
     * Copies selected gallery image into internal app storage directory.
     * Prevents security permission loss when Android restarts.
     *
     * @param uri Content URI from image picker
     * @return Absolute file path on disk, or null on error
     */
    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File dir = new File(getFilesDir(), "plant_photos");
            if (!dir.exists()) dir.mkdirs();

            File photoFile = new File(dir, "photo_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(photoFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            return photoFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void displaySelectedImage(String path) {
        if (path != null && !path.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                ivAddPlantPhoto.setImageBitmap(bitmap);
                ivAddPlantPhoto.setVisibility(View.VISIBLE);
                layoutPhotoPlaceholder.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Loads existing plant fields into the form for Edit Mode.
     */
    private void loadEditingPlant() {
        new Thread(() -> {
            editingPlant = database.plantDao().getPlantById(plantId);
            if (editingPlant != null) {
                runOnUiThread(() -> {
                    etPlantName.setText(editingPlant.getName());
                    etPlantDescription.setText(editingPlant.getDescription());
                    selectCategory(editingPlant.getCategory() != null ? editingPlant.getCategory() : "Indoor");

                    if (editingPlant.getImageUri() != null) {
                        selectedImageUriString = editingPlant.getImageUri();
                        displaySelectedImage(selectedImageUriString);
                    }

                    switchWaterReminder.setChecked(editingPlant.isReminderEnabled());
                    intervalDays = editingPlant.getReminderIntervalDays() > 0 ? editingPlant.getReminderIntervalDays() : 1;
                    updateIntervalDisplay();

                    if (editingPlant.getReminderStartDate() > 0) {
                        startCal.setTimeInMillis(editingPlant.getReminderStartDate());
                    }
                    if (editingPlant.getReminderEndDate() > 0) {
                        endCal.setTimeInMillis(editingPlant.getReminderEndDate());
                    }
                    updateDateDisplays();

                    reminderHour = editingPlant.getReminderHour();
                    reminderMinute = editingPlant.getReminderMinute();
                    updateTimeDisplay();
                });
            }
        }).start();
    }

    /**
     * Validates input, saves or updates the plant in Room DB on a worker thread,
     * schedules the system alarm, and finishes the Activity.
     */
    private void savePlant() {
        String name = etPlantName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a plant name", Toast.LENGTH_SHORT).show();
            return;
        }

        String rawDescription = etPlantDescription.getText().toString().trim();
        final String description = rawDescription.isEmpty() ? "Care for your " + name + " regularly." : rawDescription;

        boolean reminderEnabled = switchWaterReminder.isChecked();
        long startDate = startCal.getTimeInMillis();
        long endDate = endCal.getTimeInMillis();

        // Perform Room Database insert/update off the UI thread
        new Thread(() -> {
            Plant plantToSave;
            if (plantId != -1 && editingPlant != null) {
                // Update existing record
                editingPlant.setName(name);
                editingPlant.setCategory(selectedCategory);
                editingPlant.setDescription(description);
                if (selectedImageUriString != null) {
                    editingPlant.setImageUri(selectedImageUriString);
                }
                editingPlant.setReminderEnabled(reminderEnabled);
                editingPlant.setReminderStartDate(startDate);
                editingPlant.setReminderEndDate(endDate);
                editingPlant.setReminderIntervalDays(intervalDays);
                editingPlant.setWateringFrequency(intervalDays);
                editingPlant.setReminderHour(reminderHour);
                editingPlant.setReminderMinute(reminderMinute);

                database.plantDao().updatePlant(editingPlant);
                plantToSave = editingPlant;

                runOnUiThread(() -> Toast.makeText(this, name + " updated!", Toast.LENGTH_SHORT).show());
            } else {
                // Create brand new plant record
                plantToSave = new Plant(
                        name,
                        selectedCategory,
                        "1 day old",
                        description,
                        false,
                        intervalDays,
                        System.currentTimeMillis(),
                        selectedImageUriString,
                        System.currentTimeMillis(),
                        reminderEnabled,
                        startDate,
                        endDate,
                        intervalDays,
                        reminderHour,
                        reminderMinute
                );
                database.plantDao().insertPlant(plantToSave);

                // Fetch inserted plant with generated primary key
                List<Plant> plants = database.plantDao().getAllPlants();
                if (!plants.isEmpty()) {
                    plantToSave = plants.get(plants.size() - 1);
                }

                runOnUiThread(() -> Toast.makeText(this, name + " added to your garden!", Toast.LENGTH_SHORT).show());
            }

            // Schedule or update AlarmManager alarm for this plant
            if (plantToSave != null) {
                WateringReminderReceiver.schedulePlantAlarm(this, plantToSave);
            }

            finish();
        }).start();
    }
}
