#!/usr/bin/env pwsh

# Simple curl-based GraphQL watcher

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Token = ""
)

Write-Host "GraphQL Live Watcher (curl-based)" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Green

# Generate token if not provided
if (-not $Token) {
    Write-Host "`n🔐 Generating JWT token..." -ForegroundColor Yellow
    
    $loginCmd = @"
curl -s -X POST $BaseUrl/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
"@
    
    try {
        $loginResult = Invoke-Expression $loginCmd | ConvertFrom-Json
        $Token = $loginResult.token
        Write-Host "✅ Token generated: $($Token.Substring(0, 20))..." -ForegroundColor Green
    } catch {
        Write-Host "⚠️  No token generated, trying without auth..." -ForegroundColor Yellow
    }
}

# Set up headers
$authHeader = if ($Token) { "-H `"Authorization: Bearer $Token`"" } else { "" }

Write-Host "`n📊 Live GraphQL Exception Monitoring" -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop..." -ForegroundColor Yellow

$lastHash = ""
$pollCount = 0

while ($true) {
    $pollCount++
    $timestamp = (Get-Date).ToString('HH:mm:ss')
    
    Write-Host "`n[$timestamp] Poll #$pollCount" -ForegroundColor Cyan
    
    # GraphQL query for recent exceptions
    $query = @"
{
  "query": "{ exceptions(pagination: { first: 10 }) { totalCount edges { node { transactionId status severity interfaceType timestamp exceptionReason } } } }"
}
"@
    
    # Build curl command
    $curlCmd = @"
curl -s -X POST $BaseUrl/graphql \
  -H "Content-Type: application/json" \
  $authHeader \
  -d '$query'
"@
    
    try {
        # Execute curl and parse response
        $response = Invoke-Expression $curlCmd | ConvertFrom-Json
        
        if ($response.data -and $response.data.exceptions) {
            $exceptions = $response.data.exceptions
            $currentHash = ($exceptions | ConvertTo-Json -Compress | Get-FileHash -Algorithm MD5).Hash
            
            Write-Host "  Total exceptions: $($exceptions.totalCount)" -ForegroundColor White
            
            # Check if data changed
            if ($currentHash -ne $lastHash) {
                Write-Host "  🔔 NEW DATA DETECTED!" -ForegroundColor Green
                
                # Show recent exceptions
                Write-Host "  Recent exceptions:" -ForegroundColor Yellow
                foreach ($edge in $exceptions.edges) {
                    $ex = $edge.node
                    $exTime = try { [DateTime]::Parse($ex.timestamp).ToString('MM/dd HH:mm') } catch { $ex.timestamp }
                    
                    $statusColor = switch ($ex.severity) {
                        "CRITICAL" { "Red" }
                        "HIGH" { "Yellow" }
                        "MEDIUM" { "Cyan" }
                        default { "Gray" }
                    }
                    
                    Write-Host "    [$exTime] $($ex.transactionId)" -ForegroundColor $statusColor
                    Write-Host "      Status: $($ex.status) | Severity: $($ex.severity) | Type: $($ex.interfaceType)" -ForegroundColor Gray
                    Write-Host "      Reason: $($ex.exceptionReason)" -ForegroundColor DarkGray
                }
                
                $lastHash = $currentHash
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