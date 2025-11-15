package com.slginventory.ui;

import com.slginventory.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VendorManagementService {
    
    /**
     * Get all vendors with user information
     */
    public static List<VendorInfo> getAllVendors() throws SQLException {
        List<VendorInfo> vendors = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT v.id, v.user_id, v.name, v.phone, u.username, u.company
                 FROM vendors v
                 JOIN users u ON u.id = v.user_id
                 ORDER BY v.name
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vendors.add(new VendorInfo(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("username"),
                        rs.getString("company")
                    ));
                }
            }
        }
        return vendors;
    }
    
    /**
     * Get vendor by ID
     */
    public static VendorInfo getVendorById(int vendorId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT v.id, v.user_id, v.name, v.phone, u.username, u.company
                 FROM vendors v
                 JOIN users u ON u.id = v.user_id
                 WHERE v.id = ?
             """)) {
            ps.setInt(1, vendorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new VendorInfo(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("username"),
                        rs.getString("company")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Update vendor information
     */
    public static void updateVendor(int vendorId, String name, String phone) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE vendors SET name = ?, phone = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setInt(3, vendorId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Delete vendor (cascade will delete inventory)
     */
    public static void deleteVendor(int vendorId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM vendors WHERE id = ?")) {
            ps.setInt(1, vendorId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Get vendor inventory count
     */
    public static int getVendorInventoryCount(int vendorId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) as count FROM vendor_inventory WHERE vendor_id = ?")) {
            ps.setInt(1, vendorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }
    
    /**
     * Get total inventory quantity for vendor
     */
    public static int getVendorTotalQuantity(int vendorId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COALESCE(SUM(quantity), 0) as total FROM vendor_inventory WHERE vendor_id = ?")) {
            ps.setInt(1, vendorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }
    
    public static class VendorInfo {
        private final int id;
        private final int userId;
        private final String name;
        private final String phone;
        private final String username;
        private final String company;
        
        public VendorInfo(int id, int userId, String name, String phone, String username, String company) {
            this.id = id;
            this.userId = userId;
            this.name = name;
            this.phone = phone;
            this.username = username;
            this.company = company;
        }
        
        public int getId() { return id; }
        public int getUserId() { return userId; }
        public String getName() { return name; }
        public String getPhone() { return phone; }
        public String getUsername() { return username; }
        public String getCompany() { return company; }
    }
}

