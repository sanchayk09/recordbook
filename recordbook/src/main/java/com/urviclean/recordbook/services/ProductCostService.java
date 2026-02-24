package com.urviclean.recordbook.services;

import com.urviclean.recordbook.models.ProductSalesDTO;
import com.urviclean.recordbook.repositories.ProductCostManualRepository;
import com.urviclean.recordbook.repositories.DailySaleRecordRepository;
import com.urviclean.recordbook.repositories.DailyExpenseRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductCostService {

    @Autowired
    private ProductCostManualRepository productCostManualRepository;

    @Autowired
    private DailySaleRecordRepository dailySaleRecordRepository;

    @Autowired
    private DailyExpenseRecordRepository dailyExpenseRecordRepository;

    /**
     * Enriches ProductSalesDTO with cost information from product_cost_manual table
     * Calculates: totalCost = productCost * totalQuantity
     * Calculates: totalProfit = totalRevenue - totalCost
     * Calculates: netProfit = totalRevenue - totalCost - salesmanCommission
     * Calculates: avgProfit = totalProfit / totalQuantity
     * Calculates: avgNetProfit = netProfit / totalQuantity
     */
    public ProductSalesDTO enrichWithCost(ProductSalesDTO dto) {
        if (dto == null || dto.getProductCode() == null) {
            return dto;
        }

        // Look up cost from product_cost_manual table
        var productCostOptional = productCostManualRepository.findByProductCode(dto.getProductCode());

        BigDecimal opCost = dto.getOpCost() != null ? dto.getOpCost() : BigDecimal.ZERO;

        if (productCostOptional.isPresent()) {
            BigDecimal cost = productCostOptional.get().getCost();
            dto.setProductCost(cost);

            // Calculate total cost: quantity * cost per unit
            BigDecimal totalCost = BigDecimal.valueOf(dto.getTotalQuantity())
                    .multiply(cost);
            dto.setTotalCost(totalCost);

            // Calculate total profit: totalRevenue - totalCost - opCost
            BigDecimal totalProfit = dto.getTotalRevenue().subtract(totalCost).subtract(opCost);
            dto.setTotalProfit(totalProfit);

            // Calculate net profit: totalRevenue - totalCost - opCost - salesmanCommission
            BigDecimal salesmanCommission = dto.getSalesmanCommission() != null ? dto.getSalesmanCommission() : BigDecimal.ZERO;
            BigDecimal netProfit = totalProfit.subtract(salesmanCommission);
            dto.setNetProfit(netProfit);

            // Calculate average profit: totalProfit / totalQuantity
            if (dto.getTotalQuantity() != null && dto.getTotalQuantity() > 0) {
                BigDecimal avgProfit = totalProfit.divide(
                        BigDecimal.valueOf(dto.getTotalQuantity()),
                        2,
                        RoundingMode.HALF_UP
                );
                dto.setAvgProfit(avgProfit);

                // Calculate average net profit: netProfit / totalQuantity
                BigDecimal avgNetProfit = netProfit.divide(
                        BigDecimal.valueOf(dto.getTotalQuantity()),
                        2,
                        RoundingMode.HALF_UP
                );
                dto.setAvgNetProfit(avgNetProfit);
            } else {
                dto.setAvgProfit(BigDecimal.ZERO);
                dto.setAvgNetProfit(BigDecimal.ZERO);
            }
        } else {
            // If cost not found, set to 0
            dto.setProductCost(BigDecimal.ZERO);
            dto.setTotalCost(BigDecimal.ZERO);
            // If no cost, profit equals revenue minus opCost
            BigDecimal totalProfit = dto.getTotalRevenue().subtract(opCost);
            dto.setTotalProfit(totalProfit);

            BigDecimal salesmanCommission = dto.getSalesmanCommission() != null ? dto.getSalesmanCommission() : BigDecimal.ZERO;
            BigDecimal netProfit = totalProfit.subtract(salesmanCommission);
            dto.setNetProfit(netProfit);

            if (dto.getTotalQuantity() != null && dto.getTotalQuantity() > 0) {
                BigDecimal avgProfit = totalProfit.divide(
                        BigDecimal.valueOf(dto.getTotalQuantity()),
                        2,
                        RoundingMode.HALF_UP
                );
                dto.setAvgProfit(avgProfit);

                BigDecimal avgNetProfit = netProfit.divide(
                        BigDecimal.valueOf(dto.getTotalQuantity()),
                        2,
                        RoundingMode.HALF_UP
                );
                dto.setAvgNetProfit(avgNetProfit);
            } else {
                dto.setAvgProfit(BigDecimal.ZERO);
                dto.setAvgNetProfit(BigDecimal.ZERO);
            }
        }

        return dto;
    }

    /**
     * Enriches a list of ProductSalesDTO with cost information
     */
    public List<ProductSalesDTO> enrichWithCosts(List<ProductSalesDTO> dtos) {
        if (dtos == null) {
            return dtos;
        }

        return dtos.stream()
                .map(this::enrichWithCost)
                .collect(Collectors.toList());
    }

    /**
     * Enriches a list of ProductSalesDTO with cost and commission information for a specific date
     * Fetches commission data from database and maps it to each product
     * Also calculates operational cost: opCostPerUnit * totalQuantity
     */
    public List<ProductSalesDTO> enrichWithCostsAndCommission(List<ProductSalesDTO> dtos, LocalDate saleDate) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        // Fetch total expense for the given date
        BigDecimal totalExpense = dailyExpenseRecordRepository.getTotalExpenseByDate(saleDate);
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        // Calculate total quantity sold across all products
        Long totalQuantitySold = dtos.stream()
                .mapToLong(dto -> dto.getTotalQuantity() != null ? dto.getTotalQuantity() : 0)
                .sum();

        // Calculate opCost per unit
        BigDecimal opCostPerUnit = BigDecimal.ZERO;
        if (totalQuantitySold > 0 && totalExpense.compareTo(BigDecimal.ZERO) > 0) {
            opCostPerUnit = totalExpense.divide(
                    BigDecimal.valueOf(totalQuantitySold),
                    4,  // 4 decimal places for precision
                    RoundingMode.HALF_UP
            );
        }

        // Fetch commission data by product for the given date
        List<Object[]> commissionData = dailySaleRecordRepository.getCommissionByProductAndDate(saleDate);
        Map<String, BigDecimal> commissionMap = commissionData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> {
                            BigDecimal commission = (BigDecimal) row[1];
                            return commission != null ? commission : BigDecimal.ZERO;
                        }
                ));

        final BigDecimal finalOpCostPerUnit = opCostPerUnit;

        // Enrich each DTO with commission, opCost and cost data
        return dtos.stream()
                .peek(dto -> {
                    BigDecimal commission = commissionMap.getOrDefault(dto.getProductCode(), BigDecimal.ZERO);
                    dto.setSalesmanCommission(commission);

                    // Calculate opCost for this product: opCostPerUnit * totalQuantity
                    BigDecimal productOpCost = finalOpCostPerUnit.multiply(BigDecimal.valueOf(dto.getTotalQuantity()));
                    dto.setOpCost(productOpCost.setScale(2, RoundingMode.HALF_UP));
                })
                .map(this::enrichWithCost)
                .collect(Collectors.toList());
    }

    /**
     * Enriches a list of ProductSalesDTO with cost and commission information for a date range
     * Fetches commission data and expense data for the date range
     */
    public List<ProductSalesDTO> enrichWithCostsAndCommissionForDateRange(List<ProductSalesDTO> dtos, LocalDate startDate, LocalDate endDate) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        // Fetch total expense for the date range
        BigDecimal totalExpense = dailyExpenseRecordRepository.getTotalExpenseByDateRange(startDate, endDate);
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        // Calculate total quantity sold across all products
        Long totalQuantitySold = dtos.stream()
                .mapToLong(dto -> dto.getTotalQuantity() != null ? dto.getTotalQuantity() : 0)
                .sum();

        // Calculate opCost per unit
        BigDecimal opCostPerUnit = BigDecimal.ZERO;
        if (totalQuantitySold > 0 && totalExpense.compareTo(BigDecimal.ZERO) > 0) {
            opCostPerUnit = totalExpense.divide(
                    BigDecimal.valueOf(totalQuantitySold),
                    4,  // 4 decimal places for precision
                    RoundingMode.HALF_UP
            );
        }

        // Fetch commission data by product for the date range
        List<Object[]> commissionData = dailySaleRecordRepository.getCommissionByProductAndDateRange(startDate, endDate);
        Map<String, BigDecimal> commissionMap = commissionData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> {
                            BigDecimal commission = (BigDecimal) row[1];
                            return commission != null ? commission : BigDecimal.ZERO;
                        }
                ));

        final BigDecimal finalOpCostPerUnit = opCostPerUnit;

        // Enrich each DTO with commission, opCost and cost data
        return dtos.stream()
                .peek(dto -> {
                    BigDecimal commission = commissionMap.getOrDefault(dto.getProductCode(), BigDecimal.ZERO);
                    dto.setSalesmanCommission(commission);

                    // Calculate opCost for this product: opCostPerUnit * totalQuantity
                    BigDecimal productOpCost = finalOpCostPerUnit.multiply(BigDecimal.valueOf(dto.getTotalQuantity()));
                    dto.setOpCost(productOpCost.setScale(2, RoundingMode.HALF_UP));
                })
                .map(this::enrichWithCost)
                .collect(Collectors.toList());
    }

    /**
     * Enriches a list of ProductSalesDTO with cost and commission information for all-time data
     * Fetches commission data for all products (no date filter)
     * Does NOT include opCost (only for specific dates)
     */
    public List<ProductSalesDTO> enrichWithCostsAndCommissionAllTime(List<ProductSalesDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        // Fetch commission data by product for all time
        List<Object[]> commissionData = dailySaleRecordRepository.getCommissionByProductAllTime();
        Map<String, BigDecimal> commissionMap = commissionData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> {
                            BigDecimal commission = (BigDecimal) row[1];
                            return commission != null ? commission : BigDecimal.ZERO;
                        }
                ));

        // Enrich each DTO with commission and cost data
        return dtos.stream()
                .peek(dto -> {
                    BigDecimal commission = commissionMap.getOrDefault(dto.getProductCode(), BigDecimal.ZERO);
                    dto.setSalesmanCommission(commission);
                })
                .map(this::enrichWithCost)
                .collect(Collectors.toList());
    }

    /**
     * Enriches a list of ProductSalesDTO with cost and commission information for monthly data
     * Fetches commission data by product for the specific month
     * Does NOT include opCost (only for specific dates)
     */
    public List<ProductSalesDTO> enrichWithCostsAndCommissionByMonth(List<ProductSalesDTO> dtos, int year, int month) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        // Fetch commission data by product for the month
        List<Object[]> commissionData = dailySaleRecordRepository.getCommissionByProductAndMonth(year, month);
        Map<String, BigDecimal> commissionMap = commissionData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> {
                            BigDecimal commission = (BigDecimal) row[1];
                            return commission != null ? commission : BigDecimal.ZERO;
                        }
                ));

        // Enrich each DTO with commission and cost data
        return dtos.stream()
                .peek(dto -> {
                    BigDecimal commission = commissionMap.getOrDefault(dto.getProductCode(), BigDecimal.ZERO);
                    dto.setSalesmanCommission(commission);
                })
                .map(this::enrichWithCost)
                .collect(Collectors.toList());
    }
}

