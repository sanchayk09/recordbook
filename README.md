# RecordBook – Sales & Inventory Management System

**RecordBook** is a full-stack web application built for **UrviClean PVT LTD**, a cleaning-product manufacturer and distributor. It records daily sales, manages warehouse and salesman-level stock, tracks operational expenses, calculates agent commissions, and produces profit/loss summaries.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Business Domain](#2-business-domain)
3. [Product Catalogue](#3-product-catalogue)
4. [Core Business Rules](#4-core-business-rules)
   - [Commission Calculation](#41-commission-calculation)
   - [Volume Calculation](#42-volume-calculation)
   - [Profit Calculation](#43-profit-calculation)
5. [Stock Flow: Warehouse → Salesman → Customer](#5-stock-flow-warehouse--salesman--customer)
6. [Data Model (Database Tables)](#6-data-model-database-tables)
7. [Backend – Spring Boot](#7-backend--spring-boot)
   - [Services](#71-services)
   - [REST API Endpoints](#72-rest-api-endpoints)
8. [Frontend – React](#8-frontend--react)
9. [Running Locally](#9-running-locally)

---

## 1. Architecture Overview

```
┌──────────────────────────────────────────────────────┐
│  React Frontend  (recordbook-frontend, port 3000)    │
│  Pages: Dashboard · Sales Dump · Warehouse · Reports │
└───────────────────────┬──────────────────────────────┘
                        │ REST / JSON
┌───────────────────────▼──────────────────────────────┐
│  Spring Boot Backend  (recordbook, port 8080)        │
│  Controllers → Services → JPA Repositories           │
│  Swagger UI: /swagger-ui.html                        │
└───────────────────────┬──────────────────────────────┘
                        │ JDBC / Hibernate
┌───────────────────────▼──────────────────────────────┐
│  MySQL 8 Database  (urviclean)                       │
└──────────────────────────────────────────────────────┘
```

- **Backend**: Java 17, Spring Boot, Spring Data JPA, Hibernate, Swagger/OpenAPI 3
- **Frontend**: React, React Router, Axios, CSS modules
- **Database**: MySQL 8, managed via `ddl-auto: update` (Hibernate) + `createtable.sql`

---

## 2. Business Domain

UrviClean manufactures cleaning-product liquids in-house and distributes them through a field salesforce. The business cycle is:

1. **Production** – Raw chemicals are purchased from vendors, stored in a warehouse, and consumed in production batches to make finished products.
2. **Distribution** – Finished products are issued from the warehouse to individual **salesmen**. Each salesman carries their own physical stock.
3. **Sales** – Salesmen sell directly to **customers** (Dealers, Sub-dealers, Shopkeepers, Households) on daily field routes.
4. **Accounting** – Revenue, agent commission, daily operational expenses, and material cost are combined to compute **net daily profit/loss**.

---

## 3. Product Catalogue

Products are identified by a short **product code**. The standard codes are:

| Code  | Description       | Volume per unit |
|-------|-------------------|-----------------|
| N500  | Normal 500 ml     | 0.5 L           |
| L500  | Lemon 500 ml      | 0.5 L           |
| N1    | Normal 1 L        | 1.0 L           |
| L1    | Lemon 1 L         | 1.0 L           |
| N5    | Normal 5 L        | 5.0 L           |
| L5    | Lemon 5 L         | 5.0 L           |

Detailed product metadata (name, variant, metric, cost) is stored in the `product_cost_manual` table and managed through the **Product Cost Manager** UI page.

---

## 4. Core Business Rules

### 4.1 Commission Calculation

Agent (salesman) commission is calculated **per unit** based on the selling rate and then multiplied by the quantity sold. The rules, implemented in `CommissionCalculator.java`, are:

| Product Code | Rate threshold | Commission per unit |
|-------------|----------------|----------------------|
| N1, L1      | ≤ ₹35          | ₹5 flat              |
| N1, L1      | > ₹35          | ₹5 + 50 % × (rate − ₹35) |
| N500        | ≤ ₹25          | ₹5 flat              |
| N500        | > ₹25          | ₹5 + 50 % × (rate − ₹25) |
| L500        | ≤ ₹25          | ₹5 flat              |
| L500        | > ₹25          | ₹5 + 50 % × (rate − ₹25) |
| N5, L5      | ≤ ₹150         | ₹25 flat             |
| N5, L5      | > ₹150         | ₹25 + 50 % × (rate − ₹150) |
| Other       | —              | ₹0                   |

**Example:** Selling 10 units of N1 at ₹40/unit → commission = (₹5 + 50 % × (40 − 35)) × 10 = ₹7.50 × 10 = **₹75**

### 4.2 Volume Calculation

Volume sold (in litres) is derived from the product code and quantity (`VolumeCalculator.java`):

```
volume_sold = quantity × volume_per_unit
```

### 4.3 Profit Calculation

For each sale date, `ProductCostService.java` computes:

| Metric | Formula |
|--------|---------|
| **Op-cost per unit** | Total daily expense ÷ total units sold (all products) |
| **Total cost (per product)** | `product_cost_manual.cost` × quantity |
| **Op-cost (per product)** | Op-cost per unit × quantity |
| **Total profit** | Revenue − Total cost − Op-cost |
| **Net profit** | Total profit − Salesman commission |
| **Avg profit/unit** | Total profit ÷ quantity |

`DailySummary` stores the day-level roll-up: total revenue, total commission, total expense, material cost, volume sold, and net profit.

Net profit formula in `DailySummary`:
```
net_profit = total_revenue − total_agent_commission − total_expense − material_cost
```

---

## 5. Stock Flow: Warehouse → Salesman → Customer

All stock movements are **atomic transactions** (Spring `@Transactional`) and produce a double-entry ledger trail.

### Issue Stock to Salesman (Warehouse → Salesman)

```
1. warehouse_inventory.qty_available  −= qty
2. warehouse_ledger entry  ISSUE_TO_SALESMAN  (delta = −qty)
3. salesman_stock_summary.current_stock  += qty
4. salesman_ledger entry  ISSUE_FROM_WAREHOUSE  (delta = +qty)
```

### Salesman Makes a Sale (Salesman → Customer)

```
1. daily_sale_record  inserted  (revenue, commission, volume auto-calculated)
2. salesman_stock_summary.current_stock  −= qty  (atomic conditional UPDATE)
3. salesman_ledger entry  SOLD  (delta = −qty)
```
A sale is **rejected** (`InvalidInputException`) if the salesman's current stock is insufficient (race-condition-safe via single-statement conditional `UPDATE … WHERE current_stock >= qty`).

### Return Stock from Salesman (Salesman → Warehouse)

```
1. salesman_stock_summary.current_stock  −= qty
2. salesman_ledger entry  RETURN_TO_WAREHOUSE  (delta = −qty)
3. warehouse_inventory.qty_available  += qty
4. warehouse_ledger entry  RETURN_FROM_SALESMAN  (delta = +qty)
```

### Update / Void a Sale

When a sale is **updated** and stock-affecting fields change (salesman, product, or quantity), the service:
1. Restores old stock (atomic increment).
2. Writes a reversal ledger entry.
3. Decrements new stock (with floor enforcement).
4. Writes a new ledger entry.
All four steps are wrapped in one transaction and fully rolled back on failure.

When a sale is **voided (deleted)**, the salesman's stock is restored and a reversal ledger entry is written before the sale record is deleted.

---

## 6. Data Model (Database Tables)

| Table | Purpose |
|-------|---------|
| `products` | Master product definitions (name, code, size, price, commission) |
| `product_cost_manual` | Active cost per product code used for profit calculations |
| `product_recipes` | Bill-of-materials: which chemicals and what quantities make each product |
| `chemicals` | Raw materials (name, category, unit, purchase rate, vendor) |
| `chemical_inventory` | Current chemical stock levels per warehouse |
| `vendors` | Chemical suppliers |
| `warehouses` | Physical warehouse locations |
| `warehouse_inventory` | Current finished-product stock in the warehouse per product code |
| `warehouse_ledger` | Audit trail of all warehouse stock movements |
| `warehouse_ledger_archive` | Archived warehouse ledger entries |
| `production_batches` | Production runs (product, batch qty, date) |
| `batch_consumption` | Chemicals consumed per production batch |
| `salesmen` | Field salesmen (name, alias, contact) |
| `salesman_stock_summary` | Current stock held by each salesman per product code |
| `salesman_ledger` | Audit trail of all salesman stock movements |
| `salesman_ledger_archive` | Archived salesman ledger entries |
| `salesman_expenses` | Expenses logged against a salesman |
| `customers` | Customer master (shop name, type, route) |
| `routes` | Sales routes (geographic areas) |
| `route_villages` | Villages belonging to each route |
| `daily_sale_record` | Individual sale line items (salesman, customer, product, qty, rate, revenue, commission, volume) |
| `daily_summary` | Day-level aggregated P&L per salesman |
| `daily_expense_record` | Daily operational expense line items (category, amount, date) |
| `sales_records` | Legacy sales table (kept for backward compatibility) |

---

## 7. Backend – Spring Boot

### 7.1 Services

| Service | Responsibility |
|---------|---------------|
| `AdminService` | CRUD for all master-data entities (products, salesmen, customers, vendors, chemicals, routes, batches, …) |
| `SalesService` | Create / update / void daily sale records with atomic stock reconciliation and ledger writes |
| `WarehouseService` | Issue and return stock between warehouse and salesmen (4-write atomic transactions) |
| `ProductCostService` | Enrich product sales DTOs with cost, commission, op-cost, and profit metrics |
| `DailySummaryService` | Submit and query day-level P&L summaries |
| `ProductCostManualService` | Manage the `product_cost_manual` table |

### 7.2 REST API Endpoints

Base URL: `http://localhost:8080/api`

#### Sales

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/daily-sales` | List all daily sale records |
| `GET` | `/daily-sales/{id}` | Get sale by ID |
| `POST` | `/daily-sales` | Create a new sale (atomic stock decrement) |
| `PUT` | `/daily-sales/{id}` | Update a sale (stock reconciliation) |
| `DELETE` | `/daily-sales/{id}` | Void a sale (stock restoration) |
| `GET` | `/daily-sales/salesman/{alias}` | Sales by salesman alias |
| `GET` | `/daily-sales/date/{date}` | Sales by date |
| `POST` | `/daily-sales/bulk` | Bulk import sales (JSON dump) |

#### Warehouse

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/warehouse/inventory` | All warehouse inventory |
| `GET` | `/warehouse/inventory/{productCode}` | Inventory for one product |
| `POST` | `/warehouse/issue` | Issue stock to salesman |
| `POST` | `/warehouse/return` | Return stock from salesman |
| `POST` | `/warehouse/adjust` | Manual stock adjustment |
| `GET` | `/warehouse/ledger` | Full warehouse ledger |
| `GET` | `/warehouse/salesman-stock` | Current stock held by salesmen |

#### Summary & Reports

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/summary/submit` | Submit / recalculate daily P&L summary |
| `GET` | `/summary` | All daily summaries |
| `GET` | `/summary/{salesmanAlias}/{date}` | Summary for salesman on date |
| `GET` | `/product-sales-summary/date/{date}` | Product-wise profit breakdown for a date |
| `GET` | `/product-sales-summary/range` | Product-wise summary for date range |

#### Admin Master Data (`/api/admin`)

Provides full CRUD for: **vendors**, **products**, **customers**, **salesmen**, **chemicals**, **chemical-inventory**, **warehouses**, **product-recipes**, **production-batches**, **batch-consumption**, **routes**, **route-villages**, **salesman-expenses**, and legacy **sales-records**.

#### Daily Expenses

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/expenses` | All expense records |
| `POST` | `/expenses` | Add expense entry |
| `GET` | `/expenses/date/{date}` | Expenses for a specific date |
| `DELETE` | `/expenses/{id}` | Delete expense |

---

## 8. Frontend – React

| Page | Description |
|------|-------------|
| **Admin Dashboard** | Master data management: salesmen, products, customers, routes, etc. |
| **Daily Sales Dump** | Paste a JSON array of sale records and bulk-submit them for a selected salesman and date. Also records daily expense line items inline. |
| **Add Sales Record** | Form for adding individual sale records. |
| **Daily Sales Summary** | View sales aggregated by salesman and date. |
| **Daily Summary Report** | View and submit day-level P&L summaries. |
| **Product Sales Summary** | Product-wise breakdown of revenue, cost, commission, and profit for any date or range. |
| **Warehouse Dashboard** | Warehouse inventory, issue stock to salesmen, return stock, and view ledger. |
| **Product Cost Manager** | Maintain the `product_cost_manual` table (set cost per product code). |
| **API Explorer** | Embedded Swagger UI for direct API access. |

---

## 9. Running Locally

### Prerequisites

- Java 17+, Maven 3.8+
- Node.js 18+, npm
- MySQL 8

### Backend

```bash
# Configure database credentials
cp recordbook/src/main/resources/database.properties.template \
   recordbook/src/main/resources/database.properties
# Edit database.properties with db.host, db.port, db.name, db.username, db.password

cd recordbook
./mvnw spring-boot:run
# API available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

### Frontend

```bash
cd recordbook-frontend
cp .env.example .env.local
# Set REACT_APP_API_BASE_URL=http://localhost:8080

npm install
npm start
# App available at http://localhost:3000
```

### Database

Apply the schema from:

```bash
mysql -u <user> -p <dbname> < recordbook/src/main/resources/createtable.sql
```
