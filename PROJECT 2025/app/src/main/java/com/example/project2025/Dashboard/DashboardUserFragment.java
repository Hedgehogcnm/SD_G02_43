package com.example.project2025.Dashboard;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class DashboardUserFragment extends Fragment {

    private DashboardFragmentUserBinding binding;
    private SharedPreferences sharedPreferences;
    private LinearLayout feedButton;
    private WebView liveCam;
    private String PI_IP = "127.0.0.1";
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
        sharedPreferences = requireContext().getSharedPreferences("FEEDERIP", MODE_PRIVATE);
        PI_IP = sharedPreferences.getString("feeder_ip", PI_IP);

        liveCam.loadUrl("http://" + PI_IP + ":" + HTTP_PORT + LIVE_FOLDER);
        feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLevelPickerAndFeed();
            }
        });
    }
    private void sendFeedCommand(int level) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ip = null;
                    try {
                        ip = requireContext().getSharedPreferences("feeder", 0).getString("feeder_ip", null);
                    } catch (Throwable ignored) {}
                    if (ip == null || ip.trim().isEmpty()) ip = PI_IP;

                    Socket socket = new Socket(ip, FEED_PORT);
                    try {
                        socket.setSoTimeout(4000);
                    } catch (Throwable ignored) {}
                    OutputStream output = socket.getOutputStream();
                    String payload = "Feed:" + Math.max(1, Math.min(4, level));
                    output.write(payload.getBytes());
                    output.flush();

                    String response = "";
                    try {
                        InputStream in = socket.getInputStream();
                        byte[] buf = new byte[64];
                        int n = in.read(buf);
                        if (n > 0) response = new String(buf, 0, n).trim();
                    } catch (Throwable ignored) {}
                    try { socket.close(); } catch (Throwable ignored) {}

                    final String finalResponse = response;
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("ACK".equals(finalResponse)) {
                                Toast.makeText(requireContext(), "Feeding time! (Level " + level + ")", Toast.LENGTH_SHORT).show();
                                try { saveFeedHistory(level); } catch (Throwable ignored) {}
                            } else if (finalResponse != null && !finalResponse.isEmpty()) {
                                Toast.makeText(requireContext(), "Feeder: " + finalResponse, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Command sent. Waiting on feeder...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d("Manual Feed ERROR:", "Failed because of : " + e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    private void showLevelPickerAndFeed() {
        final CharSequence[] items = new CharSequence[]{"Level 1", "Level 2", "Level 3", "Level 4"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose feed level")
                .setItems(items, (dialog, which) -> {
                    int level = which + 1;
                    sendFeedCommand(level);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void saveFeedHistory(int level) {
        
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
            // Also store level so it matches scheduled entries format
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("userId", userId);
            map.put("timestamp", feedHistory.getTimestamp());
            map.put("feedType", feedHistory.getFeedType());
            map.put("level", level);
            
            // Log the feed history object
            Log.d("FeedHistory", "Feed history object - userId: " + feedHistory.getUserId() + ", feedType: " + feedHistory.getFeedType());
            
            // Add to Firestore
            db.collection("FeedHistory")
                    .add(map)
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