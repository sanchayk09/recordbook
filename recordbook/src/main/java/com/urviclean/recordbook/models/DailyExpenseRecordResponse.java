package com.urviclean.recordbook.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyExpenseRecordResponse {

    public String salesmanAlias;
    public LocalDate expenseDate;
    public BigDecimal totalExpense;

    public DailyExpenseRecordResponse() {
    }

    public DailyExpenseRecordResponse(DailyExpenseRecord record) {
        this.salesmanAlias = record.getSalesmanAlias();
        this.expenseDate = record.getExpenseDate();
        this.totalExpense = record.getTotalExpense();
    }

    public DailyExpenseRecordResponse(String salesmanAlias, LocalDate expenseDate, BigDecimal totalExpense) {
        this.salesmanAlias = salesmanAlias;
        this.expenseDate = expenseDate;
        this.totalExpense = totalExpense;
    }

    // Getters and Setters
    public String getSalesmanAlias() {
        return salesmanAlias;
    }

    public void setSalesmanAlias(String salesmanAlias) {
        this.salesmanAlias = salesmanAlias;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }
}

