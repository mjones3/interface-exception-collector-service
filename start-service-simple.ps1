#!/usr/bin/env pwsh

# Simple service startup script

Write-Host "Starting Interface Exception Collector Service..." -ForegroundColor Green

# Change to service directory
Set-Location interface-exception-collector

# Start the service with local profile
Write-Host "Running: mvn spring-boot:run with local profile" -ForegroundColor Yellow

# Use environment variable to set profile
$env:SPRING_PROFILES_ACTIVE = "local"

try {
    mvn spring-boot:run
} catch {
    Write-Host "Error starting service: $_" -ForegroundColor Red
    Write-Host "`nTrying alternative startup method..." -ForegroundColor Yellow
    
    # Alternative: compile and run
    mvn clean compile
    mvn exec:java -Dexec.mainClass="com.arcone.biopro.exception.collector.InterfaceExceptionCollectorApplication"
}