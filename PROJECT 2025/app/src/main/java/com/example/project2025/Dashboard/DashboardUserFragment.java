package com.example.project2025.Dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.project2025.Models.FeedHistory;
import com.example.project2025.R;
import com.example.project2025.databinding.DashboardFragmentUserBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


public class DashboardUserFragment extends Fragment {

    private DashboardFragmentUserBinding binding;
    private LinearLayout feedButton;
    private WebView liveCam;
    private static final String PI_IP = "192.168.180.158";
    private static final int FEED_PORT = 12345;
    private static final int HTTP_PORT = 8889;
    private static final String LIVE_FOLDER = "/cam1";
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardUserViewModel homeViewModel =
                new ViewModelProvider(this).get(DashboardUserViewModel.class);

        View root = inflater.inflate(R.layout.dashboard_fragment_user, container, false);

        liveCam = root.findViewById(R.id.ipCamera);
        feedButton = root.findViewById(R.id.feedButton);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        WebSettings webSettings = liveCam.getSettings();
        webSettings.setJavaScriptEnabled(true);
        liveCam.setWebViewClient(new WebViewClient());

        liveCam.loadUrl("http://" + PI_IP + ":" + HTTP_PORT + LIVE_FOLDER);
        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFeedCommand();
            }
        });
    }

    private void sendFeedCommand() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(PI_IP, FEED_PORT);
                    OutputStream output = socket.getOutputStream();
                    output.write("Feed".getBytes());
                    output.flush();
                    socket.close();

                    // Save feed history to Firebase
                    saveFeedHistory();

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Feeding time!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Failed because of : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("Manual Feed ERROR:", "Failed because of : " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }
    
    private void saveFeedHistory() {
        
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("FeedHistory", "Saving feed history for user ID: " + userId);
            
            // Create a new feed history entry
            FeedHistory feedHistory = new FeedHistory(
                    userId,
                    Timestamp.now(),
                    "Manual"
            );
            
            // Log the feed history object
            Log.d("FeedHistory", "Feed history object - userId: " + feedHistory.getUserId() + ", feedType: " + feedHistory.getFeedType());
            
            // Add to Firestore
            db.collection("FeedHistory")
                    .add(feedHistory)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("FeedHistory", "Feed history saved with ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FeedHistory", "Error saving feed history", e);
                    });
        } else {
            Log.e("FeedHistory", "Cannot save feed history: user is null");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}