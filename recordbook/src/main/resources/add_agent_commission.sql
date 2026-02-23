-- Add agent_commission column to daily_sale_record table
ALTER TABLE daily_sale_record
ADD COLUMN agent_commission DECIMAL(10,2) DEFAULT 0;

