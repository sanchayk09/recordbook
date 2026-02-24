package com.urviclean.recordbook.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductSalesDTO {
    private String productCode;
    private Long totalQuantity;
    private BigDecimal productCost;              // Cost per unit from product_cost_manual
    private BigDecimal totalCost;                // totalQuantity * productCost
    private BigDecimal totalRevenue;             // Sum of revenue for this product
    private BigDecimal averageRevenue;           // Average revenue per unit
    private BigDecimal salesmanCommission;       // Sum of agent_commission for this product
    private BigDecimal opCost;                   // Operational cost: opCostPerUnit * totalQuantity
    private BigDecimal totalProfit;              // Gross profit: totalRevenue - totalCost
    private BigDecimal netProfit;                // Net profit: totalRevenue - totalCost - salesmanCommission
    private BigDecimal avgProfit;                // Average profit per unit: totalProfit / totalQuantity
    private BigDecimal avgNetProfit;             // Average net profit per unit: netProfit / totalQuantity

    // Constructor used by JPA queries with revenue data
    public ProductSalesDTO(String productCode, Long totalQuantity, BigDecimal totalRevenue) {
        this.productCode = productCode;
        this.totalQuantity = totalQuantity;
        this.productCost = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
        this.salesmanCommission = BigDecimal.ZERO;
        this.opCost = BigDecimal.ZERO;
        this.totalProfit = BigDecimal.ZERO;
        this.netProfit = BigDecimal.ZERO;
        this.avgProfit = BigDecimal.ZERO;
        this.avgNetProfit = BigDecimal.ZERO;
        // Calculate average revenue as totalRevenue / totalQuantity
        if (totalQuantity != null && totalQuantity > 0 && totalRevenue != null) {
            this.averageRevenue = totalRevenue.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP);
        } else {
            this.averageRevenue = BigDecimal.ZERO;
        }
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getProductCost() {
        return productCost;
    }

    public void setProductCost(BigDecimal productCost) {
        this.productCost = productCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getAverageRevenue() {
        return averageRevenue;
    }

    public void setAverageRevenue(BigDecimal averageRevenue) {
        this.averageRevenue = averageRevenue;
    }

    public BigDecimal getSalesmanCommission() {
        return salesmanCommission;
    }

    public void setSalesmanCommission(BigDecimal salesmanCommission) {
        this.salesmanCommission = salesmanCommission;
    }

    public BigDecimal getOpCost() {
        return opCost;
    }

    public void setOpCost(BigDecimal opCost) {
        this.opCost = opCost;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public BigDecimal getAvgProfit() {
        return avgProfit;
    }

    public void setAvgProfit(BigDecimal avgProfit) {
        this.avgProfit = avgProfit;
    }

    public BigDecimal getAvgNetProfit() {
        return avgNetProfit;
    }

    public void setAvgNetProfit(BigDecimal avgNetProfit) {
        this.avgNetProfit = avgNetProfit;
    }

    @Override
    public String toString() {
        return "ProductSalesDTO{" +
                "productCode='" + productCode + '\'' +
                ", totalQuantity=" + totalQuantity +
                ", productCost=" + productCost +
                ", totalCost=" + totalCost +
                ", totalRevenue=" + totalRevenue +
                ", averageRevenue=" + averageRevenue +
                ", salesmanCommission=" + salesmanCommission +
                ", opCost=" + opCost +
                ", totalProfit=" + totalProfit +
                ", netProfit=" + netProfit +
                ", avgProfit=" + avgProfit +
                ", avgNetProfit=" + avgNetProfit +
                '}';
    }
}

