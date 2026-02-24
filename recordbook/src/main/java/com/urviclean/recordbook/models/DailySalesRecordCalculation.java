package com.urviclean.recordbook.models;

import java.math.BigDecimal;

public class DailySalesRecordCalculation {
    private BigDecimal totalRevenue;
    private BigDecimal totalAgentCommission;

    public DailySalesRecordCalculation(BigDecimal totalRevenue, BigDecimal totalAgentCommission) {
        this.totalRevenue = totalRevenue;
        this.totalAgentCommission = totalAgentCommission;
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
        return "DailySalesRecordCalculation{" +
                "totalRevenue=" + totalRevenue +
                ", totalAgentCommission=" + totalAgentCommission +
                '}';
    }
}

