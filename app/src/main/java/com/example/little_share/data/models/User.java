package com.example.little_share.data.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @SerializedName("id")
    private String id;

    @ColumnInfo(name="email")
    @SerializedName("email")
    private String email;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "full_name")
    @SerializedName("full_name")
    private String fullName;

    @ColumnInfo(name = "phone")
    @SerializedName("phone")
    private String phone;

    @ColumnInfo(name = "address")
    @SerializedName("address")
    private String address;

    @ColumnInfo(name = "avatar_url")
    @SerializedName("avatar_url")
    private String avatarUrl;

    @ColumnInfo(name = "user_type")
    @SerializedName("user_type")
    private String userType;

    @ColumnInfo(name = "total_points")
    @SerializedName("total_points")
    private int totalPoints;

    @ColumnInfo(name = "total_donations")
    @SerializedName("total_donations")
    private int totalDonations;

    @ColumnInfo(name = "total_campaigns")
    @SerializedName("total_campaigns")
    private int totalCampaigns;

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    private Date updatedAt;

    public User() {}

    public User(String id, String email, String fullName, String userType) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
        this.totalPoints = 0;
        this.totalDonations = 0;
        this.totalCampaigns = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getTotalDonations() { return totalDonations; }
    public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

    public int getTotalCampaigns() { return totalCampaigns; }
    public void setTotalCampaigns(int totalCampaigns) { this.totalCampaigns = totalCampaigns; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
