package com.example.project2025.Models;

public class UserList {
    private String username, email, UID;
    public UserList(String username, String email, String UID) {
        this.username = username;
        this.email = email;
        this.UID = UID;
    }

    // Getter methods
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUID() {
        return UID;
    }

    // Setter methods
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    @Override
    public String toString() {
        return "UserList{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", UID='" + UID + '\'' +
                '}';
    }

}
