package com.example.little_share.data.models;

import java.io.Serializable;
import java.util.Date;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

@IgnoreExtraProperties
public class Donation implements Serializable {
    @DocumentId
    private String id;

    private String userId;

    private String userName;
    private String type;

    private String organizationId;

    private String organizationName;
    private String status;

    private int pointsEarned;

    private Date donationDate;
    @ServerTimestamp
    private Date createdAt;

    public enum DonationType {
        BOOKS("Sách vở"),
        CLOTHES("Quần áo"),
        TOYS("Đồ chơi"),
        ESSENTIALS("Nhu yếu phẩm");

        private String displayName;
        DonationType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public enum DonationStatus {
        PENDING("Chờ xác nhận"),
        CONFIRMED("Đã xác nhận"),
        RECEIVED("Đã nhận"),
        REJECTED("Từ chối");

        private String displayName;
        DonationStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public Donation() {
        this.status = DonationStatus.PENDING.name();
        this.donationDate = new Date();
    }

    public Donation(String userId, DonationType type, String organizationId) {
        this.userId = userId;
        this.type = type.name();
        this.organizationId = organizationId;
        this.status = DonationStatus.PENDING.name();
        this.donationDate = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }


    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }


    public String getOrganizationId() { return organizationId; }

    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }


    public String getOrganizationName() { return organizationName; }

    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }


    public int getPointsEarned() { return pointsEarned; }

    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }


    public Date getDonationDate() { return donationDate; }

    public void setDonationDate(Date donationDate) { this.donationDate = donationDate; }


    public Date getCreatedAt() { return createdAt; }

    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public DonationType getTypeEnum() {
        try {
            return DonationType.valueOf(type);
        } catch (Exception e) {
            return DonationType.BOOKS;
        }
    }

    public DonationStatus getStatusEnum() {
        try {
            return DonationStatus.valueOf(status);
        } catch (Exception e) {
            return DonationStatus.PENDING;
        }
    }
}

