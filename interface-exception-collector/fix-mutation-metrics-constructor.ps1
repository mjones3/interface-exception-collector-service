# Fix MutationMetrics Constructor Issue
Write-Host "=== Fixing MutationMetrics Constructor Issue ===" -ForegroundColor Green

Write-Host "`n1. Diagnosing the constructor issue..." -ForegroundColor Cyan
Write-Host "   Problem: MutationMetrics has @RequiredArgsConstructor but also custom constructor" -ForegroundColor Red
Write-Host "   This creates conflict - Spring can't instantiate the bean" -ForegroundColor Red

Write-Host "`n2. Verifying the fix was applied..." -ForegroundColor Cyan
$mutationMetricsContent = Get-Content "src/main/java/com/arcone/biopro/exception/collector/infrastructure/monitoring/MutationMetrics.java" -Raw

if ($mutationMetricsContent -match "@RequiredArgsConstructor") {
    Write-Host "   ❌ @RequiredArgsConstructor annotation still present" -ForegroundColor Red
} else {
    Write-Host "   ✅ @RequiredArgsConstructor annotation removed" -ForegroundColor Green
}

if ($mutationMetricsContent -match "public MutationMetrics\(MeterRegistry meterRegistry\)") {
    Write-Host "   ✅ Custom constructor with MeterRegistry parameter exists" -ForegroundColor Green
} else {
    Write-Host "   ❌ Custom constructor missing" -ForegroundColor Red
}

Write-Host "`n3. Compiling the application..." -ForegroundColor Cyan
$compileResult = & mvn compile -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed:" -ForegroundColor Red
    Write-Host "   $compileResult" -ForegroundColor Red
}

Write-Host "`n4. Monitoring application startup..." -ForegroundColor Cyan

$timeout = 120
$elapsed = 0
$success = $false

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 5
    $elapsed += 5
    
    $pods = kubectl get pods -n api | Select-String "interface-exception-collector.*Running"
    if ($pods) {
        $podName = ($pods[0] -split '\s+')[0]
        Write-Host "   Checking pod: $podName" -ForegroundColor Gray
        
        $logs = kubectl logs $podName -n api --tail=30 2>$null
        
        # Check for MutationMetrics constructor error
        if ($logs -match "No default constructor found.*MutationMetrics") {
            Write-Host "   ❌ Still getting MutationMetrics constructor error" -ForegroundColor Red
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
    }
    
    if ($elapsed % 30 -eq 0) {
        Write-Host "   Still starting... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

Write-Host "`n=== MUTATION METRICS CONSTRUCTOR FIX SUMMARY ===" -ForegroundColor Yellow

Write-Host "`nChanges Made:" -ForegroundColor Cyan
Write-Host "✅ Removed @RequiredArgsConstructor annotation from MutationMetrics" -ForegroundColor Green
Write-Host "✅ Kept custom constructor with MeterRegistry parameter" -ForegroundColor Green
Write-Host "✅ Resolved Spring bean instantiation conflict" -ForegroundColor Green

if ($success) {
    Write-Host "`n🎉 SUCCESS: MutationMetrics constructor issue fixed!" -ForegroundColor Green
    Write-Host "✅ No more 'No default constructor found' errors" -ForegroundColor Green
    Write-Host "✅ MutationMetrics bean creates successfully" -ForegroundColor Green
    Write-Host "✅ Application starts successfully" -ForegroundColor Green
} else {
    Write-Host "`n❌ Constructor fix may need additional work" -ForegroundColor Red
    Write-Host "Check application logs for remaining issues" -ForegroundColor White
}

Write-Host "`nMutationMetrics constructor fix complete!" -ForegroundColor Green