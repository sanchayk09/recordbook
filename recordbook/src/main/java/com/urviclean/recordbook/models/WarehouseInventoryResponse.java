package com.urviclean.recordbook.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for warehouse inventory
 */
public class WarehouseInventoryResponse {

    private Long warehouseInventoryId;
    private String productCode;
    private String productName;
    private String variant;
    private Integer qtyAvailable;
    private LocalDateTime lastUpdated;
    private String metric;
    private BigDecimal metricQuantity;
    private BigDecimal totalVolume; // Calculated: qtyAvailable * metricQuantity

    public WarehouseInventoryResponse() {
    }

    public WarehouseInventoryResponse(Long warehouseInventoryId, String productCode,
                                     String productName, String variant, Integer qtyAvailable,
                                     LocalDateTime lastUpdated) {
        this.warehouseInventoryId = warehouseInventoryId;
        this.productCode = productCode;
        this.productName = productName;
        this.variant = variant;
        this.qtyAvailable = qtyAvailable;
        this.lastUpdated = lastUpdated;
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

