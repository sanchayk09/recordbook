-- ============================================================
-- Migration Script: Add total_quantity column to daily_summary
-- ============================================================
-- This script adds the total_quantity column to daily_summary table
-- ============================================================

-- Step 1: Add the total_quantity column
ALTER TABLE daily_summary
ADD COLUMN total_quantity BIGINT
COMMENT 'Total quantity sold for this day (sum of quantity from daily_sale_record)';

-- Step 2: Update existing records with calculated total_quantity values from daily_sale_record
UPDATE daily_summary ds
SET total_quantity = (
    SELECT COALESCE(SUM(dsr.quantity), 0)
    FROM daily_sale_record dsr
    WHERE dsr.salesman_name = ds.salesman_alias
    AND dsr.sale_date = ds.sale_date
)
WHERE total_quantity IS NULL;

-- Step 3: Verify the update
SELECT
    sale_date,
    salesman_alias,
    total_revenue,
    volume_sold,
    total_quantity
FROM daily_summary
ORDER BY sale_date DESC
LIMIT 10;

-- ============================================================
-- Notes:
-- - The total_quantity will be automatically calculated from daily_sale_record
-- - For new records, it's set during summary submission
-- ============================================================

