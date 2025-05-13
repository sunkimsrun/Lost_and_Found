package com.example.lost_and_found_app.model;

import com.example.lost_and_found_app.util.ISO8601DateAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostCard {

    @SerializedName("imageUrl")
    private String imageUrl;
    @SerializedName("postId")
    private String postId;
    @SerializedName("title")
    private String title;
    @SerializedName("information")
    private String information;
    @SerializedName("email")
    private String email;
    @SerializedName("phone")
    private String phone;
    @SerializedName("reward")
    private String reward;

    @SerializedName("status")
    private String status;
    @SerializedName("date")
    private String date;
    @SerializedName("postTime")
    private String postTime;

    @SerializedName("createdDate")
    @JsonAdapter(ISO8601DateAdapter.class)
    private Date createdDate;

    public PostCard() {
    }

    public PostCard(String imageUrl, String postId, String title, String information, String email, String phone, String reward, String date, String postTime, Date createdDate, String status) {
        this.imageUrl = imageUrl;
        this.postId = postId;
        this.title = title;
        this.information = information;
        this.email = email;
        this.phone = phone;
        this.reward = reward;
        this.date = date;
        this.postTime = postTime;
        this.createdDate = createdDate != null ? createdDate : new Date();
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getCreatedDate() {
        return createdDate != null
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(createdDate)
                : "Unknown";
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return String.format("Card{ imageUrl=%s ,postId=%s, title=%s, information=%s, email=%s, phoneNumber=%s, reward=%s, date=%s, postTime=%s, status=%s,createdDate=%s}",
                imageUrl, postId, title, information, email, phone, reward, date, postTime, status, getCreatedDate());
    }
}
