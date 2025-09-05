#!/usr/bin/env powershell

# PowerShell script to start the interface exception service without tests

Write-Host "Starting Interface Exception Service (No Tests)..." -ForegroundColor Green

# Step 1: Move test files temporarily to avoid compilation issues
Write-Host "Step 1: Moving test files temporarily..." -ForegroundColor Yellow
if (Test-Path "interface-exception-collector/temp-disabled-tests") {
    Remove-Item -Recurse -Force "interface-exception-collector/temp-disabled-tests"
}
New-Item -ItemType Directory -Path "interface-exception-collector/temp-disabled-tests" -Force
Move-Item "interface-exception-collector/src/test" "interface-exception-collector/temp-disabled-tests/" -Force

# Step 2: Set the Spring profile as an environment variable
Write-Host "Step 2: Setting Spring profile to 'local'..." -ForegroundColor Yellow
$env:SPRING_PROFILES_ACTIVE = "local"

# Step 3: Start the application
Write-Host "Step 3: Starting application with local profile (H2 database)..." -ForegroundColor Yellow
Write-Host "Application will be available at: http://localhost:8080" -ForegroundColor Cyan
Write-Host "Health check: http://localhost:8080/actuator/health" -ForegroundColor Cyan
Write-Host "GraphQL endpoint: http://localhost:8080/graphql" -ForegroundColor Cyan
Write-Host "GraphiQL interface: http://localhost:8080/graphiql" -ForegroundColor Cyan
Write-Host "H2 Console: http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop the application" -ForegroundColor Yellow
Write-Host ""

try {
    mvn -f interface-exception-collector/pom.xml spring-boot:run
} finally {
    # Step 4: Restore test files
    Write-Host "Restoring test files..." -ForegroundColor Yellow
    if (Test-Path "interface-exception-collector/temp-disabled-tests/test") {
        Move-Item "interface-exception-collector/temp-disabled-tests/test" "interface-exception-collector/src/" -Force
        Remove-Item -Recurse -Force "interface-exception-collector/temp-disabled-tests"
    }
    Write-Host "Test files restored." -ForegroundColor Green
}