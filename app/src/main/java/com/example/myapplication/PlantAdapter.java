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

/**
 * ============================================================================
 * PlantAdapter.java
 * ============================================================================
 * Role: RecyclerView Adapter & ViewHolder Pattern for Plant Cards.
 * 
 * Key Concepts for Project Demonstration:
 * 1. RecyclerView Architecture:
 *    - Memory Efficient: Re-uses (recycles) view objects as the user scrolls,
 *      preventing out-of-memory errors even with hundreds of plants.
 *    - onCreateViewHolder(): Inflates item_plant.xml layout.
 *    - onBindViewHolder(): Binds data from Plant model to UI elements.
 * 2. View Recycling & Image State Management:
 *    To prevent recycled views showing wrong images (a notorious Android bug),
 *    the bitmap is cleared and reset explicitly before loading or falling back
 *    to category emojis.
 * 3. Asynchronous Database Updates:
 *    Uses ExecutorService to perform DB updates (favorite toggle, deletion)
 *    off the UI thread, then synchronizes UI on runOnUiThread().
 * 4. Dynamic Watering Countdown:
 *    Calculates whether watering is "Overdue ⚠️", "Today 💧", "Tomorrow 💧",
 *    or "In N days" with matching badge colors.
 * ============================================================================
 */
public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plantList;
    private PlantDao plantDao;
    private ExecutorService executorService;

    /**
     * Constructor accepts plant data list and Room DAO reference.
     */
    public PlantAdapter(List<Plant> plantList, PlantDao plantDao) {
        this.plantList = plantList;
        this.plantDao = plantDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate individual plant card layout
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);

        // 1. Bind text details
        holder.tvPlantName.setText(plant.getName());
        holder.tvPlantCategory.setText(plant.getCategory() != null ? plant.getCategory() : "Indoor");
        holder.tvPlantDescription.setText(
                (plant.getDescription() != null && !plant.getDescription().isEmpty())
                        ? plant.getDescription()
                        : "No care notes"
        );

        // 2. Image Loading & Recycling Safety:
        // Always clear stale bitmap first to avoid recycled view artifact glitches
        if (plant.getImageUri() != null && !plant.getImageUri().isEmpty()
                && new File(plant.getImageUri()).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(plant.getImageUri());
            if (bitmap != null) {
                holder.ivPlantImage.setImageBitmap(bitmap);
                holder.ivPlantImage.setVisibility(View.VISIBLE);
                holder.tvPlantEmojiPlaceholder.setVisibility(View.GONE);
            } else {
                holder.ivPlantImage.setImageBitmap(null);
                showDefaultEmoji(holder, plant);
            }
        } else {
            holder.ivPlantImage.setImageBitmap(null);
            showDefaultEmoji(holder, plant);
        }

        // 3. Bind dynamic watering countdown badge
        bindWateringCountdown(holder, plant);

        // 4. Favorite Toggle (Heart Icon)
        holder.tvFavorite.setText(plant.isFavorite() ? "♥" : "♡");
        holder.tvFavorite.setOnClickListener(v -> {
            plant.setFavorite(!plant.isFavorite());
            // Update database on background worker thread
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

        // 5. Delete Action with Confirmation Dialog
        holder.btnDeletePlant.setOnClickListener(v -> {
            Context context = v.getContext();
            new AlertDialog.Builder(context)
                    .setTitle("Remove Plant")
                    .setMessage("Remove \"" + plant.getName() + "\" from your garden?")
                    .setPositiveButton("Delete 🗑", (dialog, which) -> {
                        executorService.execute(() -> {
                            // Remove from Room DB and cancel system alarms
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

        // 6. Tap Card to View Details
        holder.itemView.setOnClickListener(view -> {
            Context context = view.getContext();
            Intent intent = new Intent(context, PlantDetailsActivity.class);
            intent.putExtra("plant_id", plant.getId());
            context.startActivity(intent);
        });
    }

    /**
     * Sets category-specific emoji placeholder when no custom photo is attached.
     */
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

    /**
     * Calculates remaining days/hours until next watering and styles countdown badge.
     */
    private void bindWateringCountdown(PlantViewHolder holder, Plant plant) {
        TextView countdownView = holder.tvWateringCountdownCard;
        if (countdownView == null) return;

        if (!plant.isReminderEnabled()) {
            countdownView.setVisibility(View.GONE);
            return;
        }

        long nextTime = WateringReminderReceiver.calculateNextTriggerTime(plant);
        if (nextTime <= 0) {
            // Fallback calculation using lastWateredTimestamp + frequency
            nextTime = plant.getLastWateredTimestamp()
                    + (plant.getWateringFrequency() * 24L * 60 * 60 * 1000);
        }

        long timeLeft = nextTime - System.currentTimeMillis();
        long daysLeft = timeLeft / (24L * 60 * 60 * 1000);

        countdownView.setVisibility(View.VISIBLE);

        if (timeLeft < 0) {
            // Overdue state
            countdownView.setText("⚠️ Overdue – water now!");
            countdownView.setBackgroundColor(0xFFFFEBEE);
            countdownView.setTextColor(0xFFC62828);
        } else if (daysLeft == 0) {
            // Today
            countdownView.setText("💧 Water today!");
            countdownView.setBackgroundColor(0xFFDDECCF);
            countdownView.setTextColor(0xFF2E7D32);
        } else if (daysLeft == 1) {
            // Tomorrow
            countdownView.setText("💧 Water tomorrow");
            countdownView.setBackgroundColor(0xFFDDECCF);
            countdownView.setTextColor(0xFF2E7D32);
        } else {
            // Future days
            countdownView.setText("💧 In " + daysLeft + " days");
            countdownView.setBackgroundColor(0xFFF0F6EE);
            countdownView.setTextColor(0xFF4CAF50);
        }
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    /**
     * ViewHolder holds references to the subviews of each plant item row,
     * avoiding repeated and expensive findViewById calls during scrolling.
     */
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
