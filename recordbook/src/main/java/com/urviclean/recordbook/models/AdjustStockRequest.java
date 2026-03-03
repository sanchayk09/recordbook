package com.urviclean.recordbook.models;

/**
 * Request DTO for adjusting warehouse stock (TRANSFER_IN, MANUAL_ADJUST, DAMAGE)
 */
public class AdjustStockRequest {

    private String productCode;

    private Integer quantity;

    private String txnType; // TRANSFER_IN, MANUAL_ADJUST, DAMAGE

    private String remarks;

    private String createdBy;

    // Getters and Setters

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}

