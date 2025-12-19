package com.example.little_share.data.models.sponsor;

public class CampaignSponsorModel {
    private String campaignName;
    private String category;
    private String categoryColor;
    private String location;
    private String date;
    private int imageResId;

    // Cho chiến dịch đang tài trợ
    private String status;
    private String statusColor;
    private String organization;
    private String sponsoredAmount;
    private int progress;

    // Cho chiến dịch cần tài trợ
    private String group;
    private String targetAmount;
    private String beneficiaries;
    private String duration;
    private String raisedAmount;
    private String totalAmount;

    // Loại view: 0 = sponsored, 1 = need sponsor
    private int viewType;

    // Constructor cho chiến dịch đang tài trợ
    public CampaignSponsorModel(String campaignName, String category, String categoryColor,
                                String status, String statusColor, String organization,
                                String location, String date, String sponsoredAmount,
                                int progress, int imageResId) {
        this.campaignName = campaignName;
        this.category = category;
        this.categoryColor = categoryColor;
        this.status = status;
        this.statusColor = statusColor;
        this.organization = organization;
        this.location = location;
        this.date = date;
        this.sponsoredAmount = sponsoredAmount;
        this.progress = progress;
        this.imageResId = imageResId;
        this.viewType = 0; // Sponsored
    }

    // Constructor cho chiến dịch cần tài trợ
    public CampaignSponsorModel(String campaignName, String category, String categoryColor,
                                String group, String location, String date, String targetAmount,
                                String beneficiaries, String duration, String raisedAmount,
                                String totalAmount, int progress, int imageResId) {
        this.campaignName = campaignName;
        this.category = category;
        this.categoryColor = categoryColor;
        this.group = group;
        this.location = location;
        this.date = date;
        this.targetAmount = targetAmount;
        this.beneficiaries = beneficiaries;
        this.duration = duration;
        this.raisedAmount = raisedAmount;
        this.totalAmount = totalAmount;
        this.progress = progress;
        this.imageResId = imageResId;
        this.viewType = 1; // Need sponsor
    }

    // Getters and Setters
    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusColor() { return statusColor; }
    public void setStatusColor(String statusColor) { this.statusColor = statusColor; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getSponsoredAmount() { return sponsoredAmount; }
    public void setSponsoredAmount(String sponsoredAmount) { this.sponsoredAmount = sponsoredAmount; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getTargetAmount() { return targetAmount; }
    public void setTargetAmount(String targetAmount) { this.targetAmount = targetAmount; }

    public String getBeneficiaries() { return beneficiaries; }
    public void setBeneficiaries(String beneficiaries) { this.beneficiaries = beneficiaries; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getRaisedAmount() { return raisedAmount; }
    public void setRaisedAmount(String raisedAmount) { this.raisedAmount = raisedAmount; }

    public String getTotalAmount() { return totalAmount; }
    public void setTotalAmount(String totalAmount) { this.totalAmount = totalAmount; }

    public int getViewType() { return viewType; }
    public void setViewType(int viewType) { this.viewType = viewType; }
}