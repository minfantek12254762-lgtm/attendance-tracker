package com.example.attendance_tracker;

public class UserHelper {
    public String fullName, username, password, role;

    public UserHelper() {}

    public UserHelper(String fullName, String username, String password, String role) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
