# Diagnose GraphQL bean registration and component scanning

$token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

Write-Host "GraphQL Bean and Component Scanning Diagnosis" -ForegroundColor Blue
Write-Host "=============================================" -ForegroundColor Blue

# Check if actuator beans endpoint is available
Write-Host "`nChecking Spring beans..." -ForegroundColor Cyan
try {
    $beans = Invoke-RestMethod -Uri "http://localhost:8080/actuator/beans" -Headers $headers -TimeoutSec 10
    
    # Look for GraphQL related beans
    Write-Host "GraphQL related beans:" -ForegroundColor Yellow
    $graphqlBeans = $beans.contexts.application.beans.PSObject.Properties | Where-Object { 
        $_.Name -like "*graphql*" -or $_.Name -like "*GraphQL*" -or $_.Name -like "*subscription*" -or $_.Name -like "*Subscription*"
    }
    
    if ($graphqlBeans) {
        foreach ($bean in $graphqlBeans) {
            Write-Host "  - $($bean.Name): $($bean.Value.type)" -ForegroundColor Gray
        }
    } else {
        Write-Host "  No GraphQL beans found!" -ForegroundColor Red
    }
    
    # Look for our specific resolver
    Write-Host "`nLooking for ExceptionSubscriptionResolver..." -ForegroundColor Cyan
    $resolverBean = $beans.contexts.application.beans.PSObject.Properties | Where-Object { 
        $_.Name -like "*ExceptionSubscriptionResolver*" -or $_.Value.type -like "*ExceptionSubscriptionResolver*"
    }
    
    if ($resolverBean) {
        Write-Host "✅ ExceptionSubscriptionResolver bean found:" -ForegroundColor Green
        foreach ($bean in $resolverBean) {
            Write-Host "  - $($bean.Name): $($bean.Value.type)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ ExceptionSubscriptionResolver bean NOT found!" -ForegroundColor Red
    }
    
    # Look for other controller beans to verify component scanning
    Write-Host "`nOther controller beans (to verify component scanning):" -ForegroundColor Cyan
    $controllerBeans = $beans.contexts.application.beans.PSObject.Properties | Where-Object { 
        $_.Value.type -like "*Controller*" -and $_.Value.type -like "*com.arcone.biopro.exception.collector*"
    }
    
    if ($controllerBeans) {
        foreach ($bean in $controllerBeans) {
            Write-Host "  - $($bean.Name): $($bean.Value.type)" -ForegroundColor Gray
        }
    } else {
        Write-Host "  No controller beans found in our package!" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Could not access beans endpoint: $_" -ForegroundColor Red
    Write-Host "This might be due to security restrictions" -ForegroundColor Yellow
}

# Check GraphQL auto-configuration
Write-Host "`nChecking GraphQL auto-configuration..." -ForegroundColor Cyan
try {
    $autoconfig = Invoke-RestMethod -Uri "http://localhost:8080/actuator/conditions" -Headers $headers -TimeoutSec 10
    
    # Look for GraphQL related auto-configuration
    $graphqlAutoConfig = $autoconfig.contexts.application.positiveMatches.PSObject.Properties | Where-Object { 
        $_.Name -like "*GraphQL*" -or $_.Name -like "*graphql*"
    }
    
    if ($graphqlAutoConfig) {
        Write-Host "GraphQL auto-configuration matches:" -ForegroundColor Green
        foreach ($config in $graphqlAutoConfig) {
            Write-Host "  ✅ $($config.Name)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ No GraphQL auto-configuration found!" -ForegroundColor Red
    }
    
    # Check for negative matches (failed conditions)
    $graphqlNegativeMatches = $autoconfig.contexts.application.negativeMatches.PSObject.Properties | Where-Object { 
        $_.Name -like "*GraphQL*" -or $_.Name -like "*graphql*"
    }
    
    if ($graphqlNegativeMatches) {
        Write-Host "`nGraphQL auto-configuration failures:" -ForegroundColor Red
        foreach ($config in $graphqlNegativeMatches) {
            Write-Host "  ❌ $($config.Name)" -ForegroundColor Red
            if ($config.Value.notMatched) {
                foreach ($reason in $config.Value.notMatched) {
                    Write-Host "    Reason: $($reason.condition)" -ForegroundColor Gray
                }
            }
        }
    }
    
} catch {
    Write-Host "❌ Could not access conditions endpoint: $_" -ForegroundColor Red
}

# Test a simple GraphQL query to see what's working
Write-Host "`nTesting basic GraphQL functionality..." -ForegroundColor Cyan
$basicQuery = @{
    query = "{ __schema { queryType { name } mutationType { name } subscriptionType { name } } }"
} | ConvertTo-Json

try {
    $schemaResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $basicQuery -TimeoutSec 10
    
    Write-Host "GraphQL schema root types:" -ForegroundColor Yellow
    Write-Host "  Query: $($schemaResponse.data.__schema.queryType.name)" -ForegroundColor Gray
    Write-Host "  Mutation: $($schemaResponse.data.__schema.mutationType.name)" -ForegroundColor Gray
    Write-Host "  Subscription: $($schemaResponse.data.__schema.subscriptionType.name)" -ForegroundColor Gray
    
    if ($schemaResponse.data.__schema.subscriptionType.name) {
        Write-Host "✅ Subscription type exists in schema!" -ForegroundColor Green
    } else {
        Write-Host "❌ Subscription type missing from schema!" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ GraphQL query failed: $_" -ForegroundColor Red
}

Write-Host "`nDiagnosis Summary:" -ForegroundColor Blue
Write-Host "==================" -ForegroundColor Blue
Write-Host "1. Check if ExceptionSubscriptionResolver bean is created" -ForegroundColor White
Write-Host "2. Verify GraphQL auto-configuration is enabled" -ForegroundColor White
Write-Host "3. Confirm subscription type exists in schema" -ForegroundColor White
Write-Host "4. If bean exists but schema doesn't: annotation processing issue" -ForegroundColor White
Write-Host "5. If bean missing: component scanning or annotation issue" -ForegroundColor White