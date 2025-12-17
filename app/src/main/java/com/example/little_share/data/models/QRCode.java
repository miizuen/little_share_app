package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class QRCode implements Serializable {
    @DocumentId
    private String id;
    private String code;
    private QRCodeType type;
    private String referenceId; // Campaign Registration ID, Gift Redemption ID, etc.
    private String userId;
    private String metadata; // Additional data in JSON format

    private boolean isUsed;
    @ServerTimestamp
    private Date usedAt;
    @ServerTimestamp
    private Date expiryDate;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;  // <-- thêm field bị thiếu
    private boolean isActive; // <-- thêm field bị thiếu

    public enum QRCodeType {
        CAMPAIGN_REGISTRATION("Đăng ký chiến dịch"),
        GIFT_REDEMPTION("Đổi quà"),
        ATTENDANCE("Điểm danh");

        private final String displayName;

        QRCodeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public QRCode() {}

    public QRCode(String code, QRCodeType type, String referenceId, String userId) {
        this.code = code;
        this.type = type;
        this.referenceId = referenceId;
        this.userId = userId;
        this.isUsed = false;
        this.isActive = true;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public QRCodeType getType() { return type; }
    public void setType(QRCodeType type) { this.type = type; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }

    public Date getUsedAt() { return usedAt; }
    public void setUsedAt(Date usedAt) { this.usedAt = usedAt; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Helper methods
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return new Date().after(expiryDate);
    }

    public boolean isValid() {
        return isActive && !isUsed && !isExpired();
    }
}


