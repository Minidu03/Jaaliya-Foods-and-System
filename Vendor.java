package com.slginventory.model;

/**
 * Represents a vendor entity in the system.
 * Each vendor is associated with a user account.
 */
public class Vendor {
    private final int id;
    private final int userId;
    private final String name;
    private final String phone;

    public Vendor(int id, int userId, String name, String phone) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vendor vendor = (Vendor) o;
        return id == vendor.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
