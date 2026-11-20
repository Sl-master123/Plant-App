package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * ============================================================================
 * PlantDao.java (Data Access Object)
 * ============================================================================
 * Role: Defines the database interactions and SQL queries for the 'plants' table.
 * 
 * Key Concepts for Project Demonstration:
 * 1. Room DAO Pattern: Clean separation between data access logic and UI code.
 * 2. Compile-Time SQL Validation: Room checks queries at compile time, catching
 *    syntax errors and schema mismatches before the app even runs.
 * 3. CRUD Operations:
 *    - Create: insertPlant()
 *    - Read: getAllPlants(), getFavoritePlants(), getPlantsByCategory(), getPlantById()
 *    - Update: updatePlant()
 *    - Delete: deletePlant()
 * ============================================================================
 */
@Dao
public interface PlantDao {

    /**
     * Inserts a new plant into the database.
     * @param plant Plant entity to insert
     */
    @Insert
    void insertPlant(Plant plant);

    /**
     * Retrieves all plants stored in the database.
     * @return List of all plants
     */
    @Query("SELECT * FROM plants")
    List<Plant> getAllPlants();

    /**
     * Retrieves only plants marked as favorite.
     * favorite = 1 represents boolean true in SQLite.
     * @return List of favorite plants
     */
    @Query("SELECT * FROM plants WHERE favorite = 1")
    List<Plant> getFavoritePlants();

    /**
     * Retrieves plants belonging to a specific category (e.g., Indoor, Vegetables, etc.).
     * @param category Category name to filter by
     * @return Filtered list of plants
     */
    @Query("SELECT * FROM plants WHERE category = :category")
    List<Plant> getPlantsByCategory(String category);

    /**
     * Finds a single plant by its unique primary key ID.
     * @param plantId Unique integer ID
     * @return Plant entity or null if not found
     */
    @Query("SELECT * FROM plants WHERE id = :plantId LIMIT 1")
    Plant getPlantById(int plantId);

    /**
     * Updates existing plant details (e.g., name, schedule, last watered timestamp).
     * @param plant Plant entity with updated values
     */
    @Update
    void updatePlant(Plant plant);

    /**
     * Permanently deletes a plant record from the database.
     * @param plant Plant entity to remove
     */
    @Delete
    void deletePlant(Plant plant);
}
