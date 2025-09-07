package com.example.project2025.Models;

import java.util.ArrayList;
import java.util.List;

public class ScheduleData {
    private String title;
    private String time;
    private List<String> selectedDays;
    private int feedLevel;
    
    public ScheduleData() {
        this.selectedDays = new ArrayList<>();
    }
    
    public ScheduleData(String title, String time, List<String> selectedDays, int feedLevel) {
        this.title = title;
        this.time = time;
        this.selectedDays = selectedDays;
        this.feedLevel = feedLevel;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public List<String> getSelectedDays() {
        return selectedDays;
    }
    
    public void setSelectedDays(List<String> selectedDays) {
        this.selectedDays = selectedDays;
    }
    
    public int getFeedLevel() {
        return feedLevel;
    }
    
    public void setFeedLevel(int feedLevel) {
        this.feedLevel = feedLevel;
    }
    
    // Helper method to get formatted days string
    public String getFormattedDays() {
        if (selectedDays == null || selectedDays.isEmpty()) {
            return "No days selected";
        }
        return String.join(", ", selectedDays);
    }
}



