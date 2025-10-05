package com.example.project2025.Feeder;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog;

import com.example.project2025.Models.ScheduleData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.project2025.R;
import com.example.project2025.activity_notification;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeederFragment extends Fragment implements ScheduleBottomSheet.ScheduleDataListener {

    private LinearLayout scheduleContainer;
    private List<ScheduleData> scheduleList;
    private List<String> scheduleDocIds;
    private int nextScheduleIndex = 1; // Track which schedule slot to use next
    private ListenerRegistration schedulesListener;
    private FirebaseFirestore db;
    private String PI_IP = "127.0.0.1";
    private ImageView img;
    private TextView percentage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FeederViewModel dashboardViewModel =
                new ViewModelProvider(this).get(FeederViewModel.class);

        View root = inflater.inflate(R.layout.feeder_fragment, container, false);

        //for food level purpose
        img = root.findViewById(R.id.device_pic);
        percentage = root.findViewById(R.id.foodPercentage);

        // click notification icon intent to activity_notification
        ImageView notificationBtn = root.findViewById(R.id.notification);
        notificationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), activity_notification.class);
            startActivity(intent);
        });

        // Initialize schedule list and container
        scheduleList = new ArrayList<>();
        scheduleDocIds = new ArrayList<>();
        scheduleContainer = root.findViewById(R.id.scheduleContainer);
        db = FirebaseFirestore.getInstance();

        // Initialize PI_IP
        PI_IP = requireContext().getSharedPreferences("FEEDERIP", MODE_PRIVATE).getString("feeder_ip", PI_IP);

        ImageView createAlarmBtn = root.findViewById(R.id.alarm);
        createAlarmBtn.setOnClickListener(v -> {
            if (scheduleList != null && scheduleList.size() >= 4) {
                Toast.makeText(requireContext(), "You can only create 4 schedules", Toast.LENGTH_SHORT).show();
                return;
            }
            ScheduleBottomSheet bottomSheet = new ScheduleBottomSheet();
            bottomSheet.setScheduleDataListener(this);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });

        // Real-time schedules listener
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            schedulesListener = db.collection("Users")
                    .document(user.getUid())
                    .collection("schedules")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .addSnapshotListener((snap, err) -> {
                        if (err != null) {
                            Log.e("FeederFragment", "Firebase listener error: " + err.getMessage());
                            return;
                        }
                        if (snap == null) {
                            Log.d("FeederFragment", "Firebase snapshot is null");
                            return;
                        }

                        Log.d("FeederFragment", "Firebase listener triggered - " + snap.getDocuments().size() + " schedules found");

                        clearScheduleDisplay();
                        scheduleList.clear();
                        scheduleDocIds.clear();
                        nextScheduleIndex = 1;

                        for (DocumentSnapshot d : snap.getDocuments()) {
                            String title = d.getString("title");
                            String time = d.getString("time");
                            java.util.List<String> days = (java.util.List<String>) d.get("days");
                            Long level = d.getLong("level");
                            Boolean enabled = d.getBoolean("enabled");

                            ScheduleData sd = new ScheduleData(
                                    title != null ? title : "",
                                    time != null ? time : "07:00",
                                    days != null ? days : new java.util.ArrayList<>(),
                                    level != null ? level.intValue() : 1
                            );

                            scheduleList.add(sd);
                            scheduleDocIds.add(d.getId());
                            updateScheduleDisplay(sd, d.getId());

                            if (Boolean.TRUE.equals(enabled)) {
                                String docId = d.getId();
                                int base2 = (docId != null ? docId.hashCode() : 0) & 0x7FFF;
                                ScheduleHelper.cancelWeekly(requireContext(), base2);
                                ScheduleHelper.scheduleWeekly(requireContext(), sd.getTime(), sd.getSelectedDays(), base2, sd.getFeedLevel(), sd.getTitle(), docId);
                            }
                        }
                    });
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        showFoodLevel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (schedulesListener != null) {
            schedulesListener.remove();
            schedulesListener = null;
        }
    }

    @Override
    public void onScheduleDataReceived(ScheduleData scheduleData) {
        if (scheduleList != null && scheduleList.size() >= 4) {
            Toast.makeText(requireContext(), "Maximum of 4 schedules reached", Toast.LENGTH_SHORT).show();
            return;
        }
        if (scheduleData == null || scheduleData.getSelectedDays() == null || scheduleData.getSelectedDays().isEmpty()
                || scheduleData.getFeedLevel() <= 0 || scheduleData.getTime() == null || scheduleData.getTime().trim().isEmpty()) {
            Toast.makeText(requireContext(), "Enter all details for your schedule", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            java.util.Map<String, Object> doc = new java.util.HashMap<>();
            doc.put("title", scheduleData.getTitle());
            doc.put("time", scheduleData.getTime());
            doc.put("days", scheduleData.getSelectedDays());
            doc.put("level", scheduleData.getFeedLevel());
            doc.put("enabled", true);
            doc.put("createdAt", FieldValue.serverTimestamp());

            db.collection("Users")
                    .document(user.getUid())
                    .collection("schedules")
                    .add(doc);
        }
    }

    private void updateScheduleDisplay(ScheduleData scheduleData, String documentId) {
        if (nextScheduleIndex > 4) return;

        String scheduleId = String.valueOf(nextScheduleIndex);

        TextView titleView = getView().findViewById(getResources().getIdentifier(
                "schedule_title" + scheduleId, "id", getContext().getPackageName()));
        TextView timeView = getView().findViewById(getResources().getIdentifier(
                "schedule_time" + scheduleId, "id", getContext().getPackageName()));
        TextView daysView = getView().findViewById(getResources().getIdentifier(
                "schedule_days" + scheduleId, "id", getContext().getPackageName()));
        TextView feedLevelView = getView().findViewById(getResources().getIdentifier(
                "schedule_feed_level" + scheduleId, "id", getContext().getPackageName()));

        View cardView = getView().findViewById(getResources().getIdentifier(
                "schedule_card" + scheduleId, "id", getContext().getPackageName()));

        if (titleView != null && timeView != null && daysView != null && feedLevelView != null && cardView != null) {
            titleView.setText(scheduleData.getTitle());
            timeView.setText("Time: " + formatTo12Hour(scheduleData.getTime()));
            daysView.setText("Days: " + scheduleData.getFormattedDays());
            feedLevelView.setText("Feed Level: " + scheduleData.getFeedLevel());
            cardView.setVisibility(View.VISIBLE);

            titleView.setOnClickListener(v -> showChangeTitleDialog(titleView, scheduleData, documentId));

            // delete schedule
            cardView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Schedule")
                        .setMessage("Are you sure you want to delete this schedule?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                            if (u != null) {
                                FirebaseFirestore.getInstance()
                                        .collection("Users")
                                        .document(u.getUid())
                                        .collection("schedules")
                                        .document(documentId)
                                        .delete()
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(requireContext(), "Schedule Deleted", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            });

            nextScheduleIndex++;
        }
    }


    private void showChangeTitleDialog(TextView titleView, ScheduleData scheduleData, String documentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Set your schedule title");
        builder.setMessage("\nPlease enter a new title");

        final EditText input = new EditText(requireContext());
        input.setPadding(70, 1, 50, 30);
        input.setHint("\nEnter new title");
        input.setSingleLine(true);

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (newTitle.isEmpty()) {
                Toast.makeText(requireContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            if (u != null) {
                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(u.getUid())
                        .collection("schedules")
                        .document(documentId)
                        .update("title", newTitle)
                        .addOnSuccessListener(unused -> titleView.setText(newTitle))
                        .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update title", Toast.LENGTH_SHORT).show());
            }
            Toast.makeText(requireContext(), "Title has been set", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void clearScheduleDisplay() {
        int[] cardIds = new int[]{
                getResources().getIdentifier("schedule_card1", "id", getContext().getPackageName()),
                getResources().getIdentifier("schedule_card2", "id", getContext().getPackageName()),
                getResources().getIdentifier("schedule_card3", "id", getContext().getPackageName()),
                getResources().getIdentifier("schedule_card4", "id", getContext().getPackageName())
        };
        for (int id : cardIds) {
            View card = getView().findViewById(id);
            if (card != null) card.setVisibility(View.GONE);
        }
    }

    private String formatTo12Hour(String time24) {
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault());
            java.util.Date d = in.parse(time24);
            return out.format(d);
        } catch (Exception e) {
            return time24;
        }
    }

    void showFoodLevel(){
        db.collection("Feeder").whereEqualTo("ip_address", PI_IP).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    double foodLevel = document.getDouble("food_level");
                    checkFoodLevel(foodLevel);
                    displayPercentage(foodLevel);
                    Log.d("Feeder Fragment:", "Food Level: " + foodLevel + "%");
                }
            }
        });
    }

    void checkFoodLevel(double foodLevel){
        if(foodLevel <= 3){
            img.setImageResource(R.drawable.food_level_100);
        }
        else if(foodLevel <= 6){
            img.setImageResource(R.drawable.food_level_75);
        }
        else if(foodLevel <= 9){
            img.setImageResource(R.drawable.food_level_50);
        }
        else if(foodLevel <= 12){
            img.setImageResource(R.drawable.food_level_25);
        }
        else if(foodLevel <= 15){
            img.setImageResource(R.drawable.food_level_0);
        }
    }

    void displayPercentage(double foodLevel){
        int result = (int) (125 - (foodLevel / 12 * 100));
        if(foodLevel > 15){
            percentage.setText("0%");
        }
        else if(foodLevel < 3){
            percentage.setText("100%");
        }
        else if(foodLevel >= 3 && foodLevel <= 15){
            percentage.setText(result + "%");
        }
    }
}