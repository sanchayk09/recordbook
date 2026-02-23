package com.urviclean.recordbook.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "production_batches")
public class ProductionBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long batchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "total_qty_produced")
    private BigDecimal totalQtyProduced;

    @Column(name = "remaining_qty")
    private BigDecimal remainingQty;

    @Column(name = "calculated_mfg_cost_per_unit")
    private BigDecimal calculatedMfgCostPerUnit;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SalesRecord> salesRecords;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getTotalQtyProduced() {
        return totalQtyProduced;
    }

    public void setTotalQtyProduced(BigDecimal totalQtyProduced) {
        this.totalQtyProduced = totalQtyProduced;
    }

    public BigDecimal getRemainingQty() {
        return remainingQty;
    }

    public void setRemainingQty(BigDecimal remainingQty) {
        this.remainingQty = remainingQty;
    }

    public BigDecimal getCalculatedMfgCostPerUnit() {
        return calculatedMfgCostPerUnit;
    }

    public void setCalculatedMfgCostPerUnit(BigDecimal calculatedMfgCostPerUnit) {
        this.calculatedMfgCostPerUnit = calculatedMfgCostPerUnit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<SalesRecord> getSalesRecords() {
        return salesRecords;
    }

    public void setSalesRecords(List<SalesRecord> salesRecords) {
        this.salesRecords = salesRecords;
    }

}
