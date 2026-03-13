package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.DailySummaryRequest;
import com.urviclean.recordbook.models.DailySummaryResponse;
import com.urviclean.recordbook.services.DailySummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Daily Summary", description = "APIs for daily sales summary, revenue, expenses, and profit calculations")
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
    @Operation(summary = "Submit daily summary",
               description = "Creates or updates daily summary with revenue, expenses, commissions, and profit calculations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Daily summary created/updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<DailySummaryResponse> submitDailySummary(@RequestBody DailySummaryRequest request) {
        DailySummaryResponse response = dailySummaryService.submitDailySummary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * UNIFIED Daily Summary Query Endpoint
     *
     * Flexible endpoint that handles all query patterns with optional parameters:
     * - No params = all summaries
     * - alias only = summaries for specific salesman (all dates)
     * - date only = summaries for specific date (all salesmen)
     * - alias + date = single summary for salesman on specific date
     * - alias + startDate + endDate = summaries for salesman in date range
     * - startDate + endDate = summaries for all salesmen in date range
     *
     * Examples:
     * /api/summary (all summaries)
     * /api/summary?alias=muk/antr (all dates for salesman)
     * /api/summary?date=2026-02-23 (all salesmen on date)
     * /api/summary?alias=muk/antr&date=2026-02-23 (single summary)
     * /api/summary?alias=muk/antr&startDate=2026-02-01&endDate=2026-02-28 (salesman range)
     * /api/summary?startDate=2026-02-01&endDate=2026-02-28 (all salesmen range)
     */
    @GetMapping
    @Operation(
        summary = "Get daily summaries with flexible filtering",
        description = "Unified endpoint supporting queries by salesman, date, date range, or combinations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summaries retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "No summaries found matching criteria")
    })
    public ResponseEntity<?> getDailySummaries(
            @Parameter(description = "Salesman alias", example = "muk/antr")
            @RequestParam(required = false) String alias,

            @Parameter(description = "Specific date (YYYY-MM-DD)", example = "2026-02-23")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(description = "Start date for range query (YYYY-MM-DD)", example = "2026-02-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date for range query (YYYY-MM-DD)", example = "2026-02-28")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        // Priority 1: Single summary (alias + date)
        if (alias != null && date != null) {
            DailySummaryResponse response = dailySummaryService.getSummary(alias, date);
            return ResponseEntity.ok(response);
        }

        // Priority 2: Salesman in date range (alias + startDate + endDate)
        if (alias != null && startDate != null && endDate != null) {
            List<DailySummaryResponse> response = dailySummaryService.getSummariesByAliasAndDateRange(alias, startDate, endDate);
            return ResponseEntity.ok(response);
        }

        // Priority 3: All salesmen in date range (startDate + endDate)
        if (startDate != null && endDate != null) {
            List<DailySummaryResponse> response = dailySummaryService.getSummariesByDateRange(startDate, endDate);
            return ResponseEntity.ok(response);
        }

        // Priority 4: All summaries for specific salesman (alias only)
        if (alias != null) {
            List<DailySummaryResponse> response = dailySummaryService.getSummariesBySalesman(alias);
            return ResponseEntity.ok(response);
        }

        // Priority 5: All summaries for specific date (date only)
        if (date != null) {
            List<DailySummaryResponse> response = dailySummaryService.getSummariesByDate(date);
            return ResponseEntity.ok(response);
        }

        // Default: All summaries
        List<DailySummaryResponse> response = dailySummaryService.getAllSummaries();
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
        DailySummaryResponse response = dailySummaryService.updateSummary(alias, date, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete daily summary
     * DELETE /api/summary/delete?alias=muk/antr&date=2026-02-23
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteSummary(
            @RequestParam String alias,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        dailySummaryService.deleteSummary(alias, date);
        return ResponseEntity.noContent().build();
    }
}

