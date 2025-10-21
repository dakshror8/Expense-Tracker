# Expense-Tracker
Personal Expense Tracker
A comprehensive RESTful API for personal expense tracking and budget management built with Spring Boot 3.x, Spring Security 6.x, and JWT authentication.

**Features**

<details>

<summary>Authentication & Security</summary>

JWT-based authentication with stateless sessions\
User registration and login with validation\
Password encryption using BCrypt\
Role-based access control (RBAC)\
Secure API endpoints with token validation

</details>

<details>

<summary>Expense Management</summary>

Complete CRUD operations for expenses\
12 predefined expense categories\
Advanced filtering (by date, category, date range)\
Pagination and sorting support\
Comprehensive expense analytics and summaries

</details>

<details>

<summary>Budget Management</summary>

Create and manage monthly budgets by category\
Real-time budget tracking and monitoring\
Budget vs actual spending comparison\
Overspending alerts and notifications\
Budget utilization percentage tracking

</details>

<details>

<summary>Analytics & Reporting</summary>

Total expense summaries with statistics\
Category-wise spending breakdown with percentages\
Date range analytics\
Min, Max, and Average calculations\
Budget performance reports\

</details>

**Tech Stack**

**Framework:** Spring Boot 3.2.x\
**Security:** Spring Security 6.x with JWT\
**Database:** PostgreSQL 14+\
**ORM:** Spring Data JPA with Hibernate\
**Authentication:** JJWT 0.11.5\
**Validation:** Jakarta Validation\
**Build Tool:** Maven\
**Java Version:** 17+

**Prerequisites**
Before you begin, ensure you have the following installed:

Java 17 or higher\
Maven 3.6+\
PostgreSQL 14+ (or Docker)\
Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

1. Clone the Repository

   ```bash
   git clone https://github.com/yourusername/expense-tracker-backend.git
    cd expense-tracker-backend
   ```
2. Set up PostgreSQL Database\
Option A: Local PostgreSQL Installation

    ```bash
    # Connect to PostgreSQL
    psql -U postgres

    # Create database
    CREATE DATABASE expense_tracker;

    # Create user (optional)
    CREATE USER expense_user WITH PASSWORD 'your_password';
    GRANT ALL PRIVILEGES ON DATABASE expense_tracker TO expense_user;

    # Exit
    \q
    ```

3. Configure Database\
   Edit: ```src/main/resources/application.properties/```
   ```bash
   # PostgreSQL Configuration
    spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
    spring.datasource.username=expense_user
    spring.datasource.password=your_password
    spring.datasource.driver-class-name=org.postgresql.Driver

    # JPA Configuration
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.format_sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
    spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

    # Server Configuration
    server.port=8080

    # Logging
    logging.level.org.springframework.security=DEBUG
    logging.level.com.example.security=DEBUG
    ```
4. Add PostgreSQL Dependency\
Ensure ```pom.xml``` includes:
    ```bash
    <dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
    </dependency>
    ```
5. Generate JWT Secret Key
   ```bash
   # Generate a secure 256-bit base64 encoded key
    openssl rand -base64 32
   ```
Add to ```application.properties```:
  ```bash
    # JWT Configuration
    jwt.secret.key=YOUR_GENERATED_SECRET_KEY_HERE
    jwt.expiration=86400000
  ```

**Troubleshooting**\
Error: ```Could not open JDBC Connection```\
Solution:

Verify PostgreSQL is running: ```pg_isready -h localhost -p 5432```\
Check credentials in ```application.properties```\
Ensure database exists: ```psql -U postgres -c "\l"```

JWT Token Issues\
Error: ```401 Unauthorized```\
Solution:

Verify token is included in header: ```Authorization: Bearer <token>```\
Check token hasn't expired (24 hour default)\
Ensure JWT secret key is configured correctly

Port Already in Use\
Error: ```Port 8080 is already in use```\
Solution:
  ```bash
     # Find process using port 8080
      lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.properties
server.port=8081
  ```
