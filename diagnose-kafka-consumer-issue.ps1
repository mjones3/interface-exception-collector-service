#!/usr/bin/env pwsh

# Diagnose Kafka Consumer Issue Script
# This script diagnoses why Kafka consumers are not registering

Write-Host "=== Kafka Consumer Diagnostic ===" -ForegroundColor Green

# Navigate to project directory
Set-Location "interface-exception-collector"

Write-Host "Step 1: Checking if application is running..." -ForegroundColor Cyan
try {
    $healthResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($healthResponse.StatusCode -eq 200) {
        Write-Host "Application is running on port 8080" -ForegroundColor Green
        $healthData = $healthResponse.Content | ConvertFrom-Json
        Write-Host "  Health Status: $($healthData.status)" -ForegroundColor Gray
    } else {
        Write-Host "Application health check failed" -ForegroundColor Red
    }
} catch {
    Write-Host "Application is not running or not accessible on port 8080" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Step 2: Checking Kafka health..." -ForegroundColor Cyan
try {
    $kafkaHealthResponse = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health/kafka" -TimeoutSec 5 -ErrorAction SilentlyContinue
    if ($kafkaHealthResponse.StatusCode -eq 200) {
        $kafkaHealth = $kafkaHealthResponse.Content | ConvertFrom-Json
        Write-Host "Kafka health endpoint accessible" -ForegroundColor Green
        Write-Host "  Kafka Status: $($kafkaHealth.status)" -ForegroundColor Gray
    }
} catch {
    Write-Host "Kafka health check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "Step 3: Checking consumer classes..." -ForegroundColor Cyan
$consumerDir = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/kafka/consumer"
if (Test-Path $consumerDir) {
    $consumers = Get-ChildItem $consumerDir -Filter "*.java"
    Write-Host "Found $($consumers.Count) consumer classes:" -ForegroundColor Green
    foreach ($consumer in $consumers) {
        $content = Get-Content $consumer.FullName -Raw
        if ($content -match "@Component") {
            $componentStatus = "YES"
        } else {
            $componentStatus = "NO"
        }
        if ($content -match "@KafkaListener") {
            $listenerStatus = "YES"
        } else {
            $listenerStatus = "NO"
        }
        Write-Host "  $($consumer.BaseName): Component=$componentStatus KafkaListener=$listenerStatus" -ForegroundColor Gray
    }
} else {
    Write-Host "Consumer directory not found" -ForegroundColor Red
}

Write-Host "Step 4: Checking Kafka configuration..." -ForegroundColor Cyan
$kafkaConfigFile = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/kafka/config/KafkaConsumerConfig.java"
if (Test-Path $kafkaConfigFile) {
    Write-Host "KafkaConsumerConfig.java exists" -ForegroundColor Green
    $configContent = Get-Content $kafkaConfigFile -Raw
    if ($configContent -match "@EnableKafka") {
        Write-Host "@EnableKafka annotation found" -ForegroundColor Green
    } else {
        Write-Host "@EnableKafka annotation missing" -ForegroundColor Red
    }
} else {
    Write-Host "KafkaConsumerConfig.java not found" -ForegroundColor Red
}

Write-Host "Step 5: Checking application.yml..." -ForegroundColor Cyan
$appConfigFile = "src/main/resources/application.yml"
if (Test-Path $appConfigFile) {
    $configContent = Get-Content $appConfigFile -Raw
    if ($configContent -match "bootstrap-servers:") {
        Write-Host "Bootstrap servers configured" -ForegroundColor Green
    } else {
        Write-Host "Bootstrap servers not configured" -ForegroundColor Red
    }
    if ($configContent -match "group-id:.*interface-exception-collector") {
        Write-Host "Consumer group ID configured" -ForegroundColor Green
    } else {
        Write-Host "Consumer group ID issue" -ForegroundColor Red
    }
} else {
    Write-Host "application.yml not found" -ForegroundColor Red
}

Write-Host "Step 6: Testing Kafka connectivity..." -ForegroundColor Cyan
try {
    $kafkaHost = "localhost"
    $kafkaPort = 9092
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connectTask = $tcpClient.ConnectAsync($kafkaHost, $kafkaPort)
    $connectTask.Wait(3000)
    if ($tcpClient.Connected) {
        Write-Host "Kafka is accessible at ${kafkaHost}:${kafkaPort}" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "Cannot connect to Kafka at ${kafkaHost}:${kafkaPort}" -ForegroundColor Red
    }
} catch {
    Write-Host "Kafka connectivity test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== DIAGNOSTIC SUMMARY ===" -ForegroundColor Green
Write-Host "Common issues and solutions:" -ForegroundColor Yellow
Write-Host "1. Kafka broker not running - Start Kafka on localhost:9092" -ForegroundColor Yellow
Write-Host "2. Application not started - Start the Spring Boot application" -ForegroundColor Yellow
Write-Host "3. Consumer not registered - Check @Component and @EnableKafka" -ForegroundColor Yellow
Write-Host "4. Topic doesn't exist - Create OrderRejected topic in Kafka" -ForegroundColor Yellow
Write-Host ""
Write-Host "To fix the consumer registration issue:" -ForegroundColor Cyan
Write-Host "- Ensure Kafka is running" -ForegroundColor Cyan
Write-Host "- Restart the application" -ForegroundColor Cyan
Write-Host "- Check application startup logs for errors" -ForegroundColor Cyan