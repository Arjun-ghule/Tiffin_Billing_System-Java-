package com.vasudha.tiffin.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Manages the SQLite database connection and initialization.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:tiffin_billing.db";

    static {
        // Load SQLite JDBC Driver
        try {
            Class.forName("org.xerial.sqlite-jdbc");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Obtains a connection to the SQLite database.
     * Enables foreign keys for every connection created.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes the database: creates tables and inserts default settings if not present.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create Settings table
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                    "id INTEGER PRIMARY KEY DEFAULT 1," +
                    "service_name TEXT," +
                    "phone TEXT," +
                    "address TEXT," +
                    "logo_path TEXT," +
                    "qr_path TEXT" +
                    ");");

            // Create Customers table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cust_id TEXT UNIQUE NOT NULL," +
                    "name TEXT NOT NULL," +
                    "phone TEXT UNIQUE NOT NULL," +
                    "address TEXT" +
                    ");");

            // Create Bills table (with cascade delete foreign key)
            stmt.execute("CREATE TABLE IF NOT EXISTS bills (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "bill_no INTEGER UNIQUE NOT NULL," +
                    "cust_id TEXT NOT NULL," +
                    "cust_name TEXT NOT NULL," +
                    "date_from TEXT NOT NULL," +
                    "date_to TEXT NOT NULL," +
                    "morning_count INTEGER DEFAULT 0," +
                    "morning_rate REAL DEFAULT 0.0," +
                    "night_count INTEGER DEFAULT 0," +
                    "night_rate REAL DEFAULT 0.0," +
                    "subtotal REAL DEFAULT 0.0," +
                    "pending_amount REAL DEFAULT 0.0," +
                    "advance_amount REAL DEFAULT 0.0," +
                    "final_total REAL DEFAULT 0.0," +
                    "status TEXT DEFAULT 'UNPAID'," +
                    "pdf_path TEXT," +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (cust_id) REFERENCES customers(cust_id) ON DELETE CASCADE" +
                    ");");

            // Create Expenses table
            stmt.execute("CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "description TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // Insert default settings if table is empty
            insertDefaultSettings(conn);

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static void insertDefaultSettings(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM settings WHERE id = 1";
        String insertSql = "INSERT INTO settings (id, service_name, phone, address, logo_path, qr_path) " +
                "VALUES (1, ?, ?, ?, '', '')";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "Vasudha Tiffin Service");
                    pstmt.setString(2, "8484928996 / 9527929591");
                    pstmt.setString(3, "Flat no. 302, Trimurti Building, Eakta Housing Society, Mhetre Wasti");
                    pstmt.executeUpdate();
                    System.out.println("Default business settings inserted.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting default settings: " + e.getMessage());
        }
    }
}
