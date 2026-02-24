package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.DailySaleRecord;
import com.urviclean.recordbook.models.ProductSalesDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySaleRecordRepository extends JpaRepository<DailySaleRecord, Long> {

    List<DailySaleRecord> findByProductCode(String productCode);

    List<DailySaleRecord> findByQuantity(Integer quantity);

    List<DailySaleRecord> findByProductCodeAndQuantity(String productCode, Integer quantity);

    // Date filter methods
    List<DailySaleRecord> findBySaleDate(LocalDate saleDate);

    @Query("SELECT d FROM DailySaleRecord d WHERE d.saleDate >= :startDate AND d.saleDate <= :endDate ORDER BY d.saleDate DESC")
    List<DailySaleRecord> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM DailySaleRecord d WHERE YEAR(d.saleDate) = :year AND MONTH(d.saleDate) = :month ORDER BY d.saleDate DESC")
    List<DailySaleRecord> findByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT d FROM DailySaleRecord d WHERE YEAR(d.saleDate) = :year AND WEEK(d.saleDate) = :week ORDER BY d.saleDate DESC")
    List<DailySaleRecord> findByYearAndWeek(@Param("year") int year, @Param("week") int week);

    // Product sales summary - Group by product_code
    @Query("SELECT new com.urviclean.recordbook.models.ProductSalesDTO(d.productCode, SUM(d.quantity)) " +
           "FROM DailySaleRecord d GROUP BY d.productCode ORDER BY SUM(d.quantity) DESC")
    List<ProductSalesDTO> getQuantitySoldByProductCode();

    // Product sales summary with date range
    @Query("SELECT new com.urviclean.recordbook.models.ProductSalesDTO(d.productCode, SUM(d.quantity)) " +
           "FROM DailySaleRecord d WHERE d.saleDate >= :startDate AND d.saleDate <= :endDate " +
           "GROUP BY d.productCode ORDER BY SUM(d.quantity) DESC")
    List<ProductSalesDTO> getQuantitySoldByProductCodeAndDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Product sales summary for specific date
    @Query("SELECT new com.urviclean.recordbook.models.ProductSalesDTO(d.productCode, SUM(d.quantity)) " +
           "FROM DailySaleRecord d WHERE d.saleDate = :saleDate " +
           "GROUP BY d.productCode ORDER BY SUM(d.quantity) DESC")
    List<ProductSalesDTO> getQuantitySoldByProductCodeAndDate(@Param("saleDate") LocalDate saleDate);

    // Product sales summary for specific month
    @Query("SELECT new com.urviclean.recordbook.models.ProductSalesDTO(d.productCode, SUM(d.quantity)) " +
           "FROM DailySaleRecord d WHERE YEAR(d.saleDate) = :year AND MONTH(d.saleDate) = :month " +
           "GROUP BY d.productCode ORDER BY SUM(d.quantity) DESC")
    List<ProductSalesDTO> getQuantitySoldByProductCodeAndMonth(@Param("year") int year, @Param("month") int month);

    // Calculate total revenue and total agent commission for daily summary
    @Query("SELECT SUM(d.revenue) AS totalRevenue, SUM(d.agentCommission) AS totalCommission " +
           "FROM DailySaleRecord d WHERE d.salesmanName = :salesmanAlias AND d.saleDate = :saleDate")
    List<Object[]> calculateSalesSummary(@Param("salesmanAlias") String salesmanAlias, @Param("saleDate") LocalDate saleDate);
}