package com.urviclean.recordbook.services;

import com.urviclean.recordbook.exception.InvalidInputException;
import com.urviclean.recordbook.models.DailyExpenseRecord;
import com.urviclean.recordbook.models.DailySalesRecordCalculation;
import com.urviclean.recordbook.models.DailySummary;
import com.urviclean.recordbook.models.DailySummaryRequest;
import com.urviclean.recordbook.models.DailySummaryResponse;
import com.urviclean.recordbook.repositories.DailyExpenseRecordRepository;
import com.urviclean.recordbook.repositories.DailySaleRecordRepository;
import com.urviclean.recordbook.repositories.DailySummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DailySummaryService {

    @Autowired
    private DailySummaryRepository dailySummaryRepository;

    @Autowired
    private DailySaleRecordRepository dailySaleRecordRepository;

    @Autowired
    private DailyExpenseRecordRepository dailyExpenseRecordRepository;

    /**
     * Compute all summary totals from source tables and upsert into daily_summary.
     *
     * <ul>
     *   <li>Sales aggregates (revenue, commission, volume, quantity) come from daily_sale_record.</li>
     *   <li>Material cost = SUM(quantity * cost) joined with product_cost_manual.</li>
     *   <li>Expense comes from daily_expense_record (0 if no row found).</li>
     * </ul>
     *
     * This method is called automatically after any sale create/update/delete,
     * and after expense save/update, to keep daily_summary always up-to-date.
     *
     * @param salesmanAlias the salesman alias (maps to daily_sale_record.salesman_name)
     * @param saleDate      the date for which the summary should be computed
     * @return the persisted {@link DailySummaryResponse}
     */
    @Transactional
    public DailySummaryResponse computeAndPersistSummary(String salesmanAlias, LocalDate saleDate) {
        if (salesmanAlias == null || salesmanAlias.isBlank()) {
            throw new InvalidInputException("salesmanAlias", "must not be blank");
        }
        if (saleDate == null) {
            throw new InvalidInputException("saleDate", "must not be null");
        }

        // 1. Sales aggregates from daily_sale_record
        DailySalesRecordCalculation calc = calculateTotalsFromDailySales(salesmanAlias, saleDate);

        // 2. Material cost: SUM(quantity * cost) joined with product_cost_manual
        BigDecimal materialCost = dailySaleRecordRepository.calculateMaterialCost(salesmanAlias, saleDate);
        if (materialCost == null) materialCost = BigDecimal.ZERO;

        // 3. Expense from daily_expense_record (0 when no row exists)
        BigDecimal totalExpense = dailyExpenseRecordRepository
                .findBySalesmanAliasAndExpenseDate(salesmanAlias, saleDate)
                .map(DailyExpenseRecord::getTotalExpense)
                .map(e -> e != null ? e : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);

        // 4. Upsert daily_summary
        DailySummary summary = dailySummaryRepository
                .findBySalesmanAliasAndSaleDate(salesmanAlias, saleDate)
                .orElse(new DailySummary());

        summary.setSalesmanAlias(salesmanAlias);
        summary.setSaleDate(saleDate);
        summary.setTotalRevenue(calc.getTotalRevenue());
        summary.setTotalAgentCommission(calc.getTotalAgentCommission());
        summary.setMaterialCost(materialCost);
        summary.setTotalExpense(totalExpense);
        summary.setVolumeSold(calc.getTotalVolumeSold());
        summary.setTotalQuantity(calc.getTotalQuantity());
        // netProfit is recalculated automatically in @PrePersist / @PreUpdate

        DailySummary saved = dailySummaryRepository.save(summary);
        return new DailySummaryResponse(saved);
    }

    /**
     * Submit/Create daily summary
     * Calculates totalRevenue and totalAgentCommission from daily_sale_record
     * Then calculates net_profit
     * If summary already exists for this salesman+date, it will UPDATE it instead of creating new
     */
    @Transactional
    public DailySummaryResponse submitDailySummary(DailySummaryRequest request) {
        if (request == null || request.salesmanAlias == null || request.saleDate == null) {
            throw new RuntimeException("Salesman alias and sale date are required");
        }

        // Calculate totals from daily_sale_record
        DailySalesRecordCalculation calculation = calculateTotalsFromDailySales(
                request.salesmanAlias,
                request.saleDate
        );

        // Check if summary already exists for this salesman+date
        DailySummary summary = dailySummaryRepository
                .findBySalesmanAliasAndSaleDate(request.salesmanAlias, request.saleDate)
                .orElse(null);

        if (summary != null) {
            // UPDATE existing record - update all fields
            summary.setSalesmanAlias(request.salesmanAlias);
            summary.setTotalRevenue(calculation.getTotalRevenue());
            summary.setTotalAgentCommission(calculation.getTotalAgentCommission());
            summary.setTotalExpense(request.totalExpense != null ? request.totalExpense : BigDecimal.ZERO);
            summary.setMaterialCost(request.materialCost != null ? request.materialCost : BigDecimal.ZERO);
            summary.setVolumeSold(calculation.getTotalVolumeSold());
            summary.setTotalQuantity(calculation.getTotalQuantity());
            // netProfit will be recalculated in @PreUpdate
        } else {
            // CREATE new record
            summary = new DailySummary(
                    request.salesmanAlias,
                    request.saleDate,
                    calculation.getTotalRevenue(),
                    calculation.getTotalAgentCommission(),
                    request.totalExpense != null ? request.totalExpense : BigDecimal.ZERO,
                    request.materialCost != null ? request.materialCost : BigDecimal.ZERO,
                    calculation.getTotalVolumeSold(),
                    calculation.getTotalQuantity()
            );
        }

        // Save to database (insert or update)
        DailySummary saved = dailySummaryRepository.save(summary);
        return new DailySummaryResponse(saved);
    }

    /**
     * Calculate totals from daily_sale_record table
     * Groups by salesman_name and sale_date
     * Sums: revenue, agent_commission, volume_sold, and quantity
     */
    private DailySalesRecordCalculation calculateTotalsFromDailySales(String salesmanAlias, LocalDate saleDate) {
        List<Object[]> results = dailySaleRecordRepository.calculateSalesSummary(salesmanAlias, saleDate);

        if (results.isEmpty()) {
            // No sales found for this salesman on this date
            return new DailySalesRecordCalculation(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        }

        Object[] row = results.get(0);
        BigDecimal totalRevenue = (BigDecimal) row[0];
        BigDecimal totalCommission = (BigDecimal) row[1];
        BigDecimal totalVolumeSold = (BigDecimal) row[2];
        Long totalQuantity = (Long) row[3];

        return new DailySalesRecordCalculation(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalCommission != null ? totalCommission : BigDecimal.ZERO,
                totalVolumeSold != null ? totalVolumeSold : BigDecimal.ZERO,
                totalQuantity != null ? totalQuantity : 0L
        );
    }

    /**
     * Get summary by salesman and date
     */
    public DailySummaryResponse getSummary(String salesmanAlias, LocalDate saleDate) {
        return dailySummaryRepository.findBySalesmanAliasAndSaleDate(salesmanAlias, saleDate)
                .map(DailySummaryResponse::new)
                .orElseThrow(() -> new RuntimeException("Summary not found for " + salesmanAlias + " on " + saleDate));
    }

    /**
     * Get all summaries
     */
    public List<DailySummaryResponse> getAllSummaries() {
        return dailySummaryRepository.findAll()
                .stream()
                .map(DailySummaryResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get summaries for specific salesman
     */
    public List<DailySummaryResponse> getSummariesBySalesman(String salesmanAlias) {
        return dailySummaryRepository.findBySalesmanAlias(salesmanAlias)
                .stream()
                .map(DailySummaryResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get summaries for specific date
     */
    public List<DailySummaryResponse> getSummariesByDate(LocalDate saleDate) {
        return dailySummaryRepository.findBySaleDate(saleDate)
                .stream()
                .map(DailySummaryResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get summaries for salesman in date range
     */
    public List<DailySummaryResponse> getSummariesByAliasAndDateRange(String salesmanAlias, LocalDate startDate, LocalDate endDate) {
        return dailySummaryRepository.findBySalesmanAliasAndSaleDateBetween(salesmanAlias, startDate, endDate)
                .stream()
                .map(DailySummaryResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get summaries for all salesmen in date range
     */
    public List<DailySummaryResponse> getSummariesByDateRange(LocalDate startDate, LocalDate endDate) {
        return dailySummaryRepository.findBySaleDateBetween(startDate, endDate)
                .stream()
                .map(DailySummaryResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Update summary
     */
    @Transactional
    public DailySummaryResponse updateSummary(String salesmanAlias, LocalDate saleDate, DailySummaryRequest request) {
        DailySummary summary = dailySummaryRepository.findBySalesmanAliasAndSaleDate(salesmanAlias, saleDate)
                .orElseThrow(() -> new RuntimeException("Summary not found"));

        // Recalculate from daily_sale_record
        DailySalesRecordCalculation calculation = calculateTotalsFromDailySales(salesmanAlias, saleDate);

        summary.setTotalRevenue(calculation.getTotalRevenue());
        summary.setTotalAgentCommission(calculation.getTotalAgentCommission());
        summary.setVolumeSold(calculation.getTotalVolumeSold());
        summary.setTotalQuantity(calculation.getTotalQuantity());
        summary.setTotalExpense(request.totalExpense != null ? request.totalExpense : BigDecimal.ZERO);
        summary.setMaterialCost(request.materialCost != null ? request.materialCost : BigDecimal.ZERO);

        DailySummary updated = dailySummaryRepository.save(summary);
        return new DailySummaryResponse(updated);
    }

    /**
     * Delete summary
     */
    public void deleteSummary(String salesmanAlias, LocalDate saleDate) {
        DailySummary summary = dailySummaryRepository.findBySalesmanAliasAndSaleDate(salesmanAlias, saleDate)
                .orElseThrow(() -> new RuntimeException("Summary not found"));
        dailySummaryRepository.delete(summary);
    }
}
