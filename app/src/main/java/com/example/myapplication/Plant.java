package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * ============================================================================
 * Plant.java (Entity Model)
 * ============================================================================
 * Role: Represents a single plant record stored in the SQLite 'plants' table.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Room Entity: Marked with @Entity(tableName = "plants"). Each instance
 *    corresponds to a row in the database table.
 * 2. Primary Key: @PrimaryKey(autoGenerate = true) ensures an automatic,
 *    unique integer ID for every plant.
 * 3. Smart Watering Fields: Stores reminders, intervals, start/end dates,
 *    time of day (hour & minute), and last-watered timestamp.
 * 4. Image Support: Stores the absolute internal file URI of the captured/selected
 *    photo on the device.
 * ============================================================================
 */
@Entity(tableName = "plants")
public class Plant {

    @PrimaryKey(autoGenerate = true)
    private int id;                         // Unique auto-generated ID

    private String name;                    // Plant name (e.g., "Monstera")
    private String category;                // "Indoor", "Vegetables", "Fruits", "Herbs", "Succulents", "Flowers"
    private String age;                     // Age description (e.g., "6 months")
    private String description;             // Care notes and description
    private boolean favorite;               // Whether marked as favorite (heart icon)
    private int wateringFrequency;          // Watering frequency in days (e.g., every 2 days)
    private long lastWateredTimestamp;      // Epoch millisecond timestamp of last watering
    private String imageUri;                // Internal storage path to plant photo
    private long createdTimestamp;          // Epoch millisecond timestamp when plant was added

    // Reminder Configuration
    private boolean reminderEnabled;        // Toggle switch state for watering notifications
    private long reminderStartDate;         // Epoch millis for reminder period start
    private long reminderEndDate;           // Epoch millis for reminder period expiration
    private int reminderIntervalDays;       // Interval between reminders (e.g. 1 = daily, 3 = every 3 days)
    private int reminderHour;               // Hour of reminder (0-23, 24-hr format)
    private int reminderMinute;             // Minute of reminder (0-59)

    /**
     * Primary constructor used by Room Database to reconstruct Plant objects from SQLite rows.
     */
    public Plant(
            String name,
            String category,
            String age,
            String description,
            boolean favorite,
            int wateringFrequency,
            long lastWateredTimestamp,
            String imageUri,
            long createdTimestamp,
            boolean reminderEnabled,
            long reminderStartDate,
            long reminderEndDate,
            int reminderIntervalDays,
            int reminderHour,
            int reminderMinute) {

        this.name = name;
        this.category = category != null ? category : "Indoor";
        this.age = age;
        this.description = description;
        this.favorite = favorite;
        this.wateringFrequency = wateringFrequency;
        this.lastWateredTimestamp = lastWateredTimestamp;
        this.imageUri = imageUri;
        this.createdTimestamp = createdTimestamp;
        this.reminderEnabled = reminderEnabled;
        this.reminderStartDate = reminderStartDate;
        this.reminderEndDate = reminderEndDate;
        this.reminderIntervalDays = reminderIntervalDays;
        this.reminderHour = reminderHour;
        this.reminderMinute = reminderMinute;
    }

    /**
     * Convenience constructor for basic UI usage.
     * Ignored by Room so it does not conflict with the primary constructor.
     */
    @Ignore
    public Plant(
            String name,
            String category,
            String age,
            String description) {

        this(name, category, age, description, null);
    }

    /**
     * Convenience constructor with image URI and sensible defaults.
     * Ignored by Room.
     */
    @Ignore
    public Plant(
            String name,
            String category,
            String age,
            String description,
            String imageUri) {

        this.name = name;
        this.category = category != null ? category : "Indoor";
        this.age = age;
        this.description = description;
        this.favorite = false;
        this.wateringFrequency = 1;
        this.lastWateredTimestamp = System.currentTimeMillis();
        this.imageUri = imageUri;
        this.createdTimestamp = System.currentTimeMillis();
        this.reminderEnabled = true;
        this.reminderStartDate = System.currentTimeMillis();
        // Default end date: 30 days from now
        this.reminderEndDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
        this.reminderIntervalDays = 1;
        this.reminderHour = 9;   // Default 9:00 AM
        this.reminderMinute = 0;
    }

    // ==========================================
    // Getters and Setters
    // ==========================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getWateringFrequency() {
        return wateringFrequency;
    }

    public void setWateringFrequency(int wateringFrequency) {
        this.wateringFrequency = wateringFrequency;
    }

    public long getLastWateredTimestamp() {
        return lastWateredTimestamp;
    }

    public void setLastWateredTimestamp(long lastWateredTimestamp) {
        this.lastWateredTimestamp = lastWateredTimestamp;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public long getReminderStartDate() {
        return reminderStartDate;
    }

    public void setReminderStartDate(long reminderStartDate) {
        this.reminderStartDate = reminderStartDate;
    }

    public long getReminderEndDate() {
        return reminderEndDate;
    }

    public void setReminderEndDate(long reminderEndDate) {
        this.reminderEndDate = reminderEndDate;
    }

    public int getReminderIntervalDays() {
        return reminderIntervalDays;
    }

    public void setReminderIntervalDays(int reminderIntervalDays) {
        this.reminderIntervalDays = reminderIntervalDays;
    }

    public int getReminderHour() {
        return reminderHour;
    }

    public void setReminderHour(int reminderHour) {
        this.reminderHour = reminderHour;
    }

    public int getReminderMinute() {
        return reminderMinute;
    }

    public void setReminderMinute(int reminderMinute) {
        this.reminderMinute = reminderMinute;
    }
}
