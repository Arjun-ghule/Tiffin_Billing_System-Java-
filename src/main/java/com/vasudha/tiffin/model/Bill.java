package com.vasudha.tiffin.model;

/**
 * Model class representing a Tiffin Bill in the system.
 */
public class Bill {
    private int id;
    private int billNo;
    private String custId;
    private String custName;
    private String dateFrom;
    private String dateTo;
    private int morningCount;
    private double morningRate;
    private int nightCount;
    private double nightRate;
    private double subtotal;
    private double pendingAmount;
    private double advanceAmount;
    private double finalTotal;
    private String status;
    private String pdfPath;
    private String createdAt;

    // Constructors
    public Bill() {}

    public Bill(int billNo, String custId, String custName, String dateFrom, String dateTo,
                int morningCount, double morningRate, int nightCount, double nightRate,
                double subtotal, double pendingAmount, double advanceAmount, double finalTotal,
                String status, String pdfPath) {
        this.billNo = billNo;
        this.custId = custId;
        this.custName = custName;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.morningCount = morningCount;
        this.morningRate = morningRate;
        this.nightCount = nightCount;
        this.nightRate = nightRate;
        this.subtotal = subtotal;
        this.pendingAmount = pendingAmount;
        this.advanceAmount = advanceAmount;
        this.finalTotal = finalTotal;
        this.status = status;
        this.pdfPath = pdfPath;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBillNo() {
        return billNo;
    }

    public void setBillNo(int billNo) {
        this.billNo = billNo;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public int getMorningCount() {
        return morningCount;
    }

    public void setMorningCount(int morningCount) {
        this.morningCount = morningCount;
    }

    public double getMorningRate() {
        return morningRate;
    }

    public void setMorningRate(double morningRate) {
        this.morningRate = morningRate;
    }

    public int getNightCount() {
        return nightCount;
    }

    public void setNightCount(int nightCount) {
        this.nightCount = nightCount;
    }

    public double getNightRate() {
        return nightRate;
    }

    public void setNightRate(double nightRate) {
        this.nightRate = nightRate;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(double pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public double getAdvanceAmount() {
        return advanceAmount;
    }

    public void setAdvanceAmount(double advanceAmount) {
        this.advanceAmount = advanceAmount;
    }

    public double getFinalTotal() {
        return finalTotal;
    }

    public void setFinalTotal(double finalTotal) {
        this.finalTotal = finalTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
