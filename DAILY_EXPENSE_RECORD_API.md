# Daily Expense Record API Documentation

## Overview
The Daily Expense Record API provides endpoints to manage and retrieve aggregated daily expense records for salesmen. When a salesman enters expenses, the system automatically aggregates them by date and stores the total in the `daily_expense_record` table.

## Database Table Structure

### daily_expense_record
```sql
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
    INDEX idx_daily_expense_salesman (salesman_alias)
) ENGINE=InnoDB;
```

### Key Features:
- **Composite Primary Key**: `(salesman_alias, expense_date)` - ensures one entry per salesman per day
- **Total Expense**: Auto-aggregated from `salesman_expenses` table
- **Foreign Key**: References `salesmen(alias)` for data integrity
- **Timestamps**: Tracks creation and last update

---

## API Endpoints

### Base URL
```
http://localhost:8080/api/daily-expenses
```

### 1. Get All Daily Expense Records
**Endpoint**: `GET /api/daily-expenses`

**Description**: Retrieve all daily expense records

**Response**:
```json
[
  {
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-23",
    "totalExpense": 1500.00
  },
  {
    "salesmanAlias": "JANE_SMITH",
    "expenseDate": "2024-02-23",
    "totalExpense": 1200.50
  }
]
```

---

### 2. Get Expenses by Salesman Alias
**Endpoint**: `GET /api/daily-expenses/salesman?alias={salesmanAlias}`

**Parameters**:
- `alias` (required, string): The salesman's alias

**Example**:
```
GET /api/daily-expenses/salesman?alias=JOHN_DOE
```

**Response**:
```json
[
  {
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-23",
    "totalExpense": 1500.00
  },
  {
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-22",
    "totalExpense": 1200.00
  }
]
```

---

### 3. Get Expenses by Date
**Endpoint**: `GET /api/daily-expenses/date?date={date}`

**Parameters**:
- `date` (required, string): Date in ISO format (YYYY-MM-DD)

**Example**:
```
GET /api/daily-expenses/date?date=2024-02-23
```

**Response**:
```json
[
  {
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-23",
    "totalExpense": 1500.00
  },
  {
    "salesmanAlias": "JANE_SMITH",
    "expenseDate": "2024-02-23",
    "totalExpense": 1200.50
  }
]
```

---

### 4. Get Expense for Specific Salesman on Specific Date
**Endpoint**: `GET /api/daily-expenses/salesman-date?alias={alias}&date={date}`

**Parameters**:
- `alias` (required, string): The salesman's alias
- `date` (required, string): Date in ISO format (YYYY-MM-DD)

**Example**:
```
GET /api/daily-expenses/salesman-date?alias=JOHN_DOE&date=2024-02-23
```

**Response (200 OK)**:
```json
{
  "salesmanAlias": "JOHN_DOE",
  "expenseDate": "2024-02-23",
  "totalExpense": 1500.00
}
```

**Response (404 Not Found)**:
```json
{
  "error": "Not Found"
}
```

---

### 5. Get Expenses for Salesman within Date Range
**Endpoint**: `GET /api/daily-expenses/salesman-range?alias={alias}&startDate={startDate}&endDate={endDate}`

**Parameters**:
- `alias` (required, string): The salesman's alias
- `startDate` (required, string): Start date in ISO format (YYYY-MM-DD)
- `endDate` (required, string): End date in ISO format (YYYY-MM-DD)

**Example**:
```
GET /api/daily-expenses/salesman-range?alias=JOHN_DOE&startDate=2024-02-01&endDate=2024-02-28
```

**Response**:
```json
[
  {
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-01",
    "totalExpense": 1500.00
  },
  {
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-02",
    "totalExpense": 1200.00
  },
  ...
]
```

---

### 6. Get All Expenses within Date Range
**Endpoint**: `GET /api/daily-expenses/range?startDate={startDate}&endDate={endDate}`

**Parameters**:
- `startDate` (required, string): Start date in ISO format (YYYY-MM-DD)
- `endDate` (required, string): End date in ISO format (YYYY-MM-DD)

**Example**:
```
GET /api/daily-expenses/range?startDate=2024-02-01&endDate=2024-02-28
```

**Response** (sorted by date DESC, then alias ASC):
```json
[
  {
    "salesmanAlias": "JANE_SMITH",
    "expenseDate": "2024-02-28",
    "totalExpense": 950.00
  },
  {
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-28",
    "totalExpense": 1200.00
  },
  ...
]
```

---

### 7. Create Daily Expense Record
**Endpoint**: `POST /api/daily-expenses`

**Request Body**:
```json
{
  "salesmanAlias": "JOHN_DOE",
  "expenseDate": "2024-02-23",
  "totalExpense": 1500.00
}
```

**Response (200 OK)**:
```json
{
  "salesmanAlias": "JOHN_DOE",
  "expenseDate": "2024-02-23",
  "totalExpense": 1500.00
}
```

---

### 8. Update Daily Expense Record
**Endpoint**: `PUT /api/daily-expenses?alias={alias}&date={date}`

**Parameters**:
- `alias` (required, string): The salesman's alias
- `date` (required, string): Date in ISO format (YYYY-MM-DD)

**Request Body**:
```json
{
  "salesmanAlias": "JOHN_DOE",
  "expenseDate": "2024-02-23",
  "totalExpense": 1600.00
}
```

**Response (200 OK)**:
```json
{
  "salesmanAlias": "JOHN_DOE",
  "expenseDate": "2024-02-23",
  "totalExpense": 1600.00
}
```

**Response (404 Not Found)**:
```json
{
  "error": "Not Found"
}
```

---

### 9. Delete Daily Expense Record
**Endpoint**: `DELETE /api/daily-expenses?alias={alias}&date={date}`

**Parameters**:
- `alias` (required, string): The salesman's alias
- `date` (required, string): Date in ISO format (YYYY-MM-DD)

**Example**:
```
DELETE /api/daily-expenses?alias=JOHN_DOE&date=2024-02-23
```

**Response (204 No Content)**: Empty response indicating successful deletion

---

## Integration with Sales & Expense Entry

### Automatic Population Flow

When a user calls the **POST /api/sales/sales-expense** endpoint with expenses, the system automatically:

1. **Saves expenses** to `salesman_expenses` table (via SalesmanExpenseRepository)
2. **Aggregates expenses** by salesman_alias and expense_date
3. **Creates or updates** the corresponding record in `daily_expense_record` table
4. **Stores the total** of all expenses for that salesman on that date

### Example Request to `/api/sales/sales-expense`:
```json
{
  "salesmanAlias": "JOHN_DOE",
  "date": "2024-02-23",
  "expenses": [
    {
      "category": "Petrol",
      "amount": 500.00,
      "expenseDate": "2024-02-23"
    },
    {
      "category": "Food",
      "amount": 300.00,
      "expenseDate": "2024-02-23"
    },
    {
      "category": "Mobile",
      "amount": 200.00,
      "expenseDate": "2024-02-23"
    }
  ],
  "dailySales": [
    {
      "slNo": 1,
      "saleDate": "2024-02-23",
      "customerName": "ABC Shop",
      "customerType": "CUSTOMER",
      "village": "Bangalore",
      "mobileNumber": "9876543210",
      "productCode": "PROD001",
      "quantity": 5,
      "rate": 100.00
    }
  ]
}
```

### Automatic Behavior:
- Total expenses (500 + 300 + 200 = 1000.00) are aggregated
- A record is created/updated in `daily_expense_record`:
  ```json
  {
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-23",
    "totalExpense": 1000.00
  }
  ```

---

## Data Models

### DailyExpenseRecord (Entity)
```java
@Entity
@Table(name = "daily_expense_record")
@IdClass(DailyExpenseRecordId.class)
public class DailyExpenseRecord {
    @Id
    @Column(name = "salesman_alias")
    private String salesmanAlias;

    @Id
    @Column(name = "expense_date")
    private LocalDate expenseDate;

    @Column(name = "total_expense")
    private BigDecimal totalExpense;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters...
}
```

### DailyExpenseRecordId (Composite ID)
```java
public class DailyExpenseRecordId implements Serializable {
    private String salesmanAlias;
    private LocalDate expenseDate;
    
    // Getters, Setters, equals(), hashCode()...
}
```

### DailyExpenseRecordResponse (DTO)
```java
public class DailyExpenseRecordResponse {
    public String salesmanAlias;
    public LocalDate expenseDate;
    public BigDecimal totalExpense;
    
    // Getters and Setters...
}
```

---

## Repository Methods

The `DailyExpenseRecordRepository` provides the following custom query methods:

1. **findBySalesmanAlias(String salesmanAlias)**
   - Get all daily expense records for a specific salesman

2. **findByExpenseDate(LocalDate expenseDate)**
   - Get all daily expense records for a specific date

3. **findBySalesmanAliasAndExpenseDate(String salesmanAlias, LocalDate expenseDate)**
   - Get a specific daily expense record for a salesman on a date

4. **findBySalesmanAliasAndDateRange(String salesmanAlias, LocalDate startDate, LocalDate endDate)**
   - Get daily expense records for a salesman within a date range

5. **findByDateRange(LocalDate startDate, LocalDate endDate)**
   - Get all daily expense records within a date range

---

## Error Handling

### Common Error Responses

**400 Bad Request**:
```json
{
  "error": "Invalid request parameters"
}
```

**404 Not Found**:
```json
{
  "error": "Not Found"
}
```

**500 Internal Server Error**:
```json
{
  "error": "Internal server error",
  "message": "Error details..."
}
```

---

## Testing Examples

### Using cURL

**Get all expenses for a salesman:**
```bash
curl -X GET "http://localhost:8080/api/daily-expenses/salesman?alias=JOHN_DOE"
```

**Get expenses for a specific date:**
```bash
curl -X GET "http://localhost:8080/api/daily-expenses/date?date=2024-02-23"
```

**Get expense for salesman on specific date:**
```bash
curl -X GET "http://localhost:8080/api/daily-expenses/salesman-date?alias=JOHN_DOE&date=2024-02-23"
```

**Get expenses within date range:**
```bash
curl -X GET "http://localhost:8080/api/daily-expenses/salesman-range?alias=JOHN_DOE&startDate=2024-02-01&endDate=2024-02-28"
```

**Create a daily expense record:**
```bash
curl -X POST "http://localhost:8080/api/daily-expenses" \
  -H "Content-Type: application/json" \
  -d '{
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-23",
    "totalExpense": 1500.00
  }'
```

**Update a daily expense record:**
```bash
curl -X PUT "http://localhost:8080/api/daily-expenses?alias=JOHN_DOE&date=2024-02-23" \
  -H "Content-Type: application/json" \
  -d '{
    "salesmanAlias": "JOHN_DOE",
    "expenseDate": "2024-02-23",
    "totalExpense": 1600.00
  }'
```

**Delete a daily expense record:**
```bash
curl -X DELETE "http://localhost:8080/api/daily-expenses?alias=JOHN_DOE&date=2024-02-23"
```

---

## Implementation Summary

### Files Created/Modified:

1. **Database**: `createtable.sql`
   - Added `daily_expense_record` table with composite primary key

2. **Models**:
   - `DailyExpenseRecord.java` - JPA Entity
   - `DailyExpenseRecordId.java` - Composite ID class
   - `DailyExpenseRecordResponse.java` - Response DTO

3. **Repositories**:
   - `DailyExpenseRecordRepository.java` - JPA repository with custom query methods

4. **Controllers**:
   - `DailyExpenseController.java` - REST API endpoints
   - `DailySaleController.java` - Updated to auto-populate daily_expense_record

### Key Features:
✅ Composite primary key (salesman_alias + expense_date)
✅ Auto-aggregation from salesman_expenses
✅ Complete CRUD operations
✅ Advanced querying (by salesman, by date, by date range)
✅ Transactional integration with sales entry
✅ RESTful API with proper HTTP methods
✅ Date filtering with ISO format support
✅ Automatic timestamps (created_at, updated_at)

