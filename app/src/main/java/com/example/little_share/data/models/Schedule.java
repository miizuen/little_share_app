package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class Schedule implements Serializable {
    @DocumentId
    private String id;
    private String userId;
    private String campaignId;
    private String campaignName;
    private String campaignImage;
    private String roleName;
    @ServerTimestamp
    private Date workDate;
    private String shiftTime;
    private String qrCode;
    private String location;
    private ScheduleStatus status;
    @ServerTimestamp
    private Date createdAt;

    public enum ScheduleStatus {
        UPCOMING("Sắp diễn ra"),
        IN_PROGRESS("Đang diễn ra"),
        COMPLETED("Đã hoàn thành"),
        CANCELLED("Đã hủy");

        private String displayName;

        ScheduleStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Constructors
    public Schedule() {}

    public Schedule(String userId, String campaignId, Date workDate, String shiftTime) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.workDate = workDate;
        this.shiftTime = shiftTime;
        this.status = ScheduleStatus.UPCOMING;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public String getCampaignImage() { return campaignImage; }
    public void setCampaignImage(String campaignImage) { this.campaignImage = campaignImage; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }

    public String getShiftTime() { return shiftTime; }
    public void setShiftTime(String shiftTime) { this.shiftTime = shiftTime; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public ScheduleStatus getStatus() { return status; }
    public void setStatus(ScheduleStatus status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}