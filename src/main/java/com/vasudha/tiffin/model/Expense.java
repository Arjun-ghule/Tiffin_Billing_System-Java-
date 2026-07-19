package com.vasudha.tiffin.model;

/**
 * Model class representing a business expense.
 */
public class Expense {
    private int id;
    private String description;
    private double amount;
    private String createdAt;

    // Constructors
    public Expense() {}

    public Expense(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public Expense(int id, String description, double amount, String createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
