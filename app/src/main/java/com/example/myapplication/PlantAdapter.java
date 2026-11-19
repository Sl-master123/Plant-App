/** Connects plant records to rows in the scrolling plant list. */
package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plantList;
    private PlantDao plantDao;
    private ExecutorService executorService;

    public PlantAdapter(List<Plant> plantList, PlantDao plantDao) {
        this.plantList = plantList;
        this.plantDao = plantDao;
        this.executorService = Executors.newSingleThreadExecutor();
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
        holder.tvPlantCategory.setText(plant.getCategory() != null ? plant.getCategory() : "Indoor");
        holder.tvPlantDescription.setText(
                (plant.getDescription() != null && !plant.getDescription().isEmpty())
                        ? plant.getDescription()
                        : "No care notes"
        );

        // ── Critical Bug Fix: always reset image state before binding ──
        if (plant.getImageUri() != null && !plant.getImageUri().isEmpty()
                && new File(plant.getImageUri()).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(plant.getImageUri());
            if (bitmap != null) {
                holder.ivPlantImage.setImageBitmap(bitmap);
                holder.ivPlantImage.setVisibility(View.VISIBLE);
                holder.tvPlantEmojiPlaceholder.setVisibility(View.GONE);
            } else {
                holder.ivPlantImage.setImageBitmap(null);   // clear stale bitmap
                showDefaultEmoji(holder, plant);
            }
        } else {
            holder.ivPlantImage.setImageBitmap(null);       // clear stale bitmap
            showDefaultEmoji(holder, plant);
        }

        // Watering countdown
        bindWateringCountdown(holder, plant);

        // Favorite heart
        holder.tvFavorite.setText(plant.isFavorite() ? "♥" : "♡");

        holder.tvFavorite.setOnClickListener(v -> {
            plant.setFavorite(!plant.isFavorite());
            executorService.execute(() -> {
                plantDao.updatePlant(plant);
                if (v.getContext() instanceof MainActivity) {
                    ((MainActivity) v.getContext()).runOnUiThread(() -> {
                        notifyItemChanged(holder.getAdapterPosition());
                        String msg = plant.isFavorite() ? "Added to favorites ♥" : "Removed from favorites";
                        Toast.makeText(v.getContext(), msg, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        // Delete button
        holder.btnDeletePlant.setOnClickListener(v -> {
            Context context = v.getContext();
            new AlertDialog.Builder(context)
                    .setTitle("Remove Plant")
                    .setMessage("Remove \"" + plant.getName() + "\" from your garden?")
                    .setPositiveButton("Delete 🗑", (dialog, which) -> {
                        executorService.execute(() -> {
                            plantDao.deletePlant(plant);
                            WateringReminderReceiver.cancelPlantAlarm(context, plant.getId());
                            if (context instanceof MainActivity) {
                                ((MainActivity) context).runOnUiThread(() -> {
                                    int adapterPos = holder.getAdapterPosition();
                                    if (adapterPos != RecyclerView.NO_POSITION && adapterPos < plantList.size()) {
                                        plantList.remove(adapterPos);
                                        notifyItemRemoved(adapterPos);
                                        notifyItemRangeChanged(adapterPos, plantList.size());
                                    }
                                    Toast.makeText(context, plant.getName() + " removed", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Open details on card tap
        holder.itemView.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(context, PlantDetailsActivity.class);
            intent.putExtra("plant_id", plant.getId());
            context.startActivity(intent);
        });
    }

    private void showDefaultEmoji(PlantViewHolder holder, Plant plant) {
        holder.ivPlantImage.setVisibility(View.GONE);
        holder.tvPlantEmojiPlaceholder.setVisibility(View.VISIBLE);
        String category = plant.getCategory() != null ? plant.getCategory() : "";
        switch (category) {
            case "Vegetables": holder.tvPlantEmojiPlaceholder.setText("🥦"); break;
            case "Fruits":     holder.tvPlantEmojiPlaceholder.setText("🍓"); break;
            case "Herbs":      holder.tvPlantEmojiPlaceholder.setText("🌿"); break;
            case "Succulents": holder.tvPlantEmojiPlaceholder.setText("🌵"); break;
            case "Flowers":    holder.tvPlantEmojiPlaceholder.setText("🌸"); break;
            default:           holder.tvPlantEmojiPlaceholder.setText("🌿"); break;
        }
    }

    private void bindWateringCountdown(PlantViewHolder holder, Plant plant) {
        TextView countdownView = holder.tvWateringCountdownCard;
        if (countdownView == null) return;

        if (!plant.isReminderEnabled()) {
            countdownView.setVisibility(View.GONE);
            return;
        }

        long nextTime = WateringReminderReceiver.calculateNextTriggerTime(plant);
        if (nextTime <= 0) {
            // Fallback: use lastWatered + frequency
            nextTime = plant.getLastWateredTimestamp()
                    + (plant.getWateringFrequency() * 24L * 60 * 60 * 1000);
        }

        long timeLeft = nextTime - System.currentTimeMillis();
        long daysLeft = timeLeft / (24L * 60 * 60 * 1000);
        long hoursLeft = timeLeft / (60L * 60 * 1000);

        countdownView.setVisibility(View.VISIBLE);

        if (timeLeft < 0) {
            countdownView.setText("⚠️ Overdue – water now!");
            countdownView.setBackgroundResource(0);
            countdownView.setBackgroundColor(0xFFFFEBEE);
            countdownView.setTextColor(0xFFC62828);
        } else if (daysLeft == 0) {
            countdownView.setText("💧 Water today!");
            countdownView.setBackgroundColor(0xFFDDECCF);
            countdownView.setTextColor(0xFF2E7D32);
        } else if (daysLeft == 1) {
            countdownView.setText("💧 Water tomorrow");
            countdownView.setBackgroundColor(0xFFDDECCF);
            countdownView.setTextColor(0xFF2E7D32);
        } else {
            countdownView.setText("💧 In " + daysLeft + " days");
            countdownView.setBackgroundColor(0xFFF0F6EE);
            countdownView.setTextColor(0xFF4CAF50);
        }
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlantImage;
        TextView tvPlantEmojiPlaceholder;
        TextView tvPlantName;
        TextView tvPlantCategory;
        TextView tvPlantAge;
        TextView tvPlantDescription;
        TextView tvWateringCountdownCard;
        TextView tvFavorite;
        TextView btnDeletePlant;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlantImage = itemView.findViewById(R.id.ivPlantImage);
            tvPlantEmojiPlaceholder = itemView.findViewById(R.id.tvPlantEmojiPlaceholder);
            tvPlantName = itemView.findViewById(R.id.tvPlantName);
            tvPlantCategory = itemView.findViewById(R.id.tvPlantCategory);
            tvPlantAge = itemView.findViewById(R.id.tvPlantAge);
            tvPlantDescription = itemView.findViewById(R.id.tvPlantDescription);
            tvWateringCountdownCard = itemView.findViewById(R.id.tvWateringCountdownCard);
            tvFavorite = itemView.findViewById(R.id.tvFavorite);
            btnDeletePlant = itemView.findViewById(R.id.btnDeletePlant);
        }
    }
}