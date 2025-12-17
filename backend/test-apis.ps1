# PowerShell script to test backend APIs
# Usage: .\test-apis.ps1

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080/api"

Write-Host "=== Testing Paisa Finance Platform APIs ===" -ForegroundColor Cyan
Write-Host ""

# Check if backend is running
Write-Host "Checking if backend is running..." -ForegroundColor Yellow
try {
    $testResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method Post -ErrorAction Stop -TimeoutSec 2
} catch {
    if ($_.Exception.Response) {
        # Backend is running (even if it returns error, it means server is up)
        Write-Host "Backend is running!" -ForegroundColor Green
    } else {
        Write-Host "ERROR: Backend is not running! Please start it first with: mvn spring-boot:run" -ForegroundColor Red
        exit 1
    }
}

# Step 1: Register
Write-Host ""
Write-Host "1. Registering new user..." -ForegroundColor Yellow
$registerBody = @{
    email = "test@example.com"
    password = "password123"
    name = "Test User"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$baseUrl/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody `
        -ErrorAction Stop
    
    Write-Host "SUCCESS: Registration successful!" -ForegroundColor Green
    Write-Host "  Email: $($registerResponse.email)" -ForegroundColor Gray
    Write-Host "  Name: $($registerResponse.name)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400 -or $statusCode -eq 409) {
        Write-Host "INFO: User already exists, continuing with login..." -ForegroundColor Yellow
    } else {
        Write-Host "ERROR: Registration failed - Status: $statusCode" -ForegroundColor Red
        Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Step 2: Login
Write-Host ""
Write-Host "2. Logging in..." -ForegroundColor Yellow
$loginBody = @{
    email = "test@example.com"
    password = "password123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody `
        -ErrorAction Stop
    
    if ($loginResponse.token) {
        $token = $loginResponse.token
        Write-Host "SUCCESS: Login successful!" -ForegroundColor Green
        Write-Host "  Token: $($token.Substring(0, [Math]::Min(20, $token.Length)))..." -ForegroundColor Gray
    } else {
        Write-Host "ERROR: No token received in response" -ForegroundColor Red
        exit 1
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "ERROR: Login failed - Status: $statusCode" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Set headers for authenticated requests
$headers = @{
    Authorization = "Bearer $token"
    "Content-Type" = "application/json"
}

# Step 3: Create Account
Write-Host ""
Write-Host "3. Creating account..." -ForegroundColor Yellow
$accountBody = @{
    name = "Main Account"
    type = "CURRENT"
    balance = 10000.00
    isDefault = $true
} | ConvertTo-Json

try {
    $accountResponse = Invoke-RestMethod -Uri "$baseUrl/accounts" `
        -Method Post `
        -Headers $headers `
        -Body $accountBody `
        -ErrorAction Stop
    
    $accountId = $accountResponse.id
    Write-Host "SUCCESS: Account created!" -ForegroundColor Green
    Write-Host "  Account ID: $accountId" -ForegroundColor Gray
    Write-Host "  Balance: Rs $($accountResponse.balance)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "ERROR: Account creation failed - Status: $statusCode" -ForegroundColor Red
    Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
}

# Step 4: Get Accounts
Write-Host ""
Write-Host "4. Getting all accounts..." -ForegroundColor Yellow
try {
    $accounts = Invoke-RestMethod -Uri "$baseUrl/accounts" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Host "SUCCESS: Found $($accounts.Count) account(s)" -ForegroundColor Green
    foreach ($acc in $accounts) {
        Write-Host "  - $($acc.name): Rs $($acc.balance)" -ForegroundColor Gray
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "ERROR: Failed to get accounts - Status: $statusCode" -ForegroundColor Red
}

# Step 5: Create Transaction (only if we have an account)
if ($accountId) {
    Write-Host ""
    Write-Host "5. Creating transaction..." -ForegroundColor Yellow
    $transactionBody = @{
        type = "EXPENSE"
        amount = 500.00
        description = "Grocery Shopping"
        category = "Food"
        date = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
        accountId = $accountId
        isRecurring = $false
    } | ConvertTo-Json
    
    try {
        $transactionResponse = Invoke-RestMethod -Uri "$baseUrl/transactions" `
            -Method Post `
            -Headers $headers `
            -Body $transactionBody `
            -ErrorAction Stop
        
        Write-Host "SUCCESS: Transaction created!" -ForegroundColor Green
        Write-Host "  Amount: Rs $($transactionResponse.amount)" -ForegroundColor Gray
        Write-Host "  Category: $($transactionResponse.category)" -ForegroundColor Gray
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "ERROR: Transaction creation failed - Status: $statusCode" -ForegroundColor Red
    }
}

# Step 6: Get Dashboard
Write-Host ""
Write-Host "6. Getting dashboard summary..." -ForegroundColor Yellow
try {
    $dashboard = Invoke-RestMethod -Uri "$baseUrl/dashboard" `
        -Method Get `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Host "SUCCESS: Dashboard data retrieved!" -ForegroundColor Green
    Write-Host "  Total Balance: Rs $($dashboard.totalBalance)" -ForegroundColor Gray
    Write-Host "  Monthly Income: Rs $($dashboard.monthlyIncome)" -ForegroundColor Gray
    Write-Host "  Monthly Expense: Rs $($dashboard.monthlyExpense)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "ERROR: Failed to get dashboard - Status: $statusCode" -ForegroundColor Red
}

# Step 7: Create Budget
Write-Host ""
Write-Host "7. Creating budget..." -ForegroundColor Yellow
$budgetBody = @{
    amount = 5000.00
} | ConvertTo-Json

try {
    $budgetResponse = Invoke-RestMethod -Uri "$baseUrl/budgets" `
        -Method Post `
        -Headers $headers `
        -Body $budgetBody `
        -ErrorAction Stop
    
    Write-Host "SUCCESS: Budget created!" -ForegroundColor Green
    Write-Host "  Budget Amount: Rs $($budgetResponse.amount)" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "ERROR: Budget creation failed - Status: $statusCode" -ForegroundColor Red
}

# Step 8: Get AI Recommendations
Write-Host ""
Write-Host "8. Getting AI recommendations..." -ForegroundColor Yellow
try {
    $aiResponse = Invoke-RestMethod -Uri "$baseUrl/ai/recommendations" `
        -Method Post `
        -Headers $headers `
        -ErrorAction Stop
    
    Write-Host "SUCCESS: AI recommendations received!" -ForegroundColor Green
    Write-Host "  Summary: $($aiResponse.summary)" -ForegroundColor Gray
    if ($aiResponse.recommendations) {
        Write-Host "  Recommendations:" -ForegroundColor Gray
        for ($i = 0; $i -lt $aiResponse.recommendations.Count; $i++) {
            Write-Host "    $($i + 1). $($aiResponse.recommendations[$i])" -ForegroundColor Gray
        }
    }
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "ERROR: AI recommendations failed - Status: $statusCode" -ForegroundColor Red
    Write-Host "  (This might fail if GEMINI_API_KEY is not set)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== API Testing Complete ===" -ForegroundColor Cyan
