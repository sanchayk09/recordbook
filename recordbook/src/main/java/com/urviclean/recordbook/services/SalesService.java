package com.urviclean.recordbook.services;

import com.urviclean.recordbook.exception.BusinessLogicException;
import com.urviclean.recordbook.exception.InvalidInputException;
import com.urviclean.recordbook.exception.ResourceNotFoundException;
import com.urviclean.recordbook.models.DailySaleRecord;
import com.urviclean.recordbook.models.SalesmanLedger;
import com.urviclean.recordbook.models.SalesmanTxnType;
import com.urviclean.recordbook.repositories.DailySaleRecordRepository;
import com.urviclean.recordbook.repositories.SalesmanLedgerRepository;
import com.urviclean.recordbook.repositories.SalesmanStockSummaryRepository;
import com.urviclean.recordbook.utils.CommissionCalculator;
import com.urviclean.recordbook.utils.VolumeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Service responsible for sale lifecycle operations (create, update, void/delete).
 *
 * <p>All mutating methods are {@code @Transactional} so that sale persistence,
 * salesman_stock_summary updates, and salesman_ledger writes are always atomic.
 * Stock enforcement uses single-statement conditional UPDATE queries to prevent
 * race conditions between concurrent requests.</p>
 */
@Service
public class SalesService {

    @Autowired
    private DailySaleRecordRepository dailySaleRecordRepository;

    @Autowired
    private SalesmanStockSummaryRepository salesmanStockSummaryRepository;

    @Autowired
    private SalesmanLedgerRepository salesmanLedgerRepository;

    @Autowired
    @Lazy
    private DailySummaryService dailySummaryService;

    /**
     * Create a new sale record.
     *
     * <ol>
     *   <li>Validates that {@code salesmanName}, {@code productCode} and {@code quantity} are present.</li>
     *   <li>Persists the {@link DailySaleRecord}.</li>
     *   <li>Atomically decrements {@code salesman_stock_summary.current_stock} by {@code qty}.
     *       Throws {@link InvalidInputException} if stock is insufficient (affected rows == 0).</li>
     *   <li>Inserts a {@code SOLD} entry into {@code salesman_ledger}.</li>
     * </ol>
     *
     * @param record the sale record to persist
     * @return the saved {@link DailySaleRecord} with its generated ID
     * @throws InvalidInputException if required fields are missing or salesman stock is insufficient
     */
    @Transactional(rollbackFor = Exception.class)
    public DailySaleRecord createSale(DailySaleRecord record) {
        String alias = record.getSalesmanName();
        String product = record.getProductCode();
        Integer qty = record.getQuantity();

        if (alias == null || alias.isBlank()) {
            throw new InvalidInputException("salesmanName", "must not be blank");
        }
        if (product == null || product.isBlank()) {
            throw new InvalidInputException("productCode", "must not be blank");
        }
        if (qty == null || qty <= 0) {
            throw new InvalidInputException("quantity", "must be a positive integer");
        }

        // Normalise for consistent DB lookups
        alias = alias.trim();
        product = product.trim();
        record.setSalesmanName(alias);
        record.setProductCode(product);

        // Calculate derived fields if not already set
        if (record.getVolumeSold() == null) {
            record.setVolumeSold(VolumeCalculator.calculateVolumeSold(product, qty));
        }
        if (record.getRevenue() == null && record.getRate() != null) {
            record.setRevenue(record.getRate().multiply(BigDecimal.valueOf(qty)));
        }
        if (record.getAgentCommission() == null && record.getRate() != null) {
            record.setAgentCommission(CommissionCalculator.calculateCommission(product, record.getRate(), qty));
        }

        DailySaleRecord savedRecord = dailySaleRecordRepository.save(record);

        // Atomic stock decrement – single-statement conditional UPDATE prevents race conditions
        int affected = salesmanStockSummaryRepository.decrementStockIfSufficient(alias, product, qty);
        if (affected == 0) {
            int currentStock = salesmanStockSummaryRepository
                    .findBySalesmanAliasAndProductCode(alias, product)
                    .map(s -> s.getCurrentStock() != null ? s.getCurrentStock() : 0)
                    .orElse(0);
            throw new InvalidInputException(
                    "Insufficient stock with salesman. Available: " + currentStock + ", Trying to sell: " + qty,
                    "INSUFFICIENT_SALESMAN_STOCK"
            );
        }

        salesmanLedgerRepository.save(new SalesmanLedger(
                alias, product, SalesmanTxnType.SOLD,
                -qty,
                "Sale created, saleId=" + savedRecord.getId(),
                "system"
        ));

        // Recalculate daily summary for this salesman/date after the new sale
        if (savedRecord.getSaleDate() != null) {
            dailySummaryService.computeAndPersistSummary(alias, savedRecord.getSaleDate());
        }

        return savedRecord;
    }

    /**
     * Update an existing sale record and reconcile salesman stock accordingly.
     *
     * <p>If {@code salesmanName}, {@code productCode} or {@code quantity} changed, the method:
     * <ol>
     *   <li>Atomically restores the old salesman's stock by the old quantity.</li>
     *   <li>Writes a reversal {@code SOLD} ledger entry (positive delta).</li>
     *   <li>Atomically decrements the new salesman's stock by the new quantity.
     *       Throws {@link InvalidInputException} if insufficient (rolls back the whole transaction,
     *       including the stock restore above).</li>
     *   <li>Writes a new {@code SOLD} ledger entry (negative delta).</li>
     * </ol>
     * </p>
     *
     * @param saleId     ID of the sale to update
     * @param newDetails partial or full replacement values
     * @return the updated {@link DailySaleRecord}
     * @throws ResourceNotFoundException if the sale does not exist
     * @throws InvalidInputException     if the updated sale would exceed available salesman stock
     */
    @Transactional(rollbackFor = Exception.class)
    public DailySaleRecord updateSale(long saleId, DailySaleRecord newDetails) {
        DailySaleRecord record = dailySaleRecordRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("DailySaleRecord", "id", saleId));

        // Capture old stock-affecting values before applying updates
        String oldAlias = record.getSalesmanName();
        String oldProduct = record.getProductCode();
        Integer oldQty = record.getQuantity();
        LocalDate oldSaleDate = record.getSaleDate();

        // Apply field-level updates (mirrors the existing controller logic)
        if (newDetails.getSlNo() != null) {
            record.setSlNo(newDetails.getSlNo());
        }
        if (newDetails.getSaleDate() != null) {
            record.setSaleDate(newDetails.getSaleDate());
        }
        if (newDetails.getSalesmanName() != null && !newDetails.getSalesmanName().isBlank()) {
            record.setSalesmanName(newDetails.getSalesmanName().trim());
        }
        if (newDetails.getCustomerName() != null && !newDetails.getCustomerName().isBlank()) {
            record.setCustomerName(newDetails.getCustomerName().trim());
        }
        if (newDetails.getCustomerType() != null) {
            record.setCustomerType(newDetails.getCustomerType());
        }
        if (newDetails.getVillage() != null) {
            record.setVillage(newDetails.getVillage());
        }
        if (newDetails.getMobileNumber() != null) {
            record.setMobileNumber(newDetails.getMobileNumber());
        }
        if (newDetails.getProductCode() != null && !newDetails.getProductCode().isBlank()) {
            record.setProductCode(newDetails.getProductCode().trim());
        }
        if (newDetails.getQuantity() != null) {
            if (newDetails.getQuantity() <= 0) {
                throw new InvalidInputException("quantity", "must be a positive integer");
            }
            record.setQuantity(newDetails.getQuantity());
        }
        if (newDetails.getRate() != null) {
            record.setRate(newDetails.getRate());
        }

        // Recalculate derived fields
        if (record.getQuantity() != null && record.getRate() != null) {
            record.setRevenue(record.getRate().multiply(BigDecimal.valueOf(record.getQuantity())));
        }
        if (record.getProductCode() != null && record.getRate() != null && record.getQuantity() != null) {
            record.setAgentCommission(CommissionCalculator.calculateCommission(
                    record.getProductCode(), record.getRate(), record.getQuantity()));
        }
        if (record.getProductCode() != null && record.getQuantity() != null) {
            record.setVolumeSold(VolumeCalculator.calculateVolumeSold(record.getProductCode(), record.getQuantity()));
        }

        String newAlias = record.getSalesmanName();
        String newProduct = record.getProductCode();
        Integer newQty = record.getQuantity();

        // Reconcile stock only when stock-affecting fields changed
        boolean stockChanged = !Objects.equals(newAlias, oldAlias)
                || !Objects.equals(newProduct, oldProduct)
                || !Objects.equals(newQty, oldQty);

        if (stockChanged && oldAlias != null && oldProduct != null && oldQty != null) {
            // Step 1: restore old stock (atomic increment – no stock-floor constraint needed)
            int restored = salesmanStockSummaryRepository.restoreStock(oldAlias, oldProduct, oldQty);
            if (restored == 0) {
                throw new BusinessLogicException(
                        "Cannot update sale: stock summary row not found for salesman='" + oldAlias
                                + "', product='" + oldProduct + "'. Data may be inconsistent.",
                        "STOCK_SUMMARY_NOT_FOUND"
                );
            }

            // Step 2: reversal ledger entry
            salesmanLedgerRepository.save(new SalesmanLedger(
                    oldAlias, oldProduct, SalesmanTxnType.SOLD,
                    +oldQty,
                    "Sale updated (stock reversal), saleId=" + saleId,
                    "system"
            ));

            // Step 3: atomic decrement for the new values – enforces stock floor
            int affected = salesmanStockSummaryRepository.decrementStockIfSufficient(
                    newAlias, newProduct, newQty);
            if (affected == 0) {
                int currentStock = salesmanStockSummaryRepository
                        .findBySalesmanAliasAndProductCode(newAlias, newProduct)
                        .map(s -> s.getCurrentStock() != null ? s.getCurrentStock() : 0)
                        .orElse(0);
                throw new InvalidInputException(
                        "Insufficient stock with salesman. Available: " + currentStock
                                + ", Trying to sell: " + newQty,
                        "INSUFFICIENT_SALESMAN_STOCK"
                );
            }

            // Step 4: new ledger entry
            salesmanLedgerRepository.save(new SalesmanLedger(
                    newAlias, newProduct, SalesmanTxnType.SOLD,
                    -newQty,
                    "Sale updated, saleId=" + saleId,
                    "system"
            ));
        }

        DailySaleRecord updated = dailySaleRecordRepository.save(record);

        // Recalculate summary for the new salesman/date
        if (updated.getSaleDate() != null) {
            dailySummaryService.computeAndPersistSummary(updated.getSalesmanName(), updated.getSaleDate());
        }

        // If salesman or date changed, also recalculate the OLD salesman/date summary
        boolean salesmanOrDateChanged = !Objects.equals(oldAlias, updated.getSalesmanName())
                || !Objects.equals(oldSaleDate, updated.getSaleDate());
        if (salesmanOrDateChanged && oldAlias != null && oldSaleDate != null) {
            dailySummaryService.computeAndPersistSummary(oldAlias, oldSaleDate);
        }

        return updated;
    }

    /**
     * Void (delete) an existing sale record and restore the salesman's stock.
     *
     * <ol>
     *   <li>Restores {@code salesman_stock_summary.current_stock} by the sale quantity.</li>
     *   <li>Writes a positive-delta {@code SOLD} reversal entry in {@code salesman_ledger}.</li>
     *   <li>Deletes the {@link DailySaleRecord}.</li>
     * </ol>
     *
     * @param saleId ID of the sale to void
     * @throws ResourceNotFoundException if the sale does not exist
     */
    @Transactional(rollbackFor = Exception.class)
    public void voidSale(long saleId) {
        DailySaleRecord record = dailySaleRecordRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("DailySaleRecord", "id", saleId));

        String alias = record.getSalesmanName();
        String product = record.getProductCode();
        Integer qty = record.getQuantity();

        if (alias != null && product != null && qty != null) {
            // Restore stock atomically
            int restored = salesmanStockSummaryRepository.restoreStock(alias, product, qty);
            if (restored == 0) {
                // Stock summary row is missing – data is already inconsistent.
                // Write the reversal ledger entry anyway so the audit trail is complete,
                // then delete the sale record to maintain referential consistency.
                System.err.println("WARN: restoreStock affected 0 rows for saleId=" + saleId
                        + " (salesman='" + alias + "', product='" + product
                        + "'). Stock summary row may have been manually deleted.");
            }

            // Reversal ledger entry (written regardless of missing summary row)
            salesmanLedgerRepository.save(new SalesmanLedger(
                    alias, product, SalesmanTxnType.SOLD,
                    +qty,
                    "Sale voided, saleId=" + saleId,
                    "system"
            ));
        }

        LocalDate saleDate = record.getSaleDate();
        dailySaleRecordRepository.delete(record);

        // Recalculate summary after sale deletion (expense remains intact)
        if (alias != null && saleDate != null) {
            dailySummaryService.computeAndPersistSummary(alias, saleDate);
        }
    }

    /**
     * Get the DailySummaryService for computing and persisting summaries
     * @return the DailySummaryService instance
     */
    public DailySummaryService getDailySummaryService() {
        return dailySummaryService;
    }
}
