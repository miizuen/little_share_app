package com.example.little_share.data.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class MoneyDonation implements Serializable {
    private String id;
    private String sponsorId;
    private String sponsorName;
    private String campaignId;
    private String campaignName;
    private double amount;
    private String transactionId;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    @ServerTimestamp
    private Date donationDate;
    @ServerTimestamp
    private Date createdAt;

    public enum PaymentMethod {
        ZALOPAY("ZaloPay"),
        BANK_TRANSFER("Chuyển khoản"),
        CASH("Tiền mặt");

        private String displayName;

        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum PaymentStatus {
        PENDING("Đang xử lý"),
        SUCCESS("Thành công"),
        FAILED("Thất bại");

        private String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Constructors
    public MoneyDonation() {}

    public MoneyDonation(String sponsorId, String campaignId, double amount) {
        this.sponsorId = sponsorId;
        this.campaignId = campaignId;
        this.amount = amount;
        this.paymentStatus = PaymentStatus.PENDING;
        this.donationDate = new Date();
        this.createdAt = new Date();
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

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public Date getDonationDate() { return donationDate; }
    public void setDonationDate(Date donationDate) { this.donationDate = donationDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
