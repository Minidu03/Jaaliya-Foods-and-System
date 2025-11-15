package com.slginventory.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final Database INSTANCE = new Database();
    private static final String JDBC_URL = "jdbc:sqlite:slg_inventory.db";

    public static Database getInstance() {
        return INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    public void initialize() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // Enable foreign key constraints
            st.execute("PRAGMA foreign_keys = ON");
            
            // Users table
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    company TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )""");
            
            // Vendors table
            st.execute("""
                CREATE TABLE IF NOT EXISTS vendors (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    phone TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )""");
            
            // Goods table
            st.execute("""
                CREATE TABLE IF NOT EXISTS goods (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL
                )""");
            
            // Vendor inventory
            st.execute("""
                CREATE TABLE IF NOT EXISTS vendor_inventory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    vendor_id INTEGER NOT NULL,
                    goods_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    FOREIGN KEY(vendor_id) REFERENCES vendors(id) ON DELETE CASCADE,
                    FOREIGN KEY(goods_id) REFERENCES goods(id) ON DELETE CASCADE
                )""");
            
            // Center inventory
            st.execute("""
                CREATE TABLE IF NOT EXISTS center_inventory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    center_code TEXT NOT NULL,
                    goods_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    FOREIGN KEY(goods_id) REFERENCES goods(id) ON DELETE CASCADE
                )""");

            // Seed common goods
            st.execute("""
                INSERT OR IGNORE INTO goods (id, name) VALUES
                (1,'Rice'),(2,'Big Onions'),(3,'Potatoes'),(4,'Tomatoes'),(5,'Carrots')
            """);
            
            // Seed center inventory baseline
            st.execute("""
                INSERT OR IGNORE INTO center_inventory (center_code, goods_id, quantity) VALUES
                ('PETTAH', 1, 5000), ('PETTAH', 2, 3000), ('PETTAH', 3, 4000),
                ('PELIYAGODA', 1, 4500), ('PELIYAGODA', 2, 2500), ('PELIYAGODA', 3, 3500),
                ('DAMBULLA', 1, 6000), ('DAMBULLA', 2, 4000), ('DAMBULLA', 3, 5000)
            """);
            
            // Vehicles table
            st.execute("""
                CREATE TABLE IF NOT EXISTS vehicles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    vehicle_number TEXT UNIQUE NOT NULL,
                    vehicle_type TEXT NOT NULL,
                    capacity_kg INTEGER NOT NULL,
                    driver_name TEXT,
                    driver_phone TEXT,
                    status TEXT DEFAULT 'AVAILABLE',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
                )""");
            
            // Transports table (assignments)
            st.execute("""
                CREATE TABLE IF NOT EXISTS transports (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    vehicle_id INTEGER NOT NULL,
                    source_center TEXT NOT NULL,
                    destination_center TEXT NOT NULL,
                    goods_id INTEGER NOT NULL,
                    quantity INTEGER NOT NULL,
                    status TEXT DEFAULT 'SCHEDULED',
                    scheduled_date DATETIME,
                    departure_date DATETIME,
                    arrival_date DATETIME,
                    notes TEXT,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
                    FOREIGN KEY(goods_id) REFERENCES goods(id) ON DELETE CASCADE
                )""");
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}