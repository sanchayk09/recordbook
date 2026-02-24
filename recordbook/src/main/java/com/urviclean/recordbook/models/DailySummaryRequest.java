package com.urviclean.recordbook.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySummaryRequest {
    public String salesmanAlias;
    public LocalDate saleDate;
    public BigDecimal materialCost;
    public BigDecimal totalExpense;
    public BigDecimal totalRevenue;
    public BigDecimal totalAgentCommission;

    public DailySummaryRequest() {
    }

    public DailySummaryRequest(String salesmanAlias, LocalDate saleDate,
                               BigDecimal materialCost, BigDecimal totalExpense,
                               BigDecimal totalRevenue, BigDecimal totalAgentCommission) {
        this.salesmanAlias = salesmanAlias;
        this.saleDate = saleDate;
        this.materialCost = materialCost;
        this.totalExpense = totalExpense;
        this.totalRevenue = totalRevenue;
        this.totalAgentCommission = totalAgentCommission;
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

    public BigDecimal getMaterialCost() {
        return materialCost;
    }

    public void setMaterialCost(BigDecimal materialCost) {
        this.materialCost = materialCost;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
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

    @Override
    public String toString() {
        return "DailySummaryRequest{" +
                "salesmanAlias='" + salesmanAlias + '\'' +
                ", saleDate=" + saleDate +
                ", materialCost=" + materialCost +
                ", totalExpense=" + totalExpense +
                ", totalRevenue=" + totalRevenue +
                ", totalAgentCommission=" + totalAgentCommission +
                '}';
    }
}

