package com.urviclean.recordbook.models;

import java.math.BigDecimal;

/**
 * DTO for Product Stock details
 * Used in Salesman Stock Summary
 */
public class ProductStock {

    private String productCode;
    private String productName;
    private String variant;
    private Integer quantity;
    private String metric;
    private BigDecimal metricQuantity;
    private BigDecimal totalVolume; // Calculated: quantity * metricQuantity

    // Getters and Setters

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public BigDecimal getMetricQuantity() {
        return metricQuantity;
    }

    public void setMetricQuantity(BigDecimal metricQuantity) {
        this.metricQuantity = metricQuantity;
    }

    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }
}
