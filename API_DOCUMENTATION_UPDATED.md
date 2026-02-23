# Recordbook REST API Documentation

**Version:** 1.0  
**Base URL:** `http://localhost:8080/api/v1/admin`  
**Content-Type:** `application/json`  
**Last Updated:** February 19, 2026

---

## 📑 Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Response Format](#response-format)
4. [HTTP Status Codes](#http-status-codes)
5. [Salesmen](#salesmen)
6. [Vendors](#vendors)
7. [Customers](#customers)
8. [Products](#products)
9. [Chemicals](#chemicals)
10. [Warehouses](#warehouses)
11. [Routes](#routes)
12. [Route Villages](#route-villages)
13. [Chemical Inventory](#chemical-inventory)
14. [Product Recipes](#product-recipes)
15. [Production Batches](#production-batches)
16. [Batch Consumption](#batch-consumption)
17. [Sales Records](#sales-records)
18. [Salesman Expenses](#salesman-expenses)

---

## Overview

The Recordbook API provides RESTful endpoints for managing:
- Sales operations
- Inventory management
- Manufacturing/production tracking
- Salesman and customer management
- Vendor and chemical management
- Route and delivery tracking

**Key Features:**
- ✅ Auto-generated BIGINT primary keys (no manual ID input)
- ✅ Timestamp tracking (createdAt)
- ✅ Multi-warehouse support
- ✅ Batch consumption tracking
- ✅ API versioning (`/v1/`)

---

## Authentication

**Current Status:** No authentication required  
**Future:** JWT token-based authentication will be added

All endpoints are currently publicly accessible.

---

## Response Format

### Successful Response (200, 201)
```json
{
  "id": 1,
  "field": "value",
  "createdAt": "2026-02-19T12:30:00"
}
```

### Error Response (4xx, 5xx)
```json
{
  "error": "Detailed error message",
  "timestamp": "2026-02-19T12:30:00",
  "status": 400
}
```

### List Response
```json
[
  { "id": 1, "field": "value1" },
  { "id": 2, "field": "value2" }
]
```

---

## HTTP Status Codes

| Code | Meaning | Example |
|------|---------|---------|
| 200 | OK | Successful GET/PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid input data |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Database error |

---

## Salesmen

### List all salesmen
```http
GET /api/v1/admin/salesmen
```

**Response (200):**
```json
[
  {
    "salesmanId": 1,
    "firstName": "John",
    "lastName": "Doe",
    "address": "New York",
    "contactNumber": "+919876543210",
    "createdAt": "2026-02-19T12:30:00"
  }
]
```

---

### Get salesman by ID
```http
GET /api/v1/admin/salesmen/{id}
```

**Example:** `GET /api/v1/admin/salesmen/1`

**Response (200):**
```json
{
  "salesmanId": 1,
  "firstName": "John",
  "lastName": "Doe",
  "address": "New York",
  "contactNumber": "+919876543210",
  "createdAt": "2026-02-19T12:30:00"
}
```

---

### Create salesman
```http
POST /api/v1/admin/salesmen
Content-Type: application/json
```

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "address": "New York",
  "contactNumber": "+919876543210"
}
```

**Fields:**
| Field | Type | Required | Length |
|-------|------|----------|--------|
| firstName | String | Yes | 1-50 |
| lastName | String | Yes | 1-50 |
| address | String | Yes | 1-100 |
| contactNumber | String | No | 1-25 |

**Response (201):**
```json
{
  "salesmanId": 1,
  "firstName": "John",
  "lastName": "Doe",
  "address": "New York",
  "contactNumber": "+919876543210",
  "createdAt": "2026-02-19T12:30:00"
}
```

---

### Update salesman
```http
PUT /api/v1/admin/salesmen/{id}
Content-Type: application/json
```

**Example:** `PUT /api/v1/admin/salesmen/1`

**Request:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "address": "Los Angeles",
  "contactNumber": "+919876543211"
}
```

**Response (200):** Same as Create

---

### Delete salesman
```http
DELETE /api/v1/admin/salesmen/{id}
```

**Response:** 204 No Content

---

## Vendors

### List all vendors
```http
GET /api/v1/admin/vendors
```

---

### Get vendor by ID
```http
GET /api/v1/admin/vendors/{id}
```

---

### Create vendor
```http
POST /api/v1/admin/vendors
Content-Type: application/json
```

**Request:**
```json
{
  "vendorName": "ABC Chemicals",
  "firstName": "Rajesh",
  "lastName": "Kumar",
  "address": "Dhanbad",
  "contactNumber": "+919876543210"
}
```

**Fields:**
| Field | Type | Required | Length |
|-------|------|----------|--------|
| vendorName | String | Yes | 1-100 |
| firstName | String | Yes | 1-50 |
| lastName | String | Yes | 1-50 |
| address | String | Yes | 1-100 |
| contactNumber | String | No | 1-25 |

**Response (201):**
```json
{
  "vendorId": 1,
  "vendorName": "ABC Chemicals",
  "firstName": "Rajesh",
  "lastName": "Kumar",
  "address": "Dhanbad",
  "contactNumber": "+919876543210",
  "createdAt": "2026-02-19T12:30:00"
}
```

---

### Update vendor
```http
PUT /api/v1/admin/vendors/{id}
```

---

### Delete vendor
```http
DELETE /api/v1/admin/vendors/{id}
```

---

## Customers

### List all customers
```http
GET /api/v1/admin/customers
```

---

### Get customer by ID
```http
GET /api/v1/admin/customers/{id}
```

---

### Create customer
```http
POST /api/v1/admin/customers
Content-Type: application/json
```

**Request:**
```json
{
  "shopName": "Sharma General Store",
  "ownerFirstName": "Priya",
  "ownerLastName": "Sharma",
  "ownerAddress": "Hazaribagh",
  "customerType": "Shopkeeper",
  "routeId": 1,
  "villageId": 1
}
```

**Fields:**
| Field | Type | Required | Options |
|-------|------|----------|---------|
| shopName | String | Yes | - |
| ownerFirstName | String | Yes | - |
| ownerLastName | String | Yes | - |
| ownerAddress | String | Yes | - |
| customerType | Enum | Yes | Dealer, Subdealer, Shopkeeper, Household |
| routeId | Long | No | - |
| villageId | Long | No | - |

**Response (201):**
```json
{
  "customerId": 1,
  "shopName": "Sharma General Store",
  "ownerFirstName": "Priya",
  "ownerLastName": "Sharma",
  "ownerAddress": "Hazaribagh",
  "customerType": "Shopkeeper",
  "routeId": 1,
  "villageId": 1,
  "createdAt": "2026-02-19T12:30:00"
}
```

---

### Update customer
```http
PUT /api/v1/admin/customers/{id}
```

---

### Delete customer
```http
DELETE /api/v1/admin/customers/{id}
```

---

## Products

### List all products
```http
GET /api/v1/admin/products
```

---

### Get product by ID
```http
GET /api/v1/admin/products/{id}
```

---

### Create product
```http
POST /api/v1/admin/products
Content-Type: application/json
```

**Request:**
```json
{
  "productName": "Phenyl",
  "variant": "Lemon",
  "size": "5L",
  "targetPrice": 299.99,
  "baseCommission": 50.00,
  "otherOverheadCost": 25.00
}
```

**Fields:**
| Field | Type | Required |
|-------|------|----------|
| productName | String | Yes |
| variant | String | No |
| size | String | No |
| targetPrice | BigDecimal | No |
| baseCommission | BigDecimal | No |
| otherOverheadCost | BigDecimal | No |

**Response (201):**
```json
{
  "productId": 1,
  "productName": "Phenyl",
  "variant": "Lemon",
  "size": "5L",
  "targetPrice": 299.99,
  "baseCommission": 50.00,
  "otherOverheadCost": 25.00,
  "createdAt": "2026-02-19T12:30:00"
}
```

---

### Update product
```http
PUT /api/v1/admin/products/{id}
```

---

### Delete product
```http
DELETE /api/v1/admin/products/{id}
```

---

## Chemicals

### List all chemicals
```http
GET /api/v1/admin/chemicals
```

---

### Get chemical by ID
```http
GET /api/v1/admin/chemicals/{id}
```

---

### Create chemical
```http
POST /api/v1/admin/chemicals
Content-Type: application/json
```

**Request:**
```json
{
  "chemicalName": "Pine Oil",
  "category": "Raw Material",
  "unit": "Litre",
  "purchaseRate": 250.50,
  "transportCostPerUnit": 10.00,
  "vendorId": 1
}
```

**Fields:**
| Field | Type | Required | Options |
|-------|------|----------|---------|
| chemicalName | String | Yes | - |
| category | Enum | Yes | Raw Material, Packaging, Labeling, Other |
| unit | String | No | - |
| purchaseRate | BigDecimal | No | - |
| transportCostPerUnit | BigDecimal | No | - |
| vendorId | Long | No | - |

---

### Update chemical
```http
PUT /api/v1/admin/chemicals/{id}
```

---

### Delete chemical
```http
DELETE /api/v1/admin/chemicals/{id}
```

---

## Warehouses

### List all warehouses
```http
GET /api/v1/admin/warehouses
```

---

### Get warehouse by ID
```http
GET /api/v1/admin/warehouses/{id}
```

---

### Create warehouse
```http
POST /api/v1/admin/warehouses
Content-Type: application/json
```

**Request:**
```json
{
  "warehouseName": "Main Warehouse",
  "location": "Mumbai"
}
```

---

### Update warehouse
```http
PUT /api/v1/admin/warehouses/{id}
```

---

### Delete warehouse
```http
DELETE /api/v1/admin/warehouses/{id}
```

---

## Routes

### List all routes
```http
GET /api/v1/admin/routes
```

---

### Create route
```http
POST /api/v1/admin/routes
Content-Type: application/json
```

**Request:**
```json
{
  "routeName": "Route A"
}
```

---

## Route Villages

### List all villages
```http
GET /api/v1/admin/route-villages
```

---

### Create village
```http
POST /api/v1/admin/route-villages
Content-Type: application/json
```

**Request:**
```json
{
  "routeId": 1,
  "villageName": "Village A"
}
```

---

## Chemical Inventory

### List all inventory
```http
GET /api/v1/admin/chemical-inventory
```

---

### Create inventory record
```http
POST /api/v1/admin/chemical-inventory
Content-Type: application/json
```

**Request:**
```json
{
  "chemicalId": 1,
  "warehouseId": 1,
  "currentStockLevel": 100.50,
  "reorderPoint": 50.00
}
```

---

## Product Recipes

### List all recipes
```http
GET /api/v1/admin/product-recipes
```

---

### Create recipe
```http
POST /api/v1/admin/product-recipes
Content-Type: application/json
```

**Request:**
```json
{
  "productId": 1,
  "chemicalId": 5,
  "requiredQtyPerUnit": 2.50
}
```

---

## Production Batches

### List all batches
```http
GET /api/v1/admin/production-batches
```

---

### Create batch
```http
POST /api/v1/admin/production-batches
Content-Type: application/json
```

**Request:**
```json
{
  "productId": 1,
  "startDate": "2026-02-19",
  "totalQtyProduced": 1000.00,
  "remainingQty": 1000.00,
  "calculatedMfgCostPerUnit": 150.50
}
```

---

## Batch Consumption

### List all consumption records
```http
GET /api/v1/admin/batch-consumption
```

---

### Create consumption record
```http
POST /api/v1/admin/batch-consumption
Content-Type: application/json
```

**Request:**
```json
{
  "batchId": 1,
  "chemicalId": 5,
  "qtyUsed": 25.50,
  "unitCostAtTime": 150.00
}
```

---

## Sales Records

### List all sales
```http
GET /api/v1/admin/sales
```

---

### Get sale by ID
```http
GET /api/v1/admin/sales/{id}
```

---

### Create sales record
```http
POST /api/v1/admin/sales
Content-Type: application/json
```

**Request:**
```json
{
  "salesmanId": 1,
  "customerId": 1,
  "productId": 1,
  "batchId": 1,
  "orderDate": "2026-02-19",
  "actualRate": 299.99,
  "quantity": 10.00,
  "adjustedMargin": 50.00
}
```

---

### Update sales record
```http
PUT /api/v1/admin/sales/{id}
```

---

### Delete sales record
```http
DELETE /api/v1/admin/sales/{id}
```

---

## Salesman Expenses

### List all expenses
```http
GET /api/v1/admin/expenses
```

---

### Create expense
```http
POST /api/v1/admin/expenses
Content-Type: application/json
```

**Request:**
```json
{
  "salesmanId": 1,
  "expenseDate": "2026-02-19",
  "category": "Petrol",
  "amount": 500.00
}
```

**Categories:** Petrol, Food, Vehicle Rent, Mobile, Other

---

## Common Response Patterns

### Empty List
```json
[]
```

### Not Found Error
```json
{
  "error": "Salesman with ID 999 not found",
  "timestamp": "2026-02-19T12:30:00",
  "status": 404
}
```

### Validation Error
```json
{
  "error": "firstName is required",
  "timestamp": "2026-02-19T12:30:00",
  "status": 400
}
```

---

## Quick Reference - ID Types

| Entity | ID Type | Auto-Generated |
|--------|---------|----------------|
| Salesman | Long | ✅ Yes |
| Vendor | Long | ✅ Yes |
| Customer | Long | ✅ Yes |
| Product | Long | ✅ Yes |
| Route | Long | ✅ Yes |
| RouteVillage | Long | ✅ Yes |
| Chemical | Long | ✅ Yes |
| Warehouse | Long | ✅ Yes |
| SalesRecord | Long | ✅ Yes |
| ProductRecipe | Long | ✅ Yes |
| ProductionBatch | Long | ✅ Yes |
| SalesmanExpense | Long | ✅ Yes |
| ChemicalInventory | Long | ✅ Yes |
| BatchConsumption | Long | ✅ Yes |

---

## Integration Tips

### Using cURL
```bash
curl -X GET http://localhost:8080/api/v1/admin/salesmen \
  -H "Content-Type: application/json"

curl -X POST http://localhost:8080/api/v1/admin/salesmen \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","address":"NYC","contactNumber":"+919876543210"}'
```

### Using Postman
1. Create new Collection: "Recordbook API"
2. Set Base URL: `http://localhost:8080/api/v1/admin`
3. Create folders for each endpoint group
4. Import requests from examples above

### Using JavaScript/Fetch
```javascript
// GET
fetch('http://localhost:8080/api/v1/admin/salesmen')
  .then(r => r.json())
  .then(data => console.log(data));

// POST
fetch('http://localhost:8080/api/v1/admin/salesmen', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    firstName: 'John',
    lastName: 'Doe',
    address: 'New York',
    contactNumber: '+919876543210'
  })
})
.then(r => r.json())
.then(data => console.log(data));
```

---

## Notes

- All timestamps are in ISO 8601 format (YYYY-MM-DDTHH:mm:ss)
- All IDs are auto-generated by the backend (Long type)
- Decimal fields use `BigDecimal` for precision
- No authentication required (for now)
- CORS enabled for `localhost:3000`
- Maximum request size: 10MB

---

**Last Updated:** February 19, 2026  
**Version:** 1.0.0

