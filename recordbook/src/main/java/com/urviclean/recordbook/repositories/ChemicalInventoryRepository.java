package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.ChemicalInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChemicalInventoryRepository extends JpaRepository<ChemicalInventory, Long> {
}

