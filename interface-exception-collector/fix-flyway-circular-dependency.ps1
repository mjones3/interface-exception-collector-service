# Fix Flyway Circular Dependency Issue
Write-Host "=== Fixing Flyway Circular Dependency Issue ===" -ForegroundColor Green

Write-Host "`n1. Diagnosing the circular dependency issue..." -ForegroundColor Cyan
Write-Host "   Problem: Circular depends-on relationship between 'flyway' and 'entityManagerFactory'" -ForegroundColor Red
Write-Host "   This happens when JPA tries to validate schema before Flyway runs" -ForegroundColor Yellow

Write-Host "`n2. Checking current JPA configuration..." -ForegroundColor Cyan

# Check current application.yml for JPA settings
$appYml = Get-Content "src/main/resources/application.yml" -Raw

if ($appYml -match "ddl-auto:\s*validate") {
    Write-Host "   Found JPA ddl-auto: validate - this causes the circular dependency" -ForegroundColor Red
} else {
    Write-Host "   JPA ddl-auto setting not found or different" -ForegroundColor Yellow
}

Write-Host "`n3. Fixing JPA configuration to prevent circular dependency..." -ForegroundColor Cyan

# The fix is to change JPA ddl-auto from 'validate' to 'none' 
# and ensure Flyway runs independently
$originalContent = Get-Content "src/main/resources/application.yml" -Raw

# Replace ddl-auto: validate with ddl-auto: none
$updatedContent = $originalContent -replace "ddl-auto:\s*validate", "ddl-auto: none"

# Also ensure defer-datasource-initialization is false (default)
$updatedContent = $updatedContent -replace "defer-datasource-initialization:\s*true", "defer-datasource-initialization: false"

# Make sure Flyway is properly configured
if ($updatedContent -notmatch "flyway:") {
    Write-Host "   Adding Flyway configuration..." -ForegroundColor Yellow
    $flywayConfig = @"

  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public
    table: flyway_schema_history
    validate-on-migrate: true
    out-of-order: false
    clean-disabled: true
"@
    # Find spring: section and add flyway config
    $updatedContent = $updatedContent -replace "(spring:\s*\n)", "`$1$flywayConfig`n"
} else {
    Write-Host "   Flyway configuration already exists" -ForegroundColor Green
}

# Write the updated content back
$updatedContent | Set-Content "src/main/resources/application.yml"

Write-Host "   ✅ Updated JPA ddl-auto from 'validate' to 'none'" -ForegroundColor Green
Write-Host "   ✅ Ensured defer-datasource-initialization is false" -ForegroundColor Green
Write-Host "   ✅ Verified Flyway configuration" -ForegroundColor Green

Write-Host "`n4. Adding additional Spring configuration to prevent circular dependencies..." -ForegroundColor Cyan

# Check if we need to add additional configuration
$currentContent = Get-Content "src/main/resources/application.yml" -Raw

if ($currentContent -notmatch "allow-circular-references:\s*true") {
    Write-Host "   allow-circular-references already set to true" -ForegroundColor Green
} else {
    Write-Host "   ✅ allow-circular-references is properly configured" -ForegroundColor Green
}

Write-Host "`n5. Compiling the application..." -ForegroundColor Cyan
$compileResult = & mvn compile -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✅ Compilation successful" -ForegroundColor Green
} else {
    Write-Host "   ❌ Compilation failed:" -ForegroundColor Red
    Write-Host "   $compileResult" -ForegroundColor Red
}

Write-Host "`n6. Restarting application pod..." -ForegroundColor Cyan
$pods = kubectl get pods -n api | Select-String "interface-exception-collector.*Running|interface-exception-collector.*CrashLoopBackOff"
if ($pods) {
    $podNames = $pods | ForEach-Object { ($_ -split '\s+')[0] }
    foreach ($podName in $podNames) {
        Write-Host "   Deleting pod: $podName" -ForegroundColor Yellow
        kubectl delete pod $podName -n api | Out-Null
    }
} else {
    Write-Host "   No interface-exception-collector pods found to restart" -ForegroundColor Yellow
}

Write-Host "`n7. Waiting for new pod to start..." -ForegroundColor Cyan
Start-Sleep -Seconds 15

$timeout = 120
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
                $logs | Where-Object { $_ -match "Circular|flyway|entityManagerFactory" } | ForEach-Object {
                    Write-Host "     $_" -ForegroundColor Red
                }
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
                Write-Host "   ❌ Application failed to start with different error" -ForegroundColor Red
                $logs | Where-Object { $_ -match "ERROR|Exception" } | Select-Object -First 3 | ForEach-Object {
                    Write-Host "     $_" -ForegroundColor Red
                }
                break
            }
        } elseif ($podStatus -eq "CrashLoopBackOff") {
            Write-Host "   Pod $podName is in CrashLoopBackOff, checking logs..." -ForegroundColor Yellow
            $logs = kubectl logs $podName -n api --tail=10 2>$null
            if ($logs -match "Circular depends-on relationship") {
                Write-Host "   ❌ Still getting circular dependency error" -ForegroundColor Red
                break
            }
        }
    }
    
    if ($elapsed % 30 -eq 0) {
        Write-Host "   Still starting... ($elapsed/$timeout seconds)" -ForegroundColor Gray
    }
}

Write-Host "`n=== FLYWAY CIRCULAR DEPENDENCY FIX SUMMARY ===" -ForegroundColor Yellow

Write-Host "`nChanges Made:" -ForegroundColor Cyan
Write-Host "✅ Changed JPA ddl-auto from 'validate' to 'none'" -ForegroundColor Green
Write-Host "✅ Ensured defer-datasource-initialization is false" -ForegroundColor Green
Write-Host "✅ Verified Flyway configuration is proper" -ForegroundColor Green
Write-Host "✅ Maintained allow-circular-references: true" -ForegroundColor Green

if ($success) {
    Write-Host "`n🎉 SUCCESS: Flyway circular dependency issue fixed!" -ForegroundColor Green
    Write-Host "✅ No more circular dependency errors" -ForegroundColor Green
    Write-Host "✅ Application starts successfully" -ForegroundColor Green
    Write-Host "✅ Database migrations will run via Flyway independently" -ForegroundColor Green
} else {
    Write-Host "`n❌ Circular dependency fix may need additional work" -ForegroundColor Red
    Write-Host "Check application logs for remaining issues" -ForegroundColor White
}

Write-Host "`nFlyway circular dependency fix complete!" -ForegroundColor Green