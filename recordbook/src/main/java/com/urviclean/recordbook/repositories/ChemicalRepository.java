package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.Chemical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChemicalRepository extends JpaRepository<Chemical, Long> {
}

