#!/usr/bin/env pwsh

# Quick start script for GraphQL subscription testing

Write-Host "GraphQL Subscriptions Quick Start" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Check if we're in the right directory
if (-not (Test-Path "interface-exception-collector")) {
    Write-Host "❌ Error: interface-exception-collector directory not found." -ForegroundColor Red
    Write-Host "Please run this script from the project root directory." -ForegroundColor Yellow
    exit 1
}

Write-Host "`n📋 Prerequisites Check:" -ForegroundColor Yellow

# Check Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java not found. Please install Java 17 or later." -ForegroundColor Red
    exit 1
}

# Check Maven
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "✅ Maven: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven not found. Please install Maven." -ForegroundColor Red
    exit 1
}

Write-Host "`n🚀 Starting Services:" -ForegroundColor Cyan

# Function to start service in new window
function Start-ServiceInNewWindow {
    param($ServiceName, $Directory, $Command)
    
    Write-Host "Starting $ServiceName..." -ForegroundColor Yellow
    
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = "powershell.exe"
    $startInfo.Arguments = "-NoExit -Command `"cd '$Directory'; $Command`""
    $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal
    
    try {
        $process = [System.Diagnostics.Process]::Start($startInfo)
        Write-Host "✅ $ServiceName started in new window (PID: $($process.Id))" -ForegroundColor Green
        return $process
    } catch {
        Write-Host "❌ Failed to start $ServiceName`: $_" -ForegroundColor Red
        return $null
    }
}

# Start interface-exception-collector
$collectorDir = Join-Path $PWD "interface-exception-collector"
$collectorProcess = Start-ServiceInNewWindow "Interface Exception Collector" $collectorDir "mvn spring-boot:run -Dspring-boot.run.profiles=local"

if (-not $collectorProcess) {
    Write-Host "❌ Failed to start Interface Exception Collector" -ForegroundColor Red
    exit 1
}

Write-Host "`n⏳ Waiting for service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Test if service is running
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✅ Interface Exception Collector is running!" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Service might still be starting. Check the service window." -ForegroundColor Yellow
}

Write-Host "`n🧪 Testing Options:" -ForegroundColor Cyan
Write-Host "1. GraphiQL (Recommended): http://localhost:8080/graphiql" -ForegroundColor White
Write-Host "2. HTML Test Client: Open graphql-subscription-test.html in your browser" -ForegroundColor White
Write-Host "3. Node.js Client: node test-subscriptions.js (requires: npm install ws)" -ForegroundColor White
Write-Host "4. PowerShell Guide: .\test-graphql-subscriptions.ps1" -ForegroundColor White

Write-Host "`n📊 Quick Test - GraphQL Health Check:" -ForegroundColor Green
try {
    $healthQuery = @{
        query = "{ systemHealth { status } }"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "http://localhost:8080/graphql" -Method POST -Body $healthQuery -ContentType "application/json"
    Write-Host "✅ GraphQL endpoint is working!" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "❌ GraphQL endpoint test failed: $_" -ForegroundColor Red
}

Write-Host "`n🎯 Next Steps:" -ForegroundColor Magenta
Write-Host "1. Open GraphiQL: http://localhost:8080/graphiql" -ForegroundColor White
Write-Host "2. Try this subscription in the GraphiQL interface:" -ForegroundColor White

$sampleSubscription = @"
subscription {
  exceptionUpdated {
    eventType
    exception {
      transactionId
      status
      severity
    }
    timestamp
  }
}
"@

Write-Host $sampleSubscription -ForegroundColor Gray

Write-Host "`n3. In another tab, trigger events by creating exceptions" -ForegroundColor White
Write-Host "4. Watch real-time updates in your subscription!" -ForegroundColor White

Write-Host "`n🛑 To stop services:" -ForegroundColor Red
Write-Host "- Close the service windows or press Ctrl+C in them" -ForegroundColor White
Write-Host "- Or run: Get-Process | Where-Object {`$_.ProcessName -eq 'java'} | Stop-Process" -ForegroundColor White

Write-Host "`n🎉 Ready to test GraphQL subscriptions!" -ForegroundColor Green
Write-Host "Check the service window for startup logs and any errors." -ForegroundColor Yellow

# Open GraphiQL in default browser
try {
    Start-Process "http://localhost:8080/graphiql"
    Write-Host "`n🌐 GraphiQL opened in your default browser!" -ForegroundColor Green
} catch {
    Write-Host "`n💡 Manually open: http://localhost:8080/graphiql" -ForegroundColor Yellow
}