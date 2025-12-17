package com.example.little_share.data.models;



import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class Organization implements Serializable {
    @DocumentId
    private String id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private String logo;
    private String workingHours;
    private String accountNumber;
    private int totalCampaigns;
    private int totalVolunteers;
    private int totalPointsGiven;
    @ServerTimestamp
    private Date createdAt;
    @ServerTimestamp
    private Date updatedAt;

    // Constructors
    public Organization() {}

    public Organization(String id, String name, String address, String phone, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.totalCampaigns = 0;
        this.totalVolunteers = 0;
        this.totalPointsGiven = 0;
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getWorkingHours() { return workingHours; }
    public void setWorkingHours(String workingHours) { this.workingHours = workingHours; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public int getTotalCampaigns() { return totalCampaigns; }
    public void setTotalCampaigns(int totalCampaigns) { this.totalCampaigns = totalCampaigns; }

    public int getTotalVolunteers() { return totalVolunteers; }
    public void setTotalVolunteers(int totalVolunteers) { this.totalVolunteers = totalVolunteers; }

    public int getTotalPointsGiven() { return totalPointsGiven; }
    public void setTotalPointsGiven(int totalPointsGiven) { this.totalPointsGiven = totalPointsGiven; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
