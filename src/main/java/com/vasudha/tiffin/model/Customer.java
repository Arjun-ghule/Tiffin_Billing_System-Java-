package com.vasudha.tiffin.model;

/**
 * Model class representing a Customer in the Vasudha Tiffin Billing System.
 */
public class Customer {
    private int id;
    private String custId;
    private String name;
    private String phone;
    private String address;

    // Constructors
    public Customer() {}

    public Customer(String custId, String name, String phone, String address) {
        this.custId = custId;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public Customer(int id, String custId, String name, String phone, String address) {
        this.id = id;
        this.custId = custId;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * String representation for the customer dropdown list: "CustomerID | Customer Name (Phone)"
     */
    @Override
    public String toString() {
        return custId + " | " + name + " (" + phone + ")";
    }
}
