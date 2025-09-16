package com.example.project2025.Dashboard;

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

    CardView tempUserListButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextView userCount;
    EditText ip_address ;
    Button manualFeed;
    private static final int PORT = 12345;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Currently shows "Admin Dashboard" and "(Empty for now)" text
        View root = inflater.inflate(R.layout.dashboard_fragment_admin, container, false);

        ip_address = root.findViewById(R.id.ip_address);
        manualFeed = root.findViewById(R.id.manual_feed);
        userCount = root.findViewById(R.id.user_count);
        tempUserListButton = root.findViewById(R.id.temp_user_list);

        tempUserListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserListFragment.class);
                startActivity(intent);
            }
        });
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserCount();

        manualFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip_address_text = ip_address.getText().toString();
                sendFeedCommand(ip_address_text);
            }
        });
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
    private void sendFeedCommand(String ip_address) {
        final CharSequence[] items = new CharSequence[]{"Level 1", "Level 2", "Level 3", "Level 4"};
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Choose feed level")
                .setItems(items, (dialog, which) -> doSendFeed(ip_address, which + 1))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doSendFeed(String ip_address, int level) {
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
                    try { socket.setSoTimeout(4000); } catch (Throwable ignored) {}
                    OutputStream output = socket.getOutputStream();
                    String payload = "Feed:" + Math.max(1, Math.min(4, level));
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
                            } else if (finalResp != null && !finalResp.isEmpty()) {
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
