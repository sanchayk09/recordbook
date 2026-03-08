-- V010: DB structure improvements
-- Fixes the daily_summary unique constraint, adds missing indexes,
-- and adds qty_before/qty_after audit columns to salesman_ledger.

-- ============================================================
-- 1. Fix daily_summary unique constraint
--    Old: UNIQUE on sale_date alone (prevents multi-salesman days)
--    New: UNIQUE on (salesman_alias, sale_date)
-- ============================================================
ALTER TABLE `daily_summary`
    DROP INDEX `sale_date`,
    ADD UNIQUE INDEX `uk_daily_summary` (`salesman_alias`, `sale_date`),
    ADD INDEX `idx_daily_summary_date` (`sale_date`),
    MODIFY `total_revenue`          DECIMAL(12,2) NOT NULL,
    MODIFY `total_agent_commission` DECIMAL(12,2) NOT NULL,
    MODIFY `total_expense`          DECIMAL(12,2) NOT NULL,
    MODIFY `material_cost`          DECIMAL(12,2) NOT NULL,
    MODIFY `net_profit`             DECIMAL(12,2) DEFAULT NULL;

-- ============================================================
-- 2. daily_sale_record: remove unused expense_id column,
--    add missing indexes on sale_date and (salesman_name, sale_date)
-- ============================================================
ALTER TABLE `daily_sale_record`
    DROP COLUMN IF EXISTS `expense_id`,
    ADD CONSTRAINT `chk_dsr_qty_positive` CHECK (`quantity` > 0),
    ADD INDEX `idx_daily_sale_date` (`sale_date`),
    ADD INDEX `idx_daily_sale_salesman_date` (`salesman_name`, `sale_date`);

-- ============================================================
-- 3. salesman_ledger: add qty_before / qty_after for audit parity
--    with warehouse_ledger
-- ============================================================
ALTER TABLE `salesman_ledger`
    ADD COLUMN `qty_before` INT DEFAULT NULL
        COMMENT 'Salesman stock before this transaction'
        AFTER `delta_qty`,
    ADD COLUMN `qty_after` INT DEFAULT NULL
        COMMENT 'Salesman stock after this transaction'
        AFTER `qty_before`;

ALTER TABLE `salesman_ledger_archive`
    ADD COLUMN `qty_before` INT DEFAULT NULL AFTER `delta_qty`,
    ADD COLUMN `qty_after`  INT DEFAULT NULL AFTER `qty_before`;
