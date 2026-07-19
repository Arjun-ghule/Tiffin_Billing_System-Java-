package com.vasudha.tiffin.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame containing tabs for all billing modules.
 */
public class MainFrame extends JFrame {

    private CustomerPanel customerPanel;
    private BillingPanel billingPanel;
    private HistoryPanel historyPanel;
    private ExpensePanel expensePanel;
    private SettingsPanel settingsPanel;

    public MainFrame() {
        super("Vasudha Tiffin Billing");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 700);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null); // Center on screen

        initComponents();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Helvetica", Font.BOLD, 12));

        // Create Panel Instances
        customerPanel = new CustomerPanel(this);
        billingPanel = new BillingPanel(this);
        historyPanel = new HistoryPanel(this);
        expensePanel = new ExpensePanel(this);
        settingsPanel = new SettingsPanel(this);

        // Add Tabs
        tabbedPane.addTab("Customers", customerPanel);
        tabbedPane.addTab("Create Bill", billingPanel);
        tabbedPane.addTab("Search / History", historyPanel);
        tabbedPane.addTab("Monthly Expenses", expensePanel);
        tabbedPane.addTab("Settings", settingsPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Triggers Customer dropdown reload in the Billing panel.
     */
    public void refreshCustomerDropdowns() {
        if (billingPanel != null) {
            billingPanel.reloadCustomers();
        }
    }

    /**
     * Triggers updating the profit dashboards in Billing and Expense panels.
     */
    public void refreshProfitSummary() {
        if (billingPanel != null) {
            billingPanel.updateProfitDisplay();
        }
        if (expensePanel != null) {
            expensePanel.loadReport();
        }
    }

    /**
     * Triggers reloading the billing table in History Panel.
     */
    public void refreshBillsHistory() {
        if (historyPanel != null) {
            historyPanel.loadAllBills();
        }
    }
}
