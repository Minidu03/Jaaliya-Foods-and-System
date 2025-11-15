package com.slginventory.auth;

import com.slginventory.db.Database;
import com.slginventory.model.User;

import java.sql.*;

public class AuthService {
    public User login(String username, String passwordPlain) {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT id, username, password_hash, company FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (PasswordHasher.verify(passwordPlain, hash)) {
                        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("company"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Login failed", e);
        }
        return null;
    }

    public boolean register(String username, String passwordPlain, String company, String vendorName, String phone) {
        try (Connection conn = Database.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO users (username, password_hash, company) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, PasswordHasher.hash(passwordPlain));
                    ps.setString(3, company);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("User ID not generated");
                        }
                        userId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO vendors (user_id, name, phone) VALUES (?,?,?)")) {
                    ps.setInt(1, userId);
                    ps.setString(2, vendorName);
                    ps.setString(3, phone);
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                if (ex.getMessage() != null && ex.getMessage().contains("UNIQUE")) {
                    return false;
                }
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Registration failed", e);
        }
    }
}