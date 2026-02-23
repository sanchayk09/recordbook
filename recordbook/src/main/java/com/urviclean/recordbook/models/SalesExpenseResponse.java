package com.urviclean.recordbook.models;

import java.util.List;

/**
 * Response wrapper for sales and expense submission
 */
public class SalesExpenseResponse {
    public List<SalesmanExpenseResponse> expenses;
    public List<DailySaleRecordResponse> dailySales;

    public SalesExpenseResponse(List<SalesmanExpenseResponse> expenses, List<DailySaleRecordResponse> dailySales) {
        this.expenses = expenses;
        this.dailySales = dailySales;
    }
}

