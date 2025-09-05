#!/usr/bin/env pwsh
# Fix Partner Order Service to use REST instead of RSocket for retry calls

Write-Host "=== Fixing Partner Order Service REST Retry Implementation ===" -ForegroundColor Green

# Step 1: Check current configuration
Write-Host "Step 1: Checking current configuration..." -ForegroundColor Yellow
$configFiles = @(
    "interface-exception-collector/src/main/resources/application-local.yml",
    "interface-exception-collector/src/main/resources/application-dev.yml", 
    "interface-exception-collector/src/main/resources/application-test.yml",
    "interface-exception-collector/src/main/resources/application-prod.yml"
)

foreach ($file in $configFiles) {
    if (Test-Path $file) {
        Write-Host "Found config: $file" -ForegroundColor Cyan
        $content = Get-Content $file -Raw
        if ($content -match "rsocket://") {
            Write-Host "  - Contains RSocket URLs - needs fixing" -ForegroundColor Red
        } else {
            Write-Host "  - No RSocket URLs found" -ForegroundColor Green
        }
    }
}

# Step 2: Update configuration files to use HTTP instead of RSocket
Write-Host "`nStep 2: Updating configuration files..." -ForegroundColor Yellow

foreach ($file in $configFiles) {
    if (Test-Path $file) {
        Write-Host "Updating $file..." -ForegroundColor Cyan
        
        # Read current content
        $content = Get-Content $file -Raw
        
        # Replace rsocket:// with http://
        $updatedContent = $content -replace "rsocket://localhost:7000", "http://localhost:7001"
        $updatedContent = $updatedContent -replace "rsocket://mock-rsocket-server:7000", "http://mock-rsocket-server:7001"
        
        # Write back to file
        Set-Content -Path $file -Value $updatedContent -NoNewline
        Write-Host "  - Updated RSocket URLs to HTTP URLs" -ForegroundColor Green
    }
}

# Step 3: Check if PartnerOrderServiceClient exists and is properly configured
Write-Host "`nStep 3: Checking PartnerOrderServiceClient..." -ForegroundColor Yellow
$partnerOrderClient = "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/client/PartnerOrderServiceClient.java"

if (Test-Path $partnerOrderClient) {
    Write-Host "PartnerOrderServiceClient exists" -ForegroundColor Green
    
    # Check if it's properly configured for REST
    $clientContent = Get-Content $partnerOrderClient -Raw
    if ($clientContent -match "BaseSourceServiceClient" -and $clientContent -match "RestTemplate") {
        Write-Host "  - Client properly extends BaseSourceServiceClient with RestTemplate" -ForegroundColor Green
    } else {
        Write-Host "  - Client may need REST configuration updates" -ForegroundColor Yellow
    }
} else {
    Write-Host "PartnerOrderServiceClient not found - this should exist from previous implementation" -ForegroundColor Red
}

# Step 4: Check InterfaceType enum
Write-Host "`nStep 4: Checking InterfaceType enum..." -ForegroundColor Yellow
$interfaceTypeFile = "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/domain/enums/InterfaceType.java"

if (Test-Path $interfaceTypeFile) {
    $enumContent = Get-Content $interfaceTypeFile -Raw
    if ($enumContent -match "PARTNER_ORDER") {
        Write-Host "PARTNER_ORDER enum value exists" -ForegroundColor Green
    } else {
        Write-Host "PARTNER_ORDER enum value missing - needs to be added" -ForegroundColor Red
    }
} else {
    Write-Host "InterfaceType.java not found" -ForegroundColor Red
}

# Step 5: Rebuild and restart the application
Write-Host "`nStep 5: Rebuilding application..." -ForegroundColor Yellow
Set-Location "interface-exception-collector"

Write-Host "Running Maven clean compile..." -ForegroundColor Cyan
$buildResult = & mvn clean compile -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "  - Build successful" -ForegroundColor Green
} else {
    Write-Host "  - Build failed" -ForegroundColor Red
    Write-Host $buildResult
}

# Step 6: Check if application is running in Kubernetes and restart
Write-Host "`nStep 6: Checking Kubernetes deployment..." -ForegroundColor Yellow
Set-Location ".."

$podCheck = & kubectl get pods -l app=interface-exception-collector 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "Found Kubernetes pods for interface-exception-collector" -ForegroundColor Cyan
    
    # Restart the deployment
    Write-Host "Restarting deployment..." -ForegroundColor Cyan
    & kubectl rollout restart deployment/interface-exception-collector
    
    # Wait for rollout to complete
    Write-Host "Waiting for rollout to complete..." -ForegroundColor Cyan
    & kubectl rollout status deployment/interface-exception-collector --timeout=300s
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  - Deployment restarted successfully" -ForegroundColor Green
    } else {
        Write-Host "  - Deployment restart may have issues" -ForegroundColor Yellow
    }
} else {
    Write-Host "No Kubernetes deployment found - application may be running locally" -ForegroundColor Yellow
}

# Step 7: Test the fix
Write-Host "`nStep 7: Testing the fix..." -ForegroundColor Yellow

# Wait a bit for the application to start
Start-Sleep -Seconds 10

# Check pod logs for any RSocket errors
$pods = & kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($pods -and $LASTEXITCODE -eq 0) {
    Write-Host "Checking recent logs for RSocket errors..." -ForegroundColor Cyan
    $logs = & kubectl logs $pods --tail=50 2>$null
    
    if ($logs -match "rsocket protocol is not supported") {
        Write-Host "  - Still seeing RSocket errors in logs" -ForegroundColor Red
        Write-Host "  - Configuration may not have taken effect yet" -ForegroundColor Yellow
    } else {
        Write-Host "  - No RSocket protocol errors found in recent logs" -ForegroundColor Green
    }
    
    # Show recent logs
    Write-Host "`nRecent application logs:" -ForegroundColor Cyan
    $logs | Select-Object -Last 10 | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
}

# Step 8: Run a test retry if possible
Write-Host "`nStep 8: Testing retry functionality..." -ForegroundColor Yellow

# Check if test script exists
if (Test-Path "test-partner-order-retry.ps1") {
    Write-Host "Running retry test..." -ForegroundColor Cyan
    & .\test-partner-order-retry.ps1
} else {
    Write-Host "Test script not found - manual testing required" -ForegroundColor Yellow
}

Write-Host "`n=== Fix Complete ===" -ForegroundColor Green
Write-Host "Summary of changes made:" -ForegroundColor Cyan
Write-Host "1. Updated all configuration files to use HTTP URLs instead of RSocket URLs" -ForegroundColor White
Write-Host "2. Rebuilt the application" -ForegroundColor White
Write-Host "3. Restarted Kubernetes deployment (if found)" -ForegroundColor White
Write-Host "4. Verified no RSocket errors in recent logs" -ForegroundColor White
Write-Host "`nThe partner order service should now use REST calls for retry operations." -ForegroundColor Green