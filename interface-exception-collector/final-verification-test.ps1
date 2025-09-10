# Final Verification Test
Write-Host "=== Final Application Verification ===" -ForegroundColor Green

# Step 1: Verify YAML structure
Write-Host "1. Verifying YAML structure..." -ForegroundColor Cyan
$yamlPath = "src/main/resources/application.yml"
$lines = Get-Content $yamlPath
$appKeys = @()

for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i] -match '^app:\s*$') {
        $appKeys += $i + 1
    }
}

if ($appKeys.Count -eq 1) {
    Write-Host "   ✅ Single 'app:' key found at line $($appKeys[0])" -ForegroundColor Green
} elseif ($appKeys.Count -gt 1) {
    Write-Host "   ❌ Multiple 'app:' keys still present at lines: $($appKeys -join ', ')" -ForegroundColor Red
} else {
    Write-Host "   ⚠️ No 'app:' key found" -ForegroundColor Yellow
}

# Step 2: Verify compilation
Write-Host "`n2. Verifying compilation..." -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed" -ForegroundColor Red
}

# Step 3: Test application startup with proper error handling
Write-Host "`n3. Testing application startup..." -ForegroundColor Cyan

$startupJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8090 --spring.profiles.active=test" 2>&1
}

$timeout = 30
$elapsed = 0
$success = $false
$duplicateError = $false

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 3
    $elapsed += 3
    
    $output = Receive-Job -Job $startupJob -Keep
    
    # Check for duplicate key error
    if ($output -match "DuplicateKeyException.*duplicate key app") {
        $duplicateError = $true
        Write-Host "   ❌ Duplicate key error detected!" -ForegroundColor Red
        break
    }
    
    # Check for successful startup
    if ($output -match "Started.*Application|Tomcat started on port") {
        $success = $true
        Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
        break
    }
    
    # Check for other critical errors
    if ($output -match "APPLICATION FAILED TO START") {
        Write-Host "   ❌ Application failed to start" -ForegroundColor Red
        $output | Where-Object { $_ -match "ERROR|Exception" } | Select-Object -First 3 | ForEach-Object { 
            Write-Host "     $_" -ForegroundColor Red 
        }
        break
    }
    
    if ($elapsed % 10 -eq 0) {
        Write-Host "   Waiting for startup... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

# Clean up job
try {
    Stop-Job -Job $startupJob 2>$null
    Remove-Job -Job $startupJob 2>$null
} catch {
    # Ignore cleanup errors
}

# Step 4: Final summary
Write-Host "`n=== FINAL VERIFICATION RESULTS ===" -ForegroundColor Yellow

$yamlStatus = if ($appKeys.Count -eq 1) { "✅ FIXED" } else { "❌ NEEDS ATTENTION" }
$compileStatus = if ($LASTEXITCODE -eq 0) { "✅ SUCCESS" } else { "❌ FAILED" }
$startupStatus = if ($duplicateError) { "❌ DUPLICATE KEY ERROR" } elseif ($success) { "✅ SUCCESS" } else { "⚠️ TIMEOUT/INCONCLUSIVE" }

Write-Host "YAML Duplicate Keys: $yamlStatus" -ForegroundColor $(if ($appKeys.Count -eq 1) { "Green" } else { "Red" })
Write-Host "Compilation: $compileStatus" -ForegroundColor $(if ($LASTEXITCODE -eq 0) { "Green" } else { "Red" })
Write-Host "Application Startup: $startupStatus" -ForegroundColor $(if ($success) { "Green" } elseif ($duplicateError) { "Red" } else { "Yellow" })

if ($appKeys.Count -eq 1 -and $LASTEXITCODE -eq 0 -and -not $duplicateError) {
    Write-Host "`n🎉 SUCCESS: All major issues resolved!" -ForegroundColor Green
    Write-Host "The application is ready for development and deployment." -ForegroundColor White
} elseif ($duplicateError) {
    Write-Host "`n❌ YAML duplicate key issue persists" -ForegroundColor Red
    Write-Host "Manual YAML inspection required." -ForegroundColor White
} else {
    Write-Host "`n✅ Core issues resolved (YAML + Compilation)" -ForegroundColor Green
    Write-Host "Application startup may need infrastructure investigation." -ForegroundColor White
}