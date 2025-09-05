#!/usr/bin/env pwsh

Write-Host "=== Testing Application Startup ===" -ForegroundColor Green

# Start the application in background
$job = Start-Job -ScriptBlock {
    Set-Location "interface-exception-collector"
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local --server.port=8080" -q
}

Write-Host "Starting application..." -ForegroundColor Yellow

# Wait for startup (max 30 seconds)
$timeout = 30
$elapsed = 0
$started = $false

while ($elapsed -lt $timeout -and -not $started) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 2
        if ($response.status -eq "UP") {
            $started = $true
            Write-Host "✓ Application started successfully!" -ForegroundColor Green
            Write-Host "  Health status: $($response.status)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  Waiting for startup... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

if (-not $started) {
    Write-Host "✗ Application failed to start within $timeout seconds" -ForegroundColor Red
}

# Stop the background job
Stop-Job $job -Force
Remove-Job $job -Force

Write-Host "=== Startup Test Complete ===" -ForegroundColor Green