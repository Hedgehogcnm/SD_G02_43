package com.example.project2025.Models;

import com.google.firebase.Timestamp;

import java.util.Date;

public class FeedHistory {
    private String userId;
    private Timestamp timestamp;
    private String feedType; // Manual or Scheduled

    // Empty constructor needed for Firestore
    public FeedHistory() {
    }

    public FeedHistory(String userId, Timestamp timestamp, String feedType) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.feedType = feedType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFeedType() {
        return feedType;
    }

    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }

    public Date getDate() {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toDate();
    }
    
    @Override
    public String toString() {
        return "FeedHistory{" +
                "userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                ", feedType='" + feedType + '\'' +
                '}';
    }
}