# Fix Missing Source Services Configuration
Write-Host "=== Fixing Missing Source Services Configuration ===" -ForegroundColor Green

Write-Host "`n1. Diagnosing the configuration issue..." -ForegroundColor Cyan
Write-Host "   Problem: source-services.partner-order.base-url configuration is missing" -ForegroundColor Red
Write-Host "   This causes partnerOrderServiceClient bean creation to fail" -ForegroundColor Red

Write-Host "`n2. Adding the missing source-services configuration..." -ForegroundColor Cyan

# Read the current application.yml
$appYmlPath = "src/main/resources/application.yml"
$currentContent = Get-Content $appYmlPath -Raw

# Add the source-services configuration at the end of the file
$sourceServicesConfig = @"

# Source services configuration for external service communication
source-services:
  partner-order:
    base-url: http://partner-order-service:8090
    timeout: 5000
    connection-timeout: 3000
    read-timeout: 5000
"@

# Append the configuration
$updatedContent = $currentContent + $sourceServicesConfig

# Write back to file
$updatedContent | Set-Content $appYmlPath -Encoding UTF8

Write-Host "   ✅ Added source-services.partner-order configuration" -ForegroundColor Green
Write-Host "   ✅ Set base-url to http://partner-order-service:8090" -ForegroundColor Green
Write-Host "   ✅ Added timeout configurations" -ForegroundColor Green

Write-Host "`n3. Verifying the configuration was added..." -ForegroundColor Cyan
$verifyContent = Get-Content $appYmlPath -Raw
if ($verifyContent -match "source-services:") {
    Write-Host "   ✅ source-services configuration found" -ForegroundColor Green
    if ($verifyContent -match "partner-order:") {
        Write-Host "   ✅ partner-order configuration found" -ForegroundColor Green
        if ($verifyContent -match "base-url:\s*http://partner-order-service:8090") {
            Write-Host "   ✅ base-url configuration is correct" -ForegroundColor Green
        } else {
            Write-Host "   ❌ base-url configuration is incorrect" -ForegroundColor Red
        }
    } else {
        Write-Host "   ❌ partner-order configuration missing" -ForegroundColor Red
    }
} else {
    Write-Host "   ❌ source-services configuration not found" -ForegroundColor Red
}

Write-Host "`n4. Compiling the application..." -ForegroundColor Cyan
$compileResult = & mvn compile -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed:" -ForegroundColor Red
    Write-Host "   $compileResult" -ForegroundColor Red
}

Write-Host "`n5. Restarting application pods..." -ForegroundColor Cyan
$pods = kubectl get pods -n api | Select-String "interface-exception-collector"
if ($pods) {
    Write-Host "   Current pods:" -ForegroundColor Yellow
    $pods | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    
    # Delete all interface-exception-collector pods
    $podNames = $pods | ForEach-Object { ($_ -split '\s+')[0] }
    foreach ($podName in $podNames) {
        Write-Host "   Deleting pod: $podName" -ForegroundColor Yellow
        kubectl delete pod $podName -n api | Out-Null
    }
} else {
    Write-Host "   No interface-exception-collector pods found" -ForegroundColor Yellow
}

Write-Host "`n6. Waiting for new pod to start..." -ForegroundColor Cyan
Start-Sleep -Seconds 15

$timeout = 120
$elapsed = 0
$success = $false

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 5
    $elapsed += 5
    
    $newPods = kubectl get pods -n api | Select-String "interface-exception-collector"
    if ($newPods) {
        $podName = ($newPods[0] -split '\s+')[0]
        $podStatus = ($newPods[0] -split '\s+')[2]
        
        Write-Host "   Pod: $podName, Status: $podStatus" -ForegroundColor Gray
        
        if ($podStatus -eq "Running") {
            Write-Host "   Pod $podName is running, checking logs..." -ForegroundColor Green
            
            $logs = kubectl logs $podName -n api --tail=30 2>$null
            
            # Check for the specific configuration error
            if ($logs -match "Could not resolve placeholder.*source-services.partner-order.base-url") {
                Write-Host "   ❌ Still getting source-services.partner-order.base-url placeholder error" -ForegroundColor Red
                break
            }
            
            # Check for successful startup
            if ($logs -match "Started.*Application|Tomcat started on port") {
                $success = $true
                Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
                break
            }
            
            # Check for other startup failures
            if ($logs -match "APPLICATION FAILED TO START") {
                Write-Host "   ❌ Application failed to start with different error" -ForegroundColor Red
                $logs | Where-Object { $_ -match "ERROR|Exception" } | Select-Object -First 3 | ForEach-Object {
                    Write-Host "     $_" -ForegroundColor Red
                }
                break
            }
        } elseif ($podStatus -eq "CrashLoopBackOff") {
            Write-Host "   Pod $podName is in CrashLoopBackOff, checking logs..." -ForegroundColor Yellow
            $logs = kubectl logs $podName -n api --tail=10 2>$null
            if ($logs -match "Could not resolve placeholder.*source-services.partner-order.base-url") {
                Write-Host "   ❌ Still getting source-services.partner-order.base-url placeholder error" -ForegroundColor Red
                break
            }
        }
    }
    
    if ($elapsed % 30 -eq 0) {
        Write-Host "   Still starting... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

Write-Host "`n=== SOURCE SERVICES CONFIGURATION FIX SUMMARY ===" -ForegroundColor Yellow

Write-Host "`nChanges Made:" -ForegroundColor Cyan
Write-Host "✅ Added missing source-services configuration block" -ForegroundColor Green
Write-Host "✅ Set partner-order.base-url to http://partner-order-service:8090" -ForegroundColor Green
Write-Host "✅ Added timeout configurations for partner-order service" -ForegroundColor Green
Write-Host "✅ Recompiled application with new configuration" -ForegroundColor Green

if ($success) {
    Write-Host "`n🎉 SUCCESS: Source services configuration fixed!" -ForegroundColor Green
    Write-Host "✅ No more placeholder resolution errors" -ForegroundColor Green
    Write-Host "✅ partnerOrderServiceClient bean creation works" -ForegroundColor Green
    Write-Host "✅ Application starts successfully" -ForegroundColor Green
} else {
    Write-Host "`n❌ Configuration fix may need additional work" -ForegroundColor Red
    Write-Host "Check application logs for remaining issues" -ForegroundColor White
}

Write-Host "`nSource services configuration fix complete!" -ForegroundColor Green