package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    
    // Enum for notification types to support existing adapter
    public enum NotificationType {
        CAMPAIGN_NEW("CAMPAIGN_NEW"),
        CAMPAIGN_APPROVED("CAMPAIGN_APPROVED"),
        CAMPAIGN_REMINDER("CAMPAIGN_REMINDER"),
        DONATION_CONFIRMED("DONATION_CONFIRMED"),
        DONATION_SUCCESS("DONATION_SUCCESS"),
        GIFT_AVAILABLE("GIFT_AVAILABLE"),
        SPONSORSHIP_SUCCESS("SPONSORSHIP_SUCCESS"),
        CAMPAIGN_UPDATE("CAMPAIGN_UPDATE"),
        SYSTEM("SYSTEM"),
        GENERAL("GENERAL");

        private String value;
        
        NotificationType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
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
    private String type; // "CAMPAIGN_NEW", "DONATION_SUCCESS", "CAMPAIGN_UPDATE", "SYSTEM"
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
        this.message = description; // Keep in sync
        this.type = type;
        this.isRead = false;
        this.iconType = getDefaultIconForType(type);
    }

    public Notification(String userId, String title, String description, String type, String relatedId) {
        this(userId, title, description, type);
        this.relatedId = relatedId;
    }

    private String getDefaultIconForType(String type) {
        if (type == null) return "bell";
        
        switch (type.toUpperCase()) {
            case "CAMPAIGN_NEW": return "megaphone";
            case "DONATION_SUCCESS": return "heart";
            case "CAMPAIGN_UPDATE": return "bell";
            case "SYSTEM": return "info";
            default: return "bell";
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