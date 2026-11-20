package com.example.myapplication;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * ============================================================================
 * AppDatabase.java
 * ============================================================================
 * Role: Local Database Configuration using Android Room Persistence Library.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Room Architecture: Acts as an abstraction layer over SQLite for robust,
 *    type-safe database access with compile-time query verification.
 * 2. Singleton Pattern: Ensures only one instance of the database is active
 *    at any given time, preventing memory leaks and database lock contentions.
 * 3. Migration Strategy: Uses fallbackToDestructiveMigration() for rapid
 *    prototyping during development when schema changes.
 * ============================================================================
 */
@Database(
        entities = {Plant.class},  // Tables registered in this database
        version = 4,               // Database schema version
        exportSchema = false       // Do not export schema JSON to keep build simple
)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Data Access Object (DAO) getter.
     * Room automatically generates the implementation.
     */
    public abstract PlantDao plantDao();

    // Singleton instance holder
    private static volatile AppDatabase instance;

    /**
     * Returns the singleton AppDatabase instance.
     * Synchronized to guarantee thread safety during initialization.
     *
     * @param context Application context to avoid leaking Activity contexts
     * @return Single instance of AppDatabase
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "plant_database"  // SQLite database file name on device
            )
            .fallbackToDestructiveMigration() // Recreates tables if version changes without migration script
            .build();
        }
        return instance;
    }
}
