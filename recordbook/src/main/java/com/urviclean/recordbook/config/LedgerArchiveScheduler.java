package com.urviclean.recordbook.config;

import com.urviclean.recordbook.repositories.SalesmanLedgerRepository;
import com.urviclean.recordbook.repositories.WarehouseLedgerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ledger Archive Scheduler
 *
 * Archives old warehouse_ledger entries to warehouse_ledger_archive
 * Runs monthly on the 1st at 2 AM IST and also on application startup.
 *
 * Archive policy: Move entries older than 90 days.
 */
@Component
public class LedgerArchiveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LedgerArchiveScheduler.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WarehouseLedgerRepository warehouseLedgerRepository;

    @Autowired
    private SalesmanLedgerRepository salesmanLedgerRepository;

    /**
     * Run on application startup
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Application started. Running ledger archive check...");
        archiveOldLedgers();
    }

    /**
     * Monthly archive job - runs on 1st of every month at 2 AM IST
     * Cron: "0 0 2 1 * *" means minute 0, hour 2, day of month 1, any month, any day of week
     * Zone: Asia/Kolkata (IST)
     */
    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Kolkata")
    public void monthlyArchive() {
        logger.info("Monthly ledger archive job triggered (1st of month at 2 AM IST)");
        archiveOldLedgers();
    }

    /**
     * Archive warehouse_ledger entries older than 90 days
     */
    @Transactional
    public void archiveOldLedgers() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);

            logger.info("Starting ledger archival process. Cutoff date: {}", cutoffDate);

            // Ensure archive tables exist
            ensureArchiveTablesExist();

            // Archive warehouse_ledger entries older than 90 days
            String archiveWarehouseLedgerSql =
                "INSERT INTO warehouse_ledger_archive SELECT * FROM warehouse_ledger WHERE created_at < ?";

            int archivedWarehouseLedger = jdbcTemplate.update(archiveWarehouseLedgerSql, cutoffDate);
            logger.info("Archived {} warehouse_ledger entries", archivedWarehouseLedger);

            // Delete archived warehouse_ledger entries
            String deleteWarehouseLedgerSql =
                "DELETE FROM warehouse_ledger WHERE created_at < ?";

            int deletedWarehouseLedger = jdbcTemplate.update(deleteWarehouseLedgerSql, cutoffDate);
            logger.info("Deleted {} warehouse_ledger entries", deletedWarehouseLedger);

            // Archive salesman_ledger entries older than 90 days (optional - keeping full history is also fine)
            String archiveSalesmanLedgerSql =
                "INSERT INTO salesman_ledger_archive SELECT * FROM salesman_ledger WHERE created_at < ?";

            try {
                int archivedSalesmanLedger = jdbcTemplate.update(archiveSalesmanLedgerSql, cutoffDate);
                logger.info("Archived {} salesman_ledger entries", archivedSalesmanLedger);
            } catch (Exception e) {
                logger.warn("salesman_ledger_archive table not yet created. Skipping salesman_ledger archival. Error: {}", e.getMessage());
            }

            logger.info("Ledger archival completed successfully");

        } catch (Exception e) {
            logger.error("Error during ledger archival process", e);
        }
    }

    /**
     * Ensure archive tables exist before attempting to archive
     */
    private void ensureArchiveTablesExist() {
        try {
            // Create warehouse_ledger_archive if it doesn't exist
            String createWarehouseArchive =
                "CREATE TABLE IF NOT EXISTS warehouse_ledger_archive LIKE warehouse_ledger";
            jdbcTemplate.execute(createWarehouseArchive);
            logger.info("Ensured warehouse_ledger_archive table exists");

            // Add index if not exists
            try {
                String createIndex =
                    "CREATE INDEX IF NOT EXISTS idx_wla_created_at ON warehouse_ledger_archive(created_at)";
                jdbcTemplate.execute(createIndex);
            } catch (Exception e) {
                // Index might already exist, ignore
                logger.debug("Index creation skipped or already exists: {}", e.getMessage());
            }

            // Create salesman_ledger_archive if it doesn't exist
            try {
                String createSalesmanArchive =
                    "CREATE TABLE IF NOT EXISTS salesman_ledger_archive LIKE salesman_ledger";
                jdbcTemplate.execute(createSalesmanArchive);
                logger.info("Ensured salesman_ledger_archive table exists");

                // Add index if not exists
                try {
                    String createSalesmanIndex =
                        "CREATE INDEX IF NOT EXISTS idx_sla_created_at ON salesman_ledger_archive(created_at)";
                    jdbcTemplate.execute(createSalesmanIndex);
                } catch (Exception e) {
                    // Index might already exist, ignore
                    logger.debug("Salesman archive index creation skipped or already exists: {}", e.getMessage());
                }
            } catch (Exception e) {
                logger.warn("Could not create salesman_ledger_archive: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.error("Error ensuring archive tables exist", e);
            throw e;
        }
    }
}

