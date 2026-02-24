package com.urviclean.recordbook.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductCostManualRequest {

    private String productName;
    private String productCode;
    private BigDecimal cost;

    // Constructors
    public ProductCostManualRequest() {
    }

    public ProductCostManualRequest(String productName, String productCode, BigDecimal cost) {
        this.productName = productName;
        this.productCode = productCode;
        this.cost = cost;
    }

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    @Override
    public String toString() {
        return "ProductCostManualRequest{" +
                "productName='" + productName + '\'' +
                ", productCode='" + productCode + '\'' +
                ", cost=" + cost +
                '}';
    }
}

