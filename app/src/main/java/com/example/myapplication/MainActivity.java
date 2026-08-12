package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPlants;
    private PlantAdapter plantAdapter;
    private List<Plant> plantList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        recyclerViewPlants = findViewById(R.id.recyclerViewPlants);

        plantList = new ArrayList<>();

        plantList.add(new Plant(
                "Monstera",
                "4 months old",
                "A beautiful tropical indoor plant."
        ));

        plantList.add(new Plant(
                "Pothos",
                "6 months old",
                "An easy-care plant that grows well indoors."
        ));

        plantList.add(new Plant(
                "Snake Plant",
                "8 months old",
                "A hardy plant that needs very little water."
        ));

        plantList.add(new Plant(
                "Peace Lily",
                "3 months old",
                "A beautiful indoor plant with elegant flowers."
        ));

        plantAdapter = new PlantAdapter(plantList);

        recyclerViewPlants.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewPlants.setAdapter(plantAdapter);

        // Add Plant button
        Button btnAddPlant = findViewById(R.id.btnAddPlant);

        btnAddPlant.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddPlantActivity.class);
            startActivity(intent);
        });
    }
}