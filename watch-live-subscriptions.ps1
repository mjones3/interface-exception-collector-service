#!/usr/bin/env pwsh

# Live GraphQL subscription watcher with your token

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
$baseUrl = "http://localhost:8080"

Write-Host "🔴 LIVE GraphQL Subscription Watcher" -ForegroundColor Red
Write-Host "====================================" -ForegroundColor Red
Write-Host "Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
Write-Host "Watching for real-time events..." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow

# Test the token first
Write-Host "`n🔐 Testing authentication..." -ForegroundColor Cyan
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

$testQuery = '{"query": "{ __schema { subscriptionType { fields { name } } } }"}'

try {
    $testResponse = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $testQuery -Headers $headers
    
    if ($testResponse.data.__schema.subscriptionType.fields) {
        Write-Host "✅ Authentication successful!" -ForegroundColor Green
        Write-Host "Available subscriptions:" -ForegroundColor Cyan
        foreach ($field in $testResponse.data.__schema.subscriptionType.fields) {
            Write-Host "  - $($field.name)" -ForegroundColor White
        }
    }
} catch {
    Write-Host "❌ Authentication failed: $_" -ForegroundColor Red
    exit 1
}

# Start polling for exceptions (since WebSocket is complex in PowerShell)
Write-Host "`n📊 Starting live monitoring..." -ForegroundColor Green
Write-Host "Polling every 3 seconds for changes..." -ForegroundColor Gray

$lastHash = ""
$pollCount = 0

while ($true) {
    $pollCount++
    $timestamp = (Get-Date).ToString('HH:mm:ss')
    
    Write-Host "`n[$timestamp] Poll #$pollCount" -ForegroundColor Cyan
    
    # Query for recent exceptions
    $query = @{
        query = @"
{
  exceptions(pagination: { first: 10 }) {
    totalCount
    edges {
      node {
        transactionId
        status
        severity
        interfaceType
        timestamp
        exceptionReason
        retryCount
      }
    }
  }
}
"@
    } | ConvertTo-Json -Compress
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $query -Headers $headers
        
        if ($response.data.exceptions) {
            $exceptions = $response.data.exceptions
            $currentHash = ($exceptions | ConvertTo-Json -Compress | Get-FileHash -Algorithm MD5).Hash
            
            Write-Host "  Total exceptions: $($exceptions.totalCount)" -ForegroundColor White
            
            # Check if data changed
            if ($currentHash -ne $lastHash) {
                Write-Host "  🔔 NEW DATA DETECTED!" -ForegroundColor Green
                
                # Show recent exceptions with color coding
                Write-Host "  Recent exceptions:" -ForegroundColor Yellow
                foreach ($edge in $exceptions.edges) {
                    $ex = $edge.node
                    $exTime = try { 
                        [DateTime]::Parse($ex.timestamp).ToString('MM/dd HH:mm') 
                    } catch { 
                        $ex.timestamp 
                    }
                    
                    # Color based on severity
                    $color = switch ($ex.severity) {
                        "CRITICAL" { "Red" }
                        "HIGH" { "Yellow" }
                        "MEDIUM" { "Cyan" }
                        default { "Gray" }
                    }
                    
                    Write-Host "    🔸 [$exTime] $($ex.transactionId)" -ForegroundColor $color
                    Write-Host "      Status: $($ex.status) | Severity: $($ex.severity) | Retries: $($ex.retryCount)" -ForegroundColor Gray
                    Write-Host "      Type: $($ex.interfaceType)" -ForegroundColor DarkGray
                    Write-Host "      Reason: $($ex.exceptionReason)" -ForegroundColor DarkGray
                }
                
                $lastHash = $currentHash
                
                # Beep for new data
                [Console]::Beep(800, 200)
                
            } else {
                Write-Host "  No changes detected" -ForegroundColor Gray
            }
            
        } elseif ($response.errors) {
            Write-Host "  ❌ GraphQL errors:" -ForegroundColor Red
            foreach ($error in $response.errors) {
                Write-Host "    $($error.message)" -ForegroundColor Red
            }
        } else {
            Write-Host "  ⚠️  No data received" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "  ❌ Request failed: $_" -ForegroundColor Red
    }
    
    # Wait before next poll
    Start-Sleep -Seconds 3
}