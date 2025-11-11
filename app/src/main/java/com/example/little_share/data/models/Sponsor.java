package com.example.little_share.data.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity(tableName = "sponsors")
public class Sponsor {
    @PrimaryKey
    @SerializedName("id")
    private String id;

    @ColumnInfo(name = "email")
    @SerializedName("email")
    private String email;

    @ColumnInfo(name = "full_name")
    @SerializedName("full_name")
    private String fullName;

    @ColumnInfo(name = "company_name")
    @SerializedName("company_name")
    private String companyName;

    @ColumnInfo(name = "phone")
    @SerializedName("phone")
    private String phone;

    @ColumnInfo(name = "address")
    @SerializedName("address")
    private String address;

    @ColumnInfo(name = "avatar_url")
    @SerializedName("avatar_url")
    private String avatarUrl;

    @ColumnInfo(name = "total_donated")
    @SerializedName("total_donated")
    private long totalDonated;

    @ColumnInfo(name = "total_campaigns_sponsored")
    @SerializedName("total_campaigns_sponsored")
    private int totalCampaignsSponsored;

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    private Date updatedAt;

    // Constructors
    public Sponsor() {}

    public Sponsor(String id, String email, String fullName) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.totalDonated = 0;
        this.totalCampaignsSponsored = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public long getTotalDonated() { return totalDonated; }
    public void setTotalDonated(long totalDonated) { this.totalDonated = totalDonated; }

    public int getTotalCampaignsSponsored() { return totalCampaignsSponsored; }
    public void setTotalCampaignsSponsored(int totalCampaignsSponsored) {
        this.totalCampaignsSponsored = totalCampaignsSponsored;
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

}
