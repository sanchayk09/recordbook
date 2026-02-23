package com.urviclean.recordbook.repositories;

import com.urviclean.recordbook.models.Salesman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesmanRepository extends JpaRepository<Salesman, Long> {
    List<Salesman> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    java.util.Optional<Salesman> findByAliasIgnoreCase(String alias);
}
