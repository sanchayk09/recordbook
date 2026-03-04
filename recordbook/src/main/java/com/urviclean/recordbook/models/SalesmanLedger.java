package com.urviclean.recordbook.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * SalesmanLedger - Audit trail for all salesman stock movements
 *
 * Architecture: This table ONLY records movements of stock with salesmen.
 * - ISSUE_FROM_WAREHOUSE: when warehouse issues stock to salesman (+qty to salesman)
 * - SOLD: when salesman sells to customer (-qty from salesman)
 * - RETURN_TO_WAREHOUSE: when salesman returns stock to warehouse (-qty from salesman)
 * - MANUAL_ADJUST: manual inventory adjustments
 * - DAMAGE: damaged stock
 *
 * The warehouse_ledger is separate and only tracks warehouse movements.
 */
@Entity
@Table(name = "salesman_ledger", indexes = {
    @Index(name = "idx_sl_salesman_created", columnList = "salesman_alias, created_at"),
    @Index(name = "idx_sl_salesman_product_created", columnList = "salesman_alias, product_code, created_at")
})
public class SalesmanLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "salesman_ledger_id")
    private Long salesmanLedgerId;

    @Column(name = "salesman_alias", nullable = false, length = 255)
    private String salesmanAlias;

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "txn_type", nullable = false)
    private SalesmanTxnType txnType;

    @Column(name = "delta_qty", nullable = false)
    private Integer deltaQty;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = true)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // Constructors
    public SalesmanLedger() {
        this.createdAt = LocalDateTime.now();
    }

    public SalesmanLedger(String salesmanAlias, String productCode, SalesmanTxnType txnType,
                         Integer deltaQty, String remarks, String createdBy) {
        this.salesmanAlias = salesmanAlias;
        this.productCode = productCode;
        this.txnType = txnType;
        this.deltaQty = deltaQty;
        this.remarks = remarks;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getSalesmanLedgerId() {
        return salesmanLedgerId;
    }

    public void setSalesmanLedgerId(Long salesmanLedgerId) {
        this.salesmanLedgerId = salesmanLedgerId;
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

    public SalesmanTxnType getTxnType() {
        return txnType;
    }

    public void setTxnType(SalesmanTxnType txnType) {
        this.txnType = txnType;
    }

    public Integer getDeltaQty() {
        return deltaQty;
    }

    public void setDeltaQty(Integer deltaQty) {
        this.deltaQty = deltaQty;
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

    @Override
    public String toString() {
        return "SalesmanLedger{" +
                "salesmanLedgerId=" + salesmanLedgerId +
                ", salesmanAlias='" + salesmanAlias + '\'' +
                ", productCode='" + productCode + '\'' +
                ", txnType=" + txnType +
                ", deltaQty=" + deltaQty +
                ", remarks='" + remarks + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}

