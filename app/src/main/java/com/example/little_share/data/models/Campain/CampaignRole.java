package com.example.little_share.data.models.Campain;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity(tableName = "campaign_roles")
public class CampaignRole {
    @PrimaryKey
    @SerializedName("id")
    private String id;

    @ColumnInfo(name = "campaign_id")
    @SerializedName("campaign_id")
    private String campaignId;

    @ColumnInfo(name = "role_name")
    @SerializedName("role_name")
    private String roleName;

    @ColumnInfo(name = "description")
    @SerializedName("description")
    private String description;

    @ColumnInfo(name = "max_volunteers")
    @SerializedName("max_volunteers")
    private int maxVolunteers;

    @ColumnInfo(name = "current_volunteers")
    @SerializedName("current_volunteers")
    private int currentVolunteers;

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    private Date createdAt;

    public CampaignRole() {}

    public CampaignRole(String id, String campaignId, String roleName, int maxVolunteers) {
        this.id = id;
        this.campaignId = campaignId;
        this.roleName = roleName;
        this.maxVolunteers = maxVolunteers;
        this.currentVolunteers = 0;
        this.createdAt = new Date();
    }

    public boolean isFull() {
        return currentVolunteers >= maxVolunteers;
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

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
