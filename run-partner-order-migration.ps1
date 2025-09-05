#!/usr/bin/env pwsh

# Script to run the partner order service database migration
# This removes the unique constraint on external_id to allow duplicate orders for retries

Write-Host "Running Partner Order Service Database Migration..." -ForegroundColor Green

# Check if we're in the right directory
if (-not (Test-Path "partner-order-service")) {
    Write-Host "Error: partner-order-service directory not found. Please run from project root." -ForegroundColor Red
    exit 1
}

Write-Host "`nMigration Details:" -ForegroundColor Yellow
Write-Host "- File: V002__Remove_external_id_unique_constraint.sql"
Write-Host "- Purpose: Remove unique constraint on external_id"
Write-Host "- Reason: Allow duplicate orders with same external_id for retry scenarios"

Write-Host "`nStarting partner-order-service with migration..." -ForegroundColor Cyan

try {
    # Change to partner-order-service directory
    Push-Location partner-order-service
    
    # Run the service (this will automatically apply migrations)
    Write-Host "Starting service to apply migrations..." -ForegroundColor Green
    mvn spring-boot:run -Dspring-boot.run.profiles=local
    
} catch {
    Write-Host "Error running migration: $_" -ForegroundColor Red
    exit 1
} finally {
    # Return to original directory
    Pop-Location
}

Write-Host "`nMigration completed!" -ForegroundColor Green
Write-Host "The external_id unique constraint has been removed."
Write-Host "Retries can now create duplicate orders with the same external_id but different transaction_ids."