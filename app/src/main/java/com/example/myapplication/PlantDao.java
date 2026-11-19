/** Defines database queries and actions for plant records. */
package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlantDao {

    @Insert
    void insertPlant(Plant plant);

    @Query("SELECT * FROM plants")
    List<Plant> getAllPlants();

    @Query("SELECT * FROM plants WHERE favorite = 1")
    List<Plant> getFavoritePlants();

    @Query("SELECT * FROM plants WHERE category = :category")
    List<Plant> getPlantsByCategory(String category);

    @Query("SELECT * FROM plants WHERE id = :plantId LIMIT 1")
    Plant getPlantById(int plantId);

    @Update
    void updatePlant(Plant plant);

    @Delete
    void deletePlant(Plant plant);
}