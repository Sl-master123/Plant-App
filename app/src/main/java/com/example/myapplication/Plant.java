/** Defines the plant data and its watering reminder settings. */
package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "plants")
public class Plant {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String category; // "Indoor", "Vegetables", "Fruits", "Herbs", "Succulents", "Flowers"
    private String age;
    private String description;
    private boolean favorite;
    private int wateringFrequency; // in days
    private long lastWateredTimestamp;
    private String imageUri;
    private long createdTimestamp;

    // Reminder settings
    private boolean reminderEnabled;
    private long reminderStartDate;
    private long reminderEndDate;
    private int reminderIntervalDays;
    private int reminderHour;
    private int reminderMinute;

    // Constructor used by Room
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

    // Constructor used by UI code
    @Ignore
    public Plant(
            String name,
            String category,
            String age,
            String description) {

        this(name, category, age, description, null);
    }

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
        this.reminderHour = 9;
        this.reminderMinute = 0;
    }

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