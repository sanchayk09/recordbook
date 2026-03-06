-- Migration: Fix daily_summary unique constraint
-- Purpose: Change the unique constraint from (sale_date only) to (salesman_alias, sale_date)
--          so multiple salesmen can each have a summary row for the same date.
-- Date: 2026-03-06
--
-- NOTE: This project manages schema via ddl-auto=update (Hibernate).
--       Run this script MANUALLY on existing deployments before restarting the application.
--       New deployments will have the correct schema created by Hibernate from the entity.

-- ------------------------------------------------------------
-- STEP 1: Safely drop the old unique constraint on sale_date
--         (name may vary depending on how Hibernate created it)
-- ------------------------------------------------------------

DROP PROCEDURE IF EXISTS _fix_daily_summary_constraint;

DELIMITER $$
CREATE PROCEDURE _fix_daily_summary_constraint()
BEGIN
    DECLARE v_index_name VARCHAR(255) DEFAULT NULL;

    -- Only operate if the table exists (existing deployments)
    IF EXISTS (
        SELECT 1
        FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'daily_summary'
    ) THEN

        -- Find any single-column unique index on sale_date
        -- (excludes PRIMARY and the new composite constraint)
        SELECT INDEX_NAME INTO v_index_name
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'daily_summary'
          AND COLUMN_NAME = 'sale_date'
          AND NON_UNIQUE = 0
          AND INDEX_NAME != 'PRIMARY'
          AND INDEX_NAME != 'uk_daily_summary_salesman_date'
        LIMIT 1;

        IF v_index_name IS NOT NULL THEN
            SET @drop_sql = CONCAT('ALTER TABLE daily_summary DROP INDEX `', v_index_name, '`');
            PREPARE p FROM @drop_sql;
            EXECUTE p;
            DEALLOCATE PREPARE p;
        END IF;

        -- ------------------------------------------------------------
        -- STEP 2: Add composite unique constraint if not already present
        -- ------------------------------------------------------------
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.TABLE_CONSTRAINTS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = 'daily_summary'
              AND CONSTRAINT_NAME = 'uk_daily_summary_salesman_date'
        ) THEN
            ALTER TABLE daily_summary
                ADD CONSTRAINT uk_daily_summary_salesman_date UNIQUE (salesman_alias, sale_date);
        END IF;

    END IF;
END$$
DELIMITER ;

CALL _fix_daily_summary_constraint();
DROP PROCEDURE IF EXISTS _fix_daily_summary_constraint;
