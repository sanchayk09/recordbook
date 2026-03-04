package com.urviclean.recordbook.services;

import com.urviclean.recordbook.exception.ResourceNotFoundException;
import com.urviclean.recordbook.exception.InvalidInputException;
import com.urviclean.recordbook.models.*;
import com.urviclean.recordbook.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Warehouse Service - Handles all warehouse inventory and ledger operations
 * Design: Uses product_cost_manual.product_code as master product key
 */
@Service
public class WarehouseService {

    @Autowired
    private WarehouseInventoryRepository warehouseInventoryRepository;

    @Autowired
    private WarehouseLedgerRepository warehouseLedgerRepository;

    @Autowired
    private SalesmanLedgerRepository salesmanLedgerRepository;

    @Autowired
    private ProductCostManualRepository productCostManualRepository;

    @Autowired
    private SalesmanRepository salesmanRepository;

    @Autowired
    private SalesmanStockSummaryRepository salesmanStockSummaryRepository;


    /**
     * Get all warehouse inventory with product details
     */
    public List<WarehouseInventoryResponse> getAllInventory() {
        List<WarehouseInventory> inventories = warehouseInventoryRepository.findAll();

        return inventories.stream().map(inv -> {
            WarehouseInventoryResponse response = new WarehouseInventoryResponse();
            response.setWarehouseInventoryId(inv.getWarehouseInventoryId());
            response.setProductCode(inv.getProductCode());
            response.setQtyAvailable(inv.getQtyAvailable());
            response.setLastUpdated(inv.getLastUpdated());

            // Enrich with product name, variant, and calculate volume
            productCostManualRepository.findByProductCode(inv.getProductCode())
                .ifPresent(product -> {
                    response.setProductName(product.getProductName());
                    response.setVariant(product.getVariant());
                    response.setMetric(product.getMetric());
                    response.setMetricQuantity(product.getMetricQuantity());

                    // Calculate total volume if metric is liquid
                    if ("lit".equals(product.getMetric()) && product.getMetricQuantity() != null && inv.getQtyAvailable() != null) {
                        java.math.BigDecimal totalVolume = product.getMetricQuantity()
                            .multiply(java.math.BigDecimal.valueOf(inv.getQtyAvailable()))
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                        response.setTotalVolume(totalVolume);
                    }
                });

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * Get inventory for specific product
     */
    public WarehouseInventoryResponse getInventoryByProductCode(String productCode) {
        WarehouseInventory inventory = warehouseInventoryRepository.findByProductCode(productCode)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + productCode));

        WarehouseInventoryResponse response = new WarehouseInventoryResponse();
        response.setWarehouseInventoryId(inventory.getWarehouseInventoryId());
        response.setProductCode(inventory.getProductCode());
        response.setQtyAvailable(inventory.getQtyAvailable());
        response.setLastUpdated(inventory.getLastUpdated());

        // Enrich with product name, variant, and calculate volume
        productCostManualRepository.findByProductCode(productCode)
            .ifPresent(product -> {
                response.setProductName(product.getProductName());
                response.setVariant(product.getVariant());
                response.setMetric(product.getMetric());
                response.setMetricQuantity(product.getMetricQuantity());

                // Calculate total volume if metric is liquid
                if ("lit".equals(product.getMetric()) && product.getMetricQuantity() != null && inventory.getQtyAvailable() != null) {
                    java.math.BigDecimal totalVolume = product.getMetricQuantity()
                        .multiply(java.math.BigDecimal.valueOf(inventory.getQtyAvailable()))
                        .setScale(2, java.math.RoundingMode.HALF_UP);
                    response.setTotalVolume(totalVolume);
                }
            });

        return response;
    }

    /**
     * Issue stock to salesman - FOUR writes in one transaction
     * 1. Decrement warehouse_inventory
     * 2. Insert warehouse_ledger (ISSUE_TO_SALESMAN, -qty)
     * 3. Increment salesman_stock_summary
     * 4. Insert salesman_ledger (ISSUE_FROM_WAREHOUSE, +qty)
     */
    @Transactional
    public WarehouseLedger issueStockToSalesman(IssueStockRequest request) {
        // Validate product exists
        if (!productCostManualRepository.existsByProductCode(request.getProductCode())) {
            throw new ResourceNotFoundException("Product not found: " + request.getProductCode());
        }

        // Validate salesman exists
        if (!salesmanRepository.existsByAlias(request.getSalesmanAlias())) {
            throw new ResourceNotFoundException("Salesman not found: " + request.getSalesmanAlias());
        }

        // Get or create inventory
        WarehouseInventory inventory = warehouseInventoryRepository
            .findByProductCode(request.getProductCode())
            .orElseGet(() -> {
                WarehouseInventory newInv = new WarehouseInventory();
                newInv.setProductCode(request.getProductCode());
                newInv.setQtyAvailable(0);
                return warehouseInventoryRepository.save(newInv);
            });

        int qtyBefore = inventory.getQtyAvailable();
        int qtyAfter = qtyBefore - request.getQuantity();

        // Validation: Check for sufficient warehouse stock
        if (qtyAfter < 0) {
            throw new InvalidInputException(
                "Insufficient stock in warehouse. Available: " + qtyBefore + ", Requested: " + request.getQuantity(),
                "INSUFFICIENT_WAREHOUSE_STOCK"
            );
        }

        // WRITE 1: Update warehouse_inventory (decrement)
        inventory.setQtyAvailable(qtyAfter);
        warehouseInventoryRepository.save(inventory);

        // WRITE 2: Create warehouse_ledger entry (ISSUE_TO_SALESMAN, delta=-qty)
        WarehouseLedger warehouseLedger = new WarehouseLedger();
        warehouseLedger.setProductCode(request.getProductCode());
        warehouseLedger.setTxnType(WarehouseLedger.TransactionType.ISSUE_TO_SALESMAN);
        warehouseLedger.setDeltaQty(-request.getQuantity());
        warehouseLedger.setQtyBefore(qtyBefore);
        warehouseLedger.setQtyAfter(qtyAfter);
        warehouseLedger.setSalesmanAlias(request.getSalesmanAlias());
        String autoRemarks = "Issue stock to salesman on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        warehouseLedger.setRemarks(autoRemarks);
        warehouseLedger.setCreatedBy(request.getCreatedBy());
        WarehouseLedger savedWarehouseLedger = warehouseLedgerRepository.save(warehouseLedger);

        // WRITE 3: Update salesman_stock_summary (increment - salesman now holds this stock)
        SalesmanStockSummary salesmanSummary = salesmanStockSummaryRepository
            .findBySalesmanAliasAndProductCode(request.getSalesmanAlias(), request.getProductCode())
            .orElse(new SalesmanStockSummary());

        int salesmanStockBefore = salesmanSummary.getCurrentStock() != null ? salesmanSummary.getCurrentStock() : 0;
        int salesmanStockAfter = salesmanStockBefore + request.getQuantity();

        salesmanSummary.setSalesmanAlias(request.getSalesmanAlias());
        salesmanSummary.setProductCode(request.getProductCode());
        salesmanSummary.setCurrentStock(salesmanStockAfter);
        salesmanSummary.setLastUpdated(LocalDateTime.now());
        salesmanStockSummaryRepository.save(salesmanSummary);

        // WRITE 4: Create salesman_ledger entry (ISSUE_FROM_WAREHOUSE, delta=+qty)
        SalesmanLedger salesmanLedger = new SalesmanLedger(
            request.getSalesmanAlias(),
            request.getProductCode(),
            SalesmanTxnType.ISSUE_FROM_WAREHOUSE,
            request.getQuantity(),  // positive = added to salesman stock
            "Issued from warehouse. Salesman stock before: " + salesmanStockBefore + " after: " + salesmanStockAfter,
            request.getCreatedBy()
        );
        salesmanLedgerRepository.save(salesmanLedger);

        return savedWarehouseLedger;
    }

    /**
     * Return stock from salesman - FOUR writes in one transaction
     * 1. Decrement salesman_stock_summary
     * 2. Insert salesman_ledger (RETURN_TO_WAREHOUSE, -qty)
     * 3. Increment warehouse_inventory
     * 4. Insert warehouse_ledger (RETURN_FROM_SALESMAN, +qty)
     */
    @Transactional
    public WarehouseLedger returnStockFromSalesman(ReturnStockRequest request) {
        // Validate product exists
        if (!productCostManualRepository.existsByProductCode(request.getProductCode())) {
            throw new ResourceNotFoundException("Product not found: " + request.getProductCode());
        }

        // Validate salesman exists
        if (!salesmanRepository.existsByAlias(request.getSalesmanAlias())) {
            throw new ResourceNotFoundException("Salesman not found: " + request.getSalesmanAlias());
        }

        // VALIDATION 1: Check salesman has enough stock to return
        SalesmanStockSummary salesmanSummary = salesmanStockSummaryRepository
            .findBySalesmanAliasAndProductCode(request.getSalesmanAlias(), request.getProductCode())
            .orElse(new SalesmanStockSummary());

        int salesmanStockBefore = salesmanSummary.getCurrentStock() != null ? salesmanSummary.getCurrentStock() : 0;
        int salesmanStockAfter = salesmanStockBefore - request.getQuantity();

        if (salesmanStockAfter < 0) {
            throw new InvalidInputException(
                "Insufficient stock with salesman. Available: " + salesmanStockBefore + ", Requested: " + request.getQuantity(),
                "INSUFFICIENT_SALESMAN_STOCK"
            );
        }

        // WRITE 1: Decrement salesman_stock_summary (return reduces salesman stock)
        salesmanSummary.setSalesmanAlias(request.getSalesmanAlias());
        salesmanSummary.setProductCode(request.getProductCode());
        salesmanSummary.setCurrentStock(salesmanStockAfter);
        salesmanSummary.setLastUpdated(LocalDateTime.now());
        salesmanStockSummaryRepository.save(salesmanSummary);

        // WRITE 2: Create salesman_ledger entry (RETURN_TO_WAREHOUSE, delta=-qty)
        SalesmanLedger salesmanLedger = new SalesmanLedger(
            request.getSalesmanAlias(),
            request.getProductCode(),
            SalesmanTxnType.RETURN_TO_WAREHOUSE,
            -request.getQuantity(),  // negative = removed from salesman stock
            "Returned to warehouse. Salesman stock before: " + salesmanStockBefore + " after: " + salesmanStockAfter,
            request.getCreatedBy()
        );
        salesmanLedgerRepository.save(salesmanLedger);

        // Get warehouse inventory
        WarehouseInventory inventory = warehouseInventoryRepository
            .findByProductCode(request.getProductCode())
            .orElseGet(() -> {
                WarehouseInventory newInv = new WarehouseInventory();
                newInv.setProductCode(request.getProductCode());
                newInv.setQtyAvailable(0);
                return warehouseInventoryRepository.save(newInv);
            });

        int warehouseQtyBefore = inventory.getQtyAvailable();
        int warehouseQtyAfter = warehouseQtyBefore + request.getQuantity();

        // WRITE 3: Increment warehouse_inventory
        inventory.setQtyAvailable(warehouseQtyAfter);
        warehouseInventoryRepository.save(inventory);

        // WRITE 4: Create warehouse_ledger entry (RETURN_FROM_SALESMAN, delta=+qty)
        WarehouseLedger warehouseLedger = new WarehouseLedger();
        warehouseLedger.setProductCode(request.getProductCode());
        warehouseLedger.setTxnType(WarehouseLedger.TransactionType.RETURN_FROM_SALESMAN);
        warehouseLedger.setDeltaQty(request.getQuantity());
        warehouseLedger.setQtyBefore(warehouseQtyBefore);
        warehouseLedger.setQtyAfter(warehouseQtyAfter);
        warehouseLedger.setSalesmanAlias(request.getSalesmanAlias());
        String autoRemarks = "Return stock from salesman on " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        warehouseLedger.setRemarks(autoRemarks);
        warehouseLedger.setCreatedBy(request.getCreatedBy());
        WarehouseLedger savedWarehouseLedger = warehouseLedgerRepository.save(warehouseLedger);

        return savedWarehouseLedger;
    }

    /**
     * Adjust stock (TRANSFER_IN, MANUAL_ADJUST, DAMAGE)
     */
    @Transactional
    public WarehouseLedger adjustStock(AdjustStockRequest request) {
        // Validate product exists
        if (!productCostManualRepository.existsByProductCode(request.getProductCode())) {
            throw new ResourceNotFoundException("Product not found: " + request.getProductCode());
        }

        // Validate transaction type
        WarehouseLedger.TransactionType txnType;
        try {
            txnType = WarehouseLedger.TransactionType.valueOf(request.getTxnType());
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Invalid transaction type: " + request.getTxnType(), "INVALID_TXN_TYPE");
        }

        // Only allow specific types for adjust
        if (txnType == WarehouseLedger.TransactionType.ISSUE_TO_SALESMAN ||
            txnType == WarehouseLedger.TransactionType.RETURN_FROM_SALESMAN) {
            throw new InvalidInputException(
                "Use specific endpoints for ISSUE/RETURN operations",
                "INVALID_OPERATION"
            );
        }

        // Get or create inventory
        WarehouseInventory inventory = warehouseInventoryRepository
            .findByProductCode(request.getProductCode())
            .orElseGet(() -> {
                WarehouseInventory newInv = new WarehouseInventory();
                newInv.setProductCode(request.getProductCode());
                newInv.setQtyAvailable(0);
                return warehouseInventoryRepository.save(newInv);
            });

        int qtyBefore = inventory.getQtyAvailable();
        int deltaQty = request.getQuantity();

        // For DAMAGE, quantity should be negative
        if (txnType == WarehouseLedger.TransactionType.DAMAGE && deltaQty > 0) {
            deltaQty = -deltaQty;
        }

        int qtyAfter = qtyBefore + deltaQty;

        // Check for negative stock
        if (qtyAfter < 0) {
            throw new InvalidInputException(
                "Resulting stock cannot be negative. Current: " + qtyBefore + ", Change: " + deltaQty,
                "NEGATIVE_STOCK"
            );
        }

        // Update inventory
        inventory.setQtyAvailable(qtyAfter);
        warehouseInventoryRepository.save(inventory);

        // Create ledger entry
        WarehouseLedger ledger = new WarehouseLedger();
        ledger.setProductCode(request.getProductCode());
        ledger.setTxnType(txnType);
        ledger.setDeltaQty(deltaQty);
        ledger.setQtyBefore(qtyBefore);
        ledger.setQtyAfter(qtyAfter);
        ledger.setRemarks(request.getRemarks());
        ledger.setCreatedBy(request.getCreatedBy());

        return warehouseLedgerRepository.save(ledger);
    }

    /**
     * Get ledger history for a product
     */
    public List<WarehouseLedger> getLedgerByProduct(String productCode) {
        return warehouseLedgerRepository.findByProductCodeOrderByCreatedAtDesc(productCode);
    }

    /**
     * Get ledger history for a salesman
     */
    public List<WarehouseLedger> getLedgerBySalesman(String salesmanAlias) {
        return warehouseLedgerRepository.findBySalesmanAliasOrderByCreatedAtDesc(salesmanAlias);
    }

    /**
     * Get all ledger entries
     */
    public List<WarehouseLedger> getAllLedgerEntries() {
        return warehouseLedgerRepository.findAll();
    }

    /**
     * Get stock with salesman (issued - returned)
     */
    public Long getStockWithSalesman(String salesmanAlias, String productCode) {
        Long stock = warehouseLedgerRepository.getStockWithSalesman(salesmanAlias, productCode);
        return stock != null ? stock : 0L;
    }

    /**
     * Get total issued to salesman
     */
    public Long getTotalIssuedToSalesman(String salesmanAlias) {
        Long total = warehouseLedgerRepository.getTotalIssuedToSalesman(salesmanAlias);
        return total != null ? Math.abs(total) : 0L;
    }

    /**
     * Get total returned from salesman
     */
    public Long getTotalReturnedFromSalesman(String salesmanAlias) {
        Long total = warehouseLedgerRepository.getTotalReturnedFromSalesman(salesmanAlias);
        return total != null ? total : 0L;
    }

    /**
     * Get all salesmen with their current stock from salesman_stock_summary table
     * This method reads the cached stock data that should be populated by database triggers
     * FIRST TRY THIS - it's much faster than computing from ledger
     */
    public List<SalesmanStockDTO> getAllSalesmenWithStockFromSummary() {
        try {
            // Get all stock summary records WHERE current_stock > 0 (optimized query)
            List<SalesmanStockSummary> allSummary = salesmanStockSummaryRepository.findByCurrentStockGreaterThan(0);

            if (allSummary == null || allSummary.isEmpty()) {
                System.out.println("INFO: salesman_stock_summary table has no records with stock > 0.");
                System.out.println("This is normal if no stock has been issued yet.");
                return new java.util.ArrayList<>(); // Return empty list, not fallback
            }

            System.out.println("INFO: Found " + allSummary.size() + " stock records in salesman_stock_summary");

            // Group by salesman alias
            java.util.Map<String, SalesmanStockDTO> salesmenMap = new java.util.HashMap<>();

            for (SalesmanStockSummary summary : allSummary) {
                String alias = summary.getSalesmanAlias();
                SalesmanStockDTO dto = salesmenMap.getOrDefault(alias, new SalesmanStockDTO());

                if (dto.getSalesmanAlias() == null) {
                    dto.setSalesmanAlias(alias);
                    dto.setProducts(new java.util.ArrayList<>());

                    // Get salesman details
                    salesmanRepository.findByAliasIgnoreCase(alias).ifPresent(s -> {
                        dto.setFirstName(s.getFirstName());
                        dto.setLastName(s.getLastName());
                    });
                }

                // Add product stock
                ProductStock ps = new ProductStock();
                ps.setProductCode(summary.getProductCode());
                ps.setQuantity(summary.getCurrentStock());

                // Enrich with product details
                productCostManualRepository.findByProductCode(summary.getProductCode()).ifPresent(p -> {
                    ps.setProductName(p.getProductName());
                    ps.setVariant(p.getVariant());
                    ps.setMetric(p.getMetric());
                    ps.setMetricQuantity(p.getMetricQuantity());

                    // Calculate total volume if metric is liquid
                    if ("lit".equals(p.getMetric()) && p.getMetricQuantity() != null) {
                        java.math.BigDecimal totalVolume = p.getMetricQuantity()
                            .multiply(java.math.BigDecimal.valueOf(summary.getCurrentStock()))
                            .setScale(2, java.math.RoundingMode.HALF_UP);
                        ps.setTotalVolume(totalVolume);
                    }
                });

                dto.getProducts().add(ps);
                salesmenMap.put(alias, dto);
            }

            // Calculate totals and convert to list
            List<SalesmanStockDTO> result = salesmenMap.values().stream()
                .peek(dto -> {
                    dto.setTotalQuantity(dto.getProducts().stream().mapToInt(ProductStock::getQuantity).sum());
                    dto.setTotalProducts(dto.getProducts().size());
                })
                .filter(dto -> dto.getTotalQuantity() > 0)
                .sorted((a, b) -> a.getSalesmanAlias().compareToIgnoreCase(b.getSalesmanAlias()))
                .collect(Collectors.toList());

            System.out.println("INFO: Returning " + result.size() + " salesmen with stock");
            return result;

        } catch (Exception e) {
            System.err.println("ERROR reading from salesman_stock_summary: " + e.getMessage());
            e.printStackTrace();
            // Return empty list on error - don't fallback to ledger
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Get all salesmen with their current stock details (product-wise breakdown)
     * FALLBACK: Calculates directly from warehouse_ledger table
     * Use only if salesman_stock_summary is not populated
     */
    public List<SalesmanStockDTO> getAllSalesmenWithStockFromLedger() {
        // Get all salesmen
        List<Salesman> allSalesmen = salesmanRepository.findAll();

        return allSalesmen.stream()
            .map(salesman -> {
                // Get all unique products for this salesman from ledger
                List<String> productCodes = warehouseLedgerRepository
                    .findBySalesmanAliasOrderByCreatedAtDesc(salesman.getAlias())
                    .stream()
                    .map(WarehouseLedger::getProductCode)
                    .distinct()
                    .collect(Collectors.toList());

                // Calculate stock for each product
                List<ProductStock> productStocks = productCodes.stream()
                    .map(productCode -> {
                        long quantity = getStockWithSalesman(salesman.getAlias(), productCode);

                        if (quantity <= 0) {
                            return null; // Skip if no stock
                        }

                        ProductStock ps = new ProductStock();
                        ps.setProductCode(productCode);
                        ps.setQuantity((int) quantity);

                        // Enrich with product name, variant, and calculate volume
                        productCostManualRepository.findByProductCode(productCode)
                            .ifPresent(product -> {
                                ps.setProductName(product.getProductName());
                                ps.setVariant(product.getVariant());
                                ps.setMetric(product.getMetric());
                                ps.setMetricQuantity(product.getMetricQuantity());

                                // Calculate total volume if metric is liquid
                                if ("lit".equals(product.getMetric()) && product.getMetricQuantity() != null) {
                                    java.math.BigDecimal totalVolume = product.getMetricQuantity()
                                        .multiply(java.math.BigDecimal.valueOf(quantity))
                                        .setScale(2, java.math.RoundingMode.HALF_UP);
                                    ps.setTotalVolume(totalVolume);
                                }
                            });

                        return ps;
                    })
                    .filter(ps -> ps != null)
                    .sorted((a, b) -> a.getProductCode().compareTo(b.getProductCode()))
                    .collect(Collectors.toList());

                // Create summary DTO
                SalesmanStockDTO summary = new SalesmanStockDTO();
                summary.setSalesmanAlias(salesman.getAlias());
                summary.setFirstName(salesman.getFirstName());
                summary.setLastName(salesman.getLastName());
                summary.setProducts(productStocks);
                summary.setTotalProducts(productStocks.size());
                summary.setTotalQuantity(productStocks.stream().mapToInt(ProductStock::getQuantity).sum());

                return summary;
            })
            .filter(summary -> summary.getTotalQuantity() > 0) // Only include salesmen with stock
            .sorted((a, b) -> a.getSalesmanAlias().compareToIgnoreCase(b.getSalesmanAlias()))
            .collect(Collectors.toList());
    }

    /**
     * PUBLIC API METHOD: Get all salesmen with their current stock
     * Reads from salesman_stock_summary table which is populated by triggers
     * This is MUCH faster than calculating from ledger
     */
    public List<SalesmanStockDTO> getAllSalesmenWithStock() {
        System.out.println("=== getAllSalesmenWithStock() called ===");
        // Use summary table - populated by database triggers
        return getAllSalesmenWithStockFromSummary();
    }

    /**
     * Batch issue stock to salesman
     * Handles multiple products in a single transaction with proper validation
     */
    @Transactional
    public BatchOperationResponse batchIssueStock(BatchIssueRequest request) {
        BatchOperationResponse response = new BatchOperationResponse();
        response.setTotalItems(request.getItems().size());
        response.setSuccessCount(0);
        response.setFailureCount(0);
        response.setErrors(new java.util.ArrayList<>());

        // Validate salesman exists
        if (!salesmanRepository.existsByAlias(request.getSalesmanAlias())) {
            throw new ResourceNotFoundException("Salesman not found: " + request.getSalesmanAlias());
        }

        int successCount = 0;
        for (BatchIssueRequest.IssueItem item : request.getItems()) {
            try {
                IssueStockRequest issueRequest = new IssueStockRequest();
                issueRequest.setProductCode(item.getProductCode());
                issueRequest.setSalesmanAlias(request.getSalesmanAlias());
                issueRequest.setQuantity(item.getQuantity());
                issueRequest.setCreatedBy(request.getCreatedBy());

                issueStockToSalesman(issueRequest);
                successCount++;
            } catch (Exception e) {
                response.getErrors().add("Failed to issue " + item.getProductCode() + ": " + e.getMessage());
                response.setFailureCount(response.getFailureCount() + 1);
            }
        }

        response.setSuccessCount(successCount);
        response.setMessage(successCount + "/" + request.getItems().size() + " items issued successfully");
        return response;
    }

    /**
     * Batch return stock from salesman
     * Handles multiple products in a single transaction with proper validation
     */
    @Transactional
    public BatchOperationResponse batchReturnStock(BatchReturnRequest request) {
        BatchOperationResponse response = new BatchOperationResponse();
        response.setTotalItems(request.getItems().size());
        response.setSuccessCount(0);
        response.setFailureCount(0);
        response.setErrors(new java.util.ArrayList<>());

        // Validate salesman exists
        if (!salesmanRepository.existsByAlias(request.getSalesmanAlias())) {
            throw new ResourceNotFoundException("Salesman not found: " + request.getSalesmanAlias());
        }

        int successCount = 0;
        for (BatchReturnRequest.ReturnItem item : request.getItems()) {
            try {
                ReturnStockRequest returnRequest = new ReturnStockRequest();
                returnRequest.setProductCode(item.getProductCode());
                returnRequest.setSalesmanAlias(request.getSalesmanAlias());
                returnRequest.setQuantity(item.getQuantity());
                returnRequest.setCreatedBy(request.getCreatedBy());

                returnStockFromSalesman(returnRequest);
                successCount++;
            } catch (Exception e) {
                response.getErrors().add("Failed to return " + item.getProductCode() + ": " + e.getMessage());
                response.setFailureCount(response.getFailureCount() + 1);
            }
        }

        response.setSuccessCount(successCount);
        response.setMessage(successCount + "/" + request.getItems().size() + " items returned successfully");
        return response;
    }

    /**
     * Return all stock from a salesman
     * Convenience method that gets current stock and returns everything
     */
    @Transactional
    public BatchOperationResponse returnAllStockFromSalesman(String salesmanAlias, String createdBy) {
        // Get current stock for this salesman
        List<SalesmanStockDTO> salesmenStock = getAllSalesmenWithStock();
        SalesmanStockDTO salesmanData = salesmenStock.stream()
            .filter(s -> s.getSalesmanAlias().equals(salesmanAlias))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("No stock found for salesman: " + salesmanAlias));

        // Create batch return request
        BatchReturnRequest batchRequest = new BatchReturnRequest();
        batchRequest.setSalesmanAlias(salesmanAlias);
        batchRequest.setCreatedBy(createdBy);

        List<BatchReturnRequest.ReturnItem> items = new java.util.ArrayList<>();
        for (ProductStock product : salesmanData.getProducts()) {
            BatchReturnRequest.ReturnItem item = new BatchReturnRequest.ReturnItem();
            item.setProductCode(product.getProductCode());
            item.setQuantity(product.getQuantity());
            items.add(item);
        }
        batchRequest.setItems(items);

        return batchReturnStock(batchRequest);
    }}
