-- ============================================================
-- Migration Script: Add volume_sold column to daily_summary
-- ============================================================
-- This script adds the volume_sold column to daily_summary table
-- ============================================================

-- Step 1: Add the volume_sold column
ALTER TABLE daily_summary
ADD COLUMN volume_sold DECIMAL(12,2)
COMMENT 'Total volume sold for this day (sum of volume_sold from daily_sale_record)';

-- Step 2: Update existing records with calculated volume_sold values from daily_sale_record
UPDATE daily_summary ds
SET volume_sold = (
    SELECT COALESCE(SUM(dsr.volume_sold), 0)
    FROM daily_sale_record dsr
    WHERE dsr.salesman_name = ds.salesman_alias
    AND dsr.sale_date = ds.sale_date
)
WHERE volume_sold IS NULL;

-- Step 3: Verify the update
SELECT
    sale_date,
    salesman_alias,
    total_revenue,
    volume_sold
FROM daily_summary
ORDER BY sale_date DESC
LIMIT 10;

-- ============================================================
-- Notes:
-- - The volume_sold will be automatically calculated from daily_sale_record
-- - For new records, it's set during summary submission
-- ============================================================

