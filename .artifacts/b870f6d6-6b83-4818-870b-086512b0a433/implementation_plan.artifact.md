# Fix bugs in PlantDetailsActivity

The current implementation of `PlantDetailsActivity` has a few issues:
1.  It doesn't handle the case where `plant_id` is missing or invalid.
2.  It doesn't verify if the activity is still active before updating the UI from a background thread.
3.  It doesn't provide feedback if a plant is not found in the database.
4.  The `ExecutorService` is created in `onCreate` but not efficiently managed across configuration changes (though it is shut down in `onDestroy`).

## Proposed Changes

### [Component Name] app

#### [MODIFY] [PlantDetailsActivity.java](file:///E:/ANDROID/Projects/Plant APP/app/src/main/java/com/example/myapplication/PlantDetailsActivity.java)
- Add a check for `plantId == -1` and finish the activity if true.
- Add checks for `isFinishing()` or `isDestroyed()` inside the `runOnUiThread` block.
- Add a Toast message if the plant is not found.
- Clean up redundant code and improve readability.

## Verification Plan

### Manual Verification
- Launch the activity with a valid `plant_id` and verify details are shown.
- Launch the activity without a `plant_id` and verify it finishes immediately.
- Rapidly navigate away from the activity while it's loading to ensure no crashes occur.
