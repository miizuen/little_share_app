package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

public class SponsorDonation implements Serializable {
    @DocumentId
    private String id;
    private String sponsorId;
    private String sponsorName;
    private String campaignId;
    private String campaignName;
    private String organizationName;
    private double amount;
    private String message;
    private String paymentMethod;
    private String transactionId;
    private String status;
    
    @ServerTimestamp
    private Date donationDate;
    
    public SponsorDonation() {
        this.status = "COMPLETED";
        this.paymentMethod = "ZaloPay";
    }
    
    public SponsorDonation(String sponsorId, String sponsorName, String campaignId, 
                          String campaignName, String organizationName, double amount, 
                          String message, String transactionId) {
        this.sponsorId = sponsorId;
        this.sponsorName = sponsorName;
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.organizationName = organizationName;
        this.amount = amount;
        this.message = message;
        this.transactionId = transactionId;
        this.status = "COMPLETED";
        this.paymentMethod = "ZaloPay";
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
    
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getDonationDate() { return donationDate; }
    public void setDonationDate(Date donationDate) { this.donationDate = donationDate; }
}