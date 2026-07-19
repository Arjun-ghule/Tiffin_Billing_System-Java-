package com.vasudha.tiffin.model;

/**
 * Model class representing the single-row business configuration settings.
 */
public class BusinessSettings {
    private int id;
    private String serviceName;
    private String phone;
    private String address;
    private String logoPath;
    private String qrPath;

    // Constructors
    public BusinessSettings() {}

    public BusinessSettings(String serviceName, String phone, String address, String logoPath, String qrPath) {
        this.serviceName = serviceName;
        this.phone = phone;
        this.address = address;
        this.logoPath = logoPath;
        this.qrPath = qrPath;
    }

    public BusinessSettings(int id, String serviceName, String phone, String address, String logoPath, String qrPath) {
        this.id = id;
        this.serviceName = serviceName;
        this.phone = phone;
        this.address = address;
        this.logoPath = logoPath;
        this.qrPath = qrPath;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getQrPath() {
        return qrPath;
    }

    public void setQrPath(String qrPath) {
        this.qrPath = qrPath;
    }
}
