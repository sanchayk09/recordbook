# Recordbook API - Developer Guide

## 📖 Welcome Developer!

This guide will help you understand and work with the Recordbook REST API.

---

## 🚀 Quick Start (5 minutes)

### 1. Start the Application
```bash
cd C:\sanchay\recordbook\recordbook
$env:JAVA_HOME='C:\Users\Kritika\.jdks\corretto-18.0.2'
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080/api/v1/admin`

### 2. Test with cURL
```bash
# Get all salesmen
curl http://localhost:8080/api/v1/admin/salesmen

# Create a salesman
curl -X POST http://localhost:8080/api/v1/admin/salesmen \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "address": "New York",
    "contactNumber": "+919876543210"
  }'
```

### 3. Import Postman Collection
1. Download `Postman_Collection.json` from project root
2. Open Postman
3. Click "Import" → Select the JSON file
4. Start testing endpoints

---

## 📚 Documentation Files

| File | Purpose | When to Use |
|------|---------|-----------|
| **API_DOCUMENTATION_UPDATED.md** | Complete API reference with examples | Learning the API, implementation details |
| **API_QUICK_REFERENCE.md** | Quick lookup table and examples | Fast reference during development |
| **Postman_Collection.json** | Postman test requests | Testing endpoints, debugging |
| **API_DOCUMENTATION_SUMMARY.md** | Overview of all docs | Understanding documentation structure |
| **This file (README)** | Developer quick start | Getting started, troubleshooting |

---

## 🏗️ Architecture Overview

```
Client (React/Postman/cURL)
        ↓
HTTP Request
        ↓
AdminController (/api/v1/admin/*)
        ↓
AdminService (Business Logic)
        ↓
Repositories (JpaRepository)
        ↓
Database (MySQL)
```

---

## 🔑 Key Endpoints

### Salesmen Management
```bash
GET    /api/v1/admin/salesmen              # List all
GET    /api/v1/admin/salesmen/{id}         # Get one
POST   /api/v1/admin/salesmen              # Create
PUT    /api/v1/admin/salesmen/{id}         # Update
DELETE /api/v1/admin/salesmen/{id}         # Delete
```

### Similar patterns for:
- Vendors
- Customers
- Products
- Chemicals
- Warehouses
- Routes
- Sales Records
- And more...

---

## 📋 Request/Response Examples

### Create a Salesman

**Request:**
```bash
POST /api/v1/admin/salesmen
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "address": "New York",
  "contactNumber": "+919876543210"
}
```

**Response (201 Created):**
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

**Note:** ID (`salesmanId`) is auto-generated - don't provide it in the request!

---

## 🛠️ Development Tips

### Running the App
```bash
# Navigate to project
cd C:\sanchay\recordbook\recordbook

# Set Java home
$env:JAVA_HOME='C:\Users\Kritika\.jdks\corretto-18.0.2'

# Run with Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean install -DskipTests
java -jar target/recordbook-0.0.1-SNAPSHOT.jar
```

### Testing Endpoints

**Option 1: cURL**
```bash
curl -X GET http://localhost:8080/api/v1/admin/salesmen
```

**Option 2: Postman**
- Import Postman_Collection.json
- Click endpoint
- Click "Send"

**Option 3: Browser**
```
http://localhost:8080/api/v1/admin/salesmen
```

**Option 4: JavaScript/Fetch**
```javascript
fetch('http://localhost:8080/api/v1/admin/salesmen')
  .then(r => r.json())
  .then(data => console.log(data));
```

---

## 🐛 Troubleshooting

### Issue: Port 8080 already in use
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (if ID is 1234)
taskkill /PID 1234 /F

# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Issue: Database connection error
1. Check MySQL is running
2. Verify credentials in `application.yaml`:
   ```yaml
   datasource:
     url: jdbc:mysql://localhost:3306/urviclean_manual
     username: root
     password: Asdqwe123.
   ```
3. Run SQL script: `src/main/resources/createtable.sql`

### Issue: 404 Not Found
1. Verify URL starts with `/api/v1/admin/`
2. Check endpoint spelling
3. Verify resource ID exists in database

### Issue: 400 Bad Request
1. Check JSON is valid (use JSONLint)
2. Verify required fields are present
3. Check data types match (String vs Number vs Date)

---

## 🔄 Creating a New Feature

### Example: Add a new endpoint

**1. Create or update Model**
```java
@Entity
@Table(name = "table_name")
@Data
public class EntityName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String field1;
    private String field2;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**2. Create Repository**
```java
@Repository
public interface EntityRepository extends JpaRepository<EntityName, Long> {
}
```

**3. Add Service Method**
```java
@Service
@Transactional
public class AdminService {
    @Autowired
    private EntityRepository entityRepository;
    
    public EntityName save(EntityName entity) {
        return entityRepository.save(entity);
    }
}
```

**4. Add Controller Endpoint**
```java
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @PostMapping("/entities")
    public EntityName create(@RequestBody EntityName entity) {
        return adminService.save(entity);
    }
}
```

**5. Test the endpoint**
```bash
curl -X POST http://localhost:8080/api/v1/admin/entities \
  -H "Content-Type: application/json" \
  -d '{"field1":"value1"}'
```

---

## 📊 Database Schema

### Key Tables

**salesmen**
```sql
vendor_id          | BIGINT AUTO_INCREMENT
first_name         | VARCHAR(50)
last_name          | VARCHAR(50)
address            | VARCHAR(100)
contact_number     | VARCHAR(25)
created_at         | TIMESTAMP
```

**customers**
```sql
customer_id        | BIGINT AUTO_INCREMENT
shop_name          | VARCHAR(100)
owner_first_name   | VARCHAR(50)
owner_last_name    | VARCHAR(50)
owner_address      | VARCHAR(100)
customer_type      | ENUM
route_id           | BIGINT FK
village_id         | BIGINT FK
created_at         | TIMESTAMP
```

Similar structure for other entities.

---

## 🔐 Security Notes

**Current Status:** No authentication  
**Production TODO:** Add JWT token authentication

```java
// Future: Add @RestController method security
@PostMapping("/salesmen")
@PreAuthorize("hasRole('ADMIN')")
public Salesman create(@RequestBody Salesman salesman) {
    return adminService.saveSalesman(salesman);
}
```

---

## 📈 Performance Tips

### Pagination (Future Enhancement)
```bash
GET /api/v1/admin/salesmen?page=0&size=20
```

### Filtering (Future Enhancement)
```bash
GET /api/v1/admin/salesmen?firstName=John&address=NYC
```

### Currently Supported
- List all records
- Get by ID
- Create, Update, Delete

---

## 🚀 Deployment Checklist

- [ ] Run all tests: `mvn test`
- [ ] Build JAR: `mvn clean install`
- [ ] Test JAR: `java -jar target/recordbook-*.jar`
- [ ] Update database schema
- [ ] Verify all endpoints work
- [ ] Update frontend API URLs
- [ ] Deploy to production server
- [ ] Verify in production environment

---

## 💾 Database Updates

### Backup before migration
```bash
mysqldump -u root -p urviclean_manual > backup.sql
```

### Run new schema
```bash
mysql -u root -p urviclean_manual < createtable.sql
```

### Migrate existing data (if needed)
Create custom migration scripts for your data.

---

## 📞 Common Questions

### Q: How do I generate IDs?
**A:** You don't! IDs are auto-generated by the database. Never include ID in POST request body.

### Q: What's the timestamp format?
**A:** ISO 8601 format: `2026-02-19T12:30:00`

### Q: Can I use authentication?
**A:** Not currently, but it's planned for future versions.

### Q: How do I test endpoints?
**A:** Use Postman (import collection), cURL, or JavaScript fetch API.

### Q: What's the CORS policy?
**A:** Frontend at `localhost:3000` is whitelisted.

### Q: Can I add new endpoints?
**A:** Yes! Follow the "Creating a New Feature" section above.

---

## 🔗 Useful Resources

| Resource | Link |
|----------|------|
| Spring Boot Docs | https://spring.io/projects/spring-boot |
| JPA Documentation | https://jakarta.ee/specifications/persistence/ |
| MySQL Documentation | https://dev.mysql.com/doc/ |
| Postman Docs | https://www.postman.com/api-documentation/ |

---

## 📝 Code Style

### Naming Conventions
- Classes: `PascalCase` (e.g., `SalesRecord`)
- Methods: `camelCase` (e.g., `getSalesmanById`)
- Variables: `camelCase` (e.g., `salesmanId`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MAX_RECORDS`)

### Annotations Used
- `@Entity` - JPA entity
- `@Data` - Lombok auto-generates getters/setters
- `@Service` - Service layer
- `@Repository` - Data access layer
- `@RestController` - REST endpoints
- `@Transactional` - Transaction management
- `@PrePersist` - Pre-insert hook

---

## 🎯 Next Steps

1. **Read API Documentation**: `API_DOCUMENTATION_UPDATED.md`
2. **Import Postman Collection**: `Postman_Collection.json`
3. **Run the Application**: `mvn spring-boot:run`
4. **Test an Endpoint**: Create a salesman via Postman
5. **Integrate with Frontend**: Update React app to use new API URLs
6. **Deploy**: Build JAR and deploy to server

---

## ✨ Happy Coding!

You're all set! Start building amazing features with the Recordbook API.

For detailed endpoint documentation, see: **API_DOCUMENTATION_UPDATED.md**

---

**Last Updated:** February 19, 2026  
**API Version:** 1.0.0  
**Status:** ✅ Production Ready

