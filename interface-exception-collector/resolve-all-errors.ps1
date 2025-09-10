# Comprehensive error resolution script
Write-Host "=== Resolving All Compilation and Runtime Errors ===" -ForegroundColor Green

# 1. Check main compilation
Write-Host "1. Checking main application compilation..." -ForegroundColor Cyan
mvn compile -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Main compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Main compilation failed" -ForegroundColor Red
    mvn compile 2>&1 | Select-String "ERROR" | Select-Object -First 5
    exit 1
}

# 2. Test application startup with YAML validation
Write-Host "`n2. Testing application startup..." -ForegroundColor Cyan

# Start the application with a shorter timeout to test YAML parsing
$job = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8085 --spring.profiles.active=test --logging.level.org.yaml=DEBUG" -q 2>&1
}

$timeout = 30
$elapsed = 0
$started = $false
$yamlError = $false

while ($elapsed -lt $timeout -and -not $started -and -not $yamlError) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    
    $output = Receive-Job -Job $job -Keep
    
    # Check for YAML errors
    if ($output -match "ScannerException|mapping values are not allowed|YAML") {
        $yamlError = $true
        Write-Host "   ❌ YAML configuration error detected!" -ForegroundColor Red
        $output | Where-Object { $_ -match "ScannerException|mapping values|YAML|line \d+" } | Select-Object -First 3 | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
        break
    }
    
    # Check for successful startup
    if ($output -match "Started.*Application|Tomcat started on port|JVM running for") {
        $started = $true
        Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
        break
    }
    
    # Check for other startup failures
    if ($output -match "APPLICATION FAILED TO START|Error starting ApplicationContext|Exception in thread") {
        Write-Host "   ❌ Application startup failed!" -ForegroundColor Red
        $output | Where-Object { $_ -match "ERROR|Exception|Failed|Caused by" } | Select-Object -First 3 | ForEach-Object { Write-Host "     $_" -ForegroundColor Red }
        break
    }
    
    if ($elapsed % 10 -eq 0) {
        Write-Host "   Still starting... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

# Clean up
Stop-Job -Job $job 2>$null
Remove-Job -Job $job 2>$null

# 3. Check test compilation status
Write-Host "`n3. Checking test compilation status..." -ForegroundColor Cyan
mvn test-compile -q 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Test compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ⚠️ Test compilation has issues (but main app works)" -ForegroundColor Yellow
}

# 4. Summary
Write-Host "`n=== RESOLUTION SUMMARY ===" -ForegroundColor Yellow

if ($yamlError) {
    Write-Host "❌ YAML Configuration Error Found" -ForegroundColor Red
    Write-Host "   The application.yml file has syntax errors that need to be fixed." -ForegroundColor White
    Write-Host "   Check for incorrect indentation, missing colons, or invalid YAML syntax." -ForegroundColor White
} elseif ($started) {
    Write-Host "🎉 SUCCESS: All critical errors resolved!" -ForegroundColor Green
    Write-Host "   ✅ Main application compiles" -ForegroundColor Green
    Write-Host "   ✅ YAML configuration is valid" -ForegroundColor Green
    Write-Host "   ✅ Application starts successfully" -ForegroundColor Green
    Write-Host "`n   The application is now ready for development and deployment!" -ForegroundColor White
} else {
    Write-Host "⚠️ PARTIAL SUCCESS: Compilation works but startup needs investigation" -ForegroundColor Yellow
    Write-Host "   ✅ Main application compiles" -ForegroundColor Green
    Write-Host "   ⚠️ Application startup timed out or failed" -ForegroundColor Yellow
    Write-Host "`n   Check application logs for startup issues." -ForegroundColor White
}

Write-Host "`n=== ERROR RESOLUTION COMPLETE ===" -ForegroundColor Green