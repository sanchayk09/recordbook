package com.urviclean.recordbook.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    private String variant;

    private String size;

    @Column(name = "target_price")
    private BigDecimal targetPrice;

    @Column(name = "base_commission")
    private BigDecimal baseCommission;

    @Column(name = "other_overhead_cost")
    private BigDecimal otherOverheadCost;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductRecipe> productRecipes;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionBatch> productionBatches;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalesRecord> salesRecords;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }

    public BigDecimal getBaseCommission() {
        return baseCommission;
    }

    public void setBaseCommission(BigDecimal baseCommission) {
        this.baseCommission = baseCommission;
    }

    public BigDecimal getOtherOverheadCost() {
        return otherOverheadCost;
    }

    public void setOtherOverheadCost(BigDecimal otherOverheadCost) {
        this.otherOverheadCost = otherOverheadCost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ProductRecipe> getProductRecipes() {
        return productRecipes;
    }

    public void setProductRecipes(List<ProductRecipe> productRecipes) {
        this.productRecipes = productRecipes;
    }

    public List<ProductionBatch> getProductionBatches() {
        return productionBatches;
    }

    public void setProductionBatches(List<ProductionBatch> productionBatches) {
        this.productionBatches = productionBatches;
    }

    public List<SalesRecord> getSalesRecords() {
        return salesRecords;
    }

    public void setSalesRecords(List<SalesRecord> salesRecords) {
        this.salesRecords = salesRecords;
    }
}
