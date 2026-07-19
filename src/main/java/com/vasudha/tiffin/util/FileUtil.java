package com.vasudha.tiffin.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for handling file path creation and opening PDFs.
 */
public class FileUtil {

    private static final String BILLS_DIR = "bills";

    /**
     * Ensures that the bills directory exists. Creates it if it doesn't.
     * @return the File representing the bills directory.
     */
    public static File ensureBillsDirectory() {
        File dir = new File(BILLS_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created bills directory.");
            }
        }
        return dir;
    }

    /**
     * Opens the specified PDF file using the default operating system PDF viewer.
     * @param filepath absolute or relative path to the PDF file.
     * @return true if successful, false otherwise.
     */
    public static boolean openPDF(String filepath) {
        if (filepath == null || filepath.trim().isEmpty()) {
            System.err.println("Invalid PDF path: " + filepath);
            return false;
        }

        File file = new File(filepath);
        if (!file.exists()) {
            System.err.println("PDF file does not exist: " + file.getAbsolutePath());
            return false;
        }

        if (!Desktop.isDesktopSupported()) {
            System.err.println("Desktop operations are not supported on this platform.");
            return false;
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            System.err.println("Open operation is not supported by Desktop on this platform.");
            return false;
        }

        try {
            desktop.open(file);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to open PDF file: " + e.getMessage());
            // Fallback for Windows using command execution if Desktop fails
            try {
                new ProcessBuilder("cmd", "/c", "start", "", file.getAbsolutePath()).start();
                return true;
            } catch (IOException ex) {
                System.err.println("Fallback opening mechanism also failed: " + ex.getMessage());
                return false;
            }
        }
    }
}
