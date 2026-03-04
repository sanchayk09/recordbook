-- Migration: Remove trigger and SOLD_BY_SALESMAN from warehouse_ledger
-- Purpose: Enforce ledger separation design rule:
--   warehouse_ledger = ONLY warehouse movements
--   salesman_ledger  = ONLY salesman movements (including SOLD)
-- Date: 2026-03-04

-- ============================================================
-- STEP 1: Drop trigger on warehouse_ledger (no longer needed)
-- ============================================================

DROP TRIGGER IF EXISTS sync_stock_on_ledger_insert;

-- ============================================================
-- STEP 2: Drop stored procedure (no longer needed)
-- ============================================================

DROP PROCEDURE IF EXISTS sync_salesman_stock_summary;

-- ============================================================
-- STEP 3: Remove SOLD_BY_SALESMAN from warehouse_ledger enum
-- ============================================================

ALTER TABLE warehouse_ledger
MODIFY COLUMN txn_type ENUM(
    'TRANSFER_IN',
    'ISSUE_TO_SALESMAN',
    'RETURN_FROM_SALESMAN',
    'MANUAL_ADJUST',
    'DAMAGE'
) NOT NULL;

-- ============================================================
-- STEP 4: Create salesman_ledger if not already present
-- ============================================================

CREATE TABLE IF NOT EXISTS salesman_ledger (
  salesman_ledger_id BIGINT NOT NULL AUTO_INCREMENT,
  salesman_alias VARCHAR(255) NOT NULL,
  product_code VARCHAR(20) NOT NULL,
  txn_type ENUM(
    'ISSUE_FROM_WAREHOUSE',
    'SOLD',
    'RETURN_TO_WAREHOUSE',
    'MANUAL_ADJUST',
    'DAMAGE'
  ) NOT NULL,
  delta_qty INT NOT NULL COMMENT 'Positive = add to salesman stock, Negative = reduce',
  remarks TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (salesman_ledger_id),
  KEY idx_sl_salesman_created (salesman_alias, created_at),
  KEY idx_sl_salesman_product_created (salesman_alias, product_code, created_at),
  CONSTRAINT fk_sl_product_code FOREIGN KEY (product_code)
    REFERENCES product_cost_manual (product_code)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_sl_salesman_alias FOREIGN KEY (salesman_alias)
    REFERENCES salesmen (alias)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- STEP 5: Create warehouse_ledger_archive if not already present
-- ============================================================

CREATE TABLE IF NOT EXISTS warehouse_ledger_archive LIKE warehouse_ledger;
