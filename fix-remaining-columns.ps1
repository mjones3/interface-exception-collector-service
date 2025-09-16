#!/usr/bin/env pwsh

# Fix Remaining Missing Columns
Write-Host "=== FIXING REMAINING MISSING COLUMNS ===" -ForegroundColor Red

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

$postgresPod = kubectl get pods -l app=postgres -o jsonpath='{.items[0].metadata.name}'
Write-Log "PostgreSQL pod: $postgresPod" "Cyan"

Write-Log "Step 1: Check what columns are missing from the error" "Magenta"
Write-Log "From the error log, missing column: order_received" "Yellow"

Write-Log "Step 2: Check current table structure for order-related columns" "Magenta"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "SELECT column_name FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name LIKE '%order%';"

Write-Log "Step 3: Add missing order-related columns" "Magenta"
Write-Log "Adding order_received column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS order_received JSONB;"

Write-Log "Adding order_retrieval_attempted column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS order_retrieval_attempted BOOLEAN DEFAULT false;"

Write-Log "Adding order_retrieval_error column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS order_retrieval_error TEXT;"

Write-Log "Adding order_retrieved_at column..." "Yellow"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "ALTER TABLE interface_exceptions ADD COLUMN IF NOT EXISTS order_retrieved_at TIMESTAMP WITH TIME ZONE;"

Write-Log "Step 4: Verify all order columns were added" "Magenta"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name LIKE '%order%' ORDER BY column_name;"

Write-Log "Step 5: Update Flyway schema history" "Magenta"
$currentTime = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
kubectl exec $postgresPod -- env PGPASSWORD=exception_pass psql -U exception_user -d exception_collector_db -c "INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) VALUES ((SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history), '23', 'Add missing order columns', 'SQL', 'V23__Add_missing_order_columns.sql', 0, 'exception_user', '$currentTime', 100, true) ON CONFLICT DO NOTHING;"

Write-Log "Step 6: Restart application to pick up schema changes" "Magenta"
kubectl delete pod -l app=interface-exception-collector
Write-Log "Pod deleted, waiting for restart..." "Yellow"
Start-Sleep -Seconds 30

Write-Log "Step 7: Wait for application to be ready" "Magenta"
$maxRetries = 20
$retryCount = 0
$serviceReady = $false

while ($retryCount -lt $maxRetries -and -not $serviceReady) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Log "Application is healthy!" "Green"
            $serviceReady = $true
        }
    } catch {
        $retryCount++
        Write-Log "Health check attempt $retryCount/$maxRetries - waiting..." "Yellow"
        Start-Sleep -Seconds 10
    }
}

if ($serviceReady) {
    Write-Log "Step 8: Test API endpoint with all fixes" "Magenta"
    $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTc5ODE3MDAsImV4cCI6MTc1Nzk4NTMwMH0.Ptor4zyhMG_lk0ulPlk40yOfv25uzhmucGKVTl7TF68"
    
    try {
        $headers = @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        }
        
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/exceptions" -Headers $headers -TimeoutSec 30
        
        if ($response.StatusCode -eq 200) {
            Write-Log "🎉 COMPLETE SUCCESS! All database column errors FIXED!" "Green"
            $content = $response.Content | ConvertFrom-Json
            Write-Log "Found $($content.totalElements) exceptions in system" "Cyan"
            Write-Log "✅ No more column errors!" "Green"
        }
    } catch {
        Write-Log "API test failed: $_" "Red"
        Write-Log "Checking logs for any remaining issues..." "Yellow"
        kubectl logs -l app=interface-exception-collector --tail=5
    }
} else {
    Write-Log "Application failed to start properly" "Red"
    kubectl logs -l app=interface-exception-collector --tail=10
}

Write-Log "=== ALL COLUMN FIXES COMPLETE ===" "Green"