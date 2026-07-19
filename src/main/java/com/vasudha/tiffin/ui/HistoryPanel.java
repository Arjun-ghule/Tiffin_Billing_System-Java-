package com.vasudha.tiffin.ui;

import com.vasudha.tiffin.dao.BillDAO;
import com.vasudha.tiffin.model.Bill;
import com.vasudha.tiffin.util.FileUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * UI Panel for searching bills, modifying status, and viewing generated PDFs.
 */
public class HistoryPanel extends JPanel {

    private final MainFrame mainFrame;
    private final BillDAO billDAO;

    // UI Components
    private JTextField txtBillNo;
    private JTextField txtCustId;
    private JTextField txtCustName;
    private JTable tblHistory;
    private DefaultTableModel tableModel;

    public HistoryPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.billDAO = new BillDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create Panel Sections
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createSearchPanel(), BorderLayout.WEST);
        add(createTableAndActionPanel(), BorderLayout.CENTER);

        // Initial Load
        loadAllBills();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHeader = new JLabel("Billing History & Search");
        lblHeader.setFont(new Font("Helvetica", Font.BOLD, 20));
        lblHeader.setForeground(new Color(33, 37, 41));
        panel.add(lblHeader);
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
        panel.setPreferredSize(new Dimension(280, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;

        // Bill Number
        gbc.gridy = 0;
        panel.add(new JLabel("Bill Number:"), gbc);
        txtBillNo = new JTextField();
        gbc.gridy = 1;
        panel.add(txtBillNo, gbc);

        // Customer ID
        gbc.gridy = 2;
        panel.add(new JLabel("Customer ID:"), gbc);
        txtCustId = new JTextField();
        gbc.gridy = 3;
        panel.add(txtCustId, gbc);

        // Customer Name
        gbc.gridy = 4;
        panel.add(new JLabel("Customer Name:"), gbc);
        txtCustName = new JTextField();
        gbc.gridy = 5;
        panel.add(txtCustName, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(0, 123, 255));
        btnSearch.setForeground(Color.WHITE);

        JButton btnReset = new JButton("Reset");
        btnReset.setBackground(new Color(108, 117, 125));
        btnReset.setForeground(Color.WHITE);

        btnPanel.add(btnSearch);
        btnPanel.add(btnReset);

        gbc.gridy = 6;
        gbc.insets = new Insets(15, 8, 8, 8);
        panel.add(btnPanel, gbc);

        // Action Listeners
        btnSearch.addActionListener(e -> performSearch());
        btnReset.addActionListener(e -> resetFilters());

        return panel;
    }

    private JPanel createTableAndActionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Table definition
        String[] columns = {
                "Bill No", "Customer ID", "Customer Name", "From Date", "To Date",
                "Subtotal", "Pending", "Advance", "Final Total", "Status", "Created Date"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblHistory = new JTable(tableModel);
        tblHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHistory.setRowHeight(24);
        tblHistory.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollTable = new JScrollPane(tblHistory);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        JButton btnPaid = new JButton("Mark PAID");
        btnPaid.setBackground(new Color(40, 167, 69));
        btnPaid.setForeground(Color.WHITE);
        btnPaid.setFont(new Font("Helvetica", Font.BOLD, 12));

        JButton btnUnpaid = new JButton("Mark UNPAID");
        btnUnpaid.setBackground(new Color(220, 53, 69));
        btnUnpaid.setForeground(Color.WHITE);
        btnUnpaid.setFont(new Font("Helvetica", Font.BOLD, 12));

        JButton btnOpenPdf = new JButton("Open PDF");
        btnOpenPdf.setBackground(new Color(0, 123, 255));
        btnOpenPdf.setForeground(Color.WHITE);
        btnOpenPdf.setFont(new Font("Helvetica", Font.BOLD, 12));

        actionPanel.add(btnPaid);
        actionPanel.add(btnUnpaid);
        actionPanel.add(btnOpenPdf);

        panel.add(actionPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnPaid.addActionListener(e -> updateBillStatus("PAID"));
        btnUnpaid.addActionListener(e -> updateBillStatus("UNPAID"));
        btnOpenPdf.addActionListener(e -> openBillPDF());

        return panel;
    }

    /**
     * Public method to load all bills.
     */
    public void loadAllBills() {
        try {
            List<Bill> bills = billDAO.getAll();
            populateTable(bills);
        } catch (SQLException e) {
            showError("Database error loading bills: " + e.getMessage());
        }
    }

    private void populateTable(List<Bill> bills) {
        tableModel.setRowCount(0);
        for (Bill b : bills) {
            tableModel.addRow(new Object[]{
                    b.getBillNo(),
                    b.getCustId(),
                    b.getCustName(),
                    b.getDateFrom(),
                    b.getDateTo(),
                    b.getSubtotal(),
                    b.getPendingAmount(),
                    b.getAdvanceAmount(),
                    b.getFinalTotal(),
                    b.getStatus(),
                    b.getCreatedAt() != null && b.getCreatedAt().length() >= 19 ? b.getCreatedAt().substring(0, 19) : b.getCreatedAt()
            });
        }
    }

    private void performSearch() {
        String billNoStr = txtBillNo.getText().trim();
        String custIdStr = txtCustId.getText().trim();
        String custNameStr = txtCustName.getText().trim();

        // If all are empty, load all
        if (billNoStr.isEmpty() && custIdStr.isEmpty() && custNameStr.isEmpty()) {
            loadAllBills();
            return;
        }

        try {
            // We search using the most descriptive string if multiple are filled, or let our DAO handle general search.
            // Our DAO has an SQL query: WHERE CAST(bill_no AS TEXT) LIKE ? OR cust_id LIKE ? OR cust_name LIKE ?
            // Let's pass the active filter. Since our DAO takes a single search query, we can query by whatever is non-empty.
            // If they entered multiple, we will search with the first non-empty input to match the DAO signature.
            String query = "";
            if (!billNoStr.isEmpty()) {
                query = billNoStr;
            } else if (!custIdStr.isEmpty()) {
                query = custIdStr;
            } else {
                query = custNameStr;
            }
            List<Bill> bills = billDAO.search(query);
            populateTable(bills);
        } catch (SQLException e) {
            showError("Database error during search: " + e.getMessage());
        }
    }

    private void resetFilters() {
        txtBillNo.setText("");
        txtCustId.setText("");
        txtCustName.setText("");
        loadAllBills();
    }

    private void updateBillStatus(String status) {
        int selectedRow = tblHistory.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill from the history table.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billNo = (int) tblHistory.getValueAt(selectedRow, 0);
        try {
            boolean success = billDAO.updateStatus(billNo, status);
            if (success) {
                JOptionPane.showMessageDialog(this, "Bill status updated to " + status + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh table
                performSearch();

                // Refresh financial summaries
                mainFrame.refreshProfitSummary();
            } else {
                showError("Failed to update bill status.");
            }
        } catch (SQLException e) {
            showError("Database error updating status: " + e.getMessage());
        }
    }

    private void openBillPDF() {
        int selectedRow = tblHistory.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill from the history table.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billNo = (int) tblHistory.getValueAt(selectedRow, 0);
        try {
            // Find the PDF path for this bill
            List<Bill> bills = billDAO.getAll();
            for (Bill b : bills) {
                if (b.getBillNo() == billNo) {
                    if (b.getPdfPath() != null && !b.getPdfPath().isEmpty()) {
                        boolean opened = FileUtil.openPDF(b.getPdfPath());
                        if (!opened) {
                            JOptionPane.showMessageDialog(this, "Could not open PDF file. Make sure it exists at: " + b.getPdfPath(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "No PDF path found for this bill.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
