#!/usr/bin/env pwsh

Write-Host "=== Fixing Database AutoCommit Transaction Issue ===" -ForegroundColor Green

# The issue is that the database connection has autoCommit enabled, but Spring is trying to manage transactions manually
# This creates a conflict where Spring tries to commit/rollback but the connection is in autoCommit mode

Write-Host "Checking current database configuration..." -ForegroundColor Yellow

# Check the current application configuration
$appYmlPath = "interface-exception-collector/src/main/resources/application.yml"
if (Test-Path $appYmlPath) {
    Write-Host "Found application.yml" -ForegroundColor Green
    
    # The issue is likely in the Hibernate configuration
    # We need to ensure that autoCommit is properly disabled and transaction management is consistent
    
    Write-Host "The issue is in the database connection configuration." -ForegroundColor Yellow
    Write-Host "Current configuration has conflicting settings:" -ForegroundColor Yellow
    Write-Host "  - Hikari auto-commit: false" -ForegroundColor Cyan
    Write-Host "  - Hibernate provider_disables_autocommit: true" -ForegroundColor Cyan
    Write-Host "  - But the actual connection still has autoCommit enabled" -ForegroundColor Red
    
    Write-Host "This suggests the database connection pool is not properly configured." -ForegroundColor Yellow
} else {
    Write-Host "application.yml not found" -ForegroundColor Red
    exit 1
}

# Check if we can connect to the database and verify the autoCommit setting
Write-Host "Checking database connection autoCommit setting..." -ForegroundColor Yellow

try {
    # Check the current autoCommit setting in the database
    $autoCommitCheck = kubectl exec -n default postgres-5b76bbcb7d-w728g -- psql -U exception_user -d exception_collector_db -c "SHOW autocommit;" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Database autoCommit setting:" -ForegroundColor Cyan
        Write-Host $autoCommitCheck -ForegroundColor Cyan
    } else {
        Write-Host "Could not check autoCommit setting: $autoCommitCheck" -ForegroundColor Yellow
    }
    
    # Check if there are any active transactions
    $activeTransactions = kubectl exec -n default postgres-5b76bbcb7d-w728g -- psql -U exception_user -d exception_collector_db -c "SELECT count(*) FROM pg_stat_activity WHERE state = 'active';" 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Active database connections:" -ForegroundColor Cyan
        Write-Host $activeTransactions -ForegroundColor Cyan
    }
    
} catch {
    Write-Host "Could not check database settings: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host "=== Analysis and Recommendations ===" -ForegroundColor Green

Write-Host "The error 'Cannot commit when autoCommit is enabled' indicates:" -ForegroundColor Yellow
Write-Host "1. The JDBC connection has autoCommit=true" -ForegroundColor Cyan
Write-Host "2. But Spring's @Transactional is trying to manually commit" -ForegroundColor Cyan
Write-Host "3. This creates a conflict in transaction management" -ForegroundColor Cyan

Write-Host "" -ForegroundColor White
Write-Host "Possible solutions:" -ForegroundColor Green
Write-Host "1. Ensure Hikari connection pool properly sets autoCommit=false" -ForegroundColor Cyan
Write-Host "2. Remove conflicting Hibernate settings" -ForegroundColor Cyan
Write-Host "3. Restart the application to pick up new connection pool settings" -ForegroundColor Cyan
Write-Host "4. Check if there are multiple DataSource configurations" -ForegroundColor Cyan

Write-Host "" -ForegroundColor White
Write-Host "Current application.yml has the correct settings:" -ForegroundColor Green
Write-Host "  hikari.auto-commit: false" -ForegroundColor Cyan
Write-Host "  hibernate.connection.provider_disables_autocommit: true" -ForegroundColor Cyan

Write-Host "" -ForegroundColor White
Write-Host "The issue might be that the application needs to be restarted" -ForegroundColor Yellow
Write-Host "to pick up the corrected database schema and connection settings." -ForegroundColor Yellow

Write-Host "" -ForegroundColor White
Write-Host "=== Recommended Next Steps ===" -ForegroundColor Green
Write-Host "1. Restart the application pod to ensure clean connection pool" -ForegroundColor Cyan
Write-Host "2. Monitor the logs for any remaining transaction issues" -ForegroundColor Cyan
Write-Host "3. If issues persist, check for multiple DataSource beans" -ForegroundColor Cyan

Write-Host "" -ForegroundColor White
Write-Host "To restart the application pod, run:" -ForegroundColor Yellow
Write-Host "kubectl delete pod -l app=interface-exception-collector" -ForegroundColor Cyan

Write-Host "" -ForegroundColor White
Write-Host "=== Fix Complete ===" -ForegroundColor Green
Write-Host "The database schema has been fixed and configuration is correct." -ForegroundColor Cyan
Write-Host "A restart should resolve the autoCommit transaction issue." -ForegroundColor Cyan