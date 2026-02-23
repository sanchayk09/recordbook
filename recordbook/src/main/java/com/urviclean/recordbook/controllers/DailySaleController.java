package com.urviclean.recordbook.controllers;
import com.urviclean.recordbook.models.DailySaleRecordInput;
import com.urviclean.recordbook.models.ExpenseItem;
import com.urviclean.recordbook.models.SalesExpenseRequest;
import com.urviclean.recordbook.models.DailySaleRecordResponse;
import com.urviclean.recordbook.models.SalesmanExpenseResponse;
import com.urviclean.recordbook.models.SalesExpenseResponse;
import com.urviclean.recordbook.models.DailyExpenseRecord;
import com.urviclean.recordbook.models.DailyExpenseRecordResponse;
import com.urviclean.recordbook.utils.CommissionCalculator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.urviclean.recordbook.models.DailySaleCustomerType;
import com.urviclean.recordbook.models.DailySaleRecord;
import com.urviclean.recordbook.models.ExpenseCategory;
import com.urviclean.recordbook.models.Salesman;
import com.urviclean.recordbook.models.SalesmanExpense;
import com.urviclean.recordbook.repositories.DailySaleRecordRepository;
import com.urviclean.recordbook.repositories.SalesmanExpenseRepository;
import com.urviclean.recordbook.repositories.SalesmanRepository;
import com.urviclean.recordbook.repositories.DailyExpenseRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:3000")
public class DailySaleController {

    @Autowired
    private DailySaleRecordRepository repository;

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Autowired
    private SalesmanExpenseRepository salesmanExpenseRepository;

    @Autowired
    private DailyExpenseRecordRepository dailyExpenseRecordRepository;

    @PostMapping
    public DailySaleRecord createRecord(@RequestBody DailySaleRecord record) {
        return repository.save(record);
    }

    @GetMapping
    public List<DailySaleRecord> getAllRecords() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    public DailySaleRecord updateRecord(@PathVariable Long id, @RequestBody DailySaleRecord newDetails) {
        return repository.findById(id)
                .map(record -> {
                    if (newDetails.getSlNo() != null) {
                        record.setSlNo(newDetails.getSlNo());
                    }
                    if (newDetails.getSaleDate() != null) {
                        record.setSaleDate(newDetails.getSaleDate());
                    }
                    if (newDetails.getSalesmanName() != null && !newDetails.getSalesmanName().isBlank()) {
                        record.setSalesmanName(newDetails.getSalesmanName().trim());
                    }
                    if (newDetails.getCustomerName() != null && !newDetails.getCustomerName().isBlank()) {
                        record.setCustomerName(newDetails.getCustomerName().trim());
                    }
                    if (newDetails.getCustomerType() != null) {
                        record.setCustomerType(newDetails.getCustomerType());
                    }
                    if (newDetails.getVillage() != null) {
                        record.setVillage(newDetails.getVillage());
                    }
                    if (newDetails.getMobileNumber() != null) {
                        record.setMobileNumber(newDetails.getMobileNumber());
                    }
                    if (newDetails.getProductCode() != null && !newDetails.getProductCode().isBlank()) {
                        record.setProductCode(newDetails.getProductCode().trim());
                    }
                    if (newDetails.getQuantity() != null) {
                        record.setQuantity(newDetails.getQuantity());
                    }
                    if (newDetails.getRate() != null) {
                        record.setRate(newDetails.getRate());
                    }
                    if (newDetails.getRevenue() != null) {
                        record.setRevenue(newDetails.getRevenue());
                    } else {
                        record.setRevenue(resolveRevenue(record.getQuantity(), record.getRate(), record.getRevenue()));
                    }

                    if (record.getProductCode() != null && record.getRate() != null && record.getQuantity() != null) {
                        BigDecimal commission = CommissionCalculator.calculateCommission(
                                record.getProductCode(),
                                record.getRate(),
                                record.getQuantity()
                        );
                        record.setAgentCommission(commission);
                    }

                    return repository.save(record);
                }).orElseThrow(() -> new RuntimeException("Record not found"));
    }

    @DeleteMapping("/{id}")
    public void deleteRecord(@PathVariable Long id) {
        repository.deleteById(id);
    }

    // 1. Filter by Product Code: /api/sales/filter/product-code?code=ABC123
    @GetMapping("/filter/product-code")
    public List<DailySaleRecord> getByProductCode(@RequestParam String code) {
        return repository.findByProductCode(code);
    }

    // 2. Filter by Quantity: /api/sales/filter/quantity?value=5
    @GetMapping("/filter/quantity")
    public List<DailySaleRecord> getByQuantity(@RequestParam Integer value) {
        return repository.findByQuantity(value);
    }

    // 3. Filter by Product Code AND Quantity: /api/sales/filter/search?code=ABC123&quantity=10
    @GetMapping("/filter/search")
    public List<DailySaleRecord> getByProductCodeAndQuantity(
            @RequestParam String code,
            @RequestParam Integer quantity) {
        return repository.findByProductCodeAndQuantity(code, quantity);
    }

    // Report endpoint: /api/sales/report?startDate=2026-01-01&endDate=2026-01-31
    @GetMapping("/report")
    public ResponseEntity<List<DailySaleRecord>> getReportData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(29);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ResponseEntity.ok(repository.findBySaleDateBetweenOrderBySaleDateAsc(start, end));
    }

    // POST with request body for alias + expenses + daily sales
    @PostMapping("/sales-expense")
    @Transactional
    public ResponseEntity<SalesExpenseResponse> addSalesAndExpenses(@RequestBody SalesExpenseRequest request) {
        if (request == null || request.salesmanAlias == null || request.salesmanAlias.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Salesman salesman = salesmanRepository.findByAliasIgnoreCase(request.salesmanAlias.trim())
                .orElseThrow(() -> new RuntimeException("Salesman not found for alias: " + request.salesmanAlias));

        LocalDate requestDate = request.date;

        List<SalesmanExpense> expenseEntities = new ArrayList<>();
        BigDecimal totalDailyExpense = BigDecimal.ZERO;

        if (request.expenses != null) {
            for (ExpenseItem item : request.expenses) {
                SalesmanExpense expense = new SalesmanExpense();
                expense.setSalesman(salesman);
                expense.setExpenseDate(item.expenseDate != null ? item.expenseDate : requestDate);
                expense.setCategory(parseExpenseCategory(item.category));
                expense.setAmount(item.amount);
                expenseEntities.add(expense);

                // Add to daily total if it's for the request date
                if ((item.expenseDate != null ? item.expenseDate : requestDate).equals(requestDate)) {
                    totalDailyExpense = totalDailyExpense.add(item.amount);
                }
            }
        }

        List<DailySaleRecord> existingRecords = repository.findAll();
        Set<DailySaleRecord> saleEntities = new LinkedHashSet<>();
        if (request.dailySales != null) {
            for (DailySaleRecordInput item : request.dailySales) {
                DailySaleRecord incoming = new DailySaleRecord();
                incoming.setSlNo(item.slNo);
                incoming.setSaleDate(item.saleDate != null ? item.saleDate : requestDate);
                incoming.setSalesmanName(request.salesmanAlias.trim());
                incoming.setCustomerName(item.customerName);
                incoming.setCustomerType(item.customerType);
                incoming.setVillage(item.village);
                incoming.setMobileNumber(item.mobileNumber);
                incoming.setProductCode(item.productCode);
                incoming.setQuantity(item.quantity);
                incoming.setRate(item.rate);
                incoming.setRevenue(resolveRevenue(item.quantity, item.rate, item.revenue));
                // Calculate agent commission (per unit × quantity)
                BigDecimal commission = CommissionCalculator.calculateCommission(item.productCode, item.rate, item.quantity);
                incoming.setAgentCommission(commission);

                DailySaleRecord existing = findExistingRecord(incoming, existingRecords);
                if (existing == null) {
                    existingRecords.add(incoming);
                    saleEntities.add(incoming);
                } else {
                    applyUpdate(existing, incoming);
                    saleEntities.add(existing);
                }
            }
        }

        List<SalesmanExpense> savedExpenses = salesmanExpenseRepository.saveAll(expenseEntities);
        List<DailySaleRecord> savedSales = repository.saveAll(new ArrayList<>(saleEntities));

        // Populate daily_expense_record if expenses exist
        if (!expenseEntities.isEmpty()) {
            DailyExpenseRecord dailyExpenseRecord = dailyExpenseRecordRepository
                    .findBySalesmanAliasAndExpenseDate(request.salesmanAlias.trim(), requestDate)
                    .orElse(new DailyExpenseRecord(request.salesmanAlias.trim(), requestDate, BigDecimal.ZERO));

            // Update total expense by aggregating from saved expenses for this date
            BigDecimal aggregatedExpense = savedExpenses.stream()
                    .filter(exp -> exp.getExpenseDate().equals(requestDate))
                    .map(SalesmanExpense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dailyExpenseRecord.setTotalExpense(aggregatedExpense);
            dailyExpenseRecordRepository.save(dailyExpenseRecord);
        }

        // Convert to response DTOs (without IDs)
        List<SalesmanExpenseResponse> expenseResponses = savedExpenses.stream()
                .map(SalesmanExpenseResponse::new)
                .toList();
        List<DailySaleRecordResponse> saleResponses = savedSales.stream()
                .map(DailySaleRecordResponse::new)
                .toList();

        return ResponseEntity.ok(new SalesExpenseResponse(expenseResponses, saleResponses));
    }

    private BigDecimal resolveRevenue(Integer quantity, BigDecimal rate, BigDecimal revenue) {
        if (revenue != null) {
            return revenue;
        }
        if (quantity == null || rate == null) {
            return null;
        }
        return rate.multiply(BigDecimal.valueOf(quantity));
    }

    private ExpenseCategory parseExpenseCategory(String value) {
        if (value == null || value.isBlank()) {
            return ExpenseCategory.Other;
        }
        String normalized = value.trim().replace(' ', '_');
        try {
            return ExpenseCategory.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return ExpenseCategory.Other;
        }
    }

    /**
     * Find an existing record based on the natural key fields.
     */
    private DailySaleRecord findExistingRecord(DailySaleRecord incoming, List<DailySaleRecord> existingRecords) {
        for (DailySaleRecord existing : existingRecords) {
            if (matchesKey(existing, incoming)) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Natural key match: date + salesman + customer + type + village + mobile + product.
     */
    private boolean matchesKey(DailySaleRecord r1, DailySaleRecord r2) {
        return equalsDate(r1.getSaleDate(), r2.getSaleDate())
                && equalsString(r1.getSalesmanName(), r2.getSalesmanName())
                && equalsString(r1.getCustomerName(), r2.getCustomerName())
                && equals(r1.getCustomerType(), r2.getCustomerType())
                && equalsString(r1.getVillage(), r2.getVillage())
                && equalsString(r1.getMobileNumber(), r2.getMobileNumber())
                && equalsString(r1.getProductCode(), r2.getProductCode());
    }

    /**
     * Apply updates from incoming to existing record.
     */
    private void applyUpdate(DailySaleRecord target, DailySaleRecord source) {
        target.setSlNo(source.getSlNo());
        target.setSaleDate(source.getSaleDate());
        target.setSalesmanName(source.getSalesmanName());
        target.setCustomerName(source.getCustomerName());
        target.setCustomerType(source.getCustomerType());
        target.setVillage(source.getVillage());
        target.setMobileNumber(source.getMobileNumber());
        target.setProductCode(source.getProductCode());
        target.setQuantity(source.getQuantity());
        target.setRate(source.getRate());
        target.setRevenue(source.getRevenue());
        target.setAgentCommission(source.getAgentCommission());
    }

    /**
     * Null-safe equality check for general objects
     */
    private boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }
    /**
     * Null-safe equality check for strings (case-insensitive, trimmed)
     */
    private boolean equalsString(String s1, String s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        return s1.trim().equalsIgnoreCase(s2.trim());
    }
    /**
     * Null-safe equality check for BigDecimal (compares value, not scale)
     */
    private boolean equalsBigDecimal(BigDecimal bd1, BigDecimal bd2) {
        if (bd1 == null && bd2 == null) return true;
        if (bd1 == null || bd2 == null) return false;
        return bd1.compareTo(bd2) == 0;
    }
    /**
     * Null-safe equality check for LocalDate
     */
    private boolean equalsDate(LocalDate d1, LocalDate d2) {
        if (d1 == null && d2 == null) return true;
        if (d1 == null || d2 == null) return false;
        return d1.equals(d2);
    }
}
