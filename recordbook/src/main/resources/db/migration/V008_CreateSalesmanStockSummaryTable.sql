-- Migration: Create Salesman Stock Summary Table for Performance Optimization
-- Purpose: Cache salesman stock by product to avoid full ledger scan
-- Date: 2026-03-02

USE urviclean_manual;

-- ============================================================
-- STEP 1: Create salesman_stock_summary table
-- ============================================================

CREATE TABLE IF NOT EXISTS salesman_stock_summary (
    stock_summary_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    salesman_alias VARCHAR(100) NOT NULL COMMENT 'Reference to salesman',
    product_code VARCHAR(20) NOT NULL COMMENT 'Reference to product',
    current_stock INT NOT NULL DEFAULT 0 COMMENT 'Current quantity with salesman',
    total_issued INT NOT NULL DEFAULT 0 COMMENT 'Total issued to this salesman for this product',
    total_returned INT NOT NULL DEFAULT 0 COMMENT 'Total returned from this salesman for this product',
    total_sold INT NOT NULL DEFAULT 0 COMMENT 'Total sold by this salesman for this product',
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last sync from ledger',

    -- Constraints
    UNIQUE KEY uk_salesman_product (salesman_alias, product_code),
    INDEX idx_salesman_alias (salesman_alias),
    INDEX idx_product_code (product_code),
    INDEX idx_last_updated (last_updated),

    CONSTRAINT fk_summary_salesman
        FOREIGN KEY (salesman_alias) REFERENCES salesmen(alias)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_summary_product
        FOREIGN KEY (product_code) REFERENCES product_cost_manual(product_code)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Cached salesman stock summary for performance - synced from warehouse_ledger';

-- ============================================================
-- STEP 2: Create audit table for stock changes
-- ============================================================

CREATE TABLE IF NOT EXISTS salesman_stock_audit (
    audit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    salesman_alias VARCHAR(100) NOT NULL,
    product_code VARCHAR(20) NOT NULL,
    old_stock INT,
    new_stock INT,
    change_amount INT COMMENT 'Positive = increase, Negative = decrease',
    transaction_id BIGINT,
    transaction_type VARCHAR(50),
    synced_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_salesman_audit (salesman_alias),
    INDEX idx_product_audit (product_code),
    INDEX idx_synced_at (synced_at)
) ENGINE=InnoDB COMMENT='Audit log for salesman stock changes';

-- ============================================================
-- STEP 3: Create indexes on warehouse_ledger for fast queries
-- ============================================================

-- Improve query performance for ledger lookups
ALTER TABLE warehouse_ledger ADD INDEX idx_salesman_product (salesman_alias, product_code);
ALTER TABLE warehouse_ledger ADD INDEX idx_salesman_txn (salesman_alias, txn_type);

-- ============================================================
-- STEP 4: Create stored procedure to sync stock
-- ============================================================

DELIMITER $$

CREATE PROCEDURE sync_salesman_stock_summary()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_salesman_alias VARCHAR(100);
    DECLARE v_product_code VARCHAR(20);
    DECLARE v_current_stock INT;
    DECLARE v_total_issued INT;
    DECLARE v_total_returned INT;
    DECLARE v_total_sold INT;

    DECLARE ledger_cursor CURSOR FOR
        SELECT DISTINCT salesman_alias, product_code
        FROM warehouse_ledger
        WHERE salesman_alias IS NOT NULL;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- Start transaction for consistency
    START TRANSACTION;

    OPEN ledger_cursor;
    read_loop: LOOP
        FETCH ledger_cursor INTO v_salesman_alias, v_product_code;
        IF done THEN LEAVE read_loop; END IF;

        -- Calculate totals
        SELECT
            COALESCE(SUM(CASE WHEN txn_type = 'ISSUE_TO_SALESMAN' THEN ABS(delta_qty) ELSE 0 END), 0)
        INTO v_total_issued
        FROM warehouse_ledger
        WHERE salesman_alias = v_salesman_alias AND product_code = v_product_code;

        SELECT
            COALESCE(SUM(CASE WHEN txn_type = 'RETURN_FROM_SALESMAN' THEN delta_qty ELSE 0 END), 0)
        INTO v_total_returned
        FROM warehouse_ledger
        WHERE salesman_alias = v_salesman_alias AND product_code = v_product_code;

        SELECT
            COALESCE(SUM(CASE WHEN txn_type = 'SOLD_BY_SALESMAN' THEN ABS(delta_qty) ELSE 0 END), 0)
        INTO v_total_sold
        FROM warehouse_ledger
        WHERE salesman_alias = v_salesman_alias AND product_code = v_product_code;

        -- Calculate current stock
        SET v_current_stock = v_total_issued - v_total_returned - v_total_sold;

        -- Insert or update summary
        INSERT INTO salesman_stock_summary
            (salesman_alias, product_code, current_stock, total_issued, total_returned, total_sold, last_updated)
        VALUES
            (v_salesman_alias, v_product_code, v_current_stock, v_total_issued, v_total_returned, v_total_sold, NOW())
        ON DUPLICATE KEY UPDATE
            current_stock = v_current_stock,
            total_issued = v_total_issued,
            total_returned = v_total_returned,
            total_sold = v_total_sold,
            last_updated = NOW();
    END LOOP;
    CLOSE ledger_cursor;

    COMMIT;
END$$

DELIMITER ;

-- ============================================================
-- STEP 5: Create trigger to sync on every ledger update
-- ============================================================

DELIMITER $$

CREATE TRIGGER sync_stock_on_ledger_insert
AFTER INSERT ON warehouse_ledger
FOR EACH ROW
BEGIN
    DECLARE v_current_stock INT;

    IF NEW.salesman_alias IS NOT NULL THEN
        -- Calculate current stock for this salesman-product combo
        SELECT
            COALESCE(SUM(CASE
                WHEN txn_type = 'ISSUE_TO_SALESMAN' THEN ABS(delta_qty)
                WHEN txn_type = 'RETURN_FROM_SALESMAN' THEN -delta_qty
                WHEN txn_type = 'SOLD_BY_SALESMAN' THEN delta_qty
                ELSE 0 END), 0)
        INTO v_current_stock
        FROM warehouse_ledger
        WHERE salesman_alias = NEW.salesman_alias AND product_code = NEW.product_code;

        -- Update summary
        INSERT INTO salesman_stock_summary
            (salesman_alias, product_code, current_stock, last_updated)
        VALUES
            (NEW.salesman_alias, NEW.product_code, v_current_stock, NOW())
        ON DUPLICATE KEY UPDATE
            current_stock = v_current_stock,
            last_updated = NOW();
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- STEP 6: Populate initial data from existing ledger
-- ============================================================

CALL sync_salesman_stock_summary();

-- ============================================================
-- Success Message
-- ============================================================

SELECT 'Salesman stock summary table created and synced!' AS status;

