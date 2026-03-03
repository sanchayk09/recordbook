package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.WarehouseLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WarehouseLedgerRepository extends JpaRepository<WarehouseLedger, Long> {

    /**
     * Find all ledger entries for a product
     */
    List<WarehouseLedger> findByProductCodeOrderByCreatedAtDesc(String productCode);

    /**
     * Find all ledger entries for a salesman
     */
    List<WarehouseLedger> findBySalesmanAliasOrderByCreatedAtDesc(String salesmanAlias);

    /**
     * Find ledger entries by transaction type
     */
    List<WarehouseLedger> findByTxnTypeOrderByCreatedAtDesc(WarehouseLedger.TransactionType txnType);

    /**
     * Find ledger entries within date range
     */
    List<WarehouseLedger> findByCreatedAtBetweenOrderByCreatedAtDesc(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Get total quantity issued to a salesman
     */
    @Query("SELECT SUM(wl.deltaQty) FROM WarehouseLedger wl " +
           "WHERE wl.salesmanAlias = :salesmanAlias " +
           "AND wl.txnType = 'ISSUE_TO_SALESMAN'")
    Long getTotalIssuedToSalesman(@Param("salesmanAlias") String salesmanAlias);

    /**
     * Get total quantity returned from a salesman
     */
    @Query("SELECT SUM(wl.deltaQty) FROM WarehouseLedger wl " +
           "WHERE wl.salesmanAlias = :salesmanAlias " +
           "AND wl.txnType = 'RETURN_FROM_SALESMAN'")
    Long getTotalReturnedFromSalesman(@Param("salesmanAlias") String salesmanAlias);

    /**
     * Get current stock with salesman (issued - returned)
     */
    @Query("SELECT SUM(CASE " +
           "  WHEN wl.txnType = 'ISSUE_TO_SALESMAN' THEN wl.deltaQty " +
           "  WHEN wl.txnType = 'RETURN_FROM_SALESMAN' THEN -wl.deltaQty " +
           "  ELSE 0 END) " +
           "FROM WarehouseLedger wl " +
           "WHERE wl.salesmanAlias = :salesmanAlias " +
           "AND wl.productCode = :productCode")
    Long getStockWithSalesman(
        @Param("salesmanAlias") String salesmanAlias,
        @Param("productCode") String productCode
    );
}

