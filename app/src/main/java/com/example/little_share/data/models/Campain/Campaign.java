package com.example.little_share.data.models.Campain;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

@Entity(tableName = "campaigns")
public class Campaign {
    @PrimaryKey
    @SerializedName("id")
    private String id;

    @ColumnInfo(name = "title")
    @SerializedName("title")
    private String title;

    @ColumnInfo(name = "description")
    @SerializedName("description")
    private String description;

    @ColumnInfo(name = "category")
    @SerializedName("category")
    private String category; // education, food, environment, health, urgent

    @ColumnInfo(name = "image_url")
    @SerializedName("image_url")
    private String imageUrl;

    @ColumnInfo(name = "organization_id")
    @SerializedName("organization_id")
    private String organizationId;

    @ColumnInfo(name = "organization_name")
    @SerializedName("organization_name")
    private String organizationName;

    @ColumnInfo(name = "sponsor_id")
    @SerializedName("sponsor_id")
    private String sponsorId;

    @ColumnInfo(name = "sponsor_name")
    @SerializedName("sponsor_name")
    private String sponsorName;

    @ColumnInfo(name = "location")
    @SerializedName("location")
    private String location;

    @ColumnInfo(name = "specific_location")
    @SerializedName("specific_location")
    private String specificLocation;

    @ColumnInfo(name = "start_date")
    @SerializedName("start_date")
    private Date startDate;

    @ColumnInfo(name = "end_date")
    @SerializedName("end_date")
    private Date endDate;

    @ColumnInfo(name = "activity")
    @SerializedName("activity")
    private String activity;

    @ColumnInfo(name = "requirements")
    @SerializedName("requirements")
    private String requirements;

    @ColumnInfo(name = "max_volunteers")
    @SerializedName("max_volunteers")
    private int maxVolunteers;

    @ColumnInfo(name = "current_volunteers")
    @SerializedName("current_volunteers")
    private int currentVolunteers;

    @ColumnInfo(name = "points_reward")
    @SerializedName("points_reward")
    private int pointsReward;

    @ColumnInfo(name = "status")
    @SerializedName("status")
    private String status; // upcoming, ongoing, completed, cancelled

    @ColumnInfo(name = "is_urgent")
    @SerializedName("is_urgent")
    private boolean isUrgent;

    @ColumnInfo(name = "needs_sponsor")
    @SerializedName("needs_sponsor")
    private boolean needsSponsor;

    @ColumnInfo(name = "target_amount")
    @SerializedName("target_amount")
    private long targetAmount;

    @ColumnInfo(name = "current_amount")
    @SerializedName("current_amount")
    private long currentAmount;

    @ColumnInfo(name = "budget_purpose")
    @SerializedName("budget_purpose")
    private String budgetPurpose;

    @ColumnInfo(name = "beneficiaries")
    @SerializedName("beneficiaries")
    private int beneficiaries;

    @ColumnInfo(name = "contact_phone")
    @SerializedName("contact_phone")
    private String contactPhone;

    @ColumnInfo(name = "contact_email")
    @SerializedName("contact_email")
    private String contactEmail;

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    private Date updatedAt;

    @Ignore
    @SerializedName("images")
    private List<CampaignImage> images;

    @Ignore
    @SerializedName("roles")
    private List<CampaignRole> roles;

    // Constructors
    public Campaign() {}

    public Campaign(String id, String title, String category, String organizationId) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.organizationId = organizationId;
        this.currentVolunteers = 0;
        this.currentAmount = 0;
        this.isUrgent = false;
        this.needsSponsor = false;
        this.status = "upcoming";
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Helper methods
    public int getProgressPercentage() {
        if (maxVolunteers == 0) return 0;
        return (int) ((currentVolunteers * 100.0) / maxVolunteers);
    }

    public int getFundingPercentage() {
        if (targetAmount == 0) return 0;
        return (int) ((currentAmount * 100.0) / targetAmount);
    }

    public boolean isFull() {
        return currentVolunteers >= maxVolunteers;
    }

    public boolean isFullyFunded() {
        return currentAmount >= targetAmount;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getSponsorId() { return sponsorId; }
    public void setSponsorId(String sponsorId) { this.sponsorId = sponsorId; }

    public String getSponsorName() { return sponsorName; }
    public void setSponsorName(String sponsorName) { this.sponsorName = sponsorName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSpecificLocation() { return specificLocation; }
    public void setSpecificLocation(String specificLocation) { this.specificLocation = specificLocation; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public int getMaxVolunteers() { return maxVolunteers; }
    public void setMaxVolunteers(int maxVolunteers) { this.maxVolunteers = maxVolunteers; }

    public int getCurrentVolunteers() { return currentVolunteers; }
    public void setCurrentVolunteers(int currentVolunteers) { this.currentVolunteers = currentVolunteers; }

    public int getPointsReward() { return pointsReward; }
    public void setPointsReward(int pointsReward) { this.pointsReward = pointsReward; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }

    public boolean isNeedsSponsor() { return needsSponsor; }
    public void setNeedsSponsor(boolean needsSponsor) { this.needsSponsor = needsSponsor; }

    public long getTargetAmount() { return targetAmount; }
    public void setTargetAmount(long targetAmount) { this.targetAmount = targetAmount; }

    public long getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(long currentAmount) { this.currentAmount = currentAmount; }

    public String getBudgetPurpose() { return budgetPurpose; }
    public void setBudgetPurpose(String budgetPurpose) { this.budgetPurpose = budgetPurpose; }

    public int getBeneficiaries() { return beneficiaries; }
    public void setBeneficiaries(int beneficiaries) { this.beneficiaries = beneficiaries; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public List<CampaignImage> getImages() { return images; }
    public void setImages(List<CampaignImage> images) { this.images = images; }

    public List<CampaignRole> getRoles() { return roles; }
    public void setRoles(List<CampaignRole> roles) { this.roles = roles; }
}
