#!/usr/bin/env pwsh

# Trigger events to test the subscription watcher

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
$baseUrl = "http://localhost:8080"

Write-Host "🎯 Event Trigger for Subscription Testing" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Function to create a test exception
function Create-TestException {
    param([string]$ExternalId)
    
    Write-Host "`n🔥 Creating test exception: $ExternalId" -ForegroundColor Yellow
    
    # Try to create an exception by sending invalid data to partner-order-service
    try {
        $invalidOrder = @{
            externalId = $ExternalId
            invalidField = "test-data"
            locationCode = "TEST-LOC"
        } | ConvertTo-Json
        
        $response = Invoke-RestMethod -Uri "http://localhost:8090/v1/partner-order-provider/orders" -Method POST -Body $invalidOrder -ContentType "application/json"
        Write-Host "✅ Order created (might generate exception): $ExternalId" -ForegroundColor Green
        
    } catch {
        Write-Host "⚠️  Order creation failed (expected): $_" -ForegroundColor Yellow
        Write-Host "This should generate an exception to watch!" -ForegroundColor Cyan
    }
}

# Function to retry an exception
function Retry-Exception {
    param([string]$TransactionId)
    
    Write-Host "`n🔄 Retrying exception: $TransactionId" -ForegroundColor Yellow
    
    $retryMutation = @{
        query = "mutation { retryException(input: {transactionId: `"$TransactionId`"}) { success exception { transactionId status } } }"
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $retryMutation -Headers $headers
        
        if ($response.data.retryException.success) {
            Write-Host "✅ Retry initiated successfully" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Retry failed or not found" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Retry request failed: $_" -ForegroundColor Red
    }
}

# Main menu
while ($true) {
    Write-Host "`n🎮 Choose an action:" -ForegroundColor Cyan
    Write-Host "1. Create test exception" -ForegroundColor White
    Write-Host "2. Retry an exception" -ForegroundColor White
    Write-Host "3. Create multiple exceptions" -ForegroundColor White
    Write-Host "4. List current exceptions" -ForegroundColor White
    Write-Host "5. Exit" -ForegroundColor White
    
    $choice = Read-Host "`nEnter choice (1-5)"
    
    switch ($choice) {
        "1" {
            $externalId = "TEST-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
            Create-TestException -ExternalId $externalId
        }
        "2" {
            $transactionId = Read-Host "Enter transaction ID to retry"
            if ($transactionId) {
                Retry-Exception -TransactionId $transactionId
            }
        }
        "3" {
            Write-Host "`n🔥 Creating 5 test exceptions..." -ForegroundColor Yellow
            for ($i = 1; $i -le 5; $i++) {
                $externalId = "BULK-TEST-$i-$(Get-Date -Format 'HHmmss')"
                Create-TestException -ExternalId $externalId
                Start-Sleep -Seconds 1
            }
        }
        "4" {
            Write-Host "`n📋 Current exceptions:" -ForegroundColor Cyan
            $query = @{
                query = "{ exceptions(pagination: { first: 5 }) { edges { node { transactionId status severity exceptionReason } } } }"
            } | ConvertTo-Json
            
            try {
                $response = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $query -Headers $headers
                foreach ($edge in $response.data.exceptions.edges) {
                    $ex = $edge.node
                    Write-Host "  - $($ex.transactionId) | $($ex.status) | $($ex.severity)" -ForegroundColor White
                }
            } catch {
                Write-Host "❌ Failed to list exceptions: $_" -ForegroundColor Red
            }
        }
        "5" {
            Write-Host "👋 Goodbye!" -ForegroundColor Green
            exit
        }
        default {
            Write-Host "❌ Invalid choice" -ForegroundColor Red
        }
    }
}