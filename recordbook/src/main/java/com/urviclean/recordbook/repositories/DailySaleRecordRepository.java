package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.DailySaleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySaleRecordRepository extends JpaRepository<DailySaleRecord, Long> {

    List<DailySaleRecord> findByProductCode(String productCode);

    List<DailySaleRecord> findByQuantity(Integer quantity);

    List<DailySaleRecord> findByProductCodeAndQuantity(String productCode, Integer quantity);

    List<DailySaleRecord> findBySaleDateBetweenOrderBySaleDateAsc(LocalDate startDate, LocalDate endDate);
}