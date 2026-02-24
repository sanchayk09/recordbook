package com.urviclean.recordbook.services;

import com.urviclean.recordbook.models.DailySalesRecordCalculation;
import com.urviclean.recordbook.models.DailySummary;
import com.urviclean.recordbook.models.DailySummaryRequest;
import com.urviclean.recordbook.models.DailySummaryResponse;
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

    /**
     * Submit/Create daily summary
     * Calculates totalRevenue and totalAgentCommission from daily_sale_record
     * Then calculates net_profit
     */
    @Transactional
    public DailySummaryResponse submitDailySummary(DailySummaryRequest request) {
        if (request == null || request.salesmanAlias == null || request.saleDate == null) {
            throw new RuntimeException("Salesman alias and sale date are required");
        }

        // Check if summary already exists
        if (dailySummaryRepository.existsBySalesmanAliasAndSaleDate(request.salesmanAlias, request.saleDate)) {
            throw new RuntimeException("Summary already exists for " + request.salesmanAlias + " on " + request.saleDate);
        }

        // Calculate totals from daily_sale_record
        DailySalesRecordCalculation calculation = calculateTotalsFromDailySales(
                request.salesmanAlias,
                request.saleDate
        );

        // Create DailySummary entity
        DailySummary summary = new DailySummary(
                request.salesmanAlias,
                request.saleDate,
                calculation.getTotalRevenue(),
                calculation.getTotalAgentCommission(),
                request.totalExpense != null ? request.totalExpense : BigDecimal.ZERO,
                request.materialCost != null ? request.materialCost : BigDecimal.ZERO
        );

        // Save to database
        DailySummary saved = dailySummaryRepository.save(summary);
        return new DailySummaryResponse(saved);
    }

    /**
     * Calculate totals from daily_sale_record table
     * Groups by salesman_name and sale_date
     * Sums: revenue and agent_commission
     */
    private DailySalesRecordCalculation calculateTotalsFromDailySales(String salesmanAlias, LocalDate saleDate) {
        List<Object[]> results = dailySaleRecordRepository.calculateSalesSummary(salesmanAlias, saleDate);

        if (results.isEmpty()) {
            // No sales found for this salesman on this date
            return new DailySalesRecordCalculation(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        Object[] row = results.get(0);
        BigDecimal totalRevenue = (BigDecimal) row[0];
        BigDecimal totalCommission = (BigDecimal) row[1];

        return new DailySalesRecordCalculation(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalCommission != null ? totalCommission : BigDecimal.ZERO
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
