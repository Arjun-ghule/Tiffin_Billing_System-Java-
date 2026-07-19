package com.vasudha.tiffin.ui;

import com.vasudha.tiffin.dao.BillDAO;
import com.vasudha.tiffin.dao.CustomerDAO;
import com.vasudha.tiffin.dao.ExpenseDAO;
import com.vasudha.tiffin.dao.SettingsDAO;
import com.vasudha.tiffin.model.Bill;
import com.vasudha.tiffin.model.BusinessSettings;
import com.vasudha.tiffin.model.Customer;
import com.vasudha.tiffin.service.BillingService;
import com.vasudha.tiffin.service.PDFService;
import com.vasudha.tiffin.util.FileUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * UI Panel for creating tiffin bills, executing calculations, and generating PDFs.
 */
public class BillingPanel extends JPanel {

    private final MainFrame mainFrame;
    private final CustomerDAO customerDAO;
    private final BillDAO billDAO;
    private final ExpenseDAO expenseDAO;
    private final SettingsDAO settingsDAO;
    private final BillingService billingService;
    private final PDFService pdfService;

    // UI Components
    private JComboBox<Customer> cbCustomers;
    private JComboBox<String> cbMonths;
    private JComboBox<Integer> cbYears;

    private JTextField txtMorningCount;
    private JTextField txtMorningRate;
    private JTextField txtNightCount;
    private JTextField txtNightRate;

    private JTextField txtPendingAmount;
    private JTextField txtAdvanceAmount;

    private JTextField txtSubTotal;
    private JTextField txtFinalTotal;

    // Profit Display Labels
    private JLabel lblProfitSummary;

    private static final String[] MONTHS = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public BillingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.customerDAO = new CustomerDAO();
        this.billDAO = new BillDAO();
        this.expenseDAO = new ExpenseDAO();
        this.settingsDAO = new SettingsDAO();
        this.billingService = new BillingService();
        this.pdfService = new PDFService();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create Panel Sections
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createProfitPanel(), BorderLayout.SOUTH);

        // Load Customers and initial profit display
        reloadCustomers();
        updateProfitDisplay();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHeader = new JLabel("Create Monthly Bill");
        lblHeader.setFont(new Font("Helvetica", Font.BOLD, 20));
        lblHeader.setForeground(new Color(33, 37, 41));
        panel.add(lblHeader);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel mainForm = new JPanel(new GridBagLayout());
        mainForm.setBorder(BorderFactory.createTitledBorder("Billing Parameters"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Customer Dropdown
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        mainForm.add(new JLabel("Select Customer:"), gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        cbCustomers = new JComboBox<>();
        mainForm.add(cbCustomers, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2;
        JButton btnViewHistory = new JButton("View History");
        btnViewHistory.setBackground(new Color(108, 117, 125));
        btnViewHistory.setForeground(Color.WHITE);
        mainForm.add(btnViewHistory, gbc);

        // Row 1: Month and Year Selection
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        mainForm.add(new JLabel("Select Month / Year:"), gbc);

        JPanel datePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        cbMonths = new JComboBox<>(MONTHS);
        // Pre-select current month
        cbMonths.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        
        cbYears = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 2; y <= currentYear + 10; y++) {
            cbYears.addItem(y);
        }
        cbYears.setSelectedItem(currentYear);

        datePanel.add(cbMonths);
        datePanel.add(cbYears);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        mainForm.add(datePanel, gbc);

        // Listeners for month/year change to update profit report context
        cbMonths.addActionListener(e -> updateProfitDisplay());
        cbYears.addActionListener(e -> updateProfitDisplay());

        // Subpanel for inputs (Morning, Night, Adjustments)
        JPanel inputGrid = new JPanel(new GridLayout(4, 2, 15, 10));
        inputGrid.setBorder(BorderFactory.createTitledBorder("Tiffin Details & Payments"));

        inputGrid.add(new JLabel("Morning Tiffin Count:"));
        txtMorningCount = new JTextField("0");
        inputGrid.add(txtMorningCount);

        inputGrid.add(new JLabel("Morning Tiffin Rate (Rs.):"));
        txtMorningRate = new JTextField("0.0");
        inputGrid.add(txtMorningRate);

        inputGrid.add(new JLabel("Night Tiffin Count:"));
        txtNightCount = new JTextField("0");
        inputGrid.add(txtNightCount);

        inputGrid.add(new JLabel("Night Tiffin Rate (Rs.):"));
        txtNightRate = new JTextField("0.0");
        inputGrid.add(txtNightRate);

        JPanel adjustmentGrid = new JPanel(new GridLayout(4, 2, 15, 10));
        adjustmentGrid.setBorder(BorderFactory.createTitledBorder("Adjustments & Summaries"));

        adjustmentGrid.add(new JLabel("Pending Amount (Rs.):"));
        txtPendingAmount = new JTextField("0.0");
        adjustmentGrid.add(txtPendingAmount);

        adjustmentGrid.add(new JLabel("Advance Amount (Rs.):"));
        txtAdvanceAmount = new JTextField("0.0");
        adjustmentGrid.add(txtAdvanceAmount);

        adjustmentGrid.add(new JLabel("Sub Total (Rs.):"));
        txtSubTotal = new JTextField("0.0");
        txtSubTotal.setEditable(false);
        txtSubTotal.setFont(new Font("Helvetica", Font.BOLD, 12));
        adjustmentGrid.add(txtSubTotal);

        adjustmentGrid.add(new JLabel("Final Total (Rs.):"));
        txtFinalTotal = new JTextField("0.0");
        txtFinalTotal.setEditable(false);
        txtFinalTotal.setFont(new Font("Helvetica", Font.BOLD, 12));
        adjustmentGrid.add(txtFinalTotal);

        // Adding subgrids to form
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;

        JPanel splitter = new JPanel(new GridLayout(1, 2, 20, 0));
        splitter.add(inputGrid);
        splitter.add(adjustmentGrid);
        mainForm.add(splitter, gbc);

        // Buttons Panel at the bottom of form
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnCalculate = new JButton("Calculate Total");
        btnCalculate.setBackground(new Color(0, 123, 255));
        btnCalculate.setForeground(Color.WHITE);
        btnCalculate.setFont(new Font("Helvetica", Font.BOLD, 13));

        JButton btnSavePdf = new JButton("Save & Generate PDF");
        btnSavePdf.setBackground(new Color(40, 167, 69));
        btnSavePdf.setForeground(Color.WHITE);
        btnSavePdf.setFont(new Font("Helvetica", Font.BOLD, 13));

        buttonsPanel.add(btnCalculate);
        buttonsPanel.add(btnSavePdf);

        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainForm.add(buttonsPanel, gbc);

        // Wire Event Listeners
        btnViewHistory.addActionListener(e -> viewCustomerHistory());
        btnCalculate.addActionListener(e -> calculateTotal(false));
        btnSavePdf.addActionListener(e -> saveAndGenerateBill());

        return mainForm;
    }

    private JPanel createProfitPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Monthly Dashboard Summary"));
        panel.setPreferredSize(new Dimension(0, 75));

        lblProfitSummary = new JLabel("Loading monthly financial metrics...", JLabel.CENTER);
        lblProfitSummary.setFont(new Font("Helvetica", Font.BOLD, 14));
        panel.add(lblProfitSummary, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Public method to reload customers from the database.
     */
    public void reloadCustomers() {
        cbCustomers.removeAllItems();
        try {
            List<Customer> customers = customerDAO.getAll();
            for (Customer c : customers) {
                cbCustomers.addItem(c);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error reloading customer list: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refreshes the Current Month Profit label display.
     */
    public void updateProfitDisplay() {
        int selectedMonth = cbMonths.getSelectedIndex() + 1;
        Integer selectedYear = (Integer) cbYears.getSelectedItem();
        if (selectedYear == null) return;

        try {
            double revenue = billDAO.getRevenueByMonthAndYear(selectedMonth, selectedYear);
            double expenses = expenseDAO.getTotalExpensesByMonthAndYear(selectedMonth, selectedYear);
            double profit = revenue - expenses;

            String monthName = MONTHS[selectedMonth - 1];
            String text = String.format("Selected Month: %s %d   |   Total Paid Revenue: Rs. %.2f   |   Total Expenses: Rs. %.2f   |   Net Profit: Rs. %.2f",
                    monthName, selectedYear, revenue, expenses, profit);
            
            lblProfitSummary.setText(text);
            if (profit >= 0) {
                lblProfitSummary.setForeground(new Color(40, 167, 69)); // Green for profit
            } else {
                lblProfitSummary.setForeground(new Color(220, 53, 69)); // Red for loss
            }
        } catch (SQLException e) {
            lblProfitSummary.setText("Database error updating profit metrics.");
            lblProfitSummary.setForeground(Color.BLACK);
        }
    }

    private void viewCustomerHistory() {
        Customer selected = (Customer) cbCustomers.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer to view history.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomerHistoryDialog dialog = new CustomerHistoryDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                selected.getCustId(),
                selected.getName()
        );
        dialog.setVisible(true);
    }

    private Bill calculateTotal(boolean showErrors) {
        Customer selected = (Customer) cbCustomers.getSelectedItem();
        if (selected == null) {
            if (showErrors) JOptionPane.showMessageDialog(this, "Please select a customer.", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        int mCount;
        double mRate;
        int nCount;
        double nRate;
        double pending;
        double advance;

        try {
            mCount = Integer.parseInt(txtMorningCount.getText().trim());
            mRate = Double.parseDouble(txtMorningRate.getText().trim());
            nCount = Integer.parseInt(txtNightCount.getText().trim());
            nRate = Double.parseDouble(txtNightRate.getText().trim());
            pending = Double.parseDouble(txtPendingAmount.getText().trim());
            advance = Double.parseDouble(txtAdvanceAmount.getText().trim());
        } catch (NumberFormatException e) {
            if (showErrors) JOptionPane.showMessageDialog(this, "Please check all values: Counts must be integers; rates and adjustments must be numbers.", "Invalid Format", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // Logical Validations
        if (mCount < 0 || mRate < 0 || nCount < 0 || nRate < 0 || pending < 0 || advance < 0) {
            if (showErrors) JOptionPane.showMessageDialog(this, "Negative values are not allowed in counts, rates, or adjustments.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        try {
            billingService.validateAdjustments(pending, advance);
        } catch (IllegalArgumentException e) {
            if (showErrors) JOptionPane.showMessageDialog(this, e.getMessage(), "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // Build mock bill to execute calculations
        Bill bill = new Bill();
        bill.setCustId(selected.getCustId());
        bill.setCustName(selected.getName());
        bill.setMorningCount(mCount);
        bill.setMorningRate(mRate);
        bill.setNightCount(nCount);
        bill.setNightRate(nRate);
        bill.setPendingAmount(pending);
        bill.setAdvanceAmount(advance);

        billingService.processBillCalculations(bill);

        // Update read-only text fields
        txtSubTotal.setText(String.format("%.2f", bill.getSubtotal()));
        txtFinalTotal.setText(String.format("%.2f", bill.getFinalTotal()));

        return bill;
    }

    private void saveAndGenerateBill() {
        Bill bill = calculateTotal(true);
        if (bill == null) return; // Calculation/Validation failed

        int month = cbMonths.getSelectedIndex() + 1;
        Integer year = (Integer) cbYears.getSelectedItem();
        if (year == null) return;

        // Calculate billing period dates
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        bill.setDateFrom(startDate.format(dtf));
        bill.setDateTo(endDate.format(dtf));

        try {
            // Check business settings
            BusinessSettings bs = settingsDAO.getSettings();
            if (bs == null) {
                JOptionPane.showMessageDialog(this, "Business settings are missing. Please configure them in the Settings tab.", "Configuration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get next automatic bill number
            int nextBillNo = billDAO.getNextBillNo();
            bill.setBillNo(nextBillNo);

            // Generate PDF file path
            String filename = "bills/Bill_" + nextBillNo + "_" + bill.getCustId() + ".pdf";
            bill.setPdfPath(filename);

            // Generate the actual PDF
            String absolutePath = pdfService.generateBillPDF(bill, bs);
            bill.setPdfPath(absolutePath);

            // Save to database
            boolean success = billDAO.insert(bill);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Bill saved and PDF generated successfully!\nBill No: " + nextBillNo + "\nFile: " + filename,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Auto-open PDF
                FileUtil.openPDF(absolutePath);

                // Clear input fields (keep rates, reset counts & adjustments)
                txtMorningCount.setText("0");
                txtNightCount.setText("0");
                txtPendingAmount.setText("0.0");
                txtAdvanceAmount.setText("0.0");
                txtSubTotal.setText("0.0");
                txtFinalTotal.setText("0.0");

                // Refresh application dashboards
                mainFrame.refreshBillsHistory();
                mainFrame.refreshProfitSummary();

            } else {
                JOptionPane.showMessageDialog(this, "Failed to save bill into the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error creating bill: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "PDF generation error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
