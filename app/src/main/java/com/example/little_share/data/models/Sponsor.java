package com.example.little_share.data.models;


import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class Sponsor implements Serializable {
    @DocumentId
    private String id;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String address;
    private double totalDonated;
    private int totalCampaignsSponsored;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    // Constructors
    public Sponsor() {}

    public Sponsor(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.totalDonated = 0;
        this.totalCampaignsSponsored = 0;
        this.createdAt = new Date();
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

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getTotalDonated() { return totalDonated; }
    public void setTotalDonated(double totalDonated) { this.totalDonated = totalDonated; }

    public int getTotalCampaignsSponsored() { return totalCampaignsSponsored; }
    public void setTotalCampaignsSponsored(int totalCampaignsSponsored) {
        this.totalCampaignsSponsored = totalCampaignsSponsored;
    }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
