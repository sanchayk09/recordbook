package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.SalesmanStockSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesmanStockSummaryRepository extends JpaRepository<SalesmanStockSummary, Long> {

    /**
     * Get stock for a specific salesman-product combo
     * O(1) lookup instead of scanning entire ledger
     */
    Optional<SalesmanStockSummary> findBySalesmanAliasAndProductCode(String salesmanAlias, String productCode);

    /**
     * Get all products held by a salesman
     * Much faster than scanning ledger
     */
    List<SalesmanStockSummary> findBySalesmanAliasAndCurrentStockGreaterThan(String salesmanAlias, Integer minStock);

    /**
     * Get all stock for a salesman
     */
    List<SalesmanStockSummary> findBySalesmanAlias(String salesmanAlias);

    /**
     * Get all salesmen holding a specific product
     */
    List<SalesmanStockSummary> findByProductCodeAndCurrentStockGreaterThan(String productCode, Integer minStock);

    /**
     * Total stock a salesman has
     */
    @Query("SELECT COALESCE(SUM(s.currentStock), 0) FROM SalesmanStockSummary s WHERE s.salesmanAlias = :salesmanAlias")
    Integer getTotalStockBySalesman(@Param("salesmanAlias") String salesmanAlias);

    /**
     * Total quantity currently held by salesman across all products
     */
    @Query("SELECT COALESCE(SUM(s.currentStock), 0) FROM SalesmanStockSummary s WHERE s.salesmanAlias = :salesmanAlias")
    Integer getTotalIssuedBySalesman(@Param("salesmanAlias") String salesmanAlias);

    /**
     * Get total quantity sold by salesman (from sales records)
     */
    @Query("SELECT COALESCE(SUM(dsr.quantity), 0) FROM DailySaleRecord dsr WHERE dsr.salesmanName = :salesmanAlias")
    Integer getTotalSoldBySalesman(@Param("salesmanAlias") String salesmanAlias);

    /**
     * Check if stock exists for a salesman-product combo
     */
    Boolean existsBySalesmanAliasAndProductCode(String salesmanAlias, String productCode);

    /**
     * Get all records where current_stock > 0
     * This is the main method for the salesmen-stock-summary API
     */
    List<SalesmanStockSummary> findByCurrentStockGreaterThan(Integer minStock);
}

