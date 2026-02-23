# Recordbook API Documentation

## Base URL
```
http://localhost:8080
```

---

## 1. Admin Controller (`/api/admin`)
All endpoints for CRUD operations on all tables via admin interface.

### Sales Records
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/sales` | List all sales records | None | `List<SalesRecord>` |
| GET | `/api/admin/sales/{id}` | Get sales record by ID | None | `SalesRecord` |
| POST | `/api/admin/sales` | Create new sales record | `SalesRecord` | `SalesRecord` |
| PUT | `/api/admin/sales/{id}` | Update sales record | `SalesRecord` | `SalesRecord` |
| DELETE | `/api/admin/sales/{id}` | Delete sales record | None | 204 No Content |

**SalesRecord fields:**
```json
{
  "saleId": 0,
  "salesman": { "salesmanId": "", "name": "", "contactNumber": "" },
  "customer": { "customerId": "", "shopName": "", "customerType": "Dealer|Subdealer|Shopkeeper|Household" },
  "product": { "productId": "", "productName": "", "variant": "", "size": "" },
  "route": { "routeId": 0, "routeName": "" },
  "village": { "villageId": 0, "villageName": "" },
  "orderDate": "2026-02-19",
  "actualRate": 0.00,
  "quantity": 0,
  "revenue": 0.00,
  "adjustedMargin": 0.00
}
```

---

### Vendors
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/vendors` | List all vendors | None | `List<Vendor>` |
| GET | `/api/admin/vendors/{id}` | Get vendor by ID | None | `Vendor` |
| POST | `/api/admin/vendors` | Create vendor | `Vendor` | `Vendor` |
| PUT | `/api/admin/vendors/{id}` | Update vendor | `Vendor` | `Vendor` |
| DELETE | `/api/admin/vendors/{id}` | Delete vendor | None | 204 No Content |

**Vendor fields:**
```json
{
  "vendorId": "First_Last_Location",
  "vendorName": "Vendor Name",
  "location": "City",
  "contactNumber": "+91XXXXXXXXXX"
}
```

---

### Products
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/products` | List all products | None | `List<Product>` |
| GET | `/api/admin/products/{id}` | Get product by ID | None | `Product` |
| POST | `/api/admin/products` | Create product | `Product` | `Product` |
| PUT | `/api/admin/products/{id}` | Update product | `Product` | `Product` |
| DELETE | `/api/admin/products/{id}` | Delete product | None | 204 No Content |

**Product fields:**
```json
{
  "productId": "Phenyl_Lemon_5L",
  "productName": "Phenyl",
  "variant": "Lemon",
  "size": "5L",
  "targetPrice": 0.00,
  "baseCommission": 0.00,
  "otherOverheadCost": 0.00
}
```

---

### Customers
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/customers` | List all customers | None | `List<Customer>` |
| GET | `/api/admin/customers/{id}` | Get customer by ID | None | `Customer` |
| POST | `/api/admin/customers` | Create customer | `Customer` | `Customer` |
| PUT | `/api/admin/customers/{id}` | Update customer | `Customer` | `Customer` |
| DELETE | `/api/admin/customers/{id}` | Delete customer | None | 204 No Content |

**Customer fields:**
```json
{
  "customerId": "ShopName_Owner_Location",
  "shopName": "Shop Name",
  "customerType": "Dealer|Subdealer|Shopkeeper|Household",
  "route": { "routeId": 0 },
  "village": { "villageId": 0 }
}
```

---

### Salesmen
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/salesmen` | List all salesmen | None | `List<Salesman>` |
| GET | `/api/admin/salesmen/{id}` | Get salesman by ID | None | `Salesman` |
| POST | `/api/admin/salesmen` | Create salesman | `Salesman` | `Salesman` |
| PUT | `/api/admin/salesmen/{id}` | Update salesman | `Salesman` | `Salesman` |
| DELETE | `/api/admin/salesmen/{id}` | Delete salesman | None | 204 No Content |

**Salesman fields:**
```json
{
  "salesmanId": "First_Last_Location",
  "name": "Salesman Name",
  "contactNumber": "+91XXXXXXXXXX"
}
```

---

### Chemicals
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/chemicals` | List all chemicals | None | `List<Chemical>` |
| GET | `/api/admin/chemicals/{id}` | Get chemical by ID | None | `Chemical` |
| POST | `/api/admin/chemicals` | Create chemical | `Chemical` | `Chemical` |
| PUT | `/api/admin/chemicals/{id}` | Update chemical | `Chemical` | `Chemical` |
| DELETE | `/api/admin/chemicals/{id}` | Delete chemical | None | 204 No Content |

**Chemical fields:**
```json
{
  "chemicalId": "PineOil_50L_Dhanbad",
  "chemicalName": "Pine Oil",
  "category": "Raw Material|Packaging|Labeling|Other",
  "unit": "L",
  "purchaseRate": 0.00,
  "transportCostPerUnit": 0.00,
  "vendor": { "vendorId": "" }
}
```

---

### Chemical Inventory
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/chemical-inventory` | List all inventory | None | `List<ChemicalInventory>` |
| GET | `/api/admin/chemical-inventory/{id}` | Get inventory by chemical ID | None | `ChemicalInventory` |
| POST | `/api/admin/chemical-inventory` | Create inventory | `ChemicalInventory` | `ChemicalInventory` |
| PUT | `/api/admin/chemical-inventory/{id}` | Update inventory | `ChemicalInventory` | `ChemicalInventory` |
| DELETE | `/api/admin/chemical-inventory/{id}` | Delete inventory | None | 204 No Content |

**ChemicalInventory fields:**
```json
{
  "chemicalId": "PineOil_50L_Dhanbad",
  "currentStockLevel": 100.00,
  "reorderPoint": 20.00
}
```

---

### Product Recipes
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/product-recipes` | List all recipes | None | `List<ProductRecipe>` |
| GET | `/api/admin/product-recipes/{id}` | Get recipe by ID | None | `ProductRecipe` |
| POST | `/api/admin/product-recipes` | Create recipe | `ProductRecipe` | `ProductRecipe` |
| PUT | `/api/admin/product-recipes/{id}` | Update recipe | `ProductRecipe` | `ProductRecipe` |
| DELETE | `/api/admin/product-recipes/{id}` | Delete recipe | None | 204 No Content |

**ProductRecipe fields:**
```json
{
  "recipeId": 0,
  "product": { "productId": "" },
  "chemical": { "chemicalId": "" },
  "requiredQtyPerUnit": 0.5
}
```

---

### Production Batches
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/production-batches` | List all batches | None | `List<ProductionBatch>` |
| GET | `/api/admin/production-batches/{id}` | Get batch by ID | None | `ProductionBatch` |
| POST | `/api/admin/production-batches` | Create batch | `ProductionBatch` | `ProductionBatch` |
| PUT | `/api/admin/production-batches/{id}` | Update batch | `ProductionBatch` | `ProductionBatch` |
| DELETE | `/api/admin/production-batches/{id}` | Delete batch | None | 204 No Content |

**ProductionBatch fields:**
```json
{
  "batchId": "Batch_001",
  "product": { "productId": "" },
  "startDate": "2026-02-19",
  "totalQtyProduced": 1000,
  "remainingQty": 500,
  "calculatedMfgCostPerUnit": 10.00
}
```

---

### Routes
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/routes` | List all routes | None | `List<Route>` |
| GET | `/api/admin/routes/{id}` | Get route by ID | None | `Route` |
| POST | `/api/admin/routes` | Create route | `Route` | `Route` |
| PUT | `/api/admin/routes/{id}` | Update route | `Route` | `Route` |
| DELETE | `/api/admin/routes/{id}` | Delete route | None | 204 No Content |

**Route fields:**
```json
{
  "routeId": 0,
  "routeName": "Route Name"
}
```

---

### Route Villages
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/route-villages` | List all villages | None | `List<RouteVillage>` |
| GET | `/api/admin/route-villages/{id}` | Get village by ID | None | `RouteVillage` |
| POST | `/api/admin/route-villages` | Create village | `RouteVillage` | `RouteVillage` |
| PUT | `/api/admin/route-villages/{id}` | Update village | `RouteVillage` | `RouteVillage` |
| DELETE | `/api/admin/route-villages/{id}` | Delete village | None | 204 No Content |

**RouteVillage fields:**
```json
{
  "villageId": 0,
  "villageName": "Village Name",
  "route": { "routeId": 0 }
}
```

---

### Salesman Expenses
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/admin/expenses` | List all expenses | None | `List<SalesmanExpense>` |
| GET | `/api/admin/expenses/{id}` | Get expense by ID | None | `SalesmanExpense` |
| POST | `/api/admin/expenses` | Create expense | `SalesmanExpense` | `SalesmanExpense` |
| PUT | `/api/admin/expenses/{id}` | Update expense | `SalesmanExpense` | `SalesmanExpense` |
| DELETE | `/api/admin/expenses/{id}` | Delete expense | None | 204 No Content |

**SalesmanExpense fields:**
```json
{
  "expenseId": 0,
  "salesman": { "salesmanId": "" },
  "expenseDate": "2026-02-19",
  "category": "Petrol|Food|Vehicle Rent|Mobile|Other",
  "amount": 0.00
}
```

---

## 2. Sales Controller (`/api/sales`)
Endpoints for daily sales operations (existing controller).

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/sales` | Create sales record | `DailySaleRecord` | `DailySaleRecord` |
| GET | `/api/sales` | Get all sales records | None | `List<DailySaleRecord>` |
| PUT | `/api/sales/{id}` | Update sales record | `DailySaleRecord` | `DailySaleRecord` |
| DELETE | `/api/sales/{id}` | Delete sales record | None | 204 No Content |
| GET | `/api/sales/filter/variant?name=X` | Filter by variant | None | `List<DailySaleRecord>` |
| GET | `/api/sales/filter/size?value=X` | Filter by size | None | `List<DailySaleRecord>` |
| GET | `/api/sales/filter/search?variant=X&size=Y` | Filter by variant & size | None | `List<DailySaleRecord>` |

---

## 3. Actuator Endpoints (`/actuator`)
Monitoring and health check endpoints.

| Endpoint | Description |
|----------|-------------|
| GET `/actuator` | List all available actuator endpoints |
| GET `/actuator/health` | Application health status |
| GET `/actuator/metrics` | Available metrics |
| GET `/actuator/metrics/{name}` | Specific metric (e.g., `jvm.memory.used`) |
| GET `/actuator/info` | Application info |
| GET `/actuator/env` | Environment properties |

---

## Example Requests (PowerShell)

### List all salesmen
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/salesmen" -Method Get
```

### Create a new product
```powershell
$body = @{
  productId = "Phenyl_Lemon_5L"
  productName = "Phenyl"
  variant = "Lemon"
  size = "5L"
  targetPrice = 150.00
  baseCommission = 10.00
  otherOverheadCost = 5.00
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/admin/products" `
  -Method Post `
  -Body $body `
  -ContentType "application/json"
```

### Get sales record by ID
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/admin/sales/1" -Method Get
```

### Check application health
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
```

---

## Error Responses

All endpoints return standard HTTP status codes:
- **200 OK** - Success
- **201 Created** - Resource created
- **204 No Content** - Delete success
- **400 Bad Request** - Invalid input
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

---

## Notes
- All datetime fields use ISO 8601 format: `yyyy-MM-dd`
- All monetary fields are decimals (e.g., `150.50`)
- IDs in URLs are path parameters; filters use query parameters
- CORS enabled for `http://localhost:3000` (frontend)

