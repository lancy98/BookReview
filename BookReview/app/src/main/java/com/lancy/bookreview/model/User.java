package com.lancy.bookreview.model;

public class User {
    public String username;
    public String email;
    public String token;
    public String region;
    public String image;
    public String userID;

    public User() {}

    public User(String username,
                String email,
                String token,
                String region) {
        this.username = username;
        this.email = email;
        this.token = token;
        this.region = region;
    }

    private static User currentUser = null;

    public static User getCurrentUser() {
        if (currentUser == null) {
            currentUser = new User();
        }

        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}
