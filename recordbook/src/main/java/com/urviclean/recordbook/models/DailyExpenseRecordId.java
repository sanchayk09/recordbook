package com.urviclean.recordbook.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class DailyExpenseRecordId implements Serializable {

    private String salesmanAlias;
    private LocalDate expenseDate;

    public DailyExpenseRecordId() {
    }

    public DailyExpenseRecordId(String salesmanAlias, LocalDate expenseDate) {
        this.salesmanAlias = salesmanAlias;
        this.expenseDate = expenseDate;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DailyExpenseRecordId that = (DailyExpenseRecordId) o;
        return Objects.equals(salesmanAlias, that.salesmanAlias) &&
                Objects.equals(expenseDate, that.expenseDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(salesmanAlias, expenseDate);
    }
}

