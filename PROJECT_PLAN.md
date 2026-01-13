# Personal Finance & Expense Tracker - Project Plan

## 🎯 Project Goals

**Primary Goal**: Build a production-grade Spring Boot REST API backend for a Personal Finance & Expense Tracker application.

**Target Users**: 
- Individual users managing personal finances
- Future extension: Multi-user support with family/shared budgets

**Success Criteria**:
- Secure authentication & authorization
- Scalable architecture handling 10,000+ transactions
- Clean, maintainable code following Spring Boot best practices
- Production-ready error handling & validation
- Comprehensive API documentation
- Test coverage suitable for resume/portfolio

---

## 🏗️ System Architecture

### Layered Architecture (Clean Architecture Principles)

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (REST Controllers, DTOs, Validation)   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Business Logic Layer            │
│     (Services, Domain Logic)            │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Data Access Layer               │
│  (Repositories, JPA Entities, Queries)  │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Database Layer                  │
│      (MySQL/PostgreSQL)                  │
└─────────────────────────────────────────┘
```

### Package Structure

```
com.expensetracker
├── config/          # Configuration classes (Security, JPA, etc.)
├── controller/      # REST Controllers
├── dto/            # Data Transfer Objects (Request/Response)
├── entity/         # JPA Entities
├── repository/     # JPA Repositories
├── service/        # Business Logic Services
│   ├── impl/      # Service Implementations
├── security/      # Security configuration & JWT
│   ├── jwt/      # JWT utilities
├── exception/     # Custom exceptions & global handlers
├── util/          # Utility classes
└── validation/    # Custom validators
```

### Request Flow

```
HTTP Request
    ↓
Controller (validates DTO, maps to domain)
    ↓
Service Layer (business logic, validation)
    ↓
Repository (data access, queries)
    ↓
Database
    ↓
Response flows back through layers
    ↓
Controller returns DTO
```

**Why This Architecture?**
- **Separation of Concerns**: Each layer has a single responsibility
- **Testability**: Easy to mock dependencies
- **Maintainability**: Changes in one layer don't affect others
- **Scalability**: Can optimize each layer independently

---

## 🗄️ Database Design

### Core Entities

#### 1. **User**
```sql
users
├── id (PK, BIGINT, AUTO_INCREMENT)
├── email (UNIQUE, VARCHAR(100), INDEXED)
├── password_hash (VARCHAR(255))
├── first_name (VARCHAR(50))
├── last_name (VARCHAR(50))
├── enabled (BOOLEAN, DEFAULT TRUE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

#### 2. **Category**
```sql
categories
├── id (PK, BIGINT, AUTO_INCREMENT)
├── user_id (FK → users.id, NULLABLE for default categories)
├── name (VARCHAR(100), NOT NULL)
├── type (ENUM: 'INCOME', 'EXPENSE')
├── icon (VARCHAR(50), optional)
├── color (VARCHAR(7), optional)
├── is_default (BOOLEAN, DEFAULT FALSE)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (user_id, type)
```

#### 3. **Transaction**
```sql
transactions
├── id (PK, BIGINT, AUTO_INCREMENT)
├── user_id (FK → users.id, NOT NULL, INDEXED)
├── category_id (FK → categories.id, NOT NULL)
├── amount (DECIMAL(15,2), NOT NULL)
├── type (ENUM: 'INCOME', 'EXPENSE')
├── description (VARCHAR(500))
├── transaction_date (DATE, NOT NULL, INDEXED)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEXES:
- (user_id, transaction_date) - for date range queries
- (user_id, type) - for filtering by type
- (category_id) - for category-based queries
```

#### 4. **Budget**
```sql
budgets
├── id (PK, BIGINT, AUTO_INCREMENT)
├── user_id (FK → users.id, NOT NULL, INDEXED)
├── category_id (FK → categories.id, NULLABLE - NULL = total budget)
├── amount (DECIMAL(15,2), NOT NULL)
├── period_type (ENUM: 'MONTHLY', 'YEARLY', 'CUSTOM')
├── start_date (DATE, NOT NULL)
├── end_date (DATE, NULLABLE for monthly/yearly)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)

INDEX: (user_id, start_date, end_date)
UNIQUE: (user_id, category_id, start_date) - one budget per category per period
```

### ER Diagram (Text-based)

```
┌─────────────┐
│    User     │
│─────────────│
│ id (PK)     │
│ email       │
│ password    │
│ ...         │
└──────┬──────┘
       │ 1
       │
       │ *
┌──────▼──────────┐      ┌──────────────┐
│  Transaction    │      │   Category   │
│─────────────────│      │──────────────│
│ id (PK)         │      │ id (PK)      │
│ user_id (FK)    │──────│ user_id (FK) │
│ category_id(FK) │  *   │ name         │
│ amount          │      │ type         │
│ type            │      │ ...          │
│ date            │      └──────────────┘
└─────────────────┘
       │
       │ *
┌──────▼──────┐
│   Budget    │
│─────────────│
│ id (PK)     │
│ user_id(FK) │
│ category_id │
│ amount      │
│ period      │
└─────────────┘
```

### Indexing Strategy

**Why These Indexes?**
- `user_id` indexes: Most queries filter by user (security + performance)
- `transaction_date` index: Date range queries for reports
- Composite indexes: Support common query patterns (user + date, user + type)
- `email` unique index: Fast login lookups + data integrity

**Normalization**: 
- 3NF (Third Normal Form) - eliminates redundancy
- Categories can be shared (default) or user-specific
- Budgets reference categories but can be category-agnostic (total budget)

---

## 🔌 API Design (RESTful)

### Base URL: `/api/v1`

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Register new user | No |
| POST | `/auth/login` | Login & get JWT tokens | No |
| POST | `/auth/refresh` | Refresh access token | No |
| POST | `/auth/logout` | Logout (invalidate token) | Yes |
| POST | `/auth/forgot-password` | Request password reset | No |
| POST | `/auth/reset-password` | Reset password with token | No |

### User Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/users/me` | Get current user profile | Yes |
| PUT | `/users/me` | Update user profile | Yes |
| DELETE | `/users/me` | Delete account | Yes |

### Category Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/categories` | Get all user categories | Yes |
| GET | `/categories/{id}` | Get category by ID | Yes |
| POST | `/categories` | Create new category | Yes |
| PUT | `/categories/{id}` | Update category | Yes |
| DELETE | `/categories/{id}` | Delete category | Yes |

### Transaction Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/transactions` | Get transactions (paginated, filtered) | Yes |
| GET | `/transactions/{id}` | Get transaction by ID | Yes |
| POST | `/transactions` | Create new transaction | Yes |
| PUT | `/transactions/{id}` | Update transaction | Yes |
| DELETE | `/transactions/{id}` | Delete transaction | Yes |

**Query Parameters for GET /transactions:**
- `page` (default: 0)
- `size` (default: 20, max: 100)
- `type` (INCOME/EXPENSE)
- `categoryId`
- `startDate` (ISO format: YYYY-MM-DD)
- `endDate` (ISO format: YYYY-MM-DD)
- `sort` (default: transactionDate,desc)

### Budget Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/budgets` | Get all budgets | Yes |
| GET | `/budgets/{id}` | Get budget by ID | Yes |
| GET | `/budgets/current` | Get current active budgets | Yes |
| POST | `/budgets` | Create new budget | Yes |
| PUT | `/budgets/{id}` | Update budget | Yes |
| DELETE | `/budgets/{id}` | Delete budget | Yes |

### Reports Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/reports/summary` | Get summary (total income/expense) | Yes |
| GET | `/reports/monthly` | Get monthly report | Yes |
| GET | `/reports/yearly` | Get yearly report | Yes |
| GET | `/reports/by-category` | Get spending by category | Yes |

**Query Parameters:**
- `year` (for yearly/monthly)
- `month` (for monthly)
- `startDate` / `endDate` (for custom ranges)

### HTTP Status Codes

- `200 OK` - Successful GET, PUT, DELETE
- `201 Created` - Successful POST
- `400 Bad Request` - Validation errors, bad input
- `401 Unauthorized` - Missing/invalid JWT token
- `403 Forbidden` - Valid token but insufficient permissions
- `404 Not Found` - Resource doesn't exist
- `409 Conflict` - Duplicate resource (e.g., duplicate email)
- `500 Internal Server Error` - Server errors

### Response Format

**Success Response:**
```json
{
  "data": { ... },
  "message": "Success message",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Error Response:**
```json
{
  "error": "Error type",
  "message": "Human-readable error message",
  "details": [ "Validation error details" ],
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/transactions"
}
```

**Paginated Response:**
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

---

## 🔐 Security Architecture

### Authentication Flow

1. **Registration**:
   - User provides email, password, name
   - Password is hashed using BCrypt (strength: 10)
   - User is created with `enabled = true`
   - Email verification (optional for MVP)

2. **Login**:
   - User provides email + password
   - System validates credentials
   - On success: Generate JWT Access Token + Refresh Token
   - Access Token: Short-lived (15 minutes)
   - Refresh Token: Long-lived (7 days), stored in database

3. **Token Refresh**:
   - Client sends refresh token
   - System validates refresh token
   - Issues new access token
   - Optionally rotates refresh token

4. **Request Authentication**:
   - Client sends: `Authorization: Bearer <access_token>`
   - Spring Security validates token
   - Extracts user info from token claims
   - Sets SecurityContext

### Security Components

- **JWT Token Structure**:
  - Header: Algorithm (HS256)
  - Payload: userId, email, roles, issuedAt, expiration
  - Signature: HMAC-SHA256

- **Password Security**:
  - BCrypt hashing (cost factor: 10)
  - Minimum 8 characters (enforced in validation)
  - No password storage in plain text

- **Token Storage**:
  - Access Token: Client-side (memory/localStorage)
  - Refresh Token: Database table `refresh_tokens`
  - Tokens can be revoked

### Security Best Practices Implemented

✅ Password hashing (BCrypt)  
✅ JWT with expiration  
✅ Refresh token rotation  
✅ CORS configuration  
✅ Input validation & sanitization  
✅ SQL injection prevention (JPA parameterized queries)  
✅ XSS prevention (proper DTO mapping)  
✅ Rate limiting (can be added with Spring Security)  

---

## ⚡ Performance & Scalability

### Query Optimization

1. **N+1 Query Prevention**:
   - Use `@EntityGraph` for eager loading relationships
   - Use `JOIN FETCH` in custom queries
   - DTO projections instead of full entities

2. **Pagination**:
   - Spring Data JPA `Pageable`
   - Limit page size (max 100)
   - Indexed columns for sorting

3. **Indexing Strategy**:
   - Index on `user_id` (most queries filter by user)
   - Index on `transaction_date` (date range queries)
   - Composite indexes for common query patterns

4. **Caching** (Future Enhancement):
   - Cache user categories (rarely change)
   - Cache default categories
   - Use Spring Cache with Redis

### Handling 10,000+ Transactions

- **Pagination**: Always paginate list endpoints
- **Indexed Queries**: All date/user filters use indexes
- **DTO Mapping**: Return only needed fields
- **Batch Operations**: For bulk imports (future)
- **Database Connection Pooling**: HikariCP (default in Spring Boot)

---

## 🛠️ Error Handling Strategy

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handle validation errors
    // Handle authentication errors
    // Handle business logic errors
    // Handle database errors
    // Return consistent error format
}
```

### Exception Types

- `ResourceNotFoundException` → 404
- `ValidationException` → 400
- `AuthenticationException` → 401
- `AuthorizationException` → 403
- `DuplicateResourceException` → 409
- `GenericException` → 500

### Validation

- Bean Validation (`@Valid`, `@NotNull`, `@Size`, etc.)
- Custom validators for business rules
- Clear error messages

---

## 🧪 Testing Strategy

### Unit Tests
- Service layer logic
- Utility classes
- DTO mappers

### Integration Tests
- Repository layer (with TestContainers or H2)
- Controller layer (MockMvc)
- Security configuration

### Test Coverage Goals
- Service layer: 80%+
- Controller layer: Critical endpoints only
- Repository: Custom queries only

### Key Tests
- ✅ User registration & login
- ✅ Transaction CRUD operations
- ✅ Budget creation & validation
- ✅ Report generation
- ✅ Security: Unauthorized access attempts

---

## 📋 Build Roadmap

### Phase 1: Project Setup & Foundation (Week 1)
- [x] Initialize Spring Boot project
- [ ] Configure database connection
- [ ] Set up project structure (packages)
- [ ] Configure logging
- [ ] Add dependencies (Security, JWT, Validation)

### Phase 2: Authentication & Security (Week 1-2)
- [ ] User entity & repository
- [ ] Spring Security configuration
- [ ] JWT token generation & validation
- [ ] Registration & login endpoints
- [ ] Refresh token mechanism
- [ ] Password reset flow

### Phase 3: Core Entities & Database (Week 2)
- [ ] Category entity & repository
- [ ] Transaction entity & repository
- [ ] Budget entity & repository
- [ ] Database migrations (Flyway/Liquibase)
- [ ] Seed default categories

### Phase 4: Transaction APIs (Week 2-3)
- [ ] Transaction DTOs
- [ ] Transaction service & business logic
- [ ] Transaction controller
- [ ] Pagination & filtering
- [ ] Validation & error handling

### Phase 5: Category & Budget APIs (Week 3)
- [ ] Category CRUD endpoints
- [ ] Budget CRUD endpoints
- [ ] Budget validation logic
- [ ] Budget vs actual spending calculations

### Phase 6: Reports & Analytics (Week 3-4)
- [ ] Summary endpoint
- [ ] Monthly/yearly reports
- [ ] Category-wise spending reports
- [ ] Query optimization for reports

### Phase 7: Security Hardening & Optimization (Week 4)
- [ ] Security audit
- [ ] Query optimization (N+1 prevention)
- [ ] Add indexes
- [ ] Performance testing
- [ ] Error handling refinement

### Phase 8: Testing & Documentation (Week 4-5)
- [ ] Unit tests
- [ ] Integration tests
- [ ] API documentation (Swagger/OpenAPI)
- [ ] README with setup instructions
- [ ] Code cleanup & refactoring

---

## 📝 Resume Bullet Points (Draft)

- **Built a production-grade REST API** for a Personal Finance Tracker using Spring Boot, handling 10,000+ transactions with optimized queries and pagination
- **Implemented secure authentication** using JWT tokens with refresh token rotation, BCrypt password hashing, and Spring Security
- **Designed scalable database schema** with proper indexing strategy, achieving sub-100ms query times for date-range filtered transactions
- **Developed comprehensive error handling** with global exception handlers, custom validation, and consistent API response formats
- **Created RESTful APIs** following industry best practices with proper HTTP status codes, pagination, filtering, and DTO-based request/response handling

---

## 🎓 Interview Talking Points

### "Why did you design it this way?"

**Layered Architecture:**
- Separation of concerns makes code maintainable and testable
- Easy to swap implementations (e.g., change database)
- Follows SOLID principles

**JWT Authentication:**
- Stateless authentication scales better than session-based
- Refresh tokens provide security with good UX
- Industry standard for REST APIs

**Database Indexing:**
- User_id indexes ensure data isolation and fast queries
- Date indexes optimize report generation
- Composite indexes support common query patterns

**DTO Pattern:**
- Prevents exposing internal entity structure
- Allows versioning API independently
- Better performance (only fetch needed fields)

---

## 📚 Next Steps

**Before we start coding, please confirm:**

1. **Database Choice**: MySQL or PostgreSQL?
2. **Experience Level**: 
   - Beginner (need detailed explanations)
   - Intermediate (familiar with Spring Boot basics)
   - Advanced (need architecture guidance)
3. **Timeline**: How many weeks do you have?
4. **Build Tool**: Maven or Gradle? (I'll use Maven by default)

Once confirmed, I'll start building the project step-by-step! 🚀

