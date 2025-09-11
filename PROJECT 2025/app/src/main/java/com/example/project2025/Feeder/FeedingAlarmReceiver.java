package com.example.project2025.Feeder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.OutputStream;
import java.net.Socket;

public class FeedingAlarmReceiver extends BroadcastReceiver {
    private static final int PORT = 12345;

    @Override
    public void onReceive(Context context, Intent intent) {
        String ip = context.getSharedPreferences("feeder", 0).getString("feeder_ip", null);
        int level = intent != null ? intent.getIntExtra("level", 1) : 1;
        String title = intent != null ? intent.getStringExtra("title") : null;
        String scheduleId = intent != null ? intent.getStringExtra("scheduleId") : null;
        if (ip == null || ip.isEmpty()) {
            Toast.makeText(context, "Feeder IP not set", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Socket socket = new Socket(ip, PORT);
                OutputStream output = socket.getOutputStream();
                String payload = level > 0 ? ("Feed:" + level) : "Feed";
                output.write(payload.getBytes());
                output.flush();
                socket.close();

                // Save scheduled feed history
                try {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser u = auth.getCurrentUser();
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
                } catch (Throwable ignored) {}
            } catch (Exception ignored) {
            }
        }).start();
    }
}




