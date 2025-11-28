package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class GiftRedemption implements Serializable {
    @DocumentId
    private String id;
    private String userId;
    private String userName;
    private String giftId;
    private String giftName;
    private int pointsSpent;
    private String qrCode;
    private RedemptionStatus status;
    @ServerTimestamp
    private Date redemptionDate;
    @ServerTimestamp
    private Date pickupDate;
    @ServerTimestamp
    private Date createdAt;

    public enum RedemptionStatus {
        PENDING("Chờ trao"),
        COMPLETED("Đã trao"),
        CANCELLED("Đã hủy");

        private String displayName;

        RedemptionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Constructors
    public GiftRedemption() {}

    public GiftRedemption(String userId, String giftId, int pointsSpent) {
        this.userId = userId;
        this.giftId = giftId;
        this.pointsSpent = pointsSpent;
        this.status = RedemptionStatus.PENDING;
        this.redemptionDate = new Date();
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getGiftId() { return giftId; }
    public void setGiftId(String giftId) { this.giftId = giftId; }

    public String getGiftName() { return giftName; }
    public void setGiftName(String giftName) { this.giftName = giftName; }

    public int getPointsSpent() { return pointsSpent; }
    public void setPointsSpent(int pointsSpent) { this.pointsSpent = pointsSpent; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public RedemptionStatus getStatus() { return status; }
    public void setStatus(RedemptionStatus status) { this.status = status; }

    public Date getRedemptionDate() { return redemptionDate; }
    public void setRedemptionDate(Date redemptionDate) { this.redemptionDate = redemptionDate; }

    public Date getPickupDate() { return pickupDate; }
    public void setPickupDate(Date pickupDate) { this.pickupDate = pickupDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
