package com.example.lost_and_found_app.model;

public class User {
    public String userId, username, email, gender, phoneNumber;

    public User() {
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
