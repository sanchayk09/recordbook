package com.urviclean.recordbook.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Warehouse Inventory - Current stock available in warehouse (sellable stock only)
 * References product_cost_manual.product_code as the master product key
 */
@Entity
@Table(name = "warehouse_inventory")
public class WarehouseInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_inventory_id")
    private Long warehouseInventoryId;

    @Column(name = "product_code", nullable = false, unique = true, length = 20)
    private String productCode;

    @Column(name = "qty_available", nullable = false)
    private Integer qtyAvailable = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getWarehouseInventoryId() {
        return warehouseInventoryId;
    }

    public void setWarehouseInventoryId(Long warehouseInventoryId) {
        this.warehouseInventoryId = warehouseInventoryId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getQtyAvailable() {
        return qtyAvailable;
    }

    public void setQtyAvailable(Integer qtyAvailable) {
        this.qtyAvailable = qtyAvailable;
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
}

