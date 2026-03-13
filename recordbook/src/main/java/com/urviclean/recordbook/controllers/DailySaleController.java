package com.urviclean.recordbook.controllers;
import com.urviclean.recordbook.models.DailySaleRecordInput;
import com.urviclean.recordbook.models.ExpenseItem;
import com.urviclean.recordbook.models.SalesExpenseRequest;
import com.urviclean.recordbook.models.SalesOnlyRequest;
import com.urviclean.recordbook.models.SalesOnlyResponse;
import com.urviclean.recordbook.models.DailySaleRecordResponse;
import com.urviclean.recordbook.models.SalesmanExpenseResponse;
import com.urviclean.recordbook.models.SalesExpenseResponse;
import com.urviclean.recordbook.models.DailyExpenseRecord;
import com.urviclean.recordbook.models.DailyExpenseRecordResponse;
import com.urviclean.recordbook.models.ProductSalesDTO;
import com.urviclean.recordbook.utils.CommissionCalculator;
import com.urviclean.recordbook.utils.VolumeCalculator;
import com.urviclean.recordbook.services.ProductCostService;
import com.urviclean.recordbook.services.SalesService;
import com.urviclean.recordbook.models.DailySaleRecord;
import com.urviclean.recordbook.models.ExpenseCategory;
import com.urviclean.recordbook.models.Salesman;
import com.urviclean.recordbook.models.SalesmanExpense;
import com.urviclean.recordbook.repositories.DailySaleRecordRepository;
import com.urviclean.recordbook.repositories.SalesmanExpenseRepository;
import com.urviclean.recordbook.repositories.SalesmanRepository;
import com.urviclean.recordbook.repositories.DailyExpenseRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Daily Sales", description = "APIs for managing daily sales records, expenses, and product sales summaries")
public class DailySaleController {

    private static final Logger logger = LoggerFactory.getLogger(DailySaleController.class);

    @Autowired
    private DailySaleRecordRepository repository;

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Autowired
    private SalesmanExpenseRepository salesmanExpenseRepository;

    @Autowired
    private DailyExpenseRecordRepository dailyExpenseRecordRepository;

    @Autowired
    private SalesService salesService;

    @Autowired
    private ProductCostService productCostService;

    @PostMapping("/")
    @Operation(summary = "Create a new daily sale record",
               description = "Creates a new daily sale record and updates salesman ledger (NOT warehouse)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sale record created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public DailySaleRecord createRecord(@RequestBody DailySaleRecord record) {
        return salesService.createSale(record);
    }

    @GetMapping
    @Operation(summary = "Get all daily sale records",
               description = "Retrieves all daily sale records from the database")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all sale records")
    public List<DailySaleRecord> getAllRecords() {
        return repository.findAll();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a daily sale record",
               description = "Updates an existing daily sale record by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sale record updated successfully"),
        @ApiResponse(responseCode = "404", description = "Record not found")
    })
    public DailySaleRecord updateRecord(
            @Parameter(description = "ID of the record to update", required = true) @PathVariable Long id,
            @RequestBody DailySaleRecord newDetails) {
        return salesService.updateSale(id, newDetails);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a daily sale record",
               description = "Deletes a daily sale record by ID and restores salesman stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Record not found")
    })
    public void deleteRecord(@Parameter(description = "ID of the record to delete", required = true) @PathVariable Long id) {
        salesService.voidSale(id);
    }

    // 1. Filter by Product Code: /api/sales/filter/product-code?code=ABC123
    @GetMapping("/filter/product-code")
    @Operation(summary = "Filter sales by product code", description = "Retrieves all sales for a specific product")
    public List<DailySaleRecord> getByProductCode(
            @Parameter(description = "Product code to filter by", required = true) @RequestParam String code) {
        return repository.findByProductCode(code);
    }

    // 2. Filter by Quantity: /api/sales/filter/quantity?value=5
    @GetMapping("/filter/quantity")
    @Operation(summary = "Filter sales by quantity", description = "Retrieves all sales with a specific quantity")
    public List<DailySaleRecord> getByQuantity(
            @Parameter(description = "Quantity value to filter by", required = true) @RequestParam Integer value) {
        return repository.findByQuantity(value);
    }

    // 3. Filter by Product Code AND Quantity: /api/sales/filter/search?code=ABC123&quantity=10
    @GetMapping("/filter/search")
    @Operation(summary = "Search sales by product code and quantity",
               description = "Retrieves sales matching both product code and quantity")
    public List<DailySaleRecord> getByProductCodeAndQuantity(
            @Parameter(description = "Product code", required = true) @RequestParam String code,
            @Parameter(description = "Quantity", required = true) @RequestParam Integer quantity) {
        return repository.findByProductCodeAndQuantity(code, quantity);
    }

    // Date Filter Endpoints

    // 1. Filter by specific date: /api/sales/filter/date?date=2025-02-23
    @GetMapping("/filter/date")
    @Operation(summary = "Filter sales by specific date", description = "Retrieves sales for a specific date (format: YYYY-MM-DD)")
    public List<DailySaleRecord> getByDate(
            @Parameter(description = "Date in YYYY-MM-DD format", required = true, example = "2026-03-04") @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return repository.findBySaleDate(localDate);
    }

    // 2. Filter by today: /api/sales/filter/today
    @GetMapping("/filter/today")
    @Operation(summary = "Get today's sales", description = "Retrieves all sales records for today")
    public List<DailySaleRecord> getToday() {
        return repository.findBySaleDate(LocalDate.now());
    }

    // 3. Filter by date range: /api/sales/filter/range?startDate=2025-02-01&endDate=2025-02-23
    @GetMapping("/filter/range")
    @Operation(summary = "Filter sales by date range", description = "Retrieves sales within a date range")
    public List<DailySaleRecord> getByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2026-03-01") @RequestParam String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2026-03-04") @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return repository.findByDateRange(start, end);
    }

    // 4. Filter by week: /api/sales/filter/week?year=2025&week=8
    @GetMapping("/filter/week")
    @Operation(summary = "Filter sales by week", description = "Retrieves sales for a specific week of the year")
    public List<DailySaleRecord> getByWeek(
            @Parameter(description = "Year", required = true, example = "2026") @RequestParam int year,
            @Parameter(description = "Week number (1-53)", required = true, example = "10") @RequestParam int week) {
        return repository.findByYearAndWeek(year, week);
    }

    // 5. Filter by month: /api/sales/filter/month?year=2025&month=2
    @GetMapping("/filter/month")
    @Operation(summary = "Filter sales by month", description = "Retrieves sales for a specific month")
    public List<DailySaleRecord> getByMonth(
            @Parameter(description = "Year", required = true, example = "2026") @RequestParam int year,
            @Parameter(description = "Month (1-12)", required = true, example = "3") @RequestParam int month) {
        return repository.findByYearAndMonth(year, month);
    }

    // 6. Filter by current week: /api/sales/filter/current-week
    @GetMapping("/filter/current-week")
    @Operation(summary = "Get current week's sales", description = "Retrieves sales for the current week")
    public List<DailySaleRecord> getCurrentWeek() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int week = today.get(java.time.temporal.WeekFields.ISO.weekOfYear());
        return repository.findByYearAndWeek(year, week);
    }

    // 7. Filter by current month: /api/sales/filter/current-month
    @GetMapping("/filter/current-month")
    @Operation(summary = "Get current month's sales", description = "Retrieves sales for the current month")
    public List<DailySaleRecord> getCurrentMonth() {
        LocalDate today = LocalDate.now();
        return repository.findByYearAndMonth(today.getYear(), today.getMonthValue());
    }

    // 8. Filter by last N days: /api/sales/filter/last-days?days=7
    @GetMapping("/filter/last-days")
    @Operation(summary = "Get sales for last N days", description = "Retrieves sales for the last N days (including today)")
    public List<DailySaleRecord> getLastNDays(
            @Parameter(description = "Number of days", required = true, example = "7") @RequestParam int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return repository.findByDateRange(startDate, endDate);
    }

    // 9. Filter by last 7 days: /api/sales/filter/last-7-days
    @GetMapping("/filter/last-7-days")
    @Operation(summary = "Get sales for last 7 days", description = "Retrieves sales for the last 7 days (including today)")
    public List<DailySaleRecord> getLast7Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        return repository.findByDateRange(startDate, endDate);
    }

    // 10. Filter by last 15 days: /api/sales/filter/last-15-days
    @GetMapping("/filter/last-15-days")
    @Operation(summary = "Get sales for last 15 days", description = "Retrieves sales for the last 15 days (including today)")
    public List<DailySaleRecord> getLast15Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(14);
        return repository.findByDateRange(startDate, endDate);
    }

    // 11. Filter by last 30 days: /api/sales/filter/last-30-days
    @GetMapping("/filter/last-30-days")
    @Operation(summary = "Get sales for last 30 days", description = "Retrieves sales for the last 30 days (including today)")
    public List<DailySaleRecord> getLast30Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        return repository.findByDateRange(startDate, endDate);
    }

    // 12. Filter by last 90 days: /api/sales/filter/last-90-days
    @GetMapping("/filter/last-90-days")
    @Operation(summary = "Get sales for last 90 days", description = "Retrieves sales for the last 90 days (including today)")
    public List<DailySaleRecord> getLast90Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(89);
        return repository.findByDateRange(startDate, endDate);
    }

    // Product Sales Summary Endpoints (Group by Product Code)

    /**
     * UNIFIED Product Sales Summary Endpoint
     *
     * Flexible endpoint that handles all time periods with optional parameters:
     * - No params = all-time summary
     * - startDate + endDate = custom date range
     * - period = convenience shortcuts (today, last7days, last30days, etc.)
     * - year + month = specific month
     *
     * Examples:
     * /api/sales/summary/product-sales (all-time)
     * /api/sales/summary/product-sales?period=today
     * /api/sales/summary/product-sales?period=last7days
     * /api/sales/summary/product-sales?period=last30days
     * /api/sales/summary/product-sales?startDate=2026-02-01&endDate=2026-03-09
     * /api/sales/summary/product-sales?year=2026&month=3
     */
    @GetMapping("/summary/product-sales")
    @Operation(
        summary = "Get product sales summary with flexible date filtering",
        description = "Unified endpoint supporting all-time, custom ranges, convenience periods, and monthly summaries"
    )
    public List<ProductSalesDTO> getProductSalesSummary(
            @Parameter(description = "Start date (YYYY-MM-DD) for custom range", example = "2026-02-01")
            @RequestParam(required = false) String startDate,

            @Parameter(description = "End date (YYYY-MM-DD) for custom range", example = "2026-03-09")
            @RequestParam(required = false) String endDate,

            @Parameter(description = "Convenience period: today, last7days, last15days, last30days, last90days, currentMonth",
                       example = "last30days")
            @RequestParam(required = false) String period,

            @Parameter(description = "Year for monthly summary", example = "2026")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "Month (1-12) for monthly summary", example = "3")
            @RequestParam(required = false) Integer month
    ) {
        // Handle monthly summary
        if (year != null && month != null) {
            List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndMonth(year, month);
            return productCostService.enrichWithCostsAndCommissionByMonth(results, year, month);
        }

        // Handle custom date range
        if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDateRange(start, end);
            return productCostService.enrichWithCostsAndCommissionForDateRange(results, start, end);
        }

        // Handle convenience periods
        if (period != null && !period.isEmpty()) {
            LocalDate end = LocalDate.now();
            LocalDate start;

            switch (period.toLowerCase()) {
                case "today":
                    start = end;
                    List<ProductSalesDTO> todayResults = repository.getQuantitySoldByProductCodeAndDate(start);
                    return productCostService.enrichWithCostsAndCommission(todayResults, start);

                case "last7days":
                    start = end.minusDays(6);
                    break;

                case "last15days":
                    start = end.minusDays(14);
                    break;

                case "last30days":
                    start = end.minusDays(29);
                    break;

                case "last90days":
                    start = end.minusDays(89);
                    break;

                case "currentmonth":
                    List<ProductSalesDTO> monthResults = repository.getQuantitySoldByProductCodeAndMonth(
                        end.getYear(), end.getMonthValue()
                    );
                    return productCostService.enrichWithCostsAndCommissionByMonth(
                        monthResults, end.getYear(), end.getMonthValue()
                    );

                default:
                    throw new IllegalArgumentException(
                        "Invalid period: " + period + ". Valid values: today, last7days, last15days, last30days, last90days, currentMonth"
                    );
            }

            // Execute date range query for period-based requests
            List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDateRange(start, end);
            return productCostService.enrichWithCostsAndCommissionForDateRange(results, start, end);
        }

        // Default: all-time summary (no opCost)
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCode();
        return productCostService.enrichWithCostsAndCommissionAllTime(results);
    }

    // POST with request body for alias + daily sales only (NO EXPENSES)
    @PostMapping("/sales-expense")
    @Transactional
    @Operation(summary = "Submit daily sales only", description = "Creates sale records and updates salesman stock. Does NOT process expenses.")
    public ResponseEntity<SalesOnlyResponse> addSalesOnly(@RequestBody SalesOnlyRequest request) {
        if (request == null || request.salesmanAlias == null || request.salesmanAlias.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Salesman salesman = salesmanRepository.findByAliasIgnoreCase(request.salesmanAlias.trim())
                .orElseThrow(() -> new RuntimeException("Salesman not found for alias: " + request.salesmanAlias));

        LocalDate requestDate = request.date;

        List<DailySaleRecord> existingRecords = repository.findAll();
        List<DailySaleRecord> savedSales = new ArrayList<>();

        if (request.dailySales != null) {
            for (DailySaleRecordInput item : request.dailySales) {
                DailySaleRecord incoming = new DailySaleRecord();
                incoming.setSlNo(item.slNo);
                incoming.setSaleDate(item.saleDate != null ? item.saleDate : requestDate);
                incoming.setSalesmanName(salesman.getAlias());
                incoming.setCustomerName(item.customerName);
                incoming.setCustomerType(item.customerType);
                incoming.setVillage(item.village);
                incoming.setMobileNumber(item.mobileNumber);
                incoming.setProductCode(item.productCode);
                incoming.setQuantity(item.quantity);
                incoming.setRate(item.rate);
                incoming.setRevenue(resolveRevenue(item.quantity, item.rate, item.revenue));

                BigDecimal commission = CommissionCalculator.calculateCommission(item.productCode, item.rate, item.quantity);
                incoming.setAgentCommission(commission);

                BigDecimal volumeSold = VolumeCalculator.calculateVolumeSold(item.productCode, item.quantity);
                incoming.setVolumeSold(volumeSold);

                DailySaleRecord existing = findExistingRecord(incoming, existingRecords);
                if (existing == null) {
                    DailySaleRecord saved = salesService.createSale(incoming);
                    existingRecords.add(saved);
                    savedSales.add(saved);
                } else {
                    DailySaleRecord saved = salesService.updateSale(existing.getId(), incoming);
                    savedSales.add(saved);
                }
            }
        }

        List<DailySaleRecordResponse> saleResponses = savedSales.stream()
                .map(DailySaleRecordResponse::new)
                .toList();

        return ResponseEntity.ok(new SalesOnlyResponse(saleResponses, "Sales saved successfully"));
    }

    @PostMapping("/expenses")
    @Transactional
    public ResponseEntity<SalesExpenseResponse> addExpensesOnly(@RequestBody SalesExpenseRequest request) {
        if (request == null || request.salesmanAlias == null || request.salesmanAlias.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Salesman salesman = salesmanRepository.findByAliasIgnoreCase(request.salesmanAlias.trim())
                .orElseThrow(() -> new RuntimeException("Salesman not found for alias: " + request.salesmanAlias));

        LocalDate requestDate = request.date;
        if (requestDate == null) {
            requestDate = LocalDate.now();
        }

        List<SalesmanExpense> expenseEntities = new ArrayList<>();
        if (request.expenses != null) {
            for (ExpenseItem item : request.expenses) {
                if (item == null || item.amount == null) {
                    continue;
                }
                SalesmanExpense expense = new SalesmanExpense();
                expense.setSalesman(salesman);
                expense.setExpenseDate(item.expenseDate != null ? item.expenseDate : requestDate);
                expense.setCategory(parseExpenseCategory(item.category));
                expense.setAmount(item.amount);
                expenseEntities.add(expense);
            }
        }

        List<SalesmanExpense> savedExpenses = salesmanExpenseRepository.saveAll(expenseEntities);

        // Upsert daily_expense_record totals for each touched expense date.
        List<LocalDate> touchedDates = savedExpenses.stream()
                .map(SalesmanExpense::getExpenseDate)
                .distinct()
                .toList();

        for (LocalDate date : touchedDates) {
            BigDecimal aggregatedExpense = savedExpenses.stream()
                    .filter(exp -> date.equals(exp.getExpenseDate()))
                    .map(SalesmanExpense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            DailyExpenseRecord dailyExpenseRecord = dailyExpenseRecordRepository
                    .findBySalesmanAliasAndExpenseDate(salesman.getAlias(), date)
                    .orElse(new DailyExpenseRecord(salesman.getAlias(), date, BigDecimal.ZERO));

            dailyExpenseRecord.setTotalExpense(aggregatedExpense);
            dailyExpenseRecordRepository.save(dailyExpenseRecord);
        }

        List<SalesmanExpenseResponse> expenseResponses = savedExpenses.stream()
                .map(SalesmanExpenseResponse::new)
                .toList();

        return ResponseEntity.ok(new SalesExpenseResponse(expenseResponses, List.of()));
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
     * Refresh daily_sale_record for all salesmen in date range
     * Reads daily_sale_record table and creates/updates summary records in daily_summary table
     * for all salesmen and dates in the range
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<Map<String, Object>> refreshDailySalesRecords(
            @RequestBody Map<String, String> request) {
        try {
            String startDateStr = request.get("startDate");
            String endDateStr = request.get("endDate");

            if (startDateStr == null || endDateStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "startDate and endDate are required"));
            }

            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);

            logger.info("Starting refresh for date range: {} to {}", startDate, endDate);

            // Get all sales records in the date range
            List<DailySaleRecord> allRecords = repository.findByDateRange(startDate, endDate);

            if (allRecords.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("refreshedCount", 0);
                response.put("message", "No sales records found in the date range");
                logger.info("No records found for date range {} to {}", startDate, endDate);
                return ResponseEntity.ok(response);
            }

            // Group by (salesman, date) - this gives us unique salesman-date combinations
            Map<String, List<DailySaleRecord>> groupedBySalesmanDate = allRecords.stream()
                    .collect(Collectors.groupingBy(r -> r.getSalesmanName() + "|" + r.getSaleDate()));

            int refreshedCount = 0;

            // For each salesman-date combination, compute and save summary
            for (Map.Entry<String, List<DailySaleRecord>> entry : groupedBySalesmanDate.entrySet()) {
                List<DailySaleRecord> records = entry.getValue();
                if (records.isEmpty()) continue;

                String salesmanName = records.get(0).getSalesmanName();
                LocalDate saleDate = records.get(0).getSaleDate();

                logger.debug("Processing: salesman={}, date={}, recordCount={}", salesmanName, saleDate, records.size());

                // Use DailySummaryService to compute and persist the summary
                // This ensures all calculations (material cost, expenses, net profit) are correct
                try {
                    salesService.getDailySummaryService().computeAndPersistSummary(salesmanName, saleDate);
                    refreshedCount++;
                    logger.debug("Successfully refreshed summary for {} on {}", salesmanName, saleDate);
                } catch (Exception e) {
                    logger.warn("Failed to refresh summary for {} on {}: {}", salesmanName, saleDate, e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("refreshedCount", refreshedCount);
            response.put("message", "Successfully refreshed " + refreshedCount + " daily summaries for all salesmen from " + startDate + " to " + endDate);
            response.put("dateRange", startDate + " to " + endDate);

            logger.info("Completed refresh: {} summaries updated for date range {} to {}", refreshedCount, startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error refreshing daily sales records: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to refresh: " + e.getMessage()));
        }
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
