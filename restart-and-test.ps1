#!/usr/bin/env pwsh

# Restart Application and Test
Write-Host "=== RESTARTING APPLICATION AND TESTING ===" -ForegroundColor Green

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Restart the interface-exception-collector pod" "Magenta"
kubectl delete pod -l app=interface-exception-collector
Write-Log "Pod deleted, waiting for restart..." "Yellow"

Write-Log "Step 2: Wait for pod to restart" "Magenta"
Start-Sleep -Seconds 30

Write-Log "Step 3: Check pod status" "Magenta"
kubectl get pods -l app=interface-exception-collector

Write-Log "Step 4: Wait for application to be ready" "Magenta"
$maxRetries = 20
$retryCount = 0
$serviceReady = $false

while ($retryCount -lt $maxRetries -and -not $serviceReady) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Log "Application is healthy!" "Green"
            $serviceReady = $true
        }
    } catch {
        $retryCount++
        Write-Log "Health check attempt $retryCount/$maxRetries - waiting..." "Yellow"
        Start-Sleep -Seconds 10
    }
}

if ($serviceReady) {
    Write-Log "Step 5: Test API endpoint with acknowledgment_notes fix" "Magenta"
    $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc5ODE3MDAsImV4cCI6MTc1Nzk4NTMwMH0.Ptor4zyhMG_lk0ulPlk40yOfv25uzhmucGKVTl7TF68"
    
    try {
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
        
        if ($response.StatusCode -eq 200) {
            Write-Log "🎉 SUCCESS! API endpoint working - acknowledgment_notes column error COMPLETELY FIXED!" "Green"
            $content = $response.Content | ConvertFrom-Json
            Write-Log "Found $($content.totalElements) exceptions in system" "Cyan"
            Write-Log "✅ No more 'column ie1_0.acknowledgment_notes does not exist' errors!" "Green"
        }
    } catch {
        Write-Log "API test failed: $_" "Red"
        Write-Log "Checking application logs for any remaining issues..." "Yellow"
        kubectl logs -l app=interface-exception-collector --tail=10
    }
} else {
    Write-Log "Application failed to start properly" "Red"
    kubectl logs -l app=interface-exception-collector --tail=20
}

Write-Log "=== RESTART AND TEST COMPLETE ===" "Green"