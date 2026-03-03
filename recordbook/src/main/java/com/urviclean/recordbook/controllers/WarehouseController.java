package com.urviclean.recordbook.controllers;

import com.urviclean.recordbook.models.*;
import com.urviclean.recordbook.services.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Warehouse Controller - REST API for warehouse management
 *
 * Endpoints:
 * GET    /api/warehouse/inventory              - Get all warehouse stock
 * GET    /api/warehouse/inventory/{productCode} - Get stock for specific product
 * POST   /api/warehouse/issue                   - Issue stock to salesman
 * POST   /api/warehouse/return                  - Return stock from salesman
 * POST   /api/warehouse/adjust                  - Adjust stock (TRANSFER_IN, DAMAGE, etc.)
 * GET    /api/warehouse/ledger                  - Get all ledger entries
 * GET    /api/warehouse/ledger/product/{code}   - Get ledger for product
 * GET    /api/warehouse/ledger/salesman/{alias} - Get ledger for salesman
 * GET    /api/warehouse/salesman/{alias}/stock  - Get stock with salesman
 */
@RestController
@RequestMapping("/api/warehouse")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    /**
     * Get all warehouse inventory
     */
    @GetMapping("/inventory")
    public ResponseEntity<List<WarehouseInventoryResponse>> getAllInventory() {
        List<WarehouseInventoryResponse> inventory = warehouseService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    /**
     * Get inventory for specific product
     */
    @GetMapping("/inventory/{productCode}")
    public ResponseEntity<WarehouseInventoryResponse> getInventoryByProductCode(
            @PathVariable String productCode) {
        WarehouseInventoryResponse inventory = warehouseService.getInventoryByProductCode(productCode);
        return ResponseEntity.ok(inventory);
    }

    /**
     * Issue stock to salesman
     */
    @PostMapping("/issue")
    public ResponseEntity<Map<String, Object>> issueStockToSalesman(
            @RequestBody IssueStockRequest request) {
        WarehouseLedger ledger = warehouseService.issueStockToSalesman(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Stock issued successfully to " + request.getSalesmanAlias());
        response.put("ledgerEntry", ledger);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Return stock from salesman
     */
    @PostMapping("/return")
    public ResponseEntity<Map<String, Object>> returnStockFromSalesman(
            @RequestBody ReturnStockRequest request) {
        WarehouseLedger ledger = warehouseService.returnStockFromSalesman(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Stock returned successfully from " + request.getSalesmanAlias());
        response.put("ledgerEntry", ledger);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Adjust warehouse stock (TRANSFER_IN, MANUAL_ADJUST, DAMAGE)
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, Object>> adjustStock(
            @RequestBody AdjustStockRequest request) {
        WarehouseLedger ledger = warehouseService.adjustStock(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Stock adjusted successfully");
        response.put("ledgerEntry", ledger);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all ledger entries
     */
    @GetMapping("/ledger")
    public ResponseEntity<List<WarehouseLedger>> getAllLedgerEntries() {
        List<WarehouseLedger> ledger = warehouseService.getAllLedgerEntries();
        return ResponseEntity.ok(ledger);
    }

    /**
     * Get ledger entries for a product
     */
    @GetMapping("/ledger/product/{productCode}")
    public ResponseEntity<List<WarehouseLedger>> getLedgerByProduct(
            @PathVariable String productCode) {
        List<WarehouseLedger> ledger = warehouseService.getLedgerByProduct(productCode);
        return ResponseEntity.ok(ledger);
    }

    /**
     * Get ledger entries for a salesman
     */
    @GetMapping("/ledger/salesman/{salesmanAlias}")
    public ResponseEntity<List<WarehouseLedger>> getLedgerBySalesman(
            @PathVariable String salesmanAlias) {
        List<WarehouseLedger> ledger = warehouseService.getLedgerBySalesman(salesmanAlias);
        return ResponseEntity.ok(ledger);
    }

    /**
     * Get current stock with salesman
     */
    @GetMapping("/salesman/{salesmanAlias}/stock")
    public ResponseEntity<Map<String, Object>> getSalesmanStock(
            @PathVariable String salesmanAlias,
            @RequestParam(required = false) String productCode) {

        Map<String, Object> response = new HashMap<>();
        response.put("salesmanAlias", salesmanAlias);

        if (productCode != null) {
            Long stock = warehouseService.getStockWithSalesman(salesmanAlias, productCode);
            response.put("productCode", productCode);
            response.put("currentStock", stock);
        } else {
            Long totalIssued = warehouseService.getTotalIssuedToSalesman(salesmanAlias);
            Long totalReturned = warehouseService.getTotalReturnedFromSalesman(salesmanAlias);
            response.put("totalIssued", totalIssued);
            response.put("totalReturned", totalReturned);
            response.put("currentStock", totalIssued - totalReturned);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get all salesmen with their current stock (product-wise breakdown)
     */
    @GetMapping("/salesmen-stock-summary")
    public ResponseEntity<List<SalesmanStockDTO>> getAllSalesmenWithStock() {
        List<SalesmanStockDTO> summary = warehouseService.getAllSalesmenWithStock();
        return ResponseEntity.ok(summary);
    }

    /**
     * Batch issue stock to salesman (multiple products at once)
     */
    @PostMapping("/batch-issue")
    public ResponseEntity<BatchOperationResponse> batchIssueStock(@RequestBody BatchIssueRequest request) {
        BatchOperationResponse response = warehouseService.batchIssueStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Batch return stock from salesman (multiple products at once)
     */
    @PostMapping("/batch-return")
    public ResponseEntity<BatchOperationResponse> batchReturnStock(@RequestBody BatchReturnRequest request) {
        BatchOperationResponse response = warehouseService.batchReturnStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Return all stock from a salesman
     */
    @PostMapping("/return-all/{salesmanAlias}")
    public ResponseEntity<BatchOperationResponse> returnAllStock(
            @PathVariable String salesmanAlias,
            @RequestParam(required = false, defaultValue = "admin") String createdBy) {
        BatchOperationResponse response = warehouseService.returnAllStockFromSalesman(salesmanAlias, createdBy);
        return ResponseEntity.ok(response);
    }
}

