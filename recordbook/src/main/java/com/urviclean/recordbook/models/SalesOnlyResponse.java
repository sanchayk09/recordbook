package com.urviclean.recordbook.models;

import java.util.List;

/**
 * Response DTO for sales-only submission
 */
public class SalesOnlyResponse {
    public List<DailySaleRecordResponse> sales;
    public String message;

    public SalesOnlyResponse() {
    }

    public SalesOnlyResponse(List<DailySaleRecordResponse> sales, String message) {
        this.sales = sales;
        this.message = message;
    }
}

