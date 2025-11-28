package com.example.little_share.data.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
@IgnoreExtraProperties
public class Reward implements Serializable {
    @DocumentId
    private String id;
    private String userId;
    private String source; // Campaign, Donation, etc.
    private String sourceId;
    private int points;
    private String description;
    @ServerTimestamp
    private Date earnedDate;

    // Constructors
    public Reward() {}

    public Reward(String userId, String source, String sourceId, int points, String description) {
        this.userId = userId;
        this.source = source;
        this.sourceId = sourceId;
        this.points = points;
        this.description = description;
        this.earnedDate = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getEarnedDate() { return earnedDate; }
    public void setEarnedDate(Date earnedDate) { this.earnedDate = earnedDate; }
}
