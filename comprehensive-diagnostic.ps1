# Comprehensive GraphQL Diagnostic - No BS, Just Facts
$ErrorActionPreference = "Continue"

Write-Host "🔍 COMPREHENSIVE GRAPHQL DIAGNOSTIC" -ForegroundColor Blue
Write-Host "====================================" -ForegroundColor Blue

# Step 1: Check if app is running
Write-Host "`n1. Application Status Check" -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5
    Write-Host "✅ Application is running - Status: $($health.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Application not running: $_" -ForegroundColor Red
    Write-Host "Starting application..." -ForegroundColor Yellow
    
    # Start the application
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd interface-exception-collector `& mvn spring-boot:run" -WindowStyle Minimized
    
    Write-Host "Waiting for application to start..." -ForegroundColor Yellow
    $maxWait = 60
    $waited = 0
    
    while ($waited -lt $maxWait) {
        try {
            $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -TimeoutSec 2
            if ($health.status -eq "UP") {
                Write-Host "✅ Application started!" -ForegroundColor Green
                break
            }
        } catch {
            # Still starting
        }
        
        Start-Sleep -Seconds 2
        $waited += 2
        Write-Host "." -NoNewline -ForegroundColor Gray
    }
    
    if ($waited -ge $maxWait) {
        Write-Host "`n❌ Application failed to start" -ForegroundColor Red
        exit 1
    }
}

# Step 2: Generate JWT token
Write-Host "`n2. JWT Token Generation" -ForegroundColor Cyan
try {
    $token = (node generate-jwt-correct-secret.js 2>$null -split "`n")[-1].Trim()
    if ($token -and $token.Length -gt 50) {
        Write-Host "✅ JWT token generated successfully" -ForegroundColor Green
    } else {
        Write-Host "❌ JWT token generation failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ JWT generation error: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Step 3: Test GraphQL endpoint basic functionality
Write-Host "`n3. GraphQL Endpoint Basic Test" -ForegroundColor Cyan
$basicQuery = @{ query = "{ __typename }" } | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $basicQuery -TimeoutSec 10
    Write-Host "✅ GraphQL endpoint responding: $($response | ConvertTo-Json -Compress)" -ForegroundColor Green
} catch {
    Write-Host "❌ GraphQL endpoint failed: $_" -ForegroundColor Red
    exit 1
}

# Step 4: Check what query fields are actually available
Write-Host "`n4. Available Query Fields Check" -ForegroundColor Cyan
$fieldsQuery = @{ query = "{ __schema { queryType { fields { name type { name } } } } }" } | ConvertTo-Json

try {
    $fieldsResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $fieldsQuery -TimeoutSec 10
    
    if ($fieldsResponse.data.__schema.queryType.fields) {
        Write-Host "✅ Query fields found:" -ForegroundColor Green
        $fieldsResponse.data.__schema.queryType.fields | ForEach-Object {
            Write-Host "  - $($_.name): $($_.type.name)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ NO QUERY FIELDS FOUND!" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Fields query failed: $_" -ForegroundColor Red
}

# Step 5: Check subscription fields
Write-Host "`n5. Available Subscription Fields Check" -ForegroundColor Cyan
$subFieldsQuery = @{ query = "{ __schema { subscriptionType { fields { name type { name } } } } }" } | ConvertTo-Json

try {
    $subResponse = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $subFieldsQuery -TimeoutSec 10
    
    if ($subResponse.data.__schema.subscriptionType.fields) {
        Write-Host "✅ Subscription fields found:" -ForegroundColor Green
        $subResponse.data.__schema.subscriptionType.fields | ForEach-Object {
            Write-Host "  - $($_.name): $($_.type.name)" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ NO SUBSCRIPTION FIELDS FOUND!" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Subscription fields query failed: $_" -ForegroundColor Red
}

# Step 6: Test if any of our expected queries work
Write-Host "`n6. Testing Expected Queries" -ForegroundColor Cyan
$testQueries = @(
    @{ name = "hello"; query = "{ hello }" },
    @{ name = "version"; query = "{ version }" },
    @{ name = "health"; query = "{ health }" },
    @{ name = "summary"; query = "{ summary(timeRange: { start: `"2024-01-01T00:00:00Z`", end: `"2024-12-31T23:59:59Z`" }) { totalExceptions } }" }
)

foreach ($test in $testQueries) {
    $query = @{ query = $test.query } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method Post -Headers $headers -Body $query -TimeoutSec 5
        
        if ($response.errors) {
            $errorMsg = $response.errors[0].message
            if ($errorMsg -like "*not defined*" -or $errorMsg -like "*cannot query*") {
                Write-Host "❌ $($test.name): Field not defined" -ForegroundColor Red
            } else {
                Write-Host "⚠️ $($test.name): Other error - $errorMsg" -ForegroundColor Yellow
            }
        } else {
            Write-Host "✅ $($test.name): WORKS! Response: $($response.data | ConvertTo-Json -Compress)" -ForegroundColor Green
        }
    } catch {
        Write-Host "❌ $($test.name): Request failed - $_" -ForegroundColor Red
    }
}

# Step 7: Check WebSocket endpoint
Write-Host "`n7. WebSocket Endpoint Check" -ForegroundColor Cyan
try {
    # Test if WebSocket endpoint is accessible (this will fail but tell us if it exists)
    $wsTest = Invoke-WebRequest -Uri "http://localhost:8080/graphql-ws" -Method Get -TimeoutSec 5
    Write-Host "✅ WebSocket endpoint accessible" -ForegroundColor Green
} catch {
    if ($_.Exception.Message -like "*426*" -or $_.Exception.Message -like "*Upgrade*") {
        Write-Host "✅ WebSocket endpoint exists (upgrade required)" -ForegroundColor Green
    } else {
        Write-Host "❌ WebSocket endpoint issue: $_" -ForegroundColor Red
    }
}

# Step 8: Check application logs for GraphQL-related messages
Write-Host "`n8. Recent Application Logs Check" -ForegroundColor Cyan
Write-Host "Checking for GraphQL-related log messages..." -ForegroundColor Gray

# Try to find Java process and get some info
$javaProcesses = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "✅ Found $($javaProcesses.Count) Java process(es) running" -ForegroundColor Green
} else {
    Write-Host "❌ No Java processes found" -ForegroundColor Red
}

Write-Host "`n🎯 DIAGNOSTIC SUMMARY" -ForegroundColor Blue
Write-Host "===================" -ForegroundColor Blue
Write-Host "Based on the results above:" -ForegroundColor White
Write-Host "- If NO query fields found: GraphQL resolver scanning is broken" -ForegroundColor Yellow
Write-Host "- If some fields found: Partial resolver registration" -ForegroundColor Yellow  
Write-Host "- If subscription fields missing: Subscription-specific issue" -ForegroundColor Yellow
Write-Host "- If WebSocket endpoint missing: Transport configuration issue" -ForegroundColor Yellow

Write-Host "`nNext step: Check the actual resolver files and Spring configuration..." -ForegroundColor Cyan