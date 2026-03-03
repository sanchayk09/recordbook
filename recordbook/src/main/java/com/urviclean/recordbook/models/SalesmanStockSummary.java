package com.urviclean.recordbook.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for salesman_stock_summary table
 * Caches the current stock held by each salesman for each product
 * Updated automatically by database triggers
 */
@Entity
@Table(name = "salesman_stock_summary")
public class SalesmanStockSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "salesman_alias", nullable = false, length = 255)
    private String salesmanAlias;

    @Column(name = "product_code", nullable = false, length = 255)
    private String productCode;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public SalesmanStockSummary() {
        this.lastUpdated = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public SalesmanStockSummary(String salesmanAlias, String productCode, Integer currentStock) {
        this.salesmanAlias = salesmanAlias;
        this.productCode = productCode;
        this.currentStock = currentStock;
        this.lastUpdated = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSalesmanAlias() {
        return salesmanAlias;
    }

    public void setSalesmanAlias(String salesmanAlias) {
        this.salesmanAlias = salesmanAlias;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}

