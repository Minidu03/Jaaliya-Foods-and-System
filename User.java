package com.slginventory.model;

public class User {
    private final int id;
    private final String username;
    private final String company;

    public User(int id, String username, String company) {
        this.id = id;
        this.username = username;
        this.company = company;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getCompany() { return company; }
}