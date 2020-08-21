package com.example.journal.Model;

import com.google.firebase.Timestamp;

public class Journal {
    private String title;
    private String thought;
    private  String imageUrl;
    private com.google.firebase.Timestamp timeAdded;
    private String Username;
    private String UserId;

    public Journal(){

    }

    public Journal(String title, String thought, String imageUrl, com.google.firebase.Timestamp timeAdded, String username,String userId) {
        this.title = title;
        this.thought = thought;
        this.imageUrl = imageUrl;
        this.timeAdded = timeAdded;
        Username = username;
        UserId=userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThought() {
        return thought;
    }

    public void setThought(String thought) {
        this.thought = thought;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public com.google.firebase.Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }
    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}
