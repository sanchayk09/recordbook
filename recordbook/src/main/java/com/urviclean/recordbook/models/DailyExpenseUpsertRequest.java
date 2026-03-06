package com.urviclean.recordbook.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for the save/update daily expense endpoint.
 * Saves one total expense amount for a salesman on a given date.
 */
public class DailyExpenseUpsertRequest {

    public String salesmanAlias;
    public LocalDate expenseDate;
    public BigDecimal totalExpense;

    public DailyExpenseUpsertRequest() {
    }

    public DailyExpenseUpsertRequest(String salesmanAlias, LocalDate expenseDate, BigDecimal totalExpense) {
        this.salesmanAlias = salesmanAlias;
        this.expenseDate = expenseDate;
        this.totalExpense = totalExpense;
    }

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
