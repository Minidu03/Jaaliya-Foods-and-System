package com.slginventory.ui;

import com.slginventory.db.Database;
import com.slginventory.model.DistributionCenter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CenterManagementService {
    
    // Default center information
    private static final Map<String, DistributionCenter> DEFAULT_CENTERS = new HashMap<>();
    
    static {
        DEFAULT_CENTERS.put("PETTAH", new DistributionCenter("PETTAH", "Pettah Market", "Colombo", "Main Colombo distribution center"));
        DEFAULT_CENTERS.put("PELIYAGODA", new DistributionCenter("PELIYAGODA", "Peliyagoda Market", "Colombo", "New Manning Market"));
        DEFAULT_CENTERS.put("DAMBULLA", new DistributionCenter("DAMBULLA", "Dambulla Center", "Central Province", "Central Province Hub"));
        DEFAULT_CENTERS.put("KANDY", new DistributionCenter("KANDY", "Kandy Center", "Kandy", "Kandy distribution center"));
        DEFAULT_CENTERS.put("GALLE", new DistributionCenter("GALLE", "Galle Center", "Galle", "Southern Province center"));
        DEFAULT_CENTERS.put("JAFFNA", new DistributionCenter("JAFFNA", "Jaffna Center", "Jaffna", "Northern Province center"));
    }
    
    /**
     * Get all distribution centers with inventory statistics
     */
    public static List<CenterInfo> getAllCenters() throws SQLException {
        List<CenterInfo> centers = new ArrayList<>();
        
        // Get all unique center codes from inventory
        Map<String, CenterInfo> centerMap = new HashMap<>();
        
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT DISTINCT center_code
                 FROM center_inventory
                 ORDER BY center_code
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("center_code");
                    DistributionCenter dc = DEFAULT_CENTERS.getOrDefault(code, 
                        new DistributionCenter(code, code, "Unknown", "Distribution center"));
                    centerMap.put(code, new CenterInfo(dc));
                }
            }
        }
        
        // Add default centers that might not have inventory yet
        for (Map.Entry<String, DistributionCenter> entry : DEFAULT_CENTERS.entrySet()) {
            if (!centerMap.containsKey(entry.getKey())) {
                centerMap.put(entry.getKey(), new CenterInfo(entry.getValue()));
            }
        }
        
        // Get inventory statistics for each center
        for (CenterInfo center : centerMap.values()) {
            try (Connection conn = Database.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement("""
                     SELECT 
                         COUNT(DISTINCT goods_id) as item_count,
                         COALESCE(SUM(quantity), 0) as total_quantity
                     FROM center_inventory
                     WHERE center_code = ?
                 """)) {
                ps.setString(1, center.getCode());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        center.setItemCount(rs.getInt("item_count"));
                        center.setTotalQuantity(rs.getInt("total_quantity"));
                    }
                }
            }
        }
        
        centers.addAll(centerMap.values());
        centers.sort((a, b) -> a.getCode().compareTo(b.getCode()));
        return centers;
    }
    
    /**
     * Add a new distribution center
     */
    public static void addCenter(String code, String name, String location, String description) throws SQLException {
        // Validate code format (uppercase, no spaces)
        String cleanCode = code.toUpperCase().trim().replaceAll("\\s+", "_");
        
        // Check if center already exists in inventory
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) as count FROM center_inventory WHERE center_code = ?")) {
            ps.setString(1, cleanCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("count") > 0) {
                    throw new SQLException("Center code already exists: " + cleanCode);
                }
            }
        }
        
        // Add to default centers map
        DEFAULT_CENTERS.put(cleanCode, new DistributionCenter(cleanCode, name, location, description));
    }
    
    /**
     * Update center information
     */
    public static void updateCenter(String code, String name, String location, String description) throws SQLException {
        DistributionCenter dc = DEFAULT_CENTERS.get(code);
        if (dc != null) {
            dc.setName(name);
            dc.setLocation(location);
            dc.setDescription(description);
        } else {
            DEFAULT_CENTERS.put(code, new DistributionCenter(code, name, location, description));
        }
    }
    
    /**
     * Delete center (removes all inventory)
     */
    public static void deleteCenter(String code) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM center_inventory WHERE center_code = ?")) {
            ps.setString(1, code);
            ps.executeUpdate();
        }
        DEFAULT_CENTERS.remove(code);
    }
    
    /**
     * Get center by code
     */
    public static CenterInfo getCenterByCode(String code) throws SQLException {
        DistributionCenter dc = DEFAULT_CENTERS.get(code);
        if (dc == null) {
            dc = new DistributionCenter(code, code, "Unknown", "Distribution center");
        }
        
        CenterInfo info = new CenterInfo(dc);
        
        // Get inventory statistics
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT 
                     COUNT(DISTINCT goods_id) as item_count,
                     COALESCE(SUM(quantity), 0) as total_quantity
                 FROM center_inventory
                 WHERE center_code = ?
             """)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    info.setItemCount(rs.getInt("item_count"));
                    info.setTotalQuantity(rs.getInt("total_quantity"));
                }
            }
        }
        
        return info;
    }
    
    public static class CenterInfo {
        private final String code;
        private String name;
        private String location;
        private String description;
        private int itemCount;
        private int totalQuantity;
        
        public CenterInfo(DistributionCenter dc) {
            this.code = dc.getCode();
            this.name = dc.getName();
            this.location = dc.getLocation();
            this.description = dc.getDescription();
        }
        
        public String getCode() { return code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getItemCount() { return itemCount; }
        public void setItemCount(int itemCount) { this.itemCount = itemCount; }
        public int getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    }
}

