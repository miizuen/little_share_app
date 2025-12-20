package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class DonationItem implements Serializable {
    @DocumentId
    private String id;
    private String donationId;
    private String category;
    private int quantity;
    private String condition;
    private String notes;

    @ServerTimestamp
    private Date createdAt;

    public enum ItemCondition {
        NEW("Mới"),
        GOOD("Tốt"),
        FAIR("Khá"),
        ACCEPTABLE("Ổn");

        private String displayName;

        ItemCondition(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Constructors
    public DonationItem() {}

    public DonationItem(String donationId, String category, int quantity, ItemCondition condition) {
        this.donationId = donationId;
        this.category = category;
        this.quantity = quantity;
        this.condition = condition.name();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDonationId() { return donationId; }
    public void setDonationId(String donationId) { this.donationId = donationId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }


    public String getCondition() { return condition; }

    public void setCondition(ItemCondition condition) {
        this.condition = condition.name();
    }

    public ItemCondition getConditionEnum() {
        try {
            return ItemCondition.valueOf(condition);
        } catch (Exception e) {
            return ItemCondition.ACCEPTABLE;  // Default
        }
    }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}