# API Testing Guide

## Prerequisites

1. **Start MySQL Database**
   - Ensure MySQL is running
   - Database `paisa_finance` will be created automatically

2. **Set Environment Variables**
   ```bash
   export JWT_SECRET=your-secret-key-minimum-32-characters-long-for-production
   export GEMINI_API_KEY=your-gemini-api-key
   ```

3. **Start the Backend**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Backend will run on: `http://localhost:8080/api`

## Testing Methods

### Method 1: Using cURL (Command Line)

#### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"test@example.com\",
    \"password\": \"password123\",
    \"name\": \"Test User\"
  }"
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"test@example.com\",
    \"password\": \"password123\"
  }"
```

**Save the token from the response!** You'll need it for authenticated requests.

#### 3. Get Dashboard Summary
```bash
curl -X GET http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### 4. Create Account
```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"Main Account\",
    \"type\": \"CURRENT\",
    \"balance\": 10000.00,
    \"isDefault\": true
  }"
```

#### 5. Get All Accounts
```bash
curl -X GET http://localhost:8080/api/accounts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### 6. Create Transaction
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d "{
    \"type\": \"EXPENSE\",
    \"amount\": 500.00,
    \"description\": \"Grocery Shopping\",
    \"category\": \"Food\",
    \"date\": \"2024-12-18T10:00:00\",
    \"accountId\": \"ACCOUNT_ID_FROM_PREVIOUS_RESPONSE\"
  }"
```

#### 7. Get All Transactions
```bash
curl -X GET http://localhost:8080/api/transactions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

#### 8. Create/Update Budget
```bash
curl -X POST http://localhost:8080/api/budgets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d "{
    \"amount\": 5000.00
  }"
```

#### 9. Get AI Recommendations
```bash
curl -X POST http://localhost:8080/api/ai/recommendations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Method 2: Using Postman

1. **Import Collection** (create manually or use the examples below)

2. **Set Environment Variables:**
   - `base_url`: `http://localhost:8080/api`
   - `token`: (will be set after login)

3. **Test Flow:**
   - Register → Login → Save token → Use token for other requests

### Method 3: Using PowerShell Script

Create a file `test-apis.ps1`:

```powershell
$baseUrl = "http://localhost:8080/api"

# Register
Write-Host "Registering user..."
$registerBody = @{
    email = "test@example.com"
    password = "password123"
    name = "Test User"
} | ConvertTo-Json

$registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $registerBody

Write-Host "Registered: $($registerResponse | ConvertTo-Json)"

# Login
Write-Host "Logging in..."
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody

$token = $loginResponse.token
Write-Host "Token: $token"

# Get Dashboard
Write-Host "Getting dashboard..."
$headers = @{
    Authorization = "Bearer $token"
}

$dashboard = Invoke-RestMethod -Uri "$baseUrl/dashboard" `
    -Method Get `
    -Headers $headers

Write-Host "Dashboard: $($dashboard | ConvertTo-Json)"
```

Run with: `powershell -ExecutionPolicy Bypass -File test-apis.ps1`

## API Endpoints Summary

### Authentication (No Auth Required)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Accounts (Auth Required)
- `GET /api/accounts` - Get all user accounts
- `POST /api/accounts` - Create account
- `GET /api/accounts/{id}` - Get account by ID
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete account

### Transactions (Auth Required)
- `GET /api/transactions` - Get all user transactions
- `POST /api/transactions` - Create transaction
- `GET /api/transactions/{id}` - Get transaction by ID
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Delete transaction
- `GET /api/transactions/account/{accountId}` - Get transactions by account

### Budgets (Auth Required)
- `GET /api/budgets` - Get user budget
- `POST /api/budgets` - Create/update budget
- `DELETE /api/budgets` - Delete budget

### Dashboard (Auth Required)
- `GET /api/dashboard` - Get dashboard summary

### AI Recommendations (Auth Required)
- `POST /api/ai/recommendations` - Get AI-powered recommendations

## Expected Response Formats

### Register/Login Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "test@example.com",
  "name": "Test User",
  "role": "USER"
}
```

### Account Response
```json
{
  "id": "uuid",
  "name": "Main Account",
  "type": "CURRENT",
  "balance": 10000.00,
  "isDefault": true,
  "createdAt": "2024-12-18T10:00:00",
  "updatedAt": "2024-12-18T10:00:00"
}
```

### Dashboard Response
```json
{
  "totalBalance": 10000.00,
  "monthlyIncome": 5000.00,
  "monthlyExpense": 2000.00,
  "budgetAmount": 5000.00,
  "budgetSpent": 2000.00,
  "accounts": [...],
  "recentTransactions": [...],
  "expensesByCategory": {
    "Food": 1000.00,
    "Transport": 500.00
  }
}
```

## Troubleshooting

1. **401 Unauthorized**: Check if token is valid and included in Authorization header
2. **404 Not Found**: Ensure backend is running on port 8080
3. **500 Internal Server Error**: Check application logs and database connection
4. **CORS Error**: Backend is configured for `http://localhost:3000` (frontend)

## Quick Test Sequence

1. Register user
2. Login and save token
3. Create account
4. Create transaction
5. Get dashboard
6. Create budget
7. Get AI recommendations

