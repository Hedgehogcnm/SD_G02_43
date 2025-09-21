package com.example.project2025.ManageUser;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project2025.Adapters.FeedHistoryAdapter;
import com.example.project2025.EditProfileLogics.ProfileImageHelper;
import com.example.project2025.Models.FeedHistory;
import com.example.project2025.R;
import com.example.project2025.Specific_User.EditProfile;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUserProfile extends Fragment {

    private TextView username;
    private RecyclerView feedHistoryRecyclerView;
    private TextView emptyFeedHistoryText;
    private FeedHistoryAdapter feedHistoryAdapter;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private String uid;
    private ImageView profileImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.manage_user_profile, container, false);

        // Setup gear button
        ImageView createGearBtn = root.findViewById(R.id.gear_button);
        createGearBtn.setOnClickListener(v -> {
            // Navigate to EditProfile activity instead of showing SettingProfile bottom sheet
            Intent intent = new Intent(getActivity(), ManageUserEditUser.class);
            startActivity(intent);
        });

        //Initialize firebase
        db = FirebaseFirestore.getInstance();

        //Initialize Shared Preferences
        sharedPreferences = getActivity().getSharedPreferences("ADMINISTRATION", MODE_PRIVATE);
        uid = sharedPreferences.getString("uid", null);

        // Setup RecyclerView for feed history
        feedHistoryRecyclerView = root.findViewById(R.id.feed_history_recycler_view);
        emptyFeedHistoryText = root.findViewById(R.id.empty_feed_history_text);

        Log.d("FeedHistory", "RecyclerView reference: " + (feedHistoryRecyclerView != null ? "found" : "null"));
        Log.d("FeedHistory", "Empty text reference: " + (emptyFeedHistoryText != null ? "found" : "null"));

        // Initialize RecyclerView and adapter
        feedHistoryAdapter = new FeedHistoryAdapter();
        feedHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        feedHistoryRecyclerView.setAdapter(feedHistoryAdapter);

        // Set initial visibility
        feedHistoryRecyclerView.setVisibility(View.VISIBLE);
        emptyFeedHistoryText.setVisibility(View.GONE);
        Log.d("FeedHistory", "RecyclerView and adapter initialized");

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize username and profile image
        username = view.findViewById(R.id.menu_username);
        profileImage = view.findViewById(R.id.profile_image);
        username.setText(sharedPreferences.getString("name", null));
        String profilePic = sharedPreferences.getString("profilepic", null);
        ProfileImageHelper.loadProfileImage(getContext(), profileImage, profilePic);

        // Refresh uid just in case
        uid = sharedPreferences.getString("uid", null);
        loadFeedHistory();
    }

    private void loadFeedHistory() {
        Log.d("FeedHistory", "Loading feed history for user ID: " + uid);

        // Query feed history for current user, ordered by timestamp (newest first)
        // Note: This requires a composite index for userId + timestamp
        // Temporary workaround: Get all documents and sort in memory (not recommended for large datasets)
        db.collection("FeedHistory")
                .whereEqualTo("userId", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<FeedHistory> feedHistoryList = new ArrayList<>();
                        Log.d("FeedHistory", "Query successful, document count: " + task.getResult().size());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("FeedHistory", "Document ID: " + document.getId() + ", Data: " + document.getData());
                            FeedHistory feedHistory = document.toObject(FeedHistory.class);
                            feedHistoryList.add(feedHistory);
                        }

                        // Sort by timestamp in descending order (newest first) - temporary workaround
                        feedHistoryList.sort((a, b) -> {
                            if (a.getTimestamp() == null || b.getTimestamp() == null) return 0;
                            return b.getTimestamp().compareTo(a.getTimestamp());
                        });

                        // Update UI based on results
                        if (feedHistoryList.isEmpty()) {

                            feedHistoryRecyclerView.setVisibility(View.GONE);
                            emptyFeedHistoryText.setVisibility(View.VISIBLE);
                        } else {
                            Log.d("FeedHistory", "Found " + feedHistoryList.size() + " feed history items");
                            feedHistoryRecyclerView.setVisibility(View.VISIBLE);
                            emptyFeedHistoryText.setVisibility(View.GONE);
                            feedHistoryAdapter.setFeedHistoryList(feedHistoryList);
                        }
                    } else {
                        Log.e("FeedHistory", "Error getting feed history", task.getException());
                        // Check if it's a missing index error
                        if (task.getException() != null &&
                                task.getException().getMessage() != null &&
                                task.getException().getMessage().contains("index")) {
                            Log.e("FeedHistory", "Missing Firestore composite index! Check Firebase Console for index creation link.");
                        }
                        feedHistoryRecyclerView.setVisibility(View.GONE);
                        emptyFeedHistoryText.setVisibility(View.VISIBLE);
                    }
                });
    }
}