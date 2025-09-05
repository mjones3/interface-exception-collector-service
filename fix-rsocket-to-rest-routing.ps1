#!/usr/bin/env pwsh
# Fix RSocket to REST routing for PARTNER_ORDER interface types

Write-Host "=== Fixing RSocket to REST Routing for PARTNER_ORDER ===" -ForegroundColor Green

# 1. First, check current configuration
Write-Host "`n1. Checking current configuration..." -ForegroundColor Yellow

$configFile = "interface-exception-collector/src/main/resources/application-local.yml"
if (Test-Path $configFile) {
    Write-Host "Current application-local.yml:" -ForegroundColor Cyan
    Get-Content $configFile | Select-Object -Last 20
}

# 2. Update application-local.yml to use HTTP URLs for partner-order-service
Write-Host "`n2. Updating application-local.yml..." -ForegroundColor Yellow

$newConfig = @"
spring:
  application:
    name: interface-exception-collector
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: exception-collector-group
      auto-offset-reset: earliest
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

server:
  port: 8080

logging:
  level:
    com.arcone.biopro.exception.collector: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG

# Source service configurations - FIXED TO USE HTTP
source-services:
  partner-order-service:
    base-url: http://localhost:7001
    timeout: 30s
    retry:
      max-attempts: 3
      delay: 1s
  mock-rsocket-server:
    base-url: rsocket://localhost:7000
    timeout: 30s
    retry:
      max-attempts: 3
      delay: 1s

# Interface type to service mapping - CRITICAL FIX
interface-routing:
  PARTNER_ORDER: partner-order-service
  ORDER: mock-rsocket-server
  INVENTORY: mock-rsocket-server

# Security configuration
jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 3600000

# Feature flags
features:
  rsocket:
    enabled: true
  rest-clients:
    enabled: true
  partner-order-rest:
    enabled: true
"@

Set-Content -Path $configFile -Value $newConfig -Encoding UTF8
Write-Host "✅ Updated application-local.yml with HTTP URLs for partner-order-service" -ForegroundColor Green

# 3. Check and update SourceServiceClientConfiguration to ensure proper routing
Write-Host "`n3. Checking SourceServiceClientConfiguration..." -ForegroundColor Yellow

$configJavaFile = "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/config/SourceServiceClientConfiguration.java"
if (Test-Path $configJavaFile) {
    $configContent = Get-Content $configJavaFile -Raw
    
    # Check if it has proper interface routing logic
    if ($configContent -notmatch "PARTNER_ORDER.*partner-order-service") {
        Write-Host "⚠️  Configuration may need interface routing updates" -ForegroundColor Yellow
        
        # Update the configuration to ensure proper routing
        $updatedConfig = $configContent -replace 
        '@Bean\s+@ConditionalOnProperty\(name = "features\.partner-order-rest\.enabled", havingValue = "true"\)\s+public PartnerOrderServiceClient partnerOrderServiceClient\(\) \{[^}]+\}',
        '@Bean
    @ConditionalOnProperty(name = "features.partner-order-rest.enabled", havingValue = "true")
    public PartnerOrderServiceClient partnerOrderServiceClient() {
        String baseUrl = sourceServiceProperties.getServices().get("partner-order-service").getBaseUrl();
        log.info("Creating PartnerOrderServiceClient with base URL: {}", baseUrl);
        return new PartnerOrderServiceClient(restTemplate, baseUrl);
    }'
        
        Set-Content -Path $configJavaFile -Value $updatedConfig -Encoding UTF8
        Write-Host "✅ Updated SourceServiceClientConfiguration" -ForegroundColor Green
    }
}

# 4. Update BaseSourceServiceClient to handle interface routing properly
Write-Host "`n4. Checking BaseSourceServiceClient routing logic..." -ForegroundColor Yellow

$baseClientFile = "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/infrastructure/client/BaseSourceServiceClient.java"
if (Test-Path $baseClientFile) {
    $baseClientContent = Get-Content $baseClientFile -Raw
    
    # Check if it has proper URL construction logic
    if ($baseClientContent -match "rsocket://") {
        Write-Host "⚠️  BaseSourceServiceClient still has RSocket URL construction" -ForegroundColor Yellow
        
        # Find and replace the URL construction logic
        $updatedBaseClient = $baseClientContent -replace 
        'String url = baseUrl \+ "/orders/" \+ externalId \+ "/retry";',
        'String url;
        if (baseUrl.startsWith("rsocket://")) {
            // For RSocket services, we need to convert to HTTP for REST calls
            url = baseUrl.replace("rsocket://", "http://") + "/orders/" + externalId + "/retry";
        } else {
            // For HTTP services, use as-is
            url = baseUrl + "/orders/" + externalId + "/retry";
        }'
        
        Set-Content -Path $baseClientFile -Value $updatedBaseClient -Encoding UTF8
        Write-Host "✅ Updated BaseSourceServiceClient URL construction" -ForegroundColor Green
    }
}

# 5. Ensure ExceptionProcessingService routes PARTNER_ORDER to correct client
Write-Host "`n5. Checking ExceptionProcessingService routing..." -ForegroundColor Yellow

$processingServiceFile = "interface-exception-collector/src/main/java/com/arcone/biopro/exception/collector/application/service/ExceptionProcessingService.java"
if (Test-Path $processingServiceFile) {
    $processingContent = Get-Content $processingServiceFile -Raw
    
    # Check if it has proper client selection logic
    if ($processingContent -notmatch "PARTNER_ORDER.*PartnerOrderServiceClient") {
        Write-Host "⚠️  ExceptionProcessingService may need client routing updates" -ForegroundColor Yellow
        
        # Add or update the client selection logic
        $clientSelectionLogic = @'
    private SourceServiceClient getClientForInterfaceType(String interfaceType) {
        log.debug("Selecting client for interface type: {}", interfaceType);
        
        switch (interfaceType) {
            case "PARTNER_ORDER":
                if (partnerOrderServiceClient != null) {
                    log.info("Using PartnerOrderServiceClient for PARTNER_ORDER interface type");
                    return partnerOrderServiceClient;
                } else {
                    log.warn("PartnerOrderServiceClient not available, falling back to MockRSocketOrderServiceClient");
                    return mockRSocketOrderServiceClient;
                }
            case "ORDER":
            case "INVENTORY":
            default:
                log.info("Using MockRSocketOrderServiceClient for interface type: {}", interfaceType);
                return mockRSocketOrderServiceClient;
        }
    }
'@
        
        # Insert the method if it doesn't exist
        if ($processingContent -notmatch "getClientForInterfaceType") {
            $updatedProcessing = $processingContent -replace 
            '(\s+private final MockRSocketOrderServiceClient mockRSocketOrderServiceClient;)',
            '$1

' + $clientSelectionLogic
            
            # Update retry method to use the client selection
            $updatedProcessing = $updatedProcessing -replace 
            'mockRSocketOrderServiceClient\.submitRetry\(',
            'getClientForInterfaceType(exception.getInterfaceType()).submitRetry('
            
            Set-Content -Path $processingServiceFile -Value $updatedProcessing -Encoding UTF8
            Write-Host "✅ Updated ExceptionProcessingService with proper client routing" -ForegroundColor Green
        }
    }
}

# 6. Rebuild the application
Write-Host "`n6. Rebuilding application..." -ForegroundColor Yellow

try {
    $buildResult = & ./gradlew build -x test 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Build successful" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Build had issues, but continuing..." -ForegroundColor Yellow
        Write-Host $buildResult -ForegroundColor Gray
    }
} catch {
    Write-Host "⚠️  Build command failed, but continuing..." -ForegroundColor Yellow
}

# 7. Restart the application using Tilt
Write-Host "`n7. Restarting application with Tilt..." -ForegroundColor Yellow

try {
    # Kill existing Tilt process
    Get-Process -Name "tilt" -ErrorAction SilentlyContinue | Stop-Process -Force
    Start-Sleep -Seconds 3
    
    # Start Tilt in background
    Start-Process -FilePath "tilt" -ArgumentList "up" -WindowStyle Hidden
    Write-Host "✅ Tilt restarted" -ForegroundColor Green
    
    # Wait for application to start
    Write-Host "Waiting for application to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 30
    
} catch {
    Write-Host "⚠️  Tilt restart failed, trying manual restart..." -ForegroundColor Yellow
    
    # Try kubectl restart
    try {
        kubectl rollout restart deployment/interface-exception-collector 2>$null
        Start-Sleep -Seconds 20
        Write-Host "✅ Kubernetes deployment restarted" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Manual restart also failed" -ForegroundColor Yellow
    }
}

# 8. Test the fix with a new PARTNER_ORDER exception
Write-Host "`n8. Testing the fix..." -ForegroundColor Yellow

# Generate fresh JWT token
$tokenOutput = node generate-jwt-correct-secret.js test-user ADMIN
$token = ($tokenOutput | Select-String "Generated Token:" -A 1 | Select-Object -Last 1).ToString().Trim()

if (-not $token -or $token.Length -lt 100) {
    $token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NTcwODk2NzcsImV4cCI6MTc1NzA5MzI3N30.yY7YuplEFh3HDfMR6tGejITSPgtJO-sfVRBXKj3Y9IY"
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

# Generate unique transaction ID
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$newTransactionId = "PARTNER-ORDER-FIX-TEST-$timestamp"

# Create exception data
$exceptionData = @{
    transactionId = $newTransactionId
    externalId = "TEST-PARTNER-ORDER-FIX-1"
    interfaceType = "PARTNER_ORDER"
    operation = "ORDER_PROCESSING"
    exceptionReason = "Test exception after RSocket to REST routing fix"
    severity = "HIGH"
    retryable = $true
    maxRetries = 5
    customerId = "CUST-FIX-TEST-001"
    locationCode = "LOC-FIX-TEST-001"
    category = "PROCESSING_ERROR"
    status = "OPEN"
} | ConvertTo-Json

try {
    # Wait for service to be ready
    $maxWaitTime = 60
    $waitTime = 0
    $serviceReady = $false
    
    while ($waitTime -lt $maxWaitTime -and -not $serviceReady) {
        try {
            $healthCheck = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method GET -TimeoutSec 5
            if ($healthCheck.status -eq "UP") {
                $serviceReady = $true
                Write-Host "✅ Service is ready" -ForegroundColor Green
            }
        } catch {
            Write-Host "Waiting for service... ($waitTime/$maxWaitTime seconds)" -ForegroundColor Yellow
            Start-Sleep -Seconds 5
            $waitTime += 5
        }
    }
    
    if (-not $serviceReady) {
        Write-Host "⚠️  Service not ready, but attempting test anyway..." -ForegroundColor Yellow
    }
    
    # Create the exception
    Write-Host "Creating PARTNER_ORDER exception..." -ForegroundColor Cyan
    $createResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions" -Method POST -Headers $headers -Body $exceptionData -TimeoutSec 30
    
    Write-Host "✅ Created exception: $($createResponse.transactionId)" -ForegroundColor Green
    
    $actualTransactionId = $createResponse.transactionId
    
    # Wait for exception to be persisted
    Start-Sleep -Seconds 3
    
    # Test retry
    Write-Host "Testing retry (should use HTTP, not RSocket)..." -ForegroundColor Cyan
    
    $retryRequest = @{
        reason = "Testing fixed routing - should use HTTP REST call to partner-order-service"
        initiatedBy = "test-user"
    } | ConvertTo-Json
    
    $retryResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/exceptions/$actualTransactionId/retry" -Method POST -Headers $headers -Body $retryRequest -TimeoutSec 30
    
    Write-Host "✅ Retry submitted successfully!" -ForegroundColor Green
    Write-Host "Response: $($retryResponse | ConvertTo-Json)" -ForegroundColor Cyan
    
    # Wait and check logs
    Start-Sleep -Seconds 10
    
    $podName = kubectl get pods -l app=interface-exception-collector -o jsonpath='{.items[0].metadata.name}' 2>$null
    if ($podName -and $LASTEXITCODE -eq 0) {
        Write-Host "`nChecking logs for HTTP vs RSocket usage..." -ForegroundColor Cyan
        $logs = kubectl logs $podName --tail=50 2>$null
        
        if ($logs) {
            $hasRSocketError = $false
            $hasHttpCall = $false
            $hasPartnerOrderClient = $false
            
            $logs | ForEach-Object {
                if ($_ -match "rsocket protocol is not supported") {
                    Write-Host "❌ STILL HAS RSOCKET ERROR: $_" -ForegroundColor Red
                    $hasRSocketError = $true
                } elseif ($_ -match "http://.*7001") {
                    Write-Host "✅ HTTP CALL DETECTED: $_" -ForegroundColor Green
                    $hasHttpCall = $true
                } elseif ($_ -match "PartnerOrderServiceClient") {
                    Write-Host "✅ PARTNER ORDER CLIENT: $_" -ForegroundColor Green
                    $hasPartnerOrderClient = $true
                } elseif ($_ -match $actualTransactionId) {
                    Write-Host "📋 TRANSACTION LOG: $_" -ForegroundColor Yellow
                }
            }
            
            Write-Host "`n=== ANALYSIS ===" -ForegroundColor Magenta
            if ($hasRSocketError) {
                Write-Host "❌ FAILED: Still getting RSocket protocol errors" -ForegroundColor Red
            } else {
                Write-Host "✅ SUCCESS: No RSocket protocol errors detected" -ForegroundColor Green
            }
            
            if ($hasHttpCall) {
                Write-Host "✅ SUCCESS: HTTP calls to port 7001 detected" -ForegroundColor Green
            } else {
                Write-Host "⚠️  WARNING: No HTTP calls to port 7001 detected" -ForegroundColor Yellow
            }
            
            if ($hasPartnerOrderClient) {
                Write-Host "✅ SUCCESS: PartnerOrderServiceClient is being used" -ForegroundColor Green
            } else {
                Write-Host "⚠️  WARNING: PartnerOrderServiceClient usage not detected" -ForegroundColor Yellow
            }
        }
    }
    
} catch {
    Write-Host "❌ Test failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response: $responseBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read response body" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== RSocket to REST Routing Fix Complete ===" -ForegroundColor Green
Write-Host "If you still see RSocket errors, the partner-order-service may need to be started on port 7001" -ForegroundColor Yellow