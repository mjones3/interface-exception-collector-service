#!/usr/bin/env powershell

# PowerShell script to start the interface exception service properly

Write-Host "Starting Interface Exception Service..." -ForegroundColor Green

# Set the Spring profile as an environment variable
$env:SPRING_PROFILES_ACTIVE = "local"

# Start the application
Write-Host "Starting with local profile (H2 database)..." -ForegroundColor Yellow
mvn -f interface-exception-collector/pom.xml spring-boot:run