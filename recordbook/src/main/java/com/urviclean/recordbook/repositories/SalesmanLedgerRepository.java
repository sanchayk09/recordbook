package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.SalesmanLedger;
import com.urviclean.recordbook.models.SalesmanTxnType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SalesmanLedger entity
 * Provides data access for salesman stock movement audit trail
 */
@Repository
public interface SalesmanLedgerRepository extends JpaRepository<SalesmanLedger, Long> {

    /**
     * Find all ledger entries for a specific salesman
     */
    List<SalesmanLedger> findBySalesmanAlias(String salesmanAlias);

    /**
     * Find ledger entries for a specific salesman and product
     */
    List<SalesmanLedger> findBySalesmanAliasAndProductCode(String salesmanAlias, String productCode);

    /**
     * Find ledger entries by transaction type
     */
    List<SalesmanLedger> findByTxnType(SalesmanTxnType txnType);

    /**
     * Find ledger entries for a salesman within a date range
     */
    @Query("SELECT sl FROM SalesmanLedger sl WHERE sl.salesmanAlias = :alias " +
           "AND sl.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY sl.createdAt DESC")
    List<SalesmanLedger> findBySalesmanAliasAndDateRange(
            @Param("alias") String salesmanAlias,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find sold transactions for a salesman
     */
    @Query("SELECT sl FROM SalesmanLedger sl WHERE sl.salesmanAlias = :alias " +
           "AND sl.txnType = 'SOLD' " +
           "ORDER BY sl.createdAt DESC")
    List<SalesmanLedger> findSalesByAlias(@Param("alias") String salesmanAlias);

    /**
     * Calculate total stock issued to a salesman for a product
     */
    @Query("SELECT COALESCE(SUM(sl.deltaQty), 0) FROM SalesmanLedger sl " +
           "WHERE sl.salesmanAlias = :alias AND sl.productCode = :productCode " +
           "AND sl.txnType = 'ISSUE_FROM_WAREHOUSE'")
    Integer getTotalIssuedQty(@Param("alias") String salesmanAlias, @Param("productCode") String productCode);

    /**
     * Calculate total sold by a salesman for a product
     */
    @Query("SELECT COALESCE(SUM(ABS(sl.deltaQty)), 0) FROM SalesmanLedger sl " +
           "WHERE sl.salesmanAlias = :alias AND sl.productCode = :productCode " +
           "AND sl.txnType = 'SOLD'")
    Integer getTotalSoldQty(@Param("alias") String salesmanAlias, @Param("productCode") String productCode);

    /**
     * Find entries created after a specific date for archival purposes
     */
    List<SalesmanLedger> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find entries created before a specific date for archival/cleanup
     */
    List<SalesmanLedger> findByCreatedAtBefore(LocalDateTime date);

    /**
     * Calculate current stock for a salesman and product by summing all delta_qty
     * Positive delta = stock added (ISSUE_FROM_WAREHOUSE)
     * Negative delta = stock removed (SOLD, RETURN_TO_WAREHOUSE)
     */
    @Query("SELECT COALESCE(SUM(sl.deltaQty), 0) FROM SalesmanLedger sl " +
           "WHERE sl.salesmanAlias = :alias AND sl.productCode = :productCode")
    Integer getCurrentStock(@Param("alias") String salesmanAlias, @Param("productCode") String productCode);

    /**
     * Get all distinct salesman aliases who have ledger entries
     */
    @Query("SELECT DISTINCT sl.salesmanAlias FROM SalesmanLedger sl")
    List<String> findAllDistinctSalesmanAliases();

    /**
     * Get all distinct product codes for a specific salesman
     */
    @Query("SELECT DISTINCT sl.productCode FROM SalesmanLedger sl WHERE sl.salesmanAlias = :alias")
    List<String> findDistinctProductCodesByAlias(@Param("alias") String salesmanAlias);

    /**
     * Get current stock grouped by salesman and product
     * Returns tuples of [salesmanAlias, productCode, sum(deltaQty)]
     */
    @Query("SELECT sl.salesmanAlias, sl.productCode, SUM(sl.deltaQty) " +
           "FROM SalesmanLedger sl " +
           "GROUP BY sl.salesmanAlias, sl.productCode " +
           "HAVING SUM(sl.deltaQty) > 0")
    List<Object[]> getCurrentStockGroupedBySalesmanAndProduct();
}

