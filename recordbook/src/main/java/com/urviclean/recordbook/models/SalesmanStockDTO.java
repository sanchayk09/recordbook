package com.urviclean.recordbook.models;

import java.util.List;

/**
 * DTO for Salesman Stock Summary Response
 * Shows which salesman has what products and quantities
 * Used for API responses, not database entity
 */
public class SalesmanStockDTO {

    private String salesmanAlias;
    private String firstName;
    private String lastName;
    private List<ProductStock> products;
    private Integer totalProducts;
    private Integer totalQuantity;

    // Getters and Setters

    public String getSalesmanAlias() {
        return salesmanAlias;
    }

    public void setSalesmanAlias(String salesmanAlias) {
        this.salesmanAlias = salesmanAlias;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<ProductStock> getProducts() {
        return products;
    }

    public void setProducts(List<ProductStock> products) {
        this.products = products;
    }

    public Integer getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Integer totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}

