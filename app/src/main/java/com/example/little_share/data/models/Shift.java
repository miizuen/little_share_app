package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;
@IgnoreExtraProperties
public class Shift implements Serializable {
    @DocumentId
    private String id;
    private String campaignId;
    private String shiftName;
    private String startTime;
    private String endTime;
    private int maxVolunteers;
    private int currentVolunteers;

    // Constructors
    public Shift() {}

    public Shift(String campaignId, String shiftName, String startTime, String endTime, int maxVolunteers) {
        this.campaignId = campaignId;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxVolunteers = maxVolunteers;
        this.currentVolunteers = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getMaxVolunteers() { return maxVolunteers; }
    public void setMaxVolunteers(int maxVolunteers) { this.maxVolunteers = maxVolunteers; }

    public int getCurrentVolunteers() { return currentVolunteers; }
    public void setCurrentVolunteers(int currentVolunteers) { this.currentVolunteers = currentVolunteers; }

    // Helper methods
    public String getTimeRange() {
        return startTime + " - " + endTime;
    }

    public boolean isFull() {
        return currentVolunteers >= maxVolunteers;
    }
}
