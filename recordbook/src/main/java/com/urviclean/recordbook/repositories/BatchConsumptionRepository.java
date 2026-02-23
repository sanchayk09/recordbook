package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.BatchConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchConsumptionRepository extends JpaRepository<BatchConsumption, Long> {
}

