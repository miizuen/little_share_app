package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;
public class VolunteerRegistration implements Serializable {

    @DocumentId
    private String id;
    private String oderId;
    private String campaignId;
    private String campaignName;
    private String organizationId;
    private String roleId;
    private String roleName;
    private String userId;
    private String userName;
    private String date;
    private String shiftId;
    private String shiftName;
    private String shiftTime;
    private String note;
    private String status;
    private String rejectionReason;
    private Long createdAt;
    private Long approvedAt;
    private Long rejectedAt;
    private String userEmail;
    private String qrCode;
    private int points;

    private int pointsEarned; // Điểm đã nhận từ chiến dịch này

    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }


    // Getter & Setter
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public VolunteerRegistration() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOderId() { return oderId; }
    public void setOderId(String oderId) { this.oderId = oderId; }
    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getRoleId() { return roleId; }
    public void setRoleId(String roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public String getShiftTime() { return shiftTime; }
    public void setShiftTime(String shiftTime) { this.shiftTime = shiftTime; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    public Long getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Long approvedAt) { this.approvedAt = approvedAt; }
    public Long getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(Long rejectedAt) { this.rejectedAt = rejectedAt; }
}
