package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySummaryRepository extends JpaRepository<DailySummary, Long> {

    /**
     * Find daily summary by salesman alias and sale date
     */
    Optional<DailySummary> findBySalesmanAliasAndSaleDate(String salesmanAlias, LocalDate saleDate);

    /**
     * Find all summaries for a specific salesman
     */
    List<DailySummary> findBySalesmanAlias(String salesmanAlias);

    /**
     * Find all summaries for a specific date
     */
    List<DailySummary> findBySaleDate(LocalDate saleDate);

    /**
     * Find summaries for salesman in date range
     */
    List<DailySummary> findBySalesmanAliasAndSaleDateBetween(String salesmanAlias, LocalDate startDate, LocalDate endDate);

    /**
     * Find summaries for all salesmen in date range
     */
    List<DailySummary> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Check if summary exists for salesman on specific date
     */
    boolean existsBySalesmanAliasAndSaleDate(String salesmanAlias, LocalDate saleDate);
}

