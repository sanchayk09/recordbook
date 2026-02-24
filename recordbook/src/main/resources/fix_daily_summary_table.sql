-- ========================================
-- Fix Daily Summary Table Column Order
-- ========================================
-- The table columns were created in wrong order
-- This script fixes it by dropping and recreating

-- Step 1: Drop the old table with wrong column order
DROP TABLE IF EXISTS daily_summary;

-- Step 2: Create table with CORRECT column order
CREATE TABLE daily_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    salesman_alias VARCHAR(100) NOT NULL,
    sale_date DATE NOT NULL UNIQUE,

    total_revenue DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total_agent_commission DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total_expense DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    material_cost DECIMAL(14,2) NOT NULL DEFAULT 0.00,

    net_profit DECIMAL(14,2) COMMENT 'Calculated as: total_revenue - total_agent_commission - total_expense - material_cost',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_daily_summary_salesman
        FOREIGN KEY (salesman_alias) REFERENCES salesmen(alias)
        ON UPDATE RESTRICT ON DELETE RESTRICT,

    UNIQUE KEY uk_summary_alias_date (salesman_alias, sale_date),
    INDEX idx_summary_date (sale_date),
    INDEX idx_summary_alias (salesman_alias)
) ENGINE=InnoDB;

-- ========================================
-- Column Order (CORRECT):
-- ========================================
-- 1. id (auto-increment)
-- 2. salesman_alias
-- 3. sale_date
-- 4. total_revenue ← FROM daily_sale_record SUM(revenue)
-- 5. total_agent_commission ← FROM daily_sale_record SUM(agent_commission)
-- 6. total_expense ← FROM request body
-- 7. material_cost ← FROM request body
-- 8. net_profit ← CALCULATED
-- 9. created_at
-- 10. updated_at
-- ========================================

