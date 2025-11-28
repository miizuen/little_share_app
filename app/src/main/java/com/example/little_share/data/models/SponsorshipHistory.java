package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class SponsorshipHistory implements Serializable {
    @DocumentId
    private String id;
    private String sponsorId;
    private String sponsorName;
    private String campaignId;
    private String campaignName;
    private String campaignImage;
    private double amount;
    @ServerTimestamp
    private Date sponsorshipDate;
    private String status;

    // Constructors
    public SponsorshipHistory() {}

    public SponsorshipHistory(String sponsorId, String campaignId, double amount) {
        this.sponsorId = sponsorId;
        this.campaignId = campaignId;
        this.amount = amount;
        this.sponsorshipDate = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSponsorId() { return sponsorId; }
    public void setSponsorId(String sponsorId) { this.sponsorId = sponsorId; }

    public String getSponsorName() { return sponsorName; }
    public void setSponsorName(String sponsorName) { this.sponsorName = sponsorName; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public String getCampaignImage() { return campaignImage; }
    public void setCampaignImage(String campaignImage) { this.campaignImage = campaignImage; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getSponsorshipDate() { return sponsorshipDate; }
    public void setSponsorshipDate(Date sponsorshipDate) { this.sponsorshipDate = sponsorshipDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
