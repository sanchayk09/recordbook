package com.urviclean.recordbook.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for SalesmanExpense without the ID field
 */
public class SalesmanExpenseResponse {
    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate expenseDate;

    public String category;
    public BigDecimal amount;

    public SalesmanExpenseResponse() {
    }

    public SalesmanExpenseResponse(SalesmanExpense expense) {
        this.expenseDate = expense.getExpenseDate();
        this.category = expense.getCategory() != null ? expense.getCategory().name() : null;
        this.amount = expense.getAmount();
    }
}

