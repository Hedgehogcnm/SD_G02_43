package com.example.project2025.Account;

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

import com.example.project2025.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project2025.Adapters.FeedHistoryAdapter;
import com.example.project2025.Models.FeedHistory;
import com.example.project2025.Specific_User.EditProfile;
import com.example.project2025.SignIn_Login_Onboarding.SignInActivity;
import com.example.project2025.databinding.AccountFragmentBinding;
import com.example.project2025.EditProfileLogics.ProfileImageHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AccountFragment extends Fragment {

    TextView username;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    private AccountFragmentBinding binding;
    private RecyclerView feedHistoryRecyclerView;
    private TextView emptyFeedHistoryText;
    private FeedHistoryAdapter feedHistoryAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AccountViewModel notificationViewModel =
                new ViewModelProvider(this).get(AccountViewModel.class);

        binding = AccountFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup gear button
        ImageView createGearBtn = binding.gearButton;
        createGearBtn.setOnClickListener(v -> {
            // Navigate to EditProfile activity instead of showing SettingProfile bottom sheet
            Intent intent = new Intent(getActivity(), EditProfile.class);
            startActivity(intent);
        });
        
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
    public void onStart() {
        super.onStart();
        //Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        sharedPreferences = getActivity().getSharedPreferences("ROLE", MODE_PRIVATE);
        String role = sharedPreferences.getString("Role", "Users");
        
        if (currentUser == null) {
            // User not logged in, redirect to sign in
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
            return;
        }
        
        // Initialize username and profile image
        DocumentReference userRef = db.collection(role).document(currentUser.getUid());
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Display username
                    username = binding.menuUsername;
                    username.setText(document.getString("name"));
                    
                    // Load and display the user's selected profile image
                    // This retrieves the profilepic field from Firebase and displays the corresponding drawable
                    String profilePic = document.getString("profilepic");
                    ProfileImageHelper.loadProfileImage(getContext(), binding.profileImage, profilePic);
                }
                else {
                    Log.d("accountMenuFragment: ", "No such document");
                }
            }
            else {
                Log.d("accountMenuFragment: ", "get failed with ", task.getException());
            }
        });
        
        // Load feed history
        loadFeedHistory();
    }

    private void loadFeedHistory() {
        if (currentUser == null) {
            Log.d("FeedHistory", "Current user is null, cannot load feed history");
            return;
        }
        
        Log.d("FeedHistory", "Loading feed history for user ID: " + currentUser.getUid());
        
        // Query feed history for current user, ordered by timestamp (newest first)
        // Note: This requires a composite index for userId + timestamp
        // Temporary workaround: Get all documents and sort in memory (not recommended for large datasets)
        db.collection("FeedHistory")
                .whereEqualTo("userId", currentUser.getUid())
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
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}