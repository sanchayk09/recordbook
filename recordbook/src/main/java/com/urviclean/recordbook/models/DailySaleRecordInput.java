package com.urviclean.recordbook.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySaleRecordInput {
    public Integer slNo;

    @JsonFormat(pattern = "dd/MM/yyyy")
    public LocalDate saleDate;

    public String customerName;
    public DailySaleCustomerType customerType;
    public String village;
    public String mobileNumber;
    public Integer quantity;
    public BigDecimal rate;
    public BigDecimal revenue;
    public String productCode;
    public BigDecimal agentCommission;  // Calculated field
}
