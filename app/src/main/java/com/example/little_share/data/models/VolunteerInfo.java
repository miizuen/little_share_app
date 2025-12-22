package com.example.little_share.data.models;

public class VolunteerInfo {
    private VolunteerRegistration registration;
    private String avatar;
    private int totalPoints;
    private int totalCampaigns;

    // Constructor, getters, setters
    public VolunteerInfo(VolunteerRegistration registration) {
        this.registration = registration;
    }

    // Getters và setters
    public VolunteerRegistration getRegistration() { return registration; }
    public void setRegistration(VolunteerRegistration registration) { this.registration = registration; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    public int getTotalCampaigns() { return totalCampaigns; }
    public void setTotalCampaigns(int totalCampaigns) { this.totalCampaigns = totalCampaigns; }
}
