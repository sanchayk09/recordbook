# Daily Expense Record - Quick Reference Guide

## What's New?

A new **daily_expense_record** table has been added to aggregate daily expenses per salesman by date.

---

## File Structure

```
src/main/java/com/urviclean/recordbook/
├── models/
│   ├── DailyExpenseRecord.java          [NEW] JPA Entity
│   ├── DailyExpenseRecordId.java        [NEW] Composite ID
│   └── DailyExpenseRecordResponse.java  [NEW] Response DTO
├── repositories/
│   └── DailyExpenseRecordRepository.java [NEW] JPA Repository
├── controllers/
│   ├── DailyExpenseController.java      [NEW] REST API
│   └── DailySaleController.java         [UPDATED] Auto-populates daily expenses
└── ...

src/main/resources/
└── createtable.sql                       [UPDATED] Added daily_expense_record table
```

---

## Database Schema

```sql
-- Composite Primary Key: (salesman_alias, expense_date)
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

---

## API Endpoints Quick Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/daily-expenses` | Get all daily expenses |
| GET | `/api/daily-expenses/salesman?alias=NAME` | Get expenses for a salesman |
| GET | `/api/daily-expenses/date?date=YYYY-MM-DD` | Get expenses for a date |
| GET | `/api/daily-expenses/salesman-date?alias=NAME&date=YYYY-MM-DD` | Get specific record |
| GET | `/api/daily-expenses/salesman-range?alias=NAME&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Date range for salesman |
| GET | `/api/daily-expenses/range?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Date range for all |
| POST | `/api/daily-expenses` | Create new record |
| PUT | `/api/daily-expenses?alias=NAME&date=YYYY-MM-DD` | Update record |
| DELETE | `/api/daily-expenses?alias=NAME&date=YYYY-MM-DD` | Delete record |

---

## Auto-Population Logic

When user calls: **POST `/api/sales/sales-expense`**

### Steps:
1. ✅ Saves expenses to `salesman_expenses` table
2. ✅ Aggregates all expenses by salesman_alias + expense_date
3. ✅ Creates/updates `daily_expense_record` with total
4. ✅ Saves daily sales to `daily_sale_record` table

### Example Request:
```json
{
  "salesmanAlias": "JOHN_DOE",
  "date": "2024-02-23",
  "expenses": [
    { "category": "Petrol", "amount": 500.00, "expenseDate": "2024-02-23" },
    { "category": "Food", "amount": 300.00, "expenseDate": "2024-02-23" },
    { "category": "Mobile", "amount": 200.00, "expenseDate": "2024-02-23" }
  ],
  "dailySales": [ /* sales data */ ]
}
```

### Automatic Result in daily_expense_record:
```json
{
  "salesmanAlias": "JOHN_DOE",
  "expenseDate": "2024-02-23",
  "totalExpense": 1000.00  /* 500 + 300 + 200 */
}
```

---

## Code Snippets

### Autowire the Repository
```java
@Autowired
private DailyExpenseRecordRepository dailyExpenseRecordRepository;
```

### Find existing record and update
```java
DailyExpenseRecord record = dailyExpenseRecordRepository
    .findBySalesmanAliasAndExpenseDate("JOHN_DOE", LocalDate.now())
    .orElse(new DailyExpenseRecord("JOHN_DOE", LocalDate.now(), BigDecimal.ZERO));

record.setTotalExpense(BigDecimal.valueOf(1500.00));
dailyExpenseRecordRepository.save(record);
```

### Get all expenses for a date range
```java
List<DailyExpenseRecord> records = dailyExpenseRecordRepository
    .findBySalesmanAliasAndDateRange("JOHN_DOE", 
        LocalDate.of(2024, 2, 1), 
        LocalDate.of(2024, 2, 28));
```

### Get all expenses within a date range for all salesmen
```java
List<DailyExpenseRecord> records = dailyExpenseRecordRepository
    .findByDateRange(
        LocalDate.of(2024, 2, 1), 
        LocalDate.of(2024, 2, 28));
```

---

## Frontend Integration

### Fetch all expenses for a salesman:
```javascript
const alias = "JOHN_DOE";
const response = await fetch(`http://localhost:8080/api/daily-expenses/salesman?alias=${alias}`);
const expenses = await response.json();
console.log(expenses);
```

### Fetch expenses for a specific date:
```javascript
const date = "2024-02-23";
const response = await fetch(`http://localhost:8080/api/daily-expenses/date?date=${date}`);
const expenses = await response.json();
console.log(expenses);
```

### Fetch expenses for a salesman on a specific date:
```javascript
const alias = "JOHN_DOE";
const date = "2024-02-23";
const response = await fetch(`http://localhost:8080/api/daily-expenses/salesman-date?alias=${alias}&date=${date}`);
const expense = await response.json();
console.log(expense);
```

### Fetch expenses for a date range:
```javascript
const alias = "JOHN_DOE";
const startDate = "2024-02-01";
const endDate = "2024-02-28";
const response = await fetch(
  `http://localhost:8080/api/daily-expenses/salesman-range?alias=${alias}&startDate=${startDate}&endDate=${endDate}`
);
const expenses = await response.json();
console.log(expenses);
```

---

## Key Changes to DailySaleController

### New Import:
```java
import com.urviclean.recordbook.models.DailyExpenseRecord;
import com.urviclean.recordbook.models.DailyExpenseRecordResponse;
import com.urviclean.recordbook.repositories.DailyExpenseRecordRepository;
```

### New Autowired Field:
```java
@Autowired
private DailyExpenseRecordRepository dailyExpenseRecordRepository;
```

### Updated `addSalesAndExpenses()` method:
- ✅ Aggregates expenses by salesman_alias and expense_date
- ✅ Creates or updates daily_expense_record with total
- ✅ Maintains transaction safety with @Transactional

---

## Testing with Postman

### Collection Setup

1. **Base URL**: `http://localhost:8080`

### Test Cases

**1. Get all daily expenses**
```
GET /api/daily-expenses
```

**2. Get expenses by salesman**
```
GET /api/daily-expenses/salesman?alias=JOHN_DOE
```

**3. Get expenses by date**
```
GET /api/daily-expenses/date?date=2024-02-23
```

**4. Get expense for salesman on specific date**
```
GET /api/daily-expenses/salesman-date?alias=JOHN_DOE&date=2024-02-23
```

**5. Get date range for salesman**
```
GET /api/daily-expenses/salesman-range?alias=JOHN_DOE&startDate=2024-02-01&endDate=2024-02-28
```

**6. Get all expenses in date range**
```
GET /api/daily-expenses/range?startDate=2024-02-01&endDate=2024-02-28
```

**7. Create expense record**
```
POST /api/daily-expenses
Content-Type: application/json

{
  "salesmanAlias": "JOHN_DOE",
  "expenseDate": "2024-02-23",
  "totalExpense": 1500.00
}
```

**8. Update expense record**
```
PUT /api/daily-expenses?alias=JOHN_DOE&date=2024-02-23
Content-Type: application/json

{
  "salesmanAlias": "JOHN_DOE",
  "expenseDate": "2024-02-23",
  "totalExpense": 1600.00
}
```

**9. Delete expense record**
```
DELETE /api/daily-expenses?alias=JOHN_DOE&date=2024-02-23
```

**10. Post sales with automatic expense aggregation**
```
POST /api/sales/sales-expense
Content-Type: application/json

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

---

## Troubleshooting

### Issue: Foreign Key Constraint Error
**Cause**: Salesman alias doesn't exist in salesmen table
**Solution**: Create salesman with the alias before creating expense record

### Issue: Duplicate Key Error
**Cause**: Trying to create duplicate (salesman_alias, expense_date) combination
**Solution**: Use PUT to update existing record or change the date

### Issue: Null total_expense in response
**Cause**: total_expense field is not set
**Solution**: Ensure expenses are provided and properly aggregated

---

## Performance Notes

- ✅ Indexed on `salesman_alias` for fast lookups
- ✅ Indexed on `expense_date` for date-based queries
- ✅ Composite primary key ensures uniqueness and quick access
- ✅ Aggregation happens at write time (not read time) for better performance

---

## Next Steps

1. Test all endpoints with Postman
2. Integrate with frontend for expense tracking UI
3. Add daily expense report generation
4. Create dashboard visualizations
5. Set up automated notifications for high expenses

---

## Support

For issues or questions, refer to:
- Full API documentation: `DAILY_EXPENSE_RECORD_API.md`
- Database schema: `createtable.sql`
- Source code comments in controller and entity classes

