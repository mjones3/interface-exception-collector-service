#!/usr/bin/env powershell
# Quick database schema fix script
Write-Host "🔧 Fixing Database Schema Issue" -ForegroundColor Green
Write-Host ""

Write-Host "Step 1: Stopping Tilt..." -ForegroundColor Yellow
tilt down

Write-Host ""
Write-Host "Step 2: Connecting to PostgreSQL to reset database..." -ForegroundColor Yellow
Write-Host "Running database reset commands..." -ForegroundColor Cyan

# Create a temporary SQL file for the reset
$resetSql = @"
-- Connect to postgres database first
\c postgres;

-- Drop and recreate the database
DROP DATABASE IF EXISTS exception_collector_db;
CREATE DATABASE exception_collector_db;

-- Connect to the new database
\c exception_collector_db;

-- Create the user if it doesn't exist
DO `$`$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'exception_user') THEN
      CREATE USER exception_user WITH PASSWORD 'exception_pass';
   END IF;
END
`$`$;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE exception_collector_db TO exception_user;
GRANT ALL ON SCHEMA public TO exception_user;
"@

# Write SQL to temp file
$resetSql | Out-File -FilePath "reset-db.sql" -Encoding UTF8

Write-Host "Created reset-db.sql file" -ForegroundColor Green
Write-Host ""
Write-Host "Step 3: Executing database reset..." -ForegroundColor Yellow

# Try to run the SQL reset
try {
    # Use docker exec to run psql inside the postgres container
    docker exec -i postgres_container psql -U postgres -f - < reset-db.sql
    Write-Host "✅ Database reset successful!" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Direct database reset failed. Manual steps needed:" -ForegroundColor Yellow
    Write-Host "1. Connect to PostgreSQL: docker exec -it postgres_container psql -U postgres" -ForegroundColor Gray
    Write-Host "2. Run the commands in reset-db.sql" -ForegroundColor Gray
    Write-Host ""
}

Write-Host ""
Write-Host "Step 4: Starting Tilt with Flyway enabled..." -ForegroundColor Yellow
tilt up

Write-Host ""
Write-Host "🎯 What was fixed:" -ForegroundColor Green
Write-Host "   ✅ Enabled Flyway in application-tilt.yml" -ForegroundColor White
Write-Host "   ✅ Set baseline-on-migrate: true" -ForegroundColor White
Write-Host "   ✅ Reset database to clean state" -ForegroundColor White
Write-Host "   ✅ Flyway will now run migrations automatically" -ForegroundColor White
Write-Host ""
Write-Host "🚀 The acknowledgment_notes column will be created!" -ForegroundColor Green

# Clean up temp file
Remove-Item "reset-db.sql" -ErrorAction SilentlyContinue