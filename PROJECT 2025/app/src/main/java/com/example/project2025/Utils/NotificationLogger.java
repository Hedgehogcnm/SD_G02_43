package com.example.project2025.Utils;

import android.content.Context;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationLogger {

    public static void logLowFood(Context context, String targetUserId, String role) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = resolveTargetUserId(context, targetUserId);
            if (userId == null) return;
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("timestamp", Timestamp.now());
            data.put("type", "LOW_FOOD");
            data.put("source", "SYSTEM");
            data.put("role", role != null ? role : resolveRole(context));
            db.collection("NotificationHistory").add(data);
        } catch (Throwable ignored) {}
    }

    public static void logFeeding(Context context, String source, String role, int level, String title, String scheduleId, String targetUserId) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = resolveTargetUserId(context, targetUserId);
            if (userId == null) return;
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("timestamp", Timestamp.now());
            data.put("type", "FEEDING");
            data.put("source", source != null ? source : "UNKNOWN");
            data.put("role", role != null ? role : resolveRole(context));
            data.put("level", level);
            if (title != null) data.put("title", title);
            if (scheduleId != null) data.put("scheduleId", scheduleId);
            db.collection("NotificationHistory").add(data);
        } catch (Throwable ignored) {}
    }

    private static String resolveTargetUserId(Context context, String explicit) {
        if (explicit != null && !explicit.trim().isEmpty()) return explicit;
        try {
            String role = resolveRole(context);
            if ("Admin".equals(role)) {
                return context.getSharedPreferences("ADMINISTRATION", 0).getString("uid", null);
            }
        } catch (Throwable ignored) {}
        try {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            return u != null ? u.getUid() : null;
        } catch (Throwable ignored) {}
        return null;
    }

    private static String resolveRole(Context context) {
        try {
            return context.getSharedPreferences("ROLE", 0).getString("Role", null);
        } catch (Throwable ignored) {}
        return null;
    }
}



