package com.vasudha.tiffin.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.vasudha.tiffin.model.Bill;
import com.vasudha.tiffin.model.BusinessSettings;
import com.vasudha.tiffin.model.Expense;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating PDF Bills and Expense Reports.
 */
public class PDFService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Generates a PDF bill for a customer.
     * Saves it inside the bills/ directory.
     *
     * @param bill             the bill details to render
     * @param businessSettings business settings details (name, logo, qr, etc.)
     * @return the file path of the generated PDF
     * @throws DocumentException if PDF document error occurs
     * @throws IOException       if file I/O error occurs
     */
    public String generateBillPDF(Bill bill, BusinessSettings businessSettings) throws DocumentException, IOException {
        String filename = "bills/Bill_" + bill.getBillNo() + "_" + bill.getCustId() + ".pdf";
        File file = new File(filename);

        // Ensure parent directory exists
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Fonts
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(33, 37, 41));
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        Font subHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(73, 80, 87));
        Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(33, 37, 41));
        Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(73, 80, 87));
        Font footerFont = new Font(Font.HELVETICA, 10, Font.ITALIC, new Color(108, 117, 125));

        // --- BUSINESS HEADER ---
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{15f, 85f});

        // Logo cell (left)
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (businessSettings.getLogoPath() != null && !businessSettings.getLogoPath().isEmpty()) {
            File logoFile = new File(businessSettings.getLogoPath());
            if (logoFile.exists()) {
                try {
                    Image logoImg = Image.getInstance(businessSettings.getLogoPath());
                    logoImg.scaleToFit(60, 60);
                    logoCell.addElement(logoImg);
                } catch (Exception e) {
                    System.err.println("Warning: Could not add logo to PDF: " + e.getMessage());
                }
            }
        }
        headerTable.addCell(logoCell);

        // Business details cell (right)
        PdfPCell detailsCell = new PdfPCell();
        detailsCell.setBorder(PdfPCell.NO_BORDER);
        detailsCell.setPaddingLeft(10);
        detailsCell.addElement(new Paragraph(businessSettings.getServiceName(), titleFont));
        detailsCell.addElement(new Paragraph("Phone: " + businessSettings.getPhone(), normalFont));
        detailsCell.addElement(new Paragraph("Address: " + businessSettings.getAddress(), normalFont));
        headerTable.addCell(detailsCell);

        document.add(headerTable);

        // Solid separator line
        addHorizontalSeparator(document);

        // --- METADATA SECTION ---
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{50f, 50f});
        metaTable.setSpacingBefore(10);
        metaTable.setSpacingAfter(15);

        // Left: Customer details
        PdfPCell custCell = new PdfPCell();
        custCell.setBorder(PdfPCell.NO_BORDER);
        custCell.addElement(new Paragraph("Bill To:", subHeaderFont));
        custCell.addElement(new Paragraph("Customer ID: " + bill.getCustId(), boldFont));
        custCell.addElement(new Paragraph("Name: " + bill.getCustName(), boldFont));
        custCell.addElement(new Paragraph("Billing Period: " + bill.getDateFrom() + " to " + bill.getDateTo(), normalFont));
        metaTable.addCell(custCell);

        // Right: Bill details
        PdfPCell billMetaCell = new PdfPCell();
        billMetaCell.setBorder(PdfPCell.NO_BORDER);
        billMetaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Paragraph pBillNo = new Paragraph("Bill No: " + bill.getBillNo(), boldFont);
        pBillNo.setAlignment(Element.ALIGN_RIGHT);
        billMetaCell.addElement(pBillNo);

        Paragraph pBillDate = new Paragraph("Bill Date: " + LocalDate.now().format(DATE_FORMATTER), normalFont);
        pBillDate.setAlignment(Element.ALIGN_RIGHT);
        billMetaCell.addElement(pBillDate);

        Paragraph pStatus = new Paragraph("Status: " + bill.getStatus(), boldFont);
        pStatus.setAlignment(Element.ALIGN_RIGHT);
        billMetaCell.addElement(pStatus);
        
        metaTable.addCell(billMetaCell);

        document.add(metaTable);

        // --- TIFFIN DETAILS TABLE ---
        PdfPTable detailsTable = new PdfPTable(4);
        detailsTable.setWidthPercentage(100);
        detailsTable.setWidths(new float[]{40f, 20f, 20f, 20f});
        detailsTable.setSpacingAfter(15);

        // Table Header
        String[] headers = {"Tiffin Service Type", "Quantity / Count", "Rate (Rs.)", "Total Amount"};
        for (String headerText : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(headerText, headerFont));
            cell.setBackgroundColor(new Color(52, 58, 64)); // Dark Gray header
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            detailsTable.addCell(cell);
        }

        // Row 1: Morning Tiffin
        addDetailsRow(detailsTable, "Morning Tiffin", bill.getMorningCount(), bill.getMorningRate(), normalFont);

        // Row 2: Night Tiffin
        addDetailsRow(detailsTable, "Night Tiffin", bill.getNightCount(), bill.getNightRate(), normalFont);

        document.add(detailsTable);

        // --- TOTALS & SETTLEMENTS SECTION ---
        PdfPTable settlementTable = new PdfPTable(2);
        settlementTable.setWidthPercentage(100);
        settlementTable.setWidths(new float[]{60f, 40f});

        // Left Column (Empty Spacer)
        PdfPCell spacerCell = new PdfPCell();
        spacerCell.setBorder(PdfPCell.NO_BORDER);
        settlementTable.addCell(spacerCell);

        // Right Column (Subtotal, adjustments, and Final Total)
        PdfPCell totalsCell = new PdfPCell();
        totalsCell.setBorder(PdfPCell.NO_BORDER);
        
        // Subtotal row
        addTotalRow(totalsCell, "Subtotal:", bill.getSubtotal(), normalFont, Element.ALIGN_RIGHT);

        // Pending Amount (if applicable)
        if (bill.getPendingAmount() > 0) {
            addTotalRow(totalsCell, "Pending Amount (+):", bill.getPendingAmount(), normalFont, Element.ALIGN_RIGHT);
        }

        // Advance Amount (if applicable)
        if (bill.getAdvanceAmount() > 0) {
            addTotalRow(totalsCell, "Advance Paid (-):", bill.getAdvanceAmount(), normalFont, Element.ALIGN_RIGHT);
        }

        // Final Total Row (Highlight)
        PdfPTable finalTable = new PdfPTable(2);
        finalTable.setWidthPercentage(100);
        finalTable.setWidths(new float[]{55f, 45f});
        finalTable.setSpacingBefore(5);

        PdfPCell lblCell = new PdfPCell(new Phrase("Final Total (Rs.):", boldFont));
        lblCell.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM);
        lblCell.setBorderWidth(1f);
        lblCell.setPadding(5);
        lblCell.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell valCell = new PdfPCell(new Phrase(String.format("%.2f", bill.getFinalTotal()), boldFont));
        valCell.setBorder(PdfPCell.TOP | PdfPCell.BOTTOM);
        valCell.setBorderWidth(1f);
        valCell.setPadding(5);
        valCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        finalTable.addCell(lblCell);
        finalTable.addCell(valCell);
        totalsCell.addElement(finalTable);

        settlementTable.addCell(totalsCell);
        document.add(settlementTable);

        // --- PAYMENT / UPI QR CODE SECTION ---
        addHorizontalSeparator(document);

        PdfPTable paymentTable = new PdfPTable(2);
        paymentTable.setWidthPercentage(100);
        paymentTable.setWidths(new float[]{30f, 70f});
        paymentTable.setSpacingBefore(10);

        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(PdfPCell.NO_BORDER);
        qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        boolean hasQR = false;
        if (businessSettings.getQrPath() != null && !businessSettings.getQrPath().isEmpty()) {
            File qrFile = new File(businessSettings.getQrPath());
            if (qrFile.exists()) {
                try {
                    Image qrImg = Image.getInstance(businessSettings.getQrPath());
                    qrImg.scaleToFit(90, 90);
                    qrCell.addElement(qrImg);
                    hasQR = true;
                } catch (Exception e) {
                    System.err.println("Warning: Could not add QR to PDF: " + e.getMessage());
                }
            }
        }
        paymentTable.addCell(qrCell);

        PdfPCell payInstructionCell = new PdfPCell();
        payInstructionCell.setBorder(PdfPCell.NO_BORDER);
        payInstructionCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        if (hasQR) {
            payInstructionCell.addElement(new Paragraph("Scan & Pay — All UPI Apps Accepted", boldFont));
        } else {
            payInstructionCell.addElement(new Paragraph("Payment Instructions:", boldFont));
        }
        payInstructionCell.addElement(new Paragraph("You can make payments to: " + businessSettings.getPhone(), normalFont));
        payInstructionCell.addElement(new Paragraph("Please notify once payment is completed.", normalFont));
        paymentTable.addCell(payInstructionCell);

        document.add(paymentTable);

        // --- FOOTER "THANK YOU" ---
        Paragraph thankYou = new Paragraph("Thank You for choosing " + businessSettings.getServiceName() + "!", footerFont);
        thankYou.setAlignment(Element.ALIGN_CENTER);
        thankYou.setSpacingBefore(30);
        document.add(thankYou);

        document.close();
        return file.getAbsolutePath();
    }

    /**
     * Generates a monthly expense report PDF.
     */
    public String generateExpenseReportPDF(int month, int year, double revenue, double expenses, double netProfit, List<Expense> expenseList) throws DocumentException, IOException {
        String monthName = java.time.Month.of(month).name();
        // Capitalize month name
        monthName = monthName.substring(0, 1) + monthName.substring(1).toLowerCase();

        String filename = "bills/ExpenseReport_" + month + "_" + year + ".pdf";
        File file = new File(filename);

        // Ensure parent directory exists
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Fonts
        Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(33, 37, 41));
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font subHeaderFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(73, 80, 87));
        Font boldFont = new Font(Font.HELVETICA, 9, Font.BOLD, new Color(33, 37, 41));
        Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(73, 80, 87));

        // Report Title
        Paragraph pTitle = new Paragraph("Monthly Expense & Profitability Report", titleFont);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(pTitle);

        Paragraph pSubtitle = new Paragraph("Period: " + monthName + " " + year, subHeaderFont);
        pSubtitle.setAlignment(Element.ALIGN_CENTER);
        pSubtitle.setSpacingAfter(10);
        document.add(pSubtitle);

        Paragraph pGenDate = new Paragraph("Generated On: " + LocalDate.now().format(DATE_FORMATTER), normalFont);
        pGenDate.setAlignment(Element.ALIGN_CENTER);
        pGenDate.setSpacingAfter(15);
        document.add(pGenDate);

        addHorizontalSeparator(document);

        // --- SUMMARY TABLE ---
        Paragraph pSumTitle = new Paragraph("Financial Summary", subHeaderFont);
        pSumTitle.setSpacingBefore(10);
        pSumTitle.setSpacingAfter(8);
        document.add(pSumTitle);

        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{33.33f, 33.33f, 33.34f});
        summaryTable.setSpacingAfter(20);

        // Summary Headers
        String[] sumHeaders = {"Total Paid Revenue (Rs.)", "Total Expenses (Rs.)", "Net Profit / Loss (Rs.)"};
        for (String sumHeader : sumHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(sumHeader, headerFont));
            cell.setBackgroundColor(new Color(52, 58, 64));
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            summaryTable.addCell(cell);
        }

        // Summary Data Row
        PdfPCell revCell = new PdfPCell(new Phrase(String.format("%.2f", revenue), boldFont));
        revCell.setPadding(8);
        revCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(revCell);

        PdfPCell expCell = new PdfPCell(new Phrase(String.format("%.2f", expenses), boldFont));
        expCell.setPadding(8);
        expCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(expCell);

        // Color code net profit
        Font profitFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        if (netProfit >= 0) {
            profitFont.setColor(new Color(40, 167, 69)); // Green
        } else {
            profitFont.setColor(new Color(220, 53, 69)); // Red
        }
        PdfPCell profitCell = new PdfPCell(new Phrase(String.format("%.2f", netProfit), profitFont));
        profitCell.setPadding(8);
        profitCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(profitCell);

        document.add(summaryTable);

        // --- EXPENSES DETAILS TABLE ---
        Paragraph pDetailsTitle = new Paragraph("Expenses Break-down", subHeaderFont);
        pDetailsTitle.setSpacingAfter(8);
        document.add(pDetailsTitle);

        PdfPTable expensesTable = new PdfPTable(3);
        expensesTable.setWidthPercentage(100);
        expensesTable.setWidths(new float[]{50f, 25f, 25f});

        // Table Header
        String[] expTableHeaders = {"Description", "Amount (Rs.)", "Date Recorded"};
        for (String headerText : expTableHeaders) {
            PdfPCell cell = new PdfPCell(new Phrase(headerText, headerFont));
            cell.setBackgroundColor(new Color(108, 117, 125)); // Lighter Gray
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            expensesTable.addCell(cell);
        }

        if (expenseList == null || expenseList.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No expenses recorded for this period.", normalFont));
            emptyCell.setColspan(3);
            emptyCell.setPadding(10);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            expensesTable.addCell(emptyCell);
        } else {
            for (Expense exp : expenseList) {
                // Description
                PdfPCell dCell = new PdfPCell(new Phrase(exp.getDescription(), normalFont));
                dCell.setPadding(5);
                expensesTable.addCell(dCell);

                // Amount
                PdfPCell aCell = new PdfPCell(new Phrase(String.format("%.2f", exp.getAmount()), normalFont));
                aCell.setPadding(5);
                aCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                expensesTable.addCell(aCell);

                // Date
                String dateStr = exp.getCreatedAt();
                // If it includes time, take only the date portion (YYYY-MM-DD)
                if (dateStr != null && dateStr.length() >= 10) {
                    dateStr = dateStr.substring(0, 10);
                }
                PdfPCell dtCell = new PdfPCell(new Phrase(dateStr != null ? dateStr : "", normalFont));
                dtCell.setPadding(5);
                dtCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                expensesTable.addCell(dtCell);
            }
        }

        document.add(expensesTable);
        document.close();

        return file.getAbsolutePath();
    }

    private void addHorizontalSeparator(Document document) throws DocumentException {
        PdfPTable separatorLine = new PdfPTable(1);
        separatorLine.setWidthPercentage(100);
        separatorLine.setSpacingBefore(8);
        separatorLine.setSpacingAfter(8);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorder(PdfPCell.BOTTOM);
        lineCell.setBorderWidth(1.2f);
        lineCell.setBorderColor(new Color(206, 212, 218));
        lineCell.setPadding(0);
        separatorLine.addCell(lineCell);
        document.add(separatorLine);
    }

    private void addDetailsRow(PdfPTable table, String type, int count, double rate, Font font) {
        // Type
        PdfPCell cell1 = new PdfPCell(new Phrase(type, font));
        cell1.setPadding(5);
        cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell1);

        // Count
        PdfPCell cell2 = new PdfPCell(new Phrase(String.valueOf(count), font));
        cell2.setPadding(5);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell2);

        // Rate
        PdfPCell cell3 = new PdfPCell(new Phrase(String.format("%.2f", rate), font));
        cell3.setPadding(5);
        cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell3);

        // Total
        double rowTotal = count * rate;
        PdfPCell cell4 = new PdfPCell(new Phrase(String.format("%.2f", rowTotal), font));
        cell4.setPadding(5);
        cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cell4);
    }

    private void addTotalRow(PdfPCell parentCell, String label, double amount, Font font, int alignment) {
        PdfPTable rowTable = new PdfPTable(2);
        rowTable.setWidthPercentage(100);
        try {
            rowTable.setWidths(new float[]{60f, 40f});
        } catch (DocumentException e) {
            // ignore
        }

        PdfPCell lbl = new PdfPCell(new Phrase(label, font));
        lbl.setBorder(PdfPCell.NO_BORDER);
        lbl.setPadding(2);
        lbl.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell val = new PdfPCell(new Phrase(String.format("%.2f", amount), font));
        val.setBorder(PdfPCell.NO_BORDER);
        val.setPadding(2);
        val.setHorizontalAlignment(alignment);

        rowTable.addCell(lbl);
        rowTable.addCell(val);
        parentCell.addElement(rowTable);
    }
}
