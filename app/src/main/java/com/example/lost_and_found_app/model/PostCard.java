package com.example.lost_and_found_app.model;

import com.example.lost_and_found_app.util.ISO8601DateAdapter;
import com.google.gson.annotations.lost_and_found_app;
import com.google.gson.annotations.lost_and_found_app;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostCard {
    @SerializedName("postId") private String postId;
    @SerializedName("title") private String title;
    @SerializedName("description") private String description;
    @SerializedName("location") private String location;
    @SerializedName("email") private String email;
    @SerializedName("phoneNumber") private String phoneNumber;
    @SerializedName("reward") private String reward;
    @SerializedName("postDate") private String postDate;
    @SerializedName("postTime") private String postTime;

    @SerializedName("createdDate") @JsonAdapter(ISO8601DateAdapter.class)
    private Date createdDate;

    public PostCard(String postId, String title, String description, String location, String email, String phoneNumber, String reward, String postDate, String postTime, Date createdDate) {
        this.postId = postId;
        this.title = title;
        this.description = description;;
        this.location = location;;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.reward = reward;
        this.postDate = postDate;
        this.postTime = postTime;
        this.createdDate = createdDate != null ? createdDate : new Date();
    }

    public String getPostId() { return postId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getReward() { return reward; }
    public String getPostDate() { return postDate; }
    public String getPostTime() { return postTime; }
    public String getCreatedDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(createdDate);
    }

    @Override
    public String toString() {
        return String.format("Card{postId=%s, title=%s, description=%s, location=%s, email=%s, phoneNumber=%s, reward=%s, postDate=%s, postTime=%s, createdDate=%s}",
                postId, title, description, location, email, phoneNumber, reward, postDate, postTime, getCreatedDate());
    }
}
