# Kubernetes PostgreSQL Connection Diagnostics
Write-Host "=== Kubernetes PostgreSQL Connection Diagnostics ===" -ForegroundColor Green

# Step 1: Check current database configuration
Write-Host "`n1. Analyzing current database configuration..." -ForegroundColor Cyan

$yamlPath = "src/main/resources/application.yml"
if (Test-Path $yamlPath) {
    Write-Host "   Current database URL configuration:" -ForegroundColor Yellow
    Select-String -Path $yamlPath -Pattern "url:|host:|localhost" -Context 1 | ForEach-Object {
        Write-Host "     $($_.Line)" -ForegroundColor Gray
    }
}

# Step 2: Look for Kubernetes configuration files
Write-Host "`n2. Searching for Kubernetes configuration files..." -ForegroundColor Cyan

$k8sFiles = @()
$k8sFiles += Get-ChildItem -Recurse -Filter "*.yaml" | Where-Object { $_.Name -match "(deployment|service|configmap|secret)" }
$k8sFiles += Get-ChildItem -Recurse -Filter "*.yml" | Where-Object { $_.Name -match "(deployment|service|configmap|secret)" }
$k8sFiles += Get-ChildItem -Recurse -Filter "Tiltfile*" -ErrorAction SilentlyContinue
$k8sFiles += Get-ChildItem -Recurse -Filter "docker-compose*" -ErrorAction SilentlyContinue

if ($k8sFiles.Count -gt 0) {
    Write-Host "   Found Kubernetes/Docker configuration files:" -ForegroundColor Yellow
    $k8sFiles | ForEach-Object { Write-Host "     $($_.FullName)" -ForegroundColor Gray }
} else {
    Write-Host "   No Kubernetes configuration files found in current directory" -ForegroundColor Yellow
    Write-Host "   Checking parent directories..." -ForegroundColor Gray
    
    $parentK8sFiles = Get-ChildItem -Path ".." -Recurse -Filter "*.yaml" -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "(deployment|service|postgres)" } | Select-Object -First 10
    if ($parentK8sFiles) {
        Write-Host "   Found in parent directories:" -ForegroundColor Yellow
        $parentK8sFiles | ForEach-Object { Write-Host "     $($_.FullName)" -ForegroundColor Gray }
    }
}

# Step 3: Check for Tilt configuration
Write-Host "`n3. Checking for Tilt configuration..." -ForegroundColor Cyan

$tiltFiles = Get-ChildItem -Recurse -Filter "Tiltfile*" -ErrorAction SilentlyContinue
if ($tiltFiles) {
    Write-Host "   Found Tiltfile(s):" -ForegroundColor Yellow
    $tiltFiles | ForEach-Object { 
        Write-Host "     $($_.FullName)" -ForegroundColor Gray
        Write-Host "   Content preview:" -ForegroundColor Yellow
        Get-Content $_.FullName | Select-Object -First 20 | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    }
} else {
    Write-Host "   No Tiltfile found" -ForegroundColor Yellow
}

# Step 4: Check if kubectl is available and get cluster info
Write-Host "`n4. Checking Kubernetes cluster connectivity..." -ForegroundColor Cyan

try {
    $kubectlVersion = kubectl version --client --short 2>$null
    if ($kubectlVersion) {
        Write-Host "   ✅ kubectl available: $kubectlVersion" -ForegroundColor Green
        
        # Check cluster connection
        $clusterInfo = kubectl cluster-info 2>$null
        if ($clusterInfo) {
            Write-Host "   ✅ Connected to Kubernetes cluster" -ForegroundColor Green
            Write-Host "   Cluster info:" -ForegroundColor Yellow
            $clusterInfo | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
        } else {
            Write-Host "   ❌ Not connected to Kubernetes cluster" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "   ❌ kubectl not available or not in PATH" -ForegroundColor Red
}

# Step 5: Look for PostgreSQL services in the cluster
Write-Host "`n5. Searching for PostgreSQL services..." -ForegroundColor Cyan

try {
    $services = kubectl get services --all-namespaces 2>$null | Select-String -Pattern "postgres|database|db"
    if ($services) {
        Write-Host "   Found PostgreSQL-related services:" -ForegroundColor Yellow
        $services | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    } else {
        Write-Host "   No PostgreSQL services found in cluster" -ForegroundColor Yellow
    }
    
    $pods = kubectl get pods --all-namespaces 2>$null | Select-String -Pattern "postgres|database|db"
    if ($pods) {
        Write-Host "   Found PostgreSQL-related pods:" -ForegroundColor Yellow
        $pods | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
    } else {
        Write-Host "   No PostgreSQL pods found in cluster" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   Could not query Kubernetes cluster" -ForegroundColor Yellow
}

# Step 6: Check for environment variable configurations
Write-Host "`n6. Checking for environment variable configurations..." -ForegroundColor Cyan

$envFiles = Get-ChildItem -Recurse -Filter ".env*" -ErrorAction SilentlyContinue
if ($envFiles) {
    Write-Host "   Found environment files:" -ForegroundColor Yellow
    $envFiles | ForEach-Object {
        Write-Host "     $($_.FullName)" -ForegroundColor Gray
        $content = Get-Content $_.FullName | Select-String -Pattern "DB_|DATABASE_|POSTGRES"
        if ($content) {
            Write-Host "   Database-related variables:" -ForegroundColor Yellow
            $content | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
        }
    }
}

# Step 7: Generate recommended fixes
Write-Host "`n=== KUBERNETES NETWORKING DIAGNOSIS ===" -ForegroundColor Yellow

Write-Host "`n🔍 COMMON KUBERNETES DATABASE CONNECTION ISSUES:" -ForegroundColor Cyan

Write-Host "`n1. Service Name Resolution:" -ForegroundColor White
Write-Host "   ❌ Current config uses: localhost:5432" -ForegroundColor Red
Write-Host "   ✅ Should use: <postgres-service-name>:5432" -ForegroundColor Green
Write-Host "   Example: postgres-service:5432 or database:5432" -ForegroundColor Gray

Write-Host "`n2. Namespace Issues:" -ForegroundColor White
Write-Host "   If PostgreSQL is in different namespace:" -ForegroundColor Gray
Write-Host "   ✅ Use: <service-name>.<namespace>.svc.cluster.local:5432" -ForegroundColor Green
Write-Host "   Example: postgres.database.svc.cluster.local:5432" -ForegroundColor Gray

Write-Host "`n3. Environment Variables:" -ForegroundColor White
Write-Host "   ✅ Use environment variables for database host:" -ForegroundColor Green
Write-Host "   DB_HOST=postgres-service" -ForegroundColor Gray
Write-Host "   DB_PORT=5432" -ForegroundColor Gray

Write-Host "`n4. ConfigMap/Secret Configuration:" -ForegroundColor White
Write-Host "   ✅ Database credentials should be in Kubernetes secrets" -ForegroundColor Green
Write-Host "   ✅ Database host should be in ConfigMap" -ForegroundColor Green

# Step 8: Create Kubernetes-ready application.yml
Write-Host "`n7. Creating Kubernetes-ready configuration..." -ForegroundColor Cyan

$k8sYamlPath = "src/main/resources/application-k8s.yml"
$k8sConfig = @"
# Kubernetes configuration
spring:
  datasource:
    url: jdbc:postgresql://`${DB_HOST:`${POSTGRES_SERVICE_NAME:postgres-service}}:`${DB_PORT:5432}/`${DB_NAME:exception_collector_db}?sslmode=`${DB_SSL_MODE:disable}
    username: `${DB_USERNAME:postgres}
    password: `${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: `${JPA_DDL_AUTO:validate}
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: `${JPA_SHOW_SQL:false}

# Application configuration
server:
  port: `${SERVER_PORT:8080}

# Logging
logging:
  level:
    com.arcone.biopro: `${LOG_LEVEL:INFO}
    org.springframework: WARN

# Health checks
management:
  health:
    db:
      enabled: true
  endpoints:
    web:
      exposure:
        include: health,info,metrics
"@

Set-Content -Path $k8sYamlPath -Value $k8sConfig -Encoding UTF8
Write-Host "   ✅ Created $k8sYamlPath with environment variable support" -ForegroundColor Green

# Step 9: Create sample Kubernetes manifests
Write-Host "`n8. Creating sample Kubernetes manifests..." -ForegroundColor Cyan

$deploymentPath = "k8s-deployment-sample.yaml"
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
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: DB_HOST
          value: "postgres-service"  # Replace with your PostgreSQL service name
        - name: DB_PORT
          value: "5432"
        - name: DB_NAME
          value: "exception_collector_db"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
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

Set-Content -Path $deploymentPath -Value $deploymentYaml -Encoding UTF8
Write-Host "   ✅ Created $deploymentPath" -ForegroundColor Green

# Step 10: Provide diagnostic commands
Write-Host "`n=== NEXT STEPS FOR DIAGNOSIS ===" -ForegroundColor Yellow

Write-Host "`n🔧 COMMANDS TO RUN:" -ForegroundColor Cyan

Write-Host "`n1. Find PostgreSQL service name:" -ForegroundColor White
Write-Host "   kubectl get services --all-namespaces | grep postgres" -ForegroundColor Gray

Write-Host "`n2. Check PostgreSQL pod status:" -ForegroundColor White
Write-Host "   kubectl get pods --all-namespaces | grep postgres" -ForegroundColor Gray

Write-Host "`n3. Test connection from within cluster:" -ForegroundColor White
Write-Host "   kubectl run -it --rm debug --image=postgres:13 --restart=Never -- psql -h <postgres-service-name> -U postgres" -ForegroundColor Gray

Write-Host "`n4. Check service endpoints:" -ForegroundColor White
Write-Host "   kubectl get endpoints <postgres-service-name>" -ForegroundColor Gray

Write-Host "`n5. Port forward for testing:" -ForegroundColor White
Write-Host "   kubectl port-forward service/<postgres-service-name> 5432:5432" -ForegroundColor Gray

Write-Host "`n📝 CONFIGURATION FIXES NEEDED:" -ForegroundColor Cyan

Write-Host "`n1. Update application.yml to use service name instead of localhost" -ForegroundColor White
Write-Host "2. Set proper environment variables in Kubernetes deployment" -ForegroundColor White
Write-Host "3. Ensure PostgreSQL service is running and accessible" -ForegroundColor White
Write-Host "4. Check network policies if any are blocking connections" -ForegroundColor White

Write-Host "`n✅ Files created for Kubernetes deployment:" -ForegroundColor Green
Write-Host "   - application-k8s.yml (environment variable based config)" -ForegroundColor Gray
Write-Host "   - k8s-deployment-sample.yaml (sample Kubernetes manifests)" -ForegroundColor Gray

Write-Host "`nRun the diagnostic commands above to identify your PostgreSQL service name!" -ForegroundColor Cyan