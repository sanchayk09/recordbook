package com.urviclean.recordbook.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public class SalesExpenseRequest {
    public String salesmanAlias;

    @JsonFormat(pattern = "dd/MM/yyyy")
    public LocalDate date;

    public List<ExpenseItem> expenses;
    public List<DailySaleRecordInput> dailySales;
}

