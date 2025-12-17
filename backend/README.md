# Backend - Spring Boot Application

## Running the Application

1. Ensure MySQL is running and create the database:
```sql
CREATE DATABASE paisa_finance;
```

2. Set environment variables:
```bash
export JWT_SECRET=your-secret-key-minimum-32-characters
export GEMINI_API_KEY=your-gemini-api-key
```

3. Update `src/main/resources/application.yml` with your MySQL credentials

4. Run:
```bash
mvn spring-boot:run
```

## Architecture

- **Controller Layer**: REST endpoints
- **Service Layer**: Business logic
- **Repository Layer**: Data access (JPA)
- **Entity Layer**: Domain models
- **DTO Layer**: Data transfer objects
- **Security Layer**: JWT authentication and authorization

## Database

Tables are automatically created by Hibernate on startup (`ddl-auto: update`).

## Scheduled Jobs

Monthly budget alerts run on the 1st of every month at 9 AM.

