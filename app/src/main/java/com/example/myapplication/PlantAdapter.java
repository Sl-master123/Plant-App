package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plantList;

    public PlantAdapter(List<Plant> plantList) {
        this.plantList = plantList;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);

        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {

        Plant plant = plantList.get(position);

        holder.tvPlantName.setText(plant.getName());
        holder.tvPlantAge.setText(plant.getAge());
        holder.tvPlantDescription.setText(plant.getDescription());

        if (plant.isFavorite()) {
            holder.tvFavorite.setText("♥");
        } else {
            holder.tvFavorite.setText("♡");
        }

        // Open Plant Details when card is clicked
        holder.itemView.setOnClickListener(view -> {

            Context context = view.getContext();

            Intent intent = new Intent(context, PlantDetailsActivity.class);

            intent.putExtra("plant_name", plant.getName());
            intent.putExtra("plant_age", plant.getAge());
            intent.putExtra("plant_description", plant.getDescription());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {

        TextView tvPlantName;
        TextView tvPlantAge;
        TextView tvPlantDescription;
        TextView tvFavorite;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);

            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvPlantAge = itemView.findViewById(R.id.tvPlantAge);
            tvPlantDescription = itemView.findViewById(R.id.tvPlantDescription);
            tvFavorite = itemView.findViewById(R.id.tvFavorite);
        }
    }
}