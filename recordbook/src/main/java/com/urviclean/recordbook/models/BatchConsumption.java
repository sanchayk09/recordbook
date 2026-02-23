package com.urviclean.recordbook.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_consumption")
public class BatchConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumption_id")
    private Long consumptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductionBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chemical_id")
    private Chemical chemical;

    @Column(name = "qty_used")
    private BigDecimal qtyUsed;

    @Column(name = "unit_cost_at_time")
    private BigDecimal unitCostAtTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getConsumptionId() {
        return consumptionId;
    }

    public void setConsumptionId(Long consumptionId) {
        this.consumptionId = consumptionId;
    }

    public ProductionBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductionBatch batch) {
        this.batch = batch;
    }

    public Chemical getChemical() {
        return chemical;
    }

    public void setChemical(Chemical chemical) {
        this.chemical = chemical;
    }

    public BigDecimal getQtyUsed() {
        return qtyUsed;
    }

    public void setQtyUsed(BigDecimal qtyUsed) {
        this.qtyUsed = qtyUsed;
    }

    public BigDecimal getUnitCostAtTime() {
        return unitCostAtTime;
    }

    public void setUnitCostAtTime(BigDecimal unitCostAtTime) {
        this.unitCostAtTime = unitCostAtTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
