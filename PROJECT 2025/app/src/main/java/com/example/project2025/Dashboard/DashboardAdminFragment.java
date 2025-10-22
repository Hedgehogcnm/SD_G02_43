package com.example.project2025.Dashboard;

import com.example.project2025.Utils.LineChartView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.project2025.R;
import com.example.project2025.ManageUser.UserListFragment;
import com.example.project2025.Utils.LineChartView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * DashboardFragment - Admin dashboard interface
 * Currently empty as requested by user
 * This fragment will be used for admin-specific features in the future
 */
public class DashboardAdminFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView userCount;
    LineChartView userGrowthChart;
    EditText ip_address ;
    Button manualFeed;
    private static final int PORT = 12345;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Currently shows "Admin Dashboard" and "(Empty for now)" text
        View root = inflater.inflate(R.layout.dashboard_fragment_admin, container, false);

        //ip_address = root.findViewById(R.id.ip_address);
        //manualFeed = root.findViewById(R.id.manual_feed);
        userCount = root.findViewById(R.id.user_count);
        userGrowthChart = root.findViewById(R.id.userGrowthChart);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserCount();
        loadUserGrowthChart();

        /*
        manualFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip_address_text = ip_address.getText().toString();
                sendFeedCommand(ip_address_text);
            }
        });
         */
    }
    private void getUserCount(){
        Query query = db.collection("Users");
        AggregateQuery countQuery = query.count();
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Count fetched successfully
                    AggregateQuerySnapshot snapshot = task.getResult();
                    userCount.setText(String.valueOf(snapshot.getCount()));
                } else {
                    // Error occurred while fetching count
                    Exception exception = task.getException();
                    Log.d("TAG", "Error getting count", exception);
                }
            }
        });
    }

    private void loadUserGrowthChart(){
        // Aggregate current calendar year Jan -> Dec based on Users.createdAt (millis)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);

        // Prepare month labels and buckets for Jan..Dec
        // Fixed January to December labels -> first letter (J, F, M, ...)
        String[] fixedMonths = new java.text.DateFormatSymbols().getShortMonths();
        java.util.List<Integer> counts = new java.util.ArrayList<>();
        java.util.List<String> labels = new java.util.ArrayList<>();

        java.util.List<long[]> ranges = new java.util.ArrayList<>();
        java.util.Calendar iter = (java.util.Calendar) cal.clone();
        for (int i = 0; i < 12; i++) {
            long start = iter.getTimeInMillis();
            java.util.Calendar endCal = (java.util.Calendar) iter.clone();
            endCal.add(java.util.Calendar.MONTH, 1);
            long end = endCal.getTimeInMillis() - 1;
            ranges.add(new long[]{start, end});
            int m = iter.get(java.util.Calendar.MONTH);
            String lbl = fixedMonths[m];
            if (lbl == null || lbl.isEmpty()) {
                lbl = new java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(iter.getTime());
            }
            // Ensure single-letter uppercase label
            if (lbl.length() > 0) {
                lbl = lbl.substring(0, 1).toUpperCase(java.util.Locale.getDefault());
            }
            labels.add(lbl);
            counts.add(0);
            iter.add(java.util.Calendar.MONTH, 1);
        }

        // Fetch all users created from Jan 1 to Dec 31 and bucket client-side
        long minStart = ranges.get(0)[0];
        long maxEnd = ranges.get(ranges.size() - 1)[1];
        db.collection("Users")
                .whereGreaterThanOrEqualTo("createdAt", minStart)
                .whereLessThanOrEqualTo("createdAt", maxEnd)
                .get()
                .addOnSuccessListener(snap -> {
                    for (com.google.firebase.firestore.DocumentSnapshot d : snap.getDocuments()) {
                        Long createdAt = d.getLong("createdAt");
                        if (createdAt == null) continue;
                        for (int i = 0; i < ranges.size(); i++) {
                            long[] r = ranges.get(i);
                            if (createdAt >= r[0] && createdAt <= r[1]) {
                                counts.set(i, counts.get(i) + 1);
                                break;
                            }
                        }
                    }
                    java.util.List<Float> floatCounts = new java.util.ArrayList<>();
                    for (Integer c : counts) floatCounts.add(c != null ? c.floatValue() : 0f);
                    userGrowthChart.setData(floatCounts, labels, "");
                })
                .addOnFailureListener(err -> {
                    android.util.Log.e("AdminChart", "Failed to load user growth", err);
                    userGrowthChart.setData(new java.util.ArrayList<>(), labels, "");
                });
    }

    private void sendFeedCommand(String ip_address) {
        final CharSequence[] items = new CharSequence[]{"Level 1", "Level 2", "Level 3", "Level 4"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose feed level")
                .setItems(items, (dialog, which) -> doSendFeed(ip_address, which + 1))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doSendFeed(String ip_address, int level) {
        final long startTime = System.currentTimeMillis();
        if (ip_address != null) {
            try {
                requireContext().getSharedPreferences("feeder", 0).edit().putString("feeder_ip", ip_address.trim()).apply();
            } catch (Throwable ignored) {}
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket(ip_address, PORT);
                    try { socket.setSoTimeout(15000); } catch (Throwable ignored) {} // Increased timeout
                    OutputStream output = socket.getOutputStream();
                    String payload = "Feed:" + Math.max(1, Math.min(4, level));
                    android.util.Log.d("AdminFeed", "Sending command: " + payload + " to " + ip_address);
                    output.write(payload.getBytes());
                    output.flush();
                    String resp = null;
                    try {
                        java.io.InputStream in = socket.getInputStream();
                        byte[] b = new byte[64];
                        int n = in.read(b);
                        if (n > 0) resp = new String(b, 0, n).trim();
                    } catch (Throwable ignored) {}
                    try { socket.close(); } catch (Throwable ignored) {}

                    final String finalResp = resp;
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ("ACK".equals(finalResp)) {
                                Toast.makeText(requireContext(), "Feeding time! (Level " + level + ")", Toast.LENGTH_SHORT).show();
                                try {
                                    com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                                    com.google.firebase.auth.FirebaseUser u = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                                    if (u != null) {
                                        java.util.Map<String, Object> entry = new java.util.HashMap<>();
                                        entry.put("userId", u.getUid());
                                        entry.put("timestamp", com.google.firebase.Timestamp.now());
                                        entry.put("feedType", "Manual");
                                        entry.put("level", level);
                                        db.collection("FeedHistory").add(entry)
                                                .addOnSuccessListener(doc -> {
                                                    android.util.Log.d("FeedHistory", "Manual feed saved: " + doc.getId());
                                                })
                                                .addOnFailureListener(err -> {
                                                    android.util.Log.e("FeedHistory", "Failed to save manual feed", err);
                                                    Toast.makeText(requireContext(), "Failed to save history: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    }
                                } catch (Throwable ignored) {}
                                // Show notification and vibrate for admin
                                android.util.Log.d("AdminFeed", "ACK received - showing notification for level " + level + " after " + (System.currentTimeMillis() - startTime) + "ms");
                                com.example.project2025.Utils.NotificationHelper.showFeedingCompletedNotification(requireContext(), level);
                            } else if (finalResp != null && !finalResp.isEmpty()) {
                                android.util.Log.d("AdminFeed", "Response received: '" + finalResp + "' - not ACK after " + (System.currentTimeMillis() - startTime) + "ms");
                                Toast.makeText(requireContext(), "Feeder: " + finalResp, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Command sent.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireContext(), "Failed becauuse of : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
