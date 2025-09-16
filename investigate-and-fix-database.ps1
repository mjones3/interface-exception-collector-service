#!/usr/bin/env pwsh

# Investigate and Fix Database Script
Write-Host "=== INVESTIGATING DATABASE STATE ===" -ForegroundColor Red

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

Write-Log "Step 1: Finding PostgreSQL pod" "Magenta"
$postgresPod = kubectl get pods -l app=postgres -o jsonpath='{.items[0].metadata.name}'
Write-Log "PostgreSQL pod: $postgresPod" "Cyan"

Write-Log "Step 2: Checking database connection and users" "Magenta"
kubectl exec -it $postgresPod -- sh -c "psql --version"

Write-Log "Step 3: Finding correct database and user" "Magenta"
kubectl exec -it $postgresPod -- sh -c "psql -l"

Write-Log "Step 4: Checking environment variables in postgres pod" "Magenta"
kubectl exec -it $postgresPod -- env | grep -E "(POSTGRES|DB)"

Write-Log "Step 5: Attempting to connect with different users" "Magenta"
Write-Log "Trying with postgres user..." "Yellow"
kubectl exec -it $postgresPod -- sh -c "psql -U postgres -c '\l'"

Write-Log "Step 6: Checking actual table structure" "Magenta"
Write-Log "Attempting to describe interface_exceptions table..." "Yellow"
kubectl exec -it $postgresPod -- sh -c "psql -U postgres -d postgres -c '\d interface_exceptions'"

Write-Log "Step 7: Checking if database exists" "Magenta"
kubectl exec -it $postgresPod -- sh -c "psql -U postgres -c \"SELECT datname FROM pg_database WHERE datname LIKE '%interface%' OR datname LIKE '%exception%';\""

Write-Log "Step 8: Checking Flyway schema history" "Magenta"
kubectl exec -it $postgresPod -- sh -c "psql -U postgres -c \"SELECT schemaname, tablename FROM pg_tables WHERE tablename LIKE '%flyway%' OR tablename LIKE '%schema%';\""

Write-Log "Step 9: Finding the correct database name" "Magenta"
kubectl exec -it $postgresPod -- sh -c "psql -U postgres -c \"SELECT datname FROM pg_database;\""

Write-Log "Step 10: Checking application configuration" "Magenta"
Write-Log "Checking application.yml for database configuration..." "Yellow"
if (Test-Path "interface-exception-collector/src/main/resources/application.yml") {
    Get-Content "interface-exception-collector/src/main/resources/application.yml" | Select-String -Pattern "url:|database:|schema:" -A 2 -B 2
}

Write-Log "Step 11: Checking ConfigMap for database settings" "Magenta"
kubectl get configmap app-config -o yaml | Select-String -Pattern "DATABASE|POSTGRES|URL" -A 2 -B 2

Write-Log "Step 12: Attempting direct column check" "Magenta"
Write-Log "Checking for acknowledgment_notes column in all databases..." "Yellow"

# Try different database names
$databases = @("postgres", "interface_exceptions", "exception_collector_db", "interface_exception_collector")
foreach ($db in $databases) {
    Write-Log "Checking database: $db" "Cyan"
    kubectl exec -it $postgresPod -- sh -c "psql -U postgres -d $db -c \"SELECT column_name FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name = 'acknowledgment_notes';\" 2>/dev/null || echo 'Database $db not accessible'"
}

Write-Log "=== INVESTIGATION COMPLETE ===" "Red"
Write-Log "Now proceeding with direct database fix..." "Yellow"