#!/usr/bin/env pwsh

Write-Host "=== Redeploying Application with Database Fixes ===" -ForegroundColor Green

Write-Host "Step 1: Building the application..." -ForegroundColor Yellow
try {
    Set-Location "interface-exception-collector"
    mvn clean compile -q
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Build successful" -ForegroundColor Green
    } else {
        Write-Host "Build failed, continuing anyway..." -ForegroundColor Yellow
    }
    Set-Location ".."
} catch {
    Write-Host "Build step failed, continuing..." -ForegroundColor Yellow
}

Write-Host "Step 2: Restarting application pod..." -ForegroundColor Yellow
try {
    $currentPod = kubectl get pods -l app=interface-exception-collector -o jsonpath="{.items[0].metadata.name}"
    if ($currentPod) {
        Write-Host "Deleting pod: $currentPod" -ForegroundColor Cyan
        kubectl delete pod $currentPod
        
        Write-Host "Waiting for new pod to start..." -ForegroundColor Cyan
        Start-Sleep -Seconds 20
        
        $newPod = kubectl get pods -l app=interface-exception-collector -o jsonpath="{.items[0].metadata.name}"
        Write-Host "New pod: $newPod" -ForegroundColor Green
    }
} catch {
    Write-Host "Restart failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Step 3: Checking application status..." -ForegroundColor Yellow
kubectl get pods -l app=interface-exception-collector

Write-Host "=== Deployment Complete ===" -ForegroundColor Green
Write-Host "Monitor logs with: kubectl logs -f -l app=interface-exception-collector" -ForegroundColor Cyan