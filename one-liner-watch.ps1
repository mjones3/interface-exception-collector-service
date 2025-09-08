#!/usr/bin/env pwsh

# One-liner GraphQL watcher

$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTczNDIzMjIsImV4cCI6MTc1NzM0NTkyMn0.V0uYhISb1FgM7S3CDy8P_nTtgRh4BnJZFHAg8Pz9wWc"

Write-Host "Starting GraphQL watcher..." -ForegroundColor Green

while ($true) {
    Write-Host "=== $(Get-Date -Format 'HH:mm:ss') ===" -ForegroundColor Cyan
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method POST -Body '{"query": "{ exceptions(pagination: { first: 5 }) { totalCount edges { node { transactionId status severity timestamp } } } }"}' -Headers @{"Authorization" = "Bearer $token"; "Content-Type" = "application/json"}
        
        Write-Host "Total exceptions: $($response.data.exceptions.totalCount)" -ForegroundColor White
        
        foreach ($edge in $response.data.exceptions.edges) {
            $ex = $edge.node
            $color = if ($ex.severity -eq "CRITICAL") { "Red" } elseif ($ex.severity -eq "HIGH") { "Yellow" } else { "White" }
            Write-Host "  $($ex.transactionId) | $($ex.status) | $($ex.severity)" -ForegroundColor $color
        }
    } catch {
        Write-Host "Error: $_" -ForegroundColor Red
    }
    
    Start-Sleep -Seconds 5
}