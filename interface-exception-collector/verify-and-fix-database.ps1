# Verify Database Connection and Create Database if Needed
Write-Host "=== Verifying Database Connection and Setup ===" -ForegroundColor Green

# Step 1: Wait for pod to be ready
Write-Host "`n1. Waiting for application pod to be ready..." -ForegroundColor Cyan

Start-Sleep -Seconds 15

$appPods = kubectl get pods -n api | Select-String -Pattern "interface-exception-collector.*Running"
if ($appPods) {
    $podName = ($appPods[0] -split '\s+')[0]
    Write-Host "   Found running pod: $podName" -ForegroundColor Green
} else {
    Write-Host "   No running pods found, checking all pods..." -ForegroundColor Yellow
    $allAppPods = kubectl get pods -n api | Select-String -Pattern "interface-exception-collector"
    $allAppPods | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    
    if ($allAppPods) {
        $podName = ($allAppPods[0] -split '\s+')[0]
        Write-Host "   Using pod: $podName" -ForegroundColor Yellow
    } else {
        Write-Host "   ❌ No application pods found" -ForegroundColor Red
        exit 1
    }
}

# Step 2: Check application logs
Write-Host "`n2. Checking application logs..." -ForegroundColor Cyan

$logs = kubectl logs $podName -n api --tail=50 2>$null
if ($logs -match "Started.*Application") {
    Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
} elseif ($logs -match "Connection.*refused|Failed to initialize pool") {
    Write-Host "   ❌ Still has database connection issues" -ForegroundColor Red
    Write-Host "   Recent logs:" -ForegroundColor Yellow
    $logs | Select-Object -Last 10 | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
} else {
    Write-Host "   ⚠️ Application status unclear" -ForegroundColor Yellow
    Write-Host "   Recent logs:" -ForegroundColor Yellow
    $logs | Select-Object -Last 10 | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
}

# Step 3: Test database connection directly
Write-Host "`n3. Testing database connection directly..." -ForegroundColor Cyan

$dbHost = "postgresql-service.db.svc.cluster.local"
Write-Host "   Testing connection to: $dbHost" -ForegroundColor Yellow

# Test basic connectivity
$connTest = kubectl run conn-test-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h $dbHost -U postgres -c "SELECT version();" 2>$null

if ($connTest -match "PostgreSQL") {
    Write-Host "   ✅ Database connection successful" -ForegroundColor Green
    
    # Check if database exists
    Write-Host "   Checking if exception_collector_db exists..." -ForegroundColor Gray
    $dbExists = kubectl run db-exists-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h $dbHost -U postgres -c "SELECT 1 FROM pg_database WHERE datname='exception_collector_db';" 2>$null
    
    if ($dbExists -match "1") {
        Write-Host "   ✅ Database exception_collector_db exists" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Database exception_collector_db does not exist" -ForegroundColor Red
        Write-Host "   Creating database..." -ForegroundColor Yellow
        
        $createDb = kubectl run create-db-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h $dbHost -U postgres -c "CREATE DATABASE exception_collector_db;" 2>$null
        
        if ($createDb -match "CREATE DATABASE") {
            Write-Host "   ✅ Database created successfully" -ForegroundColor Green
        } else {
            Write-Host "   ❌ Failed to create database" -ForegroundColor Red
            Write-Host "   Error: $createDb" -ForegroundColor Red
        }
    }
    
    # Check database user
    Write-Host "   Checking database user..." -ForegroundColor Gray
    $userExists = kubectl run user-check-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h $dbHost -U postgres -c "SELECT 1 FROM pg_user WHERE usename='exception_user';" 2>$null
    
    if ($userExists -match "1") {
        Write-Host "   ✅ User exception_user exists" -ForegroundColor Green
    } else {
        Write-Host "   ❌ User exception_user does not exist" -ForegroundColor Red
        Write-Host "   Creating user..." -ForegroundColor Yellow
        
        $createUser = kubectl run create-user-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h $dbHost -U postgres -c "CREATE USER exception_user WITH PASSWORD 'exception_pass';" 2>$null
        
        if ($createUser -match "CREATE ROLE") {
            Write-Host "   ✅ User created successfully" -ForegroundColor Green
            
            # Grant privileges
            $grantPrivs = kubectl run grant-privs-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h $dbHost -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE exception_collector_db TO exception_user;" 2>$null
            
            if ($grantPrivs -match "GRANT") {
                Write-Host "   ✅ Privileges granted successfully" -ForegroundColor Green
            }
        } else {
            Write-Host "   ❌ Failed to create user" -ForegroundColor Red
        }
    }
    
} else {
    Write-Host "   ❌ Database connection failed" -ForegroundColor Red
    Write-Host "   Error: $connTest" -ForegroundColor Red
}

# Step 4: Restart application pod to pick up changes
Write-Host "`n4. Restarting application pod..." -ForegroundColor Cyan

kubectl delete pod $podName -n api 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Pod deleted, waiting for new pod..." -ForegroundColor Green
    
    Start-Sleep -Seconds 20
    
    # Check new pod status
    $newPods = kubectl get pods -n api | Select-String -Pattern "interface-exception-collector"
    if ($newPods) {
        $newPodName = ($newPods[0] -split '\s+')[0]
        Write-Host "   New pod: $newPodName" -ForegroundColor Yellow
        
        # Wait a bit more for startup
        Start-Sleep -Seconds 30
        
        # Check logs of new pod
        $newLogs = kubectl logs $newPodName -n api --tail=20 2>$null
        if ($newLogs -match "Started.*Application") {
            Write-Host "   ✅ Application started successfully in new pod!" -ForegroundColor Green
        } elseif ($newLogs -match "Connection.*refused|Failed to initialize pool") {
            Write-Host "   ❌ Still has connection issues in new pod" -ForegroundColor Red
            Write-Host "   Recent logs:" -ForegroundColor Yellow
            $newLogs | Select-Object -Last 5 | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
        } else {
            Write-Host "   ⚠️ New pod status unclear" -ForegroundColor Yellow
            Write-Host "   Recent logs:" -ForegroundColor Yellow
            $newLogs | Select-Object -Last 5 | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
        }
    }
} else {
    Write-Host "   ❌ Failed to delete pod" -ForegroundColor Red
}

# Step 5: Final connectivity test from application pod
Write-Host "`n5. Final connectivity test from application pod..." -ForegroundColor Cyan

$finalPods = kubectl get pods -n api | Select-String -Pattern "interface-exception-collector.*Running"
if ($finalPods) {
    $finalPodName = ($finalPods[0] -split '\s+')[0]
    Write-Host "   Testing from pod: $finalPodName" -ForegroundColor Yellow
    
    # Test network connectivity
    $finalConnTest = kubectl exec -n api $finalPodName -- nc -z postgresql-service.db.svc.cluster.local 5432 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ Network connectivity successful from app pod" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Network connectivity failed from app pod" -ForegroundColor Red
    }
    
    # Test PostgreSQL connectivity
    $finalPgTest = kubectl exec -n api $finalPodName -- timeout 10 bash -c "echo 'SELECT 1;' | nc postgresql-service.db.svc.cluster.local 5432" 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ PostgreSQL port responding from app pod" -ForegroundColor Green
    } else {
        Write-Host "   ❌ PostgreSQL port not responding from app pod" -ForegroundColor Red
    }
}

# Summary
Write-Host "`n=== DATABASE SETUP SUMMARY ===" -ForegroundColor Yellow

Write-Host "Database Host: postgresql-service.db.svc.cluster.local" -ForegroundColor White
Write-Host "Database Name: exception_collector_db" -ForegroundColor White
Write-Host "Database User: exception_user" -ForegroundColor White

$finalLogs = kubectl logs $finalPodName -n api --tail=10 2>$null
if ($finalLogs -match "Started.*Application") {
    Write-Host "`n🎉 SUCCESS: Application is now running with database connection!" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ Application may still be starting or has issues" -ForegroundColor Yellow
    Write-Host "Check logs with: kubectl logs $finalPodName -n api" -ForegroundColor Cyan
}

Write-Host "`nDatabase setup and verification complete!" -ForegroundColor Green