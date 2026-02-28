package com.urviclean.recordbook.models;

import java.math.BigDecimal;

public class DailySalesRecordCalculation {
    private BigDecimal totalRevenue;
    private BigDecimal totalAgentCommission;
    private BigDecimal totalVolumeSold;
    private Long totalQuantity;

    public DailySalesRecordCalculation(BigDecimal totalRevenue, BigDecimal totalAgentCommission, BigDecimal totalVolumeSold, Long totalQuantity) {
        this.totalRevenue = totalRevenue;
        this.totalAgentCommission = totalAgentCommission;
        this.totalVolumeSold = totalVolumeSold;
        this.totalQuantity = totalQuantity;
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

    public BigDecimal getTotalVolumeSold() {
        return totalVolumeSold;
    }

    public void setTotalVolumeSold(BigDecimal totalVolumeSold) {
        this.totalVolumeSold = totalVolumeSold;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    @Override
    public String toString() {
        return "DailySalesRecordCalculation{" +
                "totalRevenue=" + totalRevenue +
                ", totalAgentCommission=" + totalAgentCommission +
                ", totalVolumeSold=" + totalVolumeSold +
                ", totalQuantity=" + totalQuantity +
                '}';
    }
}

