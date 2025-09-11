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
$k8sFiles += Get-ChildItem -Recurse -Filter "*.yaml" -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "(deployment|service|configmap|secret)" }
$k8sFiles += Get-ChildItem -Recurse -Filter "*.yml" -ErrorAction SilentlyContinue | Where-Object { $_.Name -match "(deployment|service|configmap|secret)" }
$k8sFiles += Get-ChildItem -Recurse -Filter "Tiltfile*" -ErrorAction SilentlyContinue
$k8sFiles += Get-ChildItem -Recurse -Filter "docker-compose*" -ErrorAction SilentlyContinue

if ($k8sFiles.Count -gt 0) {
    Write-Host "   Found Kubernetes/Docker configuration files:" -ForegroundColor Yellow
    $k8sFiles | ForEach-Object { Write-Host "     $($_.FullName)" -ForegroundColor Gray }
} else {
    Write-Host "   No Kubernetes configuration files found in current directory" -ForegroundColor Yellow
}

# Step 3: Check if kubectl is available
Write-Host "`n3. Checking Kubernetes cluster connectivity..." -ForegroundColor Cyan

try {
    $kubectlCheck = kubectl version --client --short 2>$null
    if ($kubectlCheck) {
        Write-Host "   kubectl available: $kubectlCheck" -ForegroundColor Green
        
        # Check cluster connection
        $clusterInfo = kubectl cluster-info 2>$null
        if ($clusterInfo) {
            Write-Host "   Connected to Kubernetes cluster" -ForegroundColor Green
        } else {
            Write-Host "   Not connected to Kubernetes cluster" -ForegroundColor Red
        }
    }
} catch {
    Write-Host "   kubectl not available" -ForegroundColor Red
}

# Step 4: Look for PostgreSQL services
Write-Host "`n4. Searching for PostgreSQL services..." -ForegroundColor Cyan

try {
    $services = kubectl get services --all-namespaces 2>$null
    if ($services) {
        $pgServices = $services | Select-String -Pattern "postgres|database|db"
        if ($pgServices) {
            Write-Host "   Found PostgreSQL-related services:" -ForegroundColor Yellow
            $pgServices | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
        } else {
            Write-Host "   No PostgreSQL services found" -ForegroundColor Yellow
        }
    }
    
    $pods = kubectl get pods --all-namespaces 2>$null
    if ($pods) {
        $pgPods = $pods | Select-String -Pattern "postgres|database|db"
        if ($pgPods) {
            Write-Host "   Found PostgreSQL-related pods:" -ForegroundColor Yellow
            $pgPods | ForEach-Object { Write-Host "     $_" -ForegroundColor Gray }
        } else {
            Write-Host "   No PostgreSQL pods found" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "   Could not query Kubernetes cluster" -ForegroundColor Yellow
}

# Step 5: Create Kubernetes-ready configuration
Write-Host "`n5. Creating Kubernetes-ready configuration..." -ForegroundColor Cyan

$k8sYamlPath = "src/main/resources/application-k8s.yml"
$k8sConfig = @"
# Kubernetes configuration
spring:
  datasource:
    url: jdbc:postgresql://`${DB_HOST:postgres-service}:`${DB_PORT:5432}/`${DB_NAME:exception_collector_db}?sslmode=`${DB_SSL_MODE:disable}
    username: `${DB_USERNAME:postgres}
    password: `${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: `${JPA_DDL_AUTO:validate}
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    show-sql: `${JPA_SHOW_SQL:false}

server:
  port: `${SERVER_PORT:8080}

logging:
  level:
    com.arcone.biopro: `${LOG_LEVEL:INFO}
    org.springframework: WARN

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
Write-Host "   Created $k8sYamlPath with environment variable support" -ForegroundColor Green

# Step 6: Provide diagnostic summary
Write-Host "`n=== DIAGNOSIS SUMMARY ===" -ForegroundColor Yellow

Write-Host "`nCOMMON KUBERNETES DATABASE CONNECTION ISSUES:" -ForegroundColor Cyan

Write-Host "`n1. Service Name Resolution:" -ForegroundColor White
Write-Host "   Current config uses: localhost:5432" -ForegroundColor Red
Write-Host "   Should use: postgres-service:5432 or similar" -ForegroundColor Green

Write-Host "`n2. Environment Variables:" -ForegroundColor White
Write-Host "   Use DB_HOST environment variable for database host" -ForegroundColor Green
Write-Host "   Example: DB_HOST=postgres-service" -ForegroundColor Gray

Write-Host "`n3. Namespace Issues:" -ForegroundColor White
Write-Host "   If PostgreSQL is in different namespace:" -ForegroundColor Gray
Write-Host "   Use: service-name.namespace.svc.cluster.local:5432" -ForegroundColor Green

Write-Host "`nNEXT STEPS:" -ForegroundColor Cyan

Write-Host "`n1. Find your PostgreSQL service name:" -ForegroundColor White
Write-Host "   kubectl get services --all-namespaces | grep postgres" -ForegroundColor Gray

Write-Host "`n2. Check PostgreSQL pod status:" -ForegroundColor White
Write-Host "   kubectl get pods --all-namespaces | grep postgres" -ForegroundColor Gray

Write-Host "`n3. Test connection from within cluster:" -ForegroundColor White
Write-Host "   kubectl run -it --rm debug --image=postgres:13 --restart=Never -- psql -h [service-name] -U postgres" -ForegroundColor Gray

Write-Host "`n4. Update your application configuration:" -ForegroundColor White
Write-Host "   - Use application-k8s.yml profile" -ForegroundColor Gray
Write-Host "   - Set DB_HOST environment variable to PostgreSQL service name" -ForegroundColor Gray
Write-Host "   - Ensure credentials are in Kubernetes secrets" -ForegroundColor Gray

Write-Host "`nFiles created:" -ForegroundColor Green
Write-Host "   - application-k8s.yml (Kubernetes configuration)" -ForegroundColor Gray

Write-Host "`nRun the kubectl commands above to identify your PostgreSQL service!" -ForegroundColor Cyan