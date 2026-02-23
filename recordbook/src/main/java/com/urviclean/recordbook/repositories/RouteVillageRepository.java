package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.RouteVillage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteVillageRepository extends JpaRepository<RouteVillage, Long> {
}

