package com.example.luzonfinalproject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Post {
    private String postId;
    private String title;
    private String description;
    private String category;
    private String authorName;
    private String contactInfo;
    private String timestamp;
    private String imageUrl;

    // Required empty constructor for Firebase
    public Post() {

    }


    public Post(String title, String description, String category,
                String authorName, String contactInfo, String imageUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.authorName = authorName;
        this.contactInfo = contactInfo;
        this.imageUrl = imageUrl;
        this.timestamp = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(new Date());
    }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}