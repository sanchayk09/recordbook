package com.urviclean.recordbook.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Warehouse Ledger - Audit trail for all warehouse stock movements
 */
@Entity
@Table(name = "warehouse_ledger")
public class WarehouseLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_ledger_id")
    private Long warehouseLedgerId;

    @Column(name = "product_code", nullable = false, length = 255)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false)
    private TransactionType txnType;

    @Column(name = "delta_qty", nullable = false)
    private Integer deltaQty;

    @Column(name = "qty_before", nullable = false)
    private Integer qtyBefore;

    @Column(name = "qty_after", nullable = false)
    private Integer qtyAfter;

    @Column(name = "salesman_alias", length = 255)
    private String salesmanAlias;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public enum TransactionType {
        TRANSFER_IN,
        ISSUE_TO_SALESMAN,
        RETURN_FROM_SALESMAN,
        MANUAL_ADJUST,
        DAMAGE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getWarehouseLedgerId() {
        return warehouseLedgerId;
    }

    public void setWarehouseLedgerId(Long warehouseLedgerId) {
        this.warehouseLedgerId = warehouseLedgerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public TransactionType getTxnType() {
        return txnType;
    }

    public void setTxnType(TransactionType txnType) {
        this.txnType = txnType;
    }

    public Integer getDeltaQty() {
        return deltaQty;
    }

    public void setDeltaQty(Integer deltaQty) {
        this.deltaQty = deltaQty;
    }

    public Integer getQtyBefore() {
        return qtyBefore;
    }

    public void setQtyBefore(Integer qtyBefore) {
        this.qtyBefore = qtyBefore;
    }

    public Integer getQtyAfter() {
        return qtyAfter;
    }

    public void setQtyAfter(Integer qtyAfter) {
        this.qtyAfter = qtyAfter;
    }

    public String getSalesmanAlias() {
        return salesmanAlias;
    }

    public void setSalesmanAlias(String salesmanAlias) {
        this.salesmanAlias = salesmanAlias;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
