package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddPlantActivity extends AppCompatActivity {

    private EditText etSearchPlant;

    private Button btnMonstera;
    private Button btnPothos;
    private Button btnSnakePlant;
    private Button btnPeaceLily;
    private Button btnSpiderPlant;
    private Button btnAloeVera;

    private View colorGreen;
    private View colorBlue;
    private View colorYellow;
    private View colorPink;
    private View colorPurple;

    private Switch switchWaterReminder;
    private Button btnAddPlant;

    private String selectedPlant = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_plant);

        // Find views
        etSearchPlant = findViewById(R.id.etSearchPlant);

        btnMonstera = findViewById(R.id.btnMonstera);
        btnPothos = findViewById(R.id.btnPothos);
        btnSnakePlant = findViewById(R.id.btnSnakePlant);
        btnPeaceLily = findViewById(R.id.btnPeaceLily);
        btnSpiderPlant = findViewById(R.id.btnSpiderPlant);
        btnAloeVera = findViewById(R.id.btnAloeVera);

        colorGreen = findViewById(R.id.colorGreen);
        colorBlue = findViewById(R.id.colorBlue);
        colorYellow = findViewById(R.id.colorYellow);
        colorPink = findViewById(R.id.colorPink);
        colorPurple = findViewById(R.id.colorPurple);

        switchWaterReminder = findViewById(R.id.switchWaterReminder);
        btnAddPlant = findViewById(R.id.btnAddPlant);

        // Plant selection
        btnMonstera.setOnClickListener(view -> selectPlant("Monstera"));
        btnPothos.setOnClickListener(view -> selectPlant("Pothos"));
        btnSnakePlant.setOnClickListener(view -> selectPlant("Snake Plant"));
        btnPeaceLily.setOnClickListener(view -> selectPlant("Peace Lily"));
        btnSpiderPlant.setOnClickListener(view -> selectPlant("Spider Plant"));
        btnAloeVera.setOnClickListener(view -> selectPlant("Aloe Vera"));

        // Search
        etSearchPlant.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {

                filterPlants(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Color selection
        colorGreen.setOnClickListener(view -> selectColor(colorGreen));
        colorBlue.setOnClickListener(view -> selectColor(colorBlue));
        colorYellow.setOnClickListener(view -> selectColor(colorYellow));
        colorPink.setOnClickListener(view -> selectColor(colorPink));
        colorPurple.setOnClickListener(view -> selectColor(colorPurple));

        // Add Plant
        btnAddPlant.setOnClickListener(view -> {

            if (selectedPlant.isEmpty()) {
                Toast.makeText(
                        AddPlantActivity.this,
                        "Please select a plant",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Toast.makeText(
                    AddPlantActivity.this,
                    selectedPlant + " selected",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void selectPlant(String plantName) {

        selectedPlant = plantName;

        resetPlantButtons();

        if (plantName.equals("Monstera")) {
            btnMonstera.setAlpha(1.0f);
            btnMonstera.setText("✓  MONSTERA");

        } else if (plantName.equals("Pothos")) {
            btnPothos.setAlpha(1.0f);
            btnPothos.setText("✓  POTHOS");

        } else if (plantName.equals("Snake Plant")) {
            btnSnakePlant.setAlpha(1.0f);
            btnSnakePlant.setText("✓  SNAKE PLANT");

        } else if (plantName.equals("Peace Lily")) {
            btnPeaceLily.setAlpha(1.0f);
            btnPeaceLily.setText("✓  PEACE LILY");

        } else if (plantName.equals("Spider Plant")) {
            btnSpiderPlant.setAlpha(1.0f);
            btnSpiderPlant.setText("✓  SPIDER PLANT");

        } else if (plantName.equals("Aloe Vera")) {
            btnAloeVera.setAlpha(1.0f);
            btnAloeVera.setText("✓  ALOE VERA");
        }
    }

    private void resetPlantButtons() {

        btnMonstera.setAlpha(0.65f);
        btnPothos.setAlpha(0.65f);
        btnSnakePlant.setAlpha(0.65f);
        btnPeaceLily.setAlpha(0.65f);
        btnSpiderPlant.setAlpha(0.65f);
        btnAloeVera.setAlpha(0.65f);

        btnMonstera.setText("🌿  MONSTERA");
        btnPothos.setText("🌿  POTHOS");
        btnSnakePlant.setText("🌱  SNAKE PLANT");
        btnPeaceLily.setText("🌿  PEACE LILY");
        btnSpiderPlant.setText("🌱  SPIDER PLANT");
        btnAloeVera.setText("🌵  ALOE VERA");
    }

    private void filterPlants(String searchText) {

        String search = searchText.toLowerCase().trim();

        filterButton(btnMonstera, "monstera", search);
        filterButton(btnPothos, "pothos", search);
        filterButton(btnSnakePlant, "snake plant", search);
        filterButton(btnPeaceLily, "peace lily", search);
        filterButton(btnSpiderPlant, "spider plant", search);
        filterButton(btnAloeVera, "aloe vera", search);
    }

    private void filterButton(
            Button button,
            String plantName,
            String search) {

        if (plantName.contains(search)) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }
    }

    private void selectColor(View selectedColor) {

        resetColors();

        selectedColor.setAlpha(1.0f);
    }

    private void resetColors() {

        colorGreen.setAlpha(0.45f);
        colorBlue.setAlpha(0.45f);
        colorYellow.setAlpha(0.45f);
        colorPink.setAlpha(0.45f);
        colorPurple.setAlpha(0.45f);
    }
}