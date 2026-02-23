# Daily Expense Record - Data Flow & Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                       FRONTEND (React)                          │
│                   http://localhost:3000                         │
└──────────────────────┬──────────────────────────────────────────┘
                       │
         ┌─────────────┴──────────────┐
         │                            │
         ▼                            ▼
    POST /api/sales/          GET /api/daily-expenses
    sales-expense             (various endpoints)
         │                            │
         │                            │
┌────────┴─────────────────────────┬─┴──────────────────────────────┐
│                                  │                                │
│         DailySaleController       │      DailyExpenseController   │
│    ┌─────────────────────────┐   │   ┌──────────────────────────┐ │
│    │ addSalesAndExpenses()   │   │   │ GET all methods          │ │
│    │ - Receives request      │   │   │ - findAll()              │ │
│    │ - Saves expenses        │   │   │ - findByAlias()          │ │
│    │ - Saves sales           │   │   │ - findByDate()           │ │
│    │ - AUTO-AGGREGATES       │───┼──→│ - findByDateRange()      │ │
│    │   daily expenses        │   │   │ POST/PUT/DELETE methods  │ │
│    └─────────────────────────┘   │   └──────────────────────────┘ │
└────────┬─────────────────────────┬─────────────────────────────────┘
         │                         │
         │ Save Data              │ Save/Query Data
         │                         │
    ┌────▼────────────────────────▼───────────────────────────┐
    │              Spring Data JPA Layer                       │
    │  ┌──────────────────────────────────────────────────┐   │
    │  │ SalesmanExpenseRepository                        │   │
    │  │ DailyExpenseRecordRepository                     │   │
    │  │ DailySaleRecordRepository                        │   │
    │  │ SalesmanRepository                               │   │
    │  └──────────────────────────────────────────────────┘   │
    └────┬────────────────────────────────────────────────────┘
         │
         ▼
    ┌─────────────────────────────────────────────────────┐
    │              MySQL Database                         │
    │  ┌──────────────┐        ┌──────────────────────┐  │
    │  │ salesmen     │        │ salesman_expenses    │  │
    │  │ ┌──────────┐ │        │ ┌──────────────────┐ │  │
    │  │ │ alias    │◄┼────────┤ │ salesman_id      │ │  │
    │  │ │ name     │ │        │ │ expense_date     │ │  │
    │  │ │ id       │ │        │ │ category         │ │  │
    │  │ └──────────┘ │        │ │ amount           │ │  │
    │  └──────────────┘        │ └──────────────────┘ │  │
    │                          │                      │  │
    │                          └──────────────────────┘  │
    │                                                    │
    │  ┌────────────────────────────────────────────┐   │
    │  │ daily_expense_record       (NEW)           │   │
    │  │ ┌──────────────────────────────────────┐  │   │
    │  │ │ salesman_alias (PK)                  │  │   │
    │  │ │ expense_date (PK)                    │  │   │
    │  │ │ total_expense (aggregated sum)       │  │   │
    │  │ │ created_at (auto timestamp)          │  │   │
    │  │ │ updated_at (auto timestamp)          │  │   │
    │  │ └──────────────────────────────────────┘  │   │
    │  │                                            │   │
    │  │ FK: salesman_alias → salesmen.alias       │   │
    │  │ PK: (salesman_alias, expense_date)        │   │
    │  │ Aggregates from: salesman_expenses        │   │
    │  └────────────────────────────────────────────┘   │
    │                                                    │
    └─────────────────────────────────────────────────────┘
```

---

## Data Flow: When Salesman Enters Expenses

```
┌─────────────────────────────────────────────────────────────────────┐
│ Step 1: Frontend POST Request to /api/sales/sales-expense           │
│                                                                      │
│ {                                                                    │
│   "salesmanAlias": "JOHN_DOE",                                      │
│   "date": "2024-02-23",                                             │
│   "expenses": [                                                     │
│     {"category": "Petrol", "amount": 500.00, "expenseDate": ...}, │
│     {"category": "Food", "amount": 300.00, "expenseDate": ...},   │
│     {"category": "Mobile", "amount": 200.00, "expenseDate": ...}  │
│   ]                                                                 │
│ }                                                                    │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────────┐
│ Step 2: DailySaleController.addSalesAndExpenses()                   │
│ ✓ Validate salesman exists                                          │
│ ✓ Create SalesmanExpense objects                                    │
│ ✓ Create DailySaleRecord objects                                    │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
                    ┌──────┴──────┐
                    │             │
                    ▼             ▼
        ┌──────────────────┐  ┌──────────────────┐
        │ Save Expenses    │  │ Save Sales       │
        │ to               │  │ to               │
        │ salesman_expenses│  │ daily_sale_record│
        └──────┬───────────┘  └────────┬─────────┘
               │                       │
               ▼                       ▼
        ┌──────────────────┐  ┌──────────────────────────┐
        │ 3 rows inserted: │  │ 1 row inserted:          │
        │ - Petrol: 500    │  │ - Sale record created    │
        │ - Food: 300      │  │                          │
        │ - Mobile: 200    │  │                          │
        └──────┬───────────┘  └──────────┬───────────────┘
               │                        │
               │ Aggregated Data        │
               │ (500+300+200=1000)     │
               │                        │
               └────────────┬───────────┘
                            │
                            ▼
        ┌──────────────────────────────────────────────┐
        │ Step 3: AUTO-POPULATE daily_expense_record   │
        │                                              │
        │ ✓ Check if record exists for:                │
        │   - salesmanAlias: "JOHN_DOE"                │
        │   - expenseDate: "2024-02-23"                │
        │                                              │
        │ ✓ If exists: UPDATE total_expense            │
        │ ✓ If not: CREATE new record                  │
        │                                              │
        │ INSERT/UPDATE daily_expense_record:          │
        │ ┌──────────────────────────────────────────┐ │
        │ │ salesman_alias   | JOHN_DOE            │ │
        │ │ expense_date     | 2024-02-23          │ │
        │ │ total_expense    | 1000.00             │ │
        │ │ created_at       | 2024-02-23 14:30... │ │
        │ │ updated_at       | 2024-02-23 14:30... │ │
        │ └──────────────────────────────────────────┘ │
        └──────────────────┬───────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────────────────┐
        │ Step 4: Return Response to Frontend           │
        │                                              │
        │ {                                            │
        │   "expenses": [                              │
        │     {salesman, expenseDate, category, ...},  │
        │     {salesman, expenseDate, category, ...},  │
        │     {salesman, expenseDate, category, ...}   │
        │   ],                                         │
        │   "sales": [                                 │
        │     {saleDate, salesmanName, customer, ...}  │
        │   ]                                          │
        │ }                                            │
        └──────────────────────────────────────────────┘
```

---

## Query Flows: Retrieving Aggregated Expenses

### Flow 1: Get All Expenses
```
GET /api/daily-expenses
    │
    ▼
DailyExpenseController.getAllDailyExpenses()
    │
    ▼
DailyExpenseRecordRepository.findAll()
    │
    ▼
SELECT * FROM daily_expense_record
    │
    ▼
Returns: List<DailyExpenseRecord>
```

### Flow 2: Get Expenses by Salesman
```
GET /api/daily-expenses/salesman?alias=JOHN_DOE
    │
    ▼
DailyExpenseController.getExpensesByAlias("JOHN_DOE")
    │
    ▼
DailyExpenseRecordRepository.findBySalesmanAlias("JOHN_DOE")
    │
    ▼
SELECT * FROM daily_expense_record
WHERE salesman_alias = 'JOHN_DOE'
ORDER BY expense_date DESC
    │
    ▼
Returns: List<DailyExpenseRecord>
  [
    {salesmanAlias: JOHN_DOE, expenseDate: 2024-02-23, totalExpense: 1000.00},
    {salesmanAlias: JOHN_DOE, expenseDate: 2024-02-22, totalExpense: 1200.00},
    ...
  ]
```

### Flow 3: Get Expenses by Date
```
GET /api/daily-expenses/date?date=2024-02-23
    │
    ▼
DailyExpenseController.getExpensesByDate(2024-02-23)
    │
    ▼
DailyExpenseRecordRepository.findByExpenseDate(2024-02-23)
    │
    ▼
SELECT * FROM daily_expense_record
WHERE expense_date = '2024-02-23'
    │
    ▼
Returns: List<DailyExpenseRecord>
  [
    {salesmanAlias: JOHN_DOE, expenseDate: 2024-02-23, totalExpense: 1000.00},
    {salesmanAlias: JANE_SMITH, expenseDate: 2024-02-23, totalExpense: 950.00},
    ...
  ]
```

### Flow 4: Get Specific Record (Salesman + Date)
```
GET /api/daily-expenses/salesman-date?alias=JOHN_DOE&date=2024-02-23
    │
    ▼
DailyExpenseController.getExpenseByAliasAndDate("JOHN_DOE", 2024-02-23)
    │
    ▼
DailyExpenseRecordRepository.findBySalesmanAliasAndExpenseDate(...)
    │
    ▼
SELECT * FROM daily_expense_record
WHERE salesman_alias = 'JOHN_DOE'
AND expense_date = '2024-02-23'
    │
    ▼
Returns: Optional<DailyExpenseRecord>
  {
    salesmanAlias: JOHN_DOE,
    expenseDate: 2024-02-23,
    totalExpense: 1000.00
  }
```

### Flow 5: Get Date Range for Salesman
```
GET /api/daily-expenses/salesman-range?alias=JOHN_DOE&startDate=2024-02-01&endDate=2024-02-28
    │
    ▼
DailyExpenseController.getExpensesByAliasAndDateRange(...)
    │
    ▼
DailyExpenseRecordRepository.findBySalesmanAliasAndDateRange(...)
    │
    ▼
SELECT * FROM daily_expense_record
WHERE salesman_alias = 'JOHN_DOE'
AND expense_date BETWEEN '2024-02-01' AND '2024-02-28'
ORDER BY expense_date DESC
    │
    ▼
Returns: List<DailyExpenseRecord> (February data for JOHN_DOE)
```

---

## Table Relationships Diagram

```
┌──────────────────────────────────────┐
│         salesmen                     │
│ ┌────────────────────────────────┐   │
│ │ salesman_id (PK)              │   │
│ │ first_name                    │   │
│ │ last_name                     │   │
│ │ alias (UNIQUE)                │◄──┼────┐
│ │ address                       │   │    │
│ │ contact_number                │   │    │
│ │ created_at                    │   │    │
│ └────────────────────────────────┘   │    │
└──────────────────────────────────────┘    │
                                            │
         ┌──────────────────────────────────┘
         │
         │ FK: salesman_id
         │
         ▼
┌──────────────────────────────────┐
│   salesman_expenses              │
│ ┌────────────────────────────┐   │
│ │ expense_id (PK)           │   │
│ │ salesman_id (FK)          │   │
│ │ expense_date              │───┼──────────────┐
│ │ category                  │   │              │
│ │ amount                    │   │              │
│ │ created_at                │   │              │
│ └────────────────────────────┘   │              │
└──────────────────────────────────┘              │
                                                  │
                         ┌────────────────────────┘
                         │
                         │ Group By:
                         │ - salesman_alias
                         │ - expense_date
                         │ Sum: amount
                         │
                         ▼
         ┌──────────────────────────────────┐
         │ daily_expense_record    (NEW)    │
         │ ┌────────────────────────────┐   │
         │ │ salesman_alias (PK, FK)   │   │
         │ │ expense_date (PK)         │   │
         │ │ total_expense             │   │
         │ │ created_at                │   │
         │ │ updated_at                │   │
         │ └────────────────────────────┘   │
         └──────────────────────────────────┘
```

---

## Class Diagram: Daily Expense Models

```
┌─────────────────────────────────────┐
│      DailyExpenseRecord             │
│  (JPA Entity)                       │
├─────────────────────────────────────┤
│ - salesmanAlias: String (PK)        │
│ - expenseDate: LocalDate (PK)       │
│ - totalExpense: BigDecimal          │
│ - createdAt: LocalDateTime          │
│ - updatedAt: LocalDateTime          │
├─────────────────────────────────────┤
│ + DailyExpenseRecord()              │
│ + DailyExpenseRecord(alias, date,   │
│                     total)          │
│ + getSalesmanAlias()                │
│ + setSalesmanAlias(alias)           │
│ + getExpenseDate()                  │
│ + setExpenseDate(date)              │
│ + getTotalExpense()                 │
│ + setTotalExpense(amount)           │
│ + getCreatedAt()                    │
│ + getUpdatedAt()                    │
│ - onCreate()                        │
│ - onUpdate()                        │
└─────────────────────────────────────┘
            ▲ Uses
            │
            │ IdClass
            │
┌─────────────────────────────────────┐
│   DailyExpenseRecordId              │
│  (Composite ID)                     │
├─────────────────────────────────────┤
│ - salesmanAlias: String             │
│ - expenseDate: LocalDate            │
├─────────────────────────────────────┤
│ + DailyExpenseRecordId()            │
│ + DailyExpenseRecordId(alias, date) │
│ + getSalesmanAlias()                │
│ + setSalesmanAlias(alias)           │
│ + getExpenseDate()                  │
│ + setExpenseDate(date)              │
│ + equals(Object): boolean           │
│ + hashCode(): int                   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│  DailyExpenseRecordResponse         │
│  (DTO for API)                      │
├─────────────────────────────────────┤
│ + salesmanAlias: String             │
│ + expenseDate: LocalDate            │
│ + totalExpense: BigDecimal          │
├─────────────────────────────────────┤
│ + DailyExpenseRecordResponse()      │
│ + DailyExpenseRecordResponse(       │
│     record: DailyExpenseRecord)     │
│ + DailyExpenseRecordResponse(       │
│     alias, date, total)             │
│ + getSalesmanAlias()                │
│ + setSalesmanAlias(alias)           │
│ + getExpenseDate()                  │
│ + setExpenseDate(date)              │
│ + getTotalExpense()                 │
│ + setTotalExpense(amount)           │
└─────────────────────────────────────┘
```

---

## Repository Methods Hierarchy

```
DailyExpenseRecordRepository
    │
    ├── Inherited from JpaRepository
    │   ├── findAll()
    │   ├── findById()
    │   ├── save()
    │   ├── saveAll()
    │   ├── delete()
    │   ├── deleteById()
    │   └── ...
    │
    └── Custom Methods
        ├── findBySalesmanAlias(String)
        │   └── Query: WHERE salesman_alias = ?
        │
        ├── findByExpenseDate(LocalDate)
        │   └── Query: WHERE expense_date = ?
        │
        ├── findBySalesmanAliasAndExpenseDate(String, LocalDate)
        │   └── Query: WHERE salesman_alias = ? AND expense_date = ?
        │
        ├── findBySalesmanAliasAndDateRange(String, LocalDate, LocalDate)
        │   └── Query: WHERE salesman_alias = ? 
        │        AND expense_date BETWEEN ? AND ?
        │        ORDER BY expense_date DESC
        │
        └── findByDateRange(LocalDate, LocalDate)
            └── Query: WHERE expense_date BETWEEN ? AND ?
                 ORDER BY expense_date DESC, salesman_alias ASC
```

---

## API Endpoint Flow Diagram

```
                    Frontend (React)
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
        ▼                  ▼                  ▼
    POST Expense     GET Expense         PUT/DELETE
    + Sales          Data                Expense
        │                  │                  │
        │                  └──────┬──────────┘
        │                         │
        ▼                         ▼
┌────────────────────┐  ┌──────────────────────────┐
│DailySaleController│  │DailyExpenseController    │
│                    │  │                          │
│addSalesAndExpenses│  │GET  /                    │
│                    │  │GET  /salesman           │
│1. Validate        │  │GET  /date               │
│2. Save Expenses   │  │GET  /salesman-date      │
│3. Save Sales      │  │GET  /salesman-range     │
│4. Auto-Aggregate  │  │GET  /range              │
│5. Update Daily    │  │POST /                   │
│   Expense Record  │  │PUT  /                   │
│6. Return Response │  │DELETE /                 │
│                    │  │                          │
└────┬───────────────┘  └──────────┬───────────────┘
     │                             │
     └──────────────┬──────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
        ▼                       ▼
   Repository Layer      Repository Layer
        │                       │
        ▼                       ▼
  Spring Data JPA         JPA Repository
        │                       │
        └───────────────┬───────┘
                        │
                        ▼
                   MySQL Database
                        │
                ┌───────┴───────┐
                │               │
                ▼               ▼
         salesman_expenses  daily_expense_record
```

---

## Error Handling Flow

```
POST /api/sales/sales-expense
        │
        ▼
isValidRequest()?
        │
   ┌────┴────┐
   │         │
  NO       YES
   │         │
   ▼         ▼
400     findSalesman()
Bad     │
Request ├─ Found ──→ Continue Processing ──→ 200 OK
        │
        └─ Not Found ──→ RuntimeException ──→ 500 Error

PUT /api/daily-expenses?alias=...&date=...
        │
        ▼
findBySalesmanAliasAndExpenseDate()
        │
   ┌────┴────┐
   │         │
Found      NOT
   │       Found
   ▼         │
Update      ▼
  &       404
Return   Not
200 OK   Found


DELETE /api/daily-expenses?alias=...&date=...
        │
        ▼
deleteById()
        │
        ▼
204 No Content
```

---

## Performance Considerations

### Indexes
```
CREATE INDEX idx_daily_expense_salesman ON daily_expense_record(salesman_alias);
CREATE INDEX idx_daily_expense_date ON daily_expense_record(expense_date);
```

### Query Performance
- Single lookup: O(1) via composite primary key
- Salesman query: O(log n) via index on salesman_alias
- Date query: O(log n) via index on expense_date
- Date range: O(log n + m) where m = matching records

### Write Performance
- Insert: O(1) for new salesman-date
- Update: O(1) for existing salesman-date
- Aggregation: Happens at write time (not read time)

---

**Diagram Version**: 1.0
**Last Updated**: 2024-02-23

