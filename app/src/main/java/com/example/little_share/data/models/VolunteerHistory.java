package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class VolunteerHistory implements Serializable {
    @DocumentId
    private String id;
    private String userId;
    private String campaignId;
    private String campaignName;
    private String campaignImage;
    private String roleName;
    private int pointsEarned;
    @ServerTimestamp
    private Date participationDate;
    @ServerTimestamp
    private Date completionDate;
    private String status;
    @ServerTimestamp
    private Date createdAt;

    // Constructors
    public VolunteerHistory() {}

    public VolunteerHistory(String userId, String campaignId, String roleName, int pointsEarned) {
        this.userId = userId;
        this.campaignId = campaignId;
        this.roleName = roleName;
        this.pointsEarned = pointsEarned;
        this.participationDate = new Date();
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

    public int getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(int pointsEarned) { this.pointsEarned = pointsEarned; }

    public Date getParticipationDate() { return participationDate; }
    public void setParticipationDate(Date participationDate) { this.participationDate = participationDate; }

    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
