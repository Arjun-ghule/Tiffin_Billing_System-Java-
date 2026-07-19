package com.vasudha.tiffin.ui;

import com.vasudha.tiffin.dao.SettingsDAO;
import com.vasudha.tiffin.model.BusinessSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;

/**
 * UI Panel for editing and saving company business settings, logo, and payment QR.
 */
public class SettingsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final SettingsDAO settingsDAO;

    // UI Components
    private JTextField txtServiceName;
    private JTextField txtPhone;
    private JTextArea txtAddress;
    private JTextField txtLogoPath;
    private JTextField txtQRPath;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.settingsDAO = new SettingsDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create Panel Sections
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);

        // Load Settings on Start
        loadSettings();
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblHeader = new JLabel("Business Settings");
        lblHeader.setFont(new Font("Helvetica", Font.BOLD, 20));
        lblHeader.setForeground(new Color(33, 37, 41));
        panel.add(lblHeader);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Configure Business Invoice Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.gridx = 0;

        // Service Name
        gbc.gridy = 0; gbc.weightx = 0.0; gbc.gridwidth = 1;
        panel.add(new JLabel("Service Name:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtServiceName = new JTextField();
        panel.add(txtServiceName, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.gridwidth = 1;
        panel.add(new JLabel("Phone Number(s):"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtPhone = new JTextField();
        panel.add(txtPhone, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.gridwidth = 1;
        panel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.gridwidth = 2;
        txtAddress = new JTextArea(4, 25);
        txtAddress.setLineWrap(true);
        txtAddress.setWrapStyleWord(true);
        JScrollPane scrollAddress = new JScrollPane(txtAddress);
        panel.add(scrollAddress, gbc);

        // Logo Path
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0; gbc.gridwidth = 1;
        panel.add(new JLabel("Business Logo Path:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        txtLogoPath = new JTextField();
        txtLogoPath.setEditable(false);
        panel.add(txtLogoPath, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2; gbc.gridwidth = 1;
        JButton btnBrowseLogo = new JButton("Browse Logo");
        btnBrowseLogo.setBackground(new Color(108, 117, 125));
        btnBrowseLogo.setForeground(Color.WHITE);
        panel.add(btnBrowseLogo, gbc);

        // UPI QR Path
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0; gbc.gridwidth = 1;
        panel.add(new JLabel("UPI QR Code Path:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        txtQRPath = new JTextField();
        txtQRPath.setEditable(false);
        panel.add(txtQRPath, gbc);

        gbc.gridx = 2; gbc.weightx = 0.2; gbc.gridwidth = 1;
        JButton btnBrowseQR = new JButton("Browse UPI QR");
        btnBrowseQR.setBackground(new Color(108, 117, 125));
        btnBrowseQR.setForeground(Color.WHITE);
        panel.add(btnBrowseQR, gbc);

        // Action Buttons Row
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.weightx = 1.0;
        gbc.insets = new Insets(25, 15, 8, 15);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 5));
        
        JButton btnSave = new JButton("Save Settings");
        btnSave.setBackground(new Color(40, 167, 69));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Helvetica", Font.BOLD, 13));

        JButton btnApply = new JButton("Apply Now");
        btnApply.setBackground(new Color(0, 123, 255));
        btnApply.setForeground(Color.WHITE);
        btnApply.setFont(new Font("Helvetica", Font.BOLD, 13));

        buttonRow.add(btnSave);
        buttonRow.add(btnApply);
        panel.add(buttonRow, gbc);

        // Event Listeners for Browse Buttons
        btnBrowseLogo.addActionListener(e -> chooseImage(txtLogoPath));
        btnBrowseQR.addActionListener(e -> chooseImage(txtQRPath));

        // Save & Apply Listeners
        btnSave.addActionListener(e -> saveSettings(true));
        btnApply.addActionListener(e -> saveSettings(false));

        return panel;
    }

    private void chooseImage(JTextField targetField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Business Image (PNG, JPG, JPEG, BMP)");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Add File Extensions Filter
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Images (PNG, JPG, JPEG, BMP)", "png", "jpg", "jpeg", "bmp");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            targetField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void loadSettings() {
        try {
            BusinessSettings settings = settingsDAO.getSettings();
            if (settings != null) {
                txtServiceName.setText(settings.getServiceName());
                txtPhone.setText(settings.getPhone());
                txtAddress.setText(settings.getAddress());
                txtLogoPath.setText(settings.getLogoPath() != null ? settings.getLogoPath() : "");
                txtQRPath.setText(settings.getQrPath() != null ? settings.getQrPath() : "");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error loading settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveSettings(boolean showConfirmation) {
        String name = txtServiceName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String logo = txtLogoPath.getText().trim();
        String qr = txtQRPath.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Service Name, Phone, and Address are required.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BusinessSettings settings = new BusinessSettings(1, name, phone, address, logo, qr);
        try {
            boolean success = settingsDAO.updateSettings(settings);
            if (success) {
                if (showConfirmation) {
                    JOptionPane.showMessageDialog(this, "Settings saved successfully to the database.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Settings applied successfully! All future invoices will use this layout.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                loadSettings();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save settings.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error saving settings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
