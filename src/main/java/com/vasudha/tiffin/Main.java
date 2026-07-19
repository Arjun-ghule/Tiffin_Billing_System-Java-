package com.vasudha.tiffin;

import com.vasudha.tiffin.database.DatabaseManager;
import com.vasudha.tiffin.ui.MainFrame;
import com.vasudha.tiffin.util.FileUtil;

import javax.swing.*;

/**
 * Entry point for the Vasudha Tiffin Billing System.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Set System Look and Feel for a native, clean desktop UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set System Look and Feel: " + e.getMessage());
        }

        // 2. Ensure bills/ folder exists
        FileUtil.ensureBillsDirectory();

        // 3. Initialize Database and default tables/configurations
        DatabaseManager.initializeDatabase();

        // 4. Start the desktop user interface safely on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            }
        });
    }
}
