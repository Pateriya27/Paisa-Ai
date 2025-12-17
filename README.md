# Paisa - AI-Powered Finance Platform

A modern, enterprise-grade finance management platform built with React and Spring Boot.

## Tech Stack

### Frontend
- React 18 (SPA)
- React Router
- Axios
- Tailwind CSS
- Recharts
- Vite

### Backend
- Spring Boot 3.2.0
- Spring Security with JWT
- Spring Data JPA / Hibernate
- MySQL
- Google Gemini AI Integration
- WebClient for reactive HTTP

## Project Structure

```
.
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/paisa/
│   │   │   │   ├── controller/    # REST controllers
│   │   │   │   ├── service/        # Business logic
│   │   │   │   ├── repository/     # Data access layer
│   │   │   │   ├── entity/         # JPA entities
│   │   │   │   ├── dto/            # Data transfer objects
│   │   │   │   ├── security/       # Security configuration
│   │   │   │   └── util/           # Utility classes
│   │   │   └── resources/
│   │   │       └── application.yml
│   └── pom.xml
│
└── frontend/               # React SPA
    ├── src/
    │   ├── components/    # Reusable components
    │   ├── pages/         # Page components
    │   ├── context/       # React context
    │   ├── services/      # API services
    │   └── App.jsx
    ├── package.json
    └── vite.config.js
```

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+

### Backend Setup

1. Create MySQL database:
```sql
CREATE DATABASE paisa_finance;
```

2. Update `backend/src/main/resources/application.yml` with your database credentials:
```yaml
spring:
  datasource:
    username: your_username
    password: your_password
```

3. Set environment variables:
```bash
export JWT_SECRET=your-256-bit-secret-key-minimum-32-characters
export GEMINI_API_KEY=your-gemini-api-key
```

4. Build and run:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will run on `http://localhost:8080`

### Frontend Setup

1. Install dependencies:
```bash
cd frontend
npm install
```

2. Start development server:
```bash
npm run dev
```

Frontend will run on `http://localhost:3000`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Accounts
- `GET /api/accounts` - Get all user accounts
- `POST /api/accounts` - Create account
- `GET /api/accounts/{id}` - Get account by ID
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete account

### Transactions
- `GET /api/transactions` - Get all user transactions
- `POST /api/transactions` - Create transaction
- `GET /api/transactions/{id}` - Get transaction by ID
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction
- `GET /api/transactions/account/{accountId}` - Get transactions by account

### Budgets
- `GET /api/budgets` - Get user budget
- `POST /api/budgets` - Create/update budget
- `DELETE /api/budgets` - Delete budget

### Dashboard
- `GET /api/dashboard` - Get dashboard summary

### AI Recommendations
- `POST /api/ai/recommendations` - Get AI-powered recommendations

## Features

- ✅ User authentication with JWT
- ✅ Account management (Current/Savings)
- ✅ Transaction tracking (Income/Expense)
- ✅ Budget management
- ✅ Dashboard with analytics
- ✅ AI-powered financial recommendations
- ✅ Monthly budget alerts (scheduled job)
- ✅ Responsive UI with Tailwind CSS

## Security

- JWT-based authentication
- Password encryption with BCrypt
- Role-based access control
- CORS configuration
- Input validation

## License

MIT
