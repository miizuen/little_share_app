package com.example.little_share.data.models.Campain;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

@IgnoreExtraProperties
public class CampaignRole implements Serializable {
    @DocumentId
    private String id;
    private String campaignId;
    private String roleName;
    private String description;
    private int maxVolunteers;
    private int currentVolunteers;
    private int pointsReward;

    public CampaignRole() {
        this.currentVolunteers = 0;
    }

    public CampaignRole(String campaignId, String roleName, int maxVolunteers, int pointsReward) {
        this();
        this.campaignId = campaignId;
        this.roleName = roleName;
        this.maxVolunteers = maxVolunteers;
        this.pointsReward = pointsReward;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCampaignId() { return campaignId; }

    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getRoleName() { return roleName; }

    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getMaxVolunteers() { return maxVolunteers; }

    public void setMaxVolunteers(int maxVolunteers) { this.maxVolunteers = maxVolunteers; }

    public int getCurrentVolunteers() { return currentVolunteers; }

    public void setCurrentVolunteers(int currentVolunteers) { this.currentVolunteers = currentVolunteers; }

    public int getPointsReward() { return pointsReward; }

    public void setPointsReward(int pointsReward) { this.pointsReward = pointsReward; }

    public boolean isFull() { return currentVolunteers >= maxVolunteers; }
    public String getAvailabilityText() { return currentVolunteers + "/" + maxVolunteers; }
}
