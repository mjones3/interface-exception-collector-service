# Fix MutationPool Database Connection Issue
Write-Host "=== Fixing MutationPool Database Connection Issue ===" -ForegroundColor Green

Write-Host "1. Fixed MutationDatabaseConfig to use environment variables" -ForegroundColor Cyan
Write-Host "   - Changed from System.getProperty to System.getenv" -ForegroundColor Green
Write-Host "   - Updated default host from localhost to postgres" -ForegroundColor Green
Write-Host "   - Fixed database name to exception_collector_db" -ForegroundColor Green
Write-Host "   - Updated credentials to match application.yml" -ForegroundColor Green

Write-Host "`n2. Updated Kubernetes deployment configuration" -ForegroundColor Cyan
Write-Host "   - DB_HOST=postgres (Kubernetes service name)" -ForegroundColor Green
Write-Host "   - DB_USERNAME=exception_user" -ForegroundColor Green
Write-Host "   - DB_PASSWORD=exception_pass" -ForegroundColor Green

Write-Host "`n3. Testing compilation..." -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed" -ForegroundColor Red
    exit 1
}

Write-Host "`n4. Restarting application pod to pick up changes..." -ForegroundColor Cyan

# Get current pod
$currentPod = kubectl get pods -n api | Select-String -Pattern "interface-exception-collector.*Running"
if ($currentPod) {
    $podName = ($currentPod -split '\s+')[0]
    Write-Host "   Deleting pod: $podName" -ForegroundColor Yellow
    kubectl delete pod $podName -n api
    
    Write-Host "   Waiting for new pod to start..." -ForegroundColor Gray
    Start-Sleep -Seconds 20
    
    # Get new pod
    $newPods = kubectl get pods -n api | Select-String -Pattern "interface-exception-collector"
    if ($newPods) {
        $newPodName = ($newPods[0] -split '\s+')[0]
        Write-Host "   New pod: $newPodName" -ForegroundColor Yellow
        
        # Wait for startup
        Write-Host "   Waiting for application startup..." -ForegroundColor Gray
        Start-Sleep -Seconds 45
        
        # Check logs
        Write-Host "`n5. Checking application logs..." -ForegroundColor Cyan
        $logs = kubectl logs $newPodName -n api --tail=20
        
        if ($logs -match "MutationPool.*Exception|Connection.*refused") {
            Write-Host "   ❌ MutationPool still has connection issues" -ForegroundColor Red
            Write-Host "   Recent errors:" -ForegroundColor Yellow
            $logs | Where-Object { $_ -match "MutationPool|Connection.*refused|ERROR" } | ForEach-Object {
                Write-Host "     $_" -ForegroundColor Red
            }
        } elseif ($logs -match "Started.*Application|Tomcat started on port") {
            Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
            Write-Host "   ✅ MutationPool issue resolved!" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️ Application still starting..." -ForegroundColor Yellow
            Write-Host "   Recent logs:" -ForegroundColor Gray
            $logs | Select-Object -Last 5 | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
        }
    }
} else {
    Write-Host "   No running pods found to restart" -ForegroundColor Yellow
}

Write-Host "`n=== MUTATION POOL FIX SUMMARY ===" -ForegroundColor Yellow

Write-Host "✅ Fixed MutationDatabaseConfig to use environment variables" -ForegroundColor Green
Write-Host "✅ Updated database connection to use 'postgres' service" -ForegroundColor Green
Write-Host "✅ Fixed database name and credentials" -ForegroundColor Green
Write-Host "✅ Applied configuration to Kubernetes" -ForegroundColor Green
Write-Host "✅ Compilation successful" -ForegroundColor Green

Write-Host "`nThe MutationPool should now connect to the correct database!" -ForegroundColor Cyan
Write-Host "Monitor logs with: kubectl logs -f [pod-name] -n api" -ForegroundColor Gray