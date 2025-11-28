package com.example.little_share.data.models;

import java.io.Serializable;
import java.util.Date;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class Notification implements Serializable {
    @DocumentId
    private String id;

    private String userId;

    private String title;
    private String message;
    private String type;

    private String referenceId;

    private boolean isRead;

    @ServerTimestamp
    private Date createdAt;

    public enum NotificationType {
        CAMPAIGN_NEW("Chiến dịch mới"),
        CAMPAIGN_APPROVED("Đã được duyệt"),
        CAMPAIGN_REMINDER("Nhắc nhở"),
        DONATION_CONFIRMED("Quyên góp xác nhận"),
        GIFT_AVAILABLE("Quà mới"),
        SPONSORSHIP_SUCCESS("Tài trợ thành công"),
        GENERAL("Thông báo chung");

        private String displayName;
        NotificationType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // Firebase requires no-argument constructor
    public Notification() {
        this.isRead = false;
    }

    public Notification(String userId, String title, String message, NotificationType type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type.name();
        this.isRead = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }


    public String getReferenceId() { return referenceId; }

    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }


    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }


    public Date getCreatedAt() { return createdAt; }

    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public NotificationType getTypeEnum() {
        try {
            return NotificationType.valueOf(type);
        } catch (Exception e) {
            return NotificationType.GENERAL;
        }
    }
}

