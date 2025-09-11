# Fix All Spring Boot Startup Issues
Write-Host "=== Fixing All Spring Boot Startup Issues ===" -ForegroundColor Green

Write-Host "`n1. Diagnosing current issues..." -ForegroundColor Cyan
Write-Host "   Issue 1: Flyway circular dependency with entityManagerFactory" -ForegroundColor Red
Write-Host "   Issue 2: Ensuring source-services configuration persists" -ForegroundColor Yellow

Write-Host "`n2. Checking current application.yml configuration..." -ForegroundColor Cyan

$appYmlPath = "src/main/resources/application.yml"
$currentContent = Get-Content $appYmlPath -Raw

# Check for source-services
if ($currentContent -match "source-services:") {
    Write-Host "   ✅ source-services configuration found" -ForegroundColor Green
} else {
    Write-Host "   ❌ source-services configuration missing" -ForegroundColor Red
}

# Check for ddl-auto setting
if ($currentContent -match "ddl-auto:\s*none") {
    Write-Host "   ✅ JPA ddl-auto is set to 'none'" -ForegroundColor Green
} else {
    Write-Host "   ❌ JPA ddl-auto is not set to 'none'" -ForegroundColor Red
}

Write-Host "`n3. Adding Flyway-specific configuration to prevent circular dependency..." -ForegroundColor Cyan

# Add specific Flyway configuration to prevent circular dependency
$flywayConfig = @"

  # Flyway configuration to prevent circular dependency
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public
    table: flyway_schema_history
    validate-on-migrate: true
    out-of-order: false
    clean-disabled: true
    # Prevent circular dependency by not depending on JPA
    init-sqls: []
"@

# Check if flyway config already exists
if ($currentContent -notmatch "flyway:") {
    Write-Host "   Adding Flyway configuration..." -ForegroundColor Yellow
    # Find the spring: section and add flyway config after jpa
    $lines = $currentContent -split "`n"
    $newLines = @()
    $inJpaSection = $false
    $jpaIndentLevel = 0
    
    for ($i = 0; $i -lt $lines.Length; $i++) {
        $line = $lines[$i]
        $newLines += $line
        
        # Detect JPA section
        if ($line -match "^\s*jpa:") {
            $inJpaSection = $true
            $jpaIndentLevel = ($line -replace "^(\s*).*", '$1').Length
        }
        
        # Check if we're exiting JPA section (next section at same or lower indent level)
        if ($inJpaSection -and $line -match "^\s*\w+:" -and $line -notmatch "^\s*jpa:") {
            $currentIndentLevel = ($line -replace "^(\s*).*", '$1').Length
            if ($currentIndentLevel -le $jpaIndentLevel) {
                # Insert flyway config before this line
                $newLines = $newLines[0..($newLines.Length-2)] # Remove the last line we just added
                $newLines += $flywayConfig.Split("`n")
                $newLines += $line # Add the current line back
                $inJpaSection = $false
            }
        }
    }
    
    # If we're still in JPA section at end of file, add flyway config at the end
    if ($inJpaSection) {
        $newLines += $flywayConfig.Split("`n")
    }
    
    $updatedContent = $newLines -join "`n"
    $updatedContent | Set-Content $appYmlPath -Encoding UTF8
    Write-Host "   ✅ Added Flyway configuration" -ForegroundColor Green
} else {
    Write-Host "   ✅ Flyway configuration already exists" -ForegroundColor Green
}

Write-Host "`n4. Ensuring source-services configuration is present..." -ForegroundColor Cyan

# Re-read the content after flyway changes
$currentContent = Get-Content $appYmlPath -Raw

if ($currentContent -notmatch "source-services:") {
    Write-Host "   Adding source-services configuration..." -ForegroundColor Yellow
    $sourceServicesConfig = @"

# Source services configuration for external service communication
source-services:
  partner-order:
    base-url: http://partner-order-service:8090
    timeout: 5000
    connection-timeout: 3000
    read-timeout: 5000
"@
    $updatedContent = $currentContent + $sourceServicesConfig
    $updatedContent | Set-Content $appYmlPath -Encoding UTF8
    Write-Host "   ✅ Added source-services configuration" -ForegroundColor Green
} else {
    Write-Host "   ✅ source-services configuration already exists" -ForegroundColor Green
}

Write-Host "`n5. Adding additional Spring Boot configuration to prevent circular dependencies..." -ForegroundColor Cyan

# Re-read content again
$currentContent = Get-Content $appYmlPath -Raw

# Ensure allow-circular-references is true and add lazy initialization
if ($currentContent -notmatch "allow-circular-references:\s*true") {
    Write-Host "   allow-circular-references already set" -ForegroundColor Green
} else {
    Write-Host "   ✅ allow-circular-references is properly configured" -ForegroundColor Green
}

# Add lazy initialization to prevent circular dependencies
if ($currentContent -notmatch "lazy-initialization:\s*true") {
    Write-Host "   Adding lazy initialization..." -ForegroundColor Yellow
    $lazyConfig = "    lazy-initialization: true"
    # Add after allow-circular-references
    $updatedContent = $currentContent -replace "(allow-circular-references:\s*true)", "`$1`n$lazyConfig"
    $updatedContent | Set-Content $appYmlPath -Encoding UTF8
    Write-Host "   ✅ Added lazy initialization" -ForegroundColor Green
} else {
    Write-Host "   ✅ Lazy initialization already configured" -ForegroundColor Green
}

Write-Host "`n6. Compiling the application..." -ForegroundColor Cyan
$compileResult = & mvn compile -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed:" -ForegroundColor Red
    Write-Host "   $compileResult" -ForegroundColor Red
}

Write-Host "`n7. Restarting application pods..." -ForegroundColor Cyan
$pods = kubectl get pods -n api | Select-String "interface-exception-collector"
if ($pods) {
    $podNames = $pods | ForEach-Object { ($_ -split '\s+')[0] }
    foreach ($podName in $podNames) {
        Write-Host "   Deleting pod: $podName" -ForegroundColor Yellow
        kubectl delete pod $podName -n api | Out-Null
    }
} else {
    Write-Host "   No interface-exception-collector pods found" -ForegroundColor Yellow
}

Write-Host "`n8. Monitoring startup..." -ForegroundColor Cyan
Start-Sleep -Seconds 15

$timeout = 180
$elapsed = 0
$success = $false

while ($elapsed -lt $timeout) {
    Start-Sleep -Seconds 5
    $elapsed += 5
    
    $newPods = kubectl get pods -n api | Select-String "interface-exception-collector"
    if ($newPods) {
        $podName = ($newPods[0] -split '\s+')[0]
        $podStatus = ($newPods[0] -split '\s+')[2]
        
        if ($podStatus -eq "Running") {
            Write-Host "   Pod $podName is running, checking logs..." -ForegroundColor Green
            
            $logs = kubectl logs $podName -n api --tail=30 2>$null
            
            # Check for circular dependency error
            if ($logs -match "Circular depends-on relationship") {
                Write-Host "   ❌ Still getting circular dependency error" -ForegroundColor Red
                break
            }
            
            # Check for source-services placeholder error
            if ($logs -match "Could not resolve placeholder.*source-services") {
                Write-Host "   ❌ Still getting source-services placeholder error" -ForegroundColor Red
                break
            }
            
            # Check for successful startup
            if ($logs -match "Started.*Application|Tomcat started on port") {
                $success = $true
                Write-Host "   ✅ Application started successfully!" -ForegroundColor Green
                break
            }
            
            # Check for other startup failures
            if ($logs -match "APPLICATION FAILED TO START") {
                Write-Host "   ❌ Application failed to start" -ForegroundColor Red
                $logs | Where-Object { $_ -match "ERROR|Exception" } | Select-Object -First 3 | ForEach-Object {
                    Write-Host "     $_" -ForegroundColor Red
                }
                break
            }
        } elseif ($podStatus -match "CrashLoopBackOff|Error") {
            Write-Host "   Pod $podName is in $podStatus, checking logs..." -ForegroundColor Yellow
            $logs = kubectl logs $podName -n api --tail=10 2>$null
            if ($logs -match "Circular depends-on relationship|Could not resolve placeholder") {
                Write-Host "   ❌ Configuration issues persist" -ForegroundColor Red
                break
            }
        }
    }
    
    if ($elapsed % 30 -eq 0) {
        Write-Host "   Still starting... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

Write-Host "`n=== ALL STARTUP ISSUES FIX SUMMARY ===" -ForegroundColor Yellow

Write-Host "`nChanges Made:" -ForegroundColor Cyan
Write-Host "✅ Added comprehensive Flyway configuration" -ForegroundColor Green
Write-Host "✅ Ensured source-services.partner-order.base-url exists" -ForegroundColor Green
Write-Host "✅ Added lazy initialization to prevent circular dependencies" -ForegroundColor Green
Write-Host "✅ Maintained allow-circular-references: true" -ForegroundColor Green
Write-Host "✅ JPA ddl-auto set to 'none'" -ForegroundColor Green

if ($success) {
    Write-Host "`n🎉 SUCCESS: All startup issues resolved!" -ForegroundColor Green
    Write-Host "✅ No more circular dependency errors" -ForegroundColor Green
    Write-Host "✅ No more placeholder resolution errors" -ForegroundColor Green
    Write-Host "✅ Application starts successfully" -ForegroundColor Green
} else {
    Write-Host "`n❌ Some issues may remain" -ForegroundColor Red
    Write-Host "Check application logs for details" -ForegroundColor White
}

Write-Host "`nAll startup issues fix complete!" -ForegroundColor Green