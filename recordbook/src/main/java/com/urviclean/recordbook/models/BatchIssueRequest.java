package com.urviclean.recordbook.models;

import java.util.List;

/**
 * Request DTO for batch issuing stock to salesman
 */
public class BatchIssueRequest {

    private String salesmanAlias;
    private List<IssueItem> items;
    private String createdBy;

    public static class IssueItem {
        private String productCode;
        private Integer quantity;

        public String getProductCode() {
            return productCode;
        }

        public void setProductCode(String productCode) {
            this.productCode = productCode;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    // Getters and Setters

    public String getSalesmanAlias() {
        return salesmanAlias;
    }

    public void setSalesmanAlias(String salesmanAlias) {
        this.salesmanAlias = salesmanAlias;
    }

    public List<IssueItem> getItems() {
        return items;
    }

    public void setItems(List<IssueItem> items) {
        this.items = items;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}

