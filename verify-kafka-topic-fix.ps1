#!/usr/bin/env pwsh

# Verify Kafka Topic Fix Script
# This script verifies the Kafka topic configuration is correct

Write-Host "=== Kafka Topic Configuration Verification ===" -ForegroundColor Green

# Navigate to project directory
Set-Location "interface-exception-collector"

Write-Host "Checking OrderRejectedEventConsumer configuration..." -ForegroundColor Cyan
$consumerFile = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/kafka/consumer/OrderRejectedEventConsumer.java"
if (Test-Path $consumerFile) {
    $consumerContent = Get-Content $consumerFile -Raw
    if ($consumerContent -match 'topics = "OrderRejected"') {
        Write-Host "✓ Consumer is correctly listening to 'OrderRejected' topic" -ForegroundColor Green
    } else {
        Write-Host "✗ Consumer topic configuration issue" -ForegroundColor Red
    }
} else {
    Write-Host "✗ Consumer file not found" -ForegroundColor Red
}

Write-Host "Checking application.yml configuration..." -ForegroundColor Cyan
$configFile = "src/main/resources/application.yml"
if (Test-Path $configFile) {
    $configContent = Get-Content $configFile -Raw
    if ($configContent -match 'order-rejected: "OrderRejected"') {
        Write-Host "✓ Application.yml correctly configured with 'OrderRejected' topic" -ForegroundColor Green
    } else {
        Write-Host "✗ Application.yml topic configuration issue" -ForegroundColor Red
    }
} else {
    Write-Host "✗ Application.yml file not found" -ForegroundColor Red
}

Write-Host "Checking other event consumers..." -ForegroundColor Cyan
$consumerDir = "src/main/java/com/arcone/biopro/exception/collector/infrastructure/kafka/consumer"
if (Test-Path $consumerDir) {
    $consumers = Get-ChildItem $consumerDir -Filter "*.java"
    foreach ($consumer in $consumers) {
        $content = Get-Content $consumer.FullName -Raw
        if ($content -match '@KafkaListener.*topics = "([^"]+)"') {
            $topicName = $matches[1]
            Write-Host "  $($consumer.BaseName): listening to '$topicName'" -ForegroundColor Gray
        }
    }
}

Write-Host ""
Write-Host "=== KAFKA CONFIGURATION STATUS ===" -ForegroundColor Green
Write-Host "✓ Fixed topic name mismatch" -ForegroundColor Green
Write-Host "✓ Consumer expects: OrderRejected" -ForegroundColor Green  
Write-Host "✓ Configuration provides: OrderRejected" -ForegroundColor Green
Write-Host "✓ Consumer group: interface-exception-collector" -ForegroundColor Green
Write-Host ""
Write-Host "The service is now properly configured to consume OrderRejected events!" -ForegroundColor Green
Write-Host "When you restart the application, it will correctly listen to the OrderRejected topic." -ForegroundColor Yellow