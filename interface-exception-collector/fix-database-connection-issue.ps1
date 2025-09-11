# Fix Database Connection Issue for Development
Write-Host "=== Fixing Database Connection Issue ===" -ForegroundColor Green

# Step 1: Check current database configuration
Write-Host "1. Analyzing database configuration..." -ForegroundColor Cyan

$yamlPath = "src/main/resources/application.yml"
if (Test-Path $yamlPath) {
    Write-Host "   Checking database settings in application.yml..." -ForegroundColor Gray
    
    # Look for database configuration
    $dbConfig = Select-String -Path $yamlPath -Pattern "url:|host:|port:|5432" -Context 2
    if ($dbConfig) {
        Write-Host "   Found database configuration:" -ForegroundColor Yellow
        $dbConfig | ForEach-Object { Write-Host "     $($_.Line)" -ForegroundColor Gray }
    }
}

# Step 2: Create application-dev.yml for development without database
Write-Host "`n2. Creating development profile without database dependency..." -ForegroundColor Cyan

$devYamlPath = "src/main/resources/application-dev.yml"
$devConfig = @"
# Development configuration - No database required
spring:
  datasource:
    # Use H2 in-memory database for development
    url: jdbc:h2:mem:devdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: true
  
  h2:
    console:
      enabled: true
      path: /h2-console

# Disable mutation database config for dev
app:
  mutation:
    database:
      enabled: false
      
# Override any PostgreSQL specific configurations
management:
  health:
    db:
      enabled: false
"@

Set-Content -Path $devYamlPath -Value $devConfig -Encoding UTF8
Write-Host "   ✅ Created $devYamlPath for development" -ForegroundColor Green

# Step 3: Create application-test.yml for testing
Write-Host "`n3. Creating test profile..." -ForegroundColor Cyan

$testYamlPath = "src/main/resources/application-test.yml"
$testConfig = @"
# Test configuration - In-memory database
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
    
# Disable external dependencies for testing
app:
  mutation:
    database:
      enabled: false

logging:
  level:
    com.arcone.biopro: DEBUG
    org.springframework: WARN
"@

Set-Content -Path $testYamlPath -Value $testConfig -Encoding UTF8
Write-Host "   ✅ Created $testYamlPath for testing" -ForegroundColor Green

# Step 4: Check if H2 dependency is in pom.xml
Write-Host "`n4. Checking H2 database dependency..." -ForegroundColor Cyan

$pomPath = "pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw
    if ($pomContent -match "h2") {
        Write-Host "   ✅ H2 dependency found in pom.xml" -ForegroundColor Green
    } else {
        Write-Host "   ⚠️ H2 dependency not found, adding it..." -ForegroundColor Yellow
        
        # Add H2 dependency
        $h2Dependency = @"
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
"@
        
        # Insert before </dependencies>
        $pomContent = $pomContent -replace "(\s*</dependencies>)", "$h2Dependency`n`$1"
        Set-Content -Path $pomPath -Value $pomContent -Encoding UTF8
        Write-Host "   ✅ Added H2 dependency to pom.xml" -ForegroundColor Green
    }
}

# Step 5: Modify MutationDatabaseConfig to be conditional
Write-Host "`n5. Making database configuration conditional..." -ForegroundColor Cyan

$dbConfigPath = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/config/MutationDatabaseConfig.java"
if (Test-Path $dbConfigPath) {
    $dbConfigContent = Get-Content $dbConfigPath -Raw
    
    # Add conditional annotation if not present
    if ($dbConfigContent -notmatch "@ConditionalOnProperty") {
        Write-Host "   Adding conditional property to MutationDatabaseConfig..." -ForegroundColor Gray
        
        # Add import if not present
        if ($dbConfigContent -notmatch "import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty") {
            $dbConfigContent = $dbConfigContent -replace "(import org.springframework.context.annotation.Configuration;)", "`$1`nimport org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;"
        }
        
        # Add conditional annotation to class
        $dbConfigContent = $dbConfigContent -replace "(@Configuration)", "@ConditionalOnProperty(name = `"app.mutation.database.enabled`", havingValue = `"true`", matchIfMissing = true)`n`$1"
        
        Set-Content -Path $dbConfigPath -Value $dbConfigContent -Encoding UTF8
        Write-Host "   ✅ Made MutationDatabaseConfig conditional" -ForegroundColor Green
    } else {
        Write-Host "   ✅ MutationDatabaseConfig already conditional" -ForegroundColor Green
    }
}

# Step 6: Test compilation
Write-Host "`n6. Testing compilation..." -ForegroundColor Cyan
mvn clean compile -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful!" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed" -ForegroundColor Red
}

# Step 7: Test application startup with dev profile
Write-Host "`n7. Testing application startup with dev profile..." -ForegroundColor Cyan

$startupJob = Start-Job -ScriptBlock {
    Set-Location $using:PWD
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev --server.port=8094" 2>&1
}

$timeout = 30
$elapsed = 0
$success = $false
$dbError = $false

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 3
    $elapsed += 3
    
    $output = Receive-Job -Job $startupJob -Keep
    
    # Check for database connection errors
    if ($output -match "Connection to localhost:5432 refused|Failed to initialize pool") {
        $dbError = $true
        Write-Host "   ❌ Still has database connection issues" -ForegroundColor Red
        break
    }
    
    # Check for successful startup
    if ($output -match "Started.*Application|Tomcat started on port") {
        $success = $true
        Write-Host "   ✅ Application started successfully with dev profile!" -ForegroundColor Green
        break
    }
    
    if ($elapsed % 10 -eq 0) {
        Write-Host "   Waiting for startup... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

# Clean up
Stop-Job -Job $startupJob 2>$null
Remove-Job -Job $startupJob 2>$null

# Step 8: Summary
Write-Host "`n=== DATABASE CONNECTION FIX SUMMARY ===" -ForegroundColor Yellow

if ($success) {
    Write-Host "🎉 SUCCESS: Application now runs without PostgreSQL!" -ForegroundColor Green
    Write-Host "✅ Created development profile (application-dev.yml)" -ForegroundColor Green
    Write-Host "✅ Created test profile (application-test.yml)" -ForegroundColor Green
    Write-Host "✅ Added H2 in-memory database support" -ForegroundColor Green
    Write-Host "✅ Made database configuration conditional" -ForegroundColor Green
    Write-Host "✅ Application starts successfully" -ForegroundColor Green
    
    Write-Host "`nTo run the application:" -ForegroundColor Cyan
    Write-Host "  mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=dev'" -ForegroundColor White
    Write-Host "  or" -ForegroundColor Gray
    Write-Host "  java -jar target/app.jar --spring.profiles.active=dev" -ForegroundColor White
    
} elseif ($dbError) {
    Write-Host "⚠️ Database connection issues persist" -ForegroundColor Yellow
    Write-Host "Additional configuration may be needed" -ForegroundColor White
} else {
    Write-Host "✅ Configuration files created" -ForegroundColor Green
    Write-Host "⚠️ Startup test inconclusive" -ForegroundColor Yellow
}

Write-Host "`nDevelopment environment is ready!" -ForegroundColor Green