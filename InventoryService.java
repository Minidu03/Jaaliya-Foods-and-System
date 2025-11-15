package com.slginventory.ui;

import com.slginventory.db.Database;
import com.slginventory.model.Goods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    
    /**
     * Get or create a goods item by name
     * @return the goods ID
     */
    public static int getOrCreateGoods(String goodsName) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection()) {
            // First, try to find existing goods
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM goods WHERE name = ?")) {
                ps.setString(1, goodsName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }
            
            // If not found, create new goods
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO goods (name) VALUES (?)",
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, goodsName);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to get or create goods");
    }
    
    /**
     * Get vendor ID for a user
     */
    public static int getVendorId(int userId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM vendors WHERE user_id = ? LIMIT 1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        throw new SQLException("Vendor not found for user ID: " + userId);
    }
    
    /**
     * Add or update vendor inventory
     */
    public static void addVendorInventory(int vendorId, int goodsId, int quantity) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection()) {
            // Check if inventory already exists
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, quantity FROM vendor_inventory WHERE vendor_id = ? AND goods_id = ?")) {
                ps.setInt(1, vendorId);
                ps.setInt(2, goodsId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Update existing inventory
                        int existingQty = rs.getInt("quantity");
                        int newQty = existingQty + quantity;
                        try (PreparedStatement updatePs = conn.prepareStatement(
                                "UPDATE vendor_inventory SET quantity = ? WHERE vendor_id = ? AND goods_id = ?")) {
                            updatePs.setInt(1, newQty);
                            updatePs.setInt(2, vendorId);
                            updatePs.setInt(3, goodsId);
                            updatePs.executeUpdate();
                        }
                    } else {
                        // Insert new inventory
                        try (PreparedStatement insertPs = conn.prepareStatement(
                                "INSERT INTO vendor_inventory (vendor_id, goods_id, quantity) VALUES (?, ?, ?)")) {
                            insertPs.setInt(1, vendorId);
                            insertPs.setInt(2, goodsId);
                            insertPs.setInt(3, quantity);
                            insertPs.executeUpdate();
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Add or update center inventory
     */
    public static void addCenterInventory(String centerCode, int goodsId, int quantity) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection()) {
            // Check if inventory already exists
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, quantity FROM center_inventory WHERE center_code = ? AND goods_id = ?")) {
                ps.setString(1, centerCode);
                ps.setInt(2, goodsId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Update existing inventory
                        int existingQty = rs.getInt("quantity");
                        int newQty = existingQty + quantity;
                        try (PreparedStatement updatePs = conn.prepareStatement(
                                "UPDATE center_inventory SET quantity = ? WHERE center_code = ? AND goods_id = ?")) {
                            updatePs.setInt(1, newQty);
                            updatePs.setString(2, centerCode);
                            updatePs.setInt(3, goodsId);
                            updatePs.executeUpdate();
                        }
                    } else {
                        // Insert new inventory
                        try (PreparedStatement insertPs = conn.prepareStatement(
                                "INSERT INTO center_inventory (center_code, goods_id, quantity) VALUES (?, ?, ?)")) {
                            insertPs.setString(1, centerCode);
                            insertPs.setInt(2, goodsId);
                            insertPs.setInt(3, quantity);
                            insertPs.executeUpdate();
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Get all available goods
     */
    public static List<Goods> getAllGoods() throws SQLException {
        List<Goods> goodsList = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM goods ORDER BY name")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    goodsList.add(new Goods(rs.getInt("id"), rs.getString("name")));
                }
            }
        }
        return goodsList;
    }
    
    /**
     * Get all distribution center codes
     */
    public static List<String> getAllCenters() {
        List<String> centers = new ArrayList<>();
        centers.add("PETTAH");
        centers.add("PELIYAGODA");
        centers.add("DAMBULLA");
        centers.add("KANDY");
        centers.add("GALLE");
        centers.add("JAFFNA");
        return centers;
    }
}

