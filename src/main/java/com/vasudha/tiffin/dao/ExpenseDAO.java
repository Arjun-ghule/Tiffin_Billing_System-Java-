package com.vasudha.tiffin.dao;

import com.vasudha.tiffin.database.DatabaseManager;
import com.vasudha.tiffin.model.Expense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Expense operations.
 */
public class ExpenseDAO {

    /**
     * Inserts a new expense into the database.
     * Inserts the current local date time format to keep it consistent.
     */
    public boolean insert(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (description, amount, created_at) VALUES (?, ?, datetime('now', 'localtime'))";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, expense.getDescription().trim());
            pstmt.setDouble(2, expense.getAmount());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * Retrieves expenses for a given month and year.
     * Maps to the 'created_at' date starting with 'YYYY-MM'.
     */
    public List<Expense> getExpensesByMonthAndYear(int month, int year) throws SQLException {
        List<Expense> list = new ArrayList<>();
        String pattern = String.format("%d-%02d%%", year, month);
        String sql = "SELECT * FROM expenses WHERE created_at LIKE ? ORDER BY id DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Expense(
                            rs.getInt("id"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Calculates the sum of expenses for a given month and year.
     */
    public double getTotalExpensesByMonthAndYear(int month, int year) throws SQLException {
        String pattern = String.format("%d-%02d%%", year, month);
        String sql = "SELECT SUM(amount) FROM expenses WHERE created_at LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }
}
