-- ============================================================
-- Migration Script: Add volume_sold column to daily_sale_record
-- ============================================================
-- This script adds the volume_sold column and calculates initial values
-- based on product_code and quantity
-- ============================================================

-- Step 1: Add the volume_sold column
ALTER TABLE daily_sale_record
ADD COLUMN volume_sold DECIMAL(12,2)
COMMENT 'Calculated as quantity * volume_in_quantity based on product_code';

-- Step 2: Update existing records with calculated volume_sold values
-- For n500 or l500: volume_in_quantity = 0.5
UPDATE daily_sale_record
SET volume_sold = quantity * 0.5
WHERE LOWER(product_code) IN ('n500', 'l500');

-- For n5 or l5: volume_in_quantity = 5
UPDATE daily_sale_record
SET volume_sold = quantity * 5
WHERE LOWER(product_code) IN ('n5', 'l5');

-- For n1 or l1: volume_in_quantity = 1
UPDATE daily_sale_record
SET volume_sold = quantity * 1
WHERE LOWER(product_code) IN ('n1', 'l1');

-- For all other product codes: volume_in_quantity = 1 (default)
UPDATE daily_sale_record
SET volume_sold = quantity * 1
WHERE volume_sold IS NULL;

-- Step 3: Verify the update
SELECT
    product_code,
    COUNT(*) as record_count,
    SUM(quantity) as total_quantity,
    SUM(volume_sold) as total_volume_sold
FROM daily_sale_record
GROUP BY product_code
ORDER BY product_code;

-- ============================================================
-- Notes:
-- - The volume_sold will be automatically calculated for new records
-- - Product codes are case-insensitive (n500, N500, L500, l500 all work)
-- - Default volume_in_quantity is 1 for unknown product codes
-- ============================================================

