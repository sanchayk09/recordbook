-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: urviclean_test
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `batch_consumption`
--

DROP TABLE IF EXISTS `batch_consumption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `batch_consumption` (
  `consumption_id` bigint NOT NULL AUTO_INCREMENT,
  `batch_id` bigint NOT NULL,
  `chemical_id` bigint NOT NULL,
  `qty_used` decimal(38,2) DEFAULT NULL,
  `unit_cost_at_time` decimal(38,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`consumption_id`),
  KEY `idx_consumption_batch` (`batch_id`),
  KEY `idx_consumption_chemical` (`chemical_id`),
  CONSTRAINT `fk_consumption_batch` FOREIGN KEY (`batch_id`) REFERENCES `production_batches` (`batch_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_consumption_chemical` FOREIGN KEY (`chemical_id`) REFERENCES `chemicals` (`chemical_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chemical_inventory`
--

DROP TABLE IF EXISTS `chemical_inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chemical_inventory` (
  `inventory_id` bigint NOT NULL AUTO_INCREMENT,
  `chemical_id` bigint NOT NULL,
  `warehouse_id` bigint NOT NULL,
  `current_stock_level` decimal(38,2) DEFAULT NULL,
  `reorder_point` decimal(38,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`inventory_id`),
  UNIQUE KEY `uk_inventory_chemical_warehouse` (`chemical_id`,`warehouse_id`),
  KEY `idx_inventory_chemical` (`chemical_id`),
  KEY `idx_inventory_warehouse` (`warehouse_id`),
  CONSTRAINT `fk_inventory_chemical` FOREIGN KEY (`chemical_id`) REFERENCES `chemicals` (`chemical_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_inventory_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouses` (`warehouse_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `chemicals`
--

DROP TABLE IF EXISTS `chemicals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chemicals` (
  `chemical_id` bigint NOT NULL AUTO_INCREMENT,
  `chemical_name` varchar(255) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `purchase_rate` decimal(38,2) DEFAULT NULL,
  `transport_cost_per_unit` decimal(38,2) DEFAULT NULL,
  `vendor_id` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`chemical_id`),
  KEY `idx_chemicals_vendor` (`vendor_id`),
  KEY `idx_chemicals_name` (`chemical_name`),
  CONSTRAINT `fk_chemicals_vendor` FOREIGN KEY (`vendor_id`) REFERENCES `vendors` (`vendor_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT,
  `shop_name` varchar(255) DEFAULT NULL,
  `owner_first_name` varchar(255) DEFAULT NULL,
  `owner_last_name` varchar(255) DEFAULT NULL,
  `owner_address` varchar(255) DEFAULT NULL,
  `customer_type` enum('Dealer','Subdealer','Shopkeeper','Household') DEFAULT NULL,
  `route_id` bigint DEFAULT NULL,
  `village_id` bigint DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  KEY `idx_customers_route` (`route_id`),
  KEY `idx_customers_village` (`village_id`),
  KEY `idx_customers_shop` (`shop_name`),
  CONSTRAINT `fk_customers_route` FOREIGN KEY (`route_id`) REFERENCES `routes` (`route_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_customers_village` FOREIGN KEY (`village_id`) REFERENCES `route_villages` (`village_id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `daily_expense_record`
--

DROP TABLE IF EXISTS `daily_expense_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_expense_record` (
  `expense_date` date NOT NULL,
  `salesman_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `total_expense` decimal(38,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`expense_date`,`salesman_alias`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `daily_sale_record`
--

DROP TABLE IF EXISTS `daily_sale_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_sale_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sl_no` int NOT NULL,
  `sale_date` date NOT NULL,
  `salesman_name` varchar(100) NOT NULL,
  `customer_name` varchar(150) NOT NULL,
  `customer_type` enum('CUSTOMER','SHOPKEEPER') NOT NULL,
  `village` varchar(100) DEFAULT NULL,
  `mobile_number` varchar(20) DEFAULT NULL,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `quantity` int NOT NULL CHECK (`quantity` > 0),
  `rate` decimal(10,2) NOT NULL,
  `revenue` decimal(12,2) NOT NULL,
  `agent_commission` decimal(10,2) DEFAULT NULL,
  `volume_sold` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_daily_sale_date` (`sale_date`),
  KEY `idx_daily_sale_salesman_date` (`salesman_name`, `sale_date`),
  KEY `idx_daily_sale_product_code` (`product_code`)
) ENGINE=InnoDB AUTO_INCREMENT=238 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `daily_summary`
--

DROP TABLE IF EXISTS `daily_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `daily_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `salesman_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sale_date` date NOT NULL,
  `total_revenue` decimal(12,2) NOT NULL,
  `total_agent_commission` decimal(12,2) NOT NULL,
  `total_expense` decimal(12,2) NOT NULL,
  `material_cost` decimal(12,2) NOT NULL,
  `net_profit` decimal(12,2) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `volume_sold` decimal(12,2) DEFAULT NULL,
  `total_quantity` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_summary` (`salesman_alias`, `sale_date`),
  KEY `idx_daily_summary_date` (`sale_date`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_cost_manual`
--

DROP TABLE IF EXISTS `product_cost_manual`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_cost_manual` (
  `pid` bigint NOT NULL AUTO_INCREMENT,
  `cost` decimal(38,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `volume_per_pcs` decimal(38,2) DEFAULT NULL,
  `metric` varchar(255) DEFAULT NULL,
  `metric_quantity` decimal(38,2) DEFAULT NULL,
  `variant` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`pid`),
  UNIQUE KEY `UK3gk8d2fsdbjb7aewciajrgggb` (`product_code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_recipes`
--

DROP TABLE IF EXISTS `product_recipes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_recipes` (
  `recipe_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `chemical_id` bigint NOT NULL,
  `required_qty_per_unit` decimal(38,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`recipe_id`),
  UNIQUE KEY `uk_recipe_product_chemical` (`product_id`,`chemical_id`),
  KEY `idx_recipes_product` (`product_id`),
  KEY `idx_recipes_chemical` (`chemical_id`),
  CONSTRAINT `fk_recipes_chemical` FOREIGN KEY (`chemical_id`) REFERENCES `chemicals` (`chemical_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_recipes_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `production_batches`
--

DROP TABLE IF EXISTS `production_batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `production_batches` (
  `batch_id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `start_date` date NOT NULL,
  `total_qty_produced` decimal(38,2) DEFAULT NULL,
  `remaining_qty` decimal(38,2) DEFAULT NULL,
  `calculated_mfg_cost_per_unit` decimal(38,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`batch_id`),
  KEY `idx_batches_product_date` (`product_id`,`start_date`),
  KEY `idx_batches_date` (`start_date`),
  CONSTRAINT `fk_batches_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `product_name` varchar(255) DEFAULT NULL,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `size` varchar(255) DEFAULT NULL,
  `target_price` decimal(38,2) DEFAULT NULL,
  `base_commission` decimal(38,2) DEFAULT NULL,
  `other_overhead_cost` decimal(38,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  KEY `idx_products_name` (`product_name`),
  KEY `idx_products_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `route_villages`
--

DROP TABLE IF EXISTS `route_villages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `route_villages` (
  `village_id` bigint NOT NULL AUTO_INCREMENT,
  `route_id` bigint NOT NULL,
  `village_name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`village_id`),
  UNIQUE KEY `uk_villages_route_name` (`route_id`,`village_name`),
  KEY `idx_villages_route` (`route_id`),
  CONSTRAINT `fk_villages_route` FOREIGN KEY (`route_id`) REFERENCES `routes` (`route_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `routes`
--

DROP TABLE IF EXISTS `routes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `routes` (
  `route_id` bigint NOT NULL AUTO_INCREMENT,
  `route_name` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`route_id`),
  UNIQUE KEY `uk_routes_name` (`route_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sales_records`
--

DROP TABLE IF EXISTS `sales_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_records` (
  `sale_id` bigint NOT NULL AUTO_INCREMENT,
  `salesman_id` bigint NOT NULL,
  `customer_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `batch_id` bigint DEFAULT NULL,
  `order_date` date NOT NULL,
  `actual_rate` decimal(38,2) DEFAULT NULL,
  `quantity` decimal(38,2) DEFAULT NULL,
  `revenue` decimal(38,2) DEFAULT NULL,
  `adjusted_margin` decimal(38,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `village_id` bigint DEFAULT NULL,
  PRIMARY KEY (`sale_id`),
  KEY `fk_sales_batch` (`batch_id`),
  KEY `idx_sales_date` (`order_date`),
  KEY `idx_sales_salesman_date` (`salesman_id`,`order_date`),
  KEY `idx_sales_customer_date` (`customer_id`,`order_date`),
  KEY `idx_sales_product` (`product_id`),
  KEY `FKoo2on917sf0cebw3fm55milns` (`village_id`),
  CONSTRAINT `fk_sales_batch` FOREIGN KEY (`batch_id`) REFERENCES `production_batches` (`batch_id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_sales_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_sales_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_sales_salesman` FOREIGN KEY (`salesman_id`) REFERENCES `salesmen` (`salesman_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKoo2on917sf0cebw3fm55milns` FOREIGN KEY (`village_id`) REFERENCES `route_villages` (`village_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `salesman_expenses`
--

DROP TABLE IF EXISTS `salesman_expenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesman_expenses` (
  `expense_id` bigint NOT NULL AUTO_INCREMENT,
  `salesman_id` bigint NOT NULL,
  `expense_date` date NOT NULL,
  `category` varchar(255) DEFAULT NULL,
  `amount` decimal(38,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`expense_id`),
  KEY `idx_expenses_salesman_date` (`salesman_id`,`expense_date`),
  KEY `idx_expenses_date` (`expense_date`),
  CONSTRAINT `fk_expenses_salesman` FOREIGN KEY (`salesman_id`) REFERENCES `salesmen` (`salesman_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `salesman_ledger`
--

DROP TABLE IF EXISTS `salesman_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesman_ledger` (
  `salesman_ledger_id` bigint NOT NULL AUTO_INCREMENT,
  `salesman_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `txn_type` enum('ISSUE_FROM_WAREHOUSE','SOLD','RETURN_TO_WAREHOUSE','MANUAL_ADJUST','DAMAGE') NOT NULL,
  `delta_qty` int NOT NULL COMMENT 'Positive for additions, negative for removals',
  `qty_before` int DEFAULT NULL COMMENT 'Salesman stock before this transaction',
  `qty_after` int DEFAULT NULL COMMENT 'Salesman stock after this transaction',
  `remarks` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`salesman_ledger_id`),
  KEY `idx_sl_salesman_created` (`salesman_alias`,`created_at`),
  KEY `idx_sl_salesman_product_created` (`salesman_alias`,`product_code`,`created_at`),
  KEY `idx_sl_type_created` (`txn_type`,`created_at`),
  KEY `fk_sl_product_code` (`product_code`),
  CONSTRAINT `fk_sl_product_code` FOREIGN KEY (`product_code`) REFERENCES `product_cost_manual` (`product_code`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_sl_salesman_alias` FOREIGN KEY (`salesman_alias`) REFERENCES `salesmen` (`alias`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `salesman_ledger_archive`
--

DROP TABLE IF EXISTS `salesman_ledger_archive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesman_ledger_archive` (
  `salesman_ledger_id` bigint NOT NULL AUTO_INCREMENT,
  `salesman_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `txn_type` enum('ISSUE_FROM_WAREHOUSE','SOLD','RETURN_TO_WAREHOUSE','MANUAL_ADJUST','DAMAGE') NOT NULL,
  `delta_qty` int NOT NULL,
  `qty_before` int DEFAULT NULL,
  `qty_after` int DEFAULT NULL,
  `remarks` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`salesman_ledger_id`),
  KEY `idx_sl_salesman_created` (`salesman_alias`,`created_at`),
  KEY `idx_sl_salesman_product_created` (`salesman_alias`,`product_code`,`created_at`),
  KEY `idx_sl_type_created` (`txn_type`,`created_at`),
  KEY `fk_sl_product_code` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `salesman_stock_summary`
--

DROP TABLE IF EXISTS `salesman_stock_summary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesman_stock_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `salesman_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `current_stock` int NOT NULL DEFAULT '0',
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_salesman_product` (`salesman_alias`,`product_code`),
  KEY `idx_salesman_alias` (`salesman_alias`),
  KEY `idx_product_code` (`product_code`),
  KEY `idx_last_updated` (`last_updated`),
  CONSTRAINT `fk_summary_product` FOREIGN KEY (`product_code`) REFERENCES `product_cost_manual` (`product_code`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_summary_salesman` FOREIGN KEY (`salesman_alias`) REFERENCES `salesmen` (`alias`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `salesmen`
--

DROP TABLE IF EXISTS `salesmen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `salesmen` (
  `salesman_id` bigint NOT NULL AUTO_INCREMENT,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `contact_number` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`salesman_id`),
  UNIQUE KEY `uk_sales` (`alias`),
  KEY `idx_salesmen_name` (`last_name`,`first_name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vendors`
--

DROP TABLE IF EXISTS `vendors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vendors` (
  `vendor_id` bigint NOT NULL AUTO_INCREMENT,
  `vendor_name` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `contact_number` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`vendor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warehouse_inventory`
--

DROP TABLE IF EXISTS `warehouse_inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse_inventory` (
  `warehouse_inventory_id` bigint NOT NULL AUTO_INCREMENT,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `qty_available` int NOT NULL DEFAULT '0' COMMENT 'Current physical stock in warehouse (PCS)',
  `last_updated` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`warehouse_inventory_id`),
  UNIQUE KEY `uk_warehouse_product_code` (`product_code`),
  KEY `idx_warehouse_product_code` (`product_code`),
  CONSTRAINT `chk_qty_not_negative` CHECK ((`qty_available` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Current warehouse stock - sellable inventory only';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warehouse_ledger`
--

DROP TABLE IF EXISTS `warehouse_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse_ledger` (
  `warehouse_ledger_id` bigint NOT NULL AUTO_INCREMENT,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `txn_type` enum('TRANSFER_IN','ISSUE_TO_SALESMAN','RETURN_FROM_SALESMAN','MANUAL_ADJUST','DAMAGE') NOT NULL,
  `delta_qty` int NOT NULL COMMENT 'Positive for additions, negative for removals',
  `qty_before` int NOT NULL,
  `qty_after` int NOT NULL,
  `salesman_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `remarks` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`warehouse_ledger_id`),
  KEY `idx_ledger_product_code` (`product_code`),
  KEY `idx_ledger_txn_type` (`txn_type`),
  KEY `idx_ledger_salesman` (`salesman_alias`),
  KEY `idx_ledger_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Audit trail for all warehouse stock movements';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warehouse_ledger_archive`
--

DROP TABLE IF EXISTS `warehouse_ledger_archive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse_ledger_archive` (
  `warehouse_ledger_id` bigint NOT NULL AUTO_INCREMENT,
  `product_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `txn_type` enum('TRANSFER_IN','ISSUE_TO_SALESMAN','RETURN_FROM_SALESMAN','MANUAL_ADJUST','DAMAGE') NOT NULL,
  `delta_qty` int NOT NULL COMMENT 'Positive for additions, negative for removals',
  `qty_before` int NOT NULL,
  `qty_after` int NOT NULL,
  `salesman_alias` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `remarks` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`warehouse_ledger_id`),
  KEY `idx_ledger_product_code` (`product_code`),
  KEY `idx_ledger_txn_type` (`txn_type`),
  KEY `idx_ledger_salesman` (`salesman_alias`),
  KEY `idx_ledger_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Audit trail for all warehouse stock movements';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warehouses`
--

DROP TABLE IF EXISTS `warehouses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouses` (
  `warehouse_id` bigint NOT NULL AUTO_INCREMENT,
  `warehouse_name` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`warehouse_id`),
  UNIQUE KEY `uk_warehouses_name` (`warehouse_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-05 21:10:21
