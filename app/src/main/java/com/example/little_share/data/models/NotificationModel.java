package com.example.little_share.data.models;

public class NotificationModel {
    private String title;
    private String description;
    private String time;
    private int iconResId;
    private  boolean isRead;

    public NotificationModel(String title, String description, String time, int iconResId) {
        this.title = title;
        this.description = description;
        this.time = time;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public boolean isRead() {
               return isRead;
    }
    public void setRead(boolean read) {
        isRead = read;
    }
}
