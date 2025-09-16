#!/usr/bin/env pwsh

# Direct Database Investigation
Write-Host "=== DIRECT DATABASE INVESTIGATION ===" -ForegroundColor Red

function Write-Log {
    param([string]$Message, [string]$Color = "White")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message" -ForegroundColor $Color
}

$postgresPod = kubectl get pods -l app=postgres -o jsonpath='{.items[0].metadata.name}'
Write-Log "PostgreSQL pod: $postgresPod" "Cyan"

Write-Log "Step 1: Check environment variables" "Magenta"
kubectl exec $postgresPod -- env | findstr POSTGRES

Write-Log "Step 2: Check what users exist" "Magenta"
kubectl exec $postgresPod -- sh -c "ls -la /var/lib/postgresql/data/"

Write-Log "Step 3: Try connecting as different users" "Magenta"
Write-Log "Trying as root..." "Yellow"
kubectl exec $postgresPod -- whoami

Write-Log "Step 4: Check PostgreSQL process" "Magenta"
kubectl exec $postgresPod -- ps aux | findstr postgres

Write-Log "Step 5: Check database files" "Magenta"
kubectl exec $postgresPod -- ls -la /var/lib/postgresql/data/

Write-Log "Step 6: Try connecting without specifying user" "Magenta"
kubectl exec $postgresPod -- psql -l

Write-Log "Step 7: Check ConfigMap for database credentials" "Magenta"
kubectl get configmap postgres-config -o yaml

Write-Log "Step 8: Check application database configuration" "Magenta"
kubectl get configmap app-config -o yaml | findstr -i database

Write-Log "=== INVESTIGATION COMPLETE ===" "Red"