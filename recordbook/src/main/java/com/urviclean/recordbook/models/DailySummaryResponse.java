package com.urviclean.recordbook.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailySummaryResponse {
    private Long id;
    private String salesmanAlias;
    private LocalDate saleDate;
    private BigDecimal totalRevenue;
    private BigDecimal totalAgentCommission;
    private BigDecimal totalExpense;
    private BigDecimal materialCost;
    private BigDecimal netProfit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DailySummaryResponse() {
    }

    public DailySummaryResponse(DailySummary entity) {
        this.id = entity.getId();
        this.salesmanAlias = entity.getSalesmanAlias();
        this.saleDate = entity.getSaleDate();
        this.totalRevenue = entity.getTotalRevenue();
        this.totalAgentCommission = entity.getTotalAgentCommission();
        this.totalExpense = entity.getTotalExpense();
        this.materialCost = entity.getMaterialCost();
        this.netProfit = entity.getNetProfit();
        this.createdAt = entity.getCreatedAt();
        this.updatedAt = entity.getUpdatedAt();
    }

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
        return "DailySummaryResponse{" +
                "id=" + id +
                ", salesmanAlias='" + salesmanAlias + '\'' +
                ", saleDate=" + saleDate +
                ", totalRevenue=" + totalRevenue +
                ", totalAgentCommission=" + totalAgentCommission +
                ", totalExpense=" + totalExpense +
                ", materialCost=" + materialCost +
                ", netProfit=" + netProfit +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}

