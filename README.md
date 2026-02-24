# RecordBook - URVI CLEAN ERP System

RecordBook is a comprehensive Enterprise Resource Planning (ERP) system designed for managing daily sales, expenses, inventory, and production. It features a Spring Boot backend with a React frontend, providing real-time tracking and commission calculations for salesman operations.

## Core Features

**Daily Sales Management**: Capture sales records with product codes, quantities, rates, and automatic revenue calculation with agent commission computation based on configurable thresholds.

**Daily Expense Tracking**: Salesman can log expenses by category, with automatic daily aggregation into the `daily_expense_record` table. Expenses are linked to salesmen by alias and date for comprehensive tracking.

**Advanced Date Filtering**: View sales data filtered by today, specific date, current week, current month, or any custom date range with instant API-driven results.

**Inventory & Production**: Track chemical inventory across multiple warehouses, manage production batches, and consume chemicals with full recipe support.

**Customer & Route Management**: Organize customers by routes, villages, and contact information for efficient salesman route planning.

**Agent Commission System**: Automatic commission calculation based on product-specific thresholds with bonus calculations when selling price exceeds thresholds.

## Technology Stack

**Backend**: Spring Boot 3.x, MySQL 8+, Spring Data JPA, Lombok, Jackson JSON  
**Frontend**: React 18, React Router, Axios, CSS3  
**Build**: Maven 3.x, Node.js 14+

## System Architecture

The application follows a layered architecture:
- **Controllers**: REST endpoints for sales, expenses, admin operations
- **Services**: Business logic for calculations and aggregations
- **Repositories**: Data access layer using Spring Data JPA
- **Models**: Entity classes with relationships
- **Utils**: Commission calculator and date utilities

## Key Endpoints

### Sales API
- `POST /api/sales/sales-expense` - Submit sales and expenses together
- `GET /api/sales/filter/today` - Get today's sales
- `GET /api/sales/filter/date?date=YYYY-MM-DD` - Get specific date sales
- `GET /api/sales/filter/range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` - Date range
- `GET /api/sales/filter/week?year=2025&week=8` - Weekly sales
- `GET /api/sales/filter/month?year=2025&month=2` - Monthly sales

### Expense API
- `GET /api/daily-expense-record/by-date` - Get expenses for date range
- `GET /api/daily-expense-record/{alias}/{date}` - Get specific salesman daily expense

## Database Schema

Key tables: `daily_sale_record`, `salesman_expense`, `daily_expense_record`, `salesman`, `product`, `customer`, `route`, `warehouse`, `chemical_inventory`, `production_batch`.

## Getting Started

```bash
# Backend (from recordbook directory)
mvn spring-boot:run

# Frontend (from recordbook-frontend directory)  
npm install
npm start
```

Backend runs on `http://localhost:8080`, frontend on `http://localhost:3000`.

Initialize database: Run `recordbook/src/main/resources/createtable.sql` on MySQL.


