package com.urviclean.recordbook.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseItem {
    @JsonFormat(pattern = "dd/MM/yyyy")
    public LocalDate expenseDate;
    public String category;
    public BigDecimal amount;
}

