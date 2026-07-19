package com.vasudha.tiffin.dao;

import com.vasudha.tiffin.database.DatabaseManager;
import com.vasudha.tiffin.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Bill operations.
 */
public class BillDAO {

    /**
     * Obtains the next auto-incremented bill number, starting from 1000.
     */
    public int getNextBillNo() throws SQLException {
        String sql = "SELECT COALESCE(MAX(bill_no), 999) + 1 FROM bills";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1000;
    }

    /**
     * Inserts a new bill record into the database.
     */
    public boolean insert(Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (bill_no, cust_id, cust_name, date_from, date_to, " +
                "morning_count, morning_rate, night_count, night_rate, subtotal, " +
                "pending_amount, advance_amount, final_total, status, pdf_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bill.getBillNo());
            pstmt.setString(2, bill.getCustId());
            pstmt.setString(3, bill.getCustName());
            pstmt.setString(4, bill.getDateFrom());
            pstmt.setString(5, bill.getDateTo());
            pstmt.setInt(6, bill.getMorningCount());
            pstmt.setDouble(7, bill.getMorningRate());
            pstmt.setInt(8, bill.getNightCount());
            pstmt.setDouble(9, bill.getNightRate());
            pstmt.setDouble(10, bill.getSubtotal());
            pstmt.setDouble(11, bill.getPendingAmount());
            pstmt.setDouble(12, bill.getAdvanceAmount());
            pstmt.setDouble(13, bill.getFinalTotal());
            pstmt.setString(14, bill.getStatus());
            pstmt.setString(15, bill.getPdfPath());

            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * Updates the status of a specific bill (identified by bill number).
     */
    public boolean updateStatus(int billNo, String status) throws SQLException {
        String sql = "UPDATE bills SET status = ? WHERE bill_no = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, billNo);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * Retrieves all bills from the database ordered by bill number descending.
     */
    public List<Bill> getAll() throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills ORDER BY bill_no DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToBill(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves all bills for a specific customer ID.
     */
    public List<Bill> getBillsByCustomer(String custId) throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE cust_id = ? ORDER BY bill_no DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, custId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBill(rs));
                }
            }
        }
        return list;
    }

    /**
     * Searches bills by bill number, customer ID, or customer name.
     */
    public List<Bill> search(String query) throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE CAST(bill_no AS TEXT) LIKE ? OR cust_id LIKE ? OR cust_name LIKE ? ORDER BY bill_no DESC";
        String likeQuery = "%" + query.trim() + "%";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, likeQuery);
            pstmt.setString(2, likeQuery);
            pstmt.setString(3, likeQuery);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBill(rs));
                }
            }
        }
        return list;
    }

    /**
     * Calculates the total revenue from PAID bills (including PAID (Advance)) for a given month and year.
     * Note: date_from is in format dd/MM/yyyy.
     */
    public double getRevenueByMonthAndYear(int month, int year) throws SQLException {
        String formattedMonth = String.format("%02d", month);
        String datePattern = "%/" + formattedMonth + "/" + year;
        String sql = "SELECT SUM(final_total) FROM bills WHERE status LIKE 'PAID%' AND date_from LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, datePattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    private Bill mapRowToBill(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setId(rs.getInt("id"));
        bill.setBillNo(rs.getInt("bill_no"));
        bill.setCustId(rs.getString("cust_id"));
        bill.setCustName(rs.getString("cust_name"));
        bill.setDateFrom(rs.getString("date_from"));
        bill.setDateTo(rs.getString("date_to"));
        bill.setMorningCount(rs.getInt("morning_count"));
        bill.setMorningRate(rs.getDouble("morning_rate"));
        bill.setNightCount(rs.getInt("night_count"));
        bill.setNightRate(rs.getDouble("night_rate"));
        bill.setSubtotal(rs.getDouble("subtotal"));
        bill.setPendingAmount(rs.getDouble("pending_amount"));
        bill.setAdvanceAmount(rs.getDouble("advance_amount"));
        bill.setFinalTotal(rs.getDouble("final_total"));
        bill.setStatus(rs.getString("status"));
        bill.setPdfPath(rs.getString("pdf_path"));
        bill.setCreatedAt(rs.getString("created_at"));
        return bill;
    }
}
