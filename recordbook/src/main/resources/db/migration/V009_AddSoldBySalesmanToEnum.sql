-- Migration: Add SOLD_BY_SALESMAN to warehouse_ledger enum
-- Purpose: Fix "Data truncated for column 'txn_type'" error
-- Date: 2026-03-02
-- Reason: Java enum has SOLD_BY_SALESMAN but database doesn't

-- ============================================================
-- IMPORTANT: Run this for BOTH databases
-- ============================================================

-- Database 1: urviclean_manual
USE urviclean_manual;

ALTER TABLE warehouse_ledger
MODIFY COLUMN txn_type ENUM(
    'TRANSFER_IN',
    'ISSUE_TO_SALESMAN',
    'RETURN_FROM_SALESMAN',
    'MANUAL_ADJUST',
    'DAMAGE',
    'SOLD_BY_SALESMAN'
) NOT NULL COMMENT 'Transaction type - now includes SOLD_BY_SALESMAN';

SELECT 'urviclean_manual: Added SOLD_BY_SALESMAN to enum' AS status;

-- ============================================================
-- Database 2: urviclean_test
-- ============================================================

USE urviclean_test;

ALTER TABLE warehouse_ledger
MODIFY COLUMN txn_type ENUM(
    'TRANSFER_IN',
    'ISSUE_TO_SALESMAN',
    'RETURN_FROM_SALESMAN',
    'MANUAL_ADJUST',
    'DAMAGE',
    'SOLD_BY_SALESMAN'
) NOT NULL COMMENT 'Transaction type - now includes SOLD_BY_SALESMAN';

SELECT 'urviclean_test: Added SOLD_BY_SALESMAN to enum' AS status;

-- ============================================================
-- Verification
-- ============================================================

-- Check the enum values (should now include SOLD_BY_SALESMAN)
USE urviclean_manual;
SHOW COLUMNS FROM warehouse_ledger LIKE 'txn_type';

USE urviclean_test;
SHOW COLUMNS FROM warehouse_ledger LIKE 'txn_type';

SELECT '✅ ENUM updated successfully! Now restart your application.' AS final_status;

