package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class User implements Serializable {
    @DocumentId
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String avatar;
    private String address;

    @PropertyName("role")
    private UserRole role; // "VOLUNTEER", "SPONSOR", "ORGANIZATION"

    private String organizationId;

    private int totalPoints;
    private int totalDonations;
    private int totalCampaigns;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    private boolean isActive;

    // Firebase requires no-argument constructor
    public User() {
        this.isActive = true;
    }

    public User(String email, String fullName, UserRole role) {
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.totalPoints = 0;
        this.totalDonations = 0;
        this.totalCampaigns = 0;
        this.isActive = true;
    }

    public enum UserRole {
        VOLUNTEER("Tình nguyện viên"),
        SPONSOR("Nhà tài trợ"),
        ORGANIZATION("Tổ chức");

        private String displayName;

        UserRole(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    // ← GETTER/SETTER CHO organizationId
    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getTotalDonations() {
        return totalDonations;
    }

    public void setTotalDonations(int totalDonations) {
        this.totalDonations = totalDonations;
    }

    public int getTotalCampaigns() {
        return totalCampaigns;
    }

    public void setTotalCampaigns(int totalCampaigns) {
        this.totalCampaigns = totalCampaigns;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void touchUpdatedAt() {
        this.updatedAt = new Date();
    }
}