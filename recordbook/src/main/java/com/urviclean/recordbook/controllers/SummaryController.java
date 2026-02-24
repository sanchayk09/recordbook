package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.DailySummaryRequest;
import com.urviclean.recordbook.models.DailySummaryResponse;
import com.urviclean.recordbook.services.DailySummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/summary")
@CrossOrigin(origins = "http://localhost:3000")
public class SummaryController {

    @Autowired
    private DailySummaryService dailySummaryService;

    /**
     * Submit/Create daily summary
     * POST /api/summary/submit
     * Calculates totalRevenue and totalAgentCommission from daily_sale_record
     * Calculates net_profit = totalRevenue - totalAgentCommission - totalExpense - materialCost
     */
    @PostMapping("/submit")
    public ResponseEntity<DailySummaryResponse> submitDailySummary(@RequestBody DailySummaryRequest request) {
        try {
            DailySummaryResponse response = dailySummaryService.submitDailySummary(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Get daily summary by salesman alias and date
     * GET /api/summary/by-salesman-date?alias=muk/antr&date=2026-02-23
     */
    @GetMapping("/by-salesman-date")
    public ResponseEntity<DailySummaryResponse> getSummary(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            DailySummaryResponse response = dailySummaryService.getSummary(alias, date);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all daily summaries
     * GET /api/summary/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<DailySummaryResponse>> getAllSummaries() {
        List<DailySummaryResponse> response = dailySummaryService.getAllSummaries();
        return ResponseEntity.ok(response);
    }

    /**
     * Get summaries for specific salesman
     * GET /api/summary/salesman?alias=muk/antr
     */
    @GetMapping("/salesman")
    public ResponseEntity<List<DailySummaryResponse>> getSummariesBySalesman(@RequestParam String alias) {
        List<DailySummaryResponse> response = dailySummaryService.getSummariesBySalesman(alias);
        return ResponseEntity.ok(response);
    }

    /**
     * Get summaries for specific date
     * GET /api/summary/date?date=2026-02-23
     */
    @GetMapping("/date")
    public ResponseEntity<List<DailySummaryResponse>> getSummariesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DailySummaryResponse> response = dailySummaryService.getSummariesByDate(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Get summaries for salesman in date range
     * GET /api/summary/salesman-range?alias=muk/antr&startDate=2026-02-01&endDate=2026-02-28
     */
    @GetMapping("/salesman-range")
    public ResponseEntity<List<DailySummaryResponse>> getSummariesByAliasAndDateRange(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailySummaryResponse> response = dailySummaryService.getSummariesByAliasAndDateRange(alias, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get summaries for all salesmen in date range
     * GET /api/summary/range?startDate=2026-02-01&endDate=2026-02-28
     */
    @GetMapping("/range")
    public ResponseEntity<List<DailySummaryResponse>> getSummariesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailySummaryResponse> response = dailySummaryService.getSummariesByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Update daily summary
     * PUT /api/summary/update?alias=muk/antr&date=2026-02-23
     */
    @PutMapping("/update")
    public ResponseEntity<DailySummaryResponse> updateSummary(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody DailySummaryRequest request) {
        try {
            DailySummaryResponse response = dailySummaryService.updateSummary(alias, date, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete daily summary
     * DELETE /api/summary/delete?alias=muk/antr&date=2026-02-23
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteSummary(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            dailySummaryService.deleteSummary(alias, date);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

