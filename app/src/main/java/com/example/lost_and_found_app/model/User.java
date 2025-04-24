package com.example.lost_and_found_app.model;

public class User {
    public static User currentUser = new User(
            "Sok Sochetra", "soksochetra@gmail.com", "Male", "012345678", "12 - 10 - 2004", "11111"
    );

    public String name;
    public String gender;
    public String email;
    public String phone;
    public String dob;
    public String password;

    public User(String name, String email, String gender, String phone, String dob, String password) {
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.phone = phone;
        this.dob = dob;
        this.password = password;
    }
}
