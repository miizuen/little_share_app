package com.example.little_share.data.models.Campain;


import java.io.Serializable;
import java.util.Date;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

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

    private Date registrationDate;

    private Date workDate;

    private String shiftTime;
    private String notes;
    private String status;

    private String qrCode;

    private int pointsEarned;
    @ServerTimestamp
    private Date createdAt;

    public enum RegistrationStatus {
        PENDING("Chờ duyệt"),
        APPROVED("Đã duyệt"),
        REJECTED("Từ chối"),
        COMPLETED("Hoàn thành"),
        CANCELLED("Đã hủy");

        private String displayName;
        RegistrationStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public CampaignRegistration() {
        this.status = RegistrationStatus.PENDING.name();
    }

    public CampaignRegistration(String userId, String campaignId, String roleId, Date workDate, String shiftTime) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.roleId = roleId;
        this.workDate = workDate;
        this.shiftTime = shiftTime;
        this.status = RegistrationStatus.PENDING.name();
        this.registrationDate = new Date();
    }

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


    public Date getRegistrationDate() { return registrationDate; }

    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }


    public Date getWorkDate() { return workDate; }

    public void setWorkDate(Date workDate) { this.workDate = workDate; }


    public String getShiftTime() { return shiftTime; }

    public void setShiftTime(String shiftTime) { this.shiftTime = shiftTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrCode() { return qrCode; }

    public void setQrCode(String qrCode) { this.qrCode = qrCode; }


    public int getPointsEarned() { return pointsEarned; }

    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }


    public Date getCreatedAt() { return createdAt; }

    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public RegistrationStatus getStatusEnum() {
        try {
            return RegistrationStatus.valueOf(status);
        } catch (Exception e) {
            return RegistrationStatus.PENDING;
        }
    }
}
