#!/usr/bin/env pwsh

# Fix Kafka Topic Creation Script
# This script creates the missing OrderRejected topic and restarts the application

Write-Host "=== Kafka Topic Creation Fix ===" -ForegroundColor Green

Write-Host "Step 1: Checking if Kafka is running..." -ForegroundColor Cyan
try {
    $kafkaHost = "localhost"
    $kafkaPort = 9092
    $tcpClient = New-Object System.Net.Sockets.TcpClient
    $connectTask = $tcpClient.ConnectAsync($kafkaHost, $kafkaPort)
    $connectTask.Wait(3000)
    if ($tcpClient.Connected) {
        Write-Host "Kafka is running at ${kafkaHost}:${kafkaPort}" -ForegroundColor Green
        $tcpClient.Close()
    } else {
        Write-Host "Kafka is not accessible. Please start Kafka first." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Cannot connect to Kafka: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Please ensure Kafka is running on localhost:9092" -ForegroundColor Yellow
    exit 1
}

Write-Host "Step 2: Creating Kafka topics..." -ForegroundColor Cyan

# Define topics that need to be created
$topics = @(
    "OrderRejected",
    "OrderCancelled", 
    "CollectionRejected",
    "DistributionFailed",
    "ValidationError"
)

# Check if kafka-topics command is available
$kafkaTopicsCmd = $null
$possiblePaths = @(
    "kafka-topics",
    "kafka-topics.bat",
    "C:\kafka\bin\windows\kafka-topics.bat",
    "C:\kafka_2.13-3.0.0\bin\windows\kafka-topics.bat",
    ".\kafka\bin\windows\kafka-topics.bat"
)

foreach ($path in $possiblePaths) {
    try {
        $result = & $path --version 2>$null
        if ($LASTEXITCODE -eq 0) {
            $kafkaTopicsCmd = $path
            Write-Host "Found Kafka topics command: $path" -ForegroundColor Green
            break
        }
    } catch {
        # Continue to next path
    }
}

if (-not $kafkaTopicsCmd) {
    Write-Host "Kafka topics command not found. Trying alternative approach..." -ForegroundColor Yellow
    Write-Host "Please manually create these topics in Kafka:" -ForegroundColor Yellow
    foreach ($topic in $topics) {
        Write-Host "  - $topic" -ForegroundColor Cyan
    }
    Write-Host ""
    Write-Host "Example command:" -ForegroundColor Gray
    Write-Host "kafka-topics --create --topic OrderRejected --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1" -ForegroundColor Gray
} else {
    # Create topics
    foreach ($topic in $topics) {
        Write-Host "Creating topic: $topic" -ForegroundColor Yellow
        try {
            & $kafkaTopicsCmd --create --topic $topic --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 --if-not-exists
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  Topic $topic created successfully" -ForegroundColor Green
            } else {
                Write-Host "  Topic $topic creation failed or already exists" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "  Error creating topic $topic : $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
    Write-Host "Step 3: Listing existing topics..." -ForegroundColor Cyan
    try {
        & $kafkaTopicsCmd --list --bootstrap-server localhost:9092
    } catch {
        Write-Host "Could not list topics: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Step 4: Restarting application to register consumers..." -ForegroundColor Cyan

# Navigate to project directory
Set-Location "interface-exception-collector"

# Stop application
try {
    $processes = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
    if ($processes) {
        foreach ($pid in $processes) {
            Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
            Write-Host "Stopped process $pid" -ForegroundColor Yellow
        }
        Start-Sleep -Seconds 5
    }
} catch {
    Write-Host "No processes to stop on port 8080" -ForegroundColor Gray
}

# Start application
Write-Host "Starting application..." -ForegroundColor Cyan
if (Test-Path "mvnw.cmd") {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && .\mvnw.cmd spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
} elseif (Test-Path "mvnw") {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && .\mvnw spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
} else {
    Start-Process -FilePath "cmd" -ArgumentList "/c", "cd /d `"$PWD`" && mvn spring-boot:run -Dspring-boot.run.jvmArguments=`"-Dspring.profiles.active=local`"" -WindowStyle Minimized
}

Write-Host "Step 5: Waiting for application startup..." -ForegroundColor Cyan
$maxAttempts = 20
$attempt = 0

do {
    Start-Sleep -Seconds 3
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host "Application started successfully!" -ForegroundColor Green
            break
        }
    } catch {
        Write-Host "Attempt $attempt/$maxAttempts - Application starting..." -ForegroundColor Gray
    }
} while ($attempt -lt $maxAttempts)

if ($attempt -ge $maxAttempts) {
    Write-Host "Application startup timeout. Check logs for errors." -ForegroundColor Red
} else {
    Write-Host ""
    Write-Host "=== KAFKA TOPIC CREATION COMPLETE ===" -ForegroundColor Green
    Write-Host "Topics created (if Kafka tools were available):" -ForegroundColor Green
    foreach ($topic in $topics) {
        Write-Host "  - $topic" -ForegroundColor Cyan
    }
    Write-Host ""
    Write-Host "Application restarted. Consumers should now register with Kafka." -ForegroundColor Green
    Write-Host "Check your Kafka monitoring tools for the consumer group: interface-exception-collector" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Verify topics exist in Kafka" -ForegroundColor Cyan
Write-Host "2. Check consumer group registration" -ForegroundColor Cyan
Write-Host "3. Test by publishing an OrderRejected event" -ForegroundColor Cyan