package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class SponsorDonation implements Serializable {
    @DocumentId
    private String id;
    private String sponsorId;
    private String campaignId;
    private double amount;
    @ServerTimestamp
    private Date donationDate;
    private String transactionId;
    private String paymentMethod;

    // Constructors
    public SponsorDonation() {}

    public SponsorDonation(String sponsorId, String campaignId, double amount) {
        this.sponsorId = sponsorId;
        this.campaignId = campaignId;
        this.amount = amount;
        this.donationDate = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSponsorId() { return sponsorId; }
    public void setSponsorId(String sponsorId) { this.sponsorId = sponsorId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getDonationDate() { return donationDate; }
    public void setDonationDate(Date donationDate) { this.donationDate = donationDate; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}

