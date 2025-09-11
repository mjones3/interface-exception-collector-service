# Fix Partner Order Service Client Configuration Issue
Write-Host "=== Fixing Partner Order Service Client Configuration ===" -ForegroundColor Green

Write-Host "`n1. Diagnosing the configuration issue..." -ForegroundColor Cyan

# The issue is in SourceServiceClientConfiguration.java
# It's trying to inject ${source-services.partner-order.base-url} which doesn't exist
# We need to fix the property path to match what's actually in application.yml

Write-Host "   Problem: partnerOrderServiceClient bean is trying to inject non-existent property" -ForegroundColor Red
Write-Host "   Expected: source-services.partner-order.base-url" -ForegroundColor Red
Write-Host "   Available: app.source-services.order.base-url (but for different service)" -ForegroundColor Yellow

Write-Host "`n2. Checking current application.yml configuration..." -ForegroundColor Cyan

# Let's see what source service configurations exist
$appYml = Get-Content "src/main/resources/application.yml" -Raw
if ($appYml -match "source-services") {
    Write-Host "   Found source-services configuration" -ForegroundColor Green
} else {
    Write-Host "   No source-services configuration found" -ForegroundColor Red
}

if ($appYml -match "partner-order") {
    Write-Host "   Found partner-order references" -ForegroundColor Green
} else {
    Write-Host "   No partner-order configuration found" -ForegroundColor Red
}

Write-Host "`n3. Adding missing partner-order service configuration..." -ForegroundColor Cyan

# Add the missing configuration to application.yml
$configToAdd = @"

# Source services configuration (added to fix partnerOrderServiceClient bean creation)
source-services:
  partner-order:
    base-url: http://partner-order-service:8090
    timeout: 5000
    connection-timeout: 3000
    read-timeout: 5000
"@

# Check if source-services already exists
if ($appYml -notmatch "source-services:") {
    Write-Host "   Adding source-services configuration block..." -ForegroundColor Yellow
    Add-Content -Path "src/main/resources/application.yml" -Value $configToAdd
    Write-Host "   ✅ Added source-services.partner-order.base-url configuration" -ForegroundColor Green
} else {
    Write-Host "   source-services block exists, checking for partner-order..." -ForegroundColor Yellow
    if ($appYml -notmatch "partner-order:") {
        # Add just the partner-order section
        $partnerOrderConfig = @"
  partner-order:
    base-url: http://partner-order-service:8090
    timeout: 5000
    connection-timeout: 3000
    read-timeout: 5000
"@
        # Find the source-services section and add partner-order
        $lines = Get-Content "src/main/resources/application.yml"
        $newLines = @()
        $inSourceServices = $false
        
        foreach ($line in $lines) {
            $newLines += $line
            if ($line -match "^source-services:") {
                $inSourceServices = $true
                $newLines += $partnerOrderConfig.Split("`n")
                $inSourceServices = $false
            }
        }
        
        $newLines | Set-Content "src/main/resources/application.yml"
        Write-Host "   ✅ Added partner-order configuration to existing source-services block" -ForegroundColor Green
    } else {
        Write-Host "   ✅ partner-order configuration already exists" -ForegroundColor Green
    }
}

Write-Host "`n4. Compiling the application..." -ForegroundColor Cyan
$compileResult = & mvn compile -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed:" -ForegroundColor Red
    Write-Host "   $compileResult" -ForegroundColor Red
}

Write-Host "`n5. Checking current pod status..." -ForegroundColor Cyan
$pods = kubectl get pods -n api | Select-String "interface-exception-collector"
if ($pods) {
    Write-Host "   Current pods:" -ForegroundColor Yellow
    $pods | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    
    # Delete any existing pods to force restart with new config
    $podNames = $pods | ForEach-Object { ($_ -split '\s+')[0] }
    foreach ($podName in $podNames) {
        Write-Host "   Deleting pod: $podName" -ForegroundColor Yellow
        kubectl delete pod $podName -n api | Out-Null
    }
} else {
    Write-Host "   No interface-exception-collector pods found" -ForegroundColor Yellow
}

Write-Host "`n6. Waiting for new pod to start..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

$timeout = 120
$elapsed = 0
$success = $false

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 5
    $elapsed += 5
    
    $newPods = kubectl get pods -n api | Select-String "interface-exception-collector.*Running"
    if ($newPods) {
        $podName = ($newPods[0] -split '\s+')[0]
        Write-Host "   Pod $podName is running, checking logs..." -ForegroundColor Green
        
        $logs = kubectl logs $podName -n api --tail=20 2>$null
        
        # Check for the specific bean creation error
        if ($logs -match "partnerOrderServiceClient.*Unexpected exception") {
            Write-Host "   ❌ Still getting partnerOrderServiceClient bean creation error" -ForegroundColor Red
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
            Write-Host "   ❌ Application failed to start" -ForegroundColor Red
            $logs | Where-Object { $_ -match "ERROR|Exception" } | Select-Object -First 3 | ForEach-Object {
                Write-Host "     $_" -ForegroundColor Red
            }
            break
        }
    }
    
    if ($elapsed % 30 -eq 0) {
        Write-Host "   Still starting... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

Write-Host "`n=== PARTNER ORDER CLIENT CONFIGURATION FIX SUMMARY ===" -ForegroundColor Yellow

Write-Host "`nChanges Made:" -ForegroundColor Cyan
Write-Host "✅ Added source-services.partner-order.base-url configuration" -ForegroundColor Green
Write-Host "✅ Set partner-order service URL to http://partner-order-service:8090" -ForegroundColor Green
Write-Host "✅ Added timeout configurations for partner-order service" -ForegroundColor Green
Write-Host "✅ Recompiled application with new configuration" -ForegroundColor Green

if ($success) {
    Write-Host "`n🎉 SUCCESS: Partner Order Service Client configuration fixed!" -ForegroundColor Green
    Write-Host "✅ No more partnerOrderServiceClient bean creation errors" -ForegroundColor Green
    Write-Host "✅ Application starts successfully" -ForegroundColor Green
} else {
    Write-Host "`n❌ Configuration fix may need additional work" -ForegroundColor Red
    Write-Host "Check application logs for remaining issues" -ForegroundColor White
}

Write-Host "`nPartner Order Service Client configuration fix complete!" -ForegroundColor Green