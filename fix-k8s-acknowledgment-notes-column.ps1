#!/usr/bin/env pwsh

Write-Host "=== Fixing acknowledgment_notes Column Issue in Kubernetes ===" -ForegroundColor Green

# Check if kubectl is available
Write-Host "Checking kubectl availability..." -ForegroundColor Yellow
try {
    kubectl version --client --short | Out-Null
    Write-Host "kubectl is available" -ForegroundColor Green
} catch {
    Write-Host "kubectl is not available. Please install kubectl first." -ForegroundColor Red
    exit 1
}

# Find PostgreSQL pods
Write-Host "Finding PostgreSQL pods..." -ForegroundColor Yellow
$postgresPods = kubectl get pods --all-namespaces -o json | ConvertFrom-Json | 
    ForEach-Object { $_.items } | 
    Where-Object { $_.metadata.name -match "postgres" -and $_.status.phase -eq "Running" }

if ($postgresPods.Count -eq 0) {
    Write-Host "No running PostgreSQL pods found" -ForegroundColor Red
    exit 1
}

Write-Host "Found PostgreSQL pods:" -ForegroundColor Cyan
foreach ($pod in $postgresPods) {
    Write-Host "  - $($pod.metadata.name) in namespace $($pod.metadata.namespace)" -ForegroundColor Cyan
}

# Try to find the main database pod (likely in 'db' or 'default' namespace)
$targetPod = $postgresPods | Where-Object { 
    $_.metadata.namespace -eq "db" -or 
    ($_.metadata.namespace -eq "default" -and $_.metadata.name -match "postgresql-deployment")
} | Select-Object -First 1

if (-not $targetPod) {
    # Fallback to any postgres pod in default namespace
    $targetPod = $postgresPods | Where-Object { $_.metadata.namespace -eq "default" } | Select-Object -First 1
}

if (-not $targetPod) {
    Write-Host "Could not determine target PostgreSQL pod" -ForegroundColor Red
    exit 1
}

$podName = $targetPod.metadata.name
$namespace = $targetPod.metadata.namespace

Write-Host "Using PostgreSQL pod: $podName in namespace: $namespace" -ForegroundColor Green

# Test database connection first
Write-Host "Testing database connection..." -ForegroundColor Yellow
try {
    $testResult = kubectl exec -n $namespace $podName -- psql -U biopro -d biopro -c "\dt" 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to connect to biopro database: $testResult" -ForegroundColor Red
        exit 1
    }
    Write-Host "Database connection successful" -ForegroundColor Green
} catch {
    Write-Host "Database connection failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Execute the SQL fix
Write-Host "Adding missing acknowledgment_notes column..." -ForegroundColor Yellow
try {
    $sqlCommand = @"
DO `$`$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'interface_exceptions' 
        AND column_name = 'acknowledgment_notes'
    ) THEN
        ALTER TABLE interface_exceptions 
        ADD COLUMN acknowledgment_notes VARCHAR(1000);
        
        RAISE NOTICE 'Added acknowledgment_notes column to interface_exceptions table';
    ELSE
        RAISE NOTICE 'acknowledgment_notes column already exists';
    END IF;
END
`$`$;
"@

    $result = kubectl exec -n $namespace $podName -- psql -U biopro -d biopro -c $sqlCommand 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Successfully executed database fix" -ForegroundColor Green
        Write-Host "Result: $result" -ForegroundColor Cyan
    } else {
        Write-Host "Failed to execute database fix: $result" -ForegroundColor Red
        
        # Check if table exists
        Write-Host "Checking if interface_exceptions table exists..." -ForegroundColor Yellow
        $tableCheck = kubectl exec -n $namespace $podName -- psql -U biopro -d biopro -c "\dt interface_exceptions" 2>&1
        Write-Host "Table check result: $tableCheck" -ForegroundColor Cyan
        
        exit 1
    }
    
    # Verify the column was added
    Write-Host "Verifying column was added..." -ForegroundColor Yellow
    $verifyCommand = "SELECT column_name, data_type, character_maximum_length FROM information_schema.columns WHERE table_name = 'interface_exceptions' AND column_name = 'acknowledgment_notes';"
    $verifyResult = kubectl exec -n $namespace $podName -- psql -U biopro -d biopro -c $verifyCommand 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Verification result:" -ForegroundColor Cyan
        Write-Host $verifyResult -ForegroundColor Cyan
    } else {
        Write-Host "Verification failed: $verifyResult" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "Failed to execute database fix: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "=== Database Fix Complete ===" -ForegroundColor Green
Write-Host "The acknowledgment_notes column has been added to the interface_exceptions table." -ForegroundColor Cyan
Write-Host "You can now restart your application." -ForegroundColor Cyan