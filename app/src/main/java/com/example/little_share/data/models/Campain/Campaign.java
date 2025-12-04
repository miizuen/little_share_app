package com.example.little_share.data.models.Campain;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class Campaign implements Serializable {
    @DocumentId
    private String id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private String organizationId;
    private String organizationName;
    private String location;
    private String specificLocation;
    private Date startDate;


    private Date endDate;

    private String status;


    private int maxVolunteers;


    private int currentVolunteers;


    private int pointsReward;

    private String requirements;
    private String activities;


    private boolean needsSponsor;


    private double targetBudget;


    private double currentBudget;


    private String budgetPurpose;


    private String accountNumber;


    private String contactPhone;


    private String contactEmail;

    private String materials;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    public enum CampaignCategory {
        EDUCATION("Giáo dục"),
        FOOD("Nấu ăn và dinh dưỡng"),
        ENVIRONMENT("Môi trường"),
        HEALTH("Y tế"),
        URGENT("Khẩn cấp");

        private String displayName;
        CampaignCategory(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public void setCategoryCampaign(CampaignCategory categoryEnum) {
        if (categoryEnum != null) {
            this.category = categoryEnum.name();
        }
    }

    public enum CampaignStatus {
        UPCOMING("Sắp diễn ra"),
        ONGOING("Đang diễn ra"),
        COMPLETED("Đã kết thúc");

        private String displayName;
        CampaignStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    // Firebase requires no-argument constructor
    public Campaign() {
        this.status = CampaignStatus.UPCOMING.name();
        this.currentVolunteers = 0;
        this.currentBudget = 0;
    }

    public Campaign(String name, CampaignCategory category, String organizationName, String location) {
        this();
        this.name = name;
        this.category = category.name();
        this.organizationName = organizationName;
        this.location = location;
    }

    // Getters and Setters (required by Firebase)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

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

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }


    public String getSpecificLocation() { return specificLocation; }

    public void setSpecificLocation(String specificLocation) { this.specificLocation = specificLocation; }


    public Date getStartDate() { return startDate; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }


    public Date getEndDate() { return endDate; }

    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getMaxVolunteers() { return maxVolunteers; }
    public void setMaxVolunteers(int maxVolunteers) { this.maxVolunteers = maxVolunteers; }

    public int getCurrentVolunteers() { return currentVolunteers; }

    public void setCurrentVolunteers(int currentVolunteers) { this.currentVolunteers = currentVolunteers; }


    public int getPointsReward() { return pointsReward; }

    public void setPointsReward(int pointsReward) { this.pointsReward = pointsReward; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getActivities() { return activities; }
    public void setActivities(String activities) { this.activities = activities; }


    public boolean isNeedsSponsor() { return needsSponsor; }

    public void setNeedsSponsor(boolean needsSponsor) { this.needsSponsor = needsSponsor; }


    public double getTargetBudget() { return targetBudget; }

    public void setTargetBudget(double targetBudget) { this.targetBudget = targetBudget; }


    public double getCurrentBudget() { return currentBudget; }

    public void setCurrentBudget(double currentBudget) { this.currentBudget = currentBudget; }


    public String getBudgetPurpose() { return budgetPurpose; }

    public void setBudgetPurpose(String budgetPurpose) { this.budgetPurpose = budgetPurpose; }

    public String getAccountNumber() { return accountNumber; }

    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getContactPhone() { return contactPhone; }

    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }

    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getMaterials() { return materials; }
    public void setMaterials(String materials) { this.materials = materials; }


    public Date getCreatedAt() { return createdAt; }

    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public int getProgressPercentage() {
        if (maxVolunteers == 0) return 0;
        return (currentVolunteers * 100) / maxVolunteers;
    }

    public int getBudgetProgressPercentage() {
        if (targetBudget == 0) return 0;
        return (int) ((currentBudget * 100) / targetBudget);
    }

    public boolean isVolunteersFull() {
        return currentVolunteers >= maxVolunteers;
    }

    public double getRemainingBudget() {
        return targetBudget - currentBudget;
    }

    public CampaignCategory getCategoryEnum() {
        try {
            return CampaignCategory.valueOf(category);
        } catch (Exception e) {
            return CampaignCategory.EDUCATION;
        }
    }

    public CampaignStatus getStatusEnum() {
        try {
            return CampaignStatus.valueOf(status);
        } catch (Exception e) {
            return CampaignStatus.UPCOMING;
        }
    }
}

