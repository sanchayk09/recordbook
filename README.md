# RecordBook - URVI CLEAN ERP System

A comprehensive Spring Boot + React ERP system for managing sales, expenses, inventory, and production.

## Features
- **Daily Sales Management**: Track sales with product codes, quantities, and rates
- **Daily Expense Tracking**: Auto-aggregated expense tracking per salesman by date with REST API (9 endpoints)
- **Inventory Management**: Multi-warehouse chemical inventory tracking
- **Production Management**: Batch processing and chemical consumption tracking
- **Customer & Route Management**: Organize customers by routes and villages

## Tech Stack
**Backend**: Spring Boot 3, MySQL 8+, JPA/Hibernate  
**Frontend**: React 18, Node.js

## Quick Start
```bash
# Backend
cd recordbook && mvn spring-boot:run

# Frontend  
cd recordbook-frontend && npm install && npm start
```

Visit `http://localhost:3000` once both servers are running.

## Database
Run `recordbook/src/main/resources/createtable.sql` to initialize the database with all tables including the new `daily_expense_record` table.

