package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
@IgnoreExtraProperties
public class FinancialReport implements Serializable {
    @DocumentId
    private String id;

    private String campaignId;

    private String campaignName;

    private String organizationId;
    private String description;

    private double totalExpense;

    private int totalVolunteers;

    private int averagePoints;

    @ServerTimestamp
    private Date reportDate;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;
    private List<ReportExpense> expenses;
    private List<ReportImage> images;

    // Constructors
    public FinancialReport() {}

    public FinancialReport(String campaignId, String organizationId) {
        this.campaignId = campaignId;
        this.organizationId = organizationId;
        this.totalExpense = 0;
        this.reportDate = new Date();
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCampaignId() { return campaignId; }
    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }

    public int getTotalVolunteers() { return totalVolunteers; }
    public void setTotalVolunteers(int totalVolunteers) { this.totalVolunteers = totalVolunteers; }

    public int getAveragePoints() { return averagePoints; }
    public void setAveragePoints(int averagePoints) { this.averagePoints = averagePoints; }

    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public List<ReportExpense> getExpenses() { return expenses; }
    public void setExpenses(List<ReportExpense> expenses) { this.expenses = expenses; }

    public List<ReportImage> getImages() { return images; }
    public void setImages(List<ReportImage> images) { this.images = images; }
}
