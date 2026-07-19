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
 * Dialog to view a specific customer's billing history.
 */
public class CustomerHistoryDialog extends JDialog {

    private final String custId;
    private final String custName;
    private final BillDAO billDAO;
    private JTable tblBills;
    private DefaultTableModel tableModel;

    public CustomerHistoryDialog(Frame parent, String custId, String custName) {
        super(parent, "Billing History - " + custName + " (" + custId + ")", true);
        this.custId = custId;
        this.custName = custName;
        this.billDAO = new BillDAO();

        setSize(900, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // Create Panel sections
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        loadHistory();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new EmptyBorder(10, 10, 0, 10));
        JLabel lblHeader = new JLabel("Billing History for: " + custName + " [" + custId + "]");
        lblHeader.setFont(new Font("Helvetica", Font.BOLD, 16));
        panel.add(lblHeader);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {
                "Bill No", "From Date", "To Date", "Morning Count", "Morning Rate",
                "Night Count", "Night Rate", "Subtotal", "Pending", "Advance",
                "Final Total", "Status", "Created Date"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblBills = new JTable(tableModel);
        tblBills.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblBills.setRowHeight(22);
        tblBills.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(tblBills);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton btnOpenPdf = new JButton("Open PDF");
        btnOpenPdf.setBackground(new Color(0, 123, 255));
        btnOpenPdf.setForeground(Color.WHITE);
        
        JButton btnClose = new JButton("Close");
        btnClose.setBackground(new Color(108, 117, 125));
        btnClose.setForeground(Color.WHITE);

        panel.add(btnOpenPdf);
        panel.add(btnClose);

        // Action Listeners
        btnOpenPdf.addActionListener(e -> openSelectedBillPdf());
        btnClose.addActionListener(e -> dispose());

        return panel;
    }

    private void loadHistory() {
        try {
            List<Bill> bills = billDAO.getBillsByCustomer(custId);
            tableModel.setRowCount(0);
            for (Bill b : bills) {
                tableModel.addRow(new Object[]{
                        b.getBillNo(),
                        b.getDateFrom(),
                        b.getDateTo(),
                        b.getMorningCount(),
                        b.getMorningRate(),
                        b.getNightCount(),
                        b.getNightRate(),
                        b.getSubtotal(),
                        b.getPendingAmount(),
                        b.getAdvanceAmount(),
                        b.getFinalTotal(),
                        b.getStatus(),
                        b.getCreatedAt() != null && b.getCreatedAt().length() >= 19 ? b.getCreatedAt().substring(0, 19) : b.getCreatedAt()
                });
            }
            if (bills.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No billing records found for this customer.", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading customer history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedBillPdf() {
        int selectedRow = tblBills.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill from the history table.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int billNo = (int) tblBills.getValueAt(selectedRow, 0);
        try {
            List<Bill> bills = billDAO.getBillsByCustomer(custId);
            for (Bill b : bills) {
                if (b.getBillNo() == billNo) {
                    if (b.getPdfPath() != null && !b.getPdfPath().isEmpty()) {
                        boolean opened = FileUtil.openPDF(b.getPdfPath());
                        if (!opened) {
                            JOptionPane.showMessageDialog(this, "Could not open PDF file. Make sure it exists at: " + b.getPdfPath(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "No PDF path associated with this bill.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
