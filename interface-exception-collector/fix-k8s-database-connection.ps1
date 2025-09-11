# Fix Kubernetes Database Connection
Write-Host "=== Fixing Kubernetes Database Connection ===" -ForegroundColor Green

Write-Host "Found PostgreSQL services in your cluster:" -ForegroundColor Cyan
Write-Host "  - postgresql-service (in db namespace)" -ForegroundColor Yellow
Write-Host "  - partner-order-postgres (in default namespace)" -ForegroundColor Yellow  
Write-Host "  - postgres (in default namespace)" -ForegroundColor Yellow

# Step 1: Update application.yml to use environment variables
Write-Host "`n1. Updating application.yml for Kubernetes..." -ForegroundColor Cyan

$yamlPath = "src/main/resources/application.yml"
$yamlContent = Get-Content $yamlPath -Raw

# Replace localhost with environment variable
$yamlContent = $yamlContent -replace "localhost:5432", "`${DB_HOST:postgres}:`${DB_PORT:5432}"

Set-Content -Path $yamlPath -Value $yamlContent -Encoding UTF8
Write-Host "   Updated application.yml to use DB_HOST environment variable" -ForegroundColor Green

# Step 2: Create Kubernetes deployment with correct service name
Write-Host "`n2. Creating Kubernetes deployment configuration..." -ForegroundColor Cyan

$deploymentYaml = @"
apiVersion: apps/v1
kind: Deployment
metadata:
  name: interface-exception-collector
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
          value: "postgres"  # Using the postgres service in default namespace
        - name: DB_PORT
          value: "5432"
        - name: DB_NAME
          value: "exception_collector_db"
        - name: DB_USERNAME
          value: "postgres"  # Update with your actual username
        - name: DB_PASSWORD
          value: "password"  # Update with your actual password or use secret
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
---
apiVersion: v1
kind: Service
metadata:
  name: interface-exception-collector-service
spec:
  selector:
    app: interface-exception-collector
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
  type: ClusterIP
"@

Set-Content -Path "k8s-deployment.yaml" -Value $deploymentYaml -Encoding UTF8
Write-Host "   Created k8s-deployment.yaml" -ForegroundColor Green

# Step 3: Create Tilt configuration if needed
Write-Host "`n3. Creating Tilt configuration..." -ForegroundColor Cyan

$tiltfile = @"
# Tiltfile for interface-exception-collector
load('ext://restart_process', 'docker_build_with_restart')

# Build the Docker image
docker_build('interface-exception-collector', '.')

# Deploy to Kubernetes
k8s_yaml('k8s-deployment.yaml')

# Create port forward
k8s_resource('interface-exception-collector', port_forwards=8080)
"@

Set-Content -Path "Tiltfile" -Value $tiltfile -Encoding UTF8
Write-Host "   Created Tiltfile" -ForegroundColor Green

# Step 4: Create test script
Write-Host "`n4. Creating connection test script..." -ForegroundColor Cyan

$testScript = @"
# Test Database Connection in Kubernetes
Write-Host "=== Testing Database Connection ===" -ForegroundColor Green

Write-Host "1. Testing connection to postgres service..." -ForegroundColor Cyan
kubectl run -it --rm debug --image=postgres:13 --restart=Never -- psql -h postgres -U postgres -c "SELECT version();"

Write-Host "`n2. Testing connection to postgresql-service in db namespace..." -ForegroundColor Cyan
kubectl run -it --rm debug --image=postgres:13 --restart=Never -- psql -h postgresql-service.db.svc.cluster.local -U postgres -c "SELECT version();"

Write-Host "`n3. Checking if exception_collector_db database exists..." -ForegroundColor Cyan
kubectl run -it --rm debug --image=postgres:13 --restart=Never -- psql -h postgres -U postgres -c "SELECT datname FROM pg_database WHERE datname='exception_collector_db';"
"@

Set-Content -Path "test-k8s-db-connection.ps1" -Value $testScript -Encoding UTF8
Write-Host "   Created test-k8s-db-connection.ps1" -ForegroundColor Green

# Step 5: Provide specific fix instructions
Write-Host "`n=== KUBERNETES DATABASE CONNECTION FIX ===" -ForegroundColor Yellow

Write-Host "`nISSUE IDENTIFIED:" -ForegroundColor Red
Write-Host "Your application.yml uses localhost:5432 but you're running in Kubernetes" -ForegroundColor White
Write-Host "Available PostgreSQL services:" -ForegroundColor White
Write-Host "  - postgres (default namespace)" -ForegroundColor Gray
Write-Host "  - postgresql-service (db namespace)" -ForegroundColor Gray
Write-Host "  - partner-order-postgres (default namespace)" -ForegroundColor Gray

Write-Host "`nFIX APPLIED:" -ForegroundColor Green
Write-Host "1. Updated application.yml to use DB_HOST environment variable" -ForegroundColor White
Write-Host "2. Created k8s-deployment.yaml with DB_HOST=postgres" -ForegroundColor White
Write-Host "3. Created Tiltfile for easy deployment" -ForegroundColor White

Write-Host "`nNEXT STEPS:" -ForegroundColor Cyan

Write-Host "`n1. Choose the correct PostgreSQL service:" -ForegroundColor White
Write-Host "   Option A: Use 'postgres' service (default namespace)" -ForegroundColor Gray
Write-Host "   Option B: Use 'postgresql-service.db.svc.cluster.local' (db namespace)" -ForegroundColor Gray
Write-Host "   Option C: Use 'partner-order-postgres' (default namespace)" -ForegroundColor Gray

Write-Host "`n2. Test database connection:" -ForegroundColor White
Write-Host "   powershell .\test-k8s-db-connection.ps1" -ForegroundColor Gray

Write-Host "`n3. Deploy with Tilt:" -ForegroundColor White
Write-Host "   tilt up" -ForegroundColor Gray

Write-Host "`n4. Or deploy manually:" -ForegroundColor White
Write-Host "   kubectl apply -f k8s-deployment.yaml" -ForegroundColor Gray

Write-Host "`n5. Check application logs:" -ForegroundColor White
Write-Host "   kubectl logs -f deployment/interface-exception-collector" -ForegroundColor Gray

Write-Host "`nFILES CREATED:" -ForegroundColor Green
Write-Host "  - k8s-deployment.yaml (Kubernetes deployment)" -ForegroundColor Gray
Write-Host "  - Tiltfile (Tilt configuration)" -ForegroundColor Gray
Write-Host "  - test-k8s-db-connection.ps1 (Connection test)" -ForegroundColor Gray
Write-Host "  - application-k8s.yml (Environment-based config)" -ForegroundColor Gray

Write-Host "`nThe connection should work now!" -ForegroundColor Green