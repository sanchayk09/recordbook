-- Create salesman_ledger table for audit history of salesman movements
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

-- Create archive table for warehouse_ledger
CREATE TABLE IF NOT EXISTS warehouse_ledger_archive LIKE warehouse_ledger;

-- Add index to archive table for performance
CREATE INDEX IF NOT EXISTS idx_wla_created_at ON warehouse_ledger_archive(created_at);

