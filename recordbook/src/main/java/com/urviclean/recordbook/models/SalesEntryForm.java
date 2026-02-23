package com.urviclean.recordbook.models;

import java.time.LocalDate;

public class SalesEntryForm {
    // 1. Who and When
    private LocalDate entryDate;      // Defaults to today if empty
    private String salesmanName;      // e.g. "Mukul"

    // 2. The Customer (Raw Data)
    private String customerName;      // e.g. "Hari Store"
    private String mobileNumber;      // e.g. "9876543210"
    private String location;          // e.g. "Kasmar"

    // 3. The Product Sale
    private String productCode;       // e.g. "L-1L"
    private int quantity;             // e.g. 10
    private double soldRate;          // e.g. 35.00

    // 4. Financials (NEW)
    private Double totalAmount;       // e.g. 350.00 (Optional, can be calculated)

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSoldRate() {
        return soldRate;
    }

    public void setSoldRate(double soldRate) {
        this.soldRate = soldRate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}