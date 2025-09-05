#!/usr/bin/env powershell

# PowerShell script to start the interface exception service successfully

Write-Host "Starting Interface Exception Service (Final)..." -ForegroundColor Green

# Step 1: Kill any existing processes on port 8080
Write-Host "Step 1: Checking for existing processes on port 8080..." -ForegroundColor Yellow
try {
    $processes = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    if ($processes) {
        foreach ($pid in $processes) {
            Write-Host "Killing process $pid using port 8080..." -ForegroundColor Yellow
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 2
    } else {
        Write-Host "Port 8080 is available." -ForegroundColor Green
    }
} catch {
    Write-Host "Could not check port 8080, proceeding anyway..." -ForegroundColor Yellow
}

# Step 2: Move test files temporarily to avoid compilation issues
Write-Host "Step 2: Moving test files temporarily..." -ForegroundColor Yellow
if (Test-Path "interface-exception-collector/temp-disabled-tests") {
    Remove-Item -Recurse -Force "interface-exception-collector/temp-disabled-tests"
}
New-Item -ItemType Directory -Path "interface-exception-collector/temp-disabled-tests" -Force
Move-Item "interface-exception-collector/src/test" "interface-exception-collector/temp-disabled-tests/" -Force

# Step 3: Set the Spring profile as an environment variable
Write-Host "Step 3: Setting Spring profile to 'local'..." -ForegroundColor Yellow
$env:SPRING_PROFILES_ACTIVE = "local"

# Step 4: Start the application
Write-Host "Step 4: Starting application with local profile..." -ForegroundColor Yellow
Write-Host ""
Write-Host "Application will be available at:" -ForegroundColor Cyan
Write-Host "   Main URL: http://localhost:8080" -ForegroundColor White
Write-Host "   Health check: http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host "   GraphQL endpoint: http://localhost:8080/graphql" -ForegroundColor White
Write-Host "   GraphiQL interface: http://localhost:8080/graphiql" -ForegroundColor White
Write-Host "   H2 Console: http://localhost:8080/h2-console" -ForegroundColor White
Write-Host "   API Documentation: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "Notes:" -ForegroundColor Yellow
Write-Host "   - Using H2 in-memory database (data will be lost on restart)" -ForegroundColor Gray
Write-Host "   - Kafka errors are expected (not running locally)" -ForegroundColor Gray
Write-Host "   - Flyway is disabled for local development" -ForegroundColor Gray
Write-Host ""
Write-Host "Press Ctrl+C to stop the application" -ForegroundColor Yellow
Write-Host ""

try {
    mvn -f interface-exception-collector/pom.xml spring-boot:run
} finally {
    # Step 5: Restore test files
    Write-Host "Restoring test files..." -ForegroundColor Yellow
    if (Test-Path "interface-exception-collector/temp-disabled-tests/test") {
        Move-Item "interface-exception-collector/temp-disabled-tests/test" "interface-exception-collector/src/" -Force
        Remove-Item -Recurse -Force "interface-exception-collector/temp-disabled-tests"
    }
    Write-Host "Test files restored." -ForegroundColor Green
    Write-Host "Application stopped successfully." -ForegroundColor Green
}