package com.vasudha.tiffin.ui;

import com.vasudha.tiffin.dao.BillDAO;
import com.vasudha.tiffin.dao.ExpenseDAO;
import com.vasudha.tiffin.model.Expense;
import com.vasudha.tiffin.service.PDFService;
import com.vasudha.tiffin.util.FileUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * UI Panel for logging business expenses and viewing monthly profitability reports.
 */
public class ExpensePanel extends JPanel {

    private final MainFrame mainFrame;
    private final ExpenseDAO expenseDAO;
    private final BillDAO billDAO;
    private final PDFService pdfService;

    // UI Components
    private JTextField txtDescription;
    private JTextField txtAmount;

    private JComboBox<String> cbMonths;
    private JComboBox<Integer> cbYears;
    private JTable tblExpenses;
    private DefaultTableModel tableModel;

    private JLabel lblTotalRevenue;
    private JLabel lblTotalExpenses;
    private JLabel lblNetProfit;

    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public ExpensePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.expenseDAO = new ExpenseDAO();
        this.billDAO = new BillDAO();
        this.pdfService = new PDFService();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create Panel Sections
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createReportPanel(), BorderLayout.CENTER);

        // Preselect current month/year and load
        loadReport();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHeader = new JLabel("Monthly Expenses & Report");
        lblHeader.setFont(new Font("Helvetica", Font.BOLD, 20));
        lblHeader.setForeground(new Color(33, 37, 41));
        panel.add(lblHeader);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Expense"));
        panel.setPreferredSize(new Dimension(280, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;

        // Description
        gbc.gridy = 0;
        panel.add(new JLabel("Expense Description:"), gbc);
        txtDescription = new JTextField();
        gbc.gridy = 1;
        panel.add(txtDescription, gbc);

        // Amount
        gbc.gridy = 2;
        panel.add(new JLabel("Amount (Rs.):"), gbc);
        txtAmount = new JTextField();
        gbc.gridy = 3;
        panel.add(txtAmount, gbc);

        // Add Button
        JButton btnAdd = new JButton("Add Expense");
        btnAdd.setBackground(new Color(40, 167, 69));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Helvetica", Font.BOLD, 12));

        gbc.gridy = 4;
        gbc.insets = new Insets(15, 8, 8, 8);
        panel.add(btnAdd, gbc);

        btnAdd.addActionListener(e -> addExpense());

        return panel;
    }

    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Monthly Expense Report & Profitability"));

        // Top Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        filterBar.add(new JLabel("Month:"));
        cbMonths = new JComboBox<>(MONTHS);
        cbMonths.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        filterBar.add(cbMonths);

        filterBar.add(new JLabel("Year:"));
        cbYears = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 2; y <= currentYear + 10; y++) {
            cbYears.addItem(y);
        }
        cbYears.setSelectedItem(currentYear);
        filterBar.add(cbYears);

        JButton btnShowReport = new JButton("Show Report");
        btnShowReport.setBackground(new Color(0, 123, 255));
        btnShowReport.setForeground(Color.WHITE);
        filterBar.add(btnShowReport);

        JButton btnGenPdf = new JButton("Generate PDF");
        btnGenPdf.setBackground(new Color(40, 167, 69));
        btnGenPdf.setForeground(Color.WHITE);
        filterBar.add(btnGenPdf);

        panel.add(filterBar, BorderLayout.NORTH);

        // Table in the Center
        String[] columns = {"Description", "Amount (Rs.)", "Date Recorded"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblExpenses = new JTable(tableModel);
        tblExpenses.setRowHeight(22);
        tblExpenses.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollTable = new JScrollPane(tblExpenses);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Summary Info at the Bottom
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Report Summary"));
        summaryPanel.setPreferredSize(new Dimension(0, 60));

        lblTotalRevenue = new JLabel("Total Revenue: Rs. 0.00", JLabel.CENTER);
        lblTotalRevenue.setFont(new Font("Helvetica", Font.BOLD, 12));
        lblTotalRevenue.setForeground(new Color(0, 123, 255));

        lblTotalExpenses = new JLabel("Total Expenses: Rs. 0.00", JLabel.CENTER);
        lblTotalExpenses.setFont(new Font("Helvetica", Font.BOLD, 12));
        lblTotalExpenses.setForeground(new Color(108, 117, 125));

        lblNetProfit = new JLabel("Net Profit: Rs. 0.00", JLabel.CENTER);
        lblNetProfit.setFont(new Font("Helvetica", Font.BOLD, 13));

        summaryPanel.add(lblTotalRevenue);
        summaryPanel.add(lblTotalExpenses);
        summaryPanel.add(lblNetProfit);

        panel.add(summaryPanel, BorderLayout.SOUTH);

        // Listeners
        btnShowReport.addActionListener(e -> loadReport());
        btnGenPdf.addActionListener(e -> generateReportPDF());

        return panel;
    }

    /**
     * Queries expenses and revenues and populates the table and summaries.
     */
    public void loadReport() {
        int month = cbMonths.getSelectedIndex() + 1;
        Integer year = (Integer) cbYears.getSelectedItem();
        if (year == null) return;

        try {
            // Load expenses into table
            List<Expense> expenses = expenseDAO.getExpensesByMonthAndYear(month, year);
            tableModel.setRowCount(0);
            double totalExp = 0;
            for (Expense exp : expenses) {
                totalExp += exp.getAmount();
                tableModel.addRow(new Object[]{
                        exp.getDescription(),
                        String.format("%.2f", exp.getAmount()),
                        exp.getCreatedAt() != null && exp.getCreatedAt().length() >= 10 ? exp.getCreatedAt().substring(0, 10) : exp.getCreatedAt()
                });
            }

            // Load Revenue
            double revenue = billDAO.getRevenueByMonthAndYear(month, year);
            double netProfit = revenue - totalExp;

            // Update Labels
            lblTotalRevenue.setText(String.format("Total Revenue: Rs. %.2f", revenue));
            lblTotalExpenses.setText(String.format("Total Expenses: Rs. %.2f", totalExp));
            lblNetProfit.setText(String.format("Net Profit: Rs. %.2f", netProfit));

            if (netProfit >= 0) {
                lblNetProfit.setForeground(new Color(40, 167, 69)); // Green for profit
            } else {
                lblNetProfit.setForeground(new Color(220, 53, 69)); // Red for loss
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error loading report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addExpense() {
        String desc = txtDescription.getText().trim();
        String amountStr = txtAmount.getText().trim();

        if (desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Expense description cannot be empty.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Expense amount must be a valid number.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Expense amount must be greater than zero.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Expense expense = new Expense(desc, amount);
        try {
            boolean success = expenseDAO.insert(expense);
            if (success) {
                JOptionPane.showMessageDialog(this, "Expense recorded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                
                txtDescription.setText("");
                txtAmount.setText("");

                // Refresh this panel
                loadReport();

                // Refresh profit displays in BillingPanel
                mainFrame.refreshProfitSummary();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert expense.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error saving expense: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateReportPDF() {
        int month = cbMonths.getSelectedIndex() + 1;
        Integer year = (Integer) cbYears.getSelectedItem();
        if (year == null) return;

        try {
            double revenue = billDAO.getRevenueByMonthAndYear(month, year);
            double totalExp = expenseDAO.getTotalExpensesByMonthAndYear(month, year);
            double netProfit = revenue - totalExp;
            List<Expense> expensesList = expenseDAO.getExpensesByMonthAndYear(month, year);

            String absolutePath = pdfService.generateExpenseReportPDF(month, year, revenue, totalExp, netProfit, expensesList);
            
            JOptionPane.showMessageDialog(this,
                    "Monthly Expense Report PDF generated successfully!\nFile: bills/ExpenseReport_" + month + "_" + year + ".pdf",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            // Open PDF
            FileUtil.openPDF(absolutePath);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "PDF generation error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
