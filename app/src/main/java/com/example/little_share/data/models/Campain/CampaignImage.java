package com.example.little_share.data.models.Campain;

import java.io.Serializable;
import java.util.Date;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;
import com.google.firebase.firestore.ServerTimestamp;
@IgnoreExtraProperties
public class CampaignImage implements Serializable {
    @DocumentId
    private String id;

    private String campaignId;

    private String imageUrl;
    private String caption;
    @ServerTimestamp
    private Date uploadedAt;
    private String uploadedBy;

    public CampaignImage() {}

    public CampaignImage(String campaignId, String imageUrl) {
        this.campaignId = campaignId;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }


    public String getCampaignId() { return campaignId; }

    public void setCampaignId(String campaignId) { this.campaignId = campaignId; }


    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }


    public Date getUploadedAt() { return uploadedAt; }

    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }


    public String getUploadedBy() { return uploadedBy; }

    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}

