package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
@IgnoreExtraProperties
public class DonationItem implements Serializable {
    @DocumentId
    private String id;

    private String donationId;

    private String category;
    private int quantity;
    private ItemCondition condition;
    private String notes;

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
        this.condition = condition;
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

    public ItemCondition getCondition() { return condition; }
    public void setCondition(ItemCondition condition) { this.condition = condition; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
