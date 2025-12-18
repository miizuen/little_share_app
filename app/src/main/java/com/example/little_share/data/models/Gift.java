package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class Gift implements Serializable {
    @DocumentId
    private String id;

    private String name;
    private String category;
    private String description;

    private String imageUrl;

    private int pointsRequired;

    private int totalQuantity;

    private int availableQuantity;

    private String pickupLocation;

    private String organizationId;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    // Firebase requires no-argument constructor
    public Gift() {
        this.availableQuantity = 0;
    }

    public Gift(String name, String category, int pointsRequired, int totalQuantity) {
        this.name = name;
        this.category = category;
        this.pointsRequired = pointsRequired;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }


    public int getPointsRequired() { return pointsRequired; }

    public void setPointsRequired(int pointsRequired) { this.pointsRequired = pointsRequired; }

    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }

    public int getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity = availableQuantity; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isAvailable() {
        return availableQuantity > 0;
    }

    public String getAvailabilityText() {
        return availableQuantity + "/" + totalQuantity;
    }
}
