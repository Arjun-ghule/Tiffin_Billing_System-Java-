package com.vasudha.tiffin.ui;

import com.vasudha.tiffin.dao.CustomerDAO;
import com.vasudha.tiffin.model.Customer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

/**
 * UI panel for managing customers.
 */
public class CustomerPanel extends JPanel {

    private final MainFrame mainFrame;
    private final CustomerDAO customerDAO;

    // UI Components
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextArea txtAddress;
    private JTextField txtSearch;
    private JTable tblCustomers;
    private DefaultTableModel tableModel;

    public CustomerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.customerDAO = new CustomerDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create Panel sections
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        // Load Initial Data
        loadCustomers();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHeader = new JLabel("Customer Management");
        lblHeader.setFont(new Font("Helvetica", Font.BOLD, 20));
        lblHeader.setForeground(new Color(33, 37, 41));
        panel.add(lblHeader);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Customer"));
        panel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;

        // Name
        gbc.gridy = 0;
        panel.add(new JLabel("Name *"), gbc);
        txtName = new JTextField();
        gbc.gridy = 1;
        panel.add(txtName, gbc);

        // Phone
        gbc.gridy = 2;
        panel.add(new JLabel("Phone *"), gbc);
        txtPhone = new JTextField();
        gbc.gridy = 3;
        panel.add(txtPhone, gbc);

        // Address
        gbc.gridy = 4;
        panel.add(new JLabel("Address"), gbc);
        txtAddress = new JTextArea(5, 20);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane scrollAddress = new JScrollPane(txtAddress);
        gbc.gridy = 5;
        panel.add(scrollAddress, gbc);

        // Add Button
        JButton btnAdd = new JButton("Add Customer");
        btnAdd.setFont(new Font("Helvetica", Font.BOLD, 12));
        btnAdd.setBackground(new Color(40, 167, 69)); // Success green
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        gbc.gridy = 6;
        gbc.insets = new Insets(15, 8, 8, 8);
        panel.add(btnAdd, gbc);

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addCustomer();
            }
        });

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Search Bar Panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        txtSearch = new JTextField();
        txtSearch.setToolTipText("Search by ID, Name or Phone");
        searchPanel.add(txtSearch, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        JButton btnSearch = new JButton("Search");
        btnSearch.setBackground(new Color(0, 123, 255)); // Primary Blue
        btnSearch.setForeground(Color.WHITE);
        searchPanel.add(btnSearch, gbc);

        gbc.gridx = 2;
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBackground(new Color(108, 117, 125)); // Secondary gray
        btnRefresh.setForeground(Color.WHITE);
        searchPanel.add(btnRefresh, gbc);

        gbc.gridx = 3;
        JButton btnDelete = new JButton("Delete Customer");
        btnDelete.setBackground(new Color(220, 53, 69)); // Danger red
        btnDelete.setForeground(Color.WHITE);
        searchPanel.add(btnDelete, gbc);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Customer ID", "Name", "Phone", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only
            }
        };

        tblCustomers = new JTable(tableModel);
        tblCustomers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCustomers.setRowHeight(24);
        tblCustomers.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollTable = new JScrollPane(tblCustomers);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Action Listeners
        btnSearch.addActionListener(e -> searchCustomers());
        txtSearch.addActionListener(e -> searchCustomers());
        btnRefresh.addActionListener(e -> loadCustomers());
        btnDelete.addActionListener(e -> deleteCustomer());

        return panel;
    }

    private void loadCustomers() {
        try {
            List<Customer> customers = customerDAO.getAll();
            populateTable(customers);
            txtSearch.setText("");
        } catch (SQLException e) {
            showError("Database error loading customers: " + e.getMessage());
        }
    }

    private void searchCustomers() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            loadCustomers();
            return;
        }
        try {
            List<Customer> customers = customerDAO.search(query);
            populateTable(customers);
        } catch (SQLException e) {
            showError("Database error searching customers: " + e.getMessage());
        }
    }

    private void populateTable(List<Customer> customers) {
        tableModel.setRowCount(0);
        for (Customer c : customers) {
            tableModel.addRow(new Object[]{
                    c.getCustId(),
                    c.getName(),
                    c.getPhone(),
                    c.getAddress()
            });
        }
    }

    private void addCustomer() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        // Validation
        if (name.isEmpty() || phone.isEmpty()) {
            showWarning("Name and Phone Number are mandatory fields.");
            return;
        }

        try {
            // Check Duplicates
            if (customerDAO.isDuplicate(name, phone)) {
                showWarning("A customer with the same name or phone number already exists.");
                return;
            }

            // Generate Next ID
            String nextId = customerDAO.getNextCustId();

            Customer customer = new Customer(nextId, name, phone, address);
            boolean success = customerDAO.insert(customer);
            
            if (success) {
                JOptionPane.showMessageDialog(this, 
                        "Customer added successfully!\nGenerated ID: " + nextId, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Clear fields
                txtName.setText("");
                txtPhone.setText("");
                txtAddress.setText("");

                // Refresh tables and notifications
                loadCustomers();
                mainFrame.refreshCustomerDropdowns();
            } else {
                showError("Failed to add customer. Please try again.");
            }

        } catch (SQLException e) {
            showError("Database error adding customer: " + e.getMessage());
        }
    }

    private void deleteCustomer() {
        int selectedRow = tblCustomers.getSelectedRow();
        if (selectedRow == -indexSelected()) {
            showWarning("Please select a customer from the table to delete.");
            return;
        }

        String custId = (String) tblCustomers.getValueAt(selectedRow, 0);
        String name = (String) tblCustomers.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete customer " + name + " (" + custId + ")?\nThis will also delete all their bills.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = customerDAO.delete(custId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Customer deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadCustomers();
                    mainFrame.refreshCustomerDropdowns();
                    mainFrame.refreshBillsHistory();
                    mainFrame.refreshProfitSummary();
                } else {
                    showError("Failed to delete customer.");
                }
            } catch (SQLException e) {
                showError("Database error deleting customer: " + e.getMessage());
            }
        }
    }

    private int indexSelected() {
        return 1; // Used as helper for empty selections
    }

    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
