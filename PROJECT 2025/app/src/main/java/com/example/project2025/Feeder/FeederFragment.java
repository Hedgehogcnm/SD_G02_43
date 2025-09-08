package com.example.project2025.Feeder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project2025.Models.ScheduleData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.project2025.R;

import java.util.ArrayList;
import java.util.List;


public class FeederFragment extends Fragment implements ScheduleBottomSheet.ScheduleDataListener {

    private LinearLayout scheduleContainer;
    private List<ScheduleData> scheduleList;
    private int nextScheduleIndex = 1; // Track which schedule slot to use next
    private ListenerRegistration schedulesListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FeederViewModel dashboardViewModel =
                new ViewModelProvider(this).get(FeederViewModel.class);

        View root = inflater.inflate(R.layout.feeder_fragment, container, false);

        // Initialize schedule list and container
        scheduleList = new ArrayList<>();
        scheduleContainer = root.findViewById(R.id.scheduleContainer);

        // Set feeder IP (persisted for alarms and manual trigger reuse)
        requireContext().getSharedPreferences("feeder", 0)
                .edit()
                .putString("feeder_ip", "192.168.180.158")
                .apply();

        ImageView createAlarmBtn = root.findViewById(R.id.alarm);
        createAlarmBtn.setOnClickListener(v -> {
            ScheduleBottomSheet bottomSheet = new ScheduleBottomSheet();
            bottomSheet.setScheduleDataListener(this); // Set the listener
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });


        // Real-time schedules listener
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            schedulesListener = db.collection("Users")
                    .document(user.getUid())
                    .collection("schedules")
                    .addSnapshotListener((snap, err) -> {
                        if (err != null) {
                            android.util.Log.e("FeederFragment", "Firebase listener error: " + err.getMessage());
                            return;
                        }
                        if (snap == null) {
                            android.util.Log.d("FeederFragment", "Firebase snapshot is null");
                            return;
                        }
                        
                        android.util.Log.d("FeederFragment", "Firebase listener triggered - " + snap.getDocuments().size() + " schedules found");
                        
                        // Clear current UI and state, then repopulate from snapshot
                        clearScheduleDisplay();
                        scheduleList.clear();
                        nextScheduleIndex = 1;

                        for (DocumentSnapshot d : snap.getDocuments()) {
                            android.util.Log.d("FeederFragment", "Processing schedule document: " + d.getId());
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
                            updateScheduleDisplay(sd);

                            if (Boolean.TRUE.equals(enabled)) {
                                int base2 = (int) (System.currentTimeMillis() & 0xFFFF);
                                ScheduleHelper.scheduleWeekly(requireContext(), sd.getTime(), sd.getSelectedDays(), base2);
                            }
                        }
                    });
        }

        return root;
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
        android.util.Log.d("FeederFragment", "New schedule received: " + scheduleData.getTitle());
        
        // Schedule alarms on user device for selected days/time
        int base = (int) (System.currentTimeMillis() & 0xFFFF);
        ScheduleHelper.scheduleWeekly(requireContext(), scheduleData.getTime(), scheduleData.getSelectedDays(), base);

        // Save schedule to Firestore for persistence
        // The Firebase listener will automatically update the UI when this is saved
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
              .add(doc)
              .addOnSuccessListener(documentReference -> {
                  android.util.Log.d("FeederFragment", "Schedule saved to Firebase with ID: " + documentReference.getId());
              })
              .addOnFailureListener(e -> {
                  android.util.Log.e("FeederFragment", "Failed to save schedule to Firebase: " + e.getMessage());
              });
        } else {
            android.util.Log.e("FeederFragment", "User is null, cannot save schedule to Firebase");
        }
    }
    
    private void updateScheduleDisplay(ScheduleData scheduleData) {
        android.util.Log.d("FeederFragment", "updateScheduleDisplay called for: " + scheduleData.getTitle());
        
        // Find the next available schedule slot (1-4)
        if (nextScheduleIndex > 4) {
            android.util.Log.w("FeederFragment", "All schedule slots are full (4/4)");
            return;
        }
        
        // Get the TextViews for the current schedule slot
        String scheduleId = String.valueOf(nextScheduleIndex);
        
        TextView titleView = getView().findViewById(getResources().getIdentifier(
                "schedule_title" + scheduleId, "id", getContext().getPackageName()));
        TextView timeView = getView().findViewById(getResources().getIdentifier(
                "schedule_time" + scheduleId, "id", getContext().getPackageName()));
        TextView daysView = getView().findViewById(getResources().getIdentifier(
                "schedule_days" + scheduleId, "id", getContext().getPackageName()));
        TextView feedLevelView = getView().findViewById(getResources().getIdentifier(
                "schedule_feed_level" + scheduleId, "id", getContext().getPackageName()));
        
        // Get the card view using the card ID
        View cardView = getView().findViewById(getResources().getIdentifier(
                "schedule_card" + scheduleId, "id", getContext().getPackageName()));
        
        if (titleView != null && timeView != null && daysView != null && feedLevelView != null && cardView != null) {
            android.util.Log.d("FeederFragment", "All UI elements found for slot " + scheduleId + ", updating display");
            
            // Update the text with the selected data
            titleView.setText(scheduleData.getTitle());
            timeView.setText("Time: " + formatTo12Hour(scheduleData.getTime()));
            daysView.setText("Days: " + scheduleData.getFormattedDays());
            feedLevelView.setText("Feed Level: " + scheduleData.getFeedLevel());
            
            // Make the card visible
            cardView.setVisibility(View.VISIBLE);
            
            android.util.Log.d("FeederFragment", "Schedule " + scheduleData.getTitle() + " displayed in slot " + scheduleId);
            
            // Move to next slot
            nextScheduleIndex++;
        } else {
            android.util.Log.e("FeederFragment", "Some UI elements not found for slot " + scheduleId + 
                " - titleView: " + (titleView != null) + 
                ", timeView: " + (timeView != null) + 
                ", daysView: " + (daysView != null) + 
                ", feedLevelView: " + (feedLevelView != null) + 
                ", cardView: " + (cardView != null));
        }
    }

    // Hide all schedule cards and reset labels
    private void clearScheduleDisplay() {
        int[] cardIds = new int[]{
                getResources().getIdentifier("schedule_card1", "id", getContext().getPackageName()),
                getResources().getIdentifier("schedule_card2", "id", getContext().getPackageName()),
                getResources().getIdentifier("schedule_card3", "id", getContext().getPackageName()),
                getResources().getIdentifier("schedule_card4", "id", getContext().getPackageName())
        };
        for (int i = 0; i < cardIds.length; i++) {
            View card = getView().findViewById(cardIds[i]);
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
}