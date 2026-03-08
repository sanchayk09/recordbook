package com.urviclean.recordbook.services;

import com.urviclean.recordbook.exception.ResourceNotFoundException;
import com.urviclean.recordbook.models.Salesman;
import com.urviclean.recordbook.models.SalesmanExpense;
import com.urviclean.recordbook.repositories.SalesmanExpenseRepository;
import com.urviclean.recordbook.repositories.SalesmanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing salesman and salesman-expense data.
 */
@Service
@Transactional
public class SalesmanService {

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Autowired
    private SalesmanExpenseRepository salesmanExpenseRepository;

    // ---- Salesmen ----

    public List<Salesman> getAll() {
        return salesmanRepository.findAll();
    }

    public Salesman getById(Long id) {
        return salesmanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salesman", "id", id));
    }

    public Salesman save(Salesman salesman) {
        return salesmanRepository.save(salesman);
    }

    public void delete(Long id) {
        if (!salesmanRepository.existsById(id)) {
            throw new ResourceNotFoundException("Salesman", "id", id);
        }
        salesmanRepository.deleteById(id);
    }

    public List<Long> getIdsByName(String firstName, String lastName) {
        return salesmanRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)
                .stream()
                .map(Salesman::getSalesmanId)
                .toList();
    }

    // ---- Salesman Expenses ----

    public List<SalesmanExpense> getAllExpenses() {
        return salesmanExpenseRepository.findAll();
    }

    public SalesmanExpense getExpenseById(Long id) {
        return salesmanExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesmanExpense", "id", id));
    }

    public SalesmanExpense saveExpense(SalesmanExpense expense) {
        return salesmanExpenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        if (!salesmanExpenseRepository.existsById(id)) {
            throw new ResourceNotFoundException("SalesmanExpense", "id", id);
        }
        salesmanExpenseRepository.deleteById(id);
    }
}
