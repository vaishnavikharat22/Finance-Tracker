# Personal Finance & Expense Tracker - Backend API

A production-grade Spring Boot REST API for managing personal finances, expenses, and budgets.

## 🚀 Features

- **Secure Authentication**: JWT-based authentication with refresh tokens
- **Transaction Management**: Track income and expenses with categories
- **Budget Management**: Set and monitor budgets for categories or overall spending
- **Reports & Analytics**: Monthly/yearly summaries and category-wise spending
- **RESTful API**: Clean, well-documented REST endpoints
- **Production-Ready**: Error handling, validation, logging, and security best practices

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - Data Access Layer
- **MySQL/PostgreSQL** - Database
- **JWT** - Token-based authentication
- **MapStruct** - DTO Mapping
- **Lombok** - Boilerplate reduction
- **Swagger/OpenAPI** - API Documentation

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ or PostgreSQL 13+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## 🔧 Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd expense-tracker
```

### 2. Database Setup

#### MySQL:
```sql
CREATE DATABASE expense_tracker;
```

#### PostgreSQL:
```sql
CREATE DATABASE expense_tracker;
```

### 3. Configure Database

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/expense_tracker
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The API will be available at: `http://localhost:8080`

### 5. Access API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/api-docs`

## 📚 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `POST /api/v1/auth/login` - Login & get tokens
- `POST /api/v1/auth/refresh` - Refresh access token

### Transactions
- `GET /api/v1/transactions` - Get transactions (paginated)
- `POST /api/v1/transactions` - Create transaction
- `PUT /api/v1/transactions/{id}` - Update transaction
- `DELETE /api/v1/transactions/{id}` - Delete transaction

### Categories
- `GET /api/v1/categories` - Get all categories
- `POST /api/v1/categories` - Create category

### Budgets
- `GET /api/v1/budgets` - Get all budgets
- `POST /api/v1/budgets` - Create budget

### Reports
- `GET /api/v1/reports/summary` - Get summary
- `GET /api/v1/reports/monthly` - Monthly report

## 🧪 Testing

```bash
mvn test
```

## 📖 Project Structure

```
src/main/java/com/expensetracker/
├── config/          # Configuration classes
├── controller/      # REST Controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA Entities
├── repository/     # JPA Repositories
├── service/        # Business Logic
├── security/       # Security & JWT
├── exception/      # Exception Handlers
└── util/           # Utilities
```

## 🔐 Security

- JWT-based authentication
- BCrypt password hashing
- Role-based access control (future)
- CORS configuration
- Input validation

## 📝 License

This project is for educational purposes.

## 👨‍💻 Author

Built as a production-grade backend project for portfolio/resume.

