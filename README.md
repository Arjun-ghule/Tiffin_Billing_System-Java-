# Tiffin Billing System

A professional, offline, clean Java desktop application for tiffin service businesses to manage customers, record tiffin counts, calculate monthly bills, generate A4 PDF invoices with payment UPI QR codes, track expenses, and view profitability.

---

## 1. Project Overview
The ** Tiffin Billing System** is designed to replace paper registers or spreadsheets for a tiffin service. It manages customers, allows monthly tiffin consumption calculations (morning and night counts at custom rates), handles adjustments like pending balances or advance payments, generates PDF bills, tracks business expenses, and outputs comprehensive monthly profit/loss reports.

---

## 2. Features
- **Customer Management**: Add, delete, and search customers with automatic ID generation (`C0001`, `C0002`, etc.) and validation to prevent duplicates.
- **Tiffin Billing Calculation**: Compute monthly balances based on morning and night tiffin counts and rates. Supports adjusting final sums with pending or advance amounts (validated to avoid concurrent entries).
- **Invoice PDF Generation**: Creates professional A4 invoices containing business info, customer info, bill breakdown, and configured logos and payment UPI QR codes.
- **Bill Payment Status**: Track and manually update payment status (`PAID`, `UNPAID`, `PAID (Advance)`).
- **Customer Billing History**: Drill down into any customer's complete invoice history and open past PDF bills directly.
- **Monthly Expenses Tracker**: Log operating expenses with description and amount.
- **Financial Summary & Profitability Dashboard**: Get real-time insight on revenue (calculated from PAID bills), expenses, and net profit with visual color-coding (Green for profit, Red for loss).
- **Expense PDF Reports**: Compile A4 monthly expense details and summaries into printable files.
- **Business Settings**: Configure the company's name, phone, address, invoice logo, and UPI payment QR codes.

---

## 3. Technology Stack
- **Language**: Java 17+
- **GUI Framework**: Java Swing
- **Database Engine**: SQLite
- **Connectivity**: JDBC (SQLite-JDBC Driver)
- **PDF Generation**: OpenPDF (An active, open-source LGPL/MPL fork of iText)
- **Build Tool**: Maven

---

## 4. Prerequisites
To compile and run this desktop application:
1. **Java Development Kit (JDK)**: JDK 17 or newer (discovered at `C:\oracleJdk-26` on this machine).
2. **Apache Maven**: Make sure Maven is installed and added to your system `PATH`.

---

## 5. How to Build
To clean and compile the project into a target folder, execute the following Maven command:
```bash
mvn clean compile
```

To package the application into a runnable JAR file containing all classes:
```bash
mvn clean package
```

---

## 6. How to Run
Once compiled, you can run the application directly using the exec plugin:
```bash
mvn exec:java
```

Or run the packaged JAR file using:
```bash
java -jar target/TiffinBilling-1.0-SNAPSHOT.jar
```

*Note: On this local machine, you can run it by referencing the Oracle JDK path:*
```bash
& "C:\oracleJdk-26\bin\java.exe" -cp "path_to_jar_and_dependencies" com.tiffin.Main
```

---

## 7. Database Location
- **Filename**: `tiffin_billing.db`
- **Location**: Automatically created at the root directory of the project upon startup.
- **Structure**: Uses tables for `settings`, `customers`, `bills`, and `expenses`. Foreign key constraints are enabled with cascade deletions, meaning deleting a customer will automatically clean up their corresponding billing files from the database.

---

## 8. Generated Bills Location
- **Folder**: `bills/`
- **Location**: Automatically created at the root directory of the project.
- **Invoices**: Filename structure: `Bill_[BILLNUMBER]_[CUSTOMERID].pdf` (e.g. `bills/Bill_1001_C0001.pdf`).
- **Reports**: Filename structure: `ExpenseReport_[MONTH]_[YEAR].pdf` (e.g. `bills/ExpenseReport_6_2026.pdf`).

---

