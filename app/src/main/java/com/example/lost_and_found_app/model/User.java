package com.example.lost_and_found_app.model;

public class User {
    public String userId;
    public String name;
    public String email;
    public String gender;
    public String phone;
    public String dob;
    public String profileImageUrl;
    public String password;

    public static User currentUser;

    public User() {
        // Empty constructor needed for Firebase
    }

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.gender = null;
        this.phone = null;
        this.dob = null;
        this.profileImageUrl = null;
        this.password = null;
    }
}
