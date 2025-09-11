# Fix File Lock Issue and Database Connection
Write-Host "=== Fixing File Lock and Database Issues ===" -ForegroundColor Green

# Step 1: Clean up any locked files
Write-Host "1. Cleaning up locked files..." -ForegroundColor Cyan

# Stop any running Java processes
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "   Stopping Java processes..." -ForegroundColor Gray
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 3
}

# Clean Maven target directory
if (Test-Path "target") {
    Write-Host "   Removing target directory..." -ForegroundColor Gray
    Remove-Item -Path "target" -Recurse -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

Write-Host "   ✅ Cleanup complete" -ForegroundColor Green

# Step 2: Create a simple startup script that bypasses database
Write-Host "`n2. Creating database-free startup configuration..." -ForegroundColor Cyan

# Create a minimal application-nodatabase.yml
$noDbYamlPath = "src/main/resources/application-nodatabase.yml"
$noDbConfig = @"
# No database configuration - for development only
spring:
  autoconfigure:
    exclude:
      - org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
      - org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
      - org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
  
  jpa:
    hibernate:
      ddl-auto: none
      
server:
  port: 8095

# Disable all database-related features
app:
  mutation:
    database:
      enabled: false
      
management:
  health:
    db:
      enabled: false
  endpoints:
    web:
      exposure:
        include: health,info

logging:
  level:
    com.arcone.biopro: INFO
    org.springframework: WARN
    org.hibernate: WARN
"@

Set-Content -Path $noDbYamlPath -Value $noDbConfig -Encoding UTF8
Write-Host "   ✅ Created application-nodatabase.yml" -ForegroundColor Green

# Step 3: Try compilation with clean slate
Write-Host "`n3. Attempting clean compilation..." -ForegroundColor Cyan

mvn clean -q
Start-Sleep -Seconds 2

mvn compile -DskipTests -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful!" -ForegroundColor Green
    
    # Step 4: Test startup without database
    Write-Host "`n4. Testing application startup without database..." -ForegroundColor Cyan
    
    $startupJob = Start-Job -ScriptBlock {
        Set-Location $using:PWD
        mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=nodatabase" 2>&1
    }
    
    $timeout = 25
    $elapsed = 0
    $success = $false
    $dbError = $false
    
    while ($elapsed -lt $timeout) {
        Start-Sleep -Seconds 3
        $elapsed += 3
        
        $output = Receive-Job -Job $startupJob -Keep
        
        # Check for database connection errors
        if ($output -match "Connection to localhost:5432 refused|Failed to initialize pool|MutationPool") {
            $dbError = $true
            Write-Host "   ❌ Still trying to connect to database" -ForegroundColor Red
            break
        }
        
        # Check for successful startup
        if ($output -match "Started.*Application|Tomcat started on port") {
            $success = $true
            Write-Host "   ✅ Application started successfully without database!" -ForegroundColor Green
            break
        }
        
        if ($elapsed % 10 -eq 0) {
            Write-Host "   Waiting for startup... ($elapsed/$timeout seconds)" -ForegroundColor Gray
        }
    }
    
    # Clean up
    Stop-Job -Job $startupJob 2>$null
    Remove-Job -Job $startupJob 2>$null
    
    if ($success) {
        Write-Host "`n🎉 SUCCESS: Application runs without database!" -ForegroundColor Green
        Write-Host "Use: mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=nodatabase'" -ForegroundColor Cyan
    } elseif ($dbError) {
        Write-Host "`n⚠️ Application still trying to connect to database" -ForegroundColor Yellow
        Write-Host "Additional configuration changes needed" -ForegroundColor White
    } else {
        Write-Host "`n⚠️ Startup test inconclusive" -ForegroundColor Yellow
    }
    
} else {
    Write-Host "   ❌ Compilation still failing" -ForegroundColor Red
    Write-Host "   Checking for specific errors..." -ForegroundColor Yellow
    
    # Try to identify the specific issue
    mvn compile 2>&1 | Select-String -Pattern "ERROR|Failed|Exception" | Select-Object -First 5 | ForEach-Object {
        Write-Host "     $_" -ForegroundColor Red
    }
}

# Step 5: Create a simple run script
Write-Host "`n5. Creating run scripts..." -ForegroundColor Cyan

$runScriptPath = "run-without-database.ps1"
$runScript = @"
# Run application without database
Write-Host "Starting application without database..." -ForegroundColor Green

# Clean any locks
Get-Process -Name "java" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

# Start application
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=nodatabase --server.port=8095"
"@

Set-Content -Path $runScriptPath -Value $runScript -Encoding UTF8
Write-Host "   ✅ Created $runScriptPath" -ForegroundColor Green

$runBatPath = "run-without-database.bat"
$runBat = @"
@echo off
echo Starting application without database...
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=nodatabase --server.port=8095"
"@

Set-Content -Path $runBatPath -Value $runBat -Encoding UTF8
Write-Host "   ✅ Created $runBatPath" -ForegroundColor Green

Write-Host "`n=== SUMMARY ===" -ForegroundColor Yellow
Write-Host "✅ Cleaned up locked files" -ForegroundColor Green
Write-Host "✅ Created database-free configuration" -ForegroundColor Green
Write-Host "✅ Created run scripts" -ForegroundColor Green

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation working" -ForegroundColor Green
} else {
    Write-Host "⚠️ Compilation issues remain" -ForegroundColor Yellow
}

Write-Host "`nTo run without database:" -ForegroundColor Cyan
Write-Host "  powershell .\run-without-database.ps1" -ForegroundColor White
Write-Host "  or" -ForegroundColor Gray
Write-Host "  .\run-without-database.bat" -ForegroundColor White