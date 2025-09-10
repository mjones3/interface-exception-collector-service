# Comprehensive compilation fix script
Write-Host "=== Fixing All Compilation Issues ===" -ForegroundColor Green

# 1. First, let's identify missing classes and create them
Write-Host "1. Creating missing classes..." -ForegroundColor Cyan

# Create MutationTimeoutInterceptor
$mutationTimeoutInterceptorPath = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/interceptor"
if (-not (Test-Path $mutationTimeoutInterceptorPath)) {
    New-Item -ItemType Directory -Path $mutationTimeoutInterceptorPath -Force
}

# Create GraphQLConfig
$graphqlConfigPath = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/config"
if (-not (Test-Path $graphqlConfigPath)) {
    New-Item -ItemType Directory -Path $graphqlConfigPath -Force
}

# Create MutationMetricsConfig
# Already exists in the path above

# Create application DTOs
$applicationDtoPath = "src/main/java/com/arcone/biopro/exception/collector/application/dto"
if (-not (Test-Path $applicationDtoPath)) {
    New-Item -ItemType Directory -Path $applicationDtoPath -Force
}

Write-Host "2. Checking for missing enum values..." -ForegroundColor Cyan

# Check ExceptionStatus enum
$exceptionStatusPath = "src/main/java/com/arcone/biopro/exception/collector/domain/enums/ExceptionStatus.java"
if (Test-Path $exceptionStatusPath) {
    $content = Get-Content $exceptionStatusPath -Raw
    if (-not ($content -match "FAILED")) {
        Write-Host "   Need to add FAILED to ExceptionStatus enum" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ExceptionStatus enum not found!" -ForegroundColor Red
}

# Check ExceptionCategory enum
$exceptionCategoryPath = "src/main/java/com/arcone/biopro/exception/collector/domain/enums/ExceptionCategory.java"
if (Test-Path $exceptionCategoryPath) {
    $content = Get-Content $exceptionCategoryPath -Raw
    if (-not ($content -match "SYSTEM")) {
        Write-Host "   Need to add SYSTEM to ExceptionCategory enum" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ExceptionCategory enum not found!" -ForegroundColor Red
}

Write-Host "3. Checking pom.xml for missing dependencies..." -ForegroundColor Cyan

$pomPath = "pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw
    
    $missingDeps = @()
    
    if (-not ($pomContent -match "spring-boot-starter-actuator")) {
        $missingDeps += "spring-boot-starter-actuator"
    }
    
    if (-not ($pomContent -match "reactor-test")) {
        $missingDeps += "reactor-test"
    }
    
    if (-not ($pomContent -match "spring-boot-actuator-autoconfigure")) {
        $missingDeps += "spring-boot-actuator-autoconfigure"
    }
    
    if ($missingDeps.Count -gt 0) {
        Write-Host "   Missing dependencies: $($missingDeps -join ', ')" -ForegroundColor Yellow
    } else {
        Write-Host "   All required dependencies appear to be present" -ForegroundColor Green
    }
} else {
    Write-Host "   pom.xml not found!" -ForegroundColor Red
}

Write-Host "4. Running compilation test..." -ForegroundColor Cyan
mvn clean compile -q
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Main compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Main compilation failed" -ForegroundColor Red
}

Write-Host "5. Running test compilation..." -ForegroundColor Cyan
mvn test-compile -q 2>&1 | Out-File -FilePath "test-compilation-errors.log"
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Test compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Test compilation failed - see test-compilation-errors.log" -ForegroundColor Red
    
    # Show first 20 errors
    Write-Host "   First 20 compilation errors:" -ForegroundColor Yellow
    Get-Content "test-compilation-errors.log" | Where-Object { $_ -match "\\[ERROR\\]" } | Select-Object -First 20 | ForEach-Object {
        Write-Host "     $_" -ForegroundColor Red
    }
}

Write-Host "=== Analysis Complete ===" -ForegroundColor Green
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Create missing classes" -ForegroundColor White
Write-Host "2. Add missing enum values" -ForegroundColor White
Write-Host "3. Fix dependency issues" -ForegroundColor White
Write-Host "4. Fix individual test compilation errors" -ForegroundColor White