# Kubernetes Network Troubleshooting - Agentic Mode
Write-Host "=== Kubernetes Network Troubleshooting - Agentic Mode ===" -ForegroundColor Green

# Step 1: Gather cluster information
Write-Host "`n1. Gathering cluster information..." -ForegroundColor Cyan

$clusterInfo = kubectl cluster-info 2>$null
if ($clusterInfo) {
    Write-Host "   ✅ Connected to cluster" -ForegroundColor Green
} else {
    Write-Host "   ❌ Not connected to cluster" -ForegroundColor Red
    exit 1
}

# Step 2: Find all PostgreSQL services and pods
Write-Host "`n2. Identifying PostgreSQL services and pods..." -ForegroundColor Cyan

$allServices = kubectl get services --all-namespaces -o wide 2>$null
$pgServices = $allServices | Select-String -Pattern "postgres|database|db"

$allPods = kubectl get pods --all-namespaces -o wide 2>$null
$pgPods = $allPods | Select-String -Pattern "postgres|database|db"

Write-Host "   PostgreSQL Services:" -ForegroundColor Yellow
$pgServices | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }

Write-Host "   PostgreSQL Pods:" -ForegroundColor Yellow
$pgPods | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }

# Step 3: Extract service details for testing
$serviceDetails = @()
$pgServices | ForEach-Object {
    $parts = $_ -split '\s+'
    if ($parts.Count -ge 6) {
        $serviceDetails += @{
            Namespace = $parts[0]
            Name = $parts[1]
            Type = $parts[2]
            ClusterIP = $parts[3]
            Port = $parts[5]
        }
    }
}

# Step 4: Test network connectivity to each PostgreSQL service
Write-Host "`n3. Testing network connectivity to PostgreSQL services..." -ForegroundColor Cyan

foreach ($service in $serviceDetails) {
    Write-Host "   Testing service: $($service.Name) in namespace: $($service.Namespace)" -ForegroundColor Yellow
    
    # Test 1: DNS resolution
    Write-Host "     Testing DNS resolution..." -ForegroundColor Gray
    $dnsTest = kubectl run dns-test-$(Get-Random) --image=busybox --rm -i --restart=Never -- nslookup "$($service.Name).$($service.Namespace).svc.cluster.local" 2>$null
    if ($dnsTest -match "Address") {
        Write-Host "       ✅ DNS resolution successful" -ForegroundColor Green
    } else {
        Write-Host "       ❌ DNS resolution failed" -ForegroundColor Red
    }
    
    # Test 2: Port connectivity
    Write-Host "     Testing port connectivity..." -ForegroundColor Gray
    $portTest = kubectl run port-test-$(Get-Random) --image=busybox --rm -i --restart=Never -- nc -z "$($service.Name).$($service.Namespace).svc.cluster.local" 5432 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "       ✅ Port 5432 is accessible" -ForegroundColor Green
    } else {
        Write-Host "       ❌ Port 5432 is not accessible" -ForegroundColor Red
    }
    
    # Test 3: PostgreSQL connection
    Write-Host "     Testing PostgreSQL connection..." -ForegroundColor Gray
    $pgTest = kubectl run pg-test-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h "$($service.Name).$($service.Namespace).svc.cluster.local" -U postgres -c "SELECT version();" 2>$null
    if ($pgTest -match "PostgreSQL") {
        Write-Host "       ✅ PostgreSQL connection successful" -ForegroundColor Green
        $workingService = $service
    } else {
        Write-Host "       ❌ PostgreSQL connection failed" -ForegroundColor Red
    }
}

# Step 5: Check for network policies
Write-Host "`n4. Checking for network policies..." -ForegroundColor Cyan

$networkPolicies = kubectl get networkpolicies --all-namespaces 2>$null
if ($networkPolicies) {
    Write-Host "   Found network policies:" -ForegroundColor Yellow
    $networkPolicies | ForEach-Host { Write-Host "     $_" -ForegroundColor Gray }
    
    # Check if any policies might be blocking traffic
    $blockingPolicies = kubectl get networkpolicies --all-namespaces -o yaml 2>$null | Select-String -Pattern "postgres|database|5432"
    if ($blockingPolicies) {
        Write-Host "   ⚠️ Network policies may be affecting PostgreSQL traffic" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ✅ No network policies found" -ForegroundColor Green
}

# Step 6: Check service endpoints
Write-Host "`n5. Checking service endpoints..." -ForegroundColor Cyan

foreach ($service in $serviceDetails) {
    Write-Host "   Checking endpoints for $($service.Name)..." -ForegroundColor Yellow
    $endpoints = kubectl get endpoints "$($service.Name)" -n "$($service.Namespace)" 2>$null
    if ($endpoints -match "5432") {
        Write-Host "     ✅ Service has active endpoints" -ForegroundColor Green
    } else {
        Write-Host "     ❌ Service has no active endpoints" -ForegroundColor Red
    }
}

# Step 7: Test from a pod in the same namespace as the application
Write-Host "`n6. Testing from application namespace..." -ForegroundColor Cyan

# Find interface-exception-collector pods
$appPods = kubectl get pods --all-namespaces | Select-String -Pattern "interface-exception-collector"
if ($appPods) {
    $appPodInfo = ($appPods[0] -split '\s+')
    $appNamespace = $appPodInfo[0]
    $appPodName = $appPodInfo[1]
    
    Write-Host "   Found application pod: $appPodName in namespace: $appNamespace" -ForegroundColor Yellow
    
    # Test connectivity from the application pod
    foreach ($service in $serviceDetails) {
        Write-Host "     Testing from app pod to $($service.Name)..." -ForegroundColor Gray
        $appTest = kubectl exec -n $appNamespace $appPodName -- nc -z "$($service.Name).$($service.Namespace).svc.cluster.local" 5432 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "       ✅ Connection successful from app pod" -ForegroundColor Green
        } else {
            Write-Host "       ❌ Connection failed from app pod" -ForegroundColor Red
        }
    }
} else {
    Write-Host "   No interface-exception-collector pods found" -ForegroundColor Yellow
}

# Step 8: Create a working configuration based on findings
Write-Host "`n7. Creating working configuration..." -ForegroundColor Cyan

if ($workingService) {
    Write-Host "   ✅ Found working PostgreSQL service: $($workingService.Name)" -ForegroundColor Green
    
    # Update application.yml with working service
    $yamlPath = "src/main/resources/application.yml"
    $yamlContent = Get-Content $yamlPath -Raw
    
    # Create the correct connection string
    $correctHost = "$($workingService.Name).$($workingService.Namespace).svc.cluster.local"
    if ($workingService.Namespace -eq "default") {
        $correctHost = $workingService.Name
    }
    
    # Update the YAML
    $yamlContent = $yamlContent -replace '\$\{DB_HOST:[^}]+\}', "`${DB_HOST:$correctHost}"
    Set-Content -Path $yamlPath -Value $yamlContent -Encoding UTF8
    
    Write-Host "   ✅ Updated application.yml with working service: $correctHost" -ForegroundColor Green
    
    # Create updated deployment
    $deploymentYaml = @"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: interface-exception-collector
  namespace: $appNamespace
  labels:
    app: interface-exception-collector
spec:
  replicas: 1
  selector:
    matchLabels:
      app: interface-exception-collector
  template:
    metadata:
      labels:
        app: interface-exception-collector
    spec:
      containers:
      - name: interface-exception-collector
        image: interface-exception-collector:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_HOST
          value: "$correctHost"
        - name: DB_PORT
          value: "5432"
        - name: DB_NAME
          value: "exception_collector_db"
        - name: DB_USERNAME
          value: "postgres"
        - name: DB_PASSWORD
          value: "postgres"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
"@
    
    Set-Content -Path "k8s-deployment-fixed.yaml" -Value $deploymentYaml -Encoding UTF8
    Write-Host "   ✅ Created k8s-deployment-fixed.yaml with correct service" -ForegroundColor Green
    
} else {
    Write-Host "   ❌ No working PostgreSQL service found" -ForegroundColor Red
}

# Step 9: Check for database existence
Write-Host "`n8. Checking database existence..." -ForegroundColor Cyan

if ($workingService) {
    $dbCheck = kubectl run db-check-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h "$correctHost" -U postgres -c "SELECT datname FROM pg_database WHERE datname='exception_collector_db';" 2>$null
    
    if ($dbCheck -match "exception_collector_db") {
        Write-Host "   ✅ Database 'exception_collector_db' exists" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Database 'exception_collector_db' does not exist" -ForegroundColor Red
        Write-Host "   Creating database..." -ForegroundColor Yellow
        
        $createDb = kubectl run db-create-$(Get-Random) --image=postgres:13 --rm -i --restart=Never --env="PGPASSWORD=postgres" -- psql -h "$correctHost" -U postgres -c "CREATE DATABASE exception_collector_db;" 2>$null
        
        if ($createDb -match "CREATE DATABASE") {
            Write-Host "   ✅ Database created successfully" -ForegroundColor Green
        } else {
            Write-Host "   ❌ Failed to create database" -ForegroundColor Red
        }
    }
}

# Step 10: Apply the fix
Write-Host "`n9. Applying the fix..." -ForegroundColor Cyan

if (Test-Path "k8s-deployment-fixed.yaml") {
    Write-Host "   Applying fixed deployment..." -ForegroundColor Yellow
    kubectl apply -f k8s-deployment-fixed.yaml 2>$null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ Deployment applied successfully" -ForegroundColor Green
        
        # Wait for rollout
        Write-Host "   Waiting for rollout to complete..." -ForegroundColor Gray
        kubectl rollout status deployment/interface-exception-collector -n $appNamespace --timeout=120s 2>$null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✅ Rollout completed successfully" -ForegroundColor Green
        } else {
            Write-Host "   ⚠️ Rollout may still be in progress" -ForegroundColor Yellow
        }
    } else {
        Write-Host "   ❌ Failed to apply deployment" -ForegroundColor Red
    }
}

# Step 11: Final verification
Write-Host "`n10. Final verification..." -ForegroundColor Cyan

Start-Sleep -Seconds 10

$newPods = kubectl get pods --all-namespaces | Select-String -Pattern "interface-exception-collector"
if ($newPods) {
    $newPodInfo = ($newPods[0] -split '\s+')
    $newPodName = $newPodInfo[1]
    $newNamespace = $newPodInfo[0]
    
    Write-Host "   Checking new pod logs..." -ForegroundColor Yellow
    $logs = kubectl logs $newPodName -n $newNamespace --tail=20 2>$null
    
    if ($logs -match "Started.*Application") {
        Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
    } elseif ($logs -match "Connection.*refused") {
        Write-Host "   ❌ Still has connection issues" -ForegroundColor Red
    } else {
        Write-Host "   ⚠️ Application status unclear, check logs manually" -ForegroundColor Yellow
    }
}

# Summary
Write-Host "`n=== TROUBLESHOOTING SUMMARY ===" -ForegroundColor Yellow

if ($workingService) {
    Write-Host "✅ NETWORK ISSUE RESOLVED" -ForegroundColor Green
    Write-Host "Working PostgreSQL service: $($workingService.Name)" -ForegroundColor White
    Write-Host "Namespace: $($workingService.Namespace)" -ForegroundColor White
    Write-Host "Connection string updated in application.yml" -ForegroundColor White
    Write-Host "Deployment updated and applied" -ForegroundColor White
} else {
    Write-Host "❌ NETWORK ISSUE NOT RESOLVED" -ForegroundColor Red
    Write-Host "No working PostgreSQL service found" -ForegroundColor White
    Write-Host "Check PostgreSQL pod status and configuration" -ForegroundColor White
}

Write-Host "`nFiles created:" -ForegroundColor Cyan
Write-Host "- k8s-deployment-fixed.yaml (if working service found)" -ForegroundColor Gray
Write-Host "- Updated application.yml (if working service found)" -ForegroundColor Gray

Write-Host "`nTroubleshooting complete!" -ForegroundColor Green