package com.urviclean.recordbook.models;

import java.math.BigDecimal;

public class ProductSalesDTO {
    private String productCode;
    private Long totalQuantity;
    private BigDecimal productCost;        // Cost per unit from product_cost_manual
    private BigDecimal totalCost;          // totalQuantity * productCost

    public ProductSalesDTO(String productCode, Long totalQuantity) {
        this.productCode = productCode;
        this.totalQuantity = totalQuantity;
        this.productCost = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    public ProductSalesDTO(String productCode, Long totalQuantity, BigDecimal productCost, BigDecimal totalCost) {
        this.productCode = productCode;
        this.totalQuantity = totalQuantity;
        this.productCost = productCost;
        this.totalCost = totalCost;
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

    @Override
    public String toString() {
        return "ProductSalesDTO{" +
                "productCode='" + productCode + '\'' +
                ", totalQuantity=" + totalQuantity +
                ", productCost=" + productCost +
                ", totalCost=" + totalCost +
                '}';
    }
}

