#!/usr/bin/env pwsh

# Command-line GraphQL subscription watcher with JWT authentication

param(
    [string]$Username = "admin",
    [string]$Password = "password",
    [string]$BaseUrl = "http://localhost:8080",
    [string]$SubscriptionType = "exceptions"
)

Write-Host "GraphQL Subscription CLI Watcher" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Step 1: Generate JWT Token
Write-Host "`n🔐 Generating JWT token..." -ForegroundColor Yellow

$loginPayload = @{
    username = $Username
    password = $Password
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method POST -Body $loginPayload -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "✅ JWT token generated successfully" -ForegroundColor Green
    Write-Host "Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to generate token. Trying without authentication..." -ForegroundColor Red
    $token = $null
}

# Step 2: Test GraphQL endpoint
Write-Host "`n🧪 Testing GraphQL endpoint..." -ForegroundColor Yellow

$healthQuery = @{
    query = "{ systemHealth { status } }"
} | ConvertTo-Json

$headers = @{
    "Content-Type" = "application/json"
}

if ($token) {
    $headers["Authorization"] = "Bearer $token"
}

try {
    $healthResponse = Invoke-RestMethod -Uri "$BaseUrl/graphql" -Method POST -Body $healthQuery -Headers $headers
    Write-Host "✅ GraphQL endpoint is accessible" -ForegroundColor Green
    Write-Host "System status: $($healthResponse.data.systemHealth.status)" -ForegroundColor Gray
} catch {
    Write-Host "❌ GraphQL endpoint test failed: $_" -ForegroundColor Red
    Write-Host "Make sure the service is running on $BaseUrl" -ForegroundColor Yellow
    exit 1
}

# Step 3: Set up subscription query based on type
$subscriptionQuery = switch ($SubscriptionType) {
    "exceptions" {
        @{
            query = @"
subscription {
  exceptionUpdated {
    eventType
    exception {
      transactionId
      status
      severity
      interfaceType
      exceptionReason
    }
    timestamp
    triggeredBy
  }
}
"@
        } | ConvertTo-Json
    }
    "retries" {
        @{
            query = @"
subscription {
  retryStatusUpdated {
    transactionId
    eventType
    timestamp
    retryAttempt {
      attemptNumber
      status
    }
  }
}
"@
        } | ConvertTo-Json
    }
    "summary" {
        @{
            query = @"
subscription {
  summaryUpdated(timeRange: LAST_HOUR) {
    totalExceptions
    keyMetrics {
      retrySuccessRate
      criticalExceptionCount
    }
  }
}
"@
        } | ConvertTo-Json
    }
    default {
        Write-Host "❌ Unknown subscription type: $SubscriptionType" -ForegroundColor Red
        Write-Host "Available types: exceptions, retries, summary" -ForegroundColor Yellow
        exit 1
    }
}

# Step 4: Watch using curl with Server-Sent Events (if supported)
Write-Host "`n📡 Starting subscription watcher for: $SubscriptionType" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop watching..." -ForegroundColor Yellow

# Method 1: Try Server-Sent Events endpoint (if available)
Write-Host "`nTrying Server-Sent Events..." -ForegroundColor Gray

$sseHeaders = @(
    "Accept: text/event-stream"
    "Cache-Control: no-cache"
)

if ($token) {
    $sseHeaders += "Authorization: Bearer $token"
}

$sseUrl = "$BaseUrl/graphql/stream"

# Method 2: Polling approach (fallback)
Write-Host "`nFalling back to polling approach..." -ForegroundColor Gray
Write-Host "Watching for new exceptions every 5 seconds..." -ForegroundColor Yellow

$lastCount = 0
$pollCount = 0

while ($true) {
    try {
        $pollCount++
        Write-Host "`n[$((Get-Date).ToString('HH:mm:ss'))] Poll #$pollCount" -ForegroundColor Cyan
        
        # Query for recent exceptions
        $recentQuery = @{
            query = @"
{
  exceptions(pagination: { first: 5 }) {
    totalCount
    edges {
      node {
        transactionId
        status
        severity
        interfaceType
        timestamp
        exceptionReason
      }
    }
  }
}
"@
        } | ConvertTo-Json

        $response = Invoke-RestMethod -Uri "$BaseUrl/graphql" -Method POST -Body $recentQuery -Headers $headers
        
        if ($response.data -and $response.data.exceptions) {
            $currentCount = $response.data.exceptions.totalCount
            
            if ($currentCount -ne $lastCount) {
                Write-Host "🔔 Exception count changed: $lastCount → $currentCount" -ForegroundColor Green
                
                # Show recent exceptions
                foreach ($edge in $response.data.exceptions.edges) {
                    $ex = $edge.node
                    $timestamp = [DateTime]::Parse($ex.timestamp).ToString('HH:mm:ss')
                    Write-Host "  [$timestamp] $($ex.transactionId) - $($ex.status) ($($ex.severity))" -ForegroundColor White
                    Write-Host "    Type: $($ex.interfaceType)" -ForegroundColor Gray
                    Write-Host "    Reason: $($ex.exceptionReason)" -ForegroundColor Gray
                }
                
                $lastCount = $currentCount
            } else {
                Write-Host "  No new exceptions (total: $currentCount)" -ForegroundColor Gray
            }
        }
        
        # Wait before next poll
        Start-Sleep -Seconds 5
        
    } catch {
        Write-Host "❌ Polling error: $_" -ForegroundColor Red
        Start-Sleep -Seconds 10
    }
}