package com.vasudha.tiffin.dao;

import com.vasudha.tiffin.database.DatabaseManager;
import com.vasudha.tiffin.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Customer operations.
 */
public class CustomerDAO {

    /**
     * Generates the next sequential customer ID, e.g., C0001, C0002, etc.
     */
    public String getNextCustId() throws SQLException {
        String sql = "SELECT cust_id FROM customers ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                String lastId = rs.getString("cust_id");
                if (lastId != null && lastId.startsWith("C")) {
                    try {
                        int num = Integer.parseInt(lastId.substring(1));
                        return String.format("C%04d", num + 1);
                    } catch (NumberFormatException e) {
                        // ignore and fall back
                    }
                }
            }
        }
        return "C0001";
    }

    /**
     * Checks if a customer with the given name or phone number already exists.
     */
    public boolean isDuplicate(String name, String phone) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE LOWER(name) = LOWER(?) OR phone = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name.trim());
            pstmt.setString(2, phone.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Inserts a new Customer into the database.
     */
    public boolean insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (cust_id, name, phone, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getCustId());
            pstmt.setString(2, customer.getName().trim());
            pstmt.setString(3, customer.getPhone().trim());
            pstmt.setString(4, customer.getAddress() != null ? customer.getAddress().trim() : "");
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * Returns a list of all customers.
     */
    public List<Customer> getAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY cust_id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("cust_id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
        }
        return list;
    }

    /**
     * Searches for customers by customer ID, name, or phone number.
     */
    public List<Customer> search(String query) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE cust_id LIKE ? OR name LIKE ? OR phone LIKE ? ORDER BY cust_id ASC";
        String likeQuery = "%" + query.trim() + "%";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, likeQuery);
            pstmt.setString(2, likeQuery);
            pstmt.setString(3, likeQuery);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Customer(
                            rs.getInt("id"),
                            rs.getString("cust_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("address")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Deletes a customer by their unique customer ID.
     * Note: cascading delete in foreign keys will delete associated bills in bills table automatically.
     */
    public boolean delete(String custId) throws SQLException {
        String sql = "DELETE FROM customers WHERE cust_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, custId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }
}
