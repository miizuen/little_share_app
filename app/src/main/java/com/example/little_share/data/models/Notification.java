package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {

    // Enum for notification types - COMPLETE VERSION
    public enum NotificationType {
        // Campaign notifications
        CAMPAIGN_NEW("CAMPAIGN_NEW", "Chiến dịch mới"),
        CAMPAIGN_APPROVED("CAMPAIGN_APPROVED", "Đã được duyệt"),
        CAMPAIGN_REMINDER("CAMPAIGN_REMINDER", "Nhắc nhở"),
        CAMPAIGN_UPDATE("CAMPAIGN_UPDATE", "Cập nhật chiến dịch"),

        // Donation notifications (vật phẩm)
        DONATION_PENDING("DONATION_PENDING", "Quyên góp chờ duyệt"),
        DONATION_NEW("DONATION_NEW", "Quyên góp mới"),
        DONATION_CONFIRMED("DONATION_CONFIRMED", "Quyên góp xác nhận"),
        DONATION_REJECTED("DONATION_REJECTED", "Quyên góp từ chối"),
        DONATION_RECEIVED("DONATION_RECEIVED", "Đã nhận đồ"),
        DONATION_CAMPAIGN_NEW("DONATION_CAMPAIGN_NEW", "Chiến dịch quyên góp mới"),

        // Sponsorship notifications (tài trợ tiền)
        DONATION_SUCCESS("DONATION_SUCCESS", "Quyên góp thành công"),
        SPONSORSHIP_SUCCESS("SPONSORSHIP_SUCCESS", "Tài trợ thành công"),

        // Registration notifications
        REGISTRATION_APPROVED("REGISTRATION_APPROVED", "Đăng ký được duyệt"),
        REGISTRATION_REJECTED("REGISTRATION_REJECTED", "Đăng ký bị từ chối"),

        // Gift notifications
        GIFT_AVAILABLE("GIFT_AVAILABLE", "Quà mới"),

        // System notifications
        SYSTEM("SYSTEM", "Hệ thống"),
        GENERAL("GENERAL", "Thông báo chung");

        private String value;
        private String displayName;

        NotificationType(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() {
            return value;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static NotificationType fromString(String value) {
            if (value == null) return GENERAL;

            for (NotificationType type : NotificationType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return GENERAL;
        }
    }

    @DocumentId
    private String id;
    private String userId;
    private String title;
    private String description;
    private String message; // Alias for description to support existing adapter
    private String type; // "CAMPAIGN_NEW", "DONATION_SUCCESS", etc.
    private String relatedId; // campaignId, donationId, etc.
    private boolean isRead;
    private String iconType; // "megaphone", "heart", "bell", "info"

    @ServerTimestamp
    private Date createdAt;

    public Notification() {
        this.isRead = false;
    }

    public Notification(String userId, String title, String description, String type) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.message = description;
        this.type = type;
        this.isRead = false;
        this.iconType = getDefaultIconForType(type);
        this.createdAt = new Date();  // ← THÊM DÒNG NÀY
    }
    public Notification(String userId, String title, String description, String type, String relatedId) {
        this(userId, title, description, type);
        this.relatedId = relatedId;
    }

    private String getDefaultIconForType(String type) {
        if (type == null) return "bell";

        switch (type.toUpperCase()) {
            case "CAMPAIGN_NEW":
            case "CAMPAIGN_UPDATE":
            case "DONATION_CAMPAIGN_NEW":
                return "megaphone";

            case "DONATION_SUCCESS":
            case "DONATION_CONFIRMED":
            case "DONATION_RECEIVED":
            case "SPONSORSHIP_SUCCESS":
                return "heart";

            case "DONATION_PENDING":
                return "clock";

            case "DONATION_NEW":
                return "donation";

            case "DONATION_REJECTED":
            case "REGISTRATION_REJECTED":
                return "close";

            case "CAMPAIGN_APPROVED":
            case "REGISTRATION_APPROVED":
                return "check";

            case "CAMPAIGN_REMINDER":
                return "bell";

            case "GIFT_AVAILABLE":
                return "gift";

            case "SYSTEM":
                return "info";

            default:
                return "bell";
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        this.message = description; // Keep message in sync
    }

    // Alias methods for compatibility with existing adapter
    public String getMessage() {
        return message != null ? message : description;
    }
    public void setMessage(String message) {
        this.message = message;
        this.description = message; // Keep description in sync
    }

    public NotificationType getTypeEnum() {
        return NotificationType.fromString(type);
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRelatedId() { return relatedId; }
    public void setRelatedId(String relatedId) { this.relatedId = relatedId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    // Helper method to get time ago string
    public String getTimeAgo() {
        if (createdAt == null) return "Vừa xong";

        long diff = System.currentTimeMillis() - createdAt.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }
}