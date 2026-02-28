package com.urviclean.recordbook.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_summary")
public class DailySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "salesman_alias", nullable = false)
    private String salesmanAlias;

    @Column(name = "sale_date", nullable = false, unique = true)
    private LocalDate saleDate;

    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "total_agent_commission", nullable = false)
    private BigDecimal totalAgentCommission;

    @Column(name = "total_expense", nullable = false)
    private BigDecimal totalExpense;

    @Column(name = "material_cost", nullable = false)
    private BigDecimal materialCost;

    @Column(name = "volume_sold", precision = 12, scale = 2)
    private BigDecimal volumeSold;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @Column(name = "net_profit")
    private BigDecimal netProfit;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateNetProfit();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateNetProfit();
    }

    /**
     * Calculate net profit: revenue - commission - expense - material cost
     */
    private void calculateNetProfit() {
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (totalAgentCommission == null) totalAgentCommission = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        if (materialCost == null) materialCost = BigDecimal.ZERO;

        this.netProfit = totalRevenue
                .subtract(totalAgentCommission)
                .subtract(totalExpense)
                .subtract(materialCost);
    }

    // Constructors
    public DailySummary() {
    }

    public DailySummary(String salesmanAlias, LocalDate saleDate,
                        BigDecimal totalRevenue, BigDecimal totalAgentCommission,
                        BigDecimal totalExpense, BigDecimal materialCost, BigDecimal volumeSold, Long totalQuantity) {
        this.salesmanAlias = salesmanAlias;
        this.saleDate = saleDate;
        this.totalRevenue = totalRevenue;
        this.totalAgentCommission = totalAgentCommission;
        this.totalExpense = totalExpense;
        this.materialCost = materialCost;
        this.volumeSold = volumeSold;
        this.totalQuantity = totalQuantity;
        calculateNetProfit();
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

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalAgentCommission() {
        return totalAgentCommission;
    }

    public void setTotalAgentCommission(BigDecimal totalAgentCommission) {
        this.totalAgentCommission = totalAgentCommission;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getMaterialCost() {
        return materialCost;
    }

    public void setMaterialCost(BigDecimal materialCost) {
        this.materialCost = materialCost;
    }

    public BigDecimal getVolumeSold() {
        return volumeSold;
    }

    public void setVolumeSold(BigDecimal volumeSold) {
        this.volumeSold = volumeSold;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
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

    @Override
    public String toString() {
        return "DailySummary{" +
                "id=" + id +
                ", salesmanAlias='" + salesmanAlias + '\'' +
                ", saleDate=" + saleDate +
                ", totalRevenue=" + totalRevenue +
                ", totalAgentCommission=" + totalAgentCommission +
                ", totalExpense=" + totalExpense +
                ", materialCost=" + materialCost +
                ", volumeSold=" + volumeSold +
                ", totalQuantity=" + totalQuantity +
                ", netProfit=" + netProfit +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

