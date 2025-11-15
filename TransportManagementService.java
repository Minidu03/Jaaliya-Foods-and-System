package com.slginventory.ui;

import com.slginventory.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransportManagementService {
    
    /**
     * Get all vehicles
     */
    public static List<VehicleInfo> getAllVehicles() throws SQLException {
        List<VehicleInfo> vehicles = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT id, vehicle_number, vehicle_type, capacity_kg, driver_name, driver_phone, status
                 FROM vehicles
                 ORDER BY vehicle_number
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(new VehicleInfo(
                        rs.getInt("id"),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        rs.getInt("capacity_kg"),
                        rs.getString("driver_name"),
                        rs.getString("driver_phone"),
                        rs.getString("status")
                    ));
                }
            }
        }
        return vehicles;
    }
    
    /**
     * Get vehicle by ID
     */
    public static VehicleInfo getVehicleById(int vehicleId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT id, vehicle_number, vehicle_type, capacity_kg, driver_name, driver_phone, status
                 FROM vehicles
                 WHERE id = ?
             """)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new VehicleInfo(
                        rs.getInt("id"),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        rs.getInt("capacity_kg"),
                        rs.getString("driver_name"),
                        rs.getString("driver_phone"),
                        rs.getString("status")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Get available vehicles (status = 'AVAILABLE')
     */
    public static List<VehicleInfo> getAvailableVehicles() throws SQLException {
        List<VehicleInfo> vehicles = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT id, vehicle_number, vehicle_type, capacity_kg, driver_name, driver_phone, status
                 FROM vehicles
                 WHERE status = 'AVAILABLE'
                 ORDER BY vehicle_number
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehicles.add(new VehicleInfo(
                        rs.getInt("id"),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        rs.getInt("capacity_kg"),
                        rs.getString("driver_name"),
                        rs.getString("driver_phone"),
                        rs.getString("status")
                    ));
                }
            }
        }
        return vehicles;
    }
    
    /**
     * Add new vehicle
     */
    public static void addVehicle(String vehicleNumber, String vehicleType, int capacityKg, 
                                  String driverName, String driverPhone, String status) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 INSERT INTO vehicles (vehicle_number, vehicle_type, capacity_kg, driver_name, driver_phone, status)
                 VALUES (?, ?, ?, ?, ?, ?)
             """)) {
            ps.setString(1, vehicleNumber);
            ps.setString(2, vehicleType);
            ps.setInt(3, capacityKg);
            ps.setString(4, driverName);
            ps.setString(5, driverPhone);
            ps.setString(6, status);
            ps.executeUpdate();
        }
    }
    
    /**
     * Update vehicle
     */
    public static void updateVehicle(int vehicleId, String vehicleNumber, String vehicleType, 
                                    int capacityKg, String driverName, String driverPhone, String status) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 UPDATE vehicles 
                 SET vehicle_number = ?, vehicle_type = ?, capacity_kg = ?, driver_name = ?, driver_phone = ?, status = ?
                 WHERE id = ?
             """)) {
            ps.setString(1, vehicleNumber);
            ps.setString(2, vehicleType);
            ps.setInt(3, capacityKg);
            ps.setString(4, driverName);
            ps.setString(5, driverPhone);
            ps.setString(6, status);
            ps.setInt(7, vehicleId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Delete vehicle
     */
    public static void deleteVehicle(int vehicleId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM vehicles WHERE id = ?")) {
            ps.setInt(1, vehicleId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Get all transports with vehicle and goods information
     */
    public static List<TransportInfo> getAllTransports() throws SQLException {
        List<TransportInfo> transports = new ArrayList<>();
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT t.id, t.vehicle_id, v.vehicle_number, t.source_center, t.destination_center,
                        t.goods_id, g.name as goods_name, t.quantity, t.status,
                        t.scheduled_date, t.departure_date, t.arrival_date, t.notes
                 FROM transports t
                 LEFT JOIN vehicles v ON t.vehicle_id = v.id
                 LEFT JOIN goods g ON t.goods_id = g.id
                 ORDER BY t.scheduled_date DESC, t.id DESC
             """)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transports.add(new TransportInfo(
                        rs.getInt("id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("vehicle_number"),
                        rs.getString("source_center"),
                        rs.getString("destination_center"),
                        rs.getInt("goods_id"),
                        rs.getString("goods_name"),
                        rs.getInt("quantity"),
                        rs.getString("status"),
                        rs.getString("scheduled_date") != null ? LocalDateTime.parse(rs.getString("scheduled_date").replace(" ", "T")) : null,
                        rs.getString("departure_date") != null ? LocalDateTime.parse(rs.getString("departure_date").replace(" ", "T")) : null,
                        rs.getString("arrival_date") != null ? LocalDateTime.parse(rs.getString("arrival_date").replace(" ", "T")) : null,
                        rs.getString("notes")
                    ));
                }
            }
        }
        return transports;
    }
    
    /**
     * Get transport by ID
     */
    public static TransportInfo getTransportById(int transportId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT t.id, t.vehicle_id, v.vehicle_number, t.source_center, t.destination_center,
                        t.goods_id, g.name as goods_name, t.quantity, t.status,
                        t.scheduled_date, t.departure_date, t.arrival_date, t.notes
                 FROM transports t
                 LEFT JOIN vehicles v ON t.vehicle_id = v.id
                 LEFT JOIN goods g ON t.goods_id = g.id
                 WHERE t.id = ?
             """)) {
            ps.setInt(1, transportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TransportInfo(
                        rs.getInt("id"),
                        rs.getInt("vehicle_id"),
                        rs.getString("vehicle_number"),
                        rs.getString("source_center"),
                        rs.getString("destination_center"),
                        rs.getInt("goods_id"),
                        rs.getString("goods_name"),
                        rs.getInt("quantity"),
                        rs.getString("status"),
                        rs.getString("scheduled_date") != null ? LocalDateTime.parse(rs.getString("scheduled_date").replace(" ", "T")) : null,
                        rs.getString("departure_date") != null ? LocalDateTime.parse(rs.getString("departure_date").replace(" ", "T")) : null,
                        rs.getString("arrival_date") != null ? LocalDateTime.parse(rs.getString("arrival_date").replace(" ", "T")) : null,
                        rs.getString("notes")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Create new transport
     */
    public static void createTransport(int vehicleId, String sourceCenter, String destinationCenter,
                                      int goodsId, int quantity, String status, LocalDateTime scheduledDate,
                                      String notes) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 INSERT INTO transports (vehicle_id, source_center, destination_center, goods_id, quantity, status, scheduled_date, notes)
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?)
             """)) {
            ps.setInt(1, vehicleId);
            ps.setString(2, sourceCenter);
            ps.setString(3, destinationCenter);
            ps.setInt(4, goodsId);
            ps.setInt(5, quantity);
            ps.setString(6, status);
            ps.setObject(7, scheduledDate);
            ps.setString(8, notes);
            ps.executeUpdate();
        }
    }
    
    /**
     * Update transport status
     */
    public static void updateTransportStatus(int transportId, String status, LocalDateTime departureDate,
                                            LocalDateTime arrivalDate, String notes) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 UPDATE transports 
                 SET status = ?, departure_date = ?, arrival_date = ?, notes = ?
                 WHERE id = ?
             """)) {
            ps.setString(1, status);
            ps.setObject(2, departureDate);
            ps.setObject(3, arrivalDate);
            ps.setString(4, notes);
            ps.setInt(5, transportId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Update transport (full update)
     */
    public static void updateTransport(int transportId, int vehicleId, String sourceCenter, String destinationCenter,
                                      int goodsId, int quantity, String status, LocalDateTime scheduledDate,
                                      LocalDateTime departureDate, LocalDateTime arrivalDate, String notes) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 UPDATE transports 
                 SET vehicle_id = ?, source_center = ?, destination_center = ?, goods_id = ?, quantity = ?,
                     status = ?, scheduled_date = ?, departure_date = ?, arrival_date = ?, notes = ?
                 WHERE id = ?
             """)) {
            ps.setInt(1, vehicleId);
            ps.setString(2, sourceCenter);
            ps.setString(3, destinationCenter);
            ps.setInt(4, goodsId);
            ps.setInt(5, quantity);
            ps.setString(6, status);
            ps.setObject(7, scheduledDate);
            ps.setObject(8, departureDate);
            ps.setObject(9, arrivalDate);
            ps.setString(10, notes);
            ps.setInt(11, transportId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Delete transport
     */
    public static void deleteTransport(int transportId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM transports WHERE id = ?")) {
            ps.setInt(1, transportId);
            ps.executeUpdate();
        }
    }
    
    /**
     * Check if vehicle has active transports
     */
    public static boolean hasActiveTransports(int vehicleId) throws SQLException {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                 SELECT COUNT(*) as count FROM transports 
                 WHERE vehicle_id = ? AND status IN ('SCHEDULED', 'IN_TRANSIT')
             """)) {
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }
    
    public static class VehicleInfo {
        private final int id;
        private final String vehicleNumber;
        private final String vehicleType;
        private final int capacityKg;
        private final String driverName;
        private final String driverPhone;
        private final String status;
        
        public VehicleInfo(int id, String vehicleNumber, String vehicleType, int capacityKg,
                          String driverName, String driverPhone, String status) {
            this.id = id;
            this.vehicleNumber = vehicleNumber;
            this.vehicleType = vehicleType;
            this.capacityKg = capacityKg;
            this.driverName = driverName;
            this.driverPhone = driverPhone;
            this.status = status;
        }
        
        public int getId() { return id; }
        public String getVehicleNumber() { return vehicleNumber; }
        public String getVehicleType() { return vehicleType; }
        public int getCapacityKg() { return capacityKg; }
        public String getDriverName() { return driverName; }
        public String getDriverPhone() { return driverPhone; }
        public String getStatus() { return status; }
    }
    
    public static class TransportInfo {
        private final int id;
        private final int vehicleId;
        private final String vehicleNumber;
        private final String sourceCenter;
        private final String destinationCenter;
        private final int goodsId;
        private final String goodsName;
        private final int quantity;
        private final String status;
        private final LocalDateTime scheduledDate;
        private final LocalDateTime departureDate;
        private final LocalDateTime arrivalDate;
        private final String notes;
        
        public TransportInfo(int id, int vehicleId, String vehicleNumber, String sourceCenter,
                            String destinationCenter, int goodsId, String goodsName, int quantity,
                            String status, LocalDateTime scheduledDate, LocalDateTime departureDate,
                            LocalDateTime arrivalDate, String notes) {
            this.id = id;
            this.vehicleId = vehicleId;
            this.vehicleNumber = vehicleNumber;
            this.sourceCenter = sourceCenter;
            this.destinationCenter = destinationCenter;
            this.goodsId = goodsId;
            this.goodsName = goodsName;
            this.quantity = quantity;
            this.status = status;
            this.scheduledDate = scheduledDate;
            this.departureDate = departureDate;
            this.arrivalDate = arrivalDate;
            this.notes = notes;
        }
        
        public int getId() { return id; }
        public int getVehicleId() { return vehicleId; }
        public String getVehicleNumber() { return vehicleNumber; }
        public String getSourceCenter() { return sourceCenter; }
        public String getDestinationCenter() { return destinationCenter; }
        public int getGoodsId() { return goodsId; }
        public String getGoodsName() { return goodsName; }
        public int getQuantity() { return quantity; }
        public String getStatus() { return status; }
        public LocalDateTime getScheduledDate() { return scheduledDate; }
        public LocalDateTime getDepartureDate() { return departureDate; }
        public LocalDateTime getArrivalDate() { return arrivalDate; }
        public String getNotes() { return notes; }
    }
}

