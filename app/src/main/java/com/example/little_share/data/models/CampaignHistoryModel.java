package com.example.little_share.data.models;

public class CampaignHistoryModel {
    private String campaignName;
    private String location;
    private String date;
    private String donationAmount;
    private int imageResId;
    private String buttonText;

    public CampaignHistoryModel(String campaignName, String location, String date,
                                String donationAmount, int imageResId, String buttonText) {
        this.campaignName = campaignName;
        this.location = location;
        this.date = date;
        this.donationAmount = donationAmount;
        this.imageResId = imageResId;
        this.buttonText = buttonText;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDonationAmount() {
        return donationAmount;
    }

    public void setDonationAmount(String donationAmount) {
        this.donationAmount = donationAmount;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }
}
