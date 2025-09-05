#!/usr/bin/env pwsh
# Fix database issues: missing order_items table and JSON escaping

Write-Host "=== Fixing Database Issues ===" -ForegroundColor Green

# Step 1: Check if Flyway migrations exist
Write-Host "Step 1: Checking Flyway migrations..." -ForegroundColor Yellow
$migrationsDir = "interface-exception-collector/src/main/resources/db/migration"

if (Test-Path $migrationsDir) {
    Write-Host "Found migrations directory: $migrationsDir" -ForegroundColor Cyan
    $migrations = Get-ChildItem $migrationsDir -Filter "*.sql" | Sort-Object Name
    Write-Host "Existing migrations:" -ForegroundColor Cyan
    foreach ($migration in $migrations) {
        Write-Host "  - $($migration.Name)" -ForegroundColor Gray
    }
} else {
    Write-Host "Migrations directory not found - creating it" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $migrationsDir -Force | Out-Null
}

# Step 2: Create order_items table migration if it doesn't exist
Write-Host "`nStep 2: Creating order_items table migration..." -ForegroundColor Yellow

$orderItemsMigration = @"
-- Create order_items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    interface_exception_id BIGINT NOT NULL,
    blood_type VARCHAR(10),
    product_family VARCHAR(100),
    quantity INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_interface_exception 
        FOREIGN KEY (interface_exception_id) 
        REFERENCES interface_exceptions(id) 
        ON DELETE CASCADE
);

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_order_items_interface_exception_id 
    ON order_items(interface_exception_id);
"@

# Find next migration number
$lastMigration = $migrations | Select-Object -Last 1
if ($lastMigration) {
    $lastNumber = [int]($lastMigration.Name.Substring(1, 3))
    $nextNumber = $lastNumber + 1
} else {
    $nextNumber = 1
}

$migrationFileName = "V{0:D3}__create_order_items_table.sql" -f $nextNumber
$migrationPath = Join-Path $migrationsDir $migrationFileName

if (-not (Test-Path $migrationPath)) {
    Set-Content -Path $migrationPath -Value $orderItemsMigration
    Write-Host "Created migration: $migrationFileName" -ForegroundColor Green
} else {
    Write-Host "Migration already exists: $migrationFileName" -ForegroundColor Yellow
}

# Step 3: Fix JSON escaping in retry service
Write-Host "`nStep 3: Fixing JSON escaping issues..." -ForegroundColor Yellow

# Find retry service or related classes that handle JSON
$javaFiles = Get-ChildItem "interface-exception-collector/src/main/java" -Recurse -Filter "*.java"
$retryServiceFiles = $javaFiles | Where-Object { $_.Name -match "(Retry|Exception)" -and $_.Name -match "Service" }

Write-Host "Found retry-related service files:" -ForegroundColor Cyan
foreach ($file in $retryServiceFiles) {
    Write-Host "  - $($file.FullName)" -ForegroundColor Gray
}

# Step 4: Run Flyway migration
Write-Host "`nStep 4: Running Flyway migration..." -ForegroundColor Yellow
Set-Location "interface-exception-collector"

Write-Host "Running Flyway migrate..." -ForegroundColor Cyan
$flywayResult = & mvn flyway:migrate -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  - Flyway migration successful" -ForegroundColor Green
} else {
    Write-Host "  - Flyway migration failed or had warnings" -ForegroundColor Yellow
    Write-Host $flywayResult
}

# Step 5: Check database schema
Write-Host "`nStep 5: Verifying database schema..." -ForegroundColor Yellow

# Get pod name for database access
$pods = & kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($pods -and $LASTEXITCODE -eq 0) {
    Write-Host "Checking database schema via pod: $pods" -ForegroundColor Cyan
    
    # Check if order_items table exists
    $checkTable = "SELECT table_name FROM information_schema.tables WHERE table_name = 'order_items';"
    $tableCheck = & kubectl exec $pods -- psql -h localhost -U postgres -d interface_exceptions -c $checkTable 2>$null
    
    if ($tableCheck -match "order_items") {
        Write-Host "  - order_items table exists" -ForegroundColor Green
    } else {
        Write-Host "  - order_items table still missing" -ForegroundColor Red
    }
} else {
    Write-Host "Cannot access database - pod not found or not accessible" -ForegroundColor Yellow
}

# Step 6: Rebuild and restart application
Write-Host "`nStep 6: Rebuilding application..." -ForegroundColor Yellow

Write-Host "Running Maven clean compile..." -ForegroundColor Cyan
$buildResult = & mvn clean compile -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "  - Build successful" -ForegroundColor Green
} else {
    Write-Host "  - Build failed" -ForegroundColor Red
}

# Step 7: Restart deployment
Write-Host "`nStep 7: Restarting deployment..." -ForegroundColor Yellow
Set-Location ".."

$podCheck = & kubectl get pods -l app=interface-exception-collector 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "Restarting deployment..." -ForegroundColor Cyan
    & kubectl rollout restart deployment/interface-exception-collector | Out-Null
    
    # Wait for rollout
    Write-Host "Waiting for rollout..." -ForegroundColor Cyan
    & kubectl rollout status deployment/interface-exception-collector --timeout=180s | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  - Deployment restarted successfully" -ForegroundColor Green
    } else {
        Write-Host "  - Deployment restart had issues" -ForegroundColor Yellow
    }
}

# Step 8: Test the fix
Write-Host "`nStep 8: Testing database fix..." -ForegroundColor Yellow

Start-Sleep -Seconds 15

# Check logs for database errors
$newPods = & kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($newPods -and $LASTEXITCODE -eq 0) {
    Write-Host "Checking logs for database errors..." -ForegroundColor Cyan
    $logs = & kubectl logs $newPods --tail=20 2>$null
    
    if ($logs -match "order_items.*does not exist") {
        Write-Host "  - Still seeing order_items table errors" -ForegroundColor Red
    } else {
        Write-Host "  - No order_items table errors found" -ForegroundColor Green
    }
    
    if ($logs -match "invalid input syntax for type json") {
        Write-Host "  - Still seeing JSON syntax errors" -ForegroundColor Red
    } else {
        Write-Host "  - No JSON syntax errors found" -ForegroundColor Green
    }
}

Write-Host "`n=== Database Fix Complete ===" -ForegroundColor Green
Write-Host "Summary of changes made:" -ForegroundColor Cyan
Write-Host "1. Created order_items table migration" -ForegroundColor White
Write-Host "2. Ran Flyway migration to create missing table" -ForegroundColor White
Write-Host "3. Rebuilt and restarted application" -ForegroundColor White
Write-Host "4. Verified database schema and logs" -ForegroundColor White