package com.example.project2025.Models;

import com.google.firebase.Timestamp;

public class NotificationItem {
    public String id;
    public String type; // LOW_FOOD | FEEDING
    public String source; // MANUAL | SCHEDULED | SYSTEM | UNKNOWN
    public String role; // Admin | Users
    public String title;
    public String scheduleId;
    public int level;
    public Timestamp timestamp;
}



