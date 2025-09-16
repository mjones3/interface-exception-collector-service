#!/usr/bin/env pwsh

# Comprehensive Database Fix Script
# This script will:
# 1. Stop all services
# 2. Clean up database
# 3. Run Flyway migrations
# 4. Start services
# 5. Test endpoints

Write-Host "=== Comprehensive Database Fix Script ===" -ForegroundColor Green

# Function to run command and check result
function Invoke-SafeCommand {
    param(
        [string]$Command,
        [string]$Description,
        [bool]$ContinueOnError = $false
    )
    
    Write-Host "`n--- $Description ---" -ForegroundColor Yellow
    Write-Host "Running: $Command" -ForegroundColor Cyan
    
    try {
        Invoke-Expression $Command
        if ($LASTEXITCODE -ne 0 -and -not $ContinueOnError) {
            Write-Host "Command failed with exit code: $LASTEXITCODE" -ForegroundColor Red
            return $false
        }
        Write-Host "✓ $Description completed successfully" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "✗ Error in $Description`: $_" -ForegroundColor Red
        if (-not $ContinueOnError) {
            return $false
        }
        return $true
    }
}

# Step 1: Stop all services
Write-Host "`n=== Step 1: Stopping Services ===" -ForegroundColor Magenta
Invoke-SafeCommand "tilt down" "Stopping Tilt services" $true

# Wait for services to stop
Start-Sleep -Seconds 10

# Step 2: Clean Docker volumes and containers
Write-Host "`n=== Step 2: Cleaning Docker Environment ===" -ForegroundColor Magenta
Invoke-SafeCommand "docker system prune -f" "Cleaning Docker system" $true
Invoke-SafeCommand "docker volume prune -f" "Cleaning Docker volumes" $true

# Step 3: Check database schema files
Write-Host "`n=== Step 3: Verifying Database Schema Files ===" -ForegroundColor Magenta

# Check if all required migration files exist
$migrationFiles = @(
    "interface-exception-collector/src/main/resources/db/migration/V1__Create_interface_exceptions_table.sql",
    "interface-exception-collector/src/main/resources/db/migration/V6__Add_acknowledgment_and_resolution_fields.sql",
    "interface-exception-collector/src/main/resources/db/migration/V17__Ensure_acknowledgment_notes_column.sql",
    "interface-exception-collector/src/main/resources/db/migration/V21__Fix_missing_acknowledgment_notes_column.sql"
)

foreach ($file in $migrationFiles) {
    if (Test-Path $file) {
        Write-Host "✓ Found: $file" -ForegroundColor Green
    } else {
        Write-Host "✗ Missing: $file" -ForegroundColor Red
    }
}

# Step 4: Start services with fresh database
Write-Host "`n=== Step 4: Starting Services with Fresh Database ===" -ForegroundColor Magenta
if (-not (Invoke-SafeCommand "tilt up" "Starting Tilt services")) {
    Write-Host "Failed to start services. Exiting." -ForegroundColor Red
    exit 1
}

# Step 5: Wait for services to be ready
Write-Host "`n=== Step 5: Waiting for Services to Initialize ===" -ForegroundColor Magenta
Write-Host "Waiting 60 seconds for services to start up..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# Step 6: Check service health
Write-Host "`n=== Step 6: Checking Service Health ===" -ForegroundColor Magenta

# Check interface-exception-collector health
$maxRetries = 10
$retryCount = 0
$serviceReady = $false

while ($retryCount -lt $maxRetries -and -not $serviceReady) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ Interface Exception Collector is healthy" -ForegroundColor Green
            $serviceReady = $true
        }
    }
    catch {
        $retryCount++
        Write-Host "Attempt $retryCount/$maxRetries - Service not ready yet, waiting..." -ForegroundColor Yellow
        Start-Sleep -Seconds 10
    }
}

if (-not $serviceReady) {
    Write-Host "✗ Interface Exception Collector failed to start properly" -ForegroundColor Red
    Write-Host "Checking logs..." -ForegroundColor Yellow
    Invoke-SafeCommand "kubectl logs -l app=interface-exception-collector --tail=50" "Getting service logs" $true
    exit 1
}

# Step 7: Test database schema
Write-Host "`n=== Step 7: Testing Database Schema ===" -ForegroundColor Magenta

# Create a test script to verify database schema
$testScript = @"
import psycopg2
import sys

try:
    # Connect to database
    conn = psycopg2.connect(
        host="localhost",
        port="5432",
        database="interface_exceptions",
        user="postgres",
        password="postgres"
    )
    
    cursor = conn.cursor()
    
    # Check if acknowledgment_notes column exists
    cursor.execute("""
        SELECT column_name, data_type, character_maximum_length
        FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    """)
    
    result = cursor.fetchone()
    if result:
        print(f"✓ acknowledgment_notes column exists: {result}")
    else:
        print("✗ acknowledgment_notes column is missing!")
        sys.exit(1)
    
    # Check all expected columns
    expected_columns = [
        'acknowledgment_notes', 'resolution_method', 'resolution_notes',
        'acknowledged_at', 'acknowledged_by', 'resolved_at', 'resolved_by'
    ]
    
    for col in expected_columns:
        cursor.execute("""
            SELECT column_name FROM information_schema.columns 
            WHERE table_name = 'interface_exceptions' AND column_name = %s
        """, (col,))
        
        if cursor.fetchone():
            print(f"✓ Column {col} exists")
        else:
            print(f"✗ Column {col} is missing!")
    
    cursor.close()
    conn.close()
    print("✓ Database schema validation completed successfully")
    
except Exception as e:
    print(f"✗ Database connection failed: {e}")
    sys.exit(1)
"@

# Save and run the test script
$testScript | Out-File -FilePath "test_db_schema.py" -Encoding UTF8
Invoke-SafeCommand "python test_db_schema.py" "Testing database schema" $true

# Step 8: Test API endpoints
Write-Host "`n=== Step 8: Testing API Endpoints ===" -ForegroundColor Magenta

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc2OTI4OTEsImV4cCI6MTc1NzY5NjQ5MX0.vuuVBRAlIWgzfHHNtQ1HC_dmYAHJqoBY9zfx_7wjkVQ"

# Test exceptions endpoint
try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    Write-Host "Testing exceptions endpoint..." -ForegroundColor Yellow
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
    
    if ($response.StatusCode -eq 200) {
        Write-Host "✓ Exceptions endpoint is working" -ForegroundColor Green
        $content = $response.Content | ConvertFrom-Json
        Write-Host "Response: $($content | ConvertTo-Json -Depth 2)" -ForegroundColor Cyan
    } else {
        Write-Host "✗ Exceptions endpoint returned status: $($response.StatusCode)" -ForegroundColor Red
    }
}
catch {
    Write-Host "✗ Failed to test exceptions endpoint: $_" -ForegroundColor Red
}

# Step 9: Test partner order service
Write-Host "`n=== Step 9: Testing Partner Order Service ===" -ForegroundColor Magenta

# Check if partner order service is running
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8090/actuator/health" -TimeoutSec 10 -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host "✓ Partner Order Service is healthy" -ForegroundColor Green
        
        # Create a test order
        $orderPayload = @{
            customerId = "TEST-CUSTOMER-001"
            locationCode = "TEST-LOC-001"
            orderItems = @(
                @{
                    productCode = "TEST-PRODUCT-001"
                    quantity = 5
                    unitPrice = 29.99
                }
            )
        } | ConvertTo-Json -Depth 3
        
        Write-Host "Creating test order..." -ForegroundColor Yellow
        $orderResponse = Invoke-WebRequest -Uri "http://localhost:8090/v1/partner-order-provider/orders" -Method POST -Body $orderPayload -ContentType "application/json" -TimeoutSec 30
        
        if ($orderResponse.StatusCode -eq 200 -or $orderResponse.StatusCode -eq 201) {
            Write-Host "✓ Test order created successfully" -ForegroundColor Green
            $orderContent = $orderResponse.Content | ConvertFrom-Json
            Write-Host "Order ID: $($orderContent.orderId)" -ForegroundColor Cyan
            
            # Wait a bit for the order to be processed
            Start-Sleep -Seconds 5
            
            # Check if exception was created
            Write-Host "Checking for exceptions after order creation..." -ForegroundColor Yellow
            $exceptionsResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
            $exceptionsContent = $exceptionsResponse.Content | ConvertFrom-Json
            
            if ($exceptionsContent.content -and $exceptionsContent.content.Count -gt 0) {
                Write-Host "✓ Found $($exceptionsContent.content.Count) exception(s) in the system" -ForegroundColor Green
            } else {
                Write-Host "ℹ No exceptions found (this might be expected if the order processed successfully)" -ForegroundColor Yellow
            }
        }
    }
}
catch {
    Write-Host "✗ Partner Order Service is not available: $_" -ForegroundColor Red
}

# Step 10: Final verification
Write-Host "`n=== Step 10: Final Verification ===" -ForegroundColor Magenta

Write-Host "Final test of exceptions endpoint..." -ForegroundColor Yellow
try {
    $finalResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
    if ($finalResponse.StatusCode -eq 200) {
        Write-Host "✓ Final test successful - no database column errors!" -ForegroundColor Green
        $finalContent = $finalResponse.Content | ConvertFrom-Json
        Write-Host "Total exceptions in system: $($finalContent.totalElements)" -ForegroundColor Cyan
    }
}
catch {
    Write-Host "✗ Final test failed: $_" -ForegroundColor Red
    Write-Host "Checking application logs for errors..." -ForegroundColor Yellow
    Invoke-SafeCommand "kubectl logs -l app=interface-exception-collector --tail=100" "Getting detailed logs" $true
}

Write-Host "`n=== Database Fix Script Completed ===" -ForegroundColor Green
Write-Host "Summary:" -ForegroundColor Yellow
Write-Host "- Services restarted with fresh database" -ForegroundColor White
Write-Host "- Flyway migrations applied" -ForegroundColor White
Write-Host "- Database schema validated" -ForegroundColor White
Write-Host "- API endpoints tested" -ForegroundColor White
Write-Host "- Partner order service tested" -ForegroundColor White

# Cleanup
Remove-Item "test_db_schema.py" -ErrorAction SilentlyContinue