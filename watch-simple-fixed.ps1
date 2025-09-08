#!/usr/bin/env pwsh

# Fixed live GraphQL subscription watcher

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
$baseUrl = "http://localhost:8080"

Write-Host "🔴 LIVE GraphQL Watcher (Fixed)" -ForegroundColor Red
Write-Host "===============================" -ForegroundColor Red
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Test authentication
Write-Host "`n🔐 Testing authentication..." -ForegroundColor Cyan
$testQuery = '{"query": "{ __schema { subscriptionType { fields { name } } } }"}'

try {
    $testResponse = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $testQuery -Headers $headers
    Write-Host "✅ Authentication successful!" -ForegroundColor Green
} catch {
    Write-Host "❌ Authentication failed: $_" -ForegroundColor Red
    exit 1
}

# Simple hash function for change detection
function Get-StringHash {
    param([string]$InputString)
    
    $hasher = [System.Security.Cryptography.MD5]::Create()
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($InputString)
    $hashBytes = $hasher.ComputeHash($bytes)
    return [System.BitConverter]::ToString($hashBytes) -replace '-', ''
}

Write-Host "`n📊 Starting live monitoring..." -ForegroundColor Green

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
            
            # Create hash from the JSON string
            $jsonString = ($exceptions | ConvertTo-Json -Compress)
            $currentHash = Get-StringHash -InputString $jsonString
            
            Write-Host "  Total exceptions: $($exceptions.totalCount)" -ForegroundColor White
            
            # Check if data changed
            if ($currentHash -ne $lastHash) {
                Write-Host "  🔔 NEW DATA DETECTED!" -ForegroundColor Green
                
                # Show recent exceptions with color coding
                if ($exceptions.edges.Count -gt 0) {
                    Write-Host "  Recent exceptions:" -ForegroundColor Yellow
                    foreach ($edge in $exceptions.edges) {
                        $ex = $edge.node
                        $exTime = try { 
                            [DateTime]::Parse($ex.timestamp).ToString('MM/dd HH:mm') 
                        } catch { 
                            $ex.timestamp.Substring(0, 16) 
                        }
                        
                        # Color based on severity
                        $color = switch ($ex.severity) {
                            "CRITICAL" { "Red" }
                            "HIGH" { "Yellow" }
                            "MEDIUM" { "Cyan" }
                            default { "Gray" }
                        }
                        
                        Write-Host "    🔸 [$exTime] $($ex.transactionId)" -ForegroundColor $color
                        Write-Host "      $($ex.status) | $($ex.severity) | Retries: $($ex.retryCount)" -ForegroundColor Gray
                        Write-Host "      Type: $($ex.interfaceType)" -ForegroundColor DarkGray
                        
                        # Truncate long reasons
                        $reason = if ($ex.exceptionReason.Length -gt 60) { 
                            $ex.exceptionReason.Substring(0, 60) + "..." 
                        } else { 
                            $ex.exceptionReason 
                        }
                        Write-Host "      Reason: $reason" -ForegroundColor DarkGray
                    }
                } else {
                    Write-Host "  No exceptions found" -ForegroundColor Gray
                }
                
                $lastHash = $currentHash
                
                # Beep for new data (if supported)
                try {
                    [Console]::Beep(800, 200)
                } catch {
                    # Ignore if beep not supported
                }
                
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