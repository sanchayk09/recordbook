package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {

    /**
     * Find inventory by product code
     */
    Optional<WarehouseInventory> findByProductCode(String productCode);

    /**
     * Check if inventory exists for a product
     */
    boolean existsByProductCode(String productCode);

    /**
     * Get total stock value
     */
    @Query("SELECT SUM(w.qtyAvailable) FROM WarehouseInventory w")
    Long getTotalStockQuantity();
}

