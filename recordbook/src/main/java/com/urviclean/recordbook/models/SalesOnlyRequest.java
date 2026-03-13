package com.urviclean.recordbook.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for sales-only submission (no expenses)
 */
public class SalesOnlyRequest {
    public String salesmanAlias;

    @JsonFormat(pattern = "yyyy-MM-dd")
    public LocalDate date;

    public List<DailySaleRecordInput> dailySales;

    public SalesOnlyRequest() {
    }

    public SalesOnlyRequest(String salesmanAlias, LocalDate date, List<DailySaleRecordInput> dailySales) {
        this.salesmanAlias = salesmanAlias;
        this.date = date;
        this.dailySales = dailySales;
    }
}

