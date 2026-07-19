package com.vasudha.tiffin.dao;

import com.vasudha.tiffin.database.DatabaseManager;
import com.vasudha.tiffin.model.BusinessSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for Business Settings operations.
 * Always accesses the single record with ID = 1.
 */
public class SettingsDAO {

    /**
     * Retrieves the business settings.
     */
    public BusinessSettings getSettings() throws SQLException {
        String sql = "SELECT * FROM settings WHERE id = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return new BusinessSettings(
                        rs.getInt("id"),
                        rs.getString("service_name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("logo_path"),
                        rs.getString("qr_path")
                );
            }
        }
        return null;
    }

    /**
     * Updates the business settings.
     */
    public boolean updateSettings(BusinessSettings settings) throws SQLException {
        String sql = "UPDATE settings SET service_name = ?, phone = ?, address = ?, logo_path = ?, qr_path = ? WHERE id = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, settings.getServiceName().trim());
            pstmt.setString(2, settings.getPhone().trim());
            pstmt.setString(3, settings.getAddress().trim());
            pstmt.setString(4, settings.getLogoPath() != null ? settings.getLogoPath().trim() : "");
            pstmt.setString(5, settings.getQrPath() != null ? settings.getQrPath().trim() : "");
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }
}
