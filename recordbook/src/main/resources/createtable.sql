-- ============================================================
-- URVI CLEAN - Production Ready ERP Schema (MySQL 8+)
-- ============================================================
-- Notes:
-- - Uses BIGINT AUTO_INCREMENT primary keys (stable IDs)
-- - Supports multi-warehouse chemical inventory
-- - Supports production batches + batch chemical consumption for true costing
-- - Sales records reference customer/product/salesman (+ optional batch)
-- - Includes useful indexes
-- ============================================================

-- ----------------------------
-- 0) Drop in safe order
-- ----------------------------
DROP TABLE IF EXISTS daily_sale_record;
DROP TABLE IF EXISTS daily_expense_record;
DROP TABLE IF EXISTS sales_records;
DROP TABLE IF EXISTS salesman_expenses;

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

-- ----------------------------
-- 1) Infrastructure & Procurement
-- ----------------------------
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
    unit VARCHAR(20) NOT NULL, -- Litre, Kg, Piece etc.
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

-- ----------------------------
-- 2) Products & Recipes
-- ----------------------------
CREATE TABLE products (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    variant VARCHAR(50),
    size VARCHAR(20),
    target_price DECIMAL(12,2),
    base_commission DECIMAL(12,2),
    other_overhead_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_products_name (product_name),
    INDEX idx_products_variant (variant)
) ENGINE=InnoDB;

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

-- ----------------------------
-- 3) Manufacturing / Production
-- ----------------------------
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

-- ----------------------------
-- 4) Routes & Villages
-- ----------------------------
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

-- ----------------------------
-- 5) Team & Customers
-- ----------------------------
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

-- ----------------------------
-- 6) Salesman Expenses
-- ----------------------------
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

-- ----------------------------
-- 7) Sales Records
-- ----------------------------
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

-- ----------------------------
-- 8) Daily Expense Record (Aggregated)
-- ----------------------------
DROP TABLE IF EXISTS daily_expense_record;

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

    INDEX idx_daily_expense_date (expense_date),

) ENGINE=InnoDB;

DROP TABLE IF EXISTS daily_sale_record;

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
    revenue DECIMAL(12,2) NOT NULL
);

CREATE INDEX idx_salesman_name
ON daily_sale_record(salesman_name);

CREATE INDEX idx_daily_sale_product_code
ON daily_sale_record(product_code);
