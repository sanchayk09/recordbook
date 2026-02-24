package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.ProductCostManual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCostManualRepository extends JpaRepository<ProductCostManual, Long> {

    /**
     * Find product cost by product code
     */
    Optional<ProductCostManual> findByProductCode(String productCode);

    /**
     * Find all products by product name
     */
    List<ProductCostManual> findByProductName(String productName);

    /**
     * Check if product code exists
     */
    boolean existsByProductCode(String productCode);
}

