package com.example.little_share.data.models;

public class VolunteerHistoryModel {
    private String status;
    private int points;
    private String campaignTitle;
    private String role;
    private String date;
    private String time;
    private String statusColor;  // Màu của status badge
    private boolean isCompleted;

    public VolunteerHistoryModel(String status, int points, String campaignTitle,
                                 String role, String date, String time,
                                 String statusColor, boolean isCompleted) {
        this.status = status;
        this.points = points;
        this.campaignTitle = campaignTitle;
        this.role = role;
        this.date = date;
        this.time = time;
        this.statusColor = statusColor;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getCampaignTitle() {
        return campaignTitle;
    }

    public void setCampaignTitle(String campaignTitle) {
        this.campaignTitle = campaignTitle;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}