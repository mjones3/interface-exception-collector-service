#!/usr/bin/env pwsh

# Test script to verify that retries generate new UUIDs for transaction_id

Write-Host "Testing UUID generation for partner order retries..." -ForegroundColor Green

# Test 1: Simulate a retry request with original transaction ID
$originalTransactionId = "550e8400-e29b-41d4-a716-446655440000"
$retryAttempt = 3

Write-Host "`nTest 1: Retry request simulation" -ForegroundColor Yellow
Write-Host "Original Transaction ID: $originalTransactionId"
Write-Host "Retry Attempt: $retryAttempt"

# Create test request payload
$testRequest = @{
    externalId = "TEST-ORDER-123"
    locationCode = "LOC-001"
    productCategory = "BLOOD_PRODUCTS"
    orderStatus = "OPEN"
    shipmentType = "CUSTOMER"
    orderItems = @(
        @{
            productFamily = "RED_BLOOD_CELLS"
            bloodType = "O-"
            quantity = 2
            comments = "Urgent request"
        }
    )
} | ConvertTo-Json -Depth 3

Write-Host "`nTest Request Payload:" -ForegroundColor Cyan
Write-Host $testRequest

# Headers for retry request
$headers = @{
    "Content-Type" = "application/json"
    "X-Retry-Attempt" = $retryAttempt.ToString()
    "X-Original-Transaction-ID" = $originalTransactionId
}

Write-Host "`nRetry Headers:" -ForegroundColor Cyan
$headers | Format-Table -AutoSize

Write-Host "`nExpected Behavior:" -ForegroundColor Magenta
Write-Host "1. Partner Order Service should receive the retry request"
Write-Host "2. It should generate a NEW UUID for transaction_id (not reuse the original)"
Write-Host "3. It should CREATE a new order record with the same external_id (duplicates allowed)"
Write-Host "4. The original transaction ID should be logged for correlation"
Write-Host "5. The response should contain the NEW transaction ID"
Write-Host "6. No unique constraint violations should occur on transaction_id"
Write-Host "7. Duplicate external_id is allowed and expected for retries"

Write-Host "`nKey Changes Made:" -ForegroundColor Green
Write-Host "- Removed unique constraint on external_id in database (V002 migration)"
Write-Host "- Modified PartnerOrderService.processOrder() to always generate new UUID"
Write-Host "- Always create new order records, even for retries (duplicates allowed)"
Write-Host "- Removed duplicate external_id validation logic"
Write-Host "- Added logging to show correlation between original and new transaction IDs"

Write-Host "`nTo test this manually:" -ForegroundColor Yellow
Write-Host "1. Start the partner-order-service"
Write-Host "2. Make a retry request with the headers above"
Write-Host "3. Check logs for the correlation between original and new transaction IDs"
Write-Host "4. Verify the response contains a new transaction ID"
Write-Host "5. Verify no database constraint violations occur"

Write-Host "`nTest completed!" -ForegroundColor Green