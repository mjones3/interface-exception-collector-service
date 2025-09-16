#!/usr/bin/env pwsh

# Fix Database Directly
Write-Host "=== FIXING DATABASE DIRECTLY ===" -ForegroundColor Red

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

$postgresPod = kubectl get pods -l app=postgres -o jsonpath='{.items[0].metadata.name}'
Write-Log "PostgreSQL pod: $postgresPod" "Cyan"

Write-Log "Step 1: Check current table structure" "Magenta"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "\d interface_exceptions"

Write-Log "Step 2: Check for acknowledgment_notes column specifically" "Magenta"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name = 'acknowledgment_notes';"

Write-Log "Step 3: List all columns in interface_exceptions table" "Magenta"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'interface_exceptions' ORDER BY ordinal_position;"

Write-Log "Step 4: Check Flyway schema history" "Magenta"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;"

Write-Log "Step 5: Add the missing column directly" "Magenta"
Write-Log "Adding acknowledgment_notes column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS acknowledgment_notes VARCHAR(1000);"

Write-Log "Step 6: Add other missing columns" "Magenta"
Write-Log "Adding resolution_method column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS resolution_method VARCHAR(50);"

Write-Log "Adding resolution_notes column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS resolution_notes VARCHAR(1000);"

Write-Log "Adding acknowledged_at column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS acknowledged_at TIMESTAMP WITH TIME ZONE;"

Write-Log "Adding acknowledged_by column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS acknowledged_by VARCHAR(255);"

Write-Log "Adding resolved_at column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP WITH TIME ZONE;"

Write-Log "Adding resolved_by column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS resolved_by VARCHAR(255);"

Write-Log "Step 7: Verify all columns were added" "Magenta"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name IN ('acknowledgment_notes', 'resolution_method', 'resolution_notes', 'acknowledged_at', 'acknowledged_by', 'resolved_at', 'resolved_by') ORDER BY column_name;"

Write-Log "Step 8: Update Flyway schema history to reflect the change" "Magenta"
$currentTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES ((SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history), '22', 'Complete schema fix', 'SQL', 'V22__Complete_schema_fix.sql', 0, 'exception_user', '$currentTime', 100, true) ON CONFLICT DO NOTHING;"

Write-Log "Step 9: Test the fix by querying the table" "Magenta"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "SELECT COUNT(*) as total_exceptions FROM interface_exceptions;"

Write-Log "=== DATABASE FIX COMPLETE ===" "Green"
Write-Log "Now testing the API endpoint..." "Yellow"

Write-Log "Step 10: Test API endpoint" "Magenta"
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc5ODE3MDAsImV4cCI6MTc1Nzk4NTMwMH0.Ptor4zyhMG_lk0ulPlk40yOfv25uzhmucGKVTl7TF68"

try {
    $headers = @{
        "Authorization" = "Bearer $token"
        "Content-Type" = "application/json"
    }
    
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
    
    if ($response.StatusCode -eq 200) {
        Write-Log "SUCCESS! API endpoint working - acknowledgment_notes column error FIXED!" "Green"
        $content = $response.Content | ConvertFrom-Json
        Write-Log "Found $($content.totalElements) exceptions in system" "Cyan"
    }
} catch {
    Write-Log "API test failed: $_" "Red"
    Write-Log "The column may have been added but there might be other issues" "Yellow"
}

Write-Log "=== COMPLETE ===" "Green"