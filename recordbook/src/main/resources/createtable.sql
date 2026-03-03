-- ============================================================
-- URVI CLEAN - Production Ready ERP Schema (MySQL 8+)
-- Fresh Schema with All Migrations Applied
-- Generated: February 28, 2026
-- ============================================================

-- Set SQL mode for compatibility
SET SQL_MODE = "0";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";

-- ============================================================
-- Drop existing tables in safe order
-- ============================================================
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS warehouse_ledger;
DROP TABLE IF EXISTS warehouse_inventory;
DROP TABLE IF EXISTS daily_sale_record;
DROP TABLE IF EXISTS daily_expense_record;
DROP TABLE IF EXISTS daily_summary;
DROP TABLE IF EXISTS sales_records;
DROP TABLE IF EXISTS salesman_expenses;
DROP TABLE IF EXISTS product_cost_manual;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS salesmen;
DROP TABLE IF EXISTS route_villages;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS batch_consumption;
DROP TABLE IF EXISTS production_batches;
DROP TABLE IF EXISTS product_recipes;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS chemical_inventory;
DROP TABLE IF EXISTS warehouses;
DROP TABLE IF EXISTS chemicals;
DROP TABLE IF EXISTS vendors;

SET FOREIGN_KEY_CHECKS=1;

-- ============================================================
-- 1) Infrastructure & Procurement
-- ============================================================

CREATE TABLE vendors (
    vendor_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vendor_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    address VARCHAR(150),
    contact_number VARCHAR(25),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE chemicals (
    chemical_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chemical_name VARCHAR(100) NOT NULL,
    category ENUM('Raw Material', 'Packaging', 'Labeling', 'Other') NOT NULL,
    unit VARCHAR(20) NOT NULL,
    purchase_rate DECIMAL(12,4) NOT NULL,
    transport_cost_per_unit DECIMAL(12,4) NOT NULL DEFAULT 0.0000,
    vendor_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chemicals_vendor
        FOREIGN KEY (vendor_id) REFERENCES vendors(vendor_id)
        ON UPDATE RESTRICT ON DELETE SET NULL,
    INDEX idx_chemicals_vendor (vendor_id),
    INDEX idx_chemicals_name (chemical_name)
) ENGINE=InnoDB;

CREATE TABLE warehouses (
    warehouse_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_name VARCHAR(100) NOT NULL,
    location VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_warehouses_name (warehouse_name)
) ENGINE=InnoDB;

CREATE TABLE chemical_inventory (
    inventory_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chemical_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    current_stock_level DECIMAL(14,4) NOT NULL DEFAULT 0.0000,
    reorder_point DECIMAL(14,4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_chemical
        FOREIGN KEY (chemical_id) REFERENCES chemicals(chemical_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_inventory_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    UNIQUE KEY uk_inventory_chemical_warehouse (chemical_id, warehouse_id),
    INDEX idx_inventory_chemical (chemical_id),
    INDEX idx_inventory_warehouse (warehouse_id)
) ENGINE=InnoDB;

-- ============================================================
-- 2) Products & Recipes
-- ============================================================

CREATE TABLE products (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    product_code VARCHAR(50),
    size VARCHAR(20),
    target_price DECIMAL(12,2),
    base_commission DECIMAL(12,2),
    other_overhead_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_products_name (product_name),
    INDEX idx_products_product_code (product_code)
) ENGINE=InnoDB;

CREATE TABLE product_cost_manual (
    pid BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    product_code VARCHAR(20) NOT NULL,
    cost DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_code (product_code),
    INDEX idx_product_name (product_name),
    INDEX idx_product_code (product_code)
) ENGINE=InnoDB;

-- ============================================================
-- Warehouse Management (Using product_cost_manual as Product Master)
-- ============================================================

CREATE TABLE warehouse_inventory (
    warehouse_inventory_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(20) NOT NULL,
    qty_available INT NOT NULL DEFAULT 0 COMMENT 'Current physical stock in warehouse (PCS)',
    last_updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_warehouse_inv_product
        FOREIGN KEY (product_code) REFERENCES product_cost_manual(product_code)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    UNIQUE KEY uk_warehouse_product_code (product_code),
    INDEX idx_warehouse_product_code (product_code),

    CONSTRAINT chk_qty_not_negative CHECK (qty_available >= 0)
) ENGINE=InnoDB COMMENT='Current warehouse stock - sellable inventory only';

CREATE TABLE warehouse_ledger (
    warehouse_ledger_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_code VARCHAR(20) NOT NULL,
    txn_type ENUM('TRANSFER_IN', 'ISSUE_TO_SALESMAN', 'RETURN_FROM_SALESMAN', 'MANUAL_ADJUST', 'DAMAGE') NOT NULL,
    delta_qty INT NOT NULL COMMENT 'Positive for additions, negative for removals',
    qty_before INT NOT NULL,
    qty_after INT NOT NULL,
    salesman_alias VARCHAR(100) NULL COMMENT 'Applicable for ISSUE/RETURN transactions',
    remarks TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NULL,

    CONSTRAINT fk_warehouse_ledger_product
        FOREIGN KEY (product_code) REFERENCES product_cost_manual(product_code)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_warehouse_ledger_salesman
        FOREIGN KEY (salesman_alias) REFERENCES salesmen(alias)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    INDEX idx_ledger_product_code (product_code),
    INDEX idx_ledger_txn_type (txn_type),
    INDEX idx_ledger_salesman (salesman_alias),
    INDEX idx_ledger_created_at (created_at)
) ENGINE=InnoDB COMMENT='Audit trail for all warehouse stock movements';

CREATE TABLE product_recipes (
    recipe_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    chemical_id BIGINT NOT NULL,
    required_qty_per_unit DECIMAL(14,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recipes_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_recipes_chemical
        FOREIGN KEY (chemical_id) REFERENCES chemicals(chemical_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    UNIQUE KEY uk_recipe_product_chemical (product_id, chemical_id),
    INDEX idx_recipes_product (product_id),
    INDEX idx_recipes_chemical (chemical_id)
) ENGINE=InnoDB;

-- ============================================================
-- 3) Manufacturing / Production
-- ============================================================

CREATE TABLE production_batches (
    batch_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    total_qty_produced DECIMAL(14,4) NOT NULL DEFAULT 0.0000,
    remaining_qty DECIMAL(14,4) NOT NULL DEFAULT 0.0000,
    calculated_mfg_cost_per_unit DECIMAL(12,4),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_batches_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    INDEX idx_batches_product_date (product_id, start_date),
    INDEX idx_batches_date (start_date)
) ENGINE=InnoDB;

CREATE TABLE batch_consumption (
    consumption_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    batch_id BIGINT NOT NULL,
    chemical_id BIGINT NOT NULL,
    qty_used DECIMAL(14,4) NOT NULL,
    unit_cost_at_time DECIMAL(12,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_consumption_batch
        FOREIGN KEY (batch_id) REFERENCES production_batches(batch_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_consumption_chemical
        FOREIGN KEY (chemical_id) REFERENCES chemicals(chemical_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    INDEX idx_consumption_batch (batch_id),
    INDEX idx_consumption_chemical (chemical_id)
) ENGINE=InnoDB;

-- ============================================================
-- 4) Routes & Villages
-- ============================================================

CREATE TABLE routes (
    route_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_routes_name (route_name)
) ENGINE=InnoDB;

CREATE TABLE route_villages (
    village_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    route_id BIGINT NOT NULL,
    village_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_villages_route
        FOREIGN KEY (route_id) REFERENCES routes(route_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    UNIQUE KEY uk_villages_route_name (route_id, village_name),
    INDEX idx_villages_route (route_id)
) ENGINE=InnoDB;

-- ============================================================
-- 5) Team & Customers
-- ============================================================

CREATE TABLE salesmen (
    salesman_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    alias VARCHAR(100) NOT NULL,
    address VARCHAR(150),
    contact_number VARCHAR(25),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY salesmen_alias (alias)
) ENGINE=InnoDB;

CREATE TABLE customers (
    customer_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_name VARCHAR(100) NOT NULL,
    owner_first_name VARCHAR(50),
    owner_last_name VARCHAR(50),
    owner_address VARCHAR(150),
    customer_type ENUM('Dealer', 'Subdealer', 'Shopkeeper', 'Household'),
    route_id BIGINT,
    village_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customers_route
        FOREIGN KEY (route_id) REFERENCES routes(route_id)
        ON UPDATE RESTRICT ON DELETE SET NULL,
    CONSTRAINT fk_customers_village
        FOREIGN KEY (village_id) REFERENCES route_villages(village_id)
        ON UPDATE RESTRICT ON DELETE SET NULL,
    INDEX idx_customers_route (route_id),
    INDEX idx_customers_village (village_id),
    INDEX idx_customers_shop (shop_name)
) ENGINE=InnoDB;

-- ============================================================
-- 6) Salesman Expenses
-- ============================================================

CREATE TABLE salesman_expenses (
    expense_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    salesman_id BIGINT NOT NULL,
    expense_date DATE NOT NULL,
    category ENUM('Petrol', 'Food', 'Vehicle Rent', 'Mobile', 'Other'),
    amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expenses_salesman
        FOREIGN KEY (salesman_id) REFERENCES salesmen(salesman_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    INDEX idx_expenses_salesman_date (salesman_id, expense_date),
    INDEX idx_expenses_date (expense_date)
) ENGINE=InnoDB;

-- ============================================================
-- 7) Sales Records
-- ============================================================

CREATE TABLE sales_records (
    sale_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    salesman_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    batch_id BIGINT,
    order_date DATE NOT NULL,
    actual_rate DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    quantity DECIMAL(14,4) NOT NULL DEFAULT 0.0000,
    revenue DECIMAL(14,2) GENERATED ALWAYS AS (actual_rate * quantity) STORED,
    adjusted_margin DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sales_salesman
        FOREIGN KEY (salesman_id) REFERENCES salesmen(salesman_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_sales_customer
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_sales_product
        FOREIGN KEY (product_id) REFERENCES products(product_id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT fk_sales_batch
        FOREIGN KEY (batch_id) REFERENCES production_batches(batch_id)
        ON UPDATE RESTRICT ON DELETE SET NULL,

    INDEX idx_sales_date (order_date),
    INDEX idx_salesman_customer (salesman_id, customer_id),
    INDEX idx_salesman_product (salesman_id, product_id),
    INDEX idx_customer_product (customer_id, product_id),
    INDEX idx_sales_batch (batch_id)
) ENGINE=InnoDB;

-- ============================================================
-- 8) Daily Expense Record (Aggregated)
-- ============================================================

CREATE TABLE daily_expense_record (
    salesman_alias VARCHAR(100) NOT NULL,
    expense_date DATE NOT NULL,
    total_expense DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (salesman_alias, expense_date),

    CONSTRAINT fk_daily_expense_salesman
        FOREIGN KEY (salesman_alias) REFERENCES salesmen(alias)
        ON UPDATE RESTRICT ON DELETE RESTRICT,

    INDEX idx_daily_expense_date (expense_date)
) ENGINE=InnoDB;

-- ============================================================
-- 9) Daily Sale Record (with all migrations applied)
-- ============================================================

CREATE TABLE daily_sale_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sl_no INT NOT NULL,
    sale_date DATE NOT NULL,
    salesman_name VARCHAR(100) NOT NULL,
    expense_id BIGINT,
    customer_name VARCHAR(150) NOT NULL,
    customer_type ENUM('CUSTOMER','SHOPKEEPER') NOT NULL,
    village VARCHAR(100),
    mobile_number VARCHAR(20),
    product_code VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    rate DECIMAL(10,2) NOT NULL,
    revenue DECIMAL(12,2) NOT NULL,
    agent_commission DECIMAL(10,2) DEFAULT 0,
    volume_sold DECIMAL(12,2) COMMENT 'Calculated as quantity * volume_in_quantity based on product_code',

    INDEX idx_salesman_name (salesman_name),
    INDEX idx_daily_sale_product_code (product_code),
    INDEX idx_sale_date (sale_date)
) ENGINE=InnoDB;

-- ============================================================
-- 10) Daily Summary (with all migrations applied)
-- ============================================================

CREATE TABLE daily_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    salesman_alias VARCHAR(100) NOT NULL,
    sale_date DATE NOT NULL,

    total_revenue DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total_agent_commission DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    total_expense DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    material_cost DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    volume_sold DECIMAL(12,2) COMMENT 'Total volume sold for this day (sum of volume_sold from daily_sale_record)',
    total_quantity BIGINT COMMENT 'Total quantity sold for this day (sum of quantity from daily_sale_record)',

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

-- ============================================================
-- Schema creation completed successfully
-- ============================================================

COMMIT;

-- ============================================================
-- NOTES:
-- ============================================================
-- This schema includes all migrations:
-- 1. agent_commission column added to daily_sale_record
-- 2. volume_sold column added to daily_sale_record
-- 3. volume_sold column added to daily_summary
-- 4. total_quantity column added to daily_summary
-- 5. Fixed column order in daily_summary table
-- ============================================================

