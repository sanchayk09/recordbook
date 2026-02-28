package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.DailyExpenseRecord;
import com.urviclean.recordbook.models.DailyExpenseRecordResponse;
import com.urviclean.recordbook.repositories.DailyExpenseRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily-expenses")
@CrossOrigin(origins = "http://localhost:3000")
public class DailyExpenseController {

    @Autowired
    private DailyExpenseRecordRepository repository;

    /**
     * Get all daily expense records
     */
    @GetMapping
    public ResponseEntity<List<DailyExpenseRecord>> getAllDailyExpenses() {
        return ResponseEntity.ok(repository.findAll());
    }

    /**
     * Get daily expense records for a specific salesman
     * GET /api/daily-expenses/salesman?alias=SALESMAN_NAME
     */
    @GetMapping("/salesman")
    public ResponseEntity<List<DailyExpenseRecord>> getExpensesByAlias(
            @RequestParam String alias) {
        List<DailyExpenseRecord> records = repository.findBySalesmanAlias(alias);
        return ResponseEntity.ok(records);
    }

    /**
     * Get daily expense records for a specific date
     * GET /api/daily-expenses/date?date=2024-02-23
     */
    @GetMapping("/date")
    public ResponseEntity<List<DailyExpenseRecord>> getExpensesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailyExpenseRecord> records = repository.findByExpenseDate(date);
        return ResponseEntity.ok(records);
    }

    /**
     * Get daily expense record for a specific salesman on a specific date
     * GET /api/daily-expenses/salesman-date?alias=SALESMAN_NAME&date=2024-02-23
     */
    @GetMapping("/salesman-date")
    public ResponseEntity<DailyExpenseRecordResponse> getExpenseByAliasAndDate(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return repository.findBySalesmanAliasAndExpenseDate(alias, date)
                .map(record -> ResponseEntity.ok(new DailyExpenseRecordResponse(record)))
                .orElseThrow(() -> new com.urviclean.recordbook.exception.ResourceNotFoundException(
                    "DailyExpenseRecord",
                    String.format("salesman: %s, date: %s", alias, date)
                ));
    }

    /**
     * Get daily expense records for a salesman within a date range
     * GET /api/daily-expenses/salesman-range?alias=SALESMAN_NAME&startDate=2024-02-01&endDate=2024-02-28
     */
    @GetMapping("/salesman-range")
    public ResponseEntity<List<DailyExpenseRecord>> getExpensesByAliasAndDateRange(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyExpenseRecord> records = repository.findBySalesmanAliasAndDateRange(alias, startDate, endDate);
        return ResponseEntity.ok(records);
    }

    /**
     * Get daily expense records within a date range for all salesmen
     * GET /api/daily-expenses/range?startDate=2024-02-01&endDate=2024-02-28
     */
    @GetMapping("/range")
    public ResponseEntity<List<DailyExpenseRecord>> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyExpenseRecord> records = repository.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(records);
    }

    /**
     * Create a new daily expense record
     * POST /api/daily-expenses
     */
    @PostMapping
    public ResponseEntity<DailyExpenseRecordResponse> createDailyExpense(
            @RequestBody DailyExpenseRecord record) {
        DailyExpenseRecord saved = repository.save(record);
        return ResponseEntity.ok(new DailyExpenseRecordResponse(saved));
    }

    /**
     * Update an existing daily expense record
     * PUT /api/daily-expenses?alias=SALESMAN_NAME&date=2024-02-23
     */
    @PutMapping
    public ResponseEntity<DailyExpenseRecordResponse> updateDailyExpense(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody DailyExpenseRecord record) {
        return repository.findBySalesmanAliasAndExpenseDate(alias, date)
                .map(existing -> {
                    existing.setTotalExpense(record.getTotalExpense());
                    DailyExpenseRecord updated = repository.save(existing);
                    return ResponseEntity.ok(new DailyExpenseRecordResponse(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Delete a daily expense record
     * DELETE /api/daily-expenses?alias=SALESMAN_NAME&date=2024-02-23
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteDailyExpense(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var id = new com.urviclean.recordbook.models.DailyExpenseRecordId(alias, date);
        if (!repository.existsById(id)) {
            throw new com.urviclean.recordbook.exception.ResourceNotFoundException(
                "DailyExpenseRecord",
                String.format("salesman: %s, date: %s", alias, date)
            );
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

