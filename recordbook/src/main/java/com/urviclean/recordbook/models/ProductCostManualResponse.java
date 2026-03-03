package com.urviclean.recordbook.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductCostManualResponse {

    private Long pid;
    private String productName;
    private String productCode;
    private BigDecimal cost;
    private String variant;
    private String metric;
    private BigDecimal metricQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ProductCostManualResponse() {
    }

    public ProductCostManualResponse(Long pid, String productName, String productCode,
                                     BigDecimal cost, String variant, String metric,
                                     BigDecimal metricQuantity, LocalDateTime createdAt,
                                     LocalDateTime updatedAt) {
        this.pid = pid;
        this.productName = productName;
        this.productCode = productCode;
        this.cost = cost;
        this.variant = variant;
        this.metric = metric;
        this.metricQuantity = metricQuantity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Factory method to convert from entity
    public static ProductCostManualResponse fromEntity(ProductCostManual entity) {
        return new ProductCostManualResponse(
                entity.getPid(),
                entity.getProductName(),
                entity.getProductCode(),
                entity.getCost(),
                entity.getVariant(),
                entity.getMetric(),
                entity.getMetricQuantity(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    // Getters and Setters
    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

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

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ProductCostManualResponse{" +
                "pid=" + pid +
                ", productName='" + productName + '\'' +
                ", productCode='" + productCode + '\'' +
                ", cost=" + cost +
                ", variant='" + variant + '\'' +
                ", metric='" + metric + '\'' +
                ", metricQuantity=" + metricQuantity +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

