#!/usr/bin/env pwsh

# Minimal GraphQL watcher - just shows count and latest

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"
$baseUrl = "http://localhost:8080"

Write-Host "📊 Minimal GraphQL Watcher" -ForegroundColor Green
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

$lastCount = -1
$pollCount = 0

while ($true) {
    $pollCount++
    $timestamp = (Get-Date).ToString('HH:mm:ss')
    
    # Simple query for exception count
    $query = '{"query": "{ exceptions { totalCount edges { node { transactionId status severity timestamp } } } }"}'
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/graphql" -Method POST -Body $query -Headers $headers
        
        if ($response.data.exceptions) {
            $currentCount = $response.data.exceptions.totalCount
            
            Write-Host "[$timestamp] Poll #$pollCount - Total: $currentCount" -ForegroundColor Cyan -NoNewline
            
            if ($currentCount -ne $lastCount) {
                Write-Host " 🔔 CHANGED!" -ForegroundColor Green
                
                # Show latest exception
                if ($response.data.exceptions.edges.Count -gt 0) {
                    $latest = $response.data.exceptions.edges[0].node
                    $color = switch ($latest.severity) {
                        "CRITICAL" { "Red" }
                        "HIGH" { "Yellow" }
                        default { "White" }
                    }
                    Write-Host "  Latest: $($latest.transactionId) - $($latest.status) ($($latest.severity))" -ForegroundColor $color
                }
                
                $lastCount = $currentCount
            } else {
                Write-Host " (no change)" -ForegroundColor Gray
            }
            
        } else {
            Write-Host "[$timestamp] Poll #$pollCount - No data" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "[$timestamp] Poll #$pollCount - Error: $_" -ForegroundColor Red
    }
    
    Start-Sleep -Seconds 5
}