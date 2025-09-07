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
import com.example.project2025.Adapters.FeedHistoryAdapter;
import com.example.project2025.Models.FeedHistory;

import java.util.ArrayList;
import java.util.List;


public class FeederFragment extends Fragment implements ScheduleBottomSheet.ScheduleDataListener {

    private LinearLayout scheduleContainer;
    private List<ScheduleData> scheduleList;
    private int nextScheduleIndex = 1; // Track which schedule slot to use next
    private ListenerRegistration schedulesListener;
    private FeedHistoryAdapter feedHistoryAdapter;

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

        // Setup feeding history recycler within the card
        androidx.recyclerview.widget.RecyclerView historyRecycler = root.findViewById(R.id.feeder_history_recycler);
        TextView emptyHistoryText = root.findViewById(R.id.empty_history_text);
        feedHistoryAdapter = new FeedHistoryAdapter();
        historyRecycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        historyRecycler.setAdapter(feedHistoryAdapter);

        // Load history from wherever you populate it (if you have Firestore or local list)
        // For now, show empty text if adapter has no items
        if (feedHistoryAdapter.getItemCount() == 0) {
            emptyHistoryText.setVisibility(View.VISIBLE);
        } else {
            emptyHistoryText.setVisibility(View.GONE);
        }

        // Real-time schedules listener
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            schedulesListener = db.collection("Users")
                    .document(user.getUid())
                    .collection("schedules")
                    .orderBy("createdAt", Query.Direction.ASCENDING)
                    .addSnapshotListener((snap, err) -> {
                        if (err != null || snap == null) return;
                        // Clear current UI and state, then repopulate from snapshot
                        clearScheduleDisplay();
                        scheduleList.clear();
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
        // Add the new schedule to our list
        scheduleList.add(scheduleData);
        
        // Update the UI with the new schedule
        updateScheduleDisplay(scheduleData);

        // Schedule alarms on user device for selected days/time
        int base = (int) (System.currentTimeMillis() & 0xFFFF);
        ScheduleHelper.scheduleWeekly(requireContext(), scheduleData.getTime(), scheduleData.getSelectedDays(), base);

        // Save schedule to Firestore for persistence
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
    
    private void updateScheduleDisplay(ScheduleData scheduleData) {
        // Find the next available schedule slot (1-4)
        if (nextScheduleIndex > 4) {
            // All slots are full, you might want to show a message or replace the oldest
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
            // Update the text with the selected data
            titleView.setText(scheduleData.getTitle());
            timeView.setText("Time: " + formatTo12Hour(scheduleData.getTime()));
            daysView.setText("Days: " + scheduleData.getFormattedDays());
            feedLevelView.setText("Feed Level: " + scheduleData.getFeedLevel());
            
            // Make the card visible
            cardView.setVisibility(View.VISIBLE);
            
            // Move to next slot
            nextScheduleIndex++;
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