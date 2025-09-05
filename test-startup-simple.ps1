Write-Host "Testing Application Startup" -ForegroundColor Green

# Test if we can compile and package
Write-Host "1. Testing compilation..." -ForegroundColor Cyan
try {
    Set-Location "interface-exception-collector"
    $compileResult = mvn compile -q
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Compilation successful" -ForegroundColor Green
    } else {
        Write-Host "Compilation failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Compilation error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test if we can package
Write-Host "2. Testing packaging..." -ForegroundColor Cyan
try {
    $packageResult = mvn package -DskipTests -q
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Packaging successful" -ForegroundColor Green
    } else {
        Write-Host "Packaging failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Packaging error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "Build tests completed successfully" -ForegroundColor Green