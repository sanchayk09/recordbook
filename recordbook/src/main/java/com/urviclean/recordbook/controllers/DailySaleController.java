package com.urviclean.recordbook.controllers;
import com.urviclean.recordbook.models.DailySaleRecordInput;
import com.urviclean.recordbook.models.ExpenseItem;
import com.urviclean.recordbook.models.SalesExpenseRequest;
import com.urviclean.recordbook.models.DailySaleRecordResponse;
import com.urviclean.recordbook.models.SalesmanExpenseResponse;
import com.urviclean.recordbook.models.SalesExpenseResponse;
import com.urviclean.recordbook.models.DailyExpenseRecord;
import com.urviclean.recordbook.models.DailyExpenseRecordResponse;
import com.urviclean.recordbook.models.ProductSalesDTO;
import com.urviclean.recordbook.utils.CommissionCalculator;
import com.urviclean.recordbook.utils.VolumeCalculator;
import com.urviclean.recordbook.services.ProductCostService;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.urviclean.recordbook.models.DailySaleCustomerType;
import com.urviclean.recordbook.models.DailySaleRecord;
import com.urviclean.recordbook.models.ExpenseCategory;
import com.urviclean.recordbook.models.Salesman;
import com.urviclean.recordbook.models.SalesmanExpense;
import com.urviclean.recordbook.models.SalesmanLedger;
import com.urviclean.recordbook.models.SalesmanTxnType;
import com.urviclean.recordbook.models.SalesmanStockSummary;
import com.urviclean.recordbook.repositories.DailySaleRecordRepository;
import com.urviclean.recordbook.repositories.SalesmanExpenseRepository;
import com.urviclean.recordbook.repositories.SalesmanRepository;
import com.urviclean.recordbook.repositories.DailyExpenseRecordRepository;
import com.urviclean.recordbook.repositories.SalesmanLedgerRepository;
import com.urviclean.recordbook.repositories.SalesmanStockSummaryRepository;
import com.urviclean.recordbook.exception.InvalidInputException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Daily Sales", description = "APIs for managing daily sales records, expenses, and product sales summaries")
public class DailySaleController {

    @Autowired
    private DailySaleRecordRepository repository;

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Autowired
    private SalesmanExpenseRepository salesmanExpenseRepository;

    @Autowired
    private DailyExpenseRecordRepository dailyExpenseRecordRepository;

    @Autowired
    private SalesmanLedgerRepository salesmanLedgerRepository;

    @Autowired
    private SalesmanStockSummaryRepository salesmanStockSummaryRepository;

    @Autowired
    private ProductCostService productCostService;

    @PostMapping("/")
    @Transactional
    @Operation(summary = "Create a new daily sale record",
               description = "Creates a new daily sale record and updates salesman ledger (NOT warehouse)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sale record created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public DailySaleRecord createRecord(@RequestBody DailySaleRecord record) {
        // Calculate volume_sold based on product_code and quantity
        if (record.getProductCode() != null && record.getQuantity() != null) {
            BigDecimal volumeSold = VolumeCalculator.calculateVolumeSold(
                    record.getProductCode(),
                    record.getQuantity()
            );
            record.setVolumeSold(volumeSold);
        }

        // Save the sales record
        DailySaleRecord savedRecord = repository.save(record);

        // CRITICAL: When a sale is recorded:
        // 1. Validate salesman stock exists and sufficient qty in salesman_stock_summary
        // 2. Update salesman_stock_summary with -qty
        // 3. Insert salesman_ledger with SOLD transaction
        // DO NOT update warehouse_ledger (no SOLD_BY_SALESMAN entry anymore)

        if (record.getSalesmanName() != null && record.getProductCode() != null && record.getQuantity() != null) {
            try {
                System.out.println(">>> Creating salesman ledger entry for sale:");
                System.out.println("    Salesman: " + record.getSalesmanName());
                System.out.println("    Product: " + record.getProductCode());
                System.out.println("    Quantity: " + record.getQuantity());

                // Get current salesman stock for this product
                SalesmanStockSummary salesmanSummary = salesmanStockSummaryRepository
                    .findBySalesmanAliasAndProductCode(record.getSalesmanName(), record.getProductCode())
                    .orElse(new SalesmanStockSummary());

                int salesmanStockBefore = salesmanSummary.getCurrentStock() != null ? salesmanSummary.getCurrentStock() : 0;
                int qtySold = record.getQuantity();
                int salesmanStockAfter = salesmanStockBefore - qtySold;

                System.out.println("    Salesman Stock Before: " + salesmanStockBefore);
                System.out.println("    Salesman Stock After: " + salesmanStockAfter);

                // VALIDATION: Check salesman has enough stock
                if (salesmanStockAfter < 0) {
                    throw new InvalidInputException(
                        "Insufficient stock with salesman. Available: " + salesmanStockBefore + ", Trying to sell: " + qtySold,
                        "INSUFFICIENT_SALESMAN_STOCK"
                    );
                }

                // Update salesman_stock_summary (decrement by sold quantity)
                salesmanSummary.setSalesmanAlias(record.getSalesmanName());
                salesmanSummary.setProductCode(record.getProductCode());
                salesmanSummary.setCurrentStock(salesmanStockAfter);
                salesmanSummary.setLastUpdated(LocalDateTime.now());
                salesmanStockSummaryRepository.save(salesmanSummary);

                // Create salesman_ledger entry for SOLD (negative quantity)
                SalesmanLedger salesmanLedger = new SalesmanLedger(
                    record.getSalesmanName(),
                    record.getProductCode(),
                    SalesmanTxnType.SOLD,
                    -qtySold,  // Negative because sale reduces salesman stock
                    "Sold via daily_sale_record id=" + savedRecord.getId(),
                    "system"
                );
                salesmanLedgerRepository.save(salesmanLedger);
                System.out.println(">>> Salesman ledger entry created successfully!");

            } catch (Exception e) {
                // Log the error but don't fail the sale record creation
                System.err.println("!!! ERROR: Failed to update salesman ledger for sale: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> Skipping salesman ledger update - missing required fields:");
            System.out.println("    salesmanName: " + record.getSalesmanName());
            System.out.println("    productCode: " + record.getProductCode());
            System.out.println("    quantity: " + record.getQuantity());
        }

        return savedRecord;
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

                    // Always recalculate revenue based on quantity * rate
                    if (record.getQuantity() != null && record.getRate() != null) {
                        BigDecimal calculatedRevenue = record.getRate().multiply(BigDecimal.valueOf(record.getQuantity()));
                        record.setRevenue(calculatedRevenue);
                    }

                    // Always recalculate agent commission
                    if (record.getProductCode() != null && record.getRate() != null && record.getQuantity() != null) {
                        BigDecimal commission = CommissionCalculator.calculateCommission(
                                record.getProductCode(),
                                record.getRate(),
                                record.getQuantity()
                        );
                        record.setAgentCommission(commission);
                    }

                    // Always recalculate volume_sold based on product_code and quantity
                    if (record.getProductCode() != null && record.getQuantity() != null) {
                        BigDecimal volumeSold = VolumeCalculator.calculateVolumeSold(
                                record.getProductCode(),
                                record.getQuantity()
                        );
                        record.setVolumeSold(volumeSold);
                    }

                    return repository.save(record);
                }).orElseThrow(() -> new RuntimeException("Record not found"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a daily sale record",
               description = "Deletes a daily sale record by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Record deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Record not found")
    })
    public void deleteRecord(@Parameter(description = "ID of the record to delete", required = true) @PathVariable Long id) {
        repository.deleteById(id);
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

    // 8. Get total quantity sold by product code (all time): /api/sales/summary/product-sales
    @GetMapping("/summary/product-sales")
    @Operation(summary = "Get all-time product sales summary", description = "Retrieves total quantity sold for each product (all time)")
    public List<ProductSalesDTO> getProductSalesSummary() {
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCode();
        return productCostService.enrichWithCostsAndCommissionAllTime(results);
    }

    // 9. Get quantity sold by product code for specific date: /api/sales/summary/product-sales/date?date=2025-02-23
    @GetMapping("/summary/product-sales/date")
    @Operation(summary = "Get product sales summary by date", description = "Retrieves quantity sold for each product on a specific date")
    public List<ProductSalesDTO> getProductSalesSummaryByDate(
            @Parameter(description = "Date (YYYY-MM-DD)", required = true, example = "2026-03-04") @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDate(localDate);
        return productCostService.enrichWithCostsAndCommission(results, localDate);
    }

    // 10. Get quantity sold by product code for today: /api/sales/summary/product-sales/today
    @GetMapping("/summary/product-sales/today")
    @Operation(summary = "Get today's product sales summary", description = "Retrieves quantity sold for each product today")
    public List<ProductSalesDTO> getProductSalesSummaryToday() {
        LocalDate today = LocalDate.now();
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDate(today);
        return productCostService.enrichWithCostsAndCommission(results, today);
    }

    // 11. Get quantity sold by product code for date range: /api/sales/summary/product-sales/range?startDate=2025-02-01&endDate=2025-02-23
    @GetMapping("/summary/product-sales/range")
    @Operation(summary = "Get product sales summary by date range", description = "Retrieves quantity sold for each product within a date range")
    public List<ProductSalesDTO> getProductSalesSummaryByDateRange(
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true, example = "2026-03-01") @RequestParam String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true, example = "2026-03-04") @RequestParam String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDateRange(start, end);
        return productCostService.enrichWithCostsAndCommissionForDateRange(results, start, end);
    }

    // 12. Get quantity sold by product code for specific month: /api/sales/summary/product-sales/month?year=2025&month=2
    @GetMapping("/summary/product-sales/month")
    @Operation(summary = "Get product sales summary by month", description = "Retrieves quantity sold for each product in a specific month")
    public List<ProductSalesDTO> getProductSalesSummaryByMonth(
            @Parameter(description = "Year", required = true, example = "2026") @RequestParam int year,
            @Parameter(description = "Month (1-12)", required = true, example = "3") @RequestParam int month) {
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndMonth(year, month);
        return productCostService.enrichWithCostsAndCommissionByMonth(results, year, month);
    }

    // 13. Get quantity sold by product code for current month: /api/sales/summary/product-sales/current-month
    @GetMapping("/summary/product-sales/current-month")
    @Operation(summary = "Get current month's product sales summary", description = "Retrieves quantity sold for each product in the current month")
    public List<ProductSalesDTO> getProductSalesSummaryCurrentMonth() {
        LocalDate today = LocalDate.now();
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndMonth(today.getYear(), today.getMonthValue());
        return productCostService.enrichWithCostsAndCommissionByMonth(results, today.getYear(), today.getMonthValue());
    }

    // 14. Get quantity sold by product code for last 7 days: /api/sales/summary/product-sales/last-7-days
    @GetMapping("/summary/product-sales/last-7-days")
    @Operation(summary = "Get product sales summary for last 7 days", description = "Retrieves quantity sold for each product in the last 7 days")
    public List<ProductSalesDTO> getProductSalesSummaryLast7Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDateRange(startDate, endDate);
        return productCostService.enrichWithCostsAndCommissionForDateRange(results, startDate, endDate);
    }

    // 15. Get quantity sold by product code for last 15 days: /api/sales/summary/product-sales/last-15-days
    @GetMapping("/summary/product-sales/last-15-days")
    @Operation(summary = "Get product sales summary for last 15 days", description = "Retrieves quantity sold for each product in the last 15 days")
    public List<ProductSalesDTO> getProductSalesSummaryLast15Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(14);
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDateRange(startDate, endDate);
        return productCostService.enrichWithCostsAndCommissionForDateRange(results, startDate, endDate);
    }

    // 16. Get quantity sold by product code for last 30 days: /api/sales/summary/product-sales/last-30-days
    @GetMapping("/summary/product-sales/last-30-days")
    @Operation(summary = "Get product sales summary for last 30 days", description = "Retrieves quantity sold for each product in the last 30 days")
    public List<ProductSalesDTO> getProductSalesSummaryLast30Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDateRange(startDate, endDate);
        return productCostService.enrichWithCostsAndCommissionForDateRange(results, startDate, endDate);
    }

    // 17. Get quantity sold by product code for last 90 days: /api/sales/summary/product-sales/last-90-days
    @GetMapping("/summary/product-sales/last-90-days")
    @Operation(summary = "Get product sales summary for last 90 days", description = "Retrieves quantity sold for each product in the last 90 days")
    public List<ProductSalesDTO> getProductSalesSummaryLast90Days() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(89);
        List<ProductSalesDTO> results = repository.getQuantitySoldByProductCodeAndDateRange(startDate, endDate);
        return productCostService.enrichWithCostsAndCommissionForDateRange(results, startDate, endDate);
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

                // Calculate volume_sold based on product_code and quantity
                BigDecimal volumeSold = VolumeCalculator.calculateVolumeSold(item.productCode, item.quantity);
                incoming.setVolumeSold(volumeSold);

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

        // Deduct stock from salesman for each sale (SALE affects ONLY salesman stock)
        java.time.format.DateTimeFormatter saleDateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (DailySaleRecord sale : savedSales) {
            if (sale.getSalesmanName() != null && sale.getProductCode() != null && sale.getQuantity() != null) {
                try {
                    System.out.println(">>> Creating salesman ledger entry for bulk sale:");
                    System.out.println("    Salesman: " + sale.getSalesmanName());
                    System.out.println("    Product: " + sale.getProductCode());
                    System.out.println("    Quantity: " + sale.getQuantity());

                    SalesmanStockSummary salesmanSummary = salesmanStockSummaryRepository
                        .findBySalesmanAliasAndProductCode(sale.getSalesmanName(), sale.getProductCode())
                        .orElse(new SalesmanStockSummary());

                    int salesmanStockBefore = salesmanSummary.getCurrentStock() != null ? salesmanSummary.getCurrentStock() : 0;
                    int qtySold = sale.getQuantity();
                    int salesmanStockAfter = salesmanStockBefore - qtySold;

                    System.out.println("    Salesman Stock Before: " + salesmanStockBefore);
                    System.out.println("    Salesman Stock After: " + salesmanStockAfter);

                    // Update salesman_stock_summary (decrement by sold quantity)
                    salesmanSummary.setSalesmanAlias(sale.getSalesmanName());
                    salesmanSummary.setProductCode(sale.getProductCode());
                    salesmanSummary.setCurrentStock(salesmanStockAfter);
                    salesmanSummary.setLastUpdated(LocalDateTime.now());
                    salesmanStockSummaryRepository.save(salesmanSummary);

                    // Create salesman_ledger entry for SOLD (negative quantity)
                    SalesmanLedger salesmanLedger = new SalesmanLedger(
                        sale.getSalesmanName(),
                        sale.getProductCode(),
                        SalesmanTxnType.SOLD,
                        -qtySold,
                        "Sold via sales-expense bulk entry on " +
                            saleDateFormatter.format(sale.getSaleDate()),
                        "system"
                    );
                    salesmanLedgerRepository.save(salesmanLedger);
                    System.out.println(">>> Salesman ledger entry created successfully!");

                } catch (Exception e) {
                    System.err.println("!!! ERROR: Failed to update salesman ledger for sale: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

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
        target.setVolumeSold(source.getVolumeSold());
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
