package com.urviclean.recordbook.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_cost_manual")
public class ProductCostManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pid")
    private Long pid;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_code", nullable = false, unique = true, length = 255)
    private String productCode;

    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    @Column(name = "variant", nullable = true)
    private String variant;

    @Column(name = "metric", nullable = true)
    private String metric;

    @Column(name = "metric_quantity", nullable = true)
    private BigDecimal metricQuantity;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public ProductCostManual() {
    }

    public ProductCostManual(String productName, String productCode, BigDecimal cost) {
        this.productName = productName;
        this.productCode = productCode;
        this.cost = cost;
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

    @Override
    public String toString() {
        return "ProductCostManual{" +
                "pid=" + pid +
                ", productName='" + productName + '\'' +
                ", productCode='" + productCode + '\'' +
                ", variant='" + variant + '\'' +
                ", cost=" + cost +
                ", metric='" + metric + '\'' +
                ", metricQuantity=" + metricQuantity +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

