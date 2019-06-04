package com.lancy.bookreview;

public class User {
    public String username;
    public String email;
    public String token;
    public String region;

    public User() {}

    public User(String username, String email, String token, String region) {
        this.username = username;
        this.email = email;
        this.token = token;
        this.region = region;
    }
}
