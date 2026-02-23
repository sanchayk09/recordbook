package com.urviclean.recordbook.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for DailySaleRecord without the ID field
 */
public class DailySaleRecordResponse {
    public Integer slNo;

    @JsonFormat(pattern = "dd/MM/yyyy")
    public LocalDate saleDate;

    public String salesmanName;
    public String customerName;
    public DailySaleCustomerType customerType;
    public String village;
    public String mobileNumber;
    public String productCode;
    public Integer quantity;
    public BigDecimal rate;
    public BigDecimal revenue;
    public BigDecimal agentCommission;

    public DailySaleRecordResponse() {
    }

    public DailySaleRecordResponse(DailySaleRecord record) {
        this.slNo = record.getSlNo();
        this.saleDate = record.getSaleDate();
        this.salesmanName = record.getSalesmanName();
        this.customerName = record.getCustomerName();
        this.customerType = record.getCustomerType();
        this.village = record.getVillage();
        this.mobileNumber = record.getMobileNumber();
        this.productCode = record.getProductCode();
        this.quantity = record.getQuantity();
        this.rate = record.getRate();
        this.revenue = record.getRevenue();
        this.agentCommission = record.getAgentCommission();
    }
}

