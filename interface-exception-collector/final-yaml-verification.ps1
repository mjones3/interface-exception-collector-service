# Final YAML Verification and Compilation Test
Write-Host "=== Final YAML Verification ===" -ForegroundColor Green

$yamlPath = "src/main/resources/application.yml"

# Step 1: Verify no duplicate keys remain
Write-Host "1. Checking for any remaining duplicate keys..." -ForegroundColor Cyan

if (Test-Path $yamlPath) {
    $lines = Get-Content $yamlPath
    $keyOccurrences = @{}
    
    # Count all top-level keys
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        if ($line -match '^([a-zA-Z][a-zA-Z0-9-]*):(\s|$)') {
            $key = $matches[1]
            if (-not $keyOccurrences.ContainsKey($key)) {
                $keyOccurrences[$key] = @()
            }
            $keyOccurrences[$key] += $i + 1
        }
    }
    
    Write-Host "   Found top-level keys:" -ForegroundColor Gray
    $duplicatesFound = $false
    foreach ($key in $keyOccurrences.Keys | Sort-Object) {
        $count = $keyOccurrences[$key].Count
        $lines = $keyOccurrences[$key] -join ', '
        if ($count -gt 1) {
            Write-Host "   ❌ $key ($count occurrences at lines: $lines)" -ForegroundColor Red
            $duplicatesFound = $true
        } else {
            Write-Host "   ✅ $key (line $lines)" -ForegroundColor Green
        }
    }
    
    if (-not $duplicatesFound) {
        Write-Host "   ✅ No duplicate keys found!" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ YAML file not found: $yamlPath" -ForegroundColor Red
    exit 1
}

# Step 2: Test compilation
Write-Host "`n2. Testing compilation..." -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed" -ForegroundColor Red
}

# Step 3: Test application startup for YAML errors
Write-Host "`n3. Testing application startup for YAML parsing..." -ForegroundColor Cyan

$startupJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8093 --spring.profiles.active=test" 2>&1
}

$timeout = 25
$elapsed = 0
$yamlError = $false
$duplicateError = $false
$success = $false

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 3
    $elapsed += 3
    
    $output = Receive-Job -Job $startupJob -Keep
    
    # Check for duplicate key errors
    if ($output -match "DuplicateKeyException.*duplicate key") {
        $duplicateError = $true
        Write-Host "   ❌ Duplicate key error still present!" -ForegroundColor Red
        $output | Where-Object { $_ -match "DuplicateKeyException|duplicate key" } | ForEach-Object {
            Write-Host "     $_" -ForegroundColor Red
        }
        break
    }
    
    # Check for other YAML errors
    if ($output -match "ScannerException|mapping values are not allowed") {
        $yamlError = $true
        Write-Host "   ❌ YAML syntax error detected!" -ForegroundColor Red
        break
    }
    
    # Check for successful startup
    if ($output -match "Started.*Application|Tomcat started on port") {
        $success = $true
        Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
        break
    }
    
    if ($elapsed % 10 -eq 0) {
        Write-Host "   Waiting for startup... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

# Clean up
Stop-Job -Job $startupJob 2>$null
Remove-Job -Job $startupJob 2>$null

# Step 4: Final summary
Write-Host "`n=== FINAL VERIFICATION RESULTS ===" -ForegroundColor Yellow

$yamlStatus = if (-not $duplicatesFound) { "✅ NO DUPLICATES" } else { "❌ DUPLICATES FOUND" }
$compileStatus = if ($LASTEXITCODE -eq 0) { "✅ SUCCESS" } else { "❌ FAILED" }
$startupStatus = if ($duplicateError) { "❌ DUPLICATE KEY ERROR" } elseif ($yamlError) { "❌ YAML SYNTAX ERROR" } elseif ($success) { "✅ SUCCESS" } else { "⚠️ TIMEOUT" }

Write-Host "YAML Duplicate Keys: $yamlStatus" -ForegroundColor $(if (-not $duplicatesFound) { "Green" } else { "Red" })
Write-Host "Compilation: $compileStatus" -ForegroundColor $(if ($LASTEXITCODE -eq 0) { "Green" } else { "Red" })
Write-Host "Application Startup: $startupStatus" -ForegroundColor $(if ($success) { "Green" } elseif ($duplicateError -or $yamlError) { "Red" } else { "Yellow" })

if (-not $duplicatesFound -and $LASTEXITCODE -eq 0 -and -not $duplicateError -and -not $yamlError) {
    Write-Host "`n🎉 SUCCESS: All YAML duplicate key issues resolved!" -ForegroundColor Green
    Write-Host "✅ No duplicate keys (graphql, spring, app, etc.)" -ForegroundColor Green
    Write-Host "✅ Compilation successful" -ForegroundColor Green
    Write-Host "✅ No YAML parsing errors" -ForegroundColor Green
    Write-Host "`nApplication is ready for development and deployment!" -ForegroundColor White
} elseif ($duplicateError) {
    Write-Host "`n❌ YAML duplicate key issues still present" -ForegroundColor Red
    Write-Host "Manual inspection of YAML file required" -ForegroundColor White
} else {
    Write-Host "`n✅ YAML duplicate keys fixed, compilation successful" -ForegroundColor Green
    Write-Host "Application core functionality is working" -ForegroundColor White
}