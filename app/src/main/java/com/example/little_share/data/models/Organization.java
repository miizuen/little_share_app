package com.example.little_share.data.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity(tableName = "organizations")
public class Organization {
    @PrimaryKey
    @SerializedName("id")
    private String id;

    @ColumnInfo(name = "name")
    @SerializedName("name")
    private String name;

    @ColumnInfo(name = "email")
    @SerializedName("email")
    private String email;

    @ColumnInfo(name = "phone")
    @SerializedName("phone")
    private String phone;

    @ColumnInfo(name = "address")
    @SerializedName("address")
    private String address;

    @ColumnInfo(name = "description")
    @SerializedName("description")
    private String description;

    @ColumnInfo(name = "logo_url")
    @SerializedName("logo_url")
    private String logoUrl;

    @ColumnInfo(name = "bank_account")
    @SerializedName("bank_account")
    private String bankAccount;

    @ColumnInfo(name = "bank_name")
    @SerializedName("bank_name")
    private String bankName;

    @ColumnInfo(name = "operating_hours")
    @SerializedName("operating_hours")
    private String operatingHours;

    @ColumnInfo(name = "total_volunteers")
    @SerializedName("total_volunteers")
    private int totalVolunteers;

    @ColumnInfo(name = "total_campaigns")
    @SerializedName("total_campaigns")
    private int totalCampaigns;

    @ColumnInfo(name = "total_sponsors")
    @SerializedName("total_sponsors")
    private int totalSponsors;

    @ColumnInfo(name = "total_points_given")
    @SerializedName("total_points_given")
    private int totalPointsGiven;

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    private Date updatedAt;

    // Constructors
    public Organization() {}

    public Organization(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.totalVolunteers = 0;
        this.totalCampaigns = 0;
        this.totalSponsors = 0;
        this.totalPointsGiven = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }

    public int getTotalVolunteers() { return totalVolunteers; }
    public void setTotalVolunteers(int totalVolunteers) { this.totalVolunteers = totalVolunteers; }

    public int getTotalCampaigns() { return totalCampaigns; }
    public void setTotalCampaigns(int totalCampaigns) { this.totalCampaigns = totalCampaigns; }

    public int getTotalSponsors() { return totalSponsors; }
    public void setTotalSponsors(int totalSponsors) { this.totalSponsors = totalSponsors; }

    public int getTotalPointsGiven() { return totalPointsGiven; }
    public void setTotalPointsGiven(int totalPointsGiven) { this.totalPointsGiven = totalPointsGiven; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }


}
