Write-Host "=== Testing Hybrid Partner Order Service Flow ===" -ForegroundColor Green

# Configuration
$IEC_URL = "http://localhost:8080"
$PARTNER_ORDER_URL = "http://localhost:8090"

Write-Host "`n1. Checking service availability..." -ForegroundColor Cyan

# Check Interface Exception Collector
try {
    $iecHealth = Invoke-RestMethod -Uri "$IEC_URL/actuator/health" -Method GET
    Write-Host "✓ Interface Exception Collector: $($iecHealth.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ Interface Exception Collector not available: $($_.Exception.Message)" -ForegroundColor Red
}

# Check Partner Order Service
try {
    $partnerHealth = Invoke-RestMethod -Uri "$PARTNER_ORDER_URL/actuator/health" -Method GET
    Write-Host "✓ Partner Order Service: $($partnerHealth.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ Partner Order Service not available: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. Testing REST retry submission format..." -ForegroundColor Cyan

$TEST_ORDER_ID = "HYBRID-TEST-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
$TRANSACTION_ID = [System.Guid]::NewGuid().ToString()

$retryPayload = @{
    externalId = $TEST_ORDER_ID
    orderStatus = "OPEN"
    locationCode = "LOC-HYBRID-001"
    shipmentType = "CUSTOMER"
    productCategory = "BLOOD_PRODUCTS"
    orderItems = @(
        @{
            productFamily = "RED_BLOOD_CELLS_LEUKOREDUCED"
            bloodType = "O-"
            quantity = 2
            comments = "Hybrid flow test retry"
        }
    )
} | ConvertTo-Json -Depth 10

try {
    $retryHeaders = @{
        'Content-Type' = 'application/json'
        'X-Retry-Attempt' = '1'
        'X-Original-Transaction-ID' = $TRANSACTION_ID
    }
    
    Write-Host "Attempting direct submission to partner order service..." -ForegroundColor Gray
    $directResponse = Invoke-RestMethod -Uri "$PARTNER_ORDER_URL/v1/partner-order-provider/orders" -Method POST -Body $retryPayload -Headers $retryHeaders
    Write-Host "✓ Direct submission successful: $($directResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "✗ Direct submission failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Green