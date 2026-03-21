-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: urviclean_trial_05032026
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
-- Current Database: `urviclean_trial_05032026`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `urviclean_trial_05032026` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `urviclean_trial_05032026`;

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
-- Dumping data for table `batch_consumption`
--

LOCK TABLES `batch_consumption` WRITE;
/*!40000 ALTER TABLE `batch_consumption` DISABLE KEYS */;
/*!40000 ALTER TABLE `batch_consumption` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `chemical_inventory`
--

LOCK TABLES `chemical_inventory` WRITE;
/*!40000 ALTER TABLE `chemical_inventory` DISABLE KEYS */;
/*!40000 ALTER TABLE `chemical_inventory` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `chemicals`
--

LOCK TABLES `chemicals` WRITE;
/*!40000 ALTER TABLE `chemicals` DISABLE KEYS */;
/*!40000 ALTER TABLE `chemicals` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `daily_expense_record`
--

LOCK TABLES `daily_expense_record` WRITE;
/*!40000 ALTER TABLE `daily_expense_record` DISABLE KEYS */;
INSERT INTO `daily_expense_record` VALUES ('2026-02-19','muk/antr','2026-03-09 06:01:40.865212',500.00,'2026-03-09 06:01:40.865212'),('2026-02-22','muk/antr','2026-02-26 06:39:16.375825',500.00,'2026-02-26 06:39:16.375825'),('2026-02-23','muk/antr','2026-02-23 11:22:45.805203',600.00,'2026-02-23 11:22:45.805203'),('2026-02-24','muk/antr','2026-02-24 07:25:36.200782',700.00,'2026-02-24 07:25:36.200782'),('2026-02-26','munnu/mukul','2026-02-26 06:39:16.375825',549.00,'2026-02-26 06:39:16.375825'),('2026-03-06','muku/Rahul','2026-03-06 11:41:22.018728',700.00,'2026-03-06 11:41:22.018728'),('2026-03-06','san/antr','2026-03-06 07:00:04.315388',500.00,'2026-03-06 15:35:04.713523'),('2026-03-09','muk/antr','2026-03-09 06:01:40.865212',700.00,'2026-03-09 06:01:40.865212');
/*!40000 ALTER TABLE `daily_expense_record` ENABLE KEYS */;
UNLOCK TABLES;

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
  `expense_id` bigint DEFAULT NULL,
  `customer_name` varchar(150) NOT NULL,
  `customer_type` enum('CUSTOMER','SHOPKEEPER') NOT NULL,
  `village` varchar(100) DEFAULT NULL,
  `mobile_number` varchar(20) DEFAULT NULL,
  `product_code` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `rate` decimal(10,2) NOT NULL,
  `revenue` decimal(12,2) NOT NULL,
  `agent_commission` decimal(10,2) DEFAULT NULL,
  `volume_sold` decimal(12,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_salesman_name` (`salesman_name`),
  KEY `idx_daily_sale_product_code` (`product_code`)
) ENGINE=InnoDB AUTO_INCREMENT=305 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `daily_sale_record`
--

LOCK TABLES `daily_sale_record` WRITE;
/*!40000 ALTER TABLE `daily_sale_record` DISABLE KEYS */;
INSERT INTO `daily_sale_record` VALUES (1,1,'2026-02-19','muk/antr',NULL,'Kuldeep Line Hotel','CUSTOMER','Lepo','870951742','N1',1,40.00,40.00,7.50,1.00),(2,2,'2026-02-19','muk/antr',NULL,'Ashraf','CUSTOMER','Maganpur','766723029','N1',3,40.00,120.00,22.50,3.00),(3,3,'2026-02-19','muk/antr',NULL,'Mahato Line Hotel','CUSTOMER','Jhijritand','9934103880','L5',1,150.00,150.00,25.00,5.00),(4,4,'2026-02-19','muk/antr',NULL,'Jeevan Jyoti','CUSTOMER','Soso','783152012','N500',4,30.00,120.00,30.00,2.00),(5,5,'2026-02-19','muk/antr',NULL,'Jeevan Jyoti','CUSTOMER','Soso','783152012','L500',2,25.00,50.00,10.00,1.00),(6,6,'2026-02-19','muk/antr',NULL,'Reliance Petrol Pump','CUSTOMER','Gola','839091853','N5',1,170.00,170.00,35.00,5.00),(7,7,'2026-02-19','muk/antr',NULL,'Damodar Hotel','CUSTOMER','Gola','7250259608','N1',1,60.00,60.00,17.50,1.00),(8,8,'2026-02-19','muk/antr',NULL,'Priyanka Store','SHOPKEEPER','Gola','7667218617','L1',2,35.00,70.00,10.00,2.00),(9,9,'2026-02-19','muk/antr',NULL,'Priyanka Store','SHOPKEEPER','Gola','7667218617','L5',1,150.00,150.00,25.00,5.00),(10,10,'2026-02-19','muk/antr',NULL,'Vijay Kirana Store','SHOPKEEPER','Gola','9931150674','L1',6,35.00,210.00,30.00,6.00),(11,11,'2026-02-19','muk/antr',NULL,'Vijay Kirana Store','SHOPKEEPER','Gola','9931150674','L500',8,25.00,200.00,40.00,4.00),(12,12,'2026-02-19','muk/antr',NULL,'Vijay Kirana Store','SHOPKEEPER','Gola','9931150674','N1',6,40.00,240.00,45.00,6.00),(13,13,'2026-02-19','muk/antr',NULL,'Vijay Kirana Store','SHOPKEEPER','Gola','9931150674','N500',4,28.00,112.00,26.00,2.00),(14,14,'2026-02-19','muk/antr',NULL,'Mehato Beej Bhandar','CUSTOMER','Gola','8789761299','N1',6,40.00,240.00,45.00,6.00),(15,15,'2026-02-19','muk/antr',NULL,'Vineet Store','CUSTOMER','Gola','8789761299','N1',6,40.00,240.00,45.00,6.00),(16,16,'2026-02-19','muk/antr',NULL,'Vineet Store','CUSTOMER','Gola','8789761299','L1',1,35.00,35.00,5.00,1.00),(17,17,'2026-02-19','muk/antr',NULL,'Kiran Collection','CUSTOMER','Gola','8210905131','L1',2,35.00,70.00,10.00,2.00),(18,18,'2026-02-19','muk/antr',NULL,'Kiran Collection','CUSTOMER','Gola','8210905131','L500',3,25.00,75.00,15.00,1.50),(19,19,'2026-02-19','muk/antr',NULL,'Kiran Collection','CUSTOMER','Gola','8210905131','N1',2,40.00,80.00,15.00,2.00),(20,20,'2026-02-19','muk/antr',NULL,'Suraj General Store','SHOPKEEPER','Gola','9716662692','N1',1,40.00,40.00,7.50,1.00),(21,21,'2026-02-19','muk/antr',NULL,'Munna Store','SHOPKEEPER','Gola','8405824492','L1',2,35.00,70.00,10.00,2.00),(22,22,'2026-02-19','muk/antr',NULL,'Munna Store','SHOPKEEPER','Gola','8405824492','N1',4,40.00,160.00,30.00,4.00),(23,23,'2026-02-19','muk/antr',NULL,'OM General Store','SHOPKEEPER','Gola','7004895934','L1',3,35.00,105.00,15.00,3.00),(24,24,'2026-02-19','muk/antr',NULL,'Sudarshan Line Hotel','CUSTOMER','Gola','997388877','N5',1,170.00,170.00,35.00,5.00),(25,25,'2026-02-19','muk/antr',NULL,'Sinha General Store','SHOPKEEPER','Gola','73591539660','N1',2,40.00,80.00,15.00,2.00),(26,26,'2026-02-19','muk/antr',NULL,'Arogya Amritulya','CUSTOMER','Gola','7004581681','N5',2,170.00,340.00,70.00,10.00),(27,27,'2026-02-19','muk/antr',NULL,'Maa Annapurna Mart','SHOPKEEPER','Gola','8789989633','L1',6,35.00,210.00,30.00,6.00),(28,28,'2026-02-19','muk/antr',NULL,'Maa Annapurna Mart','SHOPKEEPER','Gola','8789989633','L5',3,135.00,405.00,75.00,15.00),(29,29,'2026-02-19','muk/antr',NULL,'Maa Annapurna Mart','SHOPKEEPER','Gola','8789989633','N1',6,40.00,240.00,45.00,6.00),(30,30,'2026-02-19','muk/antr',NULL,'Maa Annapurna Mart','SHOPKEEPER','Gola','8789989633','N5',3,140.00,420.00,75.00,15.00),(31,3,'2026-02-22','muk/antr',NULL,'N/A','CUSTOMER','Peterwar',NULL,'N5',1,170.00,170.00,35.00,5.00),(32,2,'2026-02-22','muk/antr',NULL,'Pooja Cloth Store','CUSTOMER','Peterwar',NULL,'N5',1,170.00,170.00,35.00,5.00),(33,4,'2026-02-22','muk/antr',NULL,'N/A','CUSTOMER','Peterwar',NULL,'N1',1,60.00,60.00,17.50,1.00),(34,5,'2026-02-22','muk/antr',NULL,'Maa Vaishno Store','SHOPKEEPER','Gola','9955655024','N500',4,30.00,120.00,30.00,2.00),(35,6,'2026-02-22','muk/antr',NULL,'Jai Durgesh Dairy','SHOPKEEPER','Gola','9534016999','N1',1,40.00,40.00,7.50,1.00),(36,7,'2026-02-22','muk/antr',NULL,'Jai Durgesh Dairy','SHOPKEEPER','Gola','9534016999','L1',1,35.00,35.00,5.00,1.00),(37,8,'2026-02-22','muk/antr',NULL,'Mahesh Masala','SHOPKEEPER','Gola','9973699431','N1',1,40.00,40.00,7.50,1.00),(38,9,'2026-02-22','muk/antr',NULL,'Komal Store','SHOPKEEPER','Gola','7970736540','N1',3,40.00,120.00,22.50,3.00),(39,10,'2026-02-22','muk/antr',NULL,'Komal Store','SHOPKEEPER','Gola','7970736540','L1',3,35.00,105.00,15.00,3.00),(40,11,'2026-02-22','muk/antr',NULL,'Goltar General','SHOPKEEPER','Gola','9934102590','N1',2,40.00,80.00,15.00,2.00),(41,12,'2026-02-22','muk/antr',NULL,'Goltar General','SHOPKEEPER','Gola','9934102590','L1',2,35.00,70.00,10.00,2.00),(42,13,'2026-02-22','muk/antr',NULL,'Ram Javedesh','SHOPKEEPER','Gola','8143925945','N1',2,40.00,80.00,15.00,2.00),(43,14,'2026-02-22','muk/antr',NULL,'Saheb Cushion','SHOPKEEPER','Gola','6207771276','N1',2,40.00,80.00,15.00,2.00),(44,15,'2026-02-22','muk/antr',NULL,'Saheb Cushion','SHOPKEEPER','Gola','6207771276','L1',2,35.00,70.00,10.00,2.00),(45,16,'2026-02-22','muk/antr',NULL,'Saheb Cushion','SHOPKEEPER','Gola','6207771276','N500',3,30.00,90.00,22.50,1.50),(46,17,'2026-02-22','muk/antr',NULL,'Saheb Cushion','SHOPKEEPER','Gola','6207771276','L500',3,25.00,75.00,15.00,1.50),(47,18,'2026-02-22','muk/antr',NULL,'Hotel Akash','SHOPKEEPER','Gola','9939246521','N5',2,150.00,300.00,50.00,10.00),(48,19,'2026-02-22','muk/antr',NULL,'Balaji Center','SHOPKEEPER','Gola','9571197340','N5',1,150.00,150.00,25.00,5.00),(49,20,'2026-02-22','muk/antr',NULL,'Anand Mandal','CUSTOMER','Gola','7004207823','N5',1,170.00,170.00,35.00,5.00),(50,21,'2026-02-22','muk/antr',NULL,'Pramod','SHOPKEEPER','Peterwar','9122719122','N5',1,170.00,170.00,35.00,5.00),(51,22,'2026-02-22','muk/antr',NULL,'Santosh Store','SHOPKEEPER','Peterwar','8789549483','L1',1,35.00,35.00,5.00,1.00),(52,23,'2026-02-22','muk/antr',NULL,'Rajan Clinic Center','SHOPKEEPER','Peterwar','9113409526','N5',2,170.00,340.00,70.00,10.00),(53,1,'2026-02-23','muk/antr',NULL,'N/A','CUSTOMER','Peterwar',NULL,'N5',1,170.00,170.00,35.00,5.00),(54,2,'2026-02-23','muk/antr',NULL,'Pooja Cloth Store','CUSTOMER','Peterwar',NULL,'N5',1,170.00,170.00,35.00,5.00),(55,4,'2026-02-23','muk/antr',NULL,'N/A','CUSTOMER','Peterwar',NULL,'N1',1,60.00,60.00,17.50,1.00),(56,5,'2026-02-23','muk/antr',NULL,'Maa Vaishno Store','SHOPKEEPER','Gola','9955655024','N500',4,30.00,120.00,30.00,2.00),(57,6,'2026-02-23','muk/antr',NULL,'New Dulhan Store','SHOPKEEPER','Gola','9534016999','N1',1,40.00,40.00,7.50,1.00),(58,7,'2026-02-23','muk/antr',NULL,'New Dulhan Store','SHOPKEEPER','Gola','9534016999','L1',1,35.00,35.00,5.00,1.00),(59,8,'2026-02-23','muk/antr',NULL,'Mahesh Matka','SHOPKEEPER','Gola','9973699431','N1',1,40.00,40.00,7.50,1.00),(60,9,'2026-02-23','muk/antr',NULL,'Komal Store','SHOPKEEPER','Gola','7970736540','N1',3,40.00,120.00,22.50,3.00),(61,10,'2026-02-23','muk/antr',NULL,'Komal Store','SHOPKEEPER','Gola','7970736540','L1',3,35.00,105.00,15.00,3.00),(62,11,'2026-02-23','muk/antr',NULL,'Poddar Bhandar','SHOPKEEPER','Gola','9934102590','N1',2,40.00,80.00,15.00,2.00),(63,12,'2026-02-23','muk/antr',NULL,'Poddar Bhandar','SHOPKEEPER','Gola','9934102590','L1',2,35.00,70.00,10.00,2.00),(64,13,'2026-02-23','muk/antr',NULL,'M Javed','SHOPKEEPER','Gola','8143925945','N1',2,40.00,80.00,15.00,2.00),(65,14,'2026-02-23','muk/antr',NULL,'Saheb Hussain','SHOPKEEPER','Gola','6207771276','N1',2,40.00,80.00,15.00,2.00),(66,15,'2026-02-23','muk/antr',NULL,'Saheb Hussain','SHOPKEEPER','Gola','6207771276','L1',2,35.00,70.00,10.00,2.00),(67,16,'2026-02-23','muk/antr',NULL,'Saheb Hussain','SHOPKEEPER','Gola','6207771276','N500',3,30.00,90.00,22.50,1.50),(68,17,'2026-02-23','muk/antr',NULL,'Saheb Hussain','SHOPKEEPER','Gola','6207771276','L500',3,25.00,75.00,15.00,1.50),(69,18,'2026-02-23','muk/antr',NULL,'Hotel Akash','SHOPKEEPER','Gola','9939246521','N5',2,150.00,300.00,50.00,10.00),(70,19,'2026-02-23','muk/antr',NULL,'Kakum store','SHOPKEEPER','Gola','9571197340','N5',1,170.00,170.00,35.00,5.00),(71,20,'2026-02-23','muk/antr',NULL,'Baba Bhandari Line Hotel','CUSTOMER','Gola','7004207823','N5',1,170.00,170.00,35.00,5.00),(72,21,'2026-02-23','muk/antr',NULL,'Kunal','SHOPKEEPER','Peterwar','9122719122','N5',1,170.00,170.00,35.00,5.00),(73,22,'2026-02-23','muk/antr',NULL,'Samar Shringar Store','SHOPKEEPER','Peterwar','8789549483','L1',1,35.00,35.00,5.00,1.00),(74,23,'2026-02-23','muk/antr',NULL,'Roshan Line Hotel','SHOPKEEPER','Chitarpur','9113409526','N5',2,170.00,340.00,70.00,10.00),(75,24,'2026-02-23','muk/antr',NULL,'Roshan Line Hotel','SHOPKEEPER','Chitarpur','9113409526','L5',1,150.00,150.00,25.00,5.00),(76,25,'2026-02-23','muk/antr',NULL,'Bharat Petroleum','CUSTOMER','Chitarpur','9341674878','L1',1,50.00,50.00,12.50,1.00),(77,26,'2026-02-23','muk/antr',NULL,'Premdeep Store','SHOPKEEPER','Chitarpur','6206199123','L1',3,35.00,105.00,15.00,3.00),(78,27,'2026-02-23','muk/antr',NULL,'Premdeep Store','SHOPKEEPER','Chitarpur','6206199123','L500',2,25.00,50.00,10.00,1.00),(79,3,'2026-02-23','muk/antr',NULL,'N/A','CUSTOMER','Peterwar',NULL,'N5',1,170.00,170.00,35.00,5.00),(80,28,'2026-02-23','muk/antr',NULL,'Subhash','SHOPKEEPER','Gola','9771320489','L5',1,135.00,135.00,25.00,5.00),(81,29,'2026-02-23','muk/antr',NULL,'Ragini','SHOPKEEPER','Gola','9546741153','L1',1,35.00,35.00,5.00,1.00),(82,30,'2026-02-23','muk/antr',NULL,'Ragini','SHOPKEEPER','Gola','9546741153','N1',1,40.00,40.00,7.50,1.00),(83,31,'2026-02-23','muk/antr',NULL,'Uttam','SHOPKEEPER','Gola','6200496767','N1',2,40.00,80.00,15.00,2.00),(84,32,'2026-02-23','muk/antr',NULL,'Yashoda','SHOPKEEPER','Gola','8210618640','N1',2,40.00,80.00,15.00,2.00),(85,33,'2026-02-23','muk/antr',NULL,'Yashoda','SHOPKEEPER','Gola','8210618640','L1',2,35.00,70.00,10.00,2.00),(86,34,'2026-02-23','muk/antr',NULL,'Yashoda','SHOPKEEPER','Gola','8210618640','N500',3,30.00,90.00,22.50,1.50),(87,35,'2026-02-23','muk/antr',NULL,'Yashoda','SHOPKEEPER','Gola','8210618640','L500',3,25.00,75.00,15.00,1.50),(88,36,'2026-02-23','muk/antr',NULL,'Gopal','SHOPKEEPER','Gola','8201138397','L5',1,135.00,135.00,25.00,5.00),(89,37,'2026-02-23','muk/antr',NULL,'Gopal','SHOPKEEPER','Gola','8201138397','N1',6,40.00,240.00,45.00,6.00),(90,38,'2026-02-23','muk/antr',NULL,'Gopal','SHOPKEEPER','Gola','8201138397','N500',6,30.00,180.00,45.00,3.00),(91,1,'2026-02-24','muk/antr',NULL,'Suman Store','SHOPKEEPER','Peterwar','9241418997','L1',2,35.00,70.00,10.00,2.00),(92,2,'2026-02-24','muk/antr',NULL,'Suman Store','SHOPKEEPER','Peterwar','9241418997','N1',2,40.00,80.00,15.00,2.00),(93,3,'2026-02-24','muk/antr',NULL,'Suman Store','SHOPKEEPER','Peterwar','924148997','L500',2,25.00,50.00,10.00,1.00),(94,4,'2026-02-24','muk/antr',NULL,'Suman Store','SHOPKEEPER','Peterwar','924148997','N500',2,30.00,60.00,15.00,1.00),(95,5,'2026-02-24','muk/antr',NULL,'Happy Biryani','CUSTOMER','Peterwar','8789324627','N5',1,170.00,170.00,35.00,5.00),(96,6,'2026-02-24','muk/antr',NULL,'Bharat Petrol Pump','CUSTOMER','Lukaya','8340413277','L5',2,150.00,300.00,50.00,10.00),(97,7,'2026-02-24','muk/antr',NULL,'Sohan Kumar','SHOPKEEPER','Lukaya','7488136924','L1',3,35.00,105.00,15.00,3.00),(98,8,'2026-02-24','muk/antr',NULL,'Sahu Line Hotel','CUSTOMER','Lukaya','9006883188','L5',1,150.00,150.00,25.00,5.00),(99,9,'2026-02-24','muk/antr',NULL,'Maheshwar Store','SHOPKEEPER','Datu','6202417600','L1',1,35.00,35.00,5.00,1.00),(100,10,'2026-02-24','muk/antr',NULL,'Pihu Store','SHOPKEEPER','Datu','9006146522','L1',2,35.00,70.00,10.00,2.00),(101,11,'2026-02-24','muk/antr',NULL,'Pihu Store','SHOPKEEPER','Datu','9006146522','L500',2,25.00,50.00,10.00,1.00),(102,12,'2026-02-24','muk/antr',NULL,'Lily Store','SHOPKEEPER','Datu','6206336760','L1',1,35.00,35.00,5.00,1.00),(103,13,'2026-02-24','muk/antr',NULL,'Lily Store','SHOPKEEPER','Datu','6206336760','N1',1,40.00,40.00,7.50,1.00),(104,14,'2026-02-24','muk/antr',NULL,'Abhay Store','SHOPKEEPER','Datu','9934325242','N1',2,40.00,80.00,15.00,2.00),(105,15,'2026-02-24','muk/antr',NULL,'Laadli Store','SHOPKEEPER','Pepradih','8294525532','N1',2,40.00,80.00,15.00,2.00),(106,16,'2026-02-24','muk/antr',NULL,'Ladli Store','SHOPKEEPER','Pepradih','8294525532','L1',2,35.00,70.00,10.00,2.00),(107,17,'2026-02-24','muk/antr',NULL,'Nawal Store','SHOPKEEPER','Khetko','8709832821','N1',2,40.00,80.00,15.00,2.00),(108,18,'2026-02-24','muk/antr',NULL,'Nawal Store','SHOPKEEPER','Khetko','8709832821','L1',2,35.00,70.00,10.00,2.00),(109,19,'2026-02-24','muk/antr',NULL,'Nawal Store','SHOPKEEPER','Khetko','8709832821','N500',3,30.00,90.00,22.50,1.50),(110,20,'2026-02-24','muk/antr',NULL,'Nawal Store','SHOPKEEPER','Khetko','8709832821','L500',3,25.00,75.00,15.00,1.50),(111,21,'2026-02-24','muk/antr',NULL,'Sharat Store','SHOPKEEPER','Khetko','6205229786','L1',8,35.00,280.00,40.00,8.00),(112,22,'2026-02-24','muk/antr',NULL,'Sharat Store','SHOPKEEPER','Khetko','6205229786','L500',6,25.00,150.00,30.00,3.00),(113,23,'2026-02-24','muk/antr',NULL,'Sharat Store','SHOPKEEPER','Khetko','6205229786','N1',2,40.00,80.00,15.00,2.00),(114,24,'2026-02-24','muk/antr',NULL,'Sharat Store','SHOPKEEPER','Khetko','6205229786','N500',2,30.00,60.00,15.00,1.00),(115,25,'2026-02-24','muk/antr',NULL,'Aftabh Store','SHOPKEEPER','Khetko','6202971887','N500',1,30.00,30.00,7.50,0.50),(116,26,'2026-02-24','muk/antr',NULL,'Aftabh Store','SHOPKEEPER','Khetko','6202971887','L500',1,25.00,25.00,5.00,0.50),(117,27,'2026-02-24','muk/antr',NULL,'Kum Rate','SHOPKEEPER','Kathara','7004794433','L1',12,35.00,420.00,60.00,12.00),(118,28,'2026-02-24','muk/antr',NULL,'Kum Rate','SHOPKEEPER','Kathara','7004794433','N1',12,40.00,480.00,90.00,12.00),(119,29,'2026-02-24','muk/antr',NULL,'Amit','CUSTOMER','Kathara','','N5',1,170.00,170.00,35.00,5.00),(120,30,'2026-02-24','muk/antr',NULL,'Maa Durga Store','SHOPKEEPER','Kathara','8271309788','N500',7,30.00,210.00,52.50,3.50),(121,31,'2026-02-24','muk/antr',NULL,'Mini mall','SHOPKEEPER','Kathara','9304561127','L1',6,35.00,210.00,30.00,6.00),(122,32,'2026-02-24','muk/antr',NULL,'Mini mall','SHOPKEEPER','Kathara','9304561127','N1',6,40.00,240.00,45.00,6.00),(123,33,'2026-02-24','muk/antr',NULL,'Margo Store','SHOPKEEPER','kathara','9031400290','N1',2,40.00,80.00,15.00,2.00),(124,34,'2026-02-24','muk/antr',NULL,'Margo Store','SHOPKEEPER','kathara','9031400290','L1',2,35.00,70.00,10.00,2.00),(125,35,'2026-02-24','muk/antr',NULL,'Margo Store','SHOPKEEPER','kathara','9031400290','N500',2,30.00,60.00,15.00,1.00),(126,36,'2026-02-24','muk/antr',NULL,'Margo Store','SHOPKEEPER','kathara','9031400290','L500',2,25.00,50.00,10.00,1.00),(127,37,'2026-02-24','muk/antr',NULL,'Jharkhand Hotel','CUSTOMER','Kathara','9031611607','L5',1,150.00,150.00,25.00,5.00),(128,38,'2026-02-24','muk/antr',NULL,'Olsan 1','SHOPKEEPER','Kathara','6201025037','N1',2,40.00,80.00,15.00,2.00),(129,39,'2026-02-24','muk/antr',NULL,'Olsan 1','SHOPKEEPER','Kathara','6201025037','L1',2,35.00,70.00,10.00,2.00),(130,40,'2026-02-24','muk/antr',NULL,'Milan Store','SHOPKEEPER','Kathara','6299213681','N500',10,30.00,300.00,75.00,5.00),(131,41,'2026-02-24','muk/antr',NULL,'Man Pasand','SHOPKEEPER','Kathara','8340217470','L1',12,35.00,420.00,60.00,12.00),(132,42,'2026-02-24','muk/antr',NULL,'Khushi','SHOPKEEPER','Kathara','7488517896','L1',6,35.00,210.00,30.00,6.00),(133,43,'2026-02-24','muk/antr',NULL,'Khushi','SHOPKEEPER','Kathara','7488517896','L500',6,25.00,150.00,30.00,3.00),(134,44,'2026-02-24','muk/antr',NULL,'Khushi','SHOPKEEPER','Kathara','7488517896','N1',6,40.00,240.00,45.00,6.00),(135,45,'2026-02-24','muk/antr',NULL,'Khushi','SHOPKEEPER','Kathara','7488517896','N500',6,30.00,180.00,45.00,3.00),(142,1,'2026-02-26','munnu/mukul',NULL,'Afiya Store','SHOPKEEPER','Bahadurpur','6203964374','L5',2,135.00,270.00,50.00,10.00),(143,2,'2026-02-26','munnu/mukul',NULL,'Afiya Store','SHOPKEEPER','Bahadurpur','6203964374','L1',2,35.00,70.00,10.00,2.00),(144,3,'2026-02-26','munnu/mukul',NULL,'Afiya Store','SHOPKEEPER','Bahadurpur','6203964374','L500',3,25.00,75.00,15.00,1.50),(145,4,'2026-02-26','munnu/mukul',NULL,'Afiya Store','SHOPKEEPER','Bahadurpur','6203964374','N5',1,140.00,140.00,25.00,5.00),(146,5,'2026-02-26','munnu/mukul',NULL,'Afiya Store','SHOPKEEPER','Bahadurpur','6203964374','N1',2,40.00,80.00,15.00,2.00),(147,6,'2026-02-26','munnu/mukul',NULL,'Afiya Store','SHOPKEEPER','Bahadurpur','6203964374','N500',3,30.00,90.00,15.00,1.50),(148,7,'2026-02-26','munnu/mukul',NULL,'Mata Rani Store','SHOPKEEPER','Bahadurpur','7480020580','L1',1,35.00,35.00,5.00,1.00),(149,8,'2026-02-26','munnu/mukul',NULL,'Mata Rani Store','SHOPKEEPER','Bahadurpur','7480020580','L500',1,25.00,25.00,5.00,0.50),(150,9,'2026-02-26','munnu/mukul',NULL,'Mata Rani Store','SHOPKEEPER','Bahadurpur','7480020580','N1',1,40.00,40.00,7.50,1.00),(151,10,'2026-02-26','munnu/mukul',NULL,'Mata Rani Store','SHOPKEEPER','Bahadurpur','7480020580','N500',1,30.00,30.00,5.00,0.50),(152,11,'2026-02-26','munnu/mukul',NULL,'Hotel Swagat','CUSTOMER','Bahadurpur','9386073432','N5',1,170.00,170.00,35.00,5.00),(153,12,'2026-02-26','munnu/mukul',NULL,'Pawan Store','SHOPKEEPER','Jainamore','9304311204','L1',3,35.00,105.00,15.00,3.00),(154,13,'2026-02-26','munnu/mukul',NULL,'Pawan Store','SHOPKEEPER','Jainamore','9304311204','L500',3,25.00,75.00,15.00,1.50),(155,14,'2026-02-26','munnu/mukul',NULL,'Pawan Store','SHOPKEEPER','Jainamore','9304311204','N1',3,40.00,120.00,22.50,3.00),(156,15,'2026-02-26','munnu/mukul',NULL,'Pawan Store','SHOPKEEPER','Jainamore','9304311204','N500',3,30.00,90.00,15.00,1.50),(157,16,'2026-02-26','munnu/mukul',NULL,'Nepal Store','SHOPKEEPER','Jainamore','6201181566','N500',3,30.00,90.00,15.00,1.50),(158,17,'2026-02-26','munnu/mukul',NULL,'Nepal Store','SHOPKEEPER','Jainamore','6201181566','L500',3,25.00,75.00,15.00,1.50),(159,18,'2026-02-26','munnu/mukul',NULL,'Agarwal Store','SHOPKEEPER','Tupkadih','7909057917','N5',2,140.00,280.00,50.00,10.00),(160,19,'2026-02-26','munnu/mukul',NULL,'Agarwal Store','SHOPKEEPER','Tupkadih','7909057917','N1',6,40.00,240.00,45.00,6.00),(161,20,'2026-02-26','munnu/mukul',NULL,'Lakshmi Store','SHOPKEEPER','Tupkadih','7004954054','N1',1,40.00,40.00,7.50,1.00),(162,21,'2026-02-26','munnu/mukul',NULL,'Lakshmi Store','SHOPKEEPER','Tupkadih','7004954054','L1',1,35.00,35.00,5.00,1.00),(163,22,'2026-02-26','munnu/mukul',NULL,'Gariman Store','SHOPKEEPER','Tupkadih','9650504527','N1',3,40.00,120.00,22.50,3.00),(164,23,'2026-02-26','munnu/mukul',NULL,'Gariman Store','SHOPKEEPER','Tupkadih','9650504527','L1',3,35.00,105.00,15.00,3.00),(165,24,'2026-02-26','munnu/mukul',NULL,'Doli Store','SHOPKEEPER','Tupkadi','9304222933','N1',6,40.00,240.00,45.00,6.00),(166,25,'2026-02-26','munnu/mukul',NULL,'Doli Store','SHOPKEEPER','Tupkadi','9304222933','L1',6,35.00,210.00,30.00,6.00),(167,26,'2026-02-26','munnu/mukul',NULL,'Panchayat Store','SHOPKEEPER','Tupkadi','9229459904','L1',1,35.00,35.00,5.00,1.00),(168,27,'2026-02-26','munnu/mukul',NULL,'Panchayat Store','SHOPKEEPER','Tupkadi','9229459904','L500',2,25.00,50.00,10.00,1.00),(169,28,'2026-02-26','munnu/mukul',NULL,'Panchayat Store','SHOPKEEPER','Tupkadi','9229459904','N1',1,40.00,40.00,7.50,1.00),(170,29,'2026-02-26','munnu/mukul',NULL,'Panchayat Store','SHOPKEEPER','Tupkadi','9229459904','N500',2,30.00,60.00,10.00,1.00),(171,30,'2026-02-26','munnu/mukul',NULL,'Sunita Ladies Store','SHOPKEEPER','Tantri','9905105301','N1',3,40.00,120.00,22.50,3.00),(172,31,'2026-02-26','munnu/mukul',NULL,'Sunita Ladies Store','SHOPKEEPER','Tantri','9905105301','N500',2,30.00,60.00,10.00,1.00),(173,32,'2026-02-26','munnu/mukul',NULL,'Sunita Ladies Store','SHOPKEEPER','Tantri','9905105301','L1',3,35.00,105.00,15.00,3.00),(174,33,'2026-02-26','munnu/mukul',NULL,'Sunita Ladies Store','SHOPKEEPER','Tantri','9905105301','L500',2,25.00,50.00,10.00,1.00),(175,34,'2026-02-26','munnu/mukul',NULL,'Indian Oil','CUSTOMER','Tantri','6204116546','N5',1,170.00,170.00,35.00,5.00),(176,35,'2026-02-26','munnu/mukul',NULL,'Bharat Petroleum','CUSTOMER','Tantri','6203263943','N5',1,170.00,170.00,35.00,5.00),(177,36,'2026-02-26','munnu/mukul',NULL,'Bharat Petroleum','CUSTOMER','Tantri','6203263943','L5',1,150.00,150.00,25.00,5.00),(178,37,'2026-02-26','munnu/mukul',NULL,'Bharat Petroleum','CUSTOMER','Tantri','6203263943','N1',1,60.00,60.00,17.50,1.00),(179,38,'2026-02-26','munnu/mukul',NULL,'Indian Oil','CUSTOMER','Tupkadi','8709811873','L5',1,150.00,150.00,25.00,5.00),(180,39,'2026-02-26','munnu/mukul',NULL,'Hotel Chandra','CUSTOMER','Tupkadi','9939328151','N5',1,160.00,160.00,30.00,5.00),(181,40,'2026-02-26','munnu/mukul',NULL,'Hotel Kaveri','CUSTOMER','Tupkadi','8340656459','N5',1,170.00,170.00,35.00,5.00),(182,41,'2026-02-26','munnu/mukul',NULL,'Johar Dhaba','CUSTOMER','Tupkadi','8986789844','L5',1,150.00,150.00,25.00,5.00),(183,42,'2026-02-26','munnu/mukul',NULL,'Bharat Petroleum','CUSTOMER','Tupkadi','8210669857','L5',1,150.00,150.00,25.00,5.00),(184,43,'2026-02-26','munnu/mukul',NULL,'Bharat Petroleum','CUSTOMER','Tupkadi','8210669857','N5',1,150.00,150.00,25.00,5.00),(238,1,'2026-03-06','san/antr',NULL,'Karan Kumar','SHOPKEEPER','Lukaiaya','7631860135','N1',1,40.00,40.00,7.50,1.00),(239,2,'2026-03-06','san/antr',NULL,'Naresh Kumar','SHOPKEEPER','Uttasara','6201392356','L500',4,25.00,100.00,20.00,2.00),(240,3,'2026-03-06','san/antr',NULL,'Sri Ram Bhandar','SHOPKEEPER','Uttasara','9102063147','N1',2,40.00,80.00,15.00,2.00),(241,4,'2026-03-06','san/antr',NULL,'Sri Ram Bhandar','SHOPKEEPER','Uttasara','9102063147','L1',2,35.00,70.00,10.00,2.00),(242,5,'2026-03-06','san/antr',NULL,'Ram Das Nayak','SHOPKEEPER','Uttasara','9934346039','N1',2,40.00,80.00,15.00,2.00),(243,6,'2026-03-06','san/antr',NULL,'Ram Das Nayak','SHOPKEEPER','Uttasara','9934346039','L1',2,35.00,70.00,10.00,2.00),(244,7,'2026-03-06','san/antr',NULL,'Alpaz Khan','SHOPKEEPER','Pipradih','8969462944','N1',2,40.00,80.00,15.00,2.00),(245,8,'2026-03-06','san/antr',NULL,'Alpaz Khan','SHOPKEEPER','Pipradih','8969462944','L1',2,35.00,70.00,10.00,2.00),(246,9,'2026-03-06','san/antr',NULL,'Alpaz Khan','SHOPKEEPER','Pipradih','9169162964','N500',2,30.00,60.00,10.00,1.00),(247,10,'2026-03-06','san/antr',NULL,'Alpaz Khan','SHOPKEEPER','Pipradih','8969462944','L500',2,25.00,50.00,10.00,1.00),(248,11,'2026-03-06','san/antr',NULL,'Kishan Karmali','SHOPKEEPER','Pipradih','7903757772','N1',1,40.00,40.00,7.50,1.00),(249,12,'2026-03-06','san/antr',NULL,'Bhagwan Nayak','SHOPKEEPER','Khetko','00','N1',1,40.00,40.00,7.50,1.00),(250,13,'2026-03-06','san/antr',NULL,'Aditya Hardware','SHOPKEEPER','Khetko','9934543022','N1',12,40.00,480.00,90.00,12.00),(251,14,'2026-03-06','san/antr',NULL,'Ruchi Store','SHOPKEEPER','Khetko','7366039017','N500',1,30.00,30.00,5.00,0.50),(252,15,'2026-03-06','san/antr',NULL,'Ruchi Store','SHOPKEEPER','Khetko','7366039017','L500',1,25.00,25.00,5.00,0.50),(253,16,'2026-03-06','san/antr',NULL,'Shankar Store','SHOPKEEPER','Khetko','9766288556','L500',2,25.00,50.00,10.00,1.00),(254,17,'2026-03-06','san/antr',NULL,'Shankar Store','SHOPKEEPER','Khetko','9766288556','N500',2,30.00,60.00,10.00,1.00),(255,18,'2026-03-06','san/antr',NULL,'Shankar Store','SHOPKEEPER','Khetko','9766288556','L1',1,35.00,35.00,5.00,1.00),(256,19,'2026-03-06','san/antr',NULL,'Shankar Store','SHOPKEEPER','Khetko','9766288556','N1',1,40.00,40.00,7.50,1.00),(257,20,'2026-03-06','san/antr',NULL,'Indian Oil Petrol Pump','CUSTOMER','Kathara','7903298345','N5',1,170.00,170.00,35.00,5.00),(258,21,'2026-03-06','san/antr',NULL,'HP Petrol Pump','CUSTOMER','Kathara','9525104920','N5',1,170.00,170.00,35.00,5.00),(259,22,'2026-03-06','san/antr',NULL,'HP Petrol Pump','CUSTOMER','Kathara','9525104920','L5',1,150.00,150.00,25.00,5.00),(260,23,'2026-03-06','san/antr',NULL,'Rajiv General Store','SHOPKEEPER','BTPS','9122623760','L1',1,35.00,35.00,5.00,1.00),(261,24,'2026-03-06','san/antr',NULL,'Rajiv General Store','SHOPKEEPER','BTPS','9122623760','N1',2,40.00,80.00,15.00,2.00),(262,25,'2026-03-06','san/antr',NULL,'Rajiv General Store','SHOPKEEPER','BTPS','9122623760','L500',1,30.00,30.00,7.50,0.50),(263,26,'2026-03-06','san/antr',NULL,'Rajiv General Store','SHOPKEEPER','BTPS','9122623760','N500',1,25.00,25.00,5.00,0.50),(264,1,'2026-03-06','muku/Rahul',NULL,'Chiku','CUSTOMER','Lepo','N/A','N5',1,170.00,170.00,35.00,5.00),(265,2,'2026-03-06','muku/Rahul',NULL,'Rocky','SHOPKEEPER','Gola','6207828463','L5',1,140.00,140.00,25.00,5.00),(266,3,'2026-03-06','muku/Rahul',NULL,'Rocky','SHOPKEEPER','Gola','6207828463','L1',1,40.00,40.00,7.50,1.00),(267,4,'2026-03-06','muku/Rahul',NULL,'Gopal Store','SHOPKEEPER','Porsadih','7903088482','N500',3,30.00,90.00,15.00,1.50),(268,5,'2026-03-06','muku/Rahul',NULL,'Gopal Store','SHOPKEEPER','Porsadih','7903088482','L500',4,25.00,100.00,20.00,2.00),(269,6,'2026-03-06','muku/Rahul',NULL,'Aishani Zaiqa','CUSTOMER','Chitarpur','7909015747','N5',1,170.00,170.00,35.00,5.00),(270,7,'2026-03-06','muku/Rahul',NULL,'Ajay Store','CUSTOMER','Chitarpur','9608948413','N500',3,30.00,90.00,15.00,1.50),(271,8,'2026-03-06','muku/Rahul',NULL,'Mahadev Hotel','CUSTOMER','Kothar','N/a','L5',1,150.00,150.00,25.00,5.00),(272,9,'2026-03-06','muku/Rahul',NULL,'Mahalakshmi Hotel','CUSTOMER','Kothar','7870598969','L5',1,150.00,150.00,25.00,5.00),(273,10,'2026-03-06','muku/Rahul',NULL,'New Raj Hotel','CUSTOMER','Rajrappa','','L5',1,150.00,150.00,25.00,5.00),(274,11,'2026-03-06','muku/Rahul',NULL,'New Raj Hotel','CUSTOMER','Rajrappa','','N5',1,170.00,170.00,35.00,5.00),(275,12,'2026-03-06','muku/Rahul',NULL,'Ravi International','CUSTOMER','Rajrappa','','L5',1,150.00,150.00,25.00,5.00),(276,13,'2026-03-06','muku/Rahul',NULL,'Raj Hotel Veg','CUSTOMER','Rajrappa','','L5',1,150.00,150.00,25.00,5.00),(277,14,'2026-03-06','muku/Rahul',NULL,'Chatargee Hotel','CUSTOMER','Rajrappa','','N5',1,170.00,170.00,35.00,5.00),(278,15,'2026-03-06','muku/Rahul',NULL,'Raj Picnic','CUSTOMER','Rajrappa','','N5',1,170.00,170.00,35.00,5.00),(279,16,'2026-03-06','san/antr',NULL,'Dawat Restaurant','CUSTOMER','BTPS','7763847046','N5',1,170.00,170.00,35.00,5.00),(283,1,'2026-03-09','muk/antr',NULL,'CNG','CUSTOMER','Bhandarupar','8252722450','N5',1,170.00,170.00,35.00,5.00),(284,2,'2026-03-09','muk/antr',NULL,'CNG','CUSTOMER','Bhandarupar','8252722450','L5',1,150.00,150.00,25.00,5.00),(285,3,'2026-03-09','muk/antr',NULL,'Jeet Restaurant','CUSTOMER','Bokaro','6207410718','N5',1,170.00,170.00,35.00,5.00),(286,4,'2026-03-09','muk/antr',NULL,'Jeet Restaurant','CUSTOMER','Bokaro','6207410718','L5',1,150.00,150.00,25.00,5.00),(287,5,'2026-03-09','muk/antr',NULL,'Bhojpur Hotel-2','CUSTOMER','Bokaro','9122707559','N5',1,170.00,170.00,35.00,5.00),(288,6,'2026-03-09','muk/antr',NULL,'Bhojpur Hotel-2','CUSTOMER','Bokaro','9122707559','L5',1,150.00,150.00,25.00,5.00),(289,7,'2026-03-09','muk/antr',NULL,'Aaditya Hospital','CUSTOMER','Chas','7004227005','N5',1,170.00,170.00,35.00,5.00),(290,8,'2026-03-09','muk/antr',NULL,'Aaditya Hospital','CUSTOMER','Chas','7004227005','L5',1,150.00,150.00,25.00,5.00),(291,10,'2026-03-09','muk/antr',NULL,'The Signature Restaurant','CUSTOMER','Chas','8789148594','N5',2,170.00,340.00,70.00,10.00),(292,11,'2026-03-09','muk/antr',NULL,'Reyansh motor','CUSTOMER','Chas','9507032358','N5',1,170.00,170.00,35.00,5.00),(293,12,'2026-03-09','muk/antr',NULL,'Reyansh motor','CUSTOMER','Chas','9507032358','N1',1,60.00,60.00,17.50,1.00),(294,13,'2026-03-09','muk/antr',NULL,'Reyansh motor','CUSTOMER','Chas','9507032358','L1',1,55.00,55.00,15.00,1.00),(295,13,'2026-03-09','muk/antr',NULL,'Jio Petrol Pump','CUSTOMER','Chas','9263949119','N5',1,170.00,170.00,35.00,5.00),(296,14,'2026-03-09','muk/antr',NULL,'Jio Petrol Pump','CUSTOMER','Chas','9263949119','L5',1,150.00,150.00,25.00,5.00),(297,15,'2026-03-09','muk/antr',NULL,'Bhojpur Hotel','CUSTOMER','Chas','8797768824','L5',1,150.00,150.00,25.00,5.00),(298,17,'2026-03-09','muk/antr',NULL,'Nayara Petrol Pump','CUSTOMER','Chas','7463065184','L5',1,150.00,150.00,25.00,5.00),(299,18,'2026-03-09','muk/antr',NULL,'Nayara Petrol Pump','CUSTOMER','Chas','7463065184','L1',1,50.00,50.00,12.50,1.00),(300,19,'2026-03-09','muk/antr',NULL,'Kia','CUSTOMER','Chas','9708529284','N5',1,170.00,170.00,35.00,5.00),(301,20,'2026-03-09','muk/antr',NULL,'Kia','CUSTOMER','Chas','9708529284','L5',1,150.00,150.00,25.00,5.00),(302,21,'2026-03-09','muk/antr',NULL,'Shital Kumari','CUSTOMER','Chas','9279251869','L5',1,150.00,150.00,25.00,5.00),(303,22,'2026-03-09','muk/antr',NULL,'Hotel Family','CUSTOMER','Chas','9204382533','L5',1,150.00,150.00,25.00,5.00),(304,23,'2026-03-09','muk/antr',NULL,'Caffe Hospital','CUSTOMER','Chas','6200506406','N5',1,170.00,170.00,35.00,5.00);
/*!40000 ALTER TABLE `daily_sale_record` ENABLE KEYS */;
UNLOCK TABLES;

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
  `total_revenue` decimal(38,2) NOT NULL,
  `total_agent_commission` decimal(38,2) NOT NULL,
  `total_expense` decimal(38,2) NOT NULL,
  `material_cost` decimal(38,2) NOT NULL,
  `net_profit` decimal(38,2) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `volume_sold` decimal(12,2) DEFAULT NULL,
  `total_quantity` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_daily_summary_salesman_date` (`salesman_alias`,`sale_date`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `daily_summary`
--

LOCK TABLES `daily_summary` WRITE;
/*!40000 ALTER TABLE `daily_summary` DISABLE KEYS */;
INSERT INTO `daily_summary` VALUES (1,'muk/antr','2026-02-23',4275.00,835.00,600.00,2015.00,825.00,'2026-02-24 01:18:31','2026-02-26 06:48:51',116.00,76),(2,'muk/antr','2026-02-24',6175.00,1125.00,700.00,2974.00,1376.00,'2026-02-24 04:26:22','2026-02-26 06:48:35',157.50,161),(4,'munnu/mukul','2026-02-26',4850.00,872.50,549.00,2348.00,1080.50,'2026-02-26 01:22:13','2026-02-26 10:14:22',136.00,90),(5,'muk/antr','2026-02-22',2570.00,497.50,500.00,1210.00,362.50,'2026-02-27 04:26:34','2026-02-27 04:26:34',71.00,40),(7,'muku/Rahul','2026-03-06',2060.00,382.50,700.00,961.00,16.50,'2026-03-06 01:30:33','2026-03-08 11:29:50',61.00,22),(8,'san/antr','2026-03-06',2330.00,432.50,500.00,1101.00,296.50,'2026-03-08 11:29:50','2026-03-08 12:05:16',60.00,52),(9,'muk/antr','2026-02-19',4672.00,866.00,500.00,2277.00,1029.00,'2026-03-08 11:29:50','2026-03-09 05:20:17',130.50,93),(11,'muk/antr','2026-03-09',3365.00,645.00,700.00,1545.00,475.00,'2026-03-09 02:34:57','2026-03-09 09:58:33',103.00,23);
/*!40000 ALTER TABLE `daily_summary` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `product_cost_manual`
--

LOCK TABLES `product_cost_manual` WRITE;
/*!40000 ALTER TABLE `product_cost_manual` DISABLE KEYS */;
INSERT INTO `product_cost_manual` VALUES (1,13.00,'2026-02-23 16:52:06.414719','N500','Floor Cleaner','2026-02-23 16:52:06.414719',NULL,'lit',0.50,'Neem 500ml'),(2,12.00,'2026-02-23 16:52:32.253566','L500','Floor Cleaner','2026-02-23 16:52:32.253566',NULL,'lit',0.50,'Lemon 500ml'),(3,19.00,'2026-02-23 16:52:48.612061','N1','Floor Cleaner','2026-02-23 16:52:48.612061',NULL,'lit',1.00,'Neem 1L'),(4,18.00,'2026-02-23 16:53:07.929918','L1','Floor Cleaner','2026-02-23 16:53:07.929918',NULL,'lit',1.00,'Lemon 1L'),(5,72.00,'2026-02-23 16:53:27.103690','L5','Floor Cleaner','2026-02-23 16:53:27.103690',NULL,'lit',5.00,'Lemon 5L'),(6,77.00,'2026-02-23 16:53:40.881055','N5','Floor Cleaner','2026-02-23 16:53:40.881055',NULL,'lit',5.00,'Neem 5L');
/*!40000 ALTER TABLE `product_cost_manual` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `product_recipes`
--

LOCK TABLES `product_recipes` WRITE;
/*!40000 ALTER TABLE `product_recipes` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_recipes` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `production_batches`
--

LOCK TABLES `production_batches` WRITE;
/*!40000 ALTER TABLE `production_batches` DISABLE KEYS */;
/*!40000 ALTER TABLE `production_batches` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `route_villages`
--

LOCK TABLES `route_villages` WRITE;
/*!40000 ALTER TABLE `route_villages` DISABLE KEYS */;
/*!40000 ALTER TABLE `route_villages` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `routes`
--

LOCK TABLES `routes` WRITE;
/*!40000 ALTER TABLE `routes` DISABLE KEYS */;
/*!40000 ALTER TABLE `routes` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `sales_records`
--

LOCK TABLES `sales_records` WRITE;
/*!40000 ALTER TABLE `sales_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `sales_records` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salesman_expenses`
--

LOCK TABLES `salesman_expenses` WRITE;
/*!40000 ALTER TABLE `salesman_expenses` DISABLE KEYS */;
INSERT INTO `salesman_expenses` VALUES (5,1,'2026-02-23','Other',300.00,'2026-02-23 05:52:46'),(6,1,'2026-02-23','Other',500.00,'2026-02-23 05:52:46'),(7,1,'2026-02-24','Other',300.00,'2026-02-24 01:55:36'),(8,1,'2026-02-24','Food',300.00,'2026-02-24 01:55:36'),(9,2,'2026-02-26','Food',200.00,'2026-02-26 01:09:16'),(10,2,'2026-02-26','Other',300.00,'2026-02-26 01:09:16'),(16,4,'2026-03-06','Food',200.00,'2026-03-06 01:30:04'),(17,5,'2026-03-06','Food',200.00,'2026-03-06 06:11:22'),(18,5,'2026-03-06','Other',500.00,'2026-03-06 06:11:22'),(19,4,'2026-03-06','Other',200.00,'2026-03-06 10:05:05'),(20,1,'2026-03-09','Other',500.00,'2026-03-09 00:31:41'),(21,1,'2026-03-09','Food',200.00,'2026-03-09 00:31:41');
/*!40000 ALTER TABLE `salesman_expenses` ENABLE KEYS */;
UNLOCK TABLES;

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
  `delta_qty` int NOT NULL,
  `remarks` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `insert_month` varchar(7) GENERATED ALWAYS AS (DATE_FORMAT(`created_at`, '%Y-%m')) STORED,
  `created_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`salesman_ledger_id`),
  KEY `idx_sl_insert_month_salesman_created` (`insert_month`,`salesman_alias`,`created_at`),
  KEY `idx_sl_salesman_product_created` (`salesman_alias`,`product_code`,`created_at`),
  KEY `idx_sl_type_created` (`txn_type`,`created_at`),
  KEY `fk_sl_product_code` (`product_code`),
  CONSTRAINT `fk_sl_product_code` FOREIGN KEY (`product_code`) REFERENCES `product_cost_manual` (`product_code`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_sl_salesman_alias` FOREIGN KEY (`salesman_alias`) REFERENCES `salesmen` (`alias`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=136 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `salesman_ledger`
--

LOCK TABLES `salesman_ledger` WRITE;
/*!40000 ALTER TABLE `salesman_ledger` DISABLE KEYS */;
INSERT INTO `salesman_ledger` VALUES (34,'san/antr','L1','ISSUE_FROM_WAREHOUSE',54,'Issued from warehouse. Salesman stock before: 0 after: 54','2026-03-05 23:12:51',NULL),(35,'san/antr','L500','ISSUE_FROM_WAREHOUSE',40,'Issued from warehouse. Salesman stock before: 0 after: 40','2026-03-05 23:12:51',NULL),(36,'san/antr','L5','ISSUE_FROM_WAREHOUSE',6,'Issued from warehouse. Salesman stock before: 0 after: 6','2026-03-05 23:12:52',NULL),(37,'san/antr','N1','ISSUE_FROM_WAREHOUSE',48,'Issued from warehouse. Salesman stock before: 0 after: 48','2026-03-05 23:12:52',NULL),(38,'san/antr','N500','ISSUE_FROM_WAREHOUSE',40,'Issued from warehouse. Salesman stock before: 0 after: 40','2026-03-05 23:12:52',NULL),(39,'san/antr','N5','ISSUE_FROM_WAREHOUSE',6,'Issued from warehouse. Salesman stock before: 0 after: 6','2026-03-05 23:12:53',NULL),(40,'muku/Rahul','L1','ISSUE_FROM_WAREHOUSE',72,'Issued from warehouse. Salesman stock before: 0 after: 72','2026-03-05 23:35:22',NULL),(41,'muku/Rahul','L500','ISSUE_FROM_WAREHOUSE',40,'Issued from warehouse. Salesman stock before: 0 after: 40','2026-03-05 23:35:22',NULL),(42,'muku/Rahul','L5','ISSUE_FROM_WAREHOUSE',10,'Issued from warehouse. Salesman stock before: 0 after: 10','2026-03-05 23:35:22',NULL),(43,'muku/Rahul','N1','ISSUE_FROM_WAREHOUSE',60,'Issued from warehouse. Salesman stock before: 0 after: 60','2026-03-05 23:35:23',NULL),(44,'muku/Rahul','N500','ISSUE_FROM_WAREHOUSE',44,'Issued from warehouse. Salesman stock before: 0 after: 44','2026-03-05 23:35:23',NULL),(45,'muku/Rahul','N5','ISSUE_FROM_WAREHOUSE',10,'Issued from warehouse. Salesman stock before: 0 after: 10','2026-03-05 23:35:23',NULL),(46,'san/antr','N1','SOLD',-1,'Sale created, saleId=238','2026-03-06 01:30:04','system'),(47,'san/antr','L500','SOLD',-4,'Sale created, saleId=239','2026-03-06 01:30:04','system'),(48,'san/antr','N1','SOLD',-2,'Sale created, saleId=240','2026-03-06 01:30:04','system'),(49,'san/antr','L1','SOLD',-2,'Sale created, saleId=241','2026-03-06 01:30:04','system'),(50,'san/antr','N1','SOLD',-2,'Sale created, saleId=242','2026-03-06 01:30:04','system'),(51,'san/antr','L1','SOLD',-2,'Sale created, saleId=243','2026-03-06 01:30:04','system'),(52,'san/antr','N1','SOLD',-2,'Sale created, saleId=244','2026-03-06 01:30:04','system'),(53,'san/antr','L1','SOLD',-2,'Sale created, saleId=245','2026-03-06 01:30:04','system'),(54,'san/antr','N500','SOLD',-2,'Sale created, saleId=246','2026-03-06 01:30:04','system'),(55,'san/antr','L500','SOLD',-2,'Sale created, saleId=247','2026-03-06 01:30:04','system'),(56,'san/antr','N1','SOLD',-1,'Sale created, saleId=248','2026-03-06 01:30:04','system'),(57,'san/antr','N1','SOLD',-1,'Sale created, saleId=249','2026-03-06 02:00:48','system'),(58,'san/antr','N1','SOLD',-12,'Sale created, saleId=250','2026-03-06 02:00:48','system'),(59,'san/antr','N500','SOLD',-1,'Sale created, saleId=251','2026-03-06 06:00:26','system'),(60,'san/antr','L500','SOLD',-1,'Sale created, saleId=252','2026-03-06 06:00:26','system'),(61,'san/antr','L500','SOLD',-2,'Sale created, saleId=253','2026-03-06 06:00:26','system'),(62,'san/antr','N500','SOLD',-2,'Sale created, saleId=254','2026-03-06 06:00:26','system'),(63,'san/antr','L1','SOLD',-1,'Sale created, saleId=255','2026-03-06 06:00:26','system'),(64,'san/antr','N1','SOLD',-1,'Sale created, saleId=256','2026-03-06 06:00:26','system'),(65,'san/antr','N5','SOLD',-1,'Sale created, saleId=257','2026-03-06 06:00:26','system'),(66,'san/antr','N5','SOLD',-1,'Sale created, saleId=258','2026-03-06 06:00:26','system'),(67,'san/antr','L5','SOLD',-1,'Sale created, saleId=259','2026-03-06 06:00:26','system'),(68,'san/antr','L1','SOLD',-1,'Sale created, saleId=260','2026-03-06 06:00:26','system'),(69,'san/antr','N1','SOLD',-2,'Sale created, saleId=261','2026-03-06 06:00:26','system'),(70,'san/antr','L500','SOLD',-1,'Sale created, saleId=262','2026-03-06 06:00:26','system'),(71,'san/antr','N500','SOLD',-1,'Sale created, saleId=263','2026-03-06 06:00:26','system'),(72,'muku/Rahul','N5','SOLD',-1,'Sale created, saleId=264','2026-03-06 06:11:22','system'),(73,'muku/Rahul','L5','SOLD',-1,'Sale created, saleId=265','2026-03-06 06:11:22','system'),(74,'muku/Rahul','L1','SOLD',-1,'Sale created, saleId=266','2026-03-06 06:11:22','system'),(75,'muku/Rahul','N500','SOLD',-3,'Sale created, saleId=267','2026-03-06 06:11:22','system'),(76,'muku/Rahul','L500','SOLD',-4,'Sale created, saleId=268','2026-03-06 06:11:22','system'),(77,'muku/Rahul','N5','SOLD',-1,'Sale created, saleId=269','2026-03-06 06:11:22','system'),(78,'muku/Rahul','N500','SOLD',-3,'Sale created, saleId=270','2026-03-06 06:11:22','system'),(79,'muku/Rahul','L5','SOLD',-1,'Sale created, saleId=271','2026-03-06 06:11:22','system'),(80,'muku/Rahul','L5','SOLD',-1,'Sale created, saleId=272','2026-03-06 06:11:22','system'),(81,'muku/Rahul','L5','SOLD',-1,'Sale created, saleId=273','2026-03-06 08:13:26','system'),(82,'muku/Rahul','N5','SOLD',-1,'Sale created, saleId=274','2026-03-06 08:13:26','system'),(83,'muku/Rahul','L5','SOLD',-1,'Sale created, saleId=275','2026-03-06 08:13:26','system'),(84,'muku/Rahul','L5','SOLD',-1,'Sale created, saleId=276','2026-03-06 08:13:26','system'),(85,'muku/Rahul','N5','SOLD',-1,'Sale created, saleId=277','2026-03-06 08:13:26','system'),(86,'muku/Rahul','N5','SOLD',-1,'Sale created, saleId=278','2026-03-06 08:13:26','system'),(87,'san/antr','N5','SOLD',-1,'Sale created, saleId=279','2026-03-06 10:05:05','system'),(88,'muku/Rahul','L1','RETURN_TO_WAREHOUSE',-71,'Returned to warehouse. Salesman stock before: 71 after: 0','2026-03-06 12:00:12',NULL),(89,'muku/Rahul','L500','RETURN_TO_WAREHOUSE',-36,'Returned to warehouse. Salesman stock before: 36 after: 0','2026-03-06 12:00:12',NULL),(90,'muku/Rahul','L5','RETURN_TO_WAREHOUSE',-4,'Returned to warehouse. Salesman stock before: 4 after: 0','2026-03-06 12:00:13',NULL),(91,'muku/Rahul','N1','RETURN_TO_WAREHOUSE',-60,'Returned to warehouse. Salesman stock before: 60 after: 0','2026-03-06 12:00:13',NULL),(92,'muku/Rahul','N500','RETURN_TO_WAREHOUSE',-38,'Returned to warehouse. Salesman stock before: 38 after: 0','2026-03-06 12:00:13',NULL),(93,'muku/Rahul','N5','RETURN_TO_WAREHOUSE',-5,'Returned to warehouse. Salesman stock before: 5 after: 0','2026-03-06 12:00:13',NULL),(94,'san/antr','L1','RETURN_TO_WAREHOUSE',-46,'Returned to warehouse. Salesman stock before: 46 after: 0','2026-03-09 00:28:15',NULL),(95,'san/antr','L500','RETURN_TO_WAREHOUSE',-30,'Returned to warehouse. Salesman stock before: 30 after: 0','2026-03-09 00:28:15',NULL),(96,'san/antr','L5','RETURN_TO_WAREHOUSE',-5,'Returned to warehouse. Salesman stock before: 5 after: 0','2026-03-09 00:28:16',NULL),(97,'san/antr','N1','RETURN_TO_WAREHOUSE',-24,'Returned to warehouse. Salesman stock before: 24 after: 0','2026-03-09 00:28:16',NULL),(98,'san/antr','N500','RETURN_TO_WAREHOUSE',-34,'Returned to warehouse. Salesman stock before: 34 after: 0','2026-03-09 00:28:16',NULL),(99,'san/antr','N5','RETURN_TO_WAREHOUSE',-3,'Returned to warehouse. Salesman stock before: 3 after: 0','2026-03-09 00:28:16',NULL),(100,'muk/antr','L1','ISSUE_FROM_WAREHOUSE',50,'Issued from warehouse. Salesman stock before: 0 after: 50','2026-03-09 00:29:59',NULL),(101,'muk/antr','L500','ISSUE_FROM_WAREHOUSE',20,'Issued from warehouse. Salesman stock before: 0 after: 20','2026-03-09 00:29:59',NULL),(102,'muk/antr','L5','ISSUE_FROM_WAREHOUSE',12,'Issued from warehouse. Salesman stock before: 0 after: 12','2026-03-09 00:29:59',NULL),(103,'muk/antr','N1','ISSUE_FROM_WAREHOUSE',50,'Issued from warehouse. Salesman stock before: 0 after: 50','2026-03-09 00:29:59',NULL),(104,'muk/antr','N500','ISSUE_FROM_WAREHOUSE',20,'Issued from warehouse. Salesman stock before: 0 after: 20','2026-03-09 00:29:59',NULL),(105,'muk/antr','N5','ISSUE_FROM_WAREHOUSE',12,'Issued from warehouse. Salesman stock before: 0 after: 12','2026-03-09 00:30:00',NULL),(106,'muk/antr','N5','SOLD',-1,'Sale created, saleId=283','2026-03-09 02:32:38','system'),(107,'muk/antr','L5','SOLD',-1,'Sale created, saleId=284','2026-03-09 02:32:39','system'),(108,'muk/antr','N5','SOLD',-1,'Sale created, saleId=285','2026-03-09 02:32:39','system'),(109,'muk/antr','L5','SOLD',-1,'Sale created, saleId=286','2026-03-09 02:32:39','system'),(110,'muk/antr','N5','SOLD',-1,'Sale created, saleId=287','2026-03-09 02:32:39','system'),(111,'muk/antr','L5','SOLD',-1,'Sale created, saleId=288','2026-03-09 02:32:39','system'),(112,'muk/antr','N5','SOLD',-1,'Sale created, saleId=289','2026-03-09 02:32:39','system'),(113,'muk/antr','L5','SOLD',-1,'Sale created, saleId=290','2026-03-09 02:32:39','system'),(114,'muk/antr','N5','SOLD',-1,'Sale created, saleId=291','2026-03-09 03:15:14','system'),(115,'muk/antr','N5','SOLD',-1,'Sale created, saleId=292','2026-03-09 03:15:14','system'),(116,'muk/antr','N1','SOLD',-1,'Sale created, saleId=293','2026-03-09 03:15:14','system'),(117,'muk/antr','L1','SOLD',-1,'Sale created, saleId=294','2026-03-09 03:15:14','system'),(118,'muk/antr','N5','SOLD',1,'Sale updated (stock reversal), saleId=291','2026-03-09 04:13:04','system'),(119,'muk/antr','N5','SOLD',-2,'Sale updated, saleId=291','2026-03-09 04:13:04','system'),(120,'muk/antr','N5','SOLD',-1,'Sale created, saleId=295','2026-03-09 04:15:13','system'),(121,'muk/antr','L5','SOLD',-1,'Sale created, saleId=296','2026-03-09 04:15:13','system'),(122,'muk/antr','L5','SOLD',-1,'Sale created, saleId=297','2026-03-09 04:15:14','system'),(123,'muk/antr','L5','SOLD',-1,'Sale created, saleId=298','2026-03-09 05:02:23','system'),(124,'muk/antr','L1','SOLD',-1,'Sale created, saleId=299','2026-03-09 05:02:23','system'),(125,'muk/antr','N5','SOLD',-1,'Sale created, saleId=300','2026-03-09 05:02:23','system'),(126,'muk/antr','L5','SOLD',-1,'Sale created, saleId=301','2026-03-09 05:02:23','system'),(127,'muk/antr','L5','SOLD',-1,'Sale created, saleId=302','2026-03-09 09:58:32','system'),(128,'muk/antr','L5','SOLD',-1,'Sale created, saleId=303','2026-03-09 09:58:33','system'),(129,'muk/antr','N5','SOLD',-1,'Sale created, saleId=304','2026-03-09 09:58:33','system'),(130,'muk/antr','L1','RETURN_TO_WAREHOUSE',-48,'Returned to warehouse. Salesman stock before: 48 after: 0','2026-03-09 10:11:21',NULL),(131,'muk/antr','L500','RETURN_TO_WAREHOUSE',-20,'Returned to warehouse. Salesman stock before: 20 after: 0','2026-03-09 10:11:21',NULL),(132,'muk/antr','L5','RETURN_TO_WAREHOUSE',-2,'Returned to warehouse. Salesman stock before: 2 after: 0','2026-03-09 10:11:22',NULL),(133,'muk/antr','N1','RETURN_TO_WAREHOUSE',-49,'Returned to warehouse. Salesman stock before: 49 after: 0','2026-03-09 10:11:22',NULL),(134,'muk/antr','N500','RETURN_TO_WAREHOUSE',-20,'Returned to warehouse. Salesman stock before: 20 after: 0','2026-03-09 10:11:22',NULL),(135,'muk/antr','N5','RETURN_TO_WAREHOUSE',-2,'Returned to warehouse. Salesman stock before: 2 after: 0','2026-03-09 10:11:22',NULL);
/*!40000 ALTER TABLE `salesman_ledger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warehouse_ledger`
--

DROP TABLE IF EXISTS `warehouse_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouse_ledger` (
  `warehouse_ledger_id` bigint NOT NULL AUTO_INCREMENT,
  `product_code` varchar(255) NOT NULL,
  `txn_type` enum('TRANSFER_IN','ISSUE_TO_SALESMAN','RETURN_FROM_SALESMAN','MANUAL_ADJUST','DAMAGE') NOT NULL,
  `delta_qty` int NOT NULL COMMENT 'Positive for additions, negative for removals',
  `qty_before` int NOT NULL,
  `qty_after` int NOT NULL,
  `salesman_alias` varchar(255) DEFAULT NULL,
  `remarks` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `insert_month` varchar(7) GENERATED ALWAYS AS (DATE_FORMAT(`created_at`, '%Y-%m')) STORED,
  `created_by` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`warehouse_ledger_id`),
  KEY `idx_ledger_insert_month_product_code` (`insert_month`,`product_code`),
  KEY `idx_ledger_product_code` (`product_code`),
  KEY `idx_ledger_txn_type` (`txn_type`),
  KEY `idx_ledger_salesman` (`salesman_alias`),
  KEY `idx_ledger_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=221 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Audit trail for all warehouse stock movements';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warehouse_ledger`
--

LOCK TABLES `warehouse_ledger` WRITE;
/*!40000 ALTER TABLE `warehouse_ledger` DISABLE KEYS */;
INSERT INTO `warehouse_ledger` VALUES (179,'N500','TRANSFER_IN',86,0,86,NULL,'Initial Entry','2026-03-05 11:11:23',NULL),(180,'N1','TRANSFER_IN',147,0,147,NULL,'Initial Entry','2026-03-05 11:11:44',NULL),(181,'N5','TRANSFER_IN',30,0,30,NULL,'Initial Entry','2026-03-05 11:11:58',NULL),(182,'L500','TRANSFER_IN',253,0,253,NULL,'Initial Entry','2026-03-05 11:13:17',NULL),(183,'L1','TRANSFER_IN',244,0,244,NULL,'Initial Entry','2026-03-05 11:13:33',NULL),(184,'L5','TRANSFER_IN',19,0,19,NULL,'Initial Entry','2026-03-05 11:14:25',NULL),(185,'L1','ISSUE_TO_SALESMAN',-54,244,190,'san/antr','Issue stock to salesman on 06-03-2026','2026-03-05 23:12:51',NULL),(186,'L500','ISSUE_TO_SALESMAN',-40,253,213,'san/antr','Issue stock to salesman on 06-03-2026','2026-03-05 23:12:51',NULL),(187,'L5','ISSUE_TO_SALESMAN',-6,19,13,'san/antr','Issue stock to salesman on 06-03-2026','2026-03-05 23:12:52',NULL),(188,'N1','ISSUE_TO_SALESMAN',-48,147,99,'san/antr','Issue stock to salesman on 06-03-2026','2026-03-05 23:12:52',NULL),(189,'N500','ISSUE_TO_SALESMAN',-40,86,46,'san/antr','Issue stock to salesman on 06-03-2026','2026-03-05 23:12:52',NULL),(190,'N5','ISSUE_TO_SALESMAN',-6,30,24,'san/antr','Issue stock to salesman on 06-03-2026','2026-03-05 23:12:53',NULL),(191,'L1','ISSUE_TO_SALESMAN',-72,190,118,'muku/Rahul','Issue stock to salesman on 06-03-2026','2026-03-05 23:35:22',NULL),(192,'L500','ISSUE_TO_SALESMAN',-40,213,173,'muku/Rahul','Issue stock to salesman on 06-03-2026','2026-03-05 23:35:22',NULL),(193,'L5','ISSUE_TO_SALESMAN',-10,13,3,'muku/Rahul','Issue stock to salesman on 06-03-2026','2026-03-05 23:35:22',NULL),(194,'N1','ISSUE_TO_SALESMAN',-60,99,39,'muku/Rahul','Issue stock to salesman on 06-03-2026','2026-03-05 23:35:23',NULL),(195,'N500','ISSUE_TO_SALESMAN',-44,46,2,'muku/Rahul','Issue stock to salesman on 06-03-2026','2026-03-05 23:35:23',NULL),(196,'N5','ISSUE_TO_SALESMAN',-10,24,14,'muku/Rahul','Issue stock to salesman on 06-03-2026','2026-03-05 23:35:23',NULL),(197,'L1','RETURN_FROM_SALESMAN',71,118,189,'muku/Rahul','Return stock from salesman on 06-03-2026','2026-03-06 12:00:12',NULL),(198,'L500','RETURN_FROM_SALESMAN',36,173,209,'muku/Rahul','Return stock from salesman on 06-03-2026','2026-03-06 12:00:12',NULL),(199,'L5','RETURN_FROM_SALESMAN',4,3,7,'muku/Rahul','Return stock from salesman on 06-03-2026','2026-03-06 12:00:13',NULL),(200,'N1','RETURN_FROM_SALESMAN',60,39,99,'muku/Rahul','Return stock from salesman on 06-03-2026','2026-03-06 12:00:13',NULL),(201,'N500','RETURN_FROM_SALESMAN',38,2,40,'muku/Rahul','Return stock from salesman on 06-03-2026','2026-03-06 12:00:13',NULL),(202,'N5','RETURN_FROM_SALESMAN',5,14,19,'muku/Rahul','Return stock from salesman on 06-03-2026','2026-03-06 12:00:13',NULL),(203,'L1','RETURN_FROM_SALESMAN',46,189,235,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 00:28:15',NULL),(204,'L500','RETURN_FROM_SALESMAN',30,209,239,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 00:28:15',NULL),(205,'L5','RETURN_FROM_SALESMAN',5,7,12,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 00:28:16',NULL),(206,'N1','RETURN_FROM_SALESMAN',24,99,123,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 00:28:16',NULL),(207,'N500','RETURN_FROM_SALESMAN',34,40,74,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 00:28:16',NULL),(208,'N5','RETURN_FROM_SALESMAN',3,19,22,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 00:28:17',NULL),(209,'L1','ISSUE_TO_SALESMAN',-50,235,185,'muk/antr','Issue stock to salesman on 09-03-2026','2026-03-09 00:29:59',NULL),(210,'L500','ISSUE_TO_SALESMAN',-20,239,219,'muk/antr','Issue stock to salesman on 09-03-2026','2026-03-09 00:29:59',NULL),(211,'L5','ISSUE_TO_SALESMAN',-12,12,0,'muk/antr','Issue stock to salesman on 09-03-2026','2026-03-09 00:29:59',NULL),(212,'N1','ISSUE_TO_SALESMAN',-50,123,73,'muk/antr','Issue stock to salesman on 09-03-2026','2026-03-09 00:29:59',NULL),(213,'N500','ISSUE_TO_SALESMAN',-20,74,54,'muk/antr','Issue stock to salesman on 09-03-2026','2026-03-09 00:29:59',NULL),(214,'N5','ISSUE_TO_SALESMAN',-12,22,10,'muk/antr','Issue stock to salesman on 09-03-2026','2026-03-09 00:29:59',NULL),(215,'L1','RETURN_FROM_SALESMAN',48,185,233,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 10:11:21',NULL),(216,'L500','RETURN_FROM_SALESMAN',20,219,239,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 10:11:21',NULL),(217,'L5','RETURN_FROM_SALESMAN',2,0,2,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 10:11:22',NULL),(218,'N1','RETURN_FROM_SALESMAN',49,73,122,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 10:11:22',NULL),(219,'N500','RETURN_FROM_SALESMAN',20,54,74,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 10:11:22',NULL),(220,'N5','RETURN_FROM_SALESMAN',2,10,12,'muk/antr','Return stock from salesman on 09-03-2026','2026-03-09 10:11:22',NULL);
/*!40000 ALTER TABLE `warehouse_ledger` ENABLE KEYS */;
UNLOCK TABLES;

/*
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
-- Dumping data for table `warehouse_ledger_archive`
--

LOCK TABLES `warehouse_ledger_archive` WRITE;
/*!40000 ALTER TABLE `warehouse_ledger_archive` DISABLE KEYS */;
/*!40000 ALTER TABLE `warehouse_ledger_archive` ENABLE KEYS */;
UNLOCK TABLES;
*/

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

--
-- Dumping data for table `warehouses`
--

LOCK TABLES `warehouses` WRITE;
/*!40000 ALTER TABLE `warehouses` DISABLE KEYS */;
/*!40000 ALTER TABLE `warehouses` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-13 23:09:57
