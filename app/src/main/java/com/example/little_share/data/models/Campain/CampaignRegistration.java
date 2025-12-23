package com.example.little_share.data.models.Campain;

import java.io.Serializable;
import java.util.Date;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class CampaignRegistration implements Serializable {
    @DocumentId
    private String id;
    private String userId;
    private String userName;
    private String campaignId;
    private String campaignName;
    private String roleId;
    private String roleName;

    // Firebase lưu dạng Long (timestamp milliseconds)
    private Long attendedAt;
    private Long createdAt; // THAY ĐỔI: Long thay vì Date
    private Long approvedAt;

    @PropertyName("date")
    private String dateString; // Firebase: "29/12/2025"

    private Date workDate; // Chỉ dùng local
    private String shiftTime;
    private String shiftId;
    private String shiftName;
    private String note;
    private String status;
    private String qrCode;
    private int pointsEarned;
    private String oderId;
    private String organizationId;
    private String userEmail;
    private boolean isAttended;
    private String rejectionReason;

    public enum RegistrationStatus {
        PENDING("pending", "Chờ duyệt"),
        APPROVED("approved", "Đã duyệt"),
        REJECTED("rejected", "Từ chối"),
        COMPLETED("completed", "Hoàn thành"),
        CANCELLED("cancelled", "Đã hủy");

        private String value;
        private String displayName;

        RegistrationStatus(String value, String displayName) {
            this.value = value;
            this.displayName = displayName;
        }

        public String getValue() { return value; }
        public String getDisplayName() { return displayName; }
    }

    public CampaignRegistration() {
        this.status = RegistrationStatus.PENDING.getValue();
    }

    public CampaignRegistration(String userId, String campaignId, String roleId, Date workDate, String shiftTime) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.roleId = roleId;
        this.workDate = workDate;
        this.shiftTime = shiftTime;
        this.status = RegistrationStatus.PENDING.getValue();
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public Long getAttendedAt() { return attendedAt; }
    public void setAttendedAt(Long attendedAt) { this.attendedAt = attendedAt; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public Long getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Long approvedAt) { this.approvedAt = approvedAt; }

    @PropertyName("date")
    public String getDateString() { return dateString; }

    @PropertyName("date")
    public void setDateString(String dateString) {
        this.dateString = dateString;

        // Auto-parse to workDate
        if (dateString != null && !dateString.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                this.workDate = sdf.parse(dateString);
            } catch (Exception e) {
                android.util.Log.e("CampaignRegistration", "Error parsing date: " + dateString, e);
            }
        }
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public String getShiftTime() { return shiftTime; }
    public void setShiftTime(String shiftTime) { this.shiftTime = shiftTime; }

    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }

    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

    public String getOderId() { return oderId; }
    public void setOderId(String oderId) { this.oderId = oderId; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public boolean isAttended() { return isAttended; }
    public void setAttended(boolean attended) { isAttended = attended; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    // Helper methods
    public RegistrationStatus getStatusEnum() {
        if (status == null) return RegistrationStatus.PENDING;

        for (RegistrationStatus rs : RegistrationStatus.values()) {
            if (rs.getValue().equalsIgnoreCase(status)) {
                return rs;
            }
        }
        return RegistrationStatus.PENDING;
    }

    public Date getCreatedAtDate() {
        return createdAt != null ? new Date(createdAt) : null;
    }

    public Date getAttendedAtDate() {
        return attendedAt != null ? new Date(attendedAt) : null;
    }

    public Date getApprovedAtDate() {
        return approvedAt != null ? new Date(approvedAt) : null;
    }
}