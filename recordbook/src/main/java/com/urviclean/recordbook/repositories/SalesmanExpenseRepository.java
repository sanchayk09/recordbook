package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.SalesmanExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesmanExpenseRepository extends JpaRepository<SalesmanExpense, Long> {
}

