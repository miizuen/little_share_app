package com.example.little_share.data.models;

import java.util.List;

public class CampaignModel {
    private String id;
    private String name;
    private String category;
    private String status;
    private String location;
    private String startDate;
    private String endDate;
    private int currentVolunteers;
    private int maxVolunteers;
    private int points;
    private long currentAmount;
    private long targetAmount;
    private int iconResId;
    private int progressPercentage;
    private List<Sponsor> sponsors;

    public CampaignModel(String id, String name, String category, String status, String location,
                         String startDate, String endDate, int currentVolunteers, int maxVolunteers,
                         int points, long currentAmount, long targetAmount, int iconResId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.status = status;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentVolunteers = currentVolunteers;
        this.maxVolunteers = maxVolunteers;
        this.points = points;
        this.currentAmount = currentAmount;
        this.targetAmount = targetAmount;
        this.iconResId = iconResId;
        this.progressPercentage = calculateProgress();
    }

    private int calculateProgress() {
        if (targetAmount == 0) return 0;
        return (int) ((currentAmount * 100) / targetAmount);
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getLocation() { return location; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public int getCurrentVolunteers() { return currentVolunteers; }
    public int getMaxVolunteers() { return maxVolunteers; }
    public int getPoints() { return points; }
    public long getCurrentAmount() { return currentAmount; }
    public long getTargetAmount() { return targetAmount; }
    public int getIconResId() { return iconResId; }
    public int getProgressPercentage() { return progressPercentage; }
    public List<Sponsor> getSponsors() { return sponsors; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setStatus(String status) { this.status = status; }
    public void setLocation(String location) { this.location = location; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setCurrentVolunteers(int currentVolunteers) { this.currentVolunteers = currentVolunteers; }
    public void setMaxVolunteers(int maxVolunteers) { this.maxVolunteers = maxVolunteers; }
    public void setPoints(int points) { this.points = points; }
    public void setCurrentAmount(long currentAmount) {
        this.currentAmount = currentAmount;
        this.progressPercentage = calculateProgress();
    }
    public void setTargetAmount(long targetAmount) {
        this.targetAmount = targetAmount;
        this.progressPercentage = calculateProgress();
    }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
    public void setSponsors(List<Sponsor> sponsors) { this.sponsors = sponsors; }

    // Helper method
    public String getDateRange() {
        return startDate + " - " + endDate;
    }

    public String getVolunteerStatus() {
        return currentVolunteers + "/" + maxVolunteers;
    }

    // Inner class for Sponsor
    public static class Sponsor {
        private String name;
        private long amount;

        public Sponsor(String name, long amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() { return name; }
        public long getAmount() { return amount; }
        public void setName(String name) { this.name = name; }
        public void setAmount(long amount) { this.amount = amount; }
    }
}