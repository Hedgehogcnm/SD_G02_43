package com.example.project2025.Feeder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class FeedingAlarmReceiver extends BroadcastReceiver {
    private static final int PORT = 12345;
    private String role;
    @Override
    public void onReceive(Context context, Intent intent) {

        role = context.getSharedPreferences("ROLE", 0).getString("Role", null);
        String ip = context.getSharedPreferences("FEEDERIP", 0).getString("feeder_ip", null);
        if (ip == null){
            ip = context.getSharedPreferences("ADMINISTRATION", 0).getString("feeder_ip", null);
        }
        int level = intent != null ? intent.getIntExtra("level", 1) : 1;
        String title = intent != null ? intent.getStringExtra("title") : null;
        String scheduleId = intent != null ? intent.getStringExtra("scheduleId") : null;
        if (ip == null || ip.isEmpty()) {
            Toast.makeText(context,  "Feeder IP not set", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalIp = ip;
        new Thread(() -> {
            try {
                Log.d("FeedingAlarmReceiver: ", "ip = " + finalIp + ", level = " + level + ", title = " + title + ", scheduleId = " + scheduleId);
                Socket socket = new Socket(finalIp, PORT);
                OutputStream output = socket.getOutputStream();
                String payload = level > 0 ? ("Feed:" + level) : "Feed";
                output.write(payload.getBytes());
                output.flush();
                
                // Wait for response from IoT device
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = reader.readLine();
                Log.d("FeedingAlarmReceiver", "Response from device: " + response);
                
                socket.close();

                // Check if feeding was successful (ACK response)
                if ("ACK".equals(response)) {
                    // Show notification and vibrate
                    com.example.project2025.Utils.NotificationHelper.showFeedingCompletedNotification(context, level);
                }

                // Save scheduled feed history
                try {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser u = auth.getCurrentUser();
                    if(role.equals("Users")){
                        if (u != null) {
                            java.util.Map<String, Object> entry = new java.util.HashMap<>();
                            entry.put("userId", u.getUid());
                            entry.put("timestamp", Timestamp.now());
                            entry.put("feedType", "Scheduled");
                            entry.put("level", level);
                            if (title != null) entry.put("title", title);
                            if (scheduleId != null) entry.put("scheduleId", scheduleId);
                            db.collection("FeedHistory").add(entry);
                        }
                    }
                    else{
                        if (u != null) {
                            java.util.Map<String, Object> entry = new java.util.HashMap<>();
                            entry.put("userId", u.getUid());
                            entry.put("timestamp", Timestamp.now());
                            entry.put("feedType", "Scheduled");
                            entry.put("level", level);
                            if (title != null) entry.put("title", title);
                            if (scheduleId != null) entry.put("scheduleId", scheduleId);
                            db.collection("FeedHistory").add(entry);
                        }
                        java.util.Map<String, Object> entry = new java.util.HashMap<>();
                        entry.put("userId", context.getSharedPreferences("ADMINISTRATION", 0).getString("uid", null));
                        entry.put("timestamp", Timestamp.now());
                        entry.put("feedType", "Scheduled");
                        entry.put("level", level);
                        if (title != null) entry.put("title", title);
                        if (scheduleId != null) entry.put("scheduleId", scheduleId);
                        db.collection("FeedHistory").add(entry);
                    }
                } catch (Throwable ignored) {}
            } catch (Exception e) {
                Log.e("FeedingAlarmReceiver", "Error during scheduled feeding", e);
            }
        }).start();
    }
}




