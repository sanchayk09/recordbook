package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.DailyExpenseRecord;
import com.urviclean.recordbook.models.DailyExpenseRecordId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyExpenseRecordRepository extends JpaRepository<DailyExpenseRecord, DailyExpenseRecordId> {

    /**
     * Find all daily expense records for a specific salesman
     */
    List<DailyExpenseRecord> findBySalesmanAlias(String salesmanAlias);

    /**
     * Find all daily expense records for a specific date
     */
    List<DailyExpenseRecord> findByExpenseDate(LocalDate expenseDate);

    /**
     * Find all daily expense records for a specific salesman on a specific date
     */
    Optional<DailyExpenseRecord> findBySalesmanAliasAndExpenseDate(String salesmanAlias, LocalDate expenseDate);

    /**
     * Find all daily expense records within a date range for a specific salesman
     */
    @Query("SELECT d FROM DailyExpenseRecord d WHERE d.salesmanAlias = :salesmanAlias AND d.expenseDate BETWEEN :startDate AND :endDate ORDER BY d.expenseDate DESC")
    List<DailyExpenseRecord> findBySalesmanAliasAndDateRange(
            @Param("salesmanAlias") String salesmanAlias,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find all daily expense records within a date range
     */
    @Query("SELECT d FROM DailyExpenseRecord d WHERE d.expenseDate BETWEEN :startDate AND :endDate ORDER BY d.expenseDate DESC, d.salesmanAlias ASC")
    List<DailyExpenseRecord> findByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

